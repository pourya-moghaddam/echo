package io.github.pourya_moghaddam.echo.user.dto;

import io.github.pourya_moghaddam.echo.user.ThemePreference;
import jakarta.validation.constraints.NotNull;

public record UpdateThemeRequest(
        @NotNull(message = "Theme preference is required")
        ThemePreference themePreference
) {}
