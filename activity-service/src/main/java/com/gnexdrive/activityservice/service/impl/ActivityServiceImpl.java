package com.gnexdrive.activityservice.service.impl;

import com.gnexdrive.common.dto.FileActivityDto;
import com.gnexdrive.activityservice.entity.FileActivity;
import com.gnexdrive.activityservice.repository.FileActivityRepository;
import com.gnexdrive.activityservice.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of Activity Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final FileActivityRepository activityRepository;

    @Override
    @Transactional
    public void logActivity(FileActivityDto activityDto) {
        log.info("Logging activity: {} for file: {} by user: {}", 
                activityDto.getActivityType(), activityDto.getFileId(), activityDto.getUserId());
        
        FileActivity activity = FileActivity.builder()
                .fileId(activityDto.getFileId())
                .fileName(activityDto.getFileName())
                .userId(activityDto.getUserId())
                .userEmail(activityDto.getUserEmail())
                .activityType(FileActivity.ActivityType.valueOf(activityDto.getActivityType().name()))
                .ipAddress(activityDto.getIpAddress())
                .userAgent(activityDto.getUserAgent())
                .metadata(activityDto.getMetadata())
                .build();
        
        activityRepository.save(activity);
        log.debug("Activity saved with ID: {}", activity.getActivityId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileActivityDto> getUserActivities(String userId, Pageable pageable) {
        log.debug("Fetching activities for user: {}", userId);
        return activityRepository.findByUserId(userId, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileActivityDto> getFileActivities(String fileId) {
        log.debug("Fetching activities for file: {}", fileId);
        return activityRepository.findByFileId(fileId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileActivityDto> getRecentActivities(String userId, int days) {
        log.debug("Fetching recent {} days activities for user: {}", days, userId);
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        return activityRepository.findRecentActivities(userId, startTime).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Object getActivityStats(String userId) {
        log.debug("Fetching activity stats for user: {}", userId);
        
        List<Object[]> stats = activityRepository.getActivityStatsByUser(userId);
        Map<String, Object> statsMap = new HashMap<>();
        
        for (Object[] stat : stats) {
            FileActivity.ActivityType type = (FileActivity.ActivityType) stat[0];
            Long count = (Long) stat[1];
            statsMap.put(type.name(), count);
        }
        
        // Add total count
        long totalActivities = stats.stream()
                .mapToLong(s -> (Long) s[1])
                .sum();
        statsMap.put("TOTAL", totalActivities);
        
        // Add recent activity count (last 7 days)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long recentCount = activityRepository.countByUserIdAndTimestampAfter(userId, weekAgo);
        statsMap.put("RECENT_7_DAYS", recentCount);
        
        return statsMap;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileActivityDto> getActivitiesByDateRange(
            String userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("Fetching activities for user: {} from {} to {}", userId, startDate, endDate);
        return activityRepository.findByUserIdAndTimestampBetween(userId, startDate, endDate, pageable)
                .map(this::convertToDto);
    }
    
    private FileActivityDto convertToDto(FileActivity activity) {
        return FileActivityDto.builder()
                .activityId(activity.getActivityId())
                .fileId(activity.getFileId())
                .fileName(activity.getFileName())
                .userId(activity.getUserId())
                .userEmail(activity.getUserEmail())
                .activityType(FileActivityDto.ActivityType.valueOf(activity.getActivityType().name()))
                .ipAddress(activity.getIpAddress())
                .userAgent(activity.getUserAgent())
                .timestamp(activity.getTimestamp())
                .metadata(activity.getMetadata())
                .build();
    }
}
