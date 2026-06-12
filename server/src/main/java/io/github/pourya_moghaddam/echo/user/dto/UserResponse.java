package io.github.pourya_moghaddam.echo.user.dto;

import io.github.pourya_moghaddam.echo.user.ThemePreference;
import io.github.pourya_moghaddam.echo.user.User;

public record UserResponse(
        Long id,
        String email,
        String username,
        String avatarUrl,
        ThemePreference themePreference
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getAvatarUrl(),
                user.getThemePreference()
        );
    }
}
