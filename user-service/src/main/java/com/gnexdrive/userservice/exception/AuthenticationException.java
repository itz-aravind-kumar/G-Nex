package com.gnexdrive.userservice.exception;

/**
 * Exception thrown for authentication failures
 */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
