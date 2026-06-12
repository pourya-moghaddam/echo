package io.github.pourya_moghaddam.echo.community;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pourya_moghaddam.echo.TestcontainersConfiguration;
import io.github.pourya_moghaddam.echo.auth.dto.LoginRequest;
import io.github.pourya_moghaddam.echo.community.dto.CreateCommunityRequest;
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

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class CommunityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private String userToken;
    private String user2Token;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        jdbcTemplate.execute("TRUNCATE TABLE user_communities, communities, users RESTART IDENTITY CASCADE");

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

        // Get tokens
        userToken = getJwtToken("user1", "password");
        user2Token = getJwtToken("user2", "password");
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
    void createCommunity_validData_returnsCreated() throws Exception {
        CreateCommunityRequest request =
                new CreateCommunityRequest("java", "Java developers", CommunityCategory.PROGRAMMING);

        mockMvc.perform(post("/api/communities")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("java"))
                .andExpect(jsonPath("$.creatorUsername").value("user1"))
                .andExpect(jsonPath("$.memberCount").value(1)); // Creator is automatically a member
    }

    @Test
    void createCommunity_duplicateName_returnsConflict() throws Exception {
        CreateCommunityRequest request =
                new CreateCommunityRequest("java", "Java developers", CommunityCategory.PROGRAMMING);

        // First creation
        mockMvc.perform(post("/api/communities")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Duplicate creation
        mockMvc.perform(post("/api/communities")
                        .header("Authorization", "Bearer " + user2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getCommunity_returnsCommunityDetails() throws Exception {
        // Create community
        CreateCommunityRequest request =
                new CreateCommunityRequest("spring", "Spring boot", CommunityCategory.PROGRAMMING);
        mockMvc.perform(post("/api/communities")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Get community
        mockMvc.perform(get("/api/communities/spring")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("spring"));
    }

    @Test
    void joinAndLeaveCommunity_updatesMemberCount() throws Exception {
        // User1 creates community
        CreateCommunityRequest request = new CreateCommunityRequest("gaming", "Gamers", CommunityCategory.GAMING);
        mockMvc.perform(post("/api/communities")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // User2 joins
        mockMvc.perform(post("/api/communities/gaming/join")
                        .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk());

        // Check member count (User1 and User2) = 2
        mockMvc.perform(get("/api/communities/gaming")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberCount").value(2));

        // User2 leaves
        mockMvc.perform(post("/api/communities/gaming/leave")
                        .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk());

        // Check member count = 1
        mockMvc.perform(get("/api/communities/gaming")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberCount").value(1));
    }

    @Test
    void getUserCommunities_returnsJoinedCommunities() throws Exception {
        CreateCommunityRequest request =
                new CreateCommunityRequest("movies", "Movie lovers", CommunityCategory.ENTERTAINMENT);
        mockMvc.perform(post("/api/communities")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/me/communities")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("movies"));
    }
}
