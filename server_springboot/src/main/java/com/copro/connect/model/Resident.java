package com.copro.connect.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "residents")
public class Resident {
    
    @Id
    @JsonProperty("id")
    private String id;
    
    @NotBlank(message = "Le numéro de lot est obligatoire")
    @Size(min = 1, max = 20, message = "Le numéro de lot doit contenir entre 1 et 20 caractères")
    @JsonProperty("lot_id")
    private String lotId;
    
    @NotBlank(message = "Le bâtiment est obligatoire")
    @Size(min = 1, max = 10, message = "Le bâtiment doit contenir entre 1 et 10 caractères")
    @Indexed
    private String batiment;
    
    @NotBlank(message = "L'étage est obligatoire")
    @Size(min = 1, max = 5, message = "L'étage doit contenir entre 1 et 5 caractères")
    private String etage;
    
    @NotBlank(message = "Le numéro de porte est obligatoire")
    @Size(min = 1, max = 10, message = "Le numéro de porte doit contenir entre 1 et 10 caractères")
    private String porte;
    
    @Size(max = 20, message = "L'identifiant de cave ne peut pas dépasser 20 caractères")
    @JsonProperty("cave_id")
    private String caveId;
    
    @Pattern(regexp = "^(Propriétaire Résident|Propriétaire Bailleur)?$", 
             message = "Le statut doit être: Propriétaire Résident ou Propriétaire Bailleur")
    @JsonProperty("statut_lot")
    private String statutLot;
    
    @NotBlank(message = "Le nom du propriétaire est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom du propriétaire doit contenir entre 2 et 100 caractères")
    @JsonProperty("proprietaire_nom")
    private String proprietaireNom;
    
    @Pattern(regexp = "^(\\+?[0-9\\s.-]{10,20})?$", 
             message = "Le numéro de téléphone n'est pas valide")
    @JsonProperty("proprietaire_mobile")
    private String proprietaireMobile;
    
    @Email(message = "L'adresse email n'est pas valide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    @JsonProperty("proprietaire_email")
    private String proprietaireEmail;
    
    @Valid
    private List<Occupant> occupants = new ArrayList<>();
    
    @Valid
    @JsonProperty("happix_accounts")
    private List<HappixAccount> happixAccounts = new ArrayList<>();
    
    @CreatedDate
    @JsonProperty("createdAt")
    private Instant createdAt;
    
    @LastModifiedDate
    @JsonProperty("updatedAt")
    private Instant updatedAt;
}
