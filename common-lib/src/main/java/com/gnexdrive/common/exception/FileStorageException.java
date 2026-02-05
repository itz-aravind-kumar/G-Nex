package com.gnexdrive.common.exception;

/**
 * Custom exception for file storage operations
 */
public class FileStorageException extends RuntimeException {
    
    private final String fileName;
    
    public FileStorageException(String message) {
        super(message);
        this.fileName = null;
    }
    
    public FileStorageException(String message, String fileName) {
        super(message);
        this.fileName = fileName;
    }
    
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
        this.fileName = null;
    }
    
    public FileStorageException(String message, String fileName, Throwable cause) {
        super(message, cause);
        this.fileName = fileName;
    }
    
    public String getFileName() {
        return fileName;
    }
}
