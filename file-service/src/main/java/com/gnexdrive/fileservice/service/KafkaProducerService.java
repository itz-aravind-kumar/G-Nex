package com.gnexdrive.fileservice.service;

import com.gnexdrive.common.event.FileEvent;

/**
 * Kafka producer service for publishing file events
 */
public interface KafkaProducerService {

    /**
     * Publish file uploaded event
     */
    void publishFileUploadedEvent(FileEvent event);

    /**
     * Publish file deleted event
     */
    void publishFileDeletedEvent(FileEvent event);

    /**
     * Publish file downloaded event
     */
    void publishFileDownloadedEvent(FileEvent event);
}
