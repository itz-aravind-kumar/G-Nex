package com.gnexdrive.userservice.exception;

/**
 * Exception thrown when resource is not found
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
