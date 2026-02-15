package com.copro.connect.dto;

import com.copro.connect.model.Resident;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResidentsResponse {
    
    private List<Resident> data;
}
