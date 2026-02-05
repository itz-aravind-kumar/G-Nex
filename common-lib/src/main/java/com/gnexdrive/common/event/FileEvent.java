package com.gnexdrive.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Base event class for Kafka messaging
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEvent {
    
    private String eventId;
    private String eventType;
    private String fileId;
    private String fileName;
    private String userId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private Map<String, Object> payload;
    
    private String correlationId;
    private String source;
    
    public enum EventType {
        FILE_UPLOADED,
        FILE_DOWNLOAD_REQUESTED,
        FILE_DELETED,
        METADATA_UPDATED,
        INDEX_REQUESTED,
        ACTIVITY_LOGGED
    }
}
