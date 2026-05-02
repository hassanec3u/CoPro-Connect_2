package com.copro.connect.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMfaRequest {

    @NotNull(message = "mfaEnabled is required")
    private Boolean mfaEnabled;
}
