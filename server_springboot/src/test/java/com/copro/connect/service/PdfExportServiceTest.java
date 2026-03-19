package com.copro.connect.service;

import com.copro.connect.model.HappixAccount;
import com.copro.connect.model.Occupant;
import com.copro.connect.model.Resident;
import com.copro.connect.repository.ResidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests PdfExportService")
class PdfExportServiceTest {

    @Mock
    private ResidentRepository residentRepository;

    @InjectMocks
    private PdfExportService pdfExportService;

    private Resident resident;

    @BeforeEach
    void setUp() {
        resident = new Resident();
        resident.setId("res-1");
        resident.setLotId("LOT-001");
        resident.setBatiment("A");
        resident.setEtage("1");
        resident.setPorte("101");
        resident.setCaveId("CAVE-01");
        resident.setStatutLot("Propriétaire Résident");
        resident.setProprietaireNom("Dupont");
        resident.setProprietaireMobile("0600000000");
        resident.setProprietaireEmail("dupont@test.com");
    }

    // ==================== exportResidentsPdf ====================

    @Test
    @DisplayName("exportResidentsPdf liste vide génère un PDF non vide")
    void exportResidentsPdf_emptyList_returnsPdf() {
        when(residentRepository.findAllByOrderByBatimentAscPorteAsc()).thenReturn(List.of());

        byte[] pdf = pdfExportService.exportResidentsPdf();

        assertThat(pdf).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("exportResidentsPdf avec résidents génère un PDF")
    void exportResidentsPdf_withResidents_returnsPdf() {
        Occupant occ = new Occupant("Jean Martin", "0700000000", "jean@test.com");
        resident.setOccupants(List.of(occ));
        when(residentRepository.findAllByOrderByBatimentAscPorteAsc()).thenReturn(List.of(resident));

        byte[] pdf = pdfExportService.exportResidentsPdf();

        assertThat(pdf).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("exportResidentsPdf avec résidents alternés (stripe)")
    void exportResidentsPdf_evenOddRows_returnsPdf() {
        Resident r2 = new Resident();
        r2.setLotId("LOT-002");
        r2.setBatiment("A");
        r2.setEtage("2");
        r2.setPorte("201");
        r2.setProprietaireNom("Martin");
        when(residentRepository.findAllByOrderByBatimentAscPorteAsc()).thenReturn(List.of(resident, r2));

        byte[] pdf = pdfExportService.exportResidentsPdf();

        assertThat(pdf).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("exportResidentsPdf avec occupants sans email ni mobile")
    void exportResidentsPdf_occupantWithMinimalData_returnsPdf() {
        Occupant occ = new Occupant("Jean", null, null);
        resident.setOccupants(List.of(occ));
        when(residentRepository.findAllByOrderByBatimentAscPorteAsc()).thenReturn(List.of(resident));

        byte[] pdf = pdfExportService.exportResidentsPdf();

        assertThat(pdf).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("exportResidentsPdf avec champs null retourne un PDF")
    void exportResidentsPdf_nullFields_returnsPdf() {
        Resident r = new Resident();
        r.setLotId("LOT-003");
        r.setBatiment("B");
        r.setEtage("1");
        r.setPorte("102");
        r.setProprietaireNom("Test");
        when(residentRepository.findAllByOrderByBatimentAscPorteAsc()).thenReturn(List.of(r));

        byte[] pdf = pdfExportService.exportResidentsPdf();

        assertThat(pdf).isNotNull().isNotEmpty();
    }

    // ==================== exportHappixPdf ====================

    @Test
    @DisplayName("exportHappixPdf liste vide génère un PDF non vide")
    void exportHappixPdf_emptyList_returnsPdf() {
        when(residentRepository.findAllByOrderByBatimentAscPorteAsc()).thenReturn(List.of());

        byte[] pdf = pdfExportService.exportHappixPdf();

        assertThat(pdf).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("exportHappixPdf avec comptes Happix génère un PDF")
    void exportHappixPdf_withHappixAccounts_returnsPdf() {
        HappixAccount h = new HappixAccount("Alice", "0600000000", "alice@test.com", "borne-1", "resident", "propriétaire");
        resident.setHappixAccounts(List.of(h));
        Occupant occ = new Occupant("Jean", null, null);
        resident.setOccupants(List.of(occ));
        when(residentRepository.findAllByOrderByBatimentAscPorteAsc()).thenReturn(List.of(resident));

        byte[] pdf = pdfExportService.exportHappixPdf();

        assertThat(pdf).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("exportHappixPdf avec Happix sans mobile utilise mobile du propriétaire")
    void exportHappixPdf_happixWithoutMobile_usesPropMobile_returnsPdf() {
        HappixAccount h = new HappixAccount("Bob", null, "bob@test.com", null, null, null);
        resident.setHappixAccounts(List.of(h));
        when(residentRepository.findAllByOrderByBatimentAscPorteAsc()).thenReturn(List.of(resident));

        byte[] pdf = pdfExportService.exportHappixPdf();

        assertThat(pdf).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("exportHappixPdf avec résidents sans occupants ni happix")
    void exportHappixPdf_noHappixAccounts_returnsPdf() {
        resident.setHappixAccounts(null);
        when(residentRepository.findAllByOrderByBatimentAscPorteAsc()).thenReturn(List.of(resident));

        byte[] pdf = pdfExportService.exportHappixPdf();

        assertThat(pdf).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("exportHappixPdf avec plusieurs comptes (stripe)")
    void exportHappixPdf_multipleAccounts_stripe_returnsPdf() {
        HappixAccount h1 = new HappixAccount("Alice", "0600000000", null, null, null, null);
        HappixAccount h2 = new HappixAccount("Bob", "0700000000", null, null, null, null);
        resident.setHappixAccounts(List.of(h1, h2));
        when(residentRepository.findAllByOrderByBatimentAscPorteAsc()).thenReturn(List.of(resident));

        byte[] pdf = pdfExportService.exportHappixPdf();

        assertThat(pdf).isNotNull().isNotEmpty();
    }
}
