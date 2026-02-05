package com.gnexdrive.fileservice.service.impl;

import com.gnexdrive.common.constant.AppConstants;
import com.gnexdrive.common.event.FileEvent;
import com.gnexdrive.fileservice.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Implementation of Kafka Producer Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, FileEvent> kafkaTemplate;

    @Override
    public void publishFileUploadedEvent(FileEvent event) {
        // TODO: Implement publish file uploaded event
        // kafkaTemplate.send(AppConstants.TOPIC_FILE_UPLOADED, event.getFileId(), event);
    }

    @Override
    public void publishFileDeletedEvent(FileEvent event) {
        // TODO: Implement publish file deleted event
        // kafkaTemplate.send(AppConstants.TOPIC_FILE_DELETED, event.getFileId(), event);
    }

    @Override
    public void publishFileDownloadedEvent(FileEvent event) {
        // TODO: Implement publish file downloaded event
        // kafkaTemplate.send(AppConstants.TOPIC_FILE_DOWNLOADED, event.getFileId(), event);
    }
}
