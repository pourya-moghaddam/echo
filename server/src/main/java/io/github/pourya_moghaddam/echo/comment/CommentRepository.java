package io.github.pourya_moghaddam.echo.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.author WHERE c.post.id = :postId ORDER BY c.createdAt DESC")
    List<Comment> findAllByPostIdWithAuthor(@org.springframework.data.repository.query.Param("postId") Long postId);
}
