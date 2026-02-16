package com.copro.connect.controller;

import com.copro.connect.dto.ResidentHistoryResponse;
import com.copro.connect.model.ResidentHistory;
import com.copro.connect.service.ResidentHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/residents/history")
@RequiredArgsConstructor
@Validated
public class ResidentHistoryController {
    
    private final ResidentHistoryService residentHistoryService;
    
    /**
     * Récupère l'historique d'un appartement
     */
    @GetMapping("/apartment")
    public ResponseEntity<List<ResidentHistoryResponse>> getApartmentHistory(
            @RequestParam String batiment,
            @RequestParam String etage,
            @RequestParam String porte) {
        log.info("GET /api/residents/history/apartment - batiment: {}, etage: {}, porte: {}", 
                 batiment, etage, porte);
        
        List<ResidentHistory> history = residentHistoryService.getApartmentHistory(batiment, etage, porte);
        List<ResidentHistoryResponse> response = history.stream()
                .map(ResidentHistoryResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Récupère l'historique d'un résident spécifique
     */
    @GetMapping("/resident/{residentId}")
    public ResponseEntity<List<ResidentHistoryResponse>> getResidentHistory(
            @PathVariable String residentId) {
        log.info("GET /api/residents/history/resident/{}", residentId);
        
        List<ResidentHistory> history = residentHistoryService.getResidentHistory(residentId);
        List<ResidentHistoryResponse> response = history.stream()
                .map(ResidentHistoryResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
}
