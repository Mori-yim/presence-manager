package com.strata.presence.report;

import com.strata.presence.model.AttendanceRecord;
import com.strata.presence.model.ReportData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Genere un rapport Excel (.xlsx) a partir d'un ReportData, avec Apache POI.
 */
public class ExcelReportExporter {

    public void export(ReportData data, String destinationPath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(data.getPeriod().name());

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int rowIdx = 0;
            Row titleRow = sheet.createRow(rowIdx++);
            titleRow.createCell(0).setCellValue(data.getTitre());
            titleRow.getCell(0).setCellStyle(titleStyle);
            rowIdx++; // ligne vide

            Row statsRow = sheet.createRow(rowIdx++);
            statsRow.createCell(0).setCellValue("Total : " + data.getTotal());
            statsRow.createCell(1).setCellValue("Presents : " + data.getTotalPresents());
            statsRow.createCell(2).setCellValue("Partis : " + data.getTotalPartis());
            statsRow.createCell(3).setCellValue("Derogations : " + data.getTotalDerogations());
            rowIdx++; // ligne vide

            String[] headers = {"Personne", "Date", "Heure arrivee", "Heure depart", "Statut", "Saisie manuelle"};
            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (AttendanceRecord r : data.getRecords()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getPersonneNomComplet());
                row.createCell(1).setCellValue(r.getDateJour().toString());
                row.createCell(2).setCellValue(r.getHeureArrivee().withNano(0).toString());
                row.createCell(3).setCellValue(r.getHeureDepart() != null ? r.getHeureDepart().withNano(0).toString() : "-");
                row.createCell(4).setCellValue(r.getStatut());
                row.createCell(5).setCellValue(r.isSaisieManuelle() ? "Oui" : "Non");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(destinationPath)) {
                workbook.write(fos);
            }
        }
    }
}
