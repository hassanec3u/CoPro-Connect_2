package com.copro.connect.service;

import com.copro.connect.dto.PagedResidentsResponse;
import com.copro.connect.dto.StatisticsResponse;
import com.copro.connect.exception.ResidentNotFoundException;
import com.copro.connect.model.Resident;
import com.copro.connect.repository.ResidentRepository;
import com.copro.connect.service.ResidentHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests ResidentService")
class ResidentServiceTest {

    @Mock
    private ResidentRepository residentRepository;

    @Mock
    private ResidentHistoryService residentHistoryService;

    @InjectMocks
    private ResidentService residentService;

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

    @Test
    @DisplayName("getAllResidents retourne la liste des résidents")
    void getAllResidents_shouldReturnList() {
        List<Resident> residents = List.of(resident);
        when(residentRepository.findAllByOrderByBatimentAscPorteAsc()).thenReturn(residents);

        List<Resident> result = residentService.getAllResidents();

        assertThat(result).hasSize(1).containsExactly(resident);
        verify(residentRepository).findAllByOrderByBatimentAscPorteAsc();
    }

    @Test
    @DisplayName("getResidentsPaginated sans filtres utilise findAll")
    void getResidentsPaginated_withoutFilters_usesFindAll() {
        Page<Resident> page = new PageImpl<>(List.of(resident), PageRequest.of(0, 10), 1);
        when(residentRepository.findAll(any(Pageable.class))).thenReturn(page);

        PagedResidentsResponse response = residentService.getResidentsPaginated(0, 10, null, null, null, null);

        assertThat(response.getResidents()).hasSize(1);
        assertThat(response.getCurrentPage()).isZero();
        assertThat(response.getTotalPages()).isOne();
        assertThat(response.getTotalElements()).isOne();
        assertThat(response.getPageSize()).isEqualTo(10);
        verify(residentRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("getResidentsPaginated avec filtres utilise findWithFilters")
    void getResidentsPaginated_withFilters_usesFindWithFilters() {
        Page<Resident> page = new PageImpl<>(List.of(resident), PageRequest.of(0, 10), 1);
        when(residentRepository.findWithFilters(anyString(), any(), any(), any(Pageable.class))).thenReturn(page);

        PagedResidentsResponse response = residentService.getResidentsPaginated(0, 10, "Dupont", "A", "Propriétaire Résident", "lotId,asc");

        assertThat(response.getResidents()).hasSize(1);
        verify(residentRepository).findWithFilters(eq("Dupont"), eq("A"), eq("Propriétaire Résident"), any(Pageable.class));
    }

    @Test
    @DisplayName("getStatistics retourne les statistiques")
    void getStatistics_shouldReturnStatistics() {
        StatisticsResponse stats = new StatisticsResponse(
                10L, 2L, 15L, 5L,
                Map.of("Propriétaire Résident", 8L),
                Map.of("A", 5L, "B", 5L),
                7L, 3L, 1.5, Map.of("resident", 5L)
        );
        when(residentRepository.calculateStatistics()).thenReturn(stats);

        StatisticsResponse result = residentService.getStatistics();

        assertThat(result.getTotalLots()).isEqualTo(10L);
        assertThat(result.getTotalBatiments()).isEqualTo(2L);
        assertThat(result.getTotalOccupants()).isEqualTo(15L);
        verify(residentRepository).calculateStatistics();
    }

    @Test
    @DisplayName("getResidentById retourne le résident quand il existe")
    void getResidentById_whenExists_returnsResident() {
        when(residentRepository.findById("res-1")).thenReturn(Optional.of(resident));

        Resident result = residentService.getResidentById("res-1");

        assertThat(result).isEqualTo(resident);
        assertThat(result.getId()).isEqualTo("res-1");
        verify(residentRepository).findById("res-1");
    }

    @Test
    @DisplayName("getResidentById lance ResidentNotFoundException quand absent")
    void getResidentById_whenNotExists_throws() {
        when(residentRepository.findById("inconnu")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> residentService.getResidentById("inconnu"))
                .isInstanceOf(ResidentNotFoundException.class)
                .hasMessageContaining("inconnu");
        verify(residentRepository).findById("inconnu");
    }

    @Test
    @DisplayName("createResident enregistre et retourne le résident")
    void createResident_shouldSaveAndReturn() {
        Resident toCreate = new Resident();
        toCreate.setLotId("LOT-002");
        toCreate.setBatiment("B");
        toCreate.setEtage("2");
        toCreate.setPorte("201");
        toCreate.setProprietaireNom("Martin");
        when(residentRepository.save(any(Resident.class))).thenAnswer(inv -> {
            Resident r = inv.getArgument(0);
            r.setId("res-2");
            return r;
        });

        Resident result = residentService.createResident(toCreate);

        assertThat(result.getId()).isEqualTo("res-2");
        assertThat(result.getLotId()).isEqualTo("LOT-002");
        verify(residentRepository).save(any(Resident.class));
    }

    @Test
    @DisplayName("updateResident met à jour et sauvegarde")
    void updateResident_shouldUpdateAndSave() {
        Resident details = new Resident();
        details.setLotId("LOT-001-bis");
        details.setBatiment("A");
        details.setEtage("1");
        details.setPorte("102");
        details.setProprietaireNom("Dupont Modifié");
        details.setStatutLot("Propriétaire Résident");

        when(residentRepository.findById("res-1")).thenReturn(Optional.of(resident));
        when(residentRepository.save(any(Resident.class))).thenAnswer(inv -> inv.getArgument(0));

        Resident result = residentService.updateResident("res-1", details);

        assertThat(result.getPorte()).isEqualTo("102");
        assertThat(result.getProprietaireNom()).isEqualTo("Dupont Modifié");
        verify(residentRepository).findById("res-1");
        verify(residentRepository).save(resident);
    }

    @Test
    @DisplayName("updateResident lance ResidentNotFoundException si id inconnu")
    void updateResident_whenNotFound_throws() {
        when(residentRepository.findById("inconnu")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> residentService.updateResident("inconnu", resident))
                .isInstanceOf(ResidentNotFoundException.class);
        verify(residentRepository).findById("inconnu");
        verify(residentRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteResident supprime le résident")
    void deleteResident_shouldDelete() {
        when(residentRepository.findById("res-1")).thenReturn(Optional.of(resident));
        doNothing().when(residentRepository).delete(resident);

        residentService.deleteResident("res-1");

        verify(residentRepository).findById("res-1");
        verify(residentRepository).delete(resident);
    }

    @Test
    @DisplayName("deleteResident lance exception si résident absent")
    void deleteResident_whenNotFound_throws() {
        when(residentRepository.findById("inconnu")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> residentService.deleteResident("inconnu"))
                .isInstanceOf(ResidentNotFoundException.class);
        verify(residentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("getResidentsByBatiment retourne la liste par bâtiment")
    void getResidentsByBatiment_shouldReturnList() {
        when(residentRepository.findByBatiment("A")).thenReturn(List.of(resident));

        List<Resident> result = residentService.getResidentsByBatiment("A");

        assertThat(result).hasSize(1).containsExactly(resident);
        verify(residentRepository).findByBatiment("A");
    }

    @Test
    @DisplayName("getResidentsByStatus retourne la liste par statut")
    void getResidentsByStatus_shouldReturnList() {
        when(residentRepository.findByStatutLot("Propriétaire Résident")).thenReturn(List.of(resident));

        List<Resident> result = residentService.getResidentsByStatus("Propriétaire Résident");

        assertThat(result).hasSize(1).containsExactly(resident);
        verify(residentRepository).findByStatutLot("Propriétaire Résident");
    }

    @Test
    @DisplayName("createResident normalise les données avec occupants et happix")
    void createResident_normalizesOccupantsAndHappix() {
        Resident toCreate = new Resident();
        toCreate.setLotId("  LOT-003  ");
        toCreate.setBatiment("  B  ");
        toCreate.setEtage("  2  ");
        toCreate.setPorte("  201  ");
        toCreate.setProprietaireNom("  Martin  ");
        toCreate.setProprietaireEmail("  MARTIN@TEST.COM  ");
        toCreate.setProprietaireMobile("  0600000000  ");

        com.copro.connect.model.Occupant occ = new com.copro.connect.model.Occupant("  Jean  ", "  0700000000  ", "  JEAN@TEST.COM  ");
        toCreate.setOccupants(List.of(occ));

        com.copro.connect.model.HappixAccount h = new com.copro.connect.model.HappixAccount("  Alice  ", "  0800000000  ", "  ALICE@TEST.COM  ", null, null, null);
        toCreate.setHappixAccounts(List.of(h));

        when(residentRepository.save(any(Resident.class))).thenAnswer(inv -> {
            Resident r = inv.getArgument(0);
            r.setId("res-3");
            return r;
        });

        Resident result = residentService.createResident(toCreate);

        assertThat(result.getLotId()).isEqualTo("LOT-003");
        assertThat(result.getBatiment()).isEqualTo("B");
        assertThat(result.getProprietaireEmail()).isEqualTo("martin@test.com");
        assertThat(result.getOccupants().get(0).getNom()).isEqualTo("Jean");
        assertThat(result.getOccupants().get(0).getEmail()).isEqualTo("jean@test.com");
        assertThat(result.getHappixAccounts().get(0).getNom()).isEqualTo("Alice");
        assertThat(result.getHappixAccounts().get(0).getEmail()).isEqualTo("alice@test.com");
    }

    @Test
    @DisplayName("createResident avec ID vide le remet à null")
    void createResident_emptyId_setsNull() {
        Resident toCreate = new Resident();
        toCreate.setId("");
        toCreate.setLotId("LOT-004");
        toCreate.setBatiment("A");
        toCreate.setEtage("1");
        toCreate.setPorte("101");
        toCreate.setProprietaireNom("Test");

        when(residentRepository.save(any(Resident.class))).thenAnswer(inv -> inv.getArgument(0));

        Resident result = residentService.createResident(toCreate);

        assertThat(result.getId()).isNull();
    }

    @Test
    @DisplayName("getResidentsPaginated avec sort desc")
    void getResidentsPaginated_withDescSort() {
        Page<Resident> page = new PageImpl<>(List.of(resident), PageRequest.of(0, 10), 1);
        when(residentRepository.findAll(any(Pageable.class))).thenReturn(page);

        PagedResidentsResponse response = residentService.getResidentsPaginated(0, 10, null, null, null, "lotId,desc");

        assertThat(response.getResidents()).hasSize(1);
    }

    @Test
    @DisplayName("getResidentsPaginated avec sort sans direction")
    void getResidentsPaginated_withSortNoDirection() {
        Page<Resident> page = new PageImpl<>(List.of(resident), PageRequest.of(0, 10), 1);
        when(residentRepository.findAll(any(Pageable.class))).thenReturn(page);

        PagedResidentsResponse response = residentService.getResidentsPaginated(0, 10, null, null, null, "lotId");

        assertThat(response.getResidents()).hasSize(1);
    }

    @Test
    @DisplayName("getResidentsPaginated avec filtre batiment=Tous ne filtre pas")
    void getResidentsPaginated_batimentTous_noFilter() {
        Page<Resident> page = new PageImpl<>(List.of(resident), PageRequest.of(0, 10), 1);
        when(residentRepository.findAll(any(Pageable.class))).thenReturn(page);

        PagedResidentsResponse response = residentService.getResidentsPaginated(0, 10, null, "Tous", "Tous", null);

        assertThat(response.getResidents()).hasSize(1);
        verify(residentRepository).findAll(any(Pageable.class));
    }
}
