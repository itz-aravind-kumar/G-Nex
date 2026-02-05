package com.gnexdrive.metadataservice.service;

import com.gnexdrive.common.dto.FileMetadataDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for metadata operations
 */
public interface MetadataService {

    /**
     * Save file metadata
     */
    FileMetadataDto saveMetadata(FileMetadataDto metadataDto);

    /**
     * Get file metadata by ID
     */
    FileMetadataDto getMetadata(String fileId);

    /**
     * Get all files for a user
     */
    Page<FileMetadataDto> getUserFiles(String userId, Pageable pageable);

    /**
     * Update file metadata
     */
    FileMetadataDto updateMetadata(String fileId, FileMetadataDto metadataDto, String userId);

    /**
     * Delete file metadata
     */
    void deleteMetadata(String fileId, String userId);

    /**
     * Search files by name or type
     */
    List<FileMetadataDto> searchFiles(String userId, String searchTerm);

    /**
     * Get user storage statistics
     */
    Object getUserStorageStats(String userId);
}
