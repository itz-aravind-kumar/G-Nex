package com.gnexdrive.fileservice.service.impl;

import com.gnexdrive.common.exception.FileStorageException;
import com.gnexdrive.fileservice.config.MinioConfig;
import com.gnexdrive.fileservice.service.ObjectStorageService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * MinIO implementation of Object Storage Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService implements ObjectStorageService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @Override
    public String uploadFile(String fileName, InputStream inputStream, String contentType, long size) {
        try {
            log.info("Uploading file to MinIO: {}", fileName);
            
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build();
            
            minioClient.putObject(args);
            
            log.info("File uploaded successfully to MinIO: {}", fileName);
            return fileName;
            
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to upload file to object storage: " + e.getMessage());
        }
    }

    @Override
    public Resource downloadFile(String fileName) {
        try {
            log.info("Downloading file from MinIO: {}", fileName);
            
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .build();
            
            InputStream inputStream = minioClient.getObject(args);
            
            log.info("File downloaded successfully from MinIO: {}", fileName);
            return new InputStreamResource(inputStream);
            
        } catch (Exception e) {
            log.error("Failed to download file from MinIO: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to download file from object storage: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            log.info("Deleting file from MinIO: {}", fileName);
            
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .build();
            
            minioClient.removeObject(args);
            
            log.info("File deleted successfully from MinIO: {}", fileName);
            
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to delete file from object storage: " + e.getMessage());
        }
    }

    @Override
    public boolean fileExists(String fileName) {
        try {
            StatObjectArgs args = StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .build();
            
            minioClient.statObject(args);
            return true;
            
        } catch (Exception e) {
            log.debug("File does not exist in MinIO: {}", fileName);
            return false;
        }
    }

    @Override
    public String getFileUrl(String fileName) {
        try {
            log.debug("Generating pre-signed URL for file: {}", fileName);
            
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .expiry(1, TimeUnit.HOURS)
                    .build();
            
            String url = minioClient.getPresignedObjectUrl(args);
            
            log.debug("Pre-signed URL generated: {}", url);
            return url;
            
        } catch (Exception e) {
            log.error("Failed to generate pre-signed URL: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to generate file URL: " + e.getMessage());
        }
    }

    @Override
    public Object getFileMetadata(String fileName) {
        try {
            StatObjectArgs args = StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .build();
            
            StatObjectResponse response = minioClient.statObject(args);
            
            log.debug("Retrieved metadata for file: {}", fileName);
            return response;
            
        } catch (Exception e) {
            log.error("Failed to retrieve file metadata: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to retrieve file metadata: " + e.getMessage());
        }
    }
}
