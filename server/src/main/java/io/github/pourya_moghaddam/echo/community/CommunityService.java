package io.github.pourya_moghaddam.echo.community;

import io.github.pourya_moghaddam.echo.community.dto.CommunityResponse;
import io.github.pourya_moghaddam.echo.community.dto.CreateCommunityRequest;
import io.github.pourya_moghaddam.echo.exception.DuplicateResourceException;
import io.github.pourya_moghaddam.echo.exception.ResourceNotFoundException;
import io.github.pourya_moghaddam.echo.search.SearchService;
import io.github.pourya_moghaddam.echo.user.User;
import io.github.pourya_moghaddam.echo.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    @Autowired
    private SearchService searchService;

    public CommunityService(CommunityRepository communityRepository, UserRepository userRepository) {
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CommunityResponse createCommunity(CreateCommunityRequest request, String creatorUsername) {
        if (communityRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Community name is already taken");
        }

        User creator = userRepository.findByUsernameIgnoreCase(creatorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Community community = new Community();
        community.setName(request.name());
        community.setDescription(request.description());
        community.setCategory(request.category());
        community.setCreator(creator);

        creator.joinCommunity(community);

        Community saved = communityRepository.save(community);
        searchService.indexCommunity(saved);
        return CommunityResponse.fromEntity(saved);
    }

    @Cacheable(value = "communities", key = "#name")
    @Transactional(readOnly = true)
    public CommunityResponse getCommunityByName(String name) {
        Community community = communityRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));
        return CommunityResponse.fromEntity(community);
    }

    @Transactional(readOnly = true)
    public java.util.List<CommunityResponse> getAllCommunities() {
        return communityRepository.findAll().stream()
                .map(CommunityResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void joinCommunity(String name, String username) {
        Community community = communityRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getJoinedCommunities().contains(community)) {
            user.joinCommunity(community);
            userRepository.save(user);
        }
    }

    @Transactional
    public void leaveCommunity(String name, String username) {
        Community community = communityRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getJoinedCommunities().contains(community)) {
            user.leaveCommunity(community);
            userRepository.save(user);
        }
    }
}
