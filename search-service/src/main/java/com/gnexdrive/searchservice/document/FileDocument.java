package com.gnexdrive.searchservice.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Elasticsearch document for file search
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDocument {

    private String fileId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String ownerId;
    private String ownerEmail;
    private String contentType;
    private LocalDateTime uploadedAt;
    private LocalDateTime modifiedAt;
    private String[] tags;
    private String status;
}
