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

        List<Comment> allComments = commentRepository.findAllByPostIdWithAuthor(postId);

        java.util.Map<Long, List<Comment>> commentsByParentId = allComments.stream()
                .filter(c -> c.getParentComment() != null)
                .collect(Collectors.groupingBy(c -> c.getParentComment().getId()));

        return allComments.stream()
                .filter(c -> c.getParentComment() == null)
                .map(c -> buildCommentTree(c, commentsByParentId))
                .collect(Collectors.toList());
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorUsername(comment.getAuthor().getUsername())
                .authorAvatar(comment.getAuthor().getAvatarUrl())
                .postId(comment.getPost().getId())
                .score(comment.getScore())
                .createdAt(comment.getCreatedAt())
                .replies(List.of())
                .build();
    }

    private CommentResponse buildCommentTree(Comment comment, java.util.Map<Long, List<Comment>> commentsByParentId) {
        List<CommentResponse> replies = commentsByParentId.getOrDefault(comment.getId(), List.of())
                .stream()
                .map(child -> buildCommentTree(child, commentsByParentId))
                .collect(Collectors.toList());

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorUsername(comment.getAuthor().getUsername())
                .authorAvatar(comment.getAuthor().getAvatarUrl())
                .postId(comment.getPost().getId())
                .score(comment.getScore())
                .createdAt(comment.getCreatedAt())
                .replies(replies)
                .build();
    }
}
