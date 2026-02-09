package com.gnexdrive.metadataservice.controller;

import com.gnexdrive.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check endpoints for monitoring
 */
@Slf4j
@RestController
@Tag(name = "Health", description = "Service health and status")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Operation(summary = "Health check endpoint")
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "metadata-service");
        health.put("timestamp", LocalDateTime.now());
        
        // Check database connection
        try {
            dataSource.getConnection().close();
            health.put("database", "UP");
        } catch (Exception e) {
            log.error("Database health check failed", e);
            health.put("database", "DOWN");
        }
        
        return ResponseEntity.ok(ApiResponse.success(health));
    }

    @Operation(summary = "Service info")
    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, String>>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("service", "metadata-service");
        info.put("version", "1.0.0");
        info.put("description", "File metadata management service");
        info.put("port", "8082");
        return ResponseEntity.ok(ApiResponse.success(info));
    }
}
