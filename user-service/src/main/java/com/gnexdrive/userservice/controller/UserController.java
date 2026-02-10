package com.gnexdrive.userservice.controller;

import com.gnexdrive.common.dto.ApiResponse;
import com.gnexdrive.userservice.dto.UserDto;
import com.gnexdrive.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * User management controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile operations")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get user by ID")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable String userId) {
        log.info("Get user request for ID: {}", userId);

        UserDto user = userService.getUserById(userId);

        ApiResponse<UserDto> response = ApiResponse.success(
                "User retrieved successfully", user);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user by username")
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserDto>> getUserByUsername(@PathVariable String username) {
        log.info("Get user request for username: {}", username);

        UserDto user = userService.getUserByUsername(username);

        ApiResponse<UserDto> response = ApiResponse.success(
                "User retrieved successfully", user);

        return ResponseEntity.ok(response);
    }
}
