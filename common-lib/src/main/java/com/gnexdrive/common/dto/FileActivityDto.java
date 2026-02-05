package com.gnexdrive.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * File activity DTO for tracking user actions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileActivityDto {
    
    private String activityId;
    private String fileId;
    private String fileName;
    private String userId;
    private String userEmail;
    private ActivityType activityType;
    private String ipAddress;
    private String userAgent;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String metadata;
    
    public enum ActivityType {
        FILE_UPLOADED,
        FILE_DOWNLOADED,
        FILE_VIEWED,
        FILE_DELETED,
        FILE_SHARED,
        FILE_RENAMED,
        FILE_MOVED,
        SEARCH_PERFORMED
    }
}
