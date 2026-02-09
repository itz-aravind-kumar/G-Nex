package com.gnexdrive.metadataservice.service.impl;

import com.gnexdrive.common.dto.FileMetadataDto;
import com.gnexdrive.metadataservice.repository.FileMetadataRepository;
import com.gnexdrive.metadataservice.service.MetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of Metadata Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetadataServiceImpl implements MetadataService {

    private final FileMetadataRepository fileMetadataRepository;
    private final com.gnexdrive.metadataservice.mapper.FileMetadataMapper fileMetadataMapper;

    @Override
    @Transactional
    public FileMetadataDto saveMetadata(FileMetadataDto metadataDto) {
        log.info("Saving metadata for file: {}", metadataDto.getFileId());
        
        try {
            // Convert DTO to entity
            com.gnexdrive.metadataservice.entity.FileMetadata entity = fileMetadataMapper.toEntity(metadataDto);
            
            // Save to database
            com.gnexdrive.metadataservice.entity.FileMetadata savedEntity = fileMetadataRepository.save(entity);
            
            log.info("Metadata saved successfully for file: {}", savedEntity.getFileId());
            return fileMetadataMapper.toDto(savedEntity);
        } catch (Exception e) {
            log.error("Error saving metadata for file: {}", metadataDto.getFileId(), e);
            throw new RuntimeException("Failed to save metadata: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FileMetadataDto getMetadata(String fileId) {
        log.info("Fetching metadata for file: {}", fileId);
        
        com.gnexdrive.metadataservice.entity.FileMetadata entity = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> {
                    log.error("File metadata not found: {}", fileId);
                    return new com.gnexdrive.common.exception.ResourceNotFoundException(
                        "File metadata not found with ID: " + fileId
                    );
                });
        
        return fileMetadataMapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileMetadataDto> getUserFiles(String userId, Pageable pageable) {
        log.info("Fetching files for user: {} with page: {}, size: {}", userId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<com.gnexdrive.metadataservice.entity.FileMetadata> entityPage = fileMetadataRepository.findByOwnerId(userId, pageable);
        
        return entityPage.map(fileMetadataMapper::toDto);
    }

    @Override
    @Transactional
    public FileMetadataDto updateMetadata(String fileId, FileMetadataDto metadataDto, String userId) {
        log.info("Updating metadata for file: {} by user: {}", fileId, userId);
        
        // Fetch existing metadata
        com.gnexdrive.metadataservice.entity.FileMetadata existing = fileMetadataRepository.findByFileIdAndOwnerId(fileId, userId)
                .orElseThrow(() -> {
                    log.error("File not found or user not authorized. File: {}, User: {}", fileId, userId);
                    return new com.gnexdrive.common.exception.ResourceNotFoundException(
                        "File not found or you don't have permission to update it"
                    );
                });
        
        // Update only allowed fields
        if (metadataDto.getFileName() != null) {
            existing.setFileName(metadataDto.getFileName());
        }
        if (metadataDto.getStatus() != null) {
            existing.setStatus(com.gnexdrive.metadataservice.entity.FileMetadata.FileStatus.valueOf(metadataDto.getStatus()));
        }
        
        com.gnexdrive.metadataservice.entity.FileMetadata updated = fileMetadataRepository.save(existing);
        log.info("Metadata updated successfully for file: {}", fileId);
        
        return fileMetadataMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteMetadata(String fileId, String userId) {
        log.info("Deleting metadata for file: {} by user: {}", fileId, userId);
        
        // Verify ownership before deletion
        com.gnexdrive.metadataservice.entity.FileMetadata existing = fileMetadataRepository.findByFileIdAndOwnerId(fileId, userId)
                .orElseThrow(() -> {
                    log.error("File not found or user not authorized. File: {}, User: {}", fileId, userId);
                    return new com.gnexdrive.common.exception.ResourceNotFoundException(
                        "File not found or you don't have permission to delete it"
                    );
                });
        
        // Mark as deleted instead of hard delete
        existing.setStatus(com.gnexdrive.metadataservice.entity.FileMetadata.FileStatus.DELETED);
        fileMetadataRepository.save(existing);
        
        log.info("Metadata deleted successfully for file: {}", fileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileMetadataDto> searchFiles(String userId, String searchTerm) {
        log.info("Searching files for user: {} with term: {}", userId, searchTerm);
        
        List<com.gnexdrive.metadataservice.entity.FileMetadata> entities = fileMetadataRepository.searchFilesByOwner(userId, searchTerm);
        
        return entities.stream()
                .map(fileMetadataMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Object getUserStorageStats(String userId) {
        log.info("Calculating storage stats for user: {}", userId);
        
        long totalFiles = fileMetadataRepository.countByOwnerId(userId);
        Long totalStorage = fileMetadataRepository.getTotalStorageByOwner(userId);
        
        return java.util.Map.of(
            "userId", userId,
            "totalFiles", totalFiles,
            "totalStorage", totalStorage != null ? totalStorage : 0L,
            "totalStorageFormatted", com.gnexdrive.common.util.FileUtils.formatFileSize(totalStorage != null ? totalStorage : 0L)
        );
    }
}
