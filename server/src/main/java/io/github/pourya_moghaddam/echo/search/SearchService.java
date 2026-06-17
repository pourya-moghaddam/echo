package io.github.pourya_moghaddam.echo.search;

import io.github.pourya_moghaddam.echo.community.Community;
import io.github.pourya_moghaddam.echo.post.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final CommunitySearchRepository communitySearchRepository;
    private final PostSearchRepository postSearchRepository;

    public void indexCommunity(Community community) {
        CommunityDocument doc = CommunityDocument.builder()
                .id(community.getId().toString())
                .name(community.getName())
                .description(community.getDescription())
                .category(community.getCategory().name())
                .build();
        communitySearchRepository.save(doc);
    }

    public void indexPost(Post post) {
        PostDocument doc = PostDocument.builder()
                .id(post.getId().toString())
                .title(post.getTitle())
                .content(post.getContent())
                .communityName(post.getCommunity().getName())
                .authorUsername(post.getAuthor().getUsername())
                .build();
        postSearchRepository.save(doc);
    }

    public List<CommunityDocument> searchCommunities(String query) {
        return communitySearchRepository.findByNameMatchesOrDescriptionMatches(query, query);
    }

    public List<PostDocument> searchPosts(String query) {
        return postSearchRepository.findByTitleMatchesOrContentMatches(query, query);
    }
}
