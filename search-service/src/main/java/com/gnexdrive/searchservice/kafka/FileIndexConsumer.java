package com.gnexdrive.searchservice.kafka;

import com.gnexdrive.common.constant.AppConstants;
import com.gnexdrive.common.event.FileEvent;
import com.gnexdrive.searchservice.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for indexing file events
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileIndexConsumer {

    private final SearchService searchService;

    @KafkaListener(topics = AppConstants.TOPIC_FILE_UPLOADED, groupId = "search-service")
    public void handleFileUploadedEvent(FileEvent event) {
        // TODO: Implement file uploaded event handler
        // Extract file info from event and index in Elasticsearch
    }

    @KafkaListener(topics = AppConstants.TOPIC_FILE_DELETED, groupId = "search-service")
    public void handleFileDeletedEvent(FileEvent event) {
        // TODO: Implement file deleted event handler
        // Delete file from Elasticsearch index
    }

    @KafkaListener(topics = AppConstants.TOPIC_METADATA_UPDATED, groupId = "search-service")
    public void handleMetadataUpdatedEvent(FileEvent event) {
        // TODO: Implement metadata updated event handler
        // Update file in Elasticsearch index
    }
}
