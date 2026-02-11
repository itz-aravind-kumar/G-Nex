package com.gnexdrive.thumbnailservice.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Thumbnail Metadata Entity
 */
@Entity
@Table(name = "thumbnail_metadata", indexes = {
    @Index(name = "idx_file_id", columnList = "file_id"),
    @Index(name = "idx_owner_id", columnList = "owner_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_file_size", columnList = "file_id,size")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ThumbnailMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "file_id", nullable = false, length = 100)
    private String fileId;

    @Column(name = "owner_id", nullable = false, length = 100)
    private String ownerId;

    @Column(name = "size", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ThumbnailSize size;

    @Column(name = "storage_path", length = 500)
    private String storagePath;

    @Column(name = "url", length = 500)
    private String url;

    @Column(name = "format", length = 10)
    private String format; // webp, jpg, png

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "file_size")
    private Long fileSize; // thumbnail file size in bytes

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ThumbnailStatus status;

    @Column(name = "attempt_count")
    private Integer attemptCount = 0;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "version")
    private Integer version = 1;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Thumbnail Size Enum
     */
    public enum ThumbnailSize {
        SMALL(150, 150),      // grid view
        GRID(200, 200),       // default grid
        PREVIEW(400, 400);    // preview modal

        private final int width;
        private final int height;

        ThumbnailSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    /**
     * Thumbnail Status Enum
     */
    public enum ThumbnailStatus {
        PENDING,
        PROCESSING,
        READY,
        FAILED,
        DELETED
    }
}
