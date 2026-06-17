package io.github.pourya_moghaddam.echo.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateAvatarRequest(
        @NotBlank(message = "Avatar URL must not be blank")
        String avatarUrl
) {}
