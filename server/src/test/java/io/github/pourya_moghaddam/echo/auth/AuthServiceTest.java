package io.github.pourya_moghaddam.echo.auth;

import io.github.pourya_moghaddam.echo.auth.dto.AuthResponse;
import io.github.pourya_moghaddam.echo.auth.dto.LoginRequest;
import io.github.pourya_moghaddam.echo.auth.dto.SignupRequest;
import io.github.pourya_moghaddam.echo.exception.DuplicateResourceException;
import io.github.pourya_moghaddam.echo.security.JwtService;
import io.github.pourya_moghaddam.echo.security.TokenBlacklistService;
import io.github.pourya_moghaddam.echo.user.ThemePreference;
import io.github.pourya_moghaddam.echo.user.User;
import io.github.pourya_moghaddam.echo.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Seed Random for deterministic avatar assignment in tests
        Random seededRandom = new Random(42);
        authService = new AuthService(userRepository, passwordEncoder, jwtService, tokenBlacklistService, seededRandom);
    }

    // --- Signup Tests ---

    @Test
    void signup_validRequest_returnsAuthResponseWithToken() {
        SignupRequest request = new SignupRequest("Test@Example.com", "TestUser", "password123");

        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase("TestUser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateToken("TestUser")).thenReturn("jwt-token");

        AuthResponse response = authService.signup(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("test@example.com"); // Email should be lowercased
        assertThat(response.user().username()).isEqualTo("TestUser");      // Username case should be preserved
        assertThat(response.user().avatarUrl()).isNotNull();
    }

    @Test
    void signup_duplicateEmail_throwsDuplicateResourceException() {
        SignupRequest request = new SignupRequest("Taken@Example.com", "newuser", "password123");
        when(userRepository.existsByEmailIgnoreCase("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email is already taken");
    }

    @Test
    void signup_duplicateUsername_throwsDuplicateResourceException() {
        SignupRequest request = new SignupRequest("new@example.com", "TakenUser", "password123");
        when(userRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase("TakenUser")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Username is already taken");
    }

    @Test
    void signup_validRequest_hashesPassword() {
        SignupRequest request = new SignupRequest("test@example.com", "testuser", "password123");

        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedvalue");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateToken(anyString())).thenReturn("token");

        authService.signup(request);

        // Verify the saved user has the hashed password, not the plain one
        org.mockito.Mockito.verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
                user.getPassword().equals("$2a$10$hashedvalue")
        ));
    }

    @Test
    void signup_validRequest_assignsRandomAvatar() {
        SignupRequest request = new SignupRequest("test@example.com", "testuser", "password123");

        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateToken(anyString())).thenReturn("token");

        AuthResponse response = authService.signup(request);

        assertThat(response.user().avatarUrl()).startsWith("avatar").endsWith(".png");
    }

    // --- Login Tests ---

    @Test
    void login_validCredentialsByUsername_returnsAuthResponse() {
        LoginRequest request = new LoginRequest("TestUser", "password123");
        User user = createTestUser();

        when(userRepository.findByUsernameIgnoreCase("TestUser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedpassword")).thenReturn(true);
        when(jwtService.generateToken("testuser")).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().username()).isEqualTo("testuser");
    }

    @Test
    void login_validCredentialsByEmail_returnsAuthResponse() {
        LoginRequest request = new LoginRequest("Test@Example.com", "password123");
        User user = createTestUser();

        when(userRepository.findByUsernameIgnoreCase("Test@Example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("Test@Example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedpassword")).thenReturn(true);
        when(jwtService.generateToken("testuser")).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("test@example.com");
    }

    @Test
    void login_userNotFound_throwsBadCredentialsException() {
        LoginRequest request = new LoginRequest("nobody", "password123");

        when(userRepository.findByUsernameIgnoreCase("nobody")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_wrongPassword_throwsBadCredentialsException() {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        User user = createTestUser();

        when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "hashedpassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("hashedpassword");
        user.setAvatarUrl("avatar1.png");
        user.setThemePreference(ThemePreference.LIGHT);
        return user;
    }
}
