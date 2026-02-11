package com.gnexdrive.thumbnailservice.service.impl;

import com.gnexdrive.thumbnailservice.config.StorageConfig;
import com.gnexdrive.thumbnailservice.dto.ThumbnailDto;
import com.gnexdrive.thumbnailservice.dto.ThumbnailRequestDto;
import com.gnexdrive.thumbnailservice.dto.ThumbnailStatusDto;
import com.gnexdrive.thumbnailservice.entity.ThumbnailMetadata;
import com.gnexdrive.thumbnailservice.entity.ThumbnailMetadata.ThumbnailSize;
import com.gnexdrive.thumbnailservice.entity.ThumbnailMetadata.ThumbnailStatus;
import com.gnexdrive.thumbnailservice.kafka.ThumbnailEventProducer;
import com.gnexdrive.thumbnailservice.mapper.ThumbnailMapper;
import com.gnexdrive.thumbnailservice.repository.ThumbnailMetadataRepository;
import com.gnexdrive.thumbnailservice.service.ThumbnailService;
import com.gnexdrive.thumbnailservice.service.ThumbnailGeneratorService;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Thumbnail Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThumbnailServiceImpl implements ThumbnailService {

    private final ThumbnailMetadataRepository thumbnailRepository;
    private final ThumbnailGeneratorService generatorService;
    private final ThumbnailMapper thumbnailMapper;
    private final MinioClient minioClient;
    private final StorageConfig.MinioProperties minioProperties;
    private final ThumbnailEventProducer eventProducer;

    @Value("${thumbnail.max-attempts:3}")
    private int maxAttempts;

    @Value("${thumbnail.retry-delay-minutes:5}")
    private int retryDelayMinutes;

    @Override
    @Transactional(readOnly = true)
    public ThumbnailDto getThumbnail(String fileId, ThumbnailSize size) {
        log.info("Getting thumbnail: fileId={}, size={}", fileId, size);
        
        return thumbnailRepository.findByFileIdAndSize(fileId, size)
                .filter(t -> t.getStatus() == ThumbnailStatus.READY)
                .map(thumbnail -> {
                    try {
                        String url = generatePresignedUrl(thumbnail.getStoragePath());
                        thumbnail.setUrl(url);
                        return thumbnailMapper.toDto(thumbnail);
                    } catch (Exception e) {
                        log.error("Failed to generate presigned URL", e);
                        return null;
                    }
                })
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public ThumbnailStatusDto getThumbnailStatus(String fileId) {
        log.info("Getting thumbnail status: fileId={}", fileId);
        
        List<ThumbnailMetadata> thumbnails = thumbnailRepository.findByFileId(fileId);
        
        if (thumbnails.isEmpty()) {
            return ThumbnailStatusDto.builder()
                    .fileId(fileId)
                    .overallStatus(ThumbnailStatus.PENDING)
                    .sizes(new ArrayList<>())
                    .build();
        }

        List<ThumbnailStatusDto.SizeStatus> sizeStatuses = thumbnails.stream()
                .map(t -> {
                    String url = null;
                    if (t.getStatus() == ThumbnailStatus.READY) {
                        try {
                            url = generatePresignedUrl(t.getStoragePath());
                        } catch (Exception e) {
                            log.error("Failed to generate presigned URL", e);
                        }
                    }
                    return ThumbnailStatusDto.SizeStatus.builder()
                            .size(t.getSize())
                            .status(t.getStatus())
                            .url(url)
                            .attemptCount(t.getAttemptCount())
                            .lastError(t.getLastError())
                            .build();
                })
                .collect(Collectors.toList());

        // Overall status: READY if all ready, FAILED if any failed, PROCESSING if any processing, PENDING otherwise
        ThumbnailStatus overallStatus = ThumbnailStatus.READY;
        if (thumbnails.stream().anyMatch(t -> t.getStatus() == ThumbnailStatus.FAILED)) {
            overallStatus = ThumbnailStatus.FAILED;
        } else if (thumbnails.stream().anyMatch(t -> t.getStatus() == ThumbnailStatus.PROCESSING)) {
            overallStatus = ThumbnailStatus.PROCESSING;
        } else if (thumbnails.stream().anyMatch(t -> t.getStatus() == ThumbnailStatus.PENDING)) {
            overallStatus = ThumbnailStatus.PENDING;
        }

        return ThumbnailStatusDto.builder()
                .fileId(fileId)
                .overallStatus(overallStatus)
                .sizes(sizeStatuses)
                .statusEndpoint("/api/v1/thumbnails/" + fileId + "/status")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThumbnailDto> getAllThumbnails(String fileId) {
        log.info("Getting all thumbnails: fileId={}", fileId);
        
        return thumbnailRepository.findReadyThumbnailsByFileId(fileId).stream()
                .map(thumbnail -> {
                    try {
                        String url = generatePresignedUrl(thumbnail.getStoragePath());
                        thumbnail.setUrl(url);
                        return thumbnailMapper.toDto(thumbnail);
                    } catch (Exception e) {
                        log.error("Failed to generate presigned URL", e);
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ThumbnailStatusDto requestThumbnailGeneration(ThumbnailRequestDto request, String userId) {
        log.info("Requesting thumbnail generation: request={}, userId={}", request, userId);
        
        String fileId = request.getFileId();
        
        // Check if thumbnails already exist
        List<ThumbnailMetadata> existing = thumbnailRepository.findByFileId(fileId);
        if (!existing.isEmpty() && !request.isForce()) {
            log.info("Thumbnails already exist for fileId={}", fileId);
            return getThumbnailStatus(fileId);
        }

        // Delete existing if force=true
        if (request.isForce() && !existing.isEmpty()) {
            existing.forEach(t -> {
                try {
                    deleteFromStorage(t.getStoragePath());
                } catch (Exception e) {
                    log.warn("Failed to delete existing thumbnail", e);
                }
            });
            thumbnailRepository.deleteAll(existing);
        }

        // Create pending records for all sizes
        List<ThumbnailSize> sizes = request.getSizes() != null && !request.getSizes().isEmpty()
                ? request.getSizes()
                : Arrays.asList(ThumbnailSize.values());

        for (ThumbnailSize size : sizes) {
            ThumbnailMetadata metadata = new ThumbnailMetadata();
            metadata.setFileId(fileId);
            metadata.setOwnerId(userId);
            metadata.setSize(size);
            metadata.setStatus(ThumbnailStatus.PENDING);
            metadata.setAttemptCount(0);
            metadata.setVersion(1);
            thumbnailRepository.save(metadata);
        }

        // Trigger async processing (will be triggered by Kafka event when file is uploaded)
        // For on-demand request, we don't have storage path here
        // processThumbnailJobAsync(fileId, userId, contentType, storagePath, version);

        return getThumbnailStatus(fileId);
    }

    @Override
    @Transactional
    public void deleteThumbnails(String fileId, String userId) {
        log.info("Deleting thumbnails: fileId={}, userId={}", fileId, userId);
        
        List<ThumbnailMetadata> thumbnails = thumbnailRepository.findByFileId(fileId);
        
        for (ThumbnailMetadata thumbnail : thumbnails) {
            if (!thumbnail.getOwnerId().equals(userId)) {
                log.warn("User {} does not own thumbnail for file {}", userId, fileId);
                continue;
            }
            
            try {
                deleteFromStorage(thumbnail.getStoragePath());
            } catch (Exception e) {
                log.error("Failed to delete thumbnail from storage", e);
            }
        }
        
        thumbnailRepository.deleteAll(thumbnails);
        eventProducer.publishThumbnailDeleted(fileId, userId);
        
        log.info("Deleted {} thumbnails for fileId={}", thumbnails.size(), fileId);
    }

    @Async("thumbnailExecutor")
    public void processThumbnailJobAsync(String fileId, String ownerId, String contentType, 
                                         String storagePath, Integer version) {
        processThumbnailJob(fileId, ownerId, contentType, storagePath, version);
    }

    @Override
    @Transactional
    public void processThumbnailJob(String fileId, String ownerId, String contentType, 
                                   String storagePath, Integer version) {
        log.info("Processing thumbnail job: fileId={}, contentType={}, version={}", 
                fileId, contentType, version);
        
        if (!generatorService.supports(contentType)) {
            log.warn("Unsupported content type for thumbnails: {}", contentType);
            return;
        }

        // Check for existing pending thumbnails
        List<ThumbnailMetadata> pendingThumbnails = thumbnailRepository.findByFileIdAndStatus(
                fileId, ThumbnailStatus.PENDING);
        
        // If no pending thumbnails exist, create them for all sizes
        if (pendingThumbnails.isEmpty()) {
            log.info("No pending thumbnails found, creating for all sizes");
            for (ThumbnailSize size : ThumbnailSize.values()) {
                ThumbnailMetadata metadata = new ThumbnailMetadata();
                metadata.setFileId(fileId);
                metadata.setOwnerId(ownerId);
                metadata.setSize(size);
                metadata.setStatus(ThumbnailStatus.PENDING);
                metadata.setAttemptCount(0);
                metadata.setVersion(version != null ? version : 1);
                ThumbnailMetadata saved = thumbnailRepository.save(metadata);
                pendingThumbnails.add(saved);
            }
        }

        for (ThumbnailMetadata thumbnail : pendingThumbnails) {
            try {
                thumbnail.setStatus(ThumbnailStatus.PROCESSING);
                thumbnail.setAttemptCount(thumbnail.getAttemptCount() + 1);
                thumbnailRepository.save(thumbnail);

                // Download original file
                byte[] originalFile = downloadFromStorage(storagePath);
                
                // Generate thumbnail
                String outputFormat = generatorService.getRecommendedFormat(contentType);
                ByteArrayOutputStream thumbnailOutput = new ByteArrayOutputStream();
                
                try (InputStream input = new ByteArrayInputStream(originalFile)) {
                    generatorService.generateThumbnail(input, thumbnailOutput, 
                            thumbnail.getSize(), contentType, outputFormat);
                }
                
                byte[] thumbnailData = thumbnailOutput.toByteArray();
                
                // Upload to storage
                String thumbnailPath = String.format("thumbnails/%s/%s_%s.%s",
                        ownerId, fileId, thumbnail.getSize().name().toLowerCase(), outputFormat);
                
                uploadToStorage(thumbnailPath, thumbnailData, "image/" + outputFormat);
                
                // Update metadata
                thumbnail.setStoragePath(thumbnailPath);
                thumbnail.setFormat(outputFormat);
                thumbnail.setWidth(thumbnail.getSize().getWidth());
                thumbnail.setHeight(thumbnail.getSize().getHeight());
                thumbnail.setFileSize((long) thumbnailData.length);
                thumbnail.setStatus(ThumbnailStatus.READY);
                thumbnail.setLastError(null);
                thumbnailRepository.save(thumbnail);
                
                // Publish event
                eventProducer.publishThumbnailReady(fileId, thumbnail.getSize(), thumbnailPath);
                
                log.info("Successfully generated thumbnail: fileId={}, size={}", fileId, thumbnail.getSize());
                
            } catch (Exception e) {
                log.error("Failed to generate thumbnail: fileId={}, size={}", fileId, thumbnail.getSize(), e);
                
                thumbnail.setStatus(thumbnail.getAttemptCount() >= maxAttempts 
                        ? ThumbnailStatus.FAILED : ThumbnailStatus.PENDING);
                thumbnail.setLastError(e.getMessage());
                thumbnailRepository.save(thumbnail);
                
                if (thumbnail.getAttemptCount() >= maxAttempts) {
                    eventProducer.publishThumbnailFailed(fileId, thumbnail.getSize(), e.getMessage());
                }
            }
        }
    }

    @Override
    @Transactional
    public void retryFailedThumbnails() {
        log.info("Retrying failed thumbnails");
        
        LocalDateTime retryThreshold = LocalDateTime.now().minusMinutes(retryDelayMinutes);
        List<ThumbnailMetadata> toRetry = thumbnailRepository.findPendingForRetry(
                retryThreshold, maxAttempts);
        
        log.info("Found {} thumbnails to retry", toRetry.size());
        
        for (ThumbnailMetadata thumbnail : toRetry) {
            log.info("Retrying thumbnail: fileId={}, size={}, attempt={}", 
                    thumbnail.getFileId(), thumbnail.getSize(), thumbnail.getAttemptCount() + 1);
            
            // Re-enqueue processing job
            processThumbnailJobAsync(thumbnail.getFileId(), thumbnail.getOwnerId(), 
                    "image/jpeg", // Default, should be stored in metadata
                    String.format("files/%s/%s", thumbnail.getOwnerId(), thumbnail.getFileId()),
                    thumbnail.getVersion());
        }
    }

    private String generatePresignedUrl(String objectPath) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(minioProperties.getBucket().getThumbnails())
                        .object(objectPath)
                        .expiry(minioProperties.getPresignedUrlTtl(), TimeUnit.SECONDS)
                        .build()
        );
    }

    private byte[] downloadFromStorage(String objectPath) throws Exception {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioProperties.getBucket().getFiles())
                        .object(objectPath)
                        .build())) {
            return stream.readAllBytes();
        }
    }

    private void uploadToStorage(String objectPath, byte[] data, String contentType) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioProperties.getBucket().getThumbnails())
                        .object(objectPath)
                        .stream(new ByteArrayInputStream(data), data.length, -1)
                        .contentType(contentType)
                        .build()
        );
    }

    private void deleteFromStorage(String objectPath) throws Exception {
        if (objectPath == null) return;
        
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(minioProperties.getBucket().getThumbnails())
                        .object(objectPath)
                        .build()
        );
    }
}
