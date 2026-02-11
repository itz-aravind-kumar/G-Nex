package com.gnexdrive.thumbnailservice.dto;

import com.gnexdrive.thumbnailservice.entity.ThumbnailMetadata.ThumbnailSize;
import com.gnexdrive.thumbnailservice.entity.ThumbnailMetadata.ThumbnailStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Thumbnail DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThumbnailDto {

    private String id;
    private String fileId;
    private String ownerId;
    private ThumbnailSize size;
    private String url;
    private String format;
    private Integer width;
    private Integer height;
    private Long fileSize;
    private ThumbnailStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
