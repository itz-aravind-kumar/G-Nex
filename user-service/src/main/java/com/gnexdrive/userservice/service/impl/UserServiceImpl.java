package com.gnexdrive.userservice.service.impl;

import com.gnexdrive.userservice.dto.AuthResponse;
import com.gnexdrive.userservice.dto.LoginRequest;
import com.gnexdrive.userservice.dto.SignupRequest;
import com.gnexdrive.userservice.dto.UserDto;
import com.gnexdrive.userservice.entity.User;
import com.gnexdrive.userservice.exception.AuthenticationException;
import com.gnexdrive.userservice.exception.ResourceNotFoundException;
import com.gnexdrive.userservice.exception.UserAlreadyExistsException;
import com.gnexdrive.userservice.repository.UserRepository;
import com.gnexdrive.userservice.service.UserService;
import com.gnexdrive.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        log.info("Signup attempt for username: {}, email: {}", request.getUsername(), request.getEmail());

        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken: " + request.getUsername());
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .isActive(true)
                .role(User.Role.USER)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully with ID: {}", user.getUserId());

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .type("Bearer")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getUsernameOrEmail());

        // Find user by username or email
        User user = userRepository.findByUsernameOrEmail(
                        request.getUsernameOrEmail(),
                        request.getUsernameOrEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid username/email or password"));

        // Check if user is active
        if (!user.getIsActive()) {
            throw new AuthenticationException("Account is inactive");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Failed login attempt for: {}", request.getUsernameOrEmail());
            throw new AuthenticationException("Invalid username/email or password");
        }

        log.info("User logged in successfully: {}", user.getUsername());

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .type("Bearer")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return convertToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return convertToDto(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .isActive(user.getIsActive())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
