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
     * Validates that a resident can be created (no duplicate lotId)
     */
    public void validateForCreation(Resident resident) {
        log.debug("Validating resident for creation: {}", resident.getLotId());

        // Check that the lot ID does not already exist
        if (resident.getLotId() != null && !resident.getLotId().trim().isEmpty()) {
            Optional<Resident> existingResident = residentRepository.findByLotIdIgnoreCase(resident.getLotId().trim());
            
            if (existingResident.isPresent()) {
                log.warn("Duplicate lotId detected: {}", resident.getLotId());
                throw new DuplicateResidentException(resident.getLotId());
            }
        }
    }
    
    /**
     * Validates that a resident can be updated
     */
    public void validateForUpdate(String id, Resident residentDetails) {
        log.debug("Validating resident for update: id={}, newLotId={}", id, residentDetails.getLotId());

        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Resident ID cannot be empty");
        }

        // Check that the resident exists
        Resident existingResident = residentRepository.findById(id)
            .orElseThrow(() -> new ValidationException("Resident with id " + id + " does not exist"));

        // Check if the new lot ID does not already exist (except for the current resident)
        if (residentDetails.getLotId() != null && !residentDetails.getLotId().trim().isEmpty()) {
            if (!existingResident.getLotId().equalsIgnoreCase(residentDetails.getLotId().trim())) {
                Optional<Resident> duplicate = residentRepository.findByLotIdIgnoreCase(residentDetails.getLotId().trim());

                // Check that the found resident is not the same as the one being updated
                if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
                    log.warn("Duplicate lotId detected during update: {}", residentDetails.getLotId());
                    throw new DuplicateResidentException(residentDetails.getLotId());
                }
            }
        }
    }
    
    /**
     * Validates that an ID is valid and not empty
     */
    public void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("ID cannot be empty");
        }
    }

    /**
     * Validates that a search parameter is valid
     */
    public void validateSearchParameter(String paramName, String paramValue) {
        if (paramValue != null && paramValue.trim().isEmpty()) {
            throw new ValidationException("Parameter '" + paramName + "' cannot be empty");
        }
    }
}
