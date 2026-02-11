package com.gnexdrive.thumbnailservice.mapper;

import com.gnexdrive.thumbnailservice.dto.ThumbnailDto;
import com.gnexdrive.thumbnailservice.entity.ThumbnailMetadata;
import org.springframework.stereotype.Component;

/**
 * Mapper for Thumbnail Entity <-> DTO
 */
@Component
public class ThumbnailMapper {

    /**
     * Convert entity to DTO
     */
    public ThumbnailDto toDto(ThumbnailMetadata entity) {
        if (entity == null) {
            return null;
        }
        
        return ThumbnailDto.builder()
                .id(entity.getId())
                .fileId(entity.getFileId())
                .ownerId(entity.getOwnerId())
                .size(entity.getSize())
                .url(entity.getUrl())
                .format(entity.getFormat())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .fileSize(entity.getFileSize())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Convert DTO to entity
     */
    public ThumbnailMetadata toEntity(ThumbnailDto dto) {
        if (dto == null) {
            return null;
        }
        
        ThumbnailMetadata entity = new ThumbnailMetadata();
        entity.setId(dto.getId());
        entity.setFileId(dto.getFileId());
        entity.setOwnerId(dto.getOwnerId());
        entity.setSize(dto.getSize());
        entity.setUrl(dto.getUrl());
        entity.setFormat(dto.getFormat());
        entity.setWidth(dto.getWidth());
        entity.setHeight(dto.getHeight());
        entity.setFileSize(dto.getFileSize());
        entity.setStatus(dto.getStatus());
        return entity;
    }
}
