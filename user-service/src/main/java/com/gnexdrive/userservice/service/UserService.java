package com.gnexdrive.userservice.service;

import com.gnexdrive.userservice.dto.AuthResponse;
import com.gnexdrive.userservice.dto.LoginRequest;
import com.gnexdrive.userservice.dto.SignupRequest;
import com.gnexdrive.userservice.dto.UserDto;

/**
 * User service interface
 */
public interface UserService {

    /**
     * Register new user
     */
    AuthResponse signup(SignupRequest request);

    /**
     * Authenticate user and generate token
     */
    AuthResponse login(LoginRequest request);

    /**
     * Get user by ID
     */
    UserDto getUserById(String userId);

    /**
     * Get user by username
     */
    UserDto getUserByUsername(String username);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
}
