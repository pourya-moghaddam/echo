package io.github.pourya_moghaddam.echo.post.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String authorUsername;
    private String communityName;
    private Integer score;
    private LocalDateTime createdAt;
}
