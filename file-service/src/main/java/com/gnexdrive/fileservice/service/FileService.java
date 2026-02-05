package com.gnexdrive.fileservice.service;

import com.gnexdrive.common.dto.FileMetadataDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for file operations
 */
public interface FileService {

    /**
     * Upload a file to object storage
     */
    FileMetadataDto uploadFile(MultipartFile file, String userId);

    /**
     * Download a file from object storage
     */
    Resource downloadFile(String fileId, String userId);

    /**
     * Delete a file from object storage
     */
    void deleteFile(String fileId, String userId);

    /**
     * Get file metadata
     */
    FileMetadataDto getFileInfo(String fileId);

    /**
     * Validate file before upload
     */
    void validateFile(MultipartFile file);
}
