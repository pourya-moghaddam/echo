package io.github.pourya_moghaddam.echo.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pourya_moghaddam.echo.TestcontainersConfiguration;
import io.github.pourya_moghaddam.echo.security.JwtService;
import io.github.pourya_moghaddam.echo.user.dto.UpdateThemeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String validToken;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        userRepository.deleteAll();

        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setAvatarUrl("avatar1.png");
        user.setThemePreference(ThemePreference.LIGHT);
        userRepository.save(user);

        validToken = jwtService.generateToken("testuser");
    }

    // --- GET /api/users/me Tests ---

    @Test
    void getMe_authenticated_returnsUserData() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.avatarUrl").value("avatar1.png"))
                .andExpect(jsonPath("$.themePreference").value("LIGHT"));
    }

    @Test
    void getMe_unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // --- PUT /api/users/theme Tests ---

    @Test
    void updateTheme_authenticated_returnsUpdatedTheme() throws Exception {
        UpdateThemeRequest request = new UpdateThemeRequest(ThemePreference.DARK);

        mockMvc.perform(put("/api/users/theme")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.themePreference").value("DARK"));
    }

    @Test
    void updateTheme_unauthenticated_returnsUnauthorized() throws Exception {
        UpdateThemeRequest request = new UpdateThemeRequest(ThemePreference.DARK);

        mockMvc.perform(put("/api/users/theme")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
