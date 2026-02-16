package com.copro.connect.dto;

import com.copro.connect.model.ChangeDetail;
import com.copro.connect.model.ResidentHistory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResidentHistoryResponse {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("resident_id")
    private String residentId;
    
    @JsonProperty("lot_id")
    private String lotId;
    
    private String batiment;
    
    private String etage;
    
    private String porte;
    
    @JsonProperty("action_type")
    private String actionType;
    
    private String description;
    
    private List<ChangeDetail> changes;
    
    @JsonProperty("changed_at")
    private Instant changedAt;
    
    @JsonProperty("changed_by")
    private String changedBy;
    
    public static ResidentHistoryResponse from(ResidentHistory history) {
        ResidentHistoryResponse response = new ResidentHistoryResponse();
        response.setId(history.getId());
        response.setResidentId(history.getResidentId());
        response.setLotId(history.getLotId());
        response.setBatiment(history.getBatiment());
        response.setEtage(history.getEtage());
        response.setPorte(history.getPorte());
        response.setActionType(history.getActionType());
        response.setDescription(history.getDescription());
        response.setChanges(history.getChanges());
        response.setChangedAt(history.getChangedAt());
        response.setChangedBy(history.getChangedBy());
        return response;
    }
}
