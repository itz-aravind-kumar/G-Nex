package com.gnexdrive.userservice.controller;

import com.gnexdrive.common.dto.ApiResponse;
import com.gnexdrive.userservice.dto.AuthResponse;
import com.gnexdrive.userservice.dto.LoginRequest;
import com.gnexdrive.userservice.dto.SignupRequest;
import com.gnexdrive.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication operations")
public class AuthController {

    private final UserService userService;

    @Operation(summary = "Register new user")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Signup request received for username: {}", request.getUsername());

        AuthResponse response = userService.signup(request);

        ApiResponse<AuthResponse> apiResponse = ApiResponse.success(
                "User registered successfully", response);

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @Operation(summary = "Login user")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for: {}", request.getUsernameOrEmail());

        AuthResponse response = userService.login(request);

        ApiResponse<AuthResponse> apiResponse = ApiResponse.success(
                "Login successful", response);

        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Check username availability")
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);

        ApiResponse<Boolean> response = ApiResponse.success(
                exists ? "Username taken" : "Username available",
                !exists
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Check email availability")
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);

        ApiResponse<Boolean> response = ApiResponse.success(
                exists ? "Email already registered" : "Email available",
                !exists
        );

        return ResponseEntity.ok(response);
    }
}
