package com.copro.connect.exception;

/**
 * Levée lorsque l'envoi d'un email échoue (messagerie, encodage, etc.).
 */
public class EmailSendException extends RuntimeException {

    public EmailSendException(String message) {
        super(message);
    }

    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
