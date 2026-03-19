package com.copro.connect.controller;

import com.copro.connect.model.ChangeDetail;
import com.copro.connect.model.ResidentHistory;
import com.copro.connect.service.ResidentHistoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests ResidentHistoryController")
class ResidentHistoryControllerTest {

    @Mock
    private ResidentHistoryService residentHistoryService;

    @InjectMocks
    private ResidentHistoryController residentHistoryController;

    @Test
    @DisplayName("getApartmentHistory retourne la liste des historiques")
    void getApartmentHistory_returnsHistoryList() {
        ResidentHistory history = buildHistory("res-1", "LOT-001", "UPDATE");
        when(residentHistoryService.getApartmentHistory("A", "1", "101"))
                .thenReturn(List.of(history));

        ResponseEntity<?> response = residentHistoryController.getApartmentHistory("A", "1", "101");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(residentHistoryService).getApartmentHistory("A", "1", "101");
    }

    @Test
    @DisplayName("getApartmentHistory retourne liste vide si aucun historique")
    void getApartmentHistory_emptyList() {
        when(residentHistoryService.getApartmentHistory("B", "2", "202"))
                .thenReturn(List.of());

        ResponseEntity<?> response = residentHistoryController.getApartmentHistory("B", "2", "202");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getResidentHistory retourne la liste des historiques du résident")
    void getResidentHistory_returnsHistoryList() {
        ResidentHistory history = buildHistory("res-1", "LOT-001", "DELETE");
        when(residentHistoryService.getResidentHistory("res-1"))
                .thenReturn(List.of(history));

        ResponseEntity<?> response = residentHistoryController.getResidentHistory("res-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(residentHistoryService).getResidentHistory("res-1");
    }

    @Test
    @DisplayName("getResidentHistory retourne liste vide si aucun historique")
    void getResidentHistory_emptyList() {
        when(residentHistoryService.getResidentHistory("res-unknown"))
                .thenReturn(List.of());

        ResponseEntity<?> response = residentHistoryController.getResidentHistory("res-unknown");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private ResidentHistory buildHistory(String residentId, String lotId, String actionType) {
        ResidentHistory h = new ResidentHistory();
        h.setId("hist-1");
        h.setResidentId(residentId);
        h.setLotId(lotId);
        h.setBatiment("A");
        h.setEtage("1");
        h.setPorte("101");
        h.setActionType(actionType);
        h.setDescription("Test description");
        h.setChangedAt(Instant.now());
        h.setApartmentKey("A-1-101");
        ChangeDetail cd = new ChangeDetail(ChangeDetail.CATEGORY_LOT, ChangeDetail.CHANGE_TYPE_MODIFIED,
                "Statut", "old", "new");
        h.setChanges(List.of(cd));
        return h;
    }
}
