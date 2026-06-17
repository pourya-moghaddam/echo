package io.github.pourya_moghaddam.echo.comment;

import io.github.pourya_moghaddam.echo.comment.dto.CommentCreateRequest;
import io.github.pourya_moghaddam.echo.comment.dto.CommentResponse;
import io.github.pourya_moghaddam.echo.exception.ResourceNotFoundException;
import io.github.pourya_moghaddam.echo.post.Post;
import io.github.pourya_moghaddam.echo.post.PostRepository;
import io.github.pourya_moghaddam.echo.user.User;
import io.github.pourya_moghaddam.echo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse createComment(Long postId, CommentCreateRequest request, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setPost(post);
        comment.setAuthor(user);

        comment = commentRepository.save(comment);
        return mapToResponse(comment);
    }

    @Transactional
    public CommentResponse replyToComment(Long commentId, CommentCreateRequest request, String username) {
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setPost(parentComment.getPost());
        comment.setAuthor(user);
        comment.setParentComment(parentComment);

        comment = commentRepository.save(comment);
        return mapToResponse(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getPostComments(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found");
        }

        List<Comment> topLevelComments =
                commentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(postId);
        return topLevelComments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CommentResponse mapToResponse(Comment comment) {
        List<CommentResponse> repliesResponse = comment.getReplies() != null ?
                comment.getReplies().stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList()) : List.of();

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorUsername(comment.getAuthor().getUsername())
                .postId(comment.getPost().getId())
                .score(comment.getScore())
                .createdAt(comment.getCreatedAt())
                .replies(repliesResponse)
                .build();
    }
}
