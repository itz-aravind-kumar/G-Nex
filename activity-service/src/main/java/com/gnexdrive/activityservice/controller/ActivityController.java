package com.gnexdrive.activityservice.controller;

import com.gnexdrive.common.dto.ApiResponse;
import com.gnexdrive.common.dto.FileActivityDto;
import com.gnexdrive.activityservice.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST Controller for activity operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
@Tag(name = "Activity Tracking", description = "File activity tracking operations")
public class ActivityController {

    private final ActivityService activityService;

    @Operation(summary = "Get user activities")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<FileActivityDto>>> getUserActivities(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Get activities request for user: {}, page: {}, size: {}", userId, page, size);
        
        Page<FileActivityDto> activities = activityService.getUserActivities(
                userId, org.springframework.data.domain.PageRequest.of(page, size, 
                org.springframework.data.domain.Sort.by("timestamp").descending()));
        
        ApiResponse<Page<FileActivityDto>> response = ApiResponse.success(
                "Activities retrieved successfully", activities);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get file activities")
    @GetMapping("/file/{fileId}")
    public ResponseEntity<ApiResponse<Object>> getFileActivities(@PathVariable String fileId) {
        log.info("Get activities request for file: {}", fileId);
        
        var activities = activityService.getFileActivities(fileId);
        ApiResponse<Object> response = ApiResponse.success(
                "File activities retrieved successfully", activities);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get recent activities")
    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<ApiResponse<Object>> getRecentActivities(
            @PathVariable String userId,
            @RequestParam(defaultValue = "7") int days) {
        log.info("Get recent {} days activities for user: {}", days, userId);
        
        var activities = activityService.getRecentActivities(userId, days);
        ApiResponse<Object> response = ApiResponse.success(
                String.format("Recent %d days activities retrieved", days), activities);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get activity statistics")
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<ApiResponse<Object>> getActivityStats(@PathVariable String userId) {
        log.info("Get activity statistics for user: {}", userId);
        
        Object stats = activityService.getActivityStats(userId);
        ApiResponse<Object> response = ApiResponse.success(
                "Activity statistics retrieved", stats);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get activities by date range")
    @GetMapping("/user/{userId}/range")
    public ResponseEntity<ApiResponse<Page<FileActivityDto>>> getActivitiesByDateRange(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Get activities for user: {} from {} to {}", userId, startDate, endDate);
        
        Page<FileActivityDto> activities = activityService.getActivitiesByDateRange(
                userId, startDate, endDate, 
                org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by("timestamp").descending()));
        
        ApiResponse<Page<FileActivityDto>> response = ApiResponse.success(
                "Activities retrieved for date range", activities);
        
        return ResponseEntity.ok(response);
    }
}
