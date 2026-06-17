package io.github.pourya_moghaddam.echo.auth.dto;

import io.github.pourya_moghaddam.echo.user.dto.UserResponse;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
