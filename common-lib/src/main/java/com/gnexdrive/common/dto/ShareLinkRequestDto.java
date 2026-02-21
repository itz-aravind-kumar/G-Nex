package com.gnexdrive.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for creating a share link
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareLinkRequestDto {
    
    private String resourceId;
    private String resourceType; // FILE or FOLDER
    private String role;  // VIEWER or EDITOR
    private LocalDateTime expiresAt;
    private String password;
    private Integer maxDownloads;
}
