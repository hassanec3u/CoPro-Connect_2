package com.copro.connect.controller;

import com.copro.connect.service.PdfExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final PdfExportService pdfExportService;

    @GetMapping("/residents/pdf")
    public ResponseEntity<byte[]> exportResidentsPdf() {
        log.info("Export PDF résidents demandé");
        byte[] pdf = pdfExportService.exportResidentsPdf();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=residents-list.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }

    @GetMapping("/happix/pdf")
    public ResponseEntity<byte[]> exportHappixPdf() {
        log.info("Export PDF Happix demandé");
        byte[] pdf = pdfExportService.exportHappixPdf();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=happix-list.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}
