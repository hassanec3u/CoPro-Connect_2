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
    
    @NotBlank(message = "Le nom du compte Happix est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom du compte Happix doit contenir entre 2 et 100 caractères")
    private String nom;
    
    @Pattern(regexp = "^(\\+?[0-9\\s.-]{10,20})?$", 
             message = "Le numéro de téléphone du compte Happix n'est pas valide")
    private String mobile;
    
    @Email(message = "L'adresse email du compte Happix n'est pas valide")
    @Size(max = 100, message = "L'email du compte Happix ne peut pas dépasser 100 caractères")
    private String email;
    
    @Size(max = 50, message = "Le nom de borne ne peut pas dépasser 50 caractères")
    @JsonProperty("nom_borne")
    private String nomBorne;
    
    @Pattern(regexp = "^(resident|autorisé)?$", 
             message = "Le type doit être: resident ou autorisé")
    private String type;
    
    @Size(max = 50, message = "La relation ne peut pas dépasser 50 caractères")
    private String relation;
}
