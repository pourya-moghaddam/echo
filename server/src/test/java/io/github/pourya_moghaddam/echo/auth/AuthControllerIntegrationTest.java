package io.github.pourya_moghaddam.echo.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pourya_moghaddam.echo.TestcontainersConfiguration;
import io.github.pourya_moghaddam.echo.auth.dto.LoginRequest;
import io.github.pourya_moghaddam.echo.auth.dto.SignupRequest;
import io.github.pourya_moghaddam.echo.user.User;
import io.github.pourya_moghaddam.echo.user.UserRepository;
import io.github.pourya_moghaddam.echo.community.CommunityRepository;
import io.github.pourya_moghaddam.echo.post.PostRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommunityRepository communityRepository;
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        jdbcTemplate.execute("TRUNCATE TABLE posts, user_communities, communities, users RESTART IDENTITY CASCADE");
    }

    // --- Signup Tests ---

    @Test
    void signup_validRequest_returnsCreatedWithTokenAndUser() throws Exception {
        SignupRequest request = new SignupRequest("newuser@example.com", "newuser", "password123");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.user.username").value("newuser"))
                .andExpect(jsonPath("$.user.avatarUrl").isNotEmpty())
                .andExpect(jsonPath("$.user.themePreference").value("LIGHT"));
    }

    @Test
    void signup_duplicateEmail_returnsConflict() throws Exception {
        // Pre-create a user with the same email
        createExistingUser("existing@example.com", "existinguser");

        SignupRequest request = new SignupRequest("existing@example.com", "differentuser", "password123");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email is already taken"));
    }

    @Test
    void signup_duplicateUsername_returnsConflict() throws Exception {
        createExistingUser("unique@example.com", "takenuser");

        SignupRequest request = new SignupRequest("another@example.com", "takenuser", "password123");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username is already taken"));
    }

    @Test
    void signup_invalidEmail_returnsBadRequest() throws Exception {
        SignupRequest request = new SignupRequest("not-an-email", "testuser", "password123");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void signup_shortPassword_returnsBadRequest() throws Exception {
        SignupRequest request = new SignupRequest("test@example.com", "testuser", "short");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    // --- Login Tests ---

    @Test
    void login_validCredentialsByUsername_returnsOkWithToken() throws Exception {
        createExistingUser("test@example.com", "testuser");

        LoginRequest request = new LoginRequest("testuser", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.username").value("testuser"));
    }

    @Test
    void login_validCredentialsByEmail_returnsOkWithToken() throws Exception {
        createExistingUser("test@example.com", "testuser");

        LoginRequest request = new LoginRequest("test@example.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    void login_wrongPassword_returnsUnauthorized() throws Exception {
        createExistingUser("test@example.com", "testuser");

        LoginRequest request = new LoginRequest("testuser", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void login_nonExistentUser_returnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest("nobody", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // --- Logout Tests ---

    @Test
    void logout_authenticated_returnsOkAndInvalidatesToken() throws Exception {
        createExistingUser("logout@example.com", "logoutuser");

        LoginRequest loginRequest = new LoginRequest("logoutuser", "password123");
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    private void createExistingUser(String email, String username) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setAvatarUrl("avatar1.png");
        userRepository.save(user);
    }
}
