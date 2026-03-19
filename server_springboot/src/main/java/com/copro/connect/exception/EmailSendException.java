package com.copro.connect.exception;

/**
 * Thrown when sending an email fails (messaging, encoding, etc.).
 */
public class EmailSendException extends RuntimeException {

    public EmailSendException(String message) {
        super(message);
    }

    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
