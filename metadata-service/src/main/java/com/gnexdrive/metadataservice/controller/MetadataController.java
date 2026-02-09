package com.gnexdrive.metadataservice.controller;

import com.gnexdrive.common.dto.ApiResponse;
import com.gnexdrive.common.dto.FileMetadataDto;
import com.gnexdrive.metadataservice.service.MetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for metadata operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/metadata")
@RequiredArgsConstructor
@Tag(name = "Metadata Management", description = "File metadata operations")
public class MetadataController {

    private final MetadataService metadataService;

    @Operation(summary = "Get file metadata by ID")
    @GetMapping("/{fileId}")
    public ResponseEntity<ApiResponse<FileMetadataDto>> getMetadata(
            @PathVariable String fileId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        log.info("GET /api/v1/metadata/{} - User: {}", fileId, userId);
        
        try {
            FileMetadataDto metadata = metadataService.getMetadata(fileId);
            return ResponseEntity.ok(ApiResponse.success("File metadata retrieved successfully", metadata));
        } catch (com.gnexdrive.common.exception.ResourceNotFoundException e) {
            log.error("File metadata not found: {}", fileId);
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("File not found: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving metadata for file: {}", fileId, e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to retrieve metadata: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get all files for a user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<FileMetadataDto>>> getUserFiles(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        log.info("GET /api/v1/metadata/user/{} - Page: {}, Size: {}", userId, page, size);
        
        try {
            org.springframework.data.domain.Sort.Direction direction = 
                sortDir.equalsIgnoreCase("ASC") ? org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC;
            org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, sortBy));
            
            Page<FileMetadataDto> userFiles = metadataService.getUserFiles(userId, pageable);
            return ResponseEntity.ok(ApiResponse.success("User files retrieved successfully", userFiles));
        } catch (Exception e) {
            log.error("Error retrieving files for user: {}", userId, e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to retrieve user files: " + e.getMessage()));
        }
    }

    @Operation(summary = "Search files by name or type")
    @GetMapping("/user/{userId}/search")
    public ResponseEntity<ApiResponse<List<FileMetadataDto>>> searchFiles(
            @PathVariable String userId,
            @RequestParam String query) {
        log.info("GET /api/v1/metadata/user/{}/search?query={}", userId, query);
        
        try {
            List<FileMetadataDto> results = metadataService.searchFiles(userId, query);
            return ResponseEntity.ok(ApiResponse.success("Search completed successfully", results));
        } catch (Exception e) {
            log.error("Error searching files for user: {}", userId, e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Search failed: " + e.getMessage()));
        }
    }

    @Operation(summary = "Update file metadata")
    @PutMapping("/{fileId}")
    public ResponseEntity<ApiResponse<FileMetadataDto>> updateMetadata(
            @PathVariable String fileId,
            @RequestBody FileMetadataDto metadataDto,
            @RequestHeader("X-User-Id") String userId) {
        log.info("PUT /api/v1/metadata/{} - User: {}", fileId, userId);
        
        try {
            FileMetadataDto updated = metadataService.updateMetadata(fileId, metadataDto, userId);
            return ResponseEntity.ok(ApiResponse.success("Metadata updated successfully", updated));
        } catch (com.gnexdrive.common.exception.ResourceNotFoundException e) {
            log.error("File not found or unauthorized: {}", fileId);
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("File not found or unauthorized: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating metadata for file: {}", fileId, e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to update metadata: " + e.getMessage()));
        }
    }

    @Operation(summary = "Delete file metadata")
    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteMetadata(
            @PathVariable String fileId,
            @RequestHeader("X-User-Id") String userId) {
        log.info("DELETE /api/v1/metadata/{} - User: {}", fileId, userId);
        
        try {
            metadataService.deleteMetadata(fileId, userId);
            return ResponseEntity.ok(ApiResponse.success("Metadata deleted successfully"));
        } catch (com.gnexdrive.common.exception.ResourceNotFoundException e) {
            log.error("File not found or unauthorized: {}", fileId);
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("File not found or unauthorized: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting metadata for file: {}", fileId, e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to delete metadata: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get user storage statistics")
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<ApiResponse<Object>> getUserStats(@PathVariable String userId) {
        log.info("GET /api/v1/metadata/user/{}/stats", userId);
        
        try {
            Object stats = metadataService.getUserStorageStats(userId);
            return ResponseEntity.ok(ApiResponse.success("Storage statistics retrieved successfully", stats));
        } catch (Exception e) {
            log.error("Error retrieving storage stats for user: {}", userId, e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to retrieve storage stats: " + e.getMessage()));
        }
    }
}
