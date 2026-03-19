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
    
    @NotBlank(message = "Lot number is required")
    @Size(min = 1, max = 20, message = "Lot number must be between 1 and 20 characters")
    @JsonProperty("lot_id")
    private String lotId;

    @NotBlank(message = "Building is required")
    @Size(min = 1, max = 10, message = "Building must be between 1 and 10 characters")
    @Indexed
    private String batiment;

    @NotBlank(message = "Floor is required")
    @Size(min = 1, max = 5, message = "Floor must be between 1 and 5 characters")
    private String etage;

    @NotBlank(message = "Door number is required")
    @Size(min = 1, max = 10, message = "Door number must be between 1 and 10 characters")
    private String porte;

    @Size(max = 20, message = "Storage room ID cannot exceed 20 characters")
    @JsonProperty("cave_id")
    private String caveId;

    @Pattern(regexp = "^(Propriétaire Résident|Propriétaire Bailleur)?$",
             message = "Status must be: Propriétaire Résident or Propriétaire Bailleur",
             flags = Pattern.Flag.CANON_EQ)
    @JsonProperty("statut_lot")
    private String statutLot;

    @NotBlank(message = "Owner name is required")
    @Size(min = 2, max = 100, message = "Owner name must be between 2 and 100 characters")
    @JsonProperty("proprietaire_nom")
    private String proprietaireNom;

    @Pattern(regexp = "^(\\+?[0-9\\s.-]{10,20})?$",
             message = "Phone number is not valid")
    @JsonProperty("proprietaire_mobile")
    private String proprietaireMobile;

    @Email(message = "Email address is not valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
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
