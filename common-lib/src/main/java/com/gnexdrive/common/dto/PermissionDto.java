package com.gnexdrive.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing a permission
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDto {
    
    private String permissionId;
    private String resourceType;
    private String resourceId;
    private String resourceName;
    private String granteeId;
    private String granteeEmail;
    private String granteeName;
    private String role;
    private String grantedBy;
    private String grantedByEmail;
    private LocalDateTime grantedAt;
    private LocalDateTime expiresAt;
    private boolean isInherited;
}
