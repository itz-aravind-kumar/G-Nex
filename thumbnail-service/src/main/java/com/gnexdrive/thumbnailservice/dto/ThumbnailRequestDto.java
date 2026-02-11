package com.gnexdrive.thumbnailservice.dto;

import com.gnexdrive.thumbnailservice.entity.ThumbnailMetadata.ThumbnailSize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Thumbnail Request DTO (for manual/on-demand generation)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThumbnailRequestDto {

    private String fileId;
    private List<ThumbnailSize> sizes;
    private boolean force; // regenerate if exists
}
