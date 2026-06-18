package io.github.pourya_moghaddam.echo.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByCommunityName(String name, Pageable pageable);

    Page<Post> findByCommunityIdIn(Collection<Long> communityIds, Pageable pageable);

    Page<Post> findByAuthorUsernameIgnoreCase(String username, Pageable pageable);
}
