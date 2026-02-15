package com.copro.connect.exception;

public class ResidentNotFoundException extends RuntimeException {
    
    public ResidentNotFoundException(String id) {
        super("Résident introuvable avec l'id: " + id);
    }
    
    public ResidentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
