package com.gnexdrive.gateway.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testGenerateAndValidateToken() {
        // Generate token
        String token = jwtUtil.generateToken("user123", "testuser", "test@example.com");
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // Validate token
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testExtractUserId() {
        String token = jwtUtil.generateToken("user123", "testuser", "test@example.com");
        String userId = jwtUtil.extractUserId(token);
        
        assertEquals("user123", userId);
    }

    @Test
    void testExtractUsername() {
        String token = jwtUtil.generateToken("user123", "testuser", "test@example.com");
        String username = jwtUtil.extractUsername(token);
        
        assertEquals("testuser", username);
    }

    @Test
    void testExtractEmail() {
        String token = jwtUtil.generateToken("user123", "testuser", "test@example.com");
        String email = jwtUtil.extractEmail(token);
        
        assertEquals("test@example.com", email);
    }

    @Test
    void testExtractClaims() {
        String token = jwtUtil.generateToken("user123", "testuser", "test@example.com");
        Claims claims = jwtUtil.extractClaims(token);
        
        assertNotNull(claims);
        assertEquals("user123", claims.getSubject());
        assertEquals("user123", claims.get("userId"));
        assertEquals("testuser", claims.get("username"));
        assertEquals("test@example.com", claims.get("email"));
    }

    @Test
    void testTokenNotExpired() {
        String token = jwtUtil.generateToken("user123", "testuser", "test@example.com");
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void testInvalidToken() {
        String invalidToken = "invalid.token.here";
        assertFalse(jwtUtil.validateToken(invalidToken));
    }
}
