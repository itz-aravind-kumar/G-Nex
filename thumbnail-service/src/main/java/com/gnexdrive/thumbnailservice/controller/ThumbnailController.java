package com.gnexdrive.thumbnailservice.controller;

import com.gnexdrive.common.dto.ApiResponse;
import com.gnexdrive.thumbnailservice.dto.ThumbnailDto;
import com.gnexdrive.thumbnailservice.dto.ThumbnailRequestDto;
import com.gnexdrive.thumbnailservice.dto.ThumbnailStatusDto;
import com.gnexdrive.thumbnailservice.entity.ThumbnailMetadata.ThumbnailSize;
import com.gnexdrive.thumbnailservice.service.ThumbnailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST Controller for thumbnail operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/thumbnails")
@RequiredArgsConstructor
@Tag(name = "Thumbnail Management", description = "Thumbnail generation and retrieval")
public class ThumbnailController {

    private final ThumbnailService thumbnailService;

    @Operation(summary = "Get thumbnail for a file")
    @GetMapping("/{fileId}")
    public ResponseEntity<?> getThumbnail(
            @PathVariable String fileId,
            @RequestParam(defaultValue = "GRID") ThumbnailSize size,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        log.info("Get thumbnail request: fileId={}, size={}, userId={}", fileId, size, userId);
        
        // TODO: Implement thumbnail retrieval logic
        // 1. Check if thumbnail exists and is ready
        // 2. If ready: return 303 redirect to presigned URL or 200 with URL
        // 3. If pending: return 202 Accepted with status endpoint
        // 4. If failed or not exists: return 404 or trigger generation
        
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "Get thumbnail status for a file")
    @GetMapping("/{fileId}/status")
    public ResponseEntity<ApiResponse<ThumbnailStatusDto>> getThumbnailStatus(
            @PathVariable String fileId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        log.info("Get thumbnail status: fileId={}, userId={}", fileId, userId);
        
        try {
            // TODO: Implement status retrieval
            // 1. Get all thumbnails for fileId
            // 2. Build status response with all sizes
            // 3. Return overall status and individual size statuses
            
            ThumbnailStatusDto status = thumbnailService.getThumbnailStatus(fileId);
            return ResponseEntity.ok(ApiResponse.success(status, "Thumbnail status retrieved"));
            
        } catch (Exception e) {
            log.error("Error getting thumbnail status: fileId={}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get thumbnail status: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get all thumbnails for a file")
    @GetMapping("/{fileId}/all")
    public ResponseEntity<ApiResponse<List<ThumbnailDto>>> getAllThumbnails(
            @PathVariable String fileId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        log.info("Get all thumbnails: fileId={}, userId={}", fileId, userId);
        
        try {
            // TODO: Implement get all thumbnails
            // 1. Validate user access
            // 2. Get all ready thumbnails for fileId
            // 3. Return list of thumbnail DTOs
            
            List<ThumbnailDto> thumbnails = thumbnailService.getAllThumbnails(fileId);
            return ResponseEntity.ok(ApiResponse.success(thumbnails, "Thumbnails retrieved"));
            
        } catch (Exception e) {
            log.error("Error getting thumbnails: fileId={}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get thumbnails: " + e.getMessage()));
        }
    }

    @Operation(summary = "Request thumbnail generation (on-demand)")
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<ThumbnailStatusDto>> requestThumbnail(
            @RequestBody ThumbnailRequestDto request,
            @RequestHeader("X-User-Id") String userId) {
        
        log.info("Thumbnail generation request: fileId={}, sizes={}, userId={}", 
                request.getFileId(), request.getSizes(), userId);
        
        try {
            // TODO: Implement on-demand thumbnail generation
            // 1. Validate user owns the file
            // 2. Check if thumbnails already exist (unless force=true)
            // 3. Enqueue thumbnail jobs
            // 4. Return status
            
            ThumbnailStatusDto status = thumbnailService.requestThumbnailGeneration(request, userId);
            return ResponseEntity.accepted()
                    .body(ApiResponse.success(status, "Thumbnail generation requested"));
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid thumbnail request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error requesting thumbnail: {}", request, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to request thumbnail: " + e.getMessage()));
        }
    }

    @Operation(summary = "Delete all thumbnails for a file")
    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteThumbnails(
            @PathVariable String fileId,
            @RequestHeader("X-User-Id") String userId) {
        
        log.info("Delete thumbnails request: fileId={}, userId={}", fileId, userId);
        
        try {
            // TODO: Implement thumbnail deletion
            // 1. Validate user owns the file
            // 2. Delete all thumbnail metadata
            // 3. Delete thumbnail files from storage
            // 4. Publish thumbnail.deleted event
            
            thumbnailService.deleteThumbnails(fileId, userId);
            return ResponseEntity.ok(ApiResponse.success(null, "Thumbnails deleted successfully"));
            
        } catch (Exception e) {
            log.error("Error deleting thumbnails: fileId={}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete thumbnails: " + e.getMessage()));
        }
    }
}
