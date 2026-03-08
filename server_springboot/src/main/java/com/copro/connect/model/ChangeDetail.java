package com.copro.connect.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeDetail {

    /** Catégories de changement */
    public static final String CATEGORY_LOT = "LOT";
    public static final String CATEGORY_PROPRIETAIRE = "PROPRIETAIRE";
    public static final String CATEGORY_OCCUPANT = "OCCUPANT";
    public static final String CATEGORY_HAPPIX = "HAPPIX";

    /** Types de changement */
    public static final String CHANGE_TYPE_MODIFIED = "MODIFIED";
    public static final String CHANGE_TYPE_ADDED = "ADDED";
    public static final String CHANGE_TYPE_REMOVED = "REMOVED";

    /** Libellés réutilisés */
    public static final String LABEL_OCCUPANT = "Occupant";
    public static final String LABEL_COMPTE_HAPPIX = "Compte Happix";

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
