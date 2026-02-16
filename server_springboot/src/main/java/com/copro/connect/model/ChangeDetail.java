package com.copro.connect.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeDetail {
    
    /**
     * Catégorie du changement : "LOT", "PROPRIETAIRE", "OCCUPANT", "HAPPIX"
     */
    private String category;
    
    /**
     * Type de changement : "MODIFIED", "ADDED", "REMOVED"
     */
    @JsonProperty("change_type")
    private String changeType;
    
    /**
     * Libellé lisible du champ (ex: "Nom du propriétaire", "Étage")
     */
    @JsonProperty("field_label")
    private String fieldLabel;
    
    /**
     * Ancienne valeur (null si ajout)
     */
    @JsonProperty("old_value")
    private String oldValue;
    
    /**
     * Nouvelle valeur (null si suppression)
     */
    @JsonProperty("new_value")
    private String newValue;
}
