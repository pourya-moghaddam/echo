package io.github.pourya_moghaddam.echo.post;

import io.github.pourya_moghaddam.echo.community.Community;
import io.github.pourya_moghaddam.echo.community.CommunityRepository;
import io.github.pourya_moghaddam.echo.exception.ResourceNotFoundException;
import io.github.pourya_moghaddam.echo.post.dto.PostCreateRequest;
import io.github.pourya_moghaddam.echo.post.dto.PostResponse;
import io.github.pourya_moghaddam.echo.search.SearchService;
import io.github.pourya_moghaddam.echo.user.User;
import io.github.pourya_moghaddam.echo.user.UserRepository;
import io.github.pourya_moghaddam.echo.vote.PostVoteRepository;
import io.github.pourya_moghaddam.echo.vote.PostVote;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final SearchService searchService;
    private final PostVoteRepository postVoteRepository;

    @Transactional
    public PostResponse createPost(String communityName, PostCreateRequest request, String authorUsername) {
        Community community = communityRepository.findByNameIgnoreCase(communityName)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));
        User author = userRepository.findByUsernameIgnoreCase(authorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(author);
        post.setCommunity(community);

        Post savedPost = postRepository.save(post);
        searchService.indexPost(savedPost);
        return mapToResponse(savedPost, authorUsername);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByCommunity(String communityName, int page, int size, String currentUsername) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "score", "createdAt"));
        return postRepository.findByCommunityName(communityName, pageable)
                .map(p -> mapToResponse(p, currentUsername));
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getFeed(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "score", "createdAt"));
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        var communityIds = user.getJoinedCommunities().stream()
                .map(Community::getId)
                .collect(Collectors.toList());

        if (communityIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return postRepository.findByCommunityIdIn(communityIds, pageable)
                .map(p -> mapToResponse(p, username));
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPopularPosts(int page, int size, String currentUsername) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "score", "createdAt"));
        return postRepository.findAll(pageable).map(p -> mapToResponse(p, currentUsername));
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByAuthor(String username, int page, int size, String currentUsername) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "score", "createdAt"));
        return postRepository.findByAuthorUsernameIgnoreCase(username, pageable).map(p -> mapToResponse(p, currentUsername));
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long id, String currentUsername) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return mapToResponse(post, currentUsername);
    }

    private PostResponse mapToResponse(Post post, String currentUsername) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setAuthorUsername(post.getAuthor().getUsername());
        response.setCommunityName(post.getCommunity().getName());
        response.setScore(post.getScore());
        response.setCreatedAt(post.getCreatedAt());

        if (currentUsername != null) {
            userRepository.findByUsernameIgnoreCase(currentUsername).ifPresent(user -> {
                postVoteRepository.findByUserIdAndPostId(user.getId(), post.getId())
                        .ifPresent(vote -> {
                            if (vote.getDirection() != io.github.pourya_moghaddam.echo.vote.VoteDirection.NONE) {
                                response.setUserVote(vote.getDirection().name().toLowerCase());
                            }
                        });
            });
        }

        return response;
    }
}
