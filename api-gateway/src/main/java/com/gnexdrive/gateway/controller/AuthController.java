package com.gnexdrive.gateway.controller;

import com.gnexdrive.gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Test/Development controller for JWT token generation
 * IMPORTANT: Remove or secure this in production!
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    /**
     * Generate a test JWT token
     * For development/testing purposes only
     */
    @PostMapping("/generate-token")
    public ResponseEntity<Map<String, String>> generateToken(
            @RequestParam(defaultValue = "user123") String userId,
            @RequestParam(defaultValue = "testuser") String username,
            @RequestParam(defaultValue = "test@example.com") String email) {
        
        log.info("Generating test token for user: {}", userId);
        
        String token = jwtUtil.generateToken(userId, username, email);
        
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", userId);
        response.put("username", username);
        response.put("email", email);
        response.put("type", "Bearer");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Validate a JWT token
     */
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Token is required"));
        }
        
        boolean isValid = jwtUtil.validateToken(token);
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);
        
        if (isValid) {
            response.put("userId", jwtUtil.extractUserId(token));
            response.put("username", jwtUtil.extractUsername(token));
            response.put("email", jwtUtil.extractEmail(token));
            response.put("expired", jwtUtil.isTokenExpired(token));
        }
        
        return ResponseEntity.ok(response);
    }
}
