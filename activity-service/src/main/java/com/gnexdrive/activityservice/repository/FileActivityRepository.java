package com.gnexdrive.activityservice.repository;

import com.gnexdrive.activityservice.entity.FileActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for File Activities
 */
@Repository
public interface FileActivityRepository extends JpaRepository<FileActivity, String> {

    Page<FileActivity> findByUserId(String userId, Pageable pageable);

    List<FileActivity> findByFileId(String fileId);

    List<FileActivity> findByUserIdAndActivityType(String userId, FileActivity.ActivityType activityType);

    Page<FileActivity> findByUserIdAndTimestampBetween(
        String userId, 
        LocalDateTime startTime, 
        LocalDateTime endTime, 
        Pageable pageable
    );

    @Query("SELECT a FROM FileActivity a WHERE a.userId = :userId " +
           "AND a.timestamp >= :startTime ORDER BY a.timestamp DESC")
    List<FileActivity> findRecentActivities(
        @Param("userId") String userId, 
        @Param("startTime") LocalDateTime startTime
    );

    @Query("SELECT a.activityType, COUNT(a) FROM FileActivity a " +
           "WHERE a.userId = :userId GROUP BY a.activityType")
    List<Object[]> getActivityStatsByUser(@Param("userId") String userId);

    long countByUserIdAndTimestampAfter(String userId, LocalDateTime timestamp);
}
