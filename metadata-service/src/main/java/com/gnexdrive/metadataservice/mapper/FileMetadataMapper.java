package com.gnexdrive.metadataservice.mapper;

import com.gnexdrive.common.dto.FileMetadataDto;
import com.gnexdrive.metadataservice.entity.FileMetadata;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Entity and DTO
 */
@Component
public class FileMetadataMapper {

    /**
     * Convert entity to DTO
     */
    public FileMetadataDto toDto(FileMetadata entity) {
        if (entity == null) {
            return null;
        }

        return FileMetadataDto.builder()
                .fileId(entity.getFileId())
                .fileName(entity.getFileName())
                .fileType(entity.getFileType())
                .fileSize(entity.getFileSize())
                .ownerId(entity.getOwnerId())
                .ownerEmail(entity.getOwnerEmail())
                .storagePath(entity.getStoragePath())
                .contentType(entity.getContentType())
                .checksum(entity.getChecksum())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .uploadedAt(entity.getUploadedAt())
                .modifiedAt(entity.getModifiedAt())
                .build();
    }

    /**
     * Convert DTO to entity
     */
    public FileMetadata toEntity(FileMetadataDto dto) {
        if (dto == null) {
            return null;
        }

        return FileMetadata.builder()
                .fileId(dto.getFileId())
                .fileName(dto.getFileName())
                .fileType(dto.getFileType())
                .fileSize(dto.getFileSize())
                .ownerId(dto.getOwnerId())
                .ownerEmail(dto.getOwnerEmail())
                .storagePath(dto.getStoragePath())
                .contentType(dto.getContentType())
                .checksum(dto.getChecksum())
                .status(dto.getStatus() != null ? FileMetadata.FileStatus.valueOf(dto.getStatus()) : null)
                .uploadedAt(dto.getUploadedAt())
                .modifiedAt(dto.getModifiedAt())
                .build();
    }
}
