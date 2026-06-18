package io.github.pourya_moghaddam.echo.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pourya_moghaddam.echo.TestcontainersConfiguration;
import io.github.pourya_moghaddam.echo.auth.dto.LoginRequest;
import io.github.pourya_moghaddam.echo.community.CommunityCategory;
import io.github.pourya_moghaddam.echo.community.dto.CreateCommunityRequest;
import io.github.pourya_moghaddam.echo.post.dto.PostCreateRequest;
import io.github.pourya_moghaddam.echo.user.User;
import io.github.pourya_moghaddam.echo.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.jdbc.core.JdbcTemplate;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class PostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ObjectMapper objectMapper;
    private String userToken;
    private String user2Token;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        jdbcTemplate.execute("TRUNCATE TABLE posts, user_communities, communities, users RESTART IDENTITY CASCADE");

        // Create main user
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setUsername("user1");
        user1.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user1);

        // Create second user
        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setUsername("user2");
        user2.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user2);

        userToken = getJwtToken("user1", "password");
        user2Token = getJwtToken("user2", "password");

        // Create a community using user1
        CreateCommunityRequest communityReq = new CreateCommunityRequest("java", "Java devs", CommunityCategory.PROGRAMMING);
        mockMvc.perform(post("/api/communities")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(communityReq)))
            .andExpect(status().isCreated());
    }

    private String getJwtToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void createPost_validData_returnsCreated() throws Exception {
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle("My first post");
        request.setContent("Hello world!");

        mockMvc.perform(post("/api/communities/java/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("My first post"))
            .andExpect(jsonPath("$.content").value("Hello world!"))
            .andExpect(jsonPath("$.authorUsername").value("user1"))
            .andExpect(jsonPath("$.communityName").value("java"));
    }

    @Test
    void getCommunityPosts_existingCommunity_returnsPaginatedPosts() throws Exception {
        // Create 2 posts
        PostCreateRequest request1 = new PostCreateRequest();
        request1.setTitle("Post 1");
        mockMvc.perform(post("/api/communities/java/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        PostCreateRequest request2 = new PostCreateRequest();
        request2.setTitle("Post 2");
        mockMvc.perform(post("/api/communities/java/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        mockMvc.perform(get("/api/communities/java/posts")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[0].title").value("Post 2")) // latest first
            .andExpect(jsonPath("$.content[1].title").value("Post 1"));
    }

    @Test
    void getFeed_userJoinedCommunity_returnsPostsFromJoinedCommunities() throws Exception {
        // user2 joins "java"
        mockMvc.perform(post("/api/communities/java/join")
                .header("Authorization", "Bearer " + user2Token));

        // Create a post in "java"
        PostCreateRequest request1 = new PostCreateRequest();
        request1.setTitle("Java Post");
        mockMvc.perform(post("/api/communities/java/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        // Create another community "spring" and user1 posts in it, user2 doesn't join
        CreateCommunityRequest communityReq = new CreateCommunityRequest("spring", "Spring devs", CommunityCategory.PROGRAMMING);
        mockMvc.perform(post("/api/communities")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(communityReq)));

        PostCreateRequest request2 = new PostCreateRequest();
        request2.setTitle("Spring Post");
        mockMvc.perform(post("/api/communities/spring/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        mockMvc.perform(get("/api/feed")
                .header("Authorization", "Bearer " + user2Token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].title").value("Java Post"));
    }

    @Test
    void getPopularPosts_returnsPaginatedPosts() throws Exception {
        // Create 2 posts
        PostCreateRequest request1 = new PostCreateRequest();
        request1.setTitle("Post 1");
        mockMvc.perform(post("/api/communities/java/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        PostCreateRequest request2 = new PostCreateRequest();
        request2.setTitle("Post 2");
        mockMvc.perform(post("/api/communities/java/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        mockMvc.perform(get("/api/posts/popular"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[0].title").value("Post 2")) // latest first
            .andExpect(jsonPath("$.content[1].title").value("Post 1"));
    }

    @Test
    void getUserPosts_returnsPaginatedPostsByAuthor() throws Exception {
        PostCreateRequest request1 = new PostCreateRequest();
        request1.setTitle("User1 Post 1");
        mockMvc.perform(post("/api/communities/java/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        PostCreateRequest request2 = new PostCreateRequest();
        request2.setTitle("User1 Post 2");
        mockMvc.perform(post("/api/communities/java/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        PostCreateRequest request3 = new PostCreateRequest();
        request3.setTitle("User2 Post 1");
        mockMvc.perform(post("/api/communities/java/posts")
                .header("Authorization", "Bearer " + user2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request3)));

        mockMvc.perform(get("/api/users/user1/posts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[0].authorUsername").value("user1"))
            .andExpect(jsonPath("$.content[1].authorUsername").value("user1"));
    }

    @Test
    void getPost_existingId_returnsPost() throws Exception {
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle("Specific Post");
        request.setContent("Specific Content");

        String response = mockMvc.perform(post("/api/communities/java/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        long postId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/posts/" + postId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(postId))
            .andExpect(jsonPath("$.title").value("Specific Post"))
            .andExpect(jsonPath("$.content").value("Specific Content"))
            .andExpect(jsonPath("$.authorUsername").value("user1"))
            .andExpect(jsonPath("$.communityName").value("java"));
    }

    @Test
    void getPost_nonExistingId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/posts/99999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getPost_withUserVote_returnsUserVote() throws Exception {
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle("Vote Post");
        request.setContent("Vote Content");

        String response = mockMvc.perform(post("/api/communities/java/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        long postId = objectMapper.readTree(response).get("id").asLong();

        // user2 votes up
        io.github.pourya_moghaddam.echo.vote.dto.VoteRequest voteReq = new io.github.pourya_moghaddam.echo.vote.dto.VoteRequest();
        voteReq.setDirection(io.github.pourya_moghaddam.echo.vote.VoteDirection.UP);

        mockMvc.perform(post("/api/posts/" + postId + "/vote")
                .header("Authorization", "Bearer " + user2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voteReq)))
            .andExpect(status().isOk());

        // user2 fetches the post and sees userVote = "up"
        mockMvc.perform(get("/api/posts/" + postId)
                .header("Authorization", "Bearer " + user2Token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(postId))
            .andExpect(jsonPath("$.userVote").value("up"));
            
        // user1 fetches the post and sees userVote = null (or absent)
        mockMvc.perform(get("/api/posts/" + postId)
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(postId))
            .andExpect(jsonPath("$.userVote").doesNotExist());
    }
}
