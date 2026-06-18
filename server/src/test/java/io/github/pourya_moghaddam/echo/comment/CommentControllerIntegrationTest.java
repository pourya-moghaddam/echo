package io.github.pourya_moghaddam.echo.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pourya_moghaddam.echo.TestcontainersConfiguration;
import io.github.pourya_moghaddam.echo.auth.dto.LoginRequest;
import io.github.pourya_moghaddam.echo.comment.dto.CommentCreateRequest;
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
class CommentControllerIntegrationTest {

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
    private Long testPostId;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        jdbcTemplate.execute("TRUNCATE TABLE comments, posts, user_communities, communities, users RESTART IDENTITY CASCADE");

        // Create main user
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setUsername("user1");
        user1.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user1);

        userToken = getJwtToken("user1", "password");

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
    void createComment_validData_returnsCreatedComment() throws Exception {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("First comment!");

        mockMvc.perform(post("/api/posts/" + testPostId + "/comments")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.content").value("First comment!"))
            .andExpect(jsonPath("$.authorUsername").value("user1"))
            .andExpect(jsonPath("$.postId").value(testPostId))
            .andExpect(jsonPath("$.postTitle").value("Test Post"))
            .andExpect(jsonPath("$.communityName").value("java"));
    }

    @Test
    void replyToComment_validData_returnsCreatedReply() throws Exception {
        // Create parent comment
        CommentCreateRequest parentReq = new CommentCreateRequest();
        parentReq.setContent("Parent comment");
        String parentResponse = mockMvc.perform(post("/api/posts/" + testPostId + "/comments")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parentReq)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long parentId = objectMapper.readTree(parentResponse).get("id").asLong();

        // Create reply
        CommentCreateRequest replyReq = new CommentCreateRequest();
        replyReq.setContent("This is a reply");

        mockMvc.perform(post("/api/comments/" + parentId + "/reply")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(replyReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.content").value("This is a reply"))
            .andExpect(jsonPath("$.postId").value(testPostId));
    }

    @Test
    void getPostComments_returnsNestedComments() throws Exception {
        // Create parent comment
        CommentCreateRequest parentReq = new CommentCreateRequest();
        parentReq.setContent("Parent comment");
        String parentResponse = mockMvc.perform(post("/api/posts/" + testPostId + "/comments")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parentReq)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long parentId = objectMapper.readTree(parentResponse).get("id").asLong();

        // Create reply
        CommentCreateRequest replyReq = new CommentCreateRequest();
        replyReq.setContent("This is a reply");
        mockMvc.perform(post("/api/comments/" + parentId + "/reply")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(replyReq)))
            .andExpect(status().isCreated());

        // Get comments for post
        mockMvc.perform(get("/api/posts/" + testPostId + "/comments")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1))) // 1 top level comment
            .andExpect(jsonPath("$[0].content").value("Parent comment"))
            .andExpect(jsonPath("$[0].replies", hasSize(1))) // 1 reply
            .andExpect(jsonPath("$[0].replies[0].content").value("This is a reply"));
    }

    @Test
    void getPostComments_withUserVote_returnsUserVote() throws Exception {
        // Create parent comment
        CommentCreateRequest parentReq = new CommentCreateRequest();
        parentReq.setContent("Vote comment");
        String parentResponse = mockMvc.perform(post("/api/posts/" + testPostId + "/comments")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parentReq)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long commentId = objectMapper.readTree(parentResponse).get("id").asLong();

        // user1 votes down
        io.github.pourya_moghaddam.echo.vote.dto.VoteRequest voteReq = new io.github.pourya_moghaddam.echo.vote.dto.VoteRequest();
        voteReq.setDirection(io.github.pourya_moghaddam.echo.vote.VoteDirection.DOWN);

        mockMvc.perform(post("/api/comments/" + commentId + "/vote")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voteReq)))
            .andExpect(status().isOk());

        // user1 fetches comments and sees userVote = "down"
        mockMvc.perform(get("/api/posts/" + testPostId + "/comments")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(commentId))
            .andExpect(jsonPath("$[0].userVote").value("down"));
    }

    @Test
    void getUserComments_returnsPaginatedCommentsByAuthor() throws Exception {
        CommentCreateRequest req1 = new CommentCreateRequest();
        req1.setContent("User1 Comment 1");
        mockMvc.perform(post("/api/posts/" + testPostId + "/comments")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)));

        CommentCreateRequest req2 = new CommentCreateRequest();
        req2.setContent("User1 Comment 2");
        mockMvc.perform(post("/api/posts/" + testPostId + "/comments")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)));

        mockMvc.perform(get("/api/users/user1/comments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[0].authorUsername").value("user1"))
            .andExpect(jsonPath("$.content[1].authorUsername").value("user1"));
    }
}
