package com.gnexdrive.userservice.exception;

/**
 * Exception thrown when username or email already exists
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
