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
    
    @NotBlank(message = "Le nom de l'occupant est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom de l'occupant doit contenir entre 2 et 100 caractères")
    private String nom;
    
    @Pattern(regexp = "^(\\+?[0-9\\s.-]{10,20})?$", 
             message = "Le numéro de téléphone de l'occupant n'est pas valide")
    private String mobile;
    
    @Email(message = "L'adresse email de l'occupant n'est pas valide")
    @Size(max = 100, message = "L'email de l'occupant ne peut pas dépasser 100 caractères")
    private String email;
}
