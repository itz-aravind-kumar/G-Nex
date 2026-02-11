package com.gnexdrive.thumbnailservice.kafka;

import com.gnexdrive.common.event.FileEvent;
import com.gnexdrive.thumbnailservice.service.ThumbnailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

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
        log.info("Received file.uploaded event: fileId={}, eventType={}", 
                event.getFileId(), event.getEventType());
        
        try {
            Map<String, Object> payload = event.getPayload();
            if (payload == null) {
                log.warn("No payload in file.uploaded event");
                return;
            }
            
            String fileId = event.getFileId();
            String userId = event.getUserId();
            String contentType = (String) payload.get("contentType");
            String storagePath = (String) payload.get("storagePath");
            Integer version = payload.get("version") != null 
                    ? ((Number) payload.get("version")).intValue() 
                    : 1;
            
            if (contentType == null || storagePath == null) {
                log.warn("Missing contentType or storagePath in event payload");
                return;
            }
            
            log.info("Processing thumbnail job: fileId={}, contentType={}", fileId, contentType);
            thumbnailService.processThumbnailJob(fileId, userId, contentType, storagePath, version);
            
        } catch (Exception e) {
            log.error("Error processing file.uploaded event: fileId={}", event.getFileId(), e);
            // TODO: Publish to DLQ or retry queue
        }
    }

    /**
     * Handle file deleted event - remove thumbnails
     */
    @KafkaListener(topics = "file.deleted", groupId = "thumbnail-service-group")
    public void handleFileDeleted(FileEvent event) {
        log.info("Received file.deleted event: fileId={}", event.getFileId());
        
        try {
            String fileId = event.getFileId();
            String userId = event.getUserId();
            
            if (fileId == null || userId == null) {
                log.warn("Missing fileId or userId in file.deleted event");
                return;
            }
            
            log.info("Deleting thumbnails: fileId={}", fileId);
            thumbnailService.deleteThumbnails(fileId, userId);
            
        } catch (Exception e) {
            log.error("Error processing file.deleted event: fileId={}", event.getFileId(), e);
        }
    }
}
