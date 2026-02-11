package com.gnexdrive.thumbnailservice.service.impl;

import com.gnexdrive.thumbnailservice.entity.ThumbnailMetadata.ThumbnailSize;
import com.gnexdrive.thumbnailservice.service.ThumbnailGeneratorService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Thumbnail Generator Service Implementation
 * Uses Thumbnailator for images, PDFBox for PDFs
 */
@Slf4j
@Service
public class ThumbnailGeneratorServiceImpl implements ThumbnailGeneratorService {

    @Value("${thumbnail.formats.preferred:webp}")
    private String preferredFormat;

    @Value("${thumbnail.formats.fallback:jpg}")
    private String fallbackFormat;

    private static final float THUMBNAIL_QUALITY = 0.95f; // High quality for sharp thumbnails
    private static final int PDF_DPI = 200; // Higher DPI for clearer PDF thumbnails

    @Override
    public boolean supports(String contentType) {
        if (contentType == null) {
            return false;
        }
        
        String type = contentType.toLowerCase();
        return type.startsWith("image/") || type.equals("application/pdf");
    }

    @Override
    public void generateThumbnail(InputStream inputStream, OutputStream outputStream,
                                 ThumbnailSize size, String contentType, String outputFormat) {
        try {
            log.info("Generating thumbnail: size={}, contentType={}, format={}", 
                    size, contentType, outputFormat);
            
            if (contentType.startsWith("image/")) {
                generateImageThumbnail(inputStream, outputStream, size, outputFormat);
            } else if (contentType.equals("application/pdf")) {
                generatePdfThumbnail(inputStream, outputStream, size, outputFormat);
            } else {
                throw new IllegalArgumentException("Unsupported content type: " + contentType);
            }
            
            log.info("Successfully generated thumbnail: size={}", size);
        } catch (Exception e) {
            log.error("Failed to generate thumbnail: size={}, contentType={}", size, contentType, e);
            throw new RuntimeException("Thumbnail generation failed: " + e.getMessage(), e);
        }
    }

    private void generateImageThumbnail(InputStream inputStream, OutputStream outputStream,
                                       ThumbnailSize size, String outputFormat) throws Exception {
        Thumbnails.of(inputStream)
                .size(size.getWidth(), size.getHeight())
                .keepAspectRatio(true)
                .outputQuality(THUMBNAIL_QUALITY)
                .outputFormat(outputFormat)
                .toOutputStream(outputStream);
    }

    private void generatePdfThumbnail(InputStream inputStream, OutputStream outputStream,
                                     ThumbnailSize size, String outputFormat) throws Exception {
        try (PDDocument document = PDDocument.load(inputStream)) {
            if (document.getNumberOfPages() == 0) {
                throw new IllegalArgumentException("PDF has no pages");
            }
            
            // Render first page
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, PDF_DPI);
            
            // Convert to thumbnail
            ByteArrayOutputStream tempOutput = new ByteArrayOutputStream();
            Thumbnails.of(image)
                    .size(size.getWidth(), size.getHeight())
                    .keepAspectRatio(true)
                    .outputQuality(THUMBNAIL_QUALITY)
                    .outputFormat(outputFormat)
                    .toOutputStream(tempOutput);
            
            // Write to final output
            tempOutput.writeTo(outputStream);
        }
    }

    @Override
    public String getRecommendedFormat(String contentType) {
        // Use preferred format (webp) for modern browsers
        // Fall back to jpg if webp not supported
        if (contentType != null && contentType.contains("png") && hasTransparency(contentType)) {
            return "png"; // Preserve transparency for PNGs
        }
        return preferredFormat;
    }

    private boolean hasTransparency(String contentType) {
        return contentType.contains("png") || contentType.contains("webp");
    }
}
