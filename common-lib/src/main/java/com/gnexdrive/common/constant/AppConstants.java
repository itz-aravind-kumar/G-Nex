package com.gnexdrive.common.constant;

/**
 * Application-wide constants
 */
public final class AppConstants {
    
    private AppConstants() {
        throw new IllegalStateException("Utility class");
    }
    
    // Kafka Topics
    public static final String TOPIC_FILE_UPLOADED = "file.uploaded";
    public static final String TOPIC_FILE_DELETED = "file.deleted";
    public static final String TOPIC_FILE_DOWNLOADED = "file.downloaded";
    public static final String TOPIC_METADATA_UPDATED = "metadata.updated";
    public static final String TOPIC_ACTIVITY_LOG = "activity.log";
    
    // Redis Keys
    public static final String CACHE_FILE_METADATA = "file:metadata:";
    public static final String CACHE_USER_FILES = "user:files:";
    public static final String CACHE_SEARCH_RESULTS = "search:results:";
    
    // Elasticsearch Indices
    public static final String INDEX_FILES = "files";
    
    // File Constraints
    public static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    public static final String[] ALLOWED_FILE_TYPES = {
        "image/jpeg", "image/png", "image/gif",
        "application/pdf", "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "text/plain", "video/mp4"
    };
    
    // API Headers
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";
    public static final String HEADER_API_KEY = "X-API-Key";
    
    // Date Formats
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
}
