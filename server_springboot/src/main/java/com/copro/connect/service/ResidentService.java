package com.copro.connect.service;

import com.copro.connect.dto.PagedResidentsResponse;
import com.copro.connect.dto.StatisticsResponse;
import com.copro.connect.exception.ResidentNotFoundException;
import com.copro.connect.model.Resident;
import com.copro.connect.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResidentService {
    
    private final ResidentRepository residentRepository;
    private final ResidentHistoryService residentHistoryService;
    
    public List<Resident> getAllResidents() {
        log.debug("Fetching all residents");
        return residentRepository.findAllByOrderByBatimentAscPorteAsc();
    }
    
    public PagedResidentsResponse getResidentsPaginated(int page, int size, String search, String batiment, String statutLot, String sort) {
        log.debug("Fetching residents - page: {}, size: {}, search: {}, batiment: {}, statut: {}, sort: {}", 
                  page, size, search, batiment, statutLot, sort);
        
        Pageable pageable = createPageable(page, size, sort);
        Page<Resident> residentPage;
        
        // If filters are applied
        if ((search != null && !search.trim().isEmpty()) || 
            (batiment != null && !batiment.equals("Tous")) || 
            (statutLot != null && !statutLot.equals("Tous"))) {
            
            String searchTerm = (search != null && !search.trim().isEmpty()) ? search.trim() : "";
            String batFilter = (batiment != null && !batiment.equals("Tous")) ? batiment : null;
            String statutFilter = (statutLot != null && !statutLot.equals("Tous")) ? statutLot : null;
            
            residentPage = residentRepository.findWithFilters(searchTerm, batFilter, statutFilter, pageable);
        } else {
            // No filters, normal query with Pageable sorting
            // Default sorting is handled in createPageable()
            residentPage = residentRepository.findAll(pageable);
        }
        
        return new PagedResidentsResponse(
            residentPage.getContent(),
            residentPage.getNumber(),
            residentPage.getTotalPages(),
            residentPage.getTotalElements(),
            residentPage.getSize()
        );
    }
    
    public StatisticsResponse getStatistics() {
        log.debug("Calculating statistics");
        return residentRepository.calculateStatistics();
    }
    
    public Resident getResidentById(String id) {
        log.debug("Fetching resident with id: {}", id);
        return residentRepository.findById(id)
                .orElseThrow(() -> new ResidentNotFoundException(id));
    }
    
    @Transactional
    public Resident createResident(Resident resident) {
        log.info("Creating new resident: {}", resident.getLotId());
        
        // Normalize data
        normalizeResidentData(resident);

        // Generate a new ID if not provided
        if (resident.getId() == null || resident.getId().isEmpty()) {
            resident.setId(null); // MongoDB will automatically generate an ObjectId
        }
        
        return residentRepository.save(resident);
    }
    
    @Transactional
    public Resident updateResident(String id, Resident residentDetails) {
        log.info("Updating resident with id: {}", id);
        
        Resident resident = getResidentById(id);
        
        // Create a copy of the old resident for history
        Resident oldResident = createCopy(resident);

        // Normalize data
        normalizeResidentData(residentDetails);

        // Update fields
        resident.setLotId(residentDetails.getLotId());
        resident.setBatiment(residentDetails.getBatiment());
        resident.setEtage(residentDetails.getEtage());
        resident.setPorte(residentDetails.getPorte());
        resident.setCaveId(residentDetails.getCaveId());
        resident.setStatutLot(residentDetails.getStatutLot());
        resident.setProprietaireNom(residentDetails.getProprietaireNom());
        resident.setProprietaireMobile(residentDetails.getProprietaireMobile());
        resident.setProprietaireEmail(residentDetails.getProprietaireEmail());
        resident.setOccupants(residentDetails.getOccupants());
        resident.setHappixAccounts(residentDetails.getHappixAccounts());
        
        Resident updatedResident = residentRepository.save(resident);
        
        // Record in history
        try {
            residentHistoryService.recordUpdate(oldResident, updatedResident);
        } catch (Exception e) {
            log.error("Error recording history for resident {}", id, e);
            // Continue even if history fails to not block the update
        }
        
        return updatedResident;
    }
    
    @Transactional
    public void deleteResident(String id) {
        log.info("Deleting resident with id: {}", id);
        Resident resident = getResidentById(id);
        
        // Record in history before deletion
        try {
            residentHistoryService.recordDelete(resident);
        } catch (Exception e) {
            log.error("Error recording history for resident {}", id, e);
            // Continue even if history fails to not block the deletion
        }
        
        residentRepository.delete(resident);
    }
    
    public List<Resident> getResidentsByBatiment(String batiment) {
        log.debug("Fetching residents for building: {}", batiment);
        return residentRepository.findByBatiment(batiment);
    }
    
    public List<Resident> getResidentsByStatus(String statutLot) {
        log.debug("Fetching residents with status: {}", statutLot);
        return residentRepository.findByStatutLot(statutLot);
    }
    
    /**
     * Creates a Pageable object with optional sorting
     */
    private Pageable createPageable(int page, int size, String sort) {
        if (sort != null && !sort.trim().isEmpty()) {
            // Parse the sort parameter: "field,direction" (e.g. "lotId,asc")
            String[] sortParams = sort.split(",");
            String field = sortParams[0].trim();
            String direction = sortParams.length > 1 ? sortParams[1].trim() : "asc";
            
            Sort sortObj = direction.equalsIgnoreCase("desc") 
                ? Sort.by(field).descending() 
                : Sort.by(field).ascending();
            
            return PageRequest.of(page, size, sortObj);
        }
        // Default sort: batiment asc, porte asc
        return PageRequest.of(page, size, Sort.by("batiment").ascending().and(Sort.by("porte").ascending()));
    }
    
    /**
     * Normalizes resident data (trims whitespace, formatting)
     */
    private void normalizeResidentData(Resident resident) {
        if (resident.getLotId() != null) {
            resident.setLotId(resident.getLotId().trim());
        }
        if (resident.getBatiment() != null) {
            resident.setBatiment(resident.getBatiment().trim());
        }
        if (resident.getEtage() != null) {
            resident.setEtage(resident.getEtage().trim());
        }
        if (resident.getPorte() != null) {
            resident.setPorte(resident.getPorte().trim());
        }
        if (resident.getProprietaireNom() != null) {
            resident.setProprietaireNom(resident.getProprietaireNom().trim());
        }
        if (resident.getProprietaireEmail() != null) {
            resident.setProprietaireEmail(resident.getProprietaireEmail().trim().toLowerCase());
        }
        if (resident.getProprietaireMobile() != null) {
            resident.setProprietaireMobile(resident.getProprietaireMobile().trim());
        }
        
        // Normalize occupants
        if (resident.getOccupants() != null) {
            resident.getOccupants().forEach(occupant -> {
                if (occupant.getNom() != null) {
                    occupant.setNom(occupant.getNom().trim());
                }
                if (occupant.getEmail() != null) {
                    occupant.setEmail(occupant.getEmail().trim().toLowerCase());
                }
                if (occupant.getMobile() != null) {
                    occupant.setMobile(occupant.getMobile().trim());
                }
            });
        }
        
        // Normalize Happix accounts
        if (resident.getHappixAccounts() != null) {
            resident.getHappixAccounts().forEach(account -> {
                if (account.getNom() != null) {
                    account.setNom(account.getNom().trim());
                }
                if (account.getEmail() != null) {
                    account.setEmail(account.getEmail().trim().toLowerCase());
                }
                if (account.getMobile() != null) {
                    account.setMobile(account.getMobile().trim());
                }
            });
        }
    }
    
    /**
     * Creates a deep copy of a resident for history tracking
     */
    private Resident createCopy(Resident original) {
        Resident copy = new Resident();
        copy.setId(original.getId());
        copy.setLotId(original.getLotId());
        copy.setBatiment(original.getBatiment());
        copy.setEtage(original.getEtage());
        copy.setPorte(original.getPorte());
        copy.setCaveId(original.getCaveId());
        copy.setStatutLot(original.getStatutLot());
        copy.setProprietaireNom(original.getProprietaireNom());
        copy.setProprietaireMobile(original.getProprietaireMobile());
        copy.setProprietaireEmail(original.getProprietaireEmail());
        copy.setCreatedAt(original.getCreatedAt());
        copy.setUpdatedAt(original.getUpdatedAt());
        
        // Copy occupants
        if (original.getOccupants() != null) {
            copy.setOccupants(new java.util.ArrayList<>(original.getOccupants()));
        } else {
            copy.setOccupants(new java.util.ArrayList<>());
        }
        
        // Copy Happix accounts
        if (original.getHappixAccounts() != null) {
            copy.setHappixAccounts(new java.util.ArrayList<>(original.getHappixAccounts()));
        } else {
            copy.setHappixAccounts(new java.util.ArrayList<>());
        }
        
        return copy;
    }
}
