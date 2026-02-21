package com.gnexdrive.metadataservice.service.impl;

import com.gnexdrive.common.dto.*;
import com.gnexdrive.common.exception.ResourceNotFoundException;
import com.gnexdrive.common.exception.UnauthorizedException;
import com.gnexdrive.metadataservice.entity.FileMetadata;
import com.gnexdrive.metadataservice.entity.Folder;
import com.gnexdrive.metadataservice.entity.Permission;
import com.gnexdrive.metadataservice.entity.ShareLink;
import com.gnexdrive.metadataservice.mapper.FileMetadataMapper;
import com.gnexdrive.metadataservice.repository.*;
import com.gnexdrive.metadataservice.service.ShareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    private final PermissionRepository permissionRepository;
    private final ShareLinkRepository shareLinkRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final FolderRepository folderRepository;
    private final FileMetadataMapper fileMetadataMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public PermissionDto shareResource(ShareRequestDto request, String grantorId) {
        log.info("Sharing {} {} with {}", request.getResourceType(), request.getResourceId(), request.getGranteeEmail());

        Permission.ResourceType resourceType = Permission.ResourceType.valueOf(request.getResourceType().toUpperCase());
        
        // Verify grantor has permission to share
        if (!canShare(resourceType, request.getResourceId(), grantorId)) {
            throw new UnauthorizedException("You don't have permission to share this resource");
        }

        // Check if permission already exists
        var existingPermission = permissionRepository.findByResourceTypeAndResourceIdAndGranteeEmail(
                resourceType, request.getResourceId(), request.getGranteeEmail());
        
        if (existingPermission.isPresent()) {
            // Update existing permission
            Permission permission = existingPermission.get();
            permission.setRole(Permission.Role.valueOf(request.getRole().toUpperCase()));
            permission.setExpiresAt(request.getExpiresAt());
            return toDto(permissionRepository.save(permission));
        }

        // Create new permission
        Permission permission = Permission.builder()
                .resourceType(resourceType)
                .resourceId(request.getResourceId())
                .granteeEmail(request.getGranteeEmail())
                .role(Permission.Role.valueOf(request.getRole().toUpperCase()))
                .grantedBy(grantorId)
                .expiresAt(request.getExpiresAt())
                .isInherited(false)
                .build();

        Permission saved = permissionRepository.save(permission);
        log.info("Created permission {} for {} on {}", saved.getPermissionId(), request.getGranteeEmail(), request.getResourceId());
        
        return toDto(saved);
    }

    @Override
    @Transactional
    public void revokePermission(String permissionId, String requesterId) {
        log.info("Revoking permission {} by user {}", permissionId, requesterId);
        
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));

        // Check if requester can revoke (owner or grantor)
        if (!canShare(permission.getResourceType(), permission.getResourceId(), requesterId) 
                && !permission.getGrantedBy().equals(requesterId)) {
            throw new UnauthorizedException("You don't have permission to revoke this share");
        }

        permissionRepository.delete(permission);
        log.info("Permission {} revoked", permissionId);
    }

    @Override
    public List<PermissionDto> getResourcePermissions(String resourceType, String resourceId, String requesterId) {
        Permission.ResourceType type = Permission.ResourceType.valueOf(resourceType.toUpperCase());
        
        // Check if requester can view permissions
        if (!canAccess(resourceType, resourceId, requesterId)) {
            throw new UnauthorizedException("You don't have access to this resource");
        }

        return permissionRepository.findByResourceTypeAndResourceId(type, resourceId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<FileMetadataDto> getFilesSharedWithUser(String userId) {
        log.info("Getting files shared with user {}", userId);
        
        List<Permission> permissions = permissionRepository.findActivePermissionsByGranteeId(userId);
        List<FileMetadataDto> sharedFiles = new ArrayList<>();

        for (Permission permission : permissions) {
            if (permission.getResourceType() == Permission.ResourceType.FILE) {
                fileMetadataRepository.findById(permission.getResourceId())
                        .ifPresent(file -> {
                            FileMetadataDto dto = fileMetadataMapper.toDto(file);
                            dto.setSharedWith(permission.getGranteeEmail());
                            sharedFiles.add(dto);
                        });
            }
        }

        return sharedFiles;
    }

    @Override
    public List<FolderDto> getFoldersSharedWithUser(String userId) {
        log.info("Getting folders shared with user {}", userId);
        
        List<Permission> permissions = permissionRepository.findActivePermissionsByGranteeId(userId);
        List<FolderDto> sharedFolders = new ArrayList<>();

        for (Permission permission : permissions) {
            if (permission.getResourceType() == Permission.ResourceType.FOLDER) {
                folderRepository.findById(permission.getResourceId())
                        .ifPresent(folder -> sharedFolders.add(toFolderDto(folder, permission)));
            }
        }

        return sharedFolders;
    }

    @Override
    public boolean hasPermission(String resourceType, String resourceId, String userId, String requiredRole) {
        Permission.ResourceType type = Permission.ResourceType.valueOf(resourceType.toUpperCase());
        Permission.Role role = Permission.Role.valueOf(requiredRole.toUpperCase());

        var permission = permissionRepository.findByResourceTypeAndResourceIdAndGranteeId(type, resourceId, userId);
        if (permission.isEmpty()) {
            return false;
        }

        Permission p = permission.get();
        if (p.isExpired()) {
            return false;
        }

        // Check role hierarchy: OWNER > EDITOR > VIEWER
        return switch (role) {
            case VIEWER -> p.canView();
            case EDITOR -> p.canEdit();
            case OWNER -> p.getRole() == Permission.Role.OWNER;
        };
    }

    @Override
    public boolean canAccess(String resourceType, String resourceId, String userId) {
        Permission.ResourceType type = Permission.ResourceType.valueOf(resourceType.toUpperCase());
        
        // Check if user is owner
        if (type == Permission.ResourceType.FILE) {
            var file = fileMetadataRepository.findById(resourceId);
            if (file.isPresent() && file.get().getOwnerId().equals(userId)) {
                return true;
            }
        } else if (type == Permission.ResourceType.FOLDER) {
            var folder = folderRepository.findById(resourceId);
            if (folder.isPresent() && folder.get().getOwnerId().equals(userId)) {
                return true;
            }
        }

        // Check permissions
        return hasPermission(resourceType, resourceId, userId, "VIEWER");
    }

    private boolean canShare(Permission.ResourceType resourceType, String resourceId, String userId) {
        // Check if user is owner
        if (resourceType == Permission.ResourceType.FILE) {
            var file = fileMetadataRepository.findById(resourceId);
            if (file.isPresent() && file.get().getOwnerId().equals(userId)) {
                return true;
            }
        } else if (resourceType == Permission.ResourceType.FOLDER) {
            var folder = folderRepository.findById(resourceId);
            if (folder.isPresent() && folder.get().getOwnerId().equals(userId)) {
                return true;
            }
        }

        // Check if user has EDITOR or OWNER permission
        var permission = permissionRepository.findByResourceTypeAndResourceIdAndGranteeId(
                resourceType, resourceId, userId);
        return permission.isPresent() && permission.get().canShare();
    }

    @Override
    @Transactional
    public ShareLinkDto createShareLink(ShareLinkRequestDto request, String creatorId) {
        log.info("Creating share link for {} {}", request.getResourceType(), request.getResourceId());

        Permission.ResourceType resourceType = Permission.ResourceType.valueOf(request.getResourceType().toUpperCase());
        
        // Verify creator has permission to share
        if (!canShare(resourceType, request.getResourceId(), creatorId)) {
            throw new UnauthorizedException("You don't have permission to create a share link for this resource");
        }

        ShareLink shareLink = ShareLink.builder()
                .resourceType(resourceType)
                .resourceId(request.getResourceId())
                .role(Permission.Role.valueOf(request.getRole() != null ? request.getRole().toUpperCase() : "VIEWER"))
                .createdBy(creatorId)
                .expiresAt(request.getExpiresAt())
                .maxDownloads(request.getMaxDownloads())
                .isPasswordProtected(request.getPassword() != null && !request.getPassword().isEmpty())
                .passwordHash(request.getPassword() != null && !request.getPassword().isEmpty() 
                        ? passwordEncoder.encode(request.getPassword()) : null)
                .build();

        ShareLink saved = shareLinkRepository.save(shareLink);
        log.info("Created share link {} with token {}", saved.getLinkId(), saved.getToken());
        
        return toShareLinkDto(saved);
    }

    @Override
    public ShareLinkDto getShareLinkByToken(String token, String password) {
        log.info("Getting share link by token");
        
        ShareLink shareLink = shareLinkRepository.findByTokenAndIsActiveTrue(token)
                .orElseThrow(() -> new ResourceNotFoundException("Share link not found or expired"));

        if (!shareLink.isValid()) {
            throw new ResourceNotFoundException("Share link has expired or reached download limit");
        }

        if (shareLink.getIsPasswordProtected()) {
            if (password == null || !passwordEncoder.matches(password, shareLink.getPasswordHash())) {
                throw new UnauthorizedException("Invalid password");
            }
        }

        return toShareLinkDto(shareLink);
    }

    @Override
    public List<ShareLinkDto> getResourceShareLinks(String resourceType, String resourceId, String requesterId) {
        Permission.ResourceType type = Permission.ResourceType.valueOf(resourceType.toUpperCase());
        
        // Check if requester can view links
        if (!canShare(type, resourceId, requesterId)) {
            throw new UnauthorizedException("You don't have access to view share links for this resource");
        }

        return shareLinkRepository.findByResourceTypeAndResourceIdAndIsActiveTrue(type, resourceId)
                .stream()
                .map(this::toShareLinkDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deactivateShareLink(String linkId, String requesterId) {
        log.info("Deactivating share link {} by user {}", linkId, requesterId);
        
        ShareLink shareLink = shareLinkRepository.findById(linkId)
                .orElseThrow(() -> new ResourceNotFoundException("Share link not found"));

        // Check if requester can deactivate (creator or resource owner)
        if (!shareLink.getCreatedBy().equals(requesterId) 
                && !canShare(shareLink.getResourceType(), shareLink.getResourceId(), requesterId)) {
            throw new UnauthorizedException("You don't have permission to deactivate this share link");
        }

        shareLink.setIsActive(false);
        shareLinkRepository.save(shareLink);
        log.info("Share link {} deactivated", linkId);
    }

    @Override
    @Transactional
    public void recordShareLinkAccess(String token) {
        shareLinkRepository.findByToken(token).ifPresent(link -> {
            link.recordAccess();
            shareLinkRepository.save(link);
        });
    }

    @Override
    @Transactional
    public PermissionDto updatePermissionRole(String permissionId, String newRole, String requesterId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));

        // Check if requester can update
        if (!canShare(permission.getResourceType(), permission.getResourceId(), requesterId)) {
            throw new UnauthorizedException("You don't have permission to update this share");
        }

        permission.setRole(Permission.Role.valueOf(newRole.toUpperCase()));
        return toDto(permissionRepository.save(permission));
    }

    @Override
    public PermissionDto getUserPermission(String resourceType, String resourceId, String userId) {
        Permission.ResourceType type = Permission.ResourceType.valueOf(resourceType.toUpperCase());
        
        return permissionRepository.findByResourceTypeAndResourceIdAndGranteeId(type, resourceId, userId)
                .map(this::toDto)
                .orElse(null);
    }

    private PermissionDto toDto(Permission permission) {
        String resourceName = "";
        if (permission.getResourceType() == Permission.ResourceType.FILE) {
            resourceName = fileMetadataRepository.findById(permission.getResourceId())
                    .map(FileMetadata::getFileName)
                    .orElse("Unknown");
        } else {
            resourceName = folderRepository.findById(permission.getResourceId())
                    .map(Folder::getFolderName)
                    .orElse("Unknown");
        }

        return PermissionDto.builder()
                .permissionId(permission.getPermissionId())
                .resourceType(permission.getResourceType().name())
                .resourceId(permission.getResourceId())
                .resourceName(resourceName)
                .granteeId(permission.getGranteeId())
                .granteeEmail(permission.getGranteeEmail())
                .role(permission.getRole().name())
                .grantedBy(permission.getGrantedBy())
                .grantedAt(permission.getGrantedAt())
                .expiresAt(permission.getExpiresAt())
                .isInherited(permission.getIsInherited())
                .build();
    }

    private FolderDto toFolderDto(Folder folder, Permission permission) {
        return FolderDto.builder()
                .folderId(folder.getFolderId())
                .folderName(folder.getFolderName())
                .parentId(folder.getParentId())
                .ownerId(folder.getOwnerId())
                .ownerEmail(folder.getOwnerEmail())
                .path(folder.getPath())
                .color(folder.getColor())
                .isStarred(folder.getIsStarred())
                .isTrashed(folder.getIsTrashed())
                .status(folder.getStatus().name())
                .createdAt(folder.getCreatedAt())
                .modifiedAt(folder.getModifiedAt())
                .permission(permission.getRole().name())
                .build();
    }

    private ShareLinkDto toShareLinkDto(ShareLink shareLink) {
        String resourceName = "";
        if (shareLink.getResourceType() == Permission.ResourceType.FILE) {
            resourceName = fileMetadataRepository.findById(shareLink.getResourceId())
                    .map(FileMetadata::getFileName)
                    .orElse("Unknown");
        } else {
            resourceName = folderRepository.findById(shareLink.getResourceId())
                    .map(Folder::getFolderName)
                    .orElse("Unknown");
        }

        return ShareLinkDto.builder()
                .linkId(shareLink.getLinkId())
                .token(shareLink.getToken())
                .url("/share/" + shareLink.getToken())
                .resourceType(shareLink.getResourceType().name())
                .resourceId(shareLink.getResourceId())
                .resourceName(resourceName)
                .role(shareLink.getRole().name())
                .createdBy(shareLink.getCreatedBy())
                .createdAt(shareLink.getCreatedAt())
                .expiresAt(shareLink.getExpiresAt())
                .isPasswordProtected(shareLink.getIsPasswordProtected())
                .maxDownloads(shareLink.getMaxDownloads())
                .downloadCount(shareLink.getDownloadCount())
                .isActive(shareLink.getIsActive())
                .accessCount(shareLink.getAccessCount())
                .lastAccessedAt(shareLink.getLastAccessedAt())
                .build();
    }
}
