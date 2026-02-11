package com.gnexdrive.thumbnailservice.kafka;

import com.gnexdrive.common.event.FileEvent;
import com.gnexdrive.thumbnailservice.service.ThumbnailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka Consumer for file events
 * Listens to file.uploaded and file.deleted events
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileEventConsumer {

    private final ThumbnailService thumbnailService;

    /**
     * Handle file uploaded event - generate thumbnails
     */
    @KafkaListener(topics = "file.uploaded", groupId = "thumbnail-service-group")
    public void handleFileUploaded(FileEvent event) {
        log.info("Received file.uploaded event: {}", event);
        
        try {
            // TODO: Implement file uploaded handling
            // 1. Extract file metadata from event
            // 2. Check if content type supports thumbnails
            // 3. Enqueue thumbnail generation jobs (async)
            // 4. For each size: create pending metadata and trigger generation
            
            thumbnailService.processThumbnailJob(
                event.getFileId(),
                event.getOwnerId(),
                event.getContentType(),
                event.getStoragePath(),
                event.getVersion()
            );
            
        } catch (Exception e) {
            log.error("Error processing file.uploaded event: {}", event, e);
            // TODO: Publish to DLQ or retry queue
        }
    }

    /**
     * Handle file deleted event - remove thumbnails
     */
    @KafkaListener(topics = "file.deleted", groupId = "thumbnail-service-group")
    public void handleFileDeleted(FileEvent event) {
        log.info("Received file.deleted event: {}", event);
        
        try {
            // TODO: Implement file deleted handling
            // 1. Find all thumbnails for the file
            // 2. Delete thumbnail files from storage
            // 3. Delete thumbnail metadata
            // 4. Publish thumbnail.deleted event
            
            thumbnailService.deleteThumbnails(event.getFileId(), event.getOwnerId());
            
        } catch (Exception e) {
            log.error("Error processing file.deleted event: {}", event, e);
        }
    }
}
