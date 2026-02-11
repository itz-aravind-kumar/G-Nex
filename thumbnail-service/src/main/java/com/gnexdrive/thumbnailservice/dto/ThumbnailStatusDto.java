package com.gnexdrive.thumbnailservice.dto;

import com.gnexdrive.thumbnailservice.entity.ThumbnailMetadata.ThumbnailSize;
import com.gnexdrive.thumbnailservice.entity.ThumbnailMetadata.ThumbnailStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Thumbnail Status DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThumbnailStatusDto {

    private String fileId;
    private ThumbnailStatus overallStatus;
    private List<SizeStatus> sizes;
    private String statusEndpoint;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SizeStatus {
        private ThumbnailSize size;
        private ThumbnailStatus status;
        private String url;
        private Integer attemptCount;
        private String lastError;
    }
}
