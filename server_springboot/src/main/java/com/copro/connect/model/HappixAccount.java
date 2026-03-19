package com.copro.connect.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class HappixAccount {
    
    @NotBlank(message = "Happix account name is required")
    @Size(min = 2, max = 100, message = "Happix account name must be between 2 and 100 characters")
    private String nom;

    @Pattern(regexp = "^(\\+?[0-9\\s.-]{10,20})?$",
             message = "Happix account phone number is not valid")
    private String mobile;

    @Email(message = "Happix account email address is not valid")
    @Size(max = 100, message = "Happix account email cannot exceed 100 characters")
    private String email;

    @Size(max = 50, message = "Terminal name cannot exceed 50 characters")
    @JsonProperty("nom_borne")
    private String nomBorne;

    @Pattern(regexp = "^(resident|autorisé)?$",
             message = "Type must be: resident or autorisé",
             flags = Pattern.Flag.CANON_EQ)
    private String type;

    @Size(max = 50, message = "Relationship cannot exceed 50 characters")
    private String relation;
}
