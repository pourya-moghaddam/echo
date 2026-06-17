package io.github.pourya_moghaddam.echo.community.dto;

import io.github.pourya_moghaddam.echo.community.Community;
import io.github.pourya_moghaddam.echo.community.CommunityCategory;

import java.time.LocalDateTime;

public record CommunityResponse(
        Long id,
        String name,
        String description,
        CommunityCategory category,
        String creatorUsername,
        int memberCount,
        LocalDateTime createdAt
) {
    public static CommunityResponse fromEntity(Community community) {
        return new CommunityResponse(
                community.getId(),
                community.getName(),
                community.getDescription(),
                community.getCategory(),
                community.getCreator() != null ? community.getCreator().getUsername() : null,
                community.getMembers() != null ? community.getMembers().size() : 0,
                community.getCreatedAt()
        );
    }
}
