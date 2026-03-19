package com.copro.connect.exception;

/**
 * Thrown when the entered MFA code is invalid or expired.
 */
public class InvalidMfaCodeException extends RuntimeException {

    public InvalidMfaCodeException(String message) {
        super(message);
    }

    public InvalidMfaCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
