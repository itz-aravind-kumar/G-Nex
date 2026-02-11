package com.gnexdrive.thumbnailservice.service;

import com.gnexdrive.thumbnailservice.dto.ThumbnailDto;
import com.gnexdrive.thumbnailservice.dto.ThumbnailRequestDto;
import com.gnexdrive.thumbnailservice.dto.ThumbnailStatusDto;
import com.gnexdrive.thumbnailservice.entity.ThumbnailMetadata.ThumbnailSize;

import java.util.List;

/**
 * Thumbnail Service Interface
 */
public interface ThumbnailService {

    /**
     * Get thumbnail by file ID and size
     */
    ThumbnailDto getThumbnail(String fileId, ThumbnailSize size);

    /**
     * Get thumbnail status for a file
     */
    ThumbnailStatusDto getThumbnailStatus(String fileId);

    /**
     * Get all thumbnails for a file
     */
    List<ThumbnailDto> getAllThumbnails(String fileId);

    /**
     * Request thumbnail generation (on-demand)
     */
    ThumbnailStatusDto requestThumbnailGeneration(ThumbnailRequestDto request, String userId);

    /**
     * Delete all thumbnails for a file
     */
    void deleteThumbnails(String fileId, String userId);

    /**
     * Process thumbnail generation job (called by worker)
     */
    void processThumbnailJob(String fileId, String ownerId, String contentType, 
                            String storagePath, Integer version);

    /**
     * Retry failed thumbnails
     */
    void retryFailedThumbnails();
}
