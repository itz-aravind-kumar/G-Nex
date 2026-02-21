package com.gnexdrive.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing a share link
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareLinkDto {
    
    private String linkId;
    private String token;
    private String url;
    private String resourceType;
    private String resourceId;
    private String resourceName;
    private String role;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isPasswordProtected;
    private Integer maxDownloads;
    private Integer downloadCount;
    private boolean isActive;
    private Integer accessCount;
    private LocalDateTime lastAccessedAt;
}
