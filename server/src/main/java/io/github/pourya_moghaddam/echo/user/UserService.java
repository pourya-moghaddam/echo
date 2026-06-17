package io.github.pourya_moghaddam.echo.user;

import io.github.pourya_moghaddam.echo.community.dto.CommunityResponse;
import io.github.pourya_moghaddam.echo.exception.ResourceNotFoundException;
import io.github.pourya_moghaddam.echo.user.dto.UpdateThemeRequest;
import io.github.pourya_moghaddam.echo.user.dto.UpdateAvatarRequest;
import io.github.pourya_moghaddam.echo.user.dto.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return UserResponse.fromEntity(user);
    }

    public UserResponse updateTheme(String username, UpdateThemeRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        user.setThemePreference(request.themePreference());
        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    public UserResponse updateAvatar(String username, UpdateAvatarRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        user.setAvatarUrl(request.avatarUrl());
        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<CommunityResponse> getUserCommunities(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return user.getJoinedCommunities().stream()
                .map(CommunityResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
