package com.gnexdrive.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for sharing a resource with a user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareRequestDto {
    
    private String resourceId;
    private String resourceType; // FILE or FOLDER
    private String granteeEmail;
    private String role; // VIEWER, EDITOR
    private LocalDateTime expiresAt;
    private boolean notifyUser;
    private String message;
}
