package com.copro.connect.exception;

public class DuplicateResidentException extends RuntimeException {
    
    public DuplicateResidentException(String lotId) {
        super("A resident already exists with lot ID: " + lotId);
    }
    
    public DuplicateResidentException(String message, Throwable cause) {
        super(message, cause);
    }
}
