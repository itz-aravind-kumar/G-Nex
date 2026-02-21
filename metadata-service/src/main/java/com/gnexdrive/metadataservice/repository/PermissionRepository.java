package com.gnexdrive.metadataservice.repository;

import com.gnexdrive.metadataservice.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {

    /**
     * Find permissions for a specific resource
     */
    List<Permission> findByResourceTypeAndResourceId(Permission.ResourceType resourceType, String resourceId);

    /**
     * Find permissions granted to a user (by user ID)
     */
    List<Permission> findByGranteeId(String granteeId);

    /**
     * Find permissions granted to a user (by email)
     */
    List<Permission> findByGranteeEmail(String granteeEmail);

    /**
     * Find a specific permission for a user on a resource
     */
    Optional<Permission> findByResourceTypeAndResourceIdAndGranteeId(
            Permission.ResourceType resourceType, 
            String resourceId, 
            String granteeId);

    /**
     * Find a specific permission for a user on a resource by email
     */
    Optional<Permission> findByResourceTypeAndResourceIdAndGranteeEmail(
            Permission.ResourceType resourceType, 
            String resourceId, 
            String granteeEmail);

    /**
     * Find all file permissions for a user
     */
    @Query("SELECT p FROM Permission p WHERE p.granteeId = :granteeId AND p.resourceType = 'FILE'")
    List<Permission> findFilePermissionsByGranteeId(@Param("granteeId") String granteeId);

    /**
     * Find all folder permissions for a user
     */
    @Query("SELECT p FROM Permission p WHERE p.granteeId = :granteeId AND p.resourceType = 'FOLDER'")
    List<Permission> findFolderPermissionsByGranteeId(@Param("granteeId") String granteeId);

    /**
     * Check if user has any permission on a resource
     */
    boolean existsByResourceTypeAndResourceIdAndGranteeId(
            Permission.ResourceType resourceType, 
            String resourceId, 
            String granteeId);

    /**
     * Check if user has specific role on a resource
     */
    boolean existsByResourceTypeAndResourceIdAndGranteeIdAndRole(
            Permission.ResourceType resourceType, 
            String resourceId, 
            String granteeId, 
            Permission.Role role);

    /**
     * Delete all permissions for a resource
     */
    void deleteByResourceTypeAndResourceId(Permission.ResourceType resourceType, String resourceId);

    /**
     * Delete a specific permission
     */
    void deleteByResourceTypeAndResourceIdAndGranteeId(
            Permission.ResourceType resourceType, 
            String resourceId, 
            String granteeId);

    /**
     * Find inherited permissions
     */
    List<Permission> findByParentPermissionId(String parentPermissionId);

    /**
     * Count permissions for a resource
     */
    long countByResourceTypeAndResourceId(Permission.ResourceType resourceType, String resourceId);

    /**
     * Find non-expired permissions for a user
     */
    @Query("SELECT p FROM Permission p WHERE p.granteeId = :granteeId AND (p.expiresAt IS NULL OR p.expiresAt > CURRENT_TIMESTAMP)")
    List<Permission> findActivePermissionsByGranteeId(@Param("granteeId") String granteeId);

    /**
     * Find permissions granted by a user
     */
    List<Permission> findByGrantedBy(String grantedBy);
}
