package io.github.pourya_moghaddam.echo.comment;

import io.github.pourya_moghaddam.echo.comment.dto.CommentCreateRequest;
import io.github.pourya_moghaddam.echo.comment.dto.CommentResponse;
import io.github.pourya_moghaddam.echo.vote.VoteService;
import io.github.pourya_moghaddam.echo.vote.dto.VoteRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final VoteService voteService;

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
    public List<CommentResponse> getPostComments(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        return commentService.getPostComments(postId, username);
    }

    @GetMapping("/users/{username}/comments")
    public org.springframework.data.domain.Page<CommentResponse> getUserComments(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        String currentUsername = userDetails != null ? userDetails.getUsername() : null;
        return commentService.getCommentsByAuthor(username, page, size, currentUsername);
    }

    @PostMapping("/comments/{commentId}/vote")
    public ResponseEntity<Void> voteComment(@PathVariable Long commentId,
                                            @Valid @RequestBody VoteRequest request,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        voteService.voteComment(commentId, request, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
