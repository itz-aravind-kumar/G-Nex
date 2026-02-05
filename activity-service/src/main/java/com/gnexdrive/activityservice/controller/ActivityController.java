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
        // TODO: Implement get user activities endpoint
        return null;
    }

    @Operation(summary = "Get file activities")
    @GetMapping("/file/{fileId}")
    public ResponseEntity<ApiResponse<Object>> getFileActivities(@PathVariable String fileId) {
        // TODO: Implement get file activities endpoint
        return null;
    }

    @Operation(summary = "Get recent activities")
    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<ApiResponse<Object>> getRecentActivities(
            @PathVariable String userId,
            @RequestParam(defaultValue = "7") int days) {
        // TODO: Implement get recent activities endpoint
        return null;
    }

    @Operation(summary = "Get activity statistics")
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<ApiResponse<Object>> getActivityStats(@PathVariable String userId) {
        // TODO: Implement get activity statistics endpoint
        return null;
    }

    @Operation(summary = "Get activities by date range")
    @GetMapping("/user/{userId}/range")
    public ResponseEntity<ApiResponse<Page<FileActivityDto>>> getActivitiesByDateRange(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // TODO: Implement get activities by date range endpoint
        return null;
    }
}
