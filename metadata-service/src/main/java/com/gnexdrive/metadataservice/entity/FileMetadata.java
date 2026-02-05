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

/**
 * File Metadata Entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "file_metadata", indexes = {
    @Index(name = "idx_owner_id", columnList = "owner_id"),
    @Index(name = "idx_file_name", columnList = "file_name"),
    @Index(name = "idx_file_type", columnList = "file_type")
})
@EntityListeners(AuditingEntityListener.class)
public class FileMetadata {

    @Id
    @Column(name = "file_id", nullable = false, length = 100)
    private String fileId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "owner_id", nullable = false, length = 100)
    private String ownerId;

    @Column(name = "owner_email")
    private String ownerEmail;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "checksum", length = 64)
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private FileStatus status;

    @CreatedDate
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @LastModifiedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    public enum FileStatus {
        UPLOADING,
        UPLOADED,
        PROCESSING,
        AVAILABLE,
        DELETED,
        ERROR
    }
}
