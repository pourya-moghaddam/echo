package io.github.pourya_moghaddam.echo.community;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    Optional<Community> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
