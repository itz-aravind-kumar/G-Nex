package com.gnexdrive.metadataservice.kafka;

import com.gnexdrive.common.constant.AppConstants;
import com.gnexdrive.common.event.FileEvent;
import com.gnexdrive.metadataservice.service.MetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for file events
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileEventConsumer {

    private final MetadataService metadataService;

    /**
     * Handle file uploaded event from file-service
     */
    @KafkaListener(topics = AppConstants.TOPIC_FILE_UPLOADED, groupId = "metadata-service")
    public void handleFileUploadedEvent(FileEvent event) {
        log.info("Received file uploaded event: {}", event.getEventId());
        
        try {
            // Extract metadata from event payload
            var payload = event.getPayload();
            
            com.gnexdrive.common.dto.FileMetadataDto metadataDto = com.gnexdrive.common.dto.FileMetadataDto.builder()
                    .fileId(event.getFileId())
                    .fileName((String) payload.get("fileName"))
                    .fileType((String) payload.get("fileType"))
                    .fileSize(getLongFromPayload(payload.get("fileSize")))
                    .ownerId(event.getUserId())
                    .ownerEmail((String) payload.get("ownerEmail"))
                    .storagePath((String) payload.get("storagePath"))
                    .contentType((String) payload.get("contentType"))
                    .checksum((String) payload.get("checksum"))
                    .status("UPLOADED")
                    .uploadedAt(event.getTimestamp())
                    .build();
            
            // Save metadata to database
            metadataService.saveMetadata(metadataDto);
            
            log.info("File metadata saved successfully for file: {}", event.getFileId());
        } catch (Exception e) {
            log.error("Error processing file uploaded event: {}", event.getEventId(), e);
        }
    }

    /**
     * Handle file deleted event from file-service
     */
    @KafkaListener(topics = AppConstants.TOPIC_FILE_DELETED, groupId = "metadata-service")
    public void handleFileDeletedEvent(FileEvent event) {
        log.info("Received file deleted event: {}", event.getEventId());
        
        try {
            // Mark file as deleted in database
            metadataService.deleteMetadata(event.getFileId(), event.getUserId());
            
            log.info("File metadata marked as deleted for file: {}", event.getFileId());
        } catch (Exception e) {
            log.error("Error processing file deleted event: {}", event.getEventId(), e);
        }
    }

    /**
     * Handle metadata updated event (from metadata-service itself or other services)
     */
    @KafkaListener(topics = AppConstants.TOPIC_METADATA_UPDATED, groupId = "metadata-service")
    public void handleMetadataUpdatedEvent(FileEvent event) {
        log.info("Received metadata updated event: {}", event.getEventId());
        
        try {
            var payload = event.getPayload();
            
            com.gnexdrive.common.dto.FileMetadataDto metadataDto = com.gnexdrive.common.dto.FileMetadataDto.builder()
                    .fileId(event.getFileId())
                    .fileName((String) payload.get("fileName"))
                    .status((String) payload.get("status"))
                    .build();
            
            // Update metadata in database
            metadataService.updateMetadata(event.getFileId(), metadataDto, event.getUserId());
            
            log.info("File metadata updated successfully for file: {}", event.getFileId());
        } catch (Exception e) {
            log.error("Error processing metadata updated event: {}", event.getEventId(), e);
        }
    }
    
    /**
     * Helper method to convert payload value to Long
     */
    private Long getLongFromPayload(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse Long from string: {}", value);
                return 0L;
            }
        }
        return 0L;
    }
}
