package io.github.pourya_moghaddam.echo.post;

import io.github.pourya_moghaddam.echo.post.dto.PostCreateRequest;
import io.github.pourya_moghaddam.echo.post.dto.PostResponse;
import io.github.pourya_moghaddam.echo.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final io.github.pourya_moghaddam.echo.vote.VoteService voteService;

    @PostMapping("/communities/{name}/posts")
    public ResponseEntity<PostResponse> createPost(
            @PathVariable String name,
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        PostResponse response = postService.createPost(name, request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/communities/{name}/posts")
    public ResponseEntity<Page<PostResponse>> getCommunityPosts(
            @PathVariable String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getPostsByCommunity(name, page, size));
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<PostResponse>> getFeed(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getFeed(userDetails.getUsername(), page, size));
    }

    @PostMapping("/posts/{postId}/vote")
    public ResponseEntity<Void> votePost(@PathVariable Long postId,
                                         @Valid @RequestBody io.github.pourya_moghaddam.echo.vote.dto.VoteRequest request,
                                         @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        voteService.votePost(postId, request, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
