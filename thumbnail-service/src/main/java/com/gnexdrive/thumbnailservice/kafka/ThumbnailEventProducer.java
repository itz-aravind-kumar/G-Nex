package com.gnexdrive.thumbnailservice.kafka;

import com.gnexdrive.common.event.FileEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka Producer for thumbnail events
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThumbnailEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publish thumbnail ready event
     */
    public void publishThumbnailReady(String fileId, String ownerId, String size, String url) {
        log.info("Publishing thumbnail.ready event: fileId={}, size={}", fileId, size);
        
        // TODO: Implement event publishing
        // 1. Create FileEvent with type THUMBNAIL_READY
        // 2. Add thumbnail metadata
        // 3. Send to thumbnail.ready topic
        // 4. Handle send errors
    }

    /**
     * Publish thumbnail failed event
     */
    public void publishThumbnailFailed(String fileId, String ownerId, String size, String error) {
        log.info("Publishing thumbnail.failed event: fileId={}, size={}, error={}", 
                fileId, size, error);
        
        // TODO: Implement failure event publishing
        // 1. Create FileEvent with type THUMBNAIL_FAILED
        // 2. Add error details
        // 3. Send to thumbnail.failed or DLQ topic
    }

    /**
     * Publish thumbnail deleted event
     */
    public void publishThumbnailDeleted(String fileId, String ownerId) {
        log.info("Publishing thumbnail.deleted event: fileId={}", fileId);
        
        // TODO: Implement delete event publishing
        // 1. Create FileEvent with type THUMBNAIL_DELETED
        // 2. Send to thumbnail.deleted topic
    }
}
