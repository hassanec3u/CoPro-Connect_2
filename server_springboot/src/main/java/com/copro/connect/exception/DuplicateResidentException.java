package com.copro.connect.exception;

public class DuplicateResidentException extends RuntimeException {
    
    public DuplicateResidentException(String lotId) {
        super("Un résident existe déjà avec le lot ID: " + lotId);
    }
    
    public DuplicateResidentException(String message, Throwable cause) {
        super(message, cause);
    }
}
