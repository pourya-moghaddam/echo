package io.github.pourya_moghaddam.echo.post;

import io.github.pourya_moghaddam.echo.post.dto.PostCreateRequest;
import io.github.pourya_moghaddam.echo.post.dto.PostResponse;
import io.github.pourya_moghaddam.echo.vote.VoteService;
import io.github.pourya_moghaddam.echo.vote.dto.VoteRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final VoteService voteService;

    @PostMapping("/communities/{name}/posts")
    public ResponseEntity<PostResponse> createPost(
            @PathVariable String name,
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
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
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getFeed(userDetails.getUsername(), page, size));
    }

    @GetMapping("/posts/popular")
    public ResponseEntity<Page<PostResponse>> getPopularPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getPopularPosts(page, size));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    @PostMapping("/posts/{postId}/vote")
    public ResponseEntity<Void> votePost(@PathVariable Long postId,
                                         @Valid @RequestBody VoteRequest request,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        voteService.votePost(postId, request, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
