package com.copro.connect.controller;

import com.copro.connect.service.PdfExportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests ExportController")
class ExportControllerTest {

    @Mock
    private PdfExportService pdfExportService;

    @InjectMocks
    private ExportController exportController;

    @Test
    @DisplayName("exportResidentsPdf retourne un PDF avec les bons headers")
    void exportResidentsPdf_returnsCorrectResponse() {
        byte[] pdfBytes = new byte[]{1, 2, 3, 4, 5};
        when(pdfExportService.exportResidentsPdf()).thenReturn(pdfBytes);

        ResponseEntity<byte[]> response = exportController.exportResidentsPdf();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(pdfBytes);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=residents-list.pdf");
        assertThat(response.getHeaders().getContentLength()).isEqualTo(5);
        verify(pdfExportService).exportResidentsPdf();
    }

    @Test
    @DisplayName("exportHappixPdf retourne un PDF avec les bons headers")
    void exportHappixPdf_returnsCorrectResponse() {
        byte[] pdfBytes = new byte[]{10, 20, 30};
        when(pdfExportService.exportHappixPdf()).thenReturn(pdfBytes);

        ResponseEntity<byte[]> response = exportController.exportHappixPdf();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(pdfBytes);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=happix-list.pdf");
        assertThat(response.getHeaders().getContentLength()).isEqualTo(3);
        verify(pdfExportService).exportHappixPdf();
    }
}
