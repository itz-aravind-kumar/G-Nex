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

    @KafkaListener(topics = AppConstants.TOPIC_FILE_UPLOADED, groupId = "metadata-service")
    public void handleFileUploadedEvent(FileEvent event) {
        // TODO: Implement file uploaded event handler
        // Extract metadata from event and save to database
    }

    @KafkaListener(topics = AppConstants.TOPIC_FILE_DELETED, groupId = "metadata-service")
    public void handleFileDeletedEvent(FileEvent event) {
        // TODO: Implement file deleted event handler
        // Mark file as deleted in database
    }

    @KafkaListener(topics = AppConstants.TOPIC_METADATA_UPDATED, groupId = "metadata-service")
    public void handleMetadataUpdatedEvent(FileEvent event) {
        // TODO: Implement metadata updated event handler
        // Update metadata in database
    }
}
