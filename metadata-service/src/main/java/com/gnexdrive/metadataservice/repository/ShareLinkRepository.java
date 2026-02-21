package com.gnexdrive.metadataservice.repository;

import com.gnexdrive.metadataservice.entity.Permission;
import com.gnexdrive.metadataservice.entity.ShareLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShareLinkRepository extends JpaRepository<ShareLink, String> {

    /**
     * Find share link by token
     */
    Optional<ShareLink> findByToken(String token);

    /**
     * Find active share link by token
     */
    Optional<ShareLink> findByTokenAndIsActiveTrue(String token);

    /**
     * Find share links for a resource
     */
    List<ShareLink> findByResourceTypeAndResourceId(Permission.ResourceType resourceType, String resourceId);

    /**
     * Find active share links for a resource
     */
    List<ShareLink> findByResourceTypeAndResourceIdAndIsActiveTrue(
            Permission.ResourceType resourceType, 
            String resourceId);

    /**
     * Find share links created by a user
     */
    List<ShareLink> findByCreatedBy(String createdBy);

    /**
     * Find active share links created by a user
     */
    List<ShareLink> findByCreatedByAndIsActiveTrue(String createdBy);

    /**
     * Check if token exists
     */
    boolean existsByToken(String token);

    /**
     * Deactivate all share links for a resource
     */
    @Query("UPDATE ShareLink s SET s.isActive = false WHERE s.resourceType = :resourceType AND s.resourceId = :resourceId")
    void deactivateAllForResource(@Param("resourceType") Permission.ResourceType resourceType, 
                                   @Param("resourceId") String resourceId);

    /**
     * Delete expired links
     */
    @Query("DELETE FROM ShareLink s WHERE s.expiresAt IS NOT NULL AND s.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpiredLinks();

    /**
     * Count active links for a resource
     */
    long countByResourceTypeAndResourceIdAndIsActiveTrue(
            Permission.ResourceType resourceType, 
            String resourceId);

    /**
     * Find links with download limits reached
     */
    @Query("SELECT s FROM ShareLink s WHERE s.maxDownloads IS NOT NULL AND s.downloadCount >= s.maxDownloads")
    List<ShareLink> findLinksWithDownloadLimitReached();
}
