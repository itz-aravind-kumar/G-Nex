package com.gnexdrive.activityservice.service;

import com.gnexdrive.common.dto.FileActivityDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for activity operations
 */
public interface ActivityService {

    /**
     * Log file activity
     */
    void logActivity(FileActivityDto activityDto);

    /**
     * Get activities by user
     */
    Page<FileActivityDto> getUserActivities(String userId, Pageable pageable);

    /**
     * Get activities by file
     */
    List<FileActivityDto> getFileActivities(String fileId);

    /**
     * Get recent activities
     */
    List<FileActivityDto> getRecentActivities(String userId, int days);

    /**
     * Get activity statistics
     */
    Object getActivityStats(String userId);

    /**
     * Get activities by date range
     */
    Page<FileActivityDto> getActivitiesByDateRange(
        String userId, 
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        Pageable pageable
    );
}
