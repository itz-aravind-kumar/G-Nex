package com.gnexdrive.thumbnailservice.kafka;

import com.gnexdrive.common.event.FileEvent;
import com.gnexdrive.thumbnailservice.entity.ThumbnailMetadata.ThumbnailSize;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka Producer for thumbnail events
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThumbnailEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_THUMBNAIL_READY = "thumbnail.ready";
    private static final String TOPIC_THUMBNAIL_FAILED = "thumbnail.failed";
    private static final String TOPIC_THUMBNAIL_DELETED = "thumbnail.deleted";

    /**
     * Publish thumbnail ready event
     */
    public void publishThumbnailReady(String fileId, ThumbnailSize size, String storagePath) {
        log.info("Publishing thumbnail.ready event: fileId={}, size={}", fileId, size);
        
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("size", size.name());
            payload.put("storagePath", storagePath);
            payload.put("width", size.getWidth());
            payload.put("height", size.getHeight());
            
            FileEvent event = FileEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("THUMBNAIL_READY")
                    .fileId(fileId)
                    .timestamp(LocalDateTime.now())
                    .payload(payload)
                    .source("thumbnail-service")
                    .build();
            
            kafkaTemplate.send(TOPIC_THUMBNAIL_READY, fileId, event);
            log.debug("Published thumbnail.ready event successfully");
        } catch (Exception e) {
            log.error("Failed to publish thumbnail.ready event", e);
        }
    }

    /**
     * Publish thumbnail failed event
     */
    public void publishThumbnailFailed(String fileId, ThumbnailSize size, String error) {
        log.info("Publishing thumbnail.failed event: fileId={}, size={}, error={}", 
                fileId, size, error);
        
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("size", size.name());
            payload.put("error", error);
            
            FileEvent event = FileEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("THUMBNAIL_FAILED")
                    .fileId(fileId)
                    .timestamp(LocalDateTime.now())
                    .payload(payload)
                    .source("thumbnail-service")
                    .build();
            
            kafkaTemplate.send(TOPIC_THUMBNAIL_FAILED, fileId, event);
            log.debug("Published thumbnail.failed event successfully");
        } catch (Exception e) {
            log.error("Failed to publish thumbnail.failed event", e);
        }
    }

    /**
     * Publish thumbnail deleted event
     */
    public void publishThumbnailDeleted(String fileId, String ownerId) {
        log.info("Publishing thumbnail.deleted event: fileId={}", fileId);
        
        try {
            FileEvent event = FileEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("THUMBNAIL_DELETED")
                    .fileId(fileId)
                    .userId(ownerId)
                    .timestamp(LocalDateTime.now())
                    .source("thumbnail-service")
                    .build();
            
            kafkaTemplate.send(TOPIC_THUMBNAIL_DELETED, fileId, event);
            log.debug("Published thumbnail.deleted event successfully");
        } catch (Exception e) {
            log.error("Failed to publish thumbnail.deleted event", e);
        }
    }
}
