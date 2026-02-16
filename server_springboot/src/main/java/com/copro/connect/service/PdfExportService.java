package com.copro.connect.service;

import com.copro.connect.model.HappixAccount;
import com.copro.connect.model.Occupant;
import com.copro.connect.model.Resident;
import com.copro.connect.repository.ResidentRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfExportService {

    private final ResidentRepository residentRepository;

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(31, 41, 55));
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(107, 114, 128));
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);
    private static final Font CELL_FONT = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, new Color(55, 65, 81));
    private static final Font CELL_BOLD_FONT = new Font(Font.HELVETICA, 7.5f, Font.BOLD, new Color(55, 65, 81));
    private static final Color HEADER_BG = new Color(37, 99, 235);
    private static final Color STRIPE_BG = new Color(249, 250, 251);

    /**
     * Exporte tous les résidents en PDF
     */
    public byte[] exportResidentsPdf() {
        log.info("Generating residents PDF export");
        List<Resident> residents = residentRepository.findAllByOrderByBatimentAscPorteAsc();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 30, 20);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Titre
            Paragraph title = new Paragraph("Liste des résidents", TITLE_FONT);
            title.setSpacingAfter(4);
            document.add(title);

            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Paragraph subtitle = new Paragraph("Généré le " + dateStr + " — " + residents.size() + " résidents", SUBTITLE_FONT);
            subtitle.setSpacingAfter(12);
            document.add(subtitle);

            // Table
            float[] widths = {5f, 5f, 5f, 4f, 4f, 10f, 14f, 10f, 14f, 29f};
            PdfPTable table = new PdfPTable(widths);
            table.setWidthPercentage(100);
            table.setSpacingBefore(4);

            String[] headers = {"Lot", "Bât", "Appt", "Étage", "Cave", "Statut", "Propriétaire", "Mobile", "Email", "Occupants"};
            for (String h : headers) {
                table.addCell(headerCell(h));
            }

            for (int i = 0; i < residents.size(); i++) {
                Resident r = residents.get(i);
                boolean stripe = i % 2 == 1;

                table.addCell(dataCell(r.getLotId(), stripe, false));
                table.addCell(dataCell(r.getBatiment(), stripe, false));
                table.addCell(dataCell(r.getPorte(), stripe, false));
                table.addCell(dataCell(r.getEtage(), stripe, false));
                table.addCell(dataCell(r.getCaveId(), stripe, false));
                table.addCell(dataCell(r.getStatutLot(), stripe, false));
                table.addCell(dataCell(r.getProprietaireNom(), stripe, true));
                table.addCell(dataCell(r.getProprietaireMobile(), stripe, false));
                table.addCell(dataCell(r.getProprietaireEmail(), stripe, false));
                table.addCell(dataCell(formatOccupants(r.getOccupants()), stripe, false));
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            log.error("Error generating residents PDF", e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }

        return out.toByteArray();
    }

    /**
     * Exporte tous les comptes Happix en PDF
     */
    public byte[] exportHappixPdf() {
        log.info("Generating Happix PDF export");
        List<Resident> residents = residentRepository.findAllByOrderByBatimentAscPorteAsc();

        // Extraire tous les comptes Happix
        List<HappixEntry> entries = residents.stream()
                .flatMap(r -> {
                    List<HappixAccount> accounts = r.getHappixAccounts() != null ? r.getHappixAccounts() : List.of();
                    return accounts.stream().map(h -> new HappixEntry(
                            h.getNom(),
                            h.getEmail(),
                            h.getMobile() != null ? h.getMobile() : r.getProprietaireMobile(),
                            h.getNomBorne(),
                            h.getType(),
                            h.getRelation(),
                            r.getBatiment(),
                            r.getPorte(),
                            formatResidents(r)
                    ));
                })
                .collect(Collectors.toList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 30, 20);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("Liste des comptes Happix", TITLE_FONT);
            title.setSpacingAfter(4);
            document.add(title);

            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Paragraph subtitle = new Paragraph("Généré le " + dateStr + " — " + entries.size() + " comptes", SUBTITLE_FONT);
            subtitle.setSpacingAfter(12);
            document.add(subtitle);

            float[] widths = {14f, 16f, 10f, 10f, 8f, 8f, 5f, 5f, 24f};
            PdfPTable table = new PdfPTable(widths);
            table.setWidthPercentage(100);
            table.setSpacingBefore(4);

            String[] headers = {"Nom", "Email", "Numéro", "Nom borne", "Type", "Relation", "Bât", "Appt", "Résidents"};
            for (String h : headers) {
                table.addCell(headerCell(h));
            }

            for (int i = 0; i < entries.size(); i++) {
                HappixEntry e = entries.get(i);
                boolean stripe = i % 2 == 1;

                table.addCell(dataCell(e.nom, stripe, true));
                table.addCell(dataCell(e.email, stripe, false));
                table.addCell(dataCell(e.mobile, stripe, false));
                table.addCell(dataCell(e.nomBorne, stripe, false));
                table.addCell(dataCell(e.type, stripe, false));
                table.addCell(dataCell(e.relation, stripe, false));
                table.addCell(dataCell(e.batiment, stripe, false));
                table.addCell(dataCell(e.appart, stripe, false));
                table.addCell(dataCell(e.residents, stripe, false));
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            log.error("Error generating Happix PDF", e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }

        return out.toByteArray();
    }

    // ==================== UTILITAIRES ====================

    private PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(HEADER_BG);
        cell.setPadding(6);
        cell.setBorderWidth(0);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private PdfPCell dataCell(String text, boolean stripe, boolean bold) {
        String value = text != null && !text.isBlank() ? text : "-";
        PdfPCell cell = new PdfPCell(new Phrase(value, bold ? CELL_BOLD_FONT : CELL_FONT));
        cell.setPadding(5);
        cell.setBorderWidth(0);
        cell.setBorderWidthBottom(0.5f);
        cell.setBorderColor(new Color(229, 231, 235));
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        if (stripe) {
            cell.setBackgroundColor(STRIPE_BG);
        }
        return cell;
    }

    private String formatOccupants(List<Occupant> occupants) {
        if (occupants == null || occupants.isEmpty()) return "-";
        return occupants.stream()
                .map(o -> {
                    String line = o.getNom();
                    if (o.getMobile() != null && !o.getMobile().isBlank()) line += " — " + o.getMobile();
                    if (o.getEmail() != null && !o.getEmail().isBlank()) line += " — " + o.getEmail();
                    return line;
                })
                .collect(Collectors.joining("\n"));
    }

    private String formatResidents(Resident r) {
        return Stream.concat(
                Stream.ofNullable(r.getProprietaireNom()),
                r.getOccupants() != null ? r.getOccupants().stream().map(Occupant::getNom) : Stream.empty()
        ).filter(n -> n != null && !n.isBlank()).distinct().collect(Collectors.joining(", "));
    }

    private record HappixEntry(String nom, String email, String mobile, String nomBorne,
                                String type, String relation, String batiment, String appart, String residents) {}
}
