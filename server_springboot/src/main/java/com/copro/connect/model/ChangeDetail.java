package com.copro.connect.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeDetail {

    /** Change categories */
    public static final String CATEGORY_LOT = "LOT";
    public static final String CATEGORY_PROPRIETAIRE = "PROPRIETAIRE";
    public static final String CATEGORY_OCCUPANT = "OCCUPANT";
    public static final String CATEGORY_HAPPIX = "HAPPIX";

    /** Change types */
    public static final String CHANGE_TYPE_MODIFIED = "MODIFIED";
    public static final String CHANGE_TYPE_ADDED = "ADDED";
    public static final String CHANGE_TYPE_REMOVED = "REMOVED";

    /** Reusable labels */
    public static final String LABEL_OCCUPANT = "Occupant";
    public static final String LABEL_COMPTE_HAPPIX = "Happix Account";

    /**
     * Change category: "LOT", "PROPRIETAIRE", "OCCUPANT", "HAPPIX"
     */
    private String category;

    /**
     * Change type: "MODIFIED", "ADDED", "REMOVED"
     */
    @JsonProperty("change_type")
    private String changeType;

    /**
     * Human-readable field label (e.g. "Owner name", "Floor")
     */
    @JsonProperty("field_label")
    private String fieldLabel;

    /**
     * Old value (null if added)
     */
    @JsonProperty("old_value")
    private String oldValue;

    /**
     * New value (null if removed)
     */
    @JsonProperty("new_value")
    private String newValue;
}
