package com.copro.connect.exception;

/**
 * Levée lorsque le code MFA saisi est invalide ou expiré.
 */
public class InvalidMfaCodeException extends RuntimeException {

    public InvalidMfaCodeException(String message) {
        super(message);
    }

    public InvalidMfaCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
