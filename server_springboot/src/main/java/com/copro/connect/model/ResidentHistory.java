package com.copro.connect.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "residents_history")
public class ResidentHistory {
    
    @Id
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("resident_id")
    @Indexed
    private String residentId;
    
    @JsonProperty("lot_id")
    private String lotId;
    
    @Indexed
    private String batiment;
    
    private String etage;
    
    private String porte;
    
    @JsonProperty("action_type")
    @Indexed
    private String actionType; // "UPDATE", "DELETE"
    
    /**
     * Description lisible de l'action (ex: "Modification du propriétaire, Suppression d'un compte Happix")
     */
    private String description;
    
    /**
     * Liste des changements détaillés
     */
    private List<ChangeDetail> changes = new ArrayList<>();
    
    @JsonProperty("changed_at")
    @Indexed
    private Instant changedAt;
    
    @JsonProperty("changed_by")
    private String changedBy;
    
    @JsonProperty("apartment_key")
    @Indexed
    private String apartmentKey;
}
