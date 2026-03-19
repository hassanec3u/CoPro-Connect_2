package com.copro.connect.service;

import com.copro.connect.model.*;
import com.copro.connect.repository.ResidentHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResidentHistoryService {
    
    private final ResidentHistoryRepository residentHistoryRepository;
    
    /**
     * Records history during an update by detecting precise changes
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
     * Records history during a deletion
     */
    @Transactional
    public void recordDelete(Resident resident) {
        log.info("Recording delete history for resident: {}", resident.getId());
        
        List<ChangeDetail> changes = new ArrayList<>();
        
        changes.add(new ChangeDetail(ChangeDetail.CATEGORY_LOT, ChangeDetail.CHANGE_TYPE_REMOVED,
                "Lot " + resident.getLotId(),
                formatLotSummary(resident), null));

        if (resident.getProprietaireNom() != null) {
            changes.add(new ChangeDetail(ChangeDetail.CATEGORY_PROPRIETAIRE, ChangeDetail.CHANGE_TYPE_REMOVED,
                    "Owner", resident.getProprietaireNom(), null));
        }

        List<Occupant> occupants = resident.getOccupants() != null ? resident.getOccupants() : Collections.emptyList();
        for (Occupant occ : occupants) {
            changes.add(new ChangeDetail(ChangeDetail.CATEGORY_OCCUPANT, ChangeDetail.CHANGE_TYPE_REMOVED,
                    ChangeDetail.LABEL_OCCUPANT, occ.getNom(), null));
        }

        List<HappixAccount> happix = resident.getHappixAccounts() != null ? resident.getHappixAccounts() : Collections.emptyList();
        for (HappixAccount h : happix) {
            changes.add(new ChangeDetail(ChangeDetail.CATEGORY_HAPPIX, ChangeDetail.CHANGE_TYPE_REMOVED,
                    ChangeDetail.LABEL_COMPTE_HAPPIX, h.getNom(), null));
        }
        
        String description = "Deletion of lot " + resident.getLotId()
                + " (Bldg. " + resident.getBatiment() + ", Apt " + resident.getPorte() + ")";
        
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
    
    // ==================== CHANGE DETECTION ====================
    
    private List<ChangeDetail> detectChanges(Resident oldR, Resident newR) {
        List<ChangeDetail> changes = new ArrayList<>();
        
        // Lot fields
        compareField(changes, ChangeDetail.CATEGORY_LOT, "Lot number", oldR.getLotId(), newR.getLotId());
        compareField(changes, ChangeDetail.CATEGORY_LOT, "Building", oldR.getBatiment(), newR.getBatiment());
        compareField(changes, ChangeDetail.CATEGORY_LOT, "Floor", oldR.getEtage(), newR.getEtage());
        compareField(changes, ChangeDetail.CATEGORY_LOT, "Apartment", oldR.getPorte(), newR.getPorte());
        compareField(changes, ChangeDetail.CATEGORY_LOT, "Storage room", oldR.getCaveId(), newR.getCaveId());
        compareField(changes, ChangeDetail.CATEGORY_LOT, "Status", oldR.getStatutLot(), newR.getStatutLot());

        // Owner fields
        compareField(changes, ChangeDetail.CATEGORY_PROPRIETAIRE, "Owner name", oldR.getProprietaireNom(), newR.getProprietaireNom());
        compareField(changes, ChangeDetail.CATEGORY_PROPRIETAIRE, "Owner phone", oldR.getProprietaireMobile(), newR.getProprietaireMobile());
        compareField(changes, ChangeDetail.CATEGORY_PROPRIETAIRE, "Owner email", oldR.getProprietaireEmail(), newR.getProprietaireEmail());

        // Occupants
        detectOccupantChanges(changes, 
                oldR.getOccupants() != null ? oldR.getOccupants() : Collections.emptyList(),
                newR.getOccupants() != null ? newR.getOccupants() : Collections.emptyList());
        
        // Happix accounts
        detectHappixChanges(changes,
                oldR.getHappixAccounts() != null ? oldR.getHappixAccounts() : Collections.emptyList(),
                newR.getHappixAccounts() != null ? newR.getHappixAccounts() : Collections.emptyList());
        
        return changes;
    }
    
    private void compareField(List<ChangeDetail> changes, String category, String label, String oldVal, String newVal) {
        String old = normalizeValue(oldVal);
        String nw = normalizeValue(newVal);
        
        if (!Objects.equals(old, nw)) {
            changes.add(new ChangeDetail(category, ChangeDetail.CHANGE_TYPE_MODIFIED, label, old, nw));
        }
    }
    
    private void detectOccupantChanges(List<ChangeDetail> changes, List<Occupant> oldList, List<Occupant> newList) {
        // Index occupants by name for comparison
        Map<String, Occupant> oldMap = new LinkedHashMap<>();
        for (Occupant o : oldList) {
            if (o.getNom() != null) oldMap.put(o.getNom().trim().toLowerCase(), o);
        }
        
        Map<String, Occupant> newMap = new LinkedHashMap<>();
        for (Occupant o : newList) {
            if (o.getNom() != null) newMap.put(o.getNom().trim().toLowerCase(), o);
        }
        
        // Removed occupants
        for (Map.Entry<String, Occupant> entry : oldMap.entrySet()) {
            if (!newMap.containsKey(entry.getKey())) {
                changes.add(new ChangeDetail(ChangeDetail.CATEGORY_OCCUPANT, ChangeDetail.CHANGE_TYPE_REMOVED,
                        ChangeDetail.LABEL_OCCUPANT, entry.getValue().getNom(), null));
            }
        }

        // Added occupants
        for (Map.Entry<String, Occupant> entry : newMap.entrySet()) {
            if (!oldMap.containsKey(entry.getKey())) {
                changes.add(new ChangeDetail(ChangeDetail.CATEGORY_OCCUPANT, ChangeDetail.CHANGE_TYPE_ADDED,
                        ChangeDetail.LABEL_OCCUPANT, null, entry.getValue().getNom()));
            }
        }

        // Modified occupants (same name, different data)
        addOccupantModifiedChanges(changes, oldMap, newMap);
    }

    private void addOccupantModifiedChanges(List<ChangeDetail> changes, Map<String, Occupant> oldMap, Map<String, Occupant> newMap) {
        for (Map.Entry<String, Occupant> entry : newMap.entrySet()) {
            if (!oldMap.containsKey(entry.getKey())) continue;
            Occupant oldOcc = oldMap.get(entry.getKey());
            Occupant newOcc = entry.getValue();
            if (!Objects.equals(normalizeValue(oldOcc.getMobile()), normalizeValue(newOcc.getMobile()))) {
                changes.add(new ChangeDetail(ChangeDetail.CATEGORY_OCCUPANT, ChangeDetail.CHANGE_TYPE_MODIFIED,
                        "Phone of " + newOcc.getNom(), oldOcc.getMobile(), newOcc.getMobile()));
            }
            if (!Objects.equals(normalizeValue(oldOcc.getEmail()), normalizeValue(newOcc.getEmail()))) {
                changes.add(new ChangeDetail(ChangeDetail.CATEGORY_OCCUPANT, ChangeDetail.CHANGE_TYPE_MODIFIED,
                        "Email of " + newOcc.getNom(), oldOcc.getEmail(), newOcc.getEmail()));
            }
        }
    }
    
    private void detectHappixChanges(List<ChangeDetail> changes, List<HappixAccount> oldList, List<HappixAccount> newList) {
        // Index by name for comparison
        Map<String, HappixAccount> oldMap = new LinkedHashMap<>();
        for (HappixAccount h : oldList) {
            if (h.getNom() != null) oldMap.put(h.getNom().trim().toLowerCase(), h);
        }
        
        Map<String, HappixAccount> newMap = new LinkedHashMap<>();
        for (HappixAccount h : newList) {
            if (h.getNom() != null) newMap.put(h.getNom().trim().toLowerCase(), h);
        }
        
        // Removed Happix
        for (Map.Entry<String, HappixAccount> entry : oldMap.entrySet()) {
            if (!newMap.containsKey(entry.getKey())) {
                HappixAccount h = entry.getValue();
                changes.add(new ChangeDetail(ChangeDetail.CATEGORY_HAPPIX, ChangeDetail.CHANGE_TYPE_REMOVED,
                        ChangeDetail.LABEL_COMPTE_HAPPIX, h.getNom(), null));
            }
        }

        // Added Happix
        for (Map.Entry<String, HappixAccount> entry : newMap.entrySet()) {
            if (!oldMap.containsKey(entry.getKey())) {
                HappixAccount h = entry.getValue();
                changes.add(new ChangeDetail(ChangeDetail.CATEGORY_HAPPIX, ChangeDetail.CHANGE_TYPE_ADDED,
                        ChangeDetail.LABEL_COMPTE_HAPPIX, null, h.getNom()));
            }
        }

        // Modified Happix
        addHappixModifiedChanges(changes, oldMap, newMap);
    }

    private void addHappixModifiedChanges(List<ChangeDetail> changes, Map<String, HappixAccount> oldMap, Map<String, HappixAccount> newMap) {
        for (Map.Entry<String, HappixAccount> entry : newMap.entrySet()) {
            if (!oldMap.containsKey(entry.getKey())) continue;
            HappixAccount oldH = oldMap.get(entry.getKey());
            HappixAccount newH = entry.getValue();
            addHappixFieldChange(changes, newH, "Happix phone of ", oldH.getMobile(), newH.getMobile());
            addHappixFieldChange(changes, newH, "Happix email of ", oldH.getEmail(), newH.getEmail());
            addHappixFieldChange(changes, newH, "Happix type of ", oldH.getType(), newH.getType());
            addHappixFieldChange(changes, newH, "Happix relationship of ", oldH.getRelation(), newH.getRelation());
            if (!Objects.equals(normalizeValue(oldH.getNomBorne()), normalizeValue(newH.getNomBorne()))) {
                changes.add(new ChangeDetail(ChangeDetail.CATEGORY_HAPPIX, ChangeDetail.CHANGE_TYPE_MODIFIED,
                        "Terminal name of " + newH.getNom(), oldH.getNomBorne(), newH.getNomBorne()));
            }
        }
    }

    private void addHappixFieldChange(List<ChangeDetail> changes, HappixAccount newH, String labelPrefix, String oldVal, String newVal) {
        if (Objects.equals(normalizeValue(oldVal), normalizeValue(newVal))) return;
        changes.add(new ChangeDetail(ChangeDetail.CATEGORY_HAPPIX, ChangeDetail.CHANGE_TYPE_MODIFIED,
                labelPrefix + newH.getNom(), oldVal, newVal));
    }
    
    // ==================== UTILITIES ====================

    /**
     * Generates a human-readable description from the list of changes
     */
    private String buildDescription(List<ChangeDetail> changes) {
        long modifiedFields = countChanges(changes, null, ChangeDetail.CHANGE_TYPE_MODIFIED,
                c -> !ChangeDetail.CATEGORY_OCCUPANT.equals(c.getCategory()) && !ChangeDetail.CATEGORY_HAPPIX.equals(c.getCategory()));
        long addedOccupants = countChanges(changes, ChangeDetail.CATEGORY_OCCUPANT, ChangeDetail.CHANGE_TYPE_ADDED, null);
        long removedOccupants = countChanges(changes, ChangeDetail.CATEGORY_OCCUPANT, ChangeDetail.CHANGE_TYPE_REMOVED, null);
        long modifiedOccupants = countChanges(changes, ChangeDetail.CATEGORY_OCCUPANT, ChangeDetail.CHANGE_TYPE_MODIFIED, null);
        long addedHappix = countChanges(changes, ChangeDetail.CATEGORY_HAPPIX, ChangeDetail.CHANGE_TYPE_ADDED, null);
        long removedHappix = countChanges(changes, ChangeDetail.CATEGORY_HAPPIX, ChangeDetail.CHANGE_TYPE_REMOVED, null);
        long modifiedHappix = countChanges(changes, ChangeDetail.CATEGORY_HAPPIX, ChangeDetail.CHANGE_TYPE_MODIFIED, null);

        List<String> parts = new ArrayList<>();
        if (modifiedFields > 0) parts.add(modifiedFields + " field" + (modifiedFields > 1 ? "s" : "") + " modified");
        if (addedOccupants > 0) parts.add(addedOccupants + " occupant" + (addedOccupants > 1 ? "s" : "") + " added");
        if (removedOccupants > 0) parts.add(removedOccupants + " occupant" + (removedOccupants > 1 ? "s" : "") + " removed");
        if (modifiedOccupants > 0) parts.add("occupant" + (modifiedOccupants > 1 ? "s" : "") + " modified");
        if (addedHappix > 0) parts.add(addedHappix + " Happix account" + (addedHappix > 1 ? "s" : "") + " added");
        if (removedHappix > 0) parts.add(removedHappix + " Happix account" + (removedHappix > 1 ? "s" : "") + " removed");
        if (modifiedHappix > 0) parts.add("Happix account" + (modifiedHappix > 1 ? "s" : "") + " modified");

        return String.join(", ", parts);
    }

    private long countChanges(List<ChangeDetail> changes, String category, String changeType, java.util.function.Predicate<ChangeDetail> extraFilter) {
        return changes.stream()
                .filter(c -> (category == null || category.equals(c.getCategory())) && changeType.equals(c.getChangeType()))
                .filter(c -> extraFilter == null || extraFilter.test(c))
                .count();
    }
    
    private String formatLotSummary(Resident resident) {
        return "Bldg. " + resident.getBatiment() + ", Floor " + resident.getEtage()
                + ", Apt " + resident.getPorte();
    }
    
    private String normalizeValue(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
    }
    
    private String buildApartmentKey(String batiment, String etage, String porte) {
        return String.format("%s-%s-%s", batiment, etage, porte);
    }
}
