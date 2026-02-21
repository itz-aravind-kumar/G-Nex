package com.gnexdrive.metadataservice.service;

import com.gnexdrive.common.dto.*;

import java.util.List;

/**
 * Service for managing sharing of files and folders
 */
public interface ShareService {

    /**
     * Share a resource with a user
     */
    PermissionDto shareResource(ShareRequestDto request, String grantorId);

    /**
     * Revoke a permission
     */
    void revokePermission(String permissionId, String requesterId);

    /**
     * Get all permissions for a resource
     */
    List<PermissionDto> getResourcePermissions(String resourceType, String resourceId, String requesterId);

    /**
     * Get resources shared with a user
     */
    List<FileMetadataDto> getFilesSharedWithUser(String userId);

    /**
     * Get folders shared with a user
     */
    List<FolderDto> getFoldersSharedWithUser(String userId);

    /**
     * Check if user has permission to access a resource
     */
    boolean hasPermission(String resourceType, String resourceId, String userId, String requiredRole);

    /**
     * Check if user can access a resource (is owner or has permission)
     */
    boolean canAccess(String resourceType, String resourceId, String userId);

    /**
     * Create a share link
     */
    ShareLinkDto createShareLink(ShareLinkRequestDto request, String creatorId);

    /**
     * Get share link by token
     */
    ShareLinkDto getShareLinkByToken(String token, String password);

    /**
     * Get share links for a resource
     */
    List<ShareLinkDto> getResourceShareLinks(String resourceType, String resourceId, String requesterId);

    /**
     * Deactivate a share link
     */
    void deactivateShareLink(String linkId, String requesterId);

    /**
     * Record share link access
     */
    void recordShareLinkAccess(String token);

    /**
     * Update permission role
     */
    PermissionDto updatePermissionRole(String permissionId, String newRole, String requesterId);

    /**
     * Get user's permission on a resource
     */
    PermissionDto getUserPermission(String resourceType, String resourceId, String userId);
}
