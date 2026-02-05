package com.gnexdrive.gateway.util;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * JWT utility for token validation and parsing
 */
@Slf4j
@Component
public class JwtUtil {

    private static final String SECRET_KEY = "your-secret-key"; // TODO: Move to configuration

    public Claims extractClaims(String token) {
        // TODO: Implement JWT claims extraction
        return null;
    }

    public boolean validateToken(String token) {
        // TODO: Implement JWT token validation
        return false;
    }

    public String extractUserId(String token) {
        // TODO: Implement user ID extraction from token
        return null;
    }

    public String extractUsername(String token) {
        // TODO: Implement username extraction from token
        return null;
    }

    public boolean isTokenExpired(String token) {
        // TODO: Implement token expiration check
        return false;
    }
}
