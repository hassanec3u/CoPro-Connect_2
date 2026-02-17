package com.copro.connect.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "mfa_codes")
public class MfaCode {

    @Id
    private String id;

    @Indexed
    private String username;

    private String code;

    private int attempts;

    @Indexed(expireAfter = "10m")
    private Instant createdAt;

    private Instant expiresAt;

    private boolean used;
}
