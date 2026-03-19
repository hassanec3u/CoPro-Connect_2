package com.copro.connect.exception;

public class ResidentNotFoundException extends RuntimeException {
    
    public ResidentNotFoundException(String id) {
        super("Resident not found with id: " + id);
    }
    
    public ResidentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
