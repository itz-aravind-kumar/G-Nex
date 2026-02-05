package com.gnexdrive.activityservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * File Activity Entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "file_activities", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_file_id", columnList = "file_id"),
    @Index(name = "idx_activity_type", columnList = "activity_type"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@EntityListeners(AuditingEntityListener.class)
public class FileActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "activity_id")
    private String activityId;

    @Column(name = "file_id", nullable = false, length = 100)
    private String fileId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "user_email")
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreatedDate
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "metadata", columnDefinition = "TEXT")
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
