package com.gnexdrive.fileservice.client;

import com.gnexdrive.common.dto.ApiResponse;
import com.gnexdrive.common.dto.FileMetadataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Client to call Metadata Service via API Gateway
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetadataServiceClient {

    private final RestTemplate restTemplate;

    @Value("${metadata.service.url:http://localhost:8080}")
    private String metadataServiceUrl;

    /**
     * Get file metadata by fileId
     */
    public FileMetadataDto getFileMetadata(String fileId) {
        try {
            String url = metadataServiceUrl + "/api/v1/metadata/" + fileId;
            log.debug("Calling metadata service: {}", url);

            ResponseEntity<ApiResponse<FileMetadataDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<FileMetadataDto>>() {}
            );

            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData();
            }

            log.warn("No metadata found for fileId: {}", fileId);
            return null;

        } catch (Exception e) {
            log.error("Failed to get file metadata: fileId={}, error={}", fileId, e.getMessage());
            throw new RuntimeException("Failed to retrieve file metadata: " + e.getMessage());
        }
    }

    /**
     * Verify if user owns the file
     */
    public boolean verifyOwnership(String fileId, String userId) {
        try {
            FileMetadataDto metadata = getFileMetadata(fileId);
            
            if (metadata == null) {
                log.warn("File not found: fileId={}", fileId);
                return false;
            }

            boolean isOwner = metadata.getOwnerId().equals(userId);
            log.debug("Ownership verification: fileId={}, userId={}, isOwner={}", 
                    fileId, userId, isOwner);
            
            return isOwner;

        } catch (Exception e) {
            log.error("Ownership verification failed: fileId={}, userId={}, error={}", 
                    fileId, userId, e.getMessage());
            return false;
        }
    }
}
