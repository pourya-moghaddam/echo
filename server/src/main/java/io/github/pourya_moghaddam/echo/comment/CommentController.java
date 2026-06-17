package io.github.pourya_moghaddam.echo.comment;

import io.github.pourya_moghaddam.echo.comment.dto.CommentCreateRequest;
import io.github.pourya_moghaddam.echo.comment.dto.CommentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createComment(@PathVariable Long postId,
                                         @Valid @RequestBody CommentCreateRequest request,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        return commentService.createComment(postId, request, userDetails.getUsername());
    }

    @PostMapping("/comments/{commentId}/reply")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse replyToComment(@PathVariable Long commentId,
                                          @Valid @RequestBody CommentCreateRequest request,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        return commentService.replyToComment(commentId, request, userDetails.getUsername());
    }

    @GetMapping("/posts/{postId}/comments")
    public List<CommentResponse> getPostComments(@PathVariable Long postId) {
        return commentService.getPostComments(postId);
    }
}
