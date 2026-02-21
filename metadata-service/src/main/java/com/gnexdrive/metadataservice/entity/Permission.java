package com.gnexdrive.metadataservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Permission Entity - represents sharing permissions for files and folders
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permission_resource", columnList = "resource_type, resource_id"),
    @Index(name = "idx_permission_grantee", columnList = "grantee_id"),
    @Index(name = "idx_permission_grantee_email", columnList = "grantee_email")
})
@EntityListeners(AuditingEntityListener.class)
public class Permission {

    @Id
    @Column(name = "permission_id", nullable = false, length = 100)
    private String permissionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 20)
    private ResourceType resourceType;

    @Column(name = "resource_id", nullable = false, length = 100)
    private String resourceId;

    @Column(name = "grantee_id", length = 100)
    private String granteeId;

    @Column(name = "grantee_email")
    private String granteeEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "granted_by", nullable = false, length = 100)
    private String grantedBy;

    @CreatedDate
    @Column(name = "granted_at", nullable = false, updatable = false)
    private LocalDateTime grantedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_inherited")
    @Builder.Default
    private Boolean isInherited = false;

    @Column(name = "parent_permission_id", length = 100)
    private String parentPermissionId;

    public enum ResourceType {
        FILE,
        FOLDER
    }

    public enum Role {
        OWNER,    // Full control including sharing and deletion
        EDITOR,   // Can edit/upload but not share or delete
        VIEWER    // Read-only access
    }

    @PrePersist
    public void prePersist() {
        if (permissionId == null) {
            permissionId = UUID.randomUUID().toString();
        }
        if (grantedAt == null) {
            grantedAt = LocalDateTime.now();
        }
    }

    /**
     * Check if permission has expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if user can view resource
     */
    public boolean canView() {
        return !isExpired();
    }

    /**
     * Check if user can edit resource
     */
    public boolean canEdit() {
        return !isExpired() && (role == Role.OWNER || role == Role.EDITOR);
    }

    /**
     * Check if user can share resource
     */
    public boolean canShare() {
        return !isExpired() && role == Role.OWNER;
    }

    /**
     * Check if user can delete resource
     */
    public boolean canDelete() {
        return !isExpired() && role == Role.OWNER;
    }
}
