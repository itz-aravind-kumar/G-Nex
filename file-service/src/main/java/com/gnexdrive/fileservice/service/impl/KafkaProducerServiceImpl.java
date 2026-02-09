package com.gnexdrive.fileservice.service.impl;

import com.gnexdrive.common.constant.AppConstants;
import com.gnexdrive.common.event.FileEvent;
import com.gnexdrive.fileservice.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Implementation of Kafka Producer Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishFileUploadedEvent(FileEvent event) {
        try {
            log.info("Publishing file uploaded event: fileId={}, userId={}", 
                    event.getFileId(), event.getUserId());
            
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(AppConstants.TOPIC_FILE_UPLOADED, event.getFileId(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("File uploaded event published successfully: fileId={}, offset={}", 
                            event.getFileId(), result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish file uploaded event: fileId={}", 
                            event.getFileId(), ex);
                }
            });
            
        } catch (Exception e) {
            log.error("Error publishing file uploaded event: {}", e.getMessage(), e);
        }
    }

    @Override
    public void publishFileDeletedEvent(FileEvent event) {
        try {
            log.info("Publishing file deleted event: fileId={}, userId={}", 
                    event.getFileId(), event.getUserId());
            
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(AppConstants.TOPIC_FILE_DELETED, event.getFileId(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("File deleted event published successfully: fileId={}, offset={}", 
                            event.getFileId(), result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish file deleted event: fileId={}", 
                            event.getFileId(), ex);
                }
            });
            
        } catch (Exception e) {
            log.error("Error publishing file deleted event: {}", e.getMessage(), e);
        }
    }

    @Override
    public void publishFileDownloadedEvent(FileEvent event) {
        try {
            log.info("Publishing file downloaded event: fileId={}, userId={}", 
                    event.getFileId(), event.getUserId());
            
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(AppConstants.TOPIC_FILE_DOWNLOADED, event.getFileId(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("File downloaded event published successfully: fileId={}, offset={}", 
                            event.getFileId(), result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish file downloaded event: fileId={}", 
                            event.getFileId(), ex);
                }
            });
            
        } catch (Exception e) {
            log.error("Error publishing file downloaded event: {}", e.getMessage(), e);
        }
    }
}
