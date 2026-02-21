package com.gnexdrive.metadataservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Folder Entity - represents a folder/directory in the drive
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "folders", indexes = {
    @Index(name = "idx_folder_owner_id", columnList = "owner_id"),
    @Index(name = "idx_folder_parent_id", columnList = "parent_id"),
    @Index(name = "idx_folder_name", columnList = "folder_name")
})
@EntityListeners(AuditingEntityListener.class)
public class Folder {

    @Id
    @Column(name = "folder_id", nullable = false, length = 100)
    private String folderId;

    @Column(name = "folder_name", nullable = false, length = 255)
    private String folderName;

    @Column(name = "parent_id", length = 100)
    private String parentId;

    @Column(name = "owner_id", nullable = false, length = 100)
    private String ownerId;

    @Column(name = "owner_email")
    private String ownerEmail;

    @Column(name = "path", length = 1000)
    private String path;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "is_starred")
    @Builder.Default
    private Boolean isStarred = false;

    @Column(name = "is_trashed")
    @Builder.Default
    private Boolean isTrashed = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private FolderStatus status = FolderStatus.ACTIVE;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    public enum FolderStatus {
        ACTIVE,
        DELETED,
        ARCHIVED
    }

    @PrePersist
    public void prePersist() {
        if (folderId == null) {
            folderId = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        // Build path from parent
        if (path == null && parentId == null) {
            path = "/" + folderName;
        }
    }
}
