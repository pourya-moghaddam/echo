package io.github.pourya_moghaddam.echo.vote;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pourya_moghaddam.echo.TestcontainersConfiguration;
import io.github.pourya_moghaddam.echo.auth.dto.LoginRequest;
import io.github.pourya_moghaddam.echo.comment.dto.CommentCreateRequest;
import io.github.pourya_moghaddam.echo.community.CommunityCategory;
import io.github.pourya_moghaddam.echo.community.dto.CreateCommunityRequest;
import io.github.pourya_moghaddam.echo.post.dto.PostCreateRequest;
import io.github.pourya_moghaddam.echo.user.User;
import io.github.pourya_moghaddam.echo.user.UserRepository;
import io.github.pourya_moghaddam.echo.vote.dto.VoteRequest;
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

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class VoteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private ObjectMapper objectMapper;
    private String userToken;
    private String user2Token;
    private Long testPostId;
    private Long testCommentId;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        jdbcTemplate.execute("TRUNCATE TABLE comment_votes, post_votes, comments, posts, user_communities, communities, users RESTART IDENTITY CASCADE");

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

        // Create a community
        CreateCommunityRequest communityReq = new CreateCommunityRequest("java", "Java devs", CommunityCategory.PROGRAMMING);
        mockMvc.perform(post("/api/communities")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(communityReq)))
            .andExpect(status().isCreated());

        // Create a post
        PostCreateRequest postReq = new PostCreateRequest();
        postReq.setTitle("Test Post");
        postReq.setContent("Post Content");
        String postResponse = mockMvc.perform(post("/api/communities/java/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postReq)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        testPostId = objectMapper.readTree(postResponse).get("id").asLong();

        // Create a comment
        CommentCreateRequest commentReq = new CommentCreateRequest();
        commentReq.setContent("First comment!");
        String commentResponse = mockMvc.perform(post("/api/posts/" + testPostId + "/comments")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentReq)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        testCommentId = objectMapper.readTree(commentResponse).get("id").asLong();
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
    void votePost_validRequest_updatesScore() throws Exception {
        VoteRequest request = new VoteRequest();
        request.setDirection(VoteDirection.UP);

        mockMvc.perform(post("/api/posts/" + testPostId + "/vote")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        // Check feed for updated score
        mockMvc.perform(get("/api/feed")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].score").value(1));

        // Change vote to DOWN
        request.setDirection(VoteDirection.DOWN);
        mockMvc.perform(post("/api/posts/" + testPostId + "/vote")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/feed")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].score").value(-1));
    }

    @Test
    void voteComment_validRequest_updatesScore() throws Exception {
        VoteRequest request = new VoteRequest();
        request.setDirection(VoteDirection.UP);

        mockMvc.perform(post("/api/comments/" + testCommentId + "/vote")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        // Check comments for updated score
        mockMvc.perform(get("/api/posts/" + testPostId + "/comments")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].score").value(1));
    }

    @Test
    void feed_sortedByScore() throws Exception {
        // Create second post
        PostCreateRequest postReq2 = new PostCreateRequest();
        postReq2.setTitle("Second Post");
        String postResponse2 = mockMvc.perform(post("/api/communities/java/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postReq2)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        Long testPostId2 = objectMapper.readTree(postResponse2).get("id").asLong();

        // user1 and user2 vote UP on post 2
        VoteRequest request = new VoteRequest();
        request.setDirection(VoteDirection.UP);

        mockMvc.perform(post("/api/posts/" + testPostId2 + "/vote")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/posts/" + testPostId2 + "/vote")
                .header("Authorization", "Bearer " + user2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        // Check feed
        mockMvc.perform(get("/api/feed")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].title").value("Second Post")) // Has score 2
            .andExpect(jsonPath("$.content[0].score").value(2))
            .andExpect(jsonPath("$.content[1].title").value("Test Post")) // Has score 0
            .andExpect(jsonPath("$.content[1].score").value(0));
    }
}
