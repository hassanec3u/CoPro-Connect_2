package com.copro.connect.validator;

import com.copro.connect.exception.DuplicateResidentException;
import com.copro.connect.exception.ValidationException;
import com.copro.connect.model.Resident;
import com.copro.connect.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResidentValidator {
    
    private final ResidentRepository residentRepository;
    
    /**
     * Valide qu'un résident peut être créé (pas de doublon de lotId)
     */
    public void validateForCreation(Resident resident) {
        log.debug("Validating resident for creation: {}", resident.getLotId());
        
        // Vérifier que le lot ID n'existe pas déjà
        if (resident.getLotId() != null && !resident.getLotId().trim().isEmpty()) {
            Optional<Resident> existingResident = residentRepository.findAll().stream()
                .filter(r -> r.getLotId().equalsIgnoreCase(resident.getLotId().trim()))
                .findFirst();
            
            if (existingResident.isPresent()) {
                log.warn("Duplicate lotId detected: {}", resident.getLotId());
                throw new DuplicateResidentException(resident.getLotId());
            }
        }
    }
    
    /**
     * Valide qu'un résident peut être mis à jour
     */
    public void validateForUpdate(String id, Resident residentDetails) {
        log.debug("Validating resident for update: id={}, newLotId={}", id, residentDetails.getLotId());
        
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("L'identifiant du résident ne peut pas être vide");
        }
        
        // Vérifier que le résident existe
        Resident existingResident = residentRepository.findById(id)
            .orElseThrow(() -> new ValidationException("Le résident avec l'id " + id + " n'existe pas"));
        
        // Vérifier si le nouveau lot ID n'existe pas déjà (sauf pour le résident actuel)
        if (residentDetails.getLotId() != null && !residentDetails.getLotId().trim().isEmpty()) {
            if (!existingResident.getLotId().equalsIgnoreCase(residentDetails.getLotId().trim())) {
                Optional<Resident> duplicate = residentRepository.findAll().stream()
                    .filter(r -> !r.getId().equals(id) && 
                                r.getLotId().equalsIgnoreCase(residentDetails.getLotId().trim()))
                    .findFirst();
                
                if (duplicate.isPresent()) {
                    log.warn("Duplicate lotId detected during update: {}", residentDetails.getLotId());
                    throw new DuplicateResidentException(residentDetails.getLotId());
                }
            }
        }
    }
    
    /**
     * Valide qu'un ID est valide et non vide
     */
    public void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("L'identifiant ne peut pas être vide");
        }
    }
    
    /**
     * Valide qu'un paramètre de recherche est valide
     */
    public void validateSearchParameter(String paramName, String paramValue) {
        if (paramValue != null && paramValue.trim().isEmpty()) {
            throw new ValidationException("Le paramètre '" + paramName + "' ne peut pas être vide");
        }
    }
}
