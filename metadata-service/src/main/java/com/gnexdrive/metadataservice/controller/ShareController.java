package com.gnexdrive.metadataservice.controller;

import com.gnexdrive.common.dto.*;
import com.gnexdrive.metadataservice.service.ShareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    /**
     * Share a file or folder with a user
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PermissionDto>> shareResource(
            @RequestBody ShareRequestDto request,
            @RequestHeader("X-User-Id") String userId) {
        log.info("Share request from user {} for resource {}", userId, request.getResourceId());
        PermissionDto permission = shareService.shareResource(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Resource shared successfully", permission));
    }

    /**
     * Revoke a permission
     */
    @DeleteMapping("/permission/{permissionId}")
    public ResponseEntity<ApiResponse<Void>> revokePermission(
            @PathVariable String permissionId,
            @RequestHeader("X-User-Id") String userId) {
        log.info("Revoke permission {} by user {}", permissionId, userId);
        shareService.revokePermission(permissionId, userId);
        return ResponseEntity.ok(ApiResponse.success("Permission revoked successfully", null));
    }

    /**
     * Get all permissions for a resource
     */
    @GetMapping("/{resourceType}/{resourceId}/permissions")
    public ResponseEntity<ApiResponse<List<PermissionDto>>> getResourcePermissions(
            @PathVariable String resourceType,
            @PathVariable String resourceId,
            @RequestHeader("X-User-Id") String userId) {
        log.info("Get permissions for {} {} by user {}", resourceType, resourceId, userId);
        List<PermissionDto> permissions = shareService.getResourcePermissions(resourceType, resourceId, userId);
        return ResponseEntity.ok(ApiResponse.success("Permissions retrieved", permissions));
    }

    /**
     * Get files shared with the current user
     */
    @GetMapping("/shared-with-me/files")
    public ResponseEntity<ApiResponse<List<FileMetadataDto>>> getFilesSharedWithMe(
            @RequestHeader("X-User-Id") String userId) {
        log.info("Get files shared with user {}", userId);
        List<FileMetadataDto> files = shareService.getFilesSharedWithUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Shared files retrieved", files));
    }

    /**
     * Get folders shared with the current user
     */
    @GetMapping("/shared-with-me/folders")
    public ResponseEntity<ApiResponse<List<FolderDto>>> getFoldersSharedWithMe(
            @RequestHeader("X-User-Id") String userId) {
        log.info("Get folders shared with user {}", userId);
        List<FolderDto> folders = shareService.getFoldersSharedWithUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Shared folders retrieved", folders));
    }

    /**
     * Check if user can access a resource
     */
    @GetMapping("/{resourceType}/{resourceId}/access")
    public ResponseEntity<ApiResponse<Boolean>> checkAccess(
            @PathVariable String resourceType,
            @PathVariable String resourceId,
            @RequestHeader("X-User-Id") String userId) {
        boolean canAccess = shareService.canAccess(resourceType, resourceId, userId);
        return ResponseEntity.ok(ApiResponse.success("Access check complete", canAccess));
    }

    /**
     * Get current user's permission on a resource
     */
    @GetMapping("/{resourceType}/{resourceId}/my-permission")
    public ResponseEntity<ApiResponse<PermissionDto>> getMyPermission(
            @PathVariable String resourceType,
            @PathVariable String resourceId,
            @RequestHeader("X-User-Id") String userId) {
        PermissionDto permission = shareService.getUserPermission(resourceType, resourceId, userId);
        return ResponseEntity.ok(ApiResponse.success("Permission retrieved", permission));
    }

    /**
     * Update a permission role
     */
    @PatchMapping("/permission/{permissionId}")
    public ResponseEntity<ApiResponse<PermissionDto>> updatePermissionRole(
            @PathVariable String permissionId,
            @RequestParam String role,
            @RequestHeader("X-User-Id") String userId) {
        log.info("Update permission {} to role {} by user {}", permissionId, role, userId);
        PermissionDto permission = shareService.updatePermissionRole(permissionId, role, userId);
        return ResponseEntity.ok(ApiResponse.success("Permission updated successfully", permission));
    }

    // =============== Share Links ===============

    /**
     * Create a share link
     */
    @PostMapping("/link")
    public ResponseEntity<ApiResponse<ShareLinkDto>> createShareLink(
            @RequestBody ShareLinkRequestDto request,
            @RequestHeader("X-User-Id") String userId) {
        log.info("Create share link for {} {} by user {}", request.getResourceType(), request.getResourceId(), userId);
        ShareLinkDto shareLink = shareService.createShareLink(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Share link created", shareLink));
    }

    /**
     * Get share link by token (public endpoint)
     */
    @GetMapping("/link/{token}")
    public ResponseEntity<ApiResponse<ShareLinkDto>> getShareLink(
            @PathVariable String token,
            @RequestParam(required = false) String password) {
        log.info("Access share link by token");
        ShareLinkDto shareLink = shareService.getShareLinkByToken(token, password);
        shareService.recordShareLinkAccess(token);
        return ResponseEntity.ok(ApiResponse.success("Share link retrieved", shareLink));
    }

    /**
     * Get all share links for a resource
     */
    @GetMapping("/{resourceType}/{resourceId}/links")
    public ResponseEntity<ApiResponse<List<ShareLinkDto>>> getResourceShareLinks(
            @PathVariable String resourceType,
            @PathVariable String resourceId,
            @RequestHeader("X-User-Id") String userId) {
        log.info("Get share links for {} {} by user {}", resourceType, resourceId, userId);
        List<ShareLinkDto> links = shareService.getResourceShareLinks(resourceType, resourceId, userId);
        return ResponseEntity.ok(ApiResponse.success("Share links retrieved", links));
    }

    /**
     * Deactivate a share link
     */
    @DeleteMapping("/link/{linkId}")
    public ResponseEntity<ApiResponse<Void>> deactivateShareLink(
            @PathVariable String linkId,
            @RequestHeader("X-User-Id") String userId) {
        log.info("Deactivate share link {} by user {}", linkId, userId);
        shareService.deactivateShareLink(linkId, userId);
        return ResponseEntity.ok(ApiResponse.success("Share link deactivated", null));
    }
}
