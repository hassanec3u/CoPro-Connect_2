package com.copro.connect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private String message;
    private int status;
    private Instant timestamp;
    private String path;
    
    public ErrorResponse(String message, int status, String path) {
        this.message = message;
        this.status = status;
        this.timestamp = Instant.now();
        this.path = path;
    }
}
