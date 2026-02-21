package com.gnexdrive.metadataservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * ShareLink Entity - represents shareable links for files and folders
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "share_links", indexes = {
    @Index(name = "idx_share_link_token", columnList = "token", unique = true),
    @Index(name = "idx_share_link_resource", columnList = "resource_type, resource_id"),
    @Index(name = "idx_share_link_created_by", columnList = "created_by")
})
@EntityListeners(AuditingEntityListener.class)
public class ShareLink {

    @Id
    @Column(name = "link_id", nullable = false, length = 100)
    private String linkId;

    @Column(name = "token", nullable = false, unique = true, length = 64)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 20)
    private Permission.ResourceType resourceType;

    @Column(name = "resource_id", nullable = false, length = 100)
    private String resourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private Permission.Role role = Permission.Role.VIEWER;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "is_password_protected")
    @Builder.Default
    private Boolean isPasswordProtected = false;

    @Column(name = "max_downloads")
    private Integer maxDownloads;

    @Column(name = "download_count")
    @Builder.Default
    private Integer downloadCount = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "access_count")
    @Builder.Default
    private Integer accessCount = 0;

    @PrePersist
    public void prePersist() {
        if (linkId == null) {
            linkId = UUID.randomUUID().toString();
        }
        if (token == null) {
            token = generateSecureToken();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Generate a secure random token for the share link
     */
    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Check if link has expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if download limit has been reached
     */
    public boolean isDownloadLimitReached() {
        return maxDownloads != null && downloadCount >= maxDownloads;
    }

    /**
     * Check if link is valid for access
     */
    public boolean isValid() {
        return isActive && !isExpired() && !isDownloadLimitReached();
    }

    /**
     * Increment download count
     */
    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    /**
     * Record access
     */
    public void recordAccess() {
        this.accessCount++;
        this.lastAccessedAt = LocalDateTime.now();
    }
}
