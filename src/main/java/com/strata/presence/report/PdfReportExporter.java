package com.strata.presence.report;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.strata.presence.model.AttendanceRecord;
import com.strata.presence.model.ReportData;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Genere un rapport PDF professionnel a partir d'un ReportData, avec OpenPDF.
 */
public class PdfReportExporter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void export(ReportData data, String destinationPath) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4, 36, 36, 54, 54);
        try (FileOutputStream fos = new FileOutputStream(destinationPath)) {
            PdfWriter.getInstance(document, fos);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(230, 126, 34));
            Font subtitleFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font cellFont = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);

            Paragraph title = new Paragraph("STRATA AGENCY", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph(data.getTitre(), subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(16);
            document.add(subtitle);

            // Bloc de statistiques
            PdfPTable statsTable = new PdfPTable(4);
            statsTable.setWidthPercentage(100);
            statsTable.setSpacingAfter(16);
            addStatCell(statsTable, "Total enregistrements", String.valueOf(data.getTotal()), headerFont, cellFont);
            addStatCell(statsTable, "Presents", String.valueOf(data.getTotalPresents()), headerFont, cellFont);
            addStatCell(statsTable, "Partis", String.valueOf(data.getTotalPartis()), headerFont, cellFont);
            addStatCell(statsTable, "Derogations horaires", String.valueOf(data.getTotalDerogations()), headerFont, cellFont);
            document.add(statsTable);

            // Tableau detaille
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 2, 2, 2, 2, 2});

            Color headerBg = new Color(230, 126, 34);
            for (String col : new String[]{"Personne", "Date", "Arrivee", "Depart", "Statut", "Saisie manuelle"}) {
                PdfPCell cell = new PdfPCell(new Phrase(col, headerFont));
                cell.setBackgroundColor(headerBg);
                cell.setPadding(6);
                table.addCell(cell);
            }

            boolean alt = false;
            for (AttendanceRecord r : data.getRecords()) {
                Color bg = alt ? new Color(245, 247, 250) : Color.WHITE;
                addCell(table, r.getPersonneNomComplet(), cellFont, bg);
                addCell(table, r.getDateJour().format(DATE_FMT), cellFont, bg);
                addCell(table, r.getHeureArrivee().withNano(0).toString(), cellFont, bg);
                addCell(table, r.getHeureDepart() != null ? r.getHeureDepart().withNano(0).toString() : "-", cellFont, bg);
                addCell(table, r.getStatut(), cellFont, bg);
                addCell(table, r.isSaisieManuelle() ? "Oui" : "Non", cellFont, bg);
                alt = !alt;
            }

            document.add(table);

            Paragraph footer = new Paragraph("Document genere automatiquement par Strata Presence Manager - "
                    + java.time.LocalDateTime.now().withNano(0), subtitleFont);
            footer.setSpacingBefore(16);
            document.add(footer);

        } finally {
            if (document.isOpen()) document.close();
        }
    }

    private void addStatCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(8);
        cell.setBorderColor(new Color(230, 230, 230));
        Paragraph p = new Paragraph();
        p.add(new Chunk(value + "\n", new Font(Font.HELVETICA, 16, Font.BOLD, new Color(230, 126, 34))));
        p.add(new Chunk(label, new Font(Font.HELVETICA, 8, Font.NORMAL, Color.GRAY)));
        cell.addElement(p);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBackgroundColor(bg);
        table.addCell(cell);
    }
}
