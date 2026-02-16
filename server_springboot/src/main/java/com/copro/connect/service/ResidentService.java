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
        
        // Si des filtres sont appliqués
        if ((search != null && !search.trim().isEmpty()) || 
            (batiment != null && !batiment.equals("Tous")) || 
            (statutLot != null && !statutLot.equals("Tous"))) {
            
            String searchTerm = (search != null && !search.trim().isEmpty()) ? search.trim() : "";
            String batFilter = (batiment != null && !batiment.equals("Tous")) ? batiment : null;
            String statutFilter = (statutLot != null && !statutLot.equals("Tous")) ? statutLot : null;
            
            residentPage = residentRepository.findWithFilters(searchTerm, batFilter, statutFilter, pageable);
        } else {
            // Pas de filtres, requête normale avec tri du Pageable
            // Le tri par défaut est géré dans createPageable()
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
        
        // Normaliser les données
        normalizeResidentData(resident);
        
        // Générer un nouvel ID si non fourni
        if (resident.getId() == null || resident.getId().isEmpty()) {
            resident.setId(null); // MongoDB génèrera automatiquement un ObjectId
        }
        
        return residentRepository.save(resident);
    }
    
    @Transactional
    public Resident updateResident(String id, Resident residentDetails) {
        log.info("Updating resident with id: {}", id);
        
        Resident resident = getResidentById(id);
        
        // Créer une copie de l'ancien résident pour l'historique
        Resident oldResident = createCopy(resident);
        
        // Normaliser les données
        normalizeResidentData(residentDetails);
        
        // Mise à jour des champs
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
        
        // Enregistrer dans l'historique
        try {
            residentHistoryService.recordUpdate(oldResident, updatedResident);
        } catch (Exception e) {
            log.error("Erreur lors de l'enregistrement de l'historique pour le résident {}", id, e);
            // On continue même si l'historique échoue pour ne pas bloquer la mise à jour
        }
        
        return updatedResident;
    }
    
    @Transactional
    public void deleteResident(String id) {
        log.info("Deleting resident with id: {}", id);
        Resident resident = getResidentById(id);
        
        // Enregistrer dans l'historique avant la suppression
        try {
            residentHistoryService.recordDelete(resident);
        } catch (Exception e) {
            log.error("Erreur lors de l'enregistrement de l'historique pour le résident {}", id, e);
            // On continue même si l'historique échoue pour ne pas bloquer la suppression
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
     * Crée un objet Pageable avec tri optionnel
     */
    private Pageable createPageable(int page, int size, String sort) {
        if (sort != null && !sort.trim().isEmpty()) {
            // Parser le paramètre sort: "field,direction" (ex: "lotId,asc")
            String[] sortParams = sort.split(",");
            String field = sortParams[0].trim();
            String direction = sortParams.length > 1 ? sortParams[1].trim() : "asc";
            
            Sort sortObj = direction.equalsIgnoreCase("desc") 
                ? Sort.by(field).descending() 
                : Sort.by(field).ascending();
            
            return PageRequest.of(page, size, sortObj);
        }
        // Tri par défaut: batiment asc, porte asc
        return PageRequest.of(page, size, Sort.by("batiment").ascending().and(Sort.by("porte").ascending()));
    }
    
    /**
     * Normalise les données du résident (trim des espaces, mise en forme)
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
        
        // Normaliser les occupants
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
        
        // Normaliser les comptes Happix
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
     * Crée une copie profonde d'un résident pour l'historique
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
        
        // Copie des occupants
        if (original.getOccupants() != null) {
            copy.setOccupants(new java.util.ArrayList<>(original.getOccupants()));
        } else {
            copy.setOccupants(new java.util.ArrayList<>());
        }
        
        // Copie des comptes Happix
        if (original.getHappixAccounts() != null) {
            copy.setHappixAccounts(new java.util.ArrayList<>(original.getHappixAccounts()));
        } else {
            copy.setHappixAccounts(new java.util.ArrayList<>());
        }
        
        return copy;
    }
}
