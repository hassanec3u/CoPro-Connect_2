package com.copro.connect.controller;

import com.copro.connect.dto.PagedResidentsResponse;
import com.copro.connect.dto.ResidentsResponse;
import com.copro.connect.dto.StatisticsResponse;
import com.copro.connect.exception.ResidentNotFoundException;
import com.copro.connect.model.Resident;
import com.copro.connect.service.ResidentService;
import com.copro.connect.validator.ResidentValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests ResidentController")
class ResidentControllerTest {

    @Mock
    private ResidentService residentService;

    @Mock
    private ResidentValidator residentValidator;

    @InjectMocks
    private ResidentController residentController;

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
    @DisplayName("getAllResidents retourne page paginée 200")
    void getAllResidents_returnsPagedResponse() {
        PagedResidentsResponse response = new PagedResidentsResponse(
                List.of(resident), 0, 1, 1L, 10
        );
        when(residentService.getResidentsPaginated(0, 10, null, null, null, null)).thenReturn(response);

        ResponseEntity<PagedResidentsResponse> result = residentController.getAllResidents(0, 10, null, null, null, null);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getResidents()).hasSize(1);
        assertThat(result.getBody().getCurrentPage()).isZero();
        assertThat(result.getBody().getTotalPages()).isOne();
        verify(residentService).getResidentsPaginated(0, 10, null, null, null, null);
    }

    @Test
    @DisplayName("getAllResidentsNoPagination retourne tous les résidents")
    void getAllResidentsNoPagination_returnsList() {
        when(residentService.getAllResidents()).thenReturn(List.of(resident));

        ResponseEntity<ResidentsResponse> result = residentController.getAllResidentsNoPagination();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).hasSize(1).first().extracting(Resident::getLotId).isEqualTo("LOT-001");
        verify(residentService).getAllResidents();
    }

    @Test
    @DisplayName("getStatistics retourne les stats")
    void getStatistics_returnsStats() {
        StatisticsResponse stats = new StatisticsResponse(
                10L, 2L, 15L, 5L,
                Map.of(), Map.of(), 7L, 3L, 1.5, Map.of()
        );
        when(residentService.getStatistics()).thenReturn(stats);

        ResponseEntity<StatisticsResponse> result = residentController.getStatistics();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getTotalLots()).isEqualTo(10L);
        assertThat(result.getBody().getTotalBatiments()).isEqualTo(2L);
        verify(residentService).getStatistics();
    }

    @Test
    @DisplayName("getResidentById retourne le résident")
    void getResidentById_returnsResident() {
        doNothing().when(residentValidator).validateId("res-1");
        when(residentService.getResidentById("res-1")).thenReturn(resident);

        ResponseEntity<Resident> result = residentController.getResidentById("res-1");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getId()).isEqualTo("res-1");
        assertThat(result.getBody().getLotId()).isEqualTo("LOT-001");
        verify(residentValidator).validateId("res-1");
        verify(residentService).getResidentById("res-1");
    }

    @Test
    @DisplayName("getResidentById propage ResidentNotFoundException")
    void getResidentById_whenNotFound_throws() {
        doNothing().when(residentValidator).validateId("inconnu");
        when(residentService.getResidentById("inconnu")).thenThrow(new ResidentNotFoundException("inconnu"));

        assertThatThrownBy(() -> residentController.getResidentById("inconnu"))
                .isInstanceOf(ResidentNotFoundException.class)
                .hasMessageContaining("inconnu");
        verify(residentValidator).validateId("inconnu");
        verify(residentService).getResidentById("inconnu");
    }

    @Test
    @DisplayName("createResident retourne 201")
    void createResident_returns201() {
        doNothing().when(residentValidator).validateForCreation(any(Resident.class));
        when(residentService.createResident(any(Resident.class))).thenReturn(resident);

        ResponseEntity<Resident> result = residentController.createResident(resident);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getLotId()).isEqualTo("LOT-001");
        verify(residentValidator).validateForCreation(any(Resident.class));
        verify(residentService).createResident(any(Resident.class));
    }

    @Test
    @DisplayName("updateResident retourne 200")
    void updateResident_returns200() {
        doNothing().when(residentValidator).validateForUpdate(eq("res-1"), any(Resident.class));
        when(residentService.updateResident(eq("res-1"), any(Resident.class))).thenReturn(resident);

        ResponseEntity<Resident> result = residentController.updateResident("res-1", resident);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getId()).isEqualTo("res-1");
        verify(residentValidator).validateForUpdate(eq("res-1"), any(Resident.class));
        verify(residentService).updateResident(eq("res-1"), any(Resident.class));
    }

    @Test
    @DisplayName("deleteResident retourne 200 et message")
    void deleteResident_returns200() {
        doNothing().when(residentValidator).validateId("res-1");
        doNothing().when(residentService).deleteResident("res-1");

        ResponseEntity<Map<String, String>> result = residentController.deleteResident("res-1");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).containsEntry("message", "Résident supprimé avec succès");
        verify(residentValidator).validateId("res-1");
        verify(residentService).deleteResident("res-1");
    }
}
