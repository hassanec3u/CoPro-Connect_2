package com.copro.connect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
    private long totalLots;
    private long totalBatiments;
    private long totalOccupants;
    private long totalHappix;
    private Map<String, Long> statutCount;
    private Map<String, Long> batimentCount;
    private long lotsAvecOccupants;
    private long lotsVides;
    private double moyenneOccupants;
    private Map<String, Long> happixByType;
}
