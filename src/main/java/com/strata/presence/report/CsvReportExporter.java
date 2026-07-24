package com.strata.presence.report;

import com.opencsv.CSVWriter;
import com.strata.presence.model.AttendanceRecord;
import com.strata.presence.model.ReportData;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Genere un rapport CSV a partir d'un ReportData, avec OpenCSV.
 * Format simple, ideal pour reimport dans Excel/LibreOffice/outils tiers.
 */
public class CsvReportExporter {

    public void export(ReportData data, String destinationPath) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(destinationPath))) {
            writer.writeNext(new String[]{data.getTitre()});
            writer.writeNext(new String[]{
                    "Total", String.valueOf(data.getTotal()),
                    "Presents", String.valueOf(data.getTotalPresents()),
                    "Partis", String.valueOf(data.getTotalPartis()),
                    "Derogations", String.valueOf(data.getTotalDerogations())
            });
            writer.writeNext(new String[]{});

            writer.writeNext(new String[]{"Personne", "Date", "Heure arrivee", "Heure depart", "Statut", "Saisie manuelle"});

            List<String[]> rows = new ArrayList<>();
            for (AttendanceRecord r : data.getRecords()) {
                rows.add(new String[]{
                        r.getPersonneNomComplet(),
                        r.getDateJour().toString(),
                        r.getHeureArrivee().withNano(0).toString(),
                        r.getHeureDepart() != null ? r.getHeureDepart().withNano(0).toString() : "-",
                        r.getStatut(),
                        r.isSaisieManuelle() ? "Oui" : "Non"
                });
            }
            writer.writeAll(rows);
        }
    }
}
