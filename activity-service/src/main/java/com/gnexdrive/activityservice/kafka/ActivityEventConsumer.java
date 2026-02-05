package com.gnexdrive.activityservice.kafka;

import com.gnexdrive.common.constant.AppConstants;
import com.gnexdrive.common.event.FileEvent;
import com.gnexdrive.activityservice.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
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
    public void handleFileUploadedEvent(FileEvent event) {
        // TODO: Implement file uploaded event handler
        // Log upload activity
    }

    @KafkaListener(topics = AppConstants.TOPIC_FILE_DELETED, groupId = "activity-service")
    public void handleFileDeletedEvent(FileEvent event) {
        // TODO: Implement file deleted event handler
        // Log delete activity
    }

    @KafkaListener(topics = AppConstants.TOPIC_FILE_DOWNLOADED, groupId = "activity-service")
    public void handleFileDownloadedEvent(FileEvent event) {
        // TODO: Implement file downloaded event handler
        // Log download activity
    }

    @KafkaListener(topics = AppConstants.TOPIC_ACTIVITY_LOG, groupId = "activity-service")
    public void handleActivityLogEvent(FileEvent event) {
        // TODO: Implement general activity log event handler
        // Log any activity
    }
}
