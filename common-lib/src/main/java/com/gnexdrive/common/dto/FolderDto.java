package com.gnexdrive.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing a folder
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderDto {
    
    private String folderId;
    private String folderName;
    private String parentId;
    private String parentName;
    private String ownerId;
    private String ownerEmail;
    private String path;
    private String color;
    private boolean isStarred;
    private boolean isTrashed;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private int fileCount;
    private int subfolderCount;
    private String sharedWith; // For display purposes
    private String permission; // User's permission on this folder
}
