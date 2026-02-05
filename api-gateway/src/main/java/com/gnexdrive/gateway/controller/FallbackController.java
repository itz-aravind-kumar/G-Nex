package com.gnexdrive.gateway.controller;

import com.gnexdrive.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Fallback controller for circuit breaker
 * Provides fallback responses when downstream services are unavailable
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/file-service")
    public ResponseEntity<ApiResponse<String>> fileServiceFallback() {
        log.warn("File service circuit breaker activated");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.<String>builder()
                        .success(false)
                        .message("File service is temporarily unavailable. Please try again later.")
                        .data(null)
                        .build());
    }

    @GetMapping("/metadata-service")
    public ResponseEntity<ApiResponse<String>> metadataServiceFallback() {
        log.warn("Metadata service circuit breaker activated");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.<String>builder()
                        .success(false)
                        .message("Metadata service is temporarily unavailable. Please try again later.")
                        .data(null)
                        .build());
    }

    @GetMapping("/search-service")
    public ResponseEntity<ApiResponse<String>> searchServiceFallback() {
        log.warn("Search service circuit breaker activated");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.<String>builder()
                        .success(false)
                        .message("Search service is temporarily unavailable. Please try again later.")
                        .data(null)
                        .build());
    }

    @GetMapping("/activity-service")
    public ResponseEntity<ApiResponse<String>> activityServiceFallback() {
        log.warn("Activity service circuit breaker activated");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.<String>builder()
                        .success(false)
                        .message("Activity service is temporarily unavailable. Please try again later.")
                        .data(null)
                        .build());
    }
}
