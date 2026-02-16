package com.copro.connect.service;

import com.copro.connect.model.*;
import com.copro.connect.repository.ResidentHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResidentHistoryService {
    
    private final ResidentHistoryRepository residentHistoryRepository;
    
    /**
     * Enregistre l'historique lors d'une modification en détectant les changements précis
     */
    @Transactional
    public void recordUpdate(Resident oldResident, Resident newResident) {
        log.info("Recording update history for resident: {}", oldResident.getId());
        
        List<ChangeDetail> changes = detectChanges(oldResident, newResident);
        
        if (changes.isEmpty()) {
            log.debug("No changes detected for resident: {}", oldResident.getId());
            return;
        }
        
        String description = buildDescription(changes);
        
        ResidentHistory history = new ResidentHistory();
        history.setResidentId(oldResident.getId());
        history.setLotId(oldResident.getLotId());
        history.setBatiment(oldResident.getBatiment());
        history.setEtage(oldResident.getEtage());
        history.setPorte(oldResident.getPorte());
        history.setActionType("UPDATE");
        history.setDescription(description);
        history.setChanges(changes);
        history.setChangedAt(Instant.now());
        history.setApartmentKey(buildApartmentKey(oldResident.getBatiment(), oldResident.getEtage(), oldResident.getPorte()));
        
        residentHistoryRepository.save(history);
    }
    
    /**
     * Enregistre l'historique lors d'une suppression
     */
    @Transactional
    public void recordDelete(Resident resident) {
        log.info("Recording delete history for resident: {}", resident.getId());
        
        List<ChangeDetail> changes = new ArrayList<>();
        
        changes.add(new ChangeDetail("LOT", "REMOVED", 
                "Lot " + resident.getLotId(), 
                formatLotSummary(resident), null));
        
        if (resident.getProprietaireNom() != null) {
            changes.add(new ChangeDetail("PROPRIETAIRE", "REMOVED", 
                    "Propriétaire", resident.getProprietaireNom(), null));
        }
        
        List<Occupant> occupants = resident.getOccupants() != null ? resident.getOccupants() : Collections.emptyList();
        for (Occupant occ : occupants) {
            changes.add(new ChangeDetail("OCCUPANT", "REMOVED", 
                    "Occupant", occ.getNom(), null));
        }
        
        List<HappixAccount> happix = resident.getHappixAccounts() != null ? resident.getHappixAccounts() : Collections.emptyList();
        for (HappixAccount h : happix) {
            changes.add(new ChangeDetail("HAPPIX", "REMOVED", 
                    "Compte Happix", h.getNom(), null));
        }
        
        String description = "Suppression du lot " + resident.getLotId() 
                + " (Bât. " + resident.getBatiment() + ", Appt " + resident.getPorte() + ")";
        
        ResidentHistory history = new ResidentHistory();
        history.setResidentId(resident.getId());
        history.setLotId(resident.getLotId());
        history.setBatiment(resident.getBatiment());
        history.setEtage(resident.getEtage());
        history.setPorte(resident.getPorte());
        history.setActionType("DELETE");
        history.setDescription(description);
        history.setChanges(changes);
        history.setChangedAt(Instant.now());
        history.setApartmentKey(buildApartmentKey(resident.getBatiment(), resident.getEtage(), resident.getPorte()));
        
        residentHistoryRepository.save(history);
    }
    
    public List<ResidentHistory> getApartmentHistory(String batiment, String etage, String porte) {
        log.debug("Fetching history for apartment: {}-{}-{}", batiment, etage, porte);
        String apartmentKey = buildApartmentKey(batiment, etage, porte);
        return residentHistoryRepository.findByApartmentKeyOrderByChangedAtDesc(apartmentKey);
    }
    
    public List<ResidentHistory> getResidentHistory(String residentId) {
        log.debug("Fetching history for resident: {}", residentId);
        return residentHistoryRepository.findByResidentIdOrderByChangedAtDesc(residentId);
    }
    
    // ==================== DETECTION DES CHANGEMENTS ====================
    
    private List<ChangeDetail> detectChanges(Resident oldR, Resident newR) {
        List<ChangeDetail> changes = new ArrayList<>();
        
        // Champs du lot
        compareField(changes, "LOT", "Numéro de lot", oldR.getLotId(), newR.getLotId());
        compareField(changes, "LOT", "Bâtiment", oldR.getBatiment(), newR.getBatiment());
        compareField(changes, "LOT", "Étage", oldR.getEtage(), newR.getEtage());
        compareField(changes, "LOT", "Appartement", oldR.getPorte(), newR.getPorte());
        compareField(changes, "LOT", "Cave", oldR.getCaveId(), newR.getCaveId());
        compareField(changes, "LOT", "Statut", oldR.getStatutLot(), newR.getStatutLot());
        
        // Champs du propriétaire
        compareField(changes, "PROPRIETAIRE", "Nom du propriétaire", oldR.getProprietaireNom(), newR.getProprietaireNom());
        compareField(changes, "PROPRIETAIRE", "Téléphone du propriétaire", oldR.getProprietaireMobile(), newR.getProprietaireMobile());
        compareField(changes, "PROPRIETAIRE", "Email du propriétaire", oldR.getProprietaireEmail(), newR.getProprietaireEmail());
        
        // Occupants
        detectOccupantChanges(changes, 
                oldR.getOccupants() != null ? oldR.getOccupants() : Collections.emptyList(),
                newR.getOccupants() != null ? newR.getOccupants() : Collections.emptyList());
        
        // Comptes Happix
        detectHappixChanges(changes,
                oldR.getHappixAccounts() != null ? oldR.getHappixAccounts() : Collections.emptyList(),
                newR.getHappixAccounts() != null ? newR.getHappixAccounts() : Collections.emptyList());
        
        return changes;
    }
    
    private void compareField(List<ChangeDetail> changes, String category, String label, String oldVal, String newVal) {
        String old = normalizeValue(oldVal);
        String nw = normalizeValue(newVal);
        
        if (!Objects.equals(old, nw)) {
            changes.add(new ChangeDetail(category, "MODIFIED", label, old, nw));
        }
    }
    
    private void detectOccupantChanges(List<ChangeDetail> changes, List<Occupant> oldList, List<Occupant> newList) {
        // Indexer les occupants par nom pour la comparaison
        Map<String, Occupant> oldMap = new LinkedHashMap<>();
        for (Occupant o : oldList) {
            if (o.getNom() != null) oldMap.put(o.getNom().trim().toLowerCase(), o);
        }
        
        Map<String, Occupant> newMap = new LinkedHashMap<>();
        for (Occupant o : newList) {
            if (o.getNom() != null) newMap.put(o.getNom().trim().toLowerCase(), o);
        }
        
        // Occupants supprimés
        for (Map.Entry<String, Occupant> entry : oldMap.entrySet()) {
            if (!newMap.containsKey(entry.getKey())) {
                changes.add(new ChangeDetail("OCCUPANT", "REMOVED", 
                        "Occupant", entry.getValue().getNom(), null));
            }
        }
        
        // Occupants ajoutés
        for (Map.Entry<String, Occupant> entry : newMap.entrySet()) {
            if (!oldMap.containsKey(entry.getKey())) {
                changes.add(new ChangeDetail("OCCUPANT", "ADDED", 
                        "Occupant", null, entry.getValue().getNom()));
            }
        }
        
        // Occupants modifiés (même nom, données différentes)
        for (Map.Entry<String, Occupant> entry : newMap.entrySet()) {
            if (oldMap.containsKey(entry.getKey())) {
                Occupant oldOcc = oldMap.get(entry.getKey());
                Occupant newOcc = entry.getValue();
                
                if (!Objects.equals(normalizeValue(oldOcc.getMobile()), normalizeValue(newOcc.getMobile()))) {
                    changes.add(new ChangeDetail("OCCUPANT", "MODIFIED", 
                            "Tél. de " + newOcc.getNom(), 
                            oldOcc.getMobile(), newOcc.getMobile()));
                }
                if (!Objects.equals(normalizeValue(oldOcc.getEmail()), normalizeValue(newOcc.getEmail()))) {
                    changes.add(new ChangeDetail("OCCUPANT", "MODIFIED", 
                            "Email de " + newOcc.getNom(), 
                            oldOcc.getEmail(), newOcc.getEmail()));
                }
            }
        }
    }
    
    private void detectHappixChanges(List<ChangeDetail> changes, List<HappixAccount> oldList, List<HappixAccount> newList) {
        // Indexer par nom pour la comparaison
        Map<String, HappixAccount> oldMap = new LinkedHashMap<>();
        for (HappixAccount h : oldList) {
            if (h.getNom() != null) oldMap.put(h.getNom().trim().toLowerCase(), h);
        }
        
        Map<String, HappixAccount> newMap = new LinkedHashMap<>();
        for (HappixAccount h : newList) {
            if (h.getNom() != null) newMap.put(h.getNom().trim().toLowerCase(), h);
        }
        
        // Happix supprimés
        for (Map.Entry<String, HappixAccount> entry : oldMap.entrySet()) {
            if (!newMap.containsKey(entry.getKey())) {
                HappixAccount h = entry.getValue();
                changes.add(new ChangeDetail("HAPPIX", "REMOVED", 
                        "Compte Happix", h.getNom(), null));
            }
        }
        
        // Happix ajoutés
        for (Map.Entry<String, HappixAccount> entry : newMap.entrySet()) {
            if (!oldMap.containsKey(entry.getKey())) {
                HappixAccount h = entry.getValue();
                changes.add(new ChangeDetail("HAPPIX", "ADDED", 
                        "Compte Happix", null, h.getNom()));
            }
        }
        
        // Happix modifiés
        for (Map.Entry<String, HappixAccount> entry : newMap.entrySet()) {
            if (oldMap.containsKey(entry.getKey())) {
                HappixAccount oldH = oldMap.get(entry.getKey());
                HappixAccount newH = entry.getValue();
                
                if (!Objects.equals(normalizeValue(oldH.getMobile()), normalizeValue(newH.getMobile()))) {
                    changes.add(new ChangeDetail("HAPPIX", "MODIFIED", 
                            "Tél. Happix de " + newH.getNom(), 
                            oldH.getMobile(), newH.getMobile()));
                }
                if (!Objects.equals(normalizeValue(oldH.getEmail()), normalizeValue(newH.getEmail()))) {
                    changes.add(new ChangeDetail("HAPPIX", "MODIFIED", 
                            "Email Happix de " + newH.getNom(), 
                            oldH.getEmail(), newH.getEmail()));
                }
                if (!Objects.equals(normalizeValue(oldH.getType()), normalizeValue(newH.getType()))) {
                    changes.add(new ChangeDetail("HAPPIX", "MODIFIED", 
                            "Type Happix de " + newH.getNom(), 
                            oldH.getType(), newH.getType()));
                }
                if (!Objects.equals(normalizeValue(oldH.getRelation()), normalizeValue(newH.getRelation()))) {
                    changes.add(new ChangeDetail("HAPPIX", "MODIFIED", 
                            "Relation Happix de " + newH.getNom(), 
                            oldH.getRelation(), newH.getRelation()));
                }
                if (!Objects.equals(normalizeValue(oldH.getNomBorne()), normalizeValue(newH.getNomBorne()))) {
                    changes.add(new ChangeDetail("HAPPIX", "MODIFIED", 
                            "Nom borne de " + newH.getNom(), 
                            oldH.getNomBorne(), newH.getNomBorne()));
                }
            }
        }
    }
    
    // ==================== UTILITAIRES ====================
    
    /**
     * Génère une description lisible à partir de la liste des changements
     */
    private String buildDescription(List<ChangeDetail> changes) {
        List<String> parts = new ArrayList<>();
        
        long modifiedFields = changes.stream().filter(c -> "MODIFIED".equals(c.getChangeType()) 
                && !"OCCUPANT".equals(c.getCategory()) && !"HAPPIX".equals(c.getCategory())).count();
        long addedOccupants = changes.stream().filter(c -> "OCCUPANT".equals(c.getCategory()) && "ADDED".equals(c.getChangeType())).count();
        long removedOccupants = changes.stream().filter(c -> "OCCUPANT".equals(c.getCategory()) && "REMOVED".equals(c.getChangeType())).count();
        long modifiedOccupants = changes.stream().filter(c -> "OCCUPANT".equals(c.getCategory()) && "MODIFIED".equals(c.getChangeType())).count();
        long addedHappix = changes.stream().filter(c -> "HAPPIX".equals(c.getCategory()) && "ADDED".equals(c.getChangeType())).count();
        long removedHappix = changes.stream().filter(c -> "HAPPIX".equals(c.getCategory()) && "REMOVED".equals(c.getChangeType())).count();
        long modifiedHappix = changes.stream().filter(c -> "HAPPIX".equals(c.getCategory()) && "MODIFIED".equals(c.getChangeType())).count();
        
        if (modifiedFields > 0) {
            parts.add("Modification de " + modifiedFields + " champ" + (modifiedFields > 1 ? "s" : ""));
        }
        if (addedOccupants > 0) parts.add("Ajout de " + addedOccupants + " occupant" + (addedOccupants > 1 ? "s" : ""));
        if (removedOccupants > 0) parts.add("Suppression de " + removedOccupants + " occupant" + (removedOccupants > 1 ? "s" : ""));
        if (modifiedOccupants > 0) parts.add("Modification d'occupant" + (modifiedOccupants > 1 ? "s" : ""));
        if (addedHappix > 0) parts.add("Ajout de " + addedHappix + " compte" + (addedHappix > 1 ? "s" : "") + " Happix");
        if (removedHappix > 0) parts.add("Suppression de " + removedHappix + " compte" + (removedHappix > 1 ? "s" : "") + " Happix");
        if (modifiedHappix > 0) parts.add("Modification de compte" + (modifiedHappix > 1 ? "s" : "") + " Happix");
        
        return String.join(", ", parts);
    }
    
    private String formatLotSummary(Resident resident) {
        return "Bât. " + resident.getBatiment() + ", Étage " + resident.getEtage() 
                + ", Appt " + resident.getPorte();
    }
    
    private String normalizeValue(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
    }
    
    private String buildApartmentKey(String batiment, String etage, String porte) {
        return String.format("%s-%s-%s", batiment, etage, porte);
    }
}
