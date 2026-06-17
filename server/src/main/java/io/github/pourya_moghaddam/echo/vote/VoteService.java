package io.github.pourya_moghaddam.echo.vote;

import io.github.pourya_moghaddam.echo.comment.Comment;
import io.github.pourya_moghaddam.echo.comment.CommentRepository;
import io.github.pourya_moghaddam.echo.exception.ResourceNotFoundException;
import io.github.pourya_moghaddam.echo.post.Post;
import io.github.pourya_moghaddam.echo.post.PostRepository;
import io.github.pourya_moghaddam.echo.user.User;
import io.github.pourya_moghaddam.echo.user.UserRepository;
import io.github.pourya_moghaddam.echo.vote.dto.VoteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoteService {
    private final PostVoteRepository postVoteRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional
    public void votePost(Long postId, VoteRequest request, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<PostVote> existingVoteOpt = postVoteRepository.findByUserIdAndPostId(user.getId(), post.getId());

        int scoreChange = 0;

        if (existingVoteOpt.isPresent()) {
            PostVote existingVote = existingVoteOpt.get();
            if (existingVote.getDirection() != request.getDirection()) {
                scoreChange = request.getDirection().getValue() - existingVote.getDirection().getValue();
                existingVote.setDirection(request.getDirection());
                postVoteRepository.save(existingVote);
            }
        } else {
            PostVote newVote = new PostVote();
            newVote.setPost(post);
            newVote.setUser(user);
            newVote.setDirection(request.getDirection());
            postVoteRepository.save(newVote);
            scoreChange = request.getDirection().getValue();
        }

        if (scoreChange != 0) {
            post.setScore(post.getScore() + scoreChange);
            postRepository.save(post);
        }
    }

    @Transactional
    public void voteComment(Long commentId, VoteRequest request, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<CommentVote> existingVoteOpt = commentVoteRepository.findByUserIdAndCommentId(user.getId(), comment.getId());

        int scoreChange = 0;

        if (existingVoteOpt.isPresent()) {
            CommentVote existingVote = existingVoteOpt.get();
            if (existingVote.getDirection() != request.getDirection()) {
                scoreChange = request.getDirection().getValue() - existingVote.getDirection().getValue();
                existingVote.setDirection(request.getDirection());
                commentVoteRepository.save(existingVote);
            }
        } else {
            CommentVote newVote = new CommentVote();
            newVote.setComment(comment);
            newVote.setUser(user);
            newVote.setDirection(request.getDirection());
            commentVoteRepository.save(newVote);
            scoreChange = request.getDirection().getValue();
        }

        if (scoreChange != 0) {
            comment.setScore(comment.getScore() + scoreChange);
            commentRepository.save(comment);
        }
    }
}
