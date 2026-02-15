package com.copro.connect.controller;

import com.copro.connect.dto.PagedResidentsResponse;
import com.copro.connect.dto.ResidentsResponse;
import com.copro.connect.dto.StatisticsResponse;
import com.copro.connect.exception.ValidationException;
import com.copro.connect.model.Resident;
import com.copro.connect.service.ResidentService;
import com.copro.connect.validator.ResidentValidator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/residents")
@RequiredArgsConstructor
@Validated
public class ResidentController {
    
    private final ResidentService residentService;
    private final ResidentValidator residentValidator;
    
    @GetMapping
    public ResponseEntity<PagedResidentsResponse> getAllResidents(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String batiment,
            @RequestParam(required = false) String statutLot,
            @RequestParam(required = false) String sort) {
        log.info("GET /api/residents - page: {}, size: {}, search: {}, batiment: {}, statut: {}, sort: {}", 
                 page, size, search, batiment, statutLot, sort);
        
        // Validation supplémentaire
        if (size > 100) {
            throw new ValidationException("La taille de la page ne peut pas dépasser 100");
        }
        
        PagedResidentsResponse response = residentService.getResidentsPaginated(page, size, search, batiment, statutLot, sort);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/all")
    public ResponseEntity<ResidentsResponse> getAllResidentsNoPagination() {
        log.info("GET /api/residents/all - Fetching all residents without pagination");
        List<Resident> residents = residentService.getAllResidents();
        return ResponseEntity.ok(new ResidentsResponse(residents));
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> getStatistics() {
        log.info("GET /api/residents/statistics - Calculating statistics");
        StatisticsResponse statistics = residentService.getStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Resident> getResidentById(@PathVariable String id) {
        log.info("GET /api/residents/{} - Fetching resident by id", id);
        
        // Validation au niveau API
        residentValidator.validateId(id);
        
        Resident resident = residentService.getResidentById(id);
        return ResponseEntity.ok(resident);
    }
    
    @PostMapping
    public ResponseEntity<Resident> createResident(@Valid @RequestBody Resident resident) {
        log.info("POST /api/residents - Creating new resident");
        
        // Validation métier au niveau API
        residentValidator.validateForCreation(resident);
        
        Resident createdResident = residentService.createResident(resident);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdResident);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Resident> updateResident(
            @PathVariable String id,
            @Valid @RequestBody Resident residentDetails) {
        log.info("PUT /api/residents/{} - Updating resident", id);
        
        // Validation métier au niveau API
        residentValidator.validateForUpdate(id, residentDetails);
        
        Resident updatedResident = residentService.updateResident(id, residentDetails);
        return ResponseEntity.ok(updatedResident);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteResident(@PathVariable String id) {
        log.info("DELETE /api/residents/{} - Deleting resident", id);
        
        // Validation au niveau API
        residentValidator.validateId(id);
        
        residentService.deleteResident(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Résident supprimé avec succès");
        
        return ResponseEntity.ok(response);
    }
}
