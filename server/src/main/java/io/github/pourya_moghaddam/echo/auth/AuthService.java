package io.github.pourya_moghaddam.echo.auth;

import io.github.pourya_moghaddam.echo.auth.dto.AuthResponse;
import io.github.pourya_moghaddam.echo.auth.dto.LoginRequest;
import io.github.pourya_moghaddam.echo.auth.dto.SignupRequest;
import io.github.pourya_moghaddam.echo.exception.DuplicateResourceException;
import io.github.pourya_moghaddam.echo.security.JwtService;
import io.github.pourya_moghaddam.echo.security.TokenBlacklistService;
import io.github.pourya_moghaddam.echo.user.User;
import io.github.pourya_moghaddam.echo.user.UserRepository;
import io.github.pourya_moghaddam.echo.user.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class AuthService {

    private static final List<String> AVATARS = List.of(
            "avatar1.png", "avatar2.png", "avatar3.png", "avatar4.png", "avatar5.png",
            "avatar6.png", "avatar7.png", "avatar8.png", "avatar9.png", "avatar10.png"
    );

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final Random random;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       TokenBlacklistService tokenBlacklistService) {
        this(userRepository, passwordEncoder, jwtService, tokenBlacklistService, new Random());
    }

    // Visible for testing
    AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                TokenBlacklistService tokenBlacklistService, Random random) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.random = random;
    }

    public AuthResponse signup(SignupRequest request) {
        String lowerEmail = request.email().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(lowerEmail)) {
            throw new DuplicateResourceException("Email is already taken");
        }
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new DuplicateResourceException("Username is already taken");
        }

        User user = new User();
        user.setEmail(lowerEmail);
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setAvatarUrl(AVATARS.get(random.nextInt(AVATARS.size())));

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved.getUsername());

        return new AuthResponse(token, UserResponse.fromEntity(saved));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(request.usernameOrEmail())
                .or(() -> userRepository.findByEmailIgnoreCase(request.usernameOrEmail()))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token, UserResponse.fromEntity(user));
    }

    public void logout(String token) {
        tokenBlacklistService.blacklistToken(token);
    }
}
