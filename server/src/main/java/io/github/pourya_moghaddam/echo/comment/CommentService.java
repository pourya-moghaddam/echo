package io.github.pourya_moghaddam.echo.comment;

import io.github.pourya_moghaddam.echo.comment.dto.CommentCreateRequest;
import io.github.pourya_moghaddam.echo.comment.dto.CommentResponse;
import io.github.pourya_moghaddam.echo.exception.ResourceNotFoundException;
import io.github.pourya_moghaddam.echo.post.Post;
import io.github.pourya_moghaddam.echo.post.PostRepository;
import io.github.pourya_moghaddam.echo.user.User;
import io.github.pourya_moghaddam.echo.user.UserRepository;
import io.github.pourya_moghaddam.echo.vote.CommentVoteRepository;
import io.github.pourya_moghaddam.echo.vote.CommentVote;
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
    private final CommentVoteRepository commentVoteRepository;

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
        return mapToResponse(comment, username);
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
        return mapToResponse(comment, username);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getPostComments(Long postId, String currentUsername) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found");
        }

        List<Comment> allComments = commentRepository.findAllByPostIdWithAuthor(postId);

        java.util.Map<Long, List<Comment>> commentsByParentId = allComments.stream()
                .filter(c -> c.getParentComment() != null)
                .collect(Collectors.groupingBy(c -> c.getParentComment().getId()));

        return allComments.stream()
                .filter(c -> c.getParentComment() == null)
                .map(c -> buildCommentTree(c, commentsByParentId, currentUsername))
                .collect(Collectors.toList());
    }

    private CommentResponse mapToResponse(Comment comment, String currentUsername) {
        CommentResponse response = CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorUsername(comment.getAuthor().getUsername())
                .authorAvatar(comment.getAuthor().getAvatarUrl())
                .postId(comment.getPost().getId())
                .score(comment.getScore())
                .createdAt(comment.getCreatedAt())
                .replies(List.of())
                .build();

        if (currentUsername != null) {
            userRepository.findByUsernameIgnoreCase(currentUsername).ifPresent(user -> {
                commentVoteRepository.findByUserIdAndCommentId(user.getId(), comment.getId())
                        .ifPresent(vote -> response.setUserVote(vote.getDirection().name().toLowerCase()));
            });
        }
        return response;
    }

    private CommentResponse buildCommentTree(Comment comment, java.util.Map<Long, List<Comment>> commentsByParentId, String currentUsername) {
        List<CommentResponse> replies = commentsByParentId.getOrDefault(comment.getId(), List.of())
                .stream()
                .map(child -> buildCommentTree(child, commentsByParentId, currentUsername))
                .collect(Collectors.toList());

        CommentResponse response = CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorUsername(comment.getAuthor().getUsername())
                .authorAvatar(comment.getAuthor().getAvatarUrl())
                .postId(comment.getPost().getId())
                .score(comment.getScore())
                .createdAt(comment.getCreatedAt())
                .replies(replies)
                .build();

        if (currentUsername != null) {
            userRepository.findByUsernameIgnoreCase(currentUsername).ifPresent(user -> {
                commentVoteRepository.findByUserIdAndCommentId(user.getId(), comment.getId())
                        .ifPresent(vote -> response.setUserVote(vote.getDirection().name().toLowerCase()));
            });
        }
        return response;
    }
}
