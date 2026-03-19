package com.copro.connect.service;

import com.copro.connect.model.*;
import com.copro.connect.repository.ResidentHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests ResidentHistoryService")
class ResidentHistoryServiceTest {

    @Mock
    private ResidentHistoryRepository residentHistoryRepository;

    @InjectMocks
    private ResidentHistoryService residentHistoryService;

    private Resident resident;

    @BeforeEach
    void setUp() {
        resident = new Resident();
        resident.setId("res-1");
        resident.setLotId("LOT-001");
        resident.setBatiment("A");
        resident.setEtage("1");
        resident.setPorte("101");
        resident.setProprietaireNom("Dupont");
        resident.setProprietaireEmail("dupont@test.com");
        resident.setStatutLot("Propriétaire Résident");
    }

    // ==================== recordUpdate ====================

    @Test
    @DisplayName("recordUpdate sans changements ne sauvegarde pas")
    void recordUpdate_noChanges_doesNotSave() {
        Resident same = copyResident(resident);

        residentHistoryService.recordUpdate(resident, same);

        verify(residentHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("recordUpdate avec changement de champ sauvegarde l'historique")
    void recordUpdate_withFieldChange_saves() {
        Resident updated = copyResident(resident);
        updated.setProprietaireNom("Martin");

        residentHistoryService.recordUpdate(resident, updated);

        ArgumentCaptor<ResidentHistory> captor = ArgumentCaptor.forClass(ResidentHistory.class);
        verify(residentHistoryRepository).save(captor.capture());
        ResidentHistory history = captor.getValue();

        assertThat(history.getActionType()).isEqualTo("UPDATE");
        assertThat(history.getResidentId()).isEqualTo("res-1");
        assertThat(history.getChanges()).isNotEmpty();
        assertThat(history.getApartmentKey()).isEqualTo("A-1-101");
        assertThat(history.getDescription()).contains("champ");
    }

    @Test
    @DisplayName("recordUpdate détecte ajout d'occupant")
    void recordUpdate_occupantAdded_detected() {
        Resident updated = copyResident(resident);
        Occupant occ = new Occupant("Jean Dupont", "0600000000", "jean@test.com");
        updated.setOccupants(List.of(occ));

        residentHistoryService.recordUpdate(resident, updated);

        ArgumentCaptor<ResidentHistory> captor = ArgumentCaptor.forClass(ResidentHistory.class);
        verify(residentHistoryRepository).save(captor.capture());
        boolean hasAdded = captor.getValue().getChanges().stream()
                .anyMatch(c -> ChangeDetail.CHANGE_TYPE_ADDED.equals(c.getChangeType())
                        && ChangeDetail.CATEGORY_OCCUPANT.equals(c.getCategory()));
        assertThat(hasAdded).isTrue();
    }

    @Test
    @DisplayName("recordUpdate détecte suppression d'occupant")
    void recordUpdate_occupantRemoved_detected() {
        Occupant occ = new Occupant("Jean Dupont", "0600000000", null);
        resident.setOccupants(List.of(occ));
        Resident updated = copyResident(resident);
        updated.setOccupants(List.of());

        residentHistoryService.recordUpdate(resident, updated);

        ArgumentCaptor<ResidentHistory> captor = ArgumentCaptor.forClass(ResidentHistory.class);
        verify(residentHistoryRepository).save(captor.capture());
        boolean hasRemoved = captor.getValue().getChanges().stream()
                .anyMatch(c -> ChangeDetail.CHANGE_TYPE_REMOVED.equals(c.getChangeType())
                        && ChangeDetail.CATEGORY_OCCUPANT.equals(c.getCategory()));
        assertThat(hasRemoved).isTrue();
    }

    @Test
    @DisplayName("recordUpdate détecte modification du mobile d'un occupant")
    void recordUpdate_occupantMobileChanged_detected() {
        Occupant oldOcc = new Occupant("Jean Dupont", "0600000000", null);
        resident.setOccupants(List.of(oldOcc));
        Resident updated = copyResident(resident);
        Occupant newOcc = new Occupant("Jean Dupont", "0700000000", null);
        updated.setOccupants(List.of(newOcc));

        residentHistoryService.recordUpdate(resident, updated);

        ArgumentCaptor<ResidentHistory> captor = ArgumentCaptor.forClass(ResidentHistory.class);
        verify(residentHistoryRepository).save(captor.capture());
        boolean hasMobileChange = captor.getValue().getChanges().stream()
                .anyMatch(c -> ChangeDetail.CHANGE_TYPE_MODIFIED.equals(c.getChangeType())
                        && c.getFieldLabel().contains("Tél."));
        assertThat(hasMobileChange).isTrue();
    }

    @Test
    @DisplayName("recordUpdate détecte modification email occupant")
    void recordUpdate_occupantEmailChanged_detected() {
        Occupant oldOcc = new Occupant("Jean Dupont", null, "jean@old.com");
        resident.setOccupants(List.of(oldOcc));
        Resident updated = copyResident(resident);
        Occupant newOcc = new Occupant("Jean Dupont", null, "jean@new.com");
        updated.setOccupants(List.of(newOcc));

        residentHistoryService.recordUpdate(resident, updated);

        ArgumentCaptor<ResidentHistory> captor = ArgumentCaptor.forClass(ResidentHistory.class);
        verify(residentHistoryRepository).save(captor.capture());
        boolean hasEmailChange = captor.getValue().getChanges().stream()
                .anyMatch(c -> c.getFieldLabel().contains("Email"));
        assertThat(hasEmailChange).isTrue();
    }

    @Test
    @DisplayName("recordUpdate détecte ajout compte Happix")
    void recordUpdate_happixAdded_detected() {
        Resident updated = copyResident(resident);
        HappixAccount h = new HappixAccount("Alice", "0600000000", "alice@test.com", "borne-1", "resident", "propriétaire");
        updated.setHappixAccounts(List.of(h));

        residentHistoryService.recordUpdate(resident, updated);

        ArgumentCaptor<ResidentHistory> captor = ArgumentCaptor.forClass(ResidentHistory.class);
        verify(residentHistoryRepository).save(captor.capture());
        boolean hasAdded = captor.getValue().getChanges().stream()
                .anyMatch(c -> ChangeDetail.CHANGE_TYPE_ADDED.equals(c.getChangeType())
                        && ChangeDetail.CATEGORY_HAPPIX.equals(c.getCategory()));
        assertThat(hasAdded).isTrue();
    }

    @Test
    @DisplayName("recordUpdate détecte suppression compte Happix")
    void recordUpdate_happixRemoved_detected() {
        HappixAccount h = new HappixAccount("Alice", null, null, null, null, null);
        resident.setHappixAccounts(List.of(h));
        Resident updated = copyResident(resident);
        updated.setHappixAccounts(List.of());

        residentHistoryService.recordUpdate(resident, updated);

        ArgumentCaptor<ResidentHistory> captor = ArgumentCaptor.forClass(ResidentHistory.class);
        verify(residentHistoryRepository).save(captor.capture());
        boolean hasRemoved = captor.getValue().getChanges().stream()
                .anyMatch(c -> ChangeDetail.CHANGE_TYPE_REMOVED.equals(c.getChangeType())
                        && ChangeDetail.CATEGORY_HAPPIX.equals(c.getCategory()));
        assertThat(hasRemoved).isTrue();
    }

    @Test
    @DisplayName("recordUpdate détecte modification des champs Happix")
    void recordUpdate_happixModified_detected() {
        HappixAccount oldH = new HappixAccount("Alice", "0600000000", "alice@old.com", "borne-1", "resident", "proche");
        resident.setHappixAccounts(List.of(oldH));
        Resident updated = copyResident(resident);
        HappixAccount newH = new HappixAccount("Alice", "0700000000", "alice@new.com", "borne-2", "autorisé", "ami");
        updated.setHappixAccounts(List.of(newH));

        residentHistoryService.recordUpdate(resident, updated);

        ArgumentCaptor<ResidentHistory> captor = ArgumentCaptor.forClass(ResidentHistory.class);
        verify(residentHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getChanges()).hasSizeGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("recordUpdate gère plusieurs changements dans la description")
    void recordUpdate_multipleChanges_buildDescription() {
        Resident updated = copyResident(resident);
        updated.setProprietaireNom("Martin");
        Occupant occ = new Occupant("Jean", null, null);
        updated.setOccupants(List.of(occ));
        HappixAccount h = new HappixAccount("Alice", null, null, null, null, null);
        updated.setHappixAccounts(List.of(h));

        residentHistoryService.recordUpdate(resident, updated);

        ArgumentCaptor<ResidentHistory> captor = ArgumentCaptor.forClass(ResidentHistory.class);
        verify(residentHistoryRepository).save(captor.capture());
        String description = captor.getValue().getDescription();
        assertThat(description).isNotBlank();
    }

    // ==================== recordDelete ====================

    @Test
    @DisplayName("recordDelete enregistre la suppression basique")
    void recordDelete_basic_saves() {
        residentHistoryService.recordDelete(resident);

        ArgumentCaptor<ResidentHistory> captor = ArgumentCaptor.forClass(ResidentHistory.class);
        verify(residentHistoryRepository).save(captor.capture());
        ResidentHistory history = captor.getValue();

        assertThat(history.getActionType()).isEqualTo("DELETE");
        assertThat(history.getResidentId()).isEqualTo("res-1");
        assertThat(history.getDescription()).contains("LOT-001");
    }

    @Test
    @DisplayName("recordDelete avec occupants et happix enregistre tous les changements")
    void recordDelete_withOccupantsAndHappix_savesAll() {
        resident.setOccupants(List.of(
                new Occupant("Jean", null, null),
                new Occupant("Marie", null, null)
        ));
        resident.setHappixAccounts(List.of(
                new HappixAccount("Alice", null, null, null, null, null)
        ));

        residentHistoryService.recordDelete(resident);

        ArgumentCaptor<ResidentHistory> captor = ArgumentCaptor.forClass(ResidentHistory.class);
        verify(residentHistoryRepository).save(captor.capture());
        List<ChangeDetail> changes = captor.getValue().getChanges();

        long removedOccupants = changes.stream()
                .filter(c -> ChangeDetail.CATEGORY_OCCUPANT.equals(c.getCategory())).count();
        long removedHappix = changes.stream()
                .filter(c -> ChangeDetail.CATEGORY_HAPPIX.equals(c.getCategory())).count();

        assertThat(removedOccupants).isEqualTo(2);
        assertThat(removedHappix).isEqualTo(1);
    }

    @Test
    @DisplayName("recordDelete sans propriétaire ni occupants ni happix")
    void recordDelete_minimal_saves() {
        resident.setProprietaireNom(null);
        resident.setOccupants(null);
        resident.setHappixAccounts(null);

        residentHistoryService.recordDelete(resident);

        verify(residentHistoryRepository).save(any(ResidentHistory.class));
    }

    // ==================== getApartmentHistory ====================

    @Test
    @DisplayName("getApartmentHistory retourne l'historique de l'appartement")
    void getApartmentHistory_returnsHistory() {
        ResidentHistory h = new ResidentHistory();
        when(residentHistoryRepository.findByApartmentKeyOrderByChangedAtDesc("A-1-101"))
                .thenReturn(List.of(h));

        List<ResidentHistory> result = residentHistoryService.getApartmentHistory("A", "1", "101");

        assertThat(result).hasSize(1);
        verify(residentHistoryRepository).findByApartmentKeyOrderByChangedAtDesc("A-1-101");
    }

    // ==================== getResidentHistory ====================

    @Test
    @DisplayName("getResidentHistory retourne l'historique du résident")
    void getResidentHistory_returnsHistory() {
        ResidentHistory h = new ResidentHistory();
        when(residentHistoryRepository.findByResidentIdOrderByChangedAtDesc("res-1"))
                .thenReturn(List.of(h));

        List<ResidentHistory> result = residentHistoryService.getResidentHistory("res-1");

        assertThat(result).hasSize(1);
        verify(residentHistoryRepository).findByResidentIdOrderByChangedAtDesc("res-1");
    }

    // ==================== Helper ====================

    private Resident copyResident(Resident original) {
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
        copy.setOccupants(original.getOccupants() != null ? new java.util.ArrayList<>(original.getOccupants()) : new java.util.ArrayList<>());
        copy.setHappixAccounts(original.getHappixAccounts() != null ? new java.util.ArrayList<>(original.getHappixAccounts()) : new java.util.ArrayList<>());
        return copy;
    }
}
