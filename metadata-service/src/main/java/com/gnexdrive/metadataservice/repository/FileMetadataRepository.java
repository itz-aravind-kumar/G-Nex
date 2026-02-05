package com.gnexdrive.metadataservice.repository;

import com.gnexdrive.metadataservice.entity.FileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for File Metadata
 */
@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, String> {

    Optional<FileMetadata> findByFileIdAndOwnerId(String fileId, String ownerId);

    Page<FileMetadata> findByOwnerId(String ownerId, Pageable pageable);

    List<FileMetadata> findByOwnerIdAndStatus(String ownerId, FileMetadata.FileStatus status);

    List<FileMetadata> findByFileNameContainingIgnoreCase(String fileName);

    List<FileMetadata> findByFileType(String fileType);

    @Query("SELECT f FROM FileMetadata f WHERE f.ownerId = :ownerId AND " +
           "(LOWER(f.fileName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(f.fileType) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<FileMetadata> searchFilesByOwner(@Param("ownerId") String ownerId, 
                                          @Param("searchTerm") String searchTerm);

    long countByOwnerId(String ownerId);

    @Query("SELECT SUM(f.fileSize) FROM FileMetadata f WHERE f.ownerId = :ownerId")
    Long getTotalStorageByOwner(@Param("ownerId") String ownerId);
}
