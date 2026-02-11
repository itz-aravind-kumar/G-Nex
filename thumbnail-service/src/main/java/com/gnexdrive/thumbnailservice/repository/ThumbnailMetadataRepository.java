package com.gnexdrive.thumbnailservice.repository;

import com.gnexdrive.thumbnailservice.entity.ThumbnailMetadata;
import com.gnexdrive.thumbnailservice.entity.ThumbnailMetadata.ThumbnailSize;
import com.gnexdrive.thumbnailservice.entity.ThumbnailMetadata.ThumbnailStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Thumbnail Metadata
 */
@Repository
public interface ThumbnailMetadataRepository extends JpaRepository<ThumbnailMetadata, String> {

    /**
     * Find thumbnail by file ID and size
     */
    Optional<ThumbnailMetadata> findByFileIdAndSize(String fileId, ThumbnailSize size);

    /**
     * Find all thumbnails for a file
     */
    List<ThumbnailMetadata> findByFileId(String fileId);

    /**
     * Find all thumbnails by owner
     */
    List<ThumbnailMetadata> findByOwnerId(String ownerId);

    /**
     * Find thumbnails by status
     */
    List<ThumbnailMetadata> findByStatus(ThumbnailStatus status);

    /**
     * Find pending thumbnails older than specified time (for retry)
     */
    @Query("SELECT t FROM ThumbnailMetadata t WHERE t.status = :status " +
           "AND t.updatedAt < :olderThan AND t.attemptCount < :maxAttempts")
    List<ThumbnailMetadata> findPendingForRetry(
        @Param("status") ThumbnailStatus status,
        @Param("olderThan") LocalDateTime olderThan,
        @Param("maxAttempts") int maxAttempts
    );

    /**
     * Find ready thumbnails by file ID
     */
    @Query("SELECT t FROM ThumbnailMetadata t WHERE t.fileId = :fileId AND t.status = 'READY'")
    List<ThumbnailMetadata> findReadyThumbnailsByFileId(@Param("fileId") String fileId);

    /**
     * Check if thumbnail exists
     */
    boolean existsByFileIdAndSize(String fileId, ThumbnailSize size);

    /**
     * Delete all thumbnails for a file
     */
    void deleteByFileId(String fileId);

    /**
     * Count thumbnails by status
     */
    long countByStatus(ThumbnailStatus status);
}
