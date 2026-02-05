package com.gnexdrive.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard API response wrapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String requestId;
    
    public static <T> ApiResponse<T> success(T data) {
        // TODO: Implement success response builder
        return null;
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        // TODO: Implement success response builder with message
        return null;
    }
    
    public static <T> ApiResponse<T> error(String message) {
        // TODO: Implement error response builder
        return null;
    }
    
    public static <T> ApiResponse<T> error(String message, T data) {
        // TODO: Implement error response builder with data
        return null;
    }
}
