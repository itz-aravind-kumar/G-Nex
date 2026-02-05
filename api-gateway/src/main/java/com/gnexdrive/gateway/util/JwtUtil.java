package com.gnexdrive.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * JWT utility for token validation and parsing
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationAndValidation12345678}")
    private String secretKey;

    /**
     * Get the signing key from secret
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extract all claims from token
     */
    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Failed to extract claims from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract a specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractClaims(token);
        return claims != null ? claimsResolver.apply(claims) : null;
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractClaims(token);
            if (claims == null) {
                return false;
            }
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract user ID from token
     */
    public String extractUserId(String token) {
        Claims claims = extractClaims(token);
        if (claims == null) {
            return null;
        }
        // Try to get userId from claims, fallback to subject
        Object userId = claims.get("userId");
        if (userId != null) {
            return userId.toString();
        }
        return claims.getSubject();
    }

    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        Claims claims = extractClaims(token);
        if (claims == null) {
            return null;
        }
        // Try to get username from claims
        Object username = claims.get("username");
        if (username != null) {
            return username.toString();
        }
        // Fallback to subject if username not present
        return claims.getSubject();
    }

    /**
     * Extract email from token
     */
    public String extractEmail(String token) {
        Claims claims = extractClaims(token);
        if (claims == null) {
            return null;
        }
        Object email = claims.get("email");
        return email != null ? email.toString() : null;
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            log.error("Failed to check token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Generate a token for testing purposes
     */
    public String generateToken(String userId, String username, String email) {
        return Jwts.builder()
                .subject(userId)
                .claim("userId", userId)
                .claim("username", username)
                .claim("email", email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                .signWith(getSigningKey())
                .compact();
    }
}
