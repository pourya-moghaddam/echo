package io.github.pourya_moghaddam.echo.user;

import io.github.pourya_moghaddam.echo.exception.ResourceNotFoundException;
import io.github.pourya_moghaddam.echo.user.dto.UpdateThemeRequest;
import io.github.pourya_moghaddam.echo.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    // --- getCurrentUser Tests ---

    @Test
    void getCurrentUser_existingUser_returnsUserResponse() {
        User user = createTestUser();
        when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(user));

        UserResponse response = userService.getCurrentUser("testuser");

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.avatarUrl()).isEqualTo("avatar1.png");
        assertThat(response.themePreference()).isEqualTo(ThemePreference.LIGHT);
    }

    @Test
    void getCurrentUser_nonExistentUser_throwsResourceNotFoundException() {
        when(userRepository.findByUsernameIgnoreCase("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser("ghost"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ghost");
    }

    // --- updateTheme Tests ---

    @Test
    void updateTheme_validRequest_returnsUpdatedTheme() {
        User user = createTestUser();
        when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateThemeRequest request = new UpdateThemeRequest(ThemePreference.DARK);
        UserResponse response = userService.updateTheme("testuser", request);

        assertThat(response.themePreference()).isEqualTo(ThemePreference.DARK);
    }

    @Test
    void updateTheme_nonExistentUser_throwsResourceNotFoundException() {
        when(userRepository.findByUsernameIgnoreCase("ghost")).thenReturn(Optional.empty());

        UpdateThemeRequest request = new UpdateThemeRequest(ThemePreference.DARK);
        assertThatThrownBy(() -> userService.updateTheme("ghost", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ghost");
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
