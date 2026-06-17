package io.github.pourya_moghaddam.echo.comment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private String authorUsername;
    private Long postId;
    private Integer score;
    private LocalDateTime createdAt;
    private List<CommentResponse> replies;
}
