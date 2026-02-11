package com.gnexdrive.thumbnailservice.service;

import com.gnexdrive.thumbnailservice.entity.ThumbnailMetadata.ThumbnailSize;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Thumbnail Generator Service Interface
 * Handles actual image/pdf/video thumbnail generation
 */
public interface ThumbnailGeneratorService {

    /**
     * Check if content type supports thumbnail generation
     */
    boolean supports(String contentType);

    /**
     * Generate thumbnail from input stream
     * 
     * @param inputStream Original file input stream
     * @param outputStream Thumbnail output stream
     * @param size Target thumbnail size
     * @param contentType Original file content type
     * @param outputFormat Desired output format (webp, jpg, png)
     */
    void generateThumbnail(InputStream inputStream, OutputStream outputStream, 
                          ThumbnailSize size, String contentType, String outputFormat);

    /**
     * Get recommended output format for content type
     */
    String getRecommendedFormat(String contentType);
}
