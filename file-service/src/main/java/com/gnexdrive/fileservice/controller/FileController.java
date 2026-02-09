package com.gnexdrive.fileservice.controller;

import com.gnexdrive.common.dto.ApiResponse;
import com.gnexdrive.common.dto.FileMetadataDto;
import com.gnexdrive.fileservice.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for file operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "File Management", description = "File upload/download operations")
public class FileController {

    private final FileService fileService;

    @Operation(summary = "Upload a file")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileMetadataDto>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "anonymous") String userId) {
        
        log.info("Upload request received: fileName={}, size={}, userId={}", 
                file.getOriginalFilename(), file.getSize(), userId);
        
        try {
            FileMetadataDto metadata = fileService.uploadFile(file, userId);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("File uploaded successfully", metadata));
                    
        } catch (IllegalArgumentException e) {
            log.warn("File upload validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
                    
        } catch (Exception e) {
            log.error("File upload failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("File upload failed: " + e.getMessage()));
        }
    }

    @Operation(summary = "Download a file")
    @GetMapping("/{fileId}/download")
    public ResponseEntity<?> downloadFile(
            @PathVariable String fileId,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "anonymous") String userId) {
        
        log.info("Download request received: fileId={}, userId={}", fileId, userId);
        
        try {
            Resource resource = fileService.downloadFile(fileId, userId);
            
            // Set headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + fileId + "\"");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("File download failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("File not found or download failed: " + e.getMessage()));
        }
    }

    @Operation(summary = "Delete a file")
    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable String fileId,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "anonymous") String userId) {
        
        log.info("Delete request received: fileId={}, userId={}", fileId, userId);
        
        try {
            fileService.deleteFile(fileId, userId);
            
            return ResponseEntity.ok(
                    ApiResponse.success("File deleted successfully", null));
                    
        } catch (Exception e) {
            log.error("File deletion failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("File deletion failed: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get file info")
    @GetMapping("/{fileId}")
    public ResponseEntity<ApiResponse<FileMetadataDto>> getFileInfo(
            @PathVariable String fileId) {
        
        log.info("Get file info request: fileId={}", fileId);
        
        try {
            FileMetadataDto metadata = fileService.getFileInfo(fileId);
            
            return ResponseEntity.ok(
                    ApiResponse.success("File info retrieved successfully", metadata));
                    
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(ApiResponse.error("File info should be retrieved from metadata-service"));
                    
        } catch (Exception e) {
            log.error("Failed to get file info: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("File not found: " + e.getMessage()));
        }
    }
}
