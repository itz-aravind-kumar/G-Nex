package com.gnexdrive.activityservice.kafka;

import com.gnexdrive.common.constant.AppConstants;
import com.gnexdrive.common.dto.FileActivityDto;
import com.gnexdrive.common.event.FileEvent;
import com.gnexdrive.activityservice.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for file activity events
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityEventConsumer {

    private final ActivityService activityService;

    @KafkaListener(topics = AppConstants.TOPIC_FILE_UPLOADED, groupId = "activity-service")
    public void handleFileUploadedEvent(
            @Payload FileEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            log.info("Received file uploaded event from topic: {} for file: {}", topic, event.getFileId());
            
            FileActivityDto activity = FileActivityDto.builder()
                    .fileId(event.getFileId())
                    .fileName(event.getFileName())
                    .userId(event.getUserId())
                    .userEmail(extractFromPayload(event, "userEmail"))
                    .activityType(FileActivityDto.ActivityType.FILE_UPLOADED)
                    .metadata(buildMetadata(event))
                    .build();
            
            activityService.logActivity(activity);
            log.debug("Successfully logged upload activity for file: {}", event.getFileId());
        } catch (Exception e) {
            log.error("Error processing file uploaded event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = AppConstants.TOPIC_FILE_DELETED, groupId = "activity-service")
    public void handleFileDeletedEvent(
            @Payload FileEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            log.info("Received file deleted event from topic: {} for file: {}", topic, event.getFileId());
            
            FileActivityDto activity = FileActivityDto.builder()
                    .fileId(event.getFileId())
                    .fileName(event.getFileName())
                    .userId(event.getUserId())
                    .userEmail(extractFromPayload(event, "userEmail"))
                    .activityType(FileActivityDto.ActivityType.FILE_DELETED)
                    .metadata(buildMetadata(event))
                    .build();
            
            activityService.logActivity(activity);
            log.debug("Successfully logged delete activity for file: {}", event.getFileId());
        } catch (Exception e) {
            log.error("Error processing file deleted event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = AppConstants.TOPIC_FILE_DOWNLOADED, groupId = "activity-service")
    public void handleFileDownloadedEvent(
            @Payload FileEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            log.info("Received file downloaded event from topic: {} for file: {}", topic, event.getFileId());
            
            FileActivityDto activity = FileActivityDto.builder()
                    .fileId(event.getFileId())
                    .fileName(event.getFileName())
                    .userId(event.getUserId())
                    .userEmail(extractFromPayload(event, "userEmail"))
                    .activityType(FileActivityDto.ActivityType.FILE_DOWNLOADED)
                    .metadata(buildMetadata(event))
                    .build();
            
            activityService.logActivity(activity);
            log.debug("Successfully logged download activity for file: {}", event.getFileId());
        } catch (Exception e) {
            log.error("Error processing file downloaded event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = AppConstants.TOPIC_ACTIVITY_LOG, groupId = "activity-service")
    public void handleActivityLogEvent(
            @Payload FileEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            log.info("Received activity log event from topic: {} for file: {}", topic, event.getFileId());
            
            FileActivityDto activity = FileActivityDto.builder()
                    .fileId(event.getFileId())
                    .fileName(event.getFileName())
                    .userId(event.getUserId())
                    .userEmail(extractFromPayload(event, "userEmail"))
                    .activityType(FileActivityDto.ActivityType.FILE_VIEWED)
                    .metadata(buildMetadata(event))
                    .build();
            
            activityService.logActivity(activity);
            log.debug("Successfully logged activity for file: {}", event.getFileId());
        } catch (Exception e) {
            log.error("Error processing activity log event: {}", e.getMessage(), e);
        }
    }
    
    private String extractFromPayload(FileEvent event, String key) {
        if (event.getPayload() != null && event.getPayload().containsKey(key)) {
            Object value = event.getPayload().get(key);
            return value != null ? value.toString() : null;
        }
        return null;
    }
    
    private String buildMetadata(FileEvent event) {
        if (event.getPayload() == null) {
            return "{}";
        }
        
        try {
            StringBuilder metadata = new StringBuilder("{");
            metadata.append("\"eventType\":\"").append(event.getEventType()).append("\",");
            metadata.append("\"source\":\"").append(event.getSource()).append("\",");
            metadata.append("\"timestamp\":\"").append(event.getTimestamp()).append("\"");
            
            String contentType = extractFromPayload(event, "contentType");
            if (contentType != null) {
                metadata.append(",\"contentType\":\"").append(contentType).append("\"");
            }
            
            String fileSize = extractFromPayload(event, "fileSize");
            if (fileSize != null) {
                metadata.append(",\"fileSize\":").append(fileSize);
            }
            
            metadata.append("}");
            return metadata.toString();
        } catch (Exception e) {
            log.warn("Error building metadata: {}", e.getMessage());
            return "{}";
        }
    }
}
