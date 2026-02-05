package com.gnexdrive.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * File metadata DTO shared across services
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataDto {
    
    private String fileId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String ownerId;
    private String ownerEmail;
    private String storagePath;
    private String contentType;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime uploadedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime modifiedAt;
    
    private String checksum;
    private FileStatus status;
    
    public enum FileStatus {
        UPLOADING,
        UPLOADED,
        PROCESSING,
        AVAILABLE,
        DELETED,
        ERROR
    }
}
