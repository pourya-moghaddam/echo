package io.github.pourya_moghaddam.echo.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final JwtService jwtService;
    private final Map<String, Date> blacklistedTokens = new ConcurrentHashMap<>();

    public TokenBlacklistService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public void blacklistToken(String token) {
        try {
            Date expiration = jwtService.extractExpiration(token);
            blacklistedTokens.put(token, expiration);
        } catch (Exception e) {
            // Token is invalid or already expired
        }
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    @Scheduled(fixedRate = 3600000)
    public void cleanUpBlacklist() {
        Date now = new Date();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
    }
}
