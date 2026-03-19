package com.copro.connect.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Occupant {
    
    @NotBlank(message = "Occupant name is required")
    @Size(min = 2, max = 100, message = "Occupant name must be between 2 and 100 characters")
    private String nom;

    @Pattern(regexp = "^(\\+?[0-9\\s.-]{10,20})?$",
             message = "Occupant phone number is not valid")
    private String mobile;

    @Email(message = "Occupant email address is not valid")
    @Size(max = 100, message = "Occupant email cannot exceed 100 characters")
    private String email;
}
