package com.gnexdrive.metadataservice.repository;

import com.gnexdrive.metadataservice.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, String> {

    /**
     * Find folders by owner ID
     */
    List<Folder> findByOwnerIdAndStatus(String ownerId, Folder.FolderStatus status);

    /**
     * Find folders by owner ID (all statuses)
     */
    List<Folder> findByOwnerId(String ownerId);

    /**
     * Find folders in a parent folder
     */
    List<Folder> findByParentIdAndStatus(String parentId, Folder.FolderStatus status);

    /**
     * Find root folders for a user (no parent)
     */
    List<Folder> findByOwnerIdAndParentIdIsNullAndStatus(String ownerId, Folder.FolderStatus status);

    /**
     * Find folder by name in a specific parent folder
     */
    Optional<Folder> findByFolderNameAndParentIdAndOwnerId(String folderName, String parentId, String ownerId);

    /**
     * Find starred folders for a user
     */
    List<Folder> findByOwnerIdAndIsStarredTrueAndStatus(String ownerId, Folder.FolderStatus status);

    /**
     * Find trashed folders for a user
     */
    List<Folder> findByOwnerIdAndIsTrashedTrue(String ownerId);

    /**
     * Find all subfolders (recursive) using path prefix
     */
    @Query("SELECT f FROM Folder f WHERE f.path LIKE CONCAT(:parentPath, '%') AND f.ownerId = :ownerId")
    List<Folder> findAllSubfolders(@Param("parentPath") String parentPath, @Param("ownerId") String ownerId);

    /**
     * Check if folder exists by name in parent
     */
    boolean existsByFolderNameAndParentIdAndOwnerId(String folderName, String parentId, String ownerId);

    /**
     * Count folders by owner
     */
    long countByOwnerIdAndStatus(String ownerId, Folder.FolderStatus status);

    /**
     * Find by path
     */
    Optional<Folder> findByPathAndOwnerId(String path, String ownerId);
}
