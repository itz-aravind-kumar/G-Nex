package com.gnexdrive.fileservice.service.impl;

import com.gnexdrive.common.constant.AppConstants;
import com.gnexdrive.common.dto.FileMetadataDto;
import com.gnexdrive.common.event.FileEvent;
import com.gnexdrive.common.exception.FileStorageException;
import com.gnexdrive.common.exception.ResourceNotFoundException;
import com.gnexdrive.common.util.FileUtils;
import com.gnexdrive.fileservice.service.FileService;
import com.gnexdrive.fileservice.service.KafkaProducerService;
import com.gnexdrive.fileservice.service.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of File Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final ObjectStorageService objectStorageService;
    private final KafkaProducerService kafkaProducerService;

    @Override
    public FileMetadataDto uploadFile(MultipartFile file, String userId) {
        log.info("Starting file upload: fileName={}, size={}, userId={}", 
                file.getOriginalFilename(), file.getSize(), userId);
        
        try {
            // Validate file
            validateFile(file);
            
            // Generate unique file ID
            String fileId = FileUtils.generateFileId();
            String originalFileName = file.getOriginalFilename();
            String sanitizedFileName = FileUtils.sanitizeFileName(originalFileName);
            String fileExtension = FileUtils.extractFileExtension(originalFileName);
            
            // Create storage path: userId/fileId.extension
            String storagePath = userId + "/" + fileId + 
                    (fileExtension.isEmpty() ? "" : "." + fileExtension);
            
            // Upload to object storage
            objectStorageService.uploadFile(
                    storagePath, 
                    file.getInputStream(), 
                    file.getContentType(), 
                    file.getSize()
            );
            
            // Calculate checksum
            String checksum = FileUtils.calculateChecksum(file.getBytes());
            
            // Build metadata DTO
            FileMetadataDto metadata = FileMetadataDto.builder()
                    .fileId(fileId)
                    .fileName(originalFileName)
                    .fileType(fileExtension)
                    .fileSize(file.getSize())
                    .ownerId(userId)
                    .storagePath(storagePath)
                    .contentType(file.getContentType())
                    .checksum(checksum)
                    .uploadedAt(LocalDateTime.now())
                    .status(FileMetadataDto.FileStatus.UPLOADED)
                    .build();
            
            // Publish event to Kafka
            publishUploadEvent(metadata);
            
            log.info("File uploaded successfully: fileId={}, path={}", fileId, storagePath);
            return metadata;
            
        } catch (Exception e) {
            log.error("Failed to upload file: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public Resource downloadFile(String fileId, String userId) {
        log.info("Downloading file: fileId={}, userId={}", fileId, userId);
        
        try {
            // In a real implementation, you would fetch file metadata from metadata-service
            // For now, we'll construct the storage path
            // This is a simplified version - in production, validate ownership via metadata service
            
            String storagePath = findStoragePath(fileId, userId);
            
            if (!objectStorageService.fileExists(storagePath)) {
                throw new ResourceNotFoundException("File not found: " + fileId);
            }
            
            Resource resource = objectStorageService.downloadFile(storagePath);
            
            // Publish download event
            publishDownloadEvent(fileId, userId);
            
            log.info("File downloaded successfully: fileId={}", fileId);
            return resource;
            
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to download file: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to download file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileId, String userId) {
        log.info("Deleting file: fileId={}, userId={}", fileId, userId);
        
        try {
            // In production, validate ownership via metadata service
            String storagePath = findStoragePath(fileId, userId);
            
            if (!objectStorageService.fileExists(storagePath)) {
                throw new ResourceNotFoundException("File not found: " + fileId);
            }
            
            objectStorageService.deleteFile(storagePath);
            
            // Publish delete event
            publishDeleteEvent(fileId, userId);
            
            log.info("File deleted successfully: fileId={}", fileId);
            
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete file: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to delete file: " + e.getMessage());
        }
    }

    @Override
    public FileMetadataDto getFileInfo(String fileId) {
        log.info("Getting file info: fileId={}", fileId);
        
        // In production, this would fetch from metadata-service
        // For now, return a placeholder
        throw new UnsupportedOperationException("File info retrieval should be done via metadata-service");
    }

    @Override
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }
        
        // Validate file size
        if (file.getSize() > AppConstants.MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("File size exceeds maximum allowed size of %s. Actual size: %s",
                            FileUtils.formatFileSize(AppConstants.MAX_FILE_SIZE),
                            FileUtils.formatFileSize(file.getSize()))
            );
        }
        
        // Validate file name
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name is empty");
        }
        
        // Validate content type (optional - can be restrictive)
        String contentType = file.getContentType();
        if (contentType != null && !FileUtils.isValidFileType(contentType, AppConstants.ALLOWED_FILE_TYPES)) {
            log.warn("Potentially unsupported file type: {}", contentType);
            // Uncomment to enforce strict validation:
            // throw new IllegalArgumentException("File type not allowed: " + contentType);
        }
        
        log.debug("File validation passed: fileName={}, size={}, type={}", 
                fileName, file.getSize(), contentType);
    }

    /**
     * Find storage path for a file (simplified - in production, query metadata-service)
     */
    private String findStoragePath(String fileId, String userId) {
        // This is a simplified implementation
        // In production, you would call metadata-service to get the actual storage path
        // For now, assume path format: userId/fileId.extension
        return userId + "/" + fileId;
    }

    /**
     * Publish file upload event to Kafka
     */
    private void publishUploadEvent(FileMetadataDto metadata) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("fileName", metadata.getFileName());
            payload.put("fileSize", metadata.getFileSize());
            payload.put("fileType", metadata.getFileType());
            payload.put("contentType", metadata.getContentType());
            payload.put("storagePath", metadata.getStoragePath());
            payload.put("checksum", metadata.getChecksum());
            
            FileEvent event = FileEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(FileEvent.EventType.FILE_UPLOADED.name())
                    .fileId(metadata.getFileId())
                    .fileName(metadata.getFileName())
                    .userId(metadata.getOwnerId())
                    .timestamp(LocalDateTime.now())
                    .payload(payload)
                    .source("file-service")
                    .build();
            
            kafkaProducerService.publishFileUploadedEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish upload event: {}", e.getMessage(), e);
            // Don't fail the upload if event publishing fails
        }
    }

    /**
     * Publish file download event to Kafka
     */
    private void publishDownloadEvent(String fileId, String userId) {
        try {
            FileEvent event = FileEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(FileEvent.EventType.FILE_DOWNLOAD_REQUESTED.name())
                    .fileId(fileId)
                    .userId(userId)
                    .timestamp(LocalDateTime.now())
                    .source("file-service")
                    .build();
            
            kafkaProducerService.publishFileDownloadedEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish download event: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish file delete event to Kafka
     */
    private void publishDeleteEvent(String fileId, String userId) {
        try {
            FileEvent event = FileEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(FileEvent.EventType.FILE_DELETED.name())
                    .fileId(fileId)
                    .userId(userId)
                    .timestamp(LocalDateTime.now())
                    .source("file-service")
                    .build();
            
            kafkaProducerService.publishFileDeletedEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish delete event: {}", e.getMessage(), e);
        }
    }
}
