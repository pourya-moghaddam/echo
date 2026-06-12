package io.github.pourya_moghaddam.echo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // A sufficiently long secret for HMAC-SHA256 (min 256 bits / 32 bytes)
        String secret = "testSecretKeyThatIsAtLeast256BitsLongForHmacSha!!";
        long expirationMs = 86400000; // 24 hours
        jwtService = new JwtService(secret, expirationMs);
    }

    @Test
    void generateToken_validUsername_returnsNonNullToken() {
        String token = jwtService.generateToken("testuser");
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void extractUsername_validToken_returnsCorrectUsername() {
        String token = jwtService.generateToken("testuser");
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void isTokenValid_validTokenAndMatchingUser_returnsTrue() {
        String token = jwtService.generateToken("testuser");
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_validTokenAndDifferentUser_returnsFalse() {
        String token = jwtService.generateToken("testuser");
        UserDetails userDetails = new User("otheruser", "password", Collections.emptyList());
        assertThat(jwtService.isTokenValid(token, userDetails)).isFalse();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        // Create a service with 0ms expiration so the token is instantly expired
        JwtService expiredService = new JwtService(
                "testSecretKeyThatIsAtLeast256BitsLongForHmacSha!!", 0);
        String token = expiredService.generateToken("testuser");
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());
        assertThat(expiredService.isTokenValid(token, userDetails)).isFalse();
    }
}
