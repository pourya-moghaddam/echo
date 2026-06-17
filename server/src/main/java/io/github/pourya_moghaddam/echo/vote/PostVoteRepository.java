package io.github.pourya_moghaddam.echo.vote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, Long> {
    Optional<PostVote> findByUserIdAndPostId(Long userId, Long postId);
}
