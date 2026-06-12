package io.github.pourya_moghaddam.echo.community.dto;

import io.github.pourya_moghaddam.echo.community.CommunityCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCommunityRequest(
        @NotBlank(message = "Community name is required")
        @Size(min = 3, max = 21, message = "Community name must be between 3 and 21 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$",
                message = "Community name can only contain letters, numbers, and underscores")
        String name,

        @NotBlank(message = "Description is required")
        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        @jakarta.validation.constraints.NotNull(message = "Category is required")
        CommunityCategory category
) {
}
