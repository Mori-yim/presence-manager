package com.strata.presence.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Regroupe les donnees necessaires a la generation d'un rapport
 * (quelle que soit la periode) et ses statistiques agregees.
 */
public class ReportData {
    private final ReportPeriod period;
    private final LocalDate dateDebut;
    private final LocalDate dateFin;
    private final List<AttendanceRecord> records;

    public ReportData(ReportPeriod period, LocalDate dateDebut, LocalDate dateFin, List<AttendanceRecord> records) {
        this.period = period;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.records = records;
    }

    public ReportPeriod getPeriod() { return period; }
    public LocalDate getDateDebut() { return dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public List<AttendanceRecord> getRecords() { return records; }

    public int getTotal() {
        return records.size();
    }

    public long getTotalPresents() {
        return records.stream().filter(r -> "PRESENT".equals(r.getStatut())).count();
    }

    public long getTotalPartis() {
        return records.stream().filter(r -> "PARTI".equals(r.getStatut())).count();
    }

    public long getTotalDerogations() {
        return records.stream().filter(AttendanceRecord::isSaisieManuelle).count();
    }

    public String getTitre() {
        return period.getLibelle() + " - Strata Agency (" + dateDebut + " au " + dateFin + ")";
    }
}
