package io.github.pourya_moghaddam.echo.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostCreateRequest {
    @NotBlank
    private String title;
    private String content;
}
