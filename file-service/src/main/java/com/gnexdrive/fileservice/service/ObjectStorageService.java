package com.gnexdrive.fileservice.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Service interface for object storage operations (MinIO/S3)
 */
public interface ObjectStorageService {

    /**
     * Upload file to object storage
     */
    String uploadFile(String fileName, InputStream inputStream, String contentType, long size);

    /**
     * Download file from object storage
     */
    Resource downloadFile(String fileName);

    /**
     * Delete file from object storage
     */
    void deleteFile(String fileName);

    /**
     * Check if file exists in object storage
     */
    boolean fileExists(String fileName);

    /**
     * Get file URL (pre-signed URL)
     */
    String getFileUrl(String fileName);

    /**
     * Get file metadata from storage
     */
    Object getFileMetadata(String fileName);
}
