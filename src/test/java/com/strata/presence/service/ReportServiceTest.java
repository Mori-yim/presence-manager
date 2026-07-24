package com.strata.presence.service;

import com.strata.presence.model.ReportData;
import com.strata.presence.model.ReportPeriod;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifie que les bornes de dates calculees pour chaque type de rapport
 * (journalier, hebdomadaire, mensuel, annuel) sont correctes, independamment
 * des donnees reellement presentes en base.
 */
class ReportServiceTest {

    private final ReportService reportService = new ReportService();

    @Test
    void rapportJournalierCouvreUneSeuleJournee() {
        LocalDate reference = LocalDate.of(2026, 3, 15);
        ReportData data = reportService.genererRapport(ReportPeriod.JOURNALIER, reference);
        assertEquals(reference, data.getDateDebut());
        assertEquals(reference, data.getDateFin());
    }

    @Test
    void rapportHebdomadaireCouvreDuLundiAuDimanche() {
        // Le 18 mars 2026 est un mercredi
        LocalDate reference = LocalDate.of(2026, 3, 18);
        ReportData data = reportService.genererRapport(ReportPeriod.HEBDOMADAIRE, reference);
        assertEquals(LocalDate.of(2026, 3, 16), data.getDateDebut(), "Le debut doit etre le lundi de la semaine");
        assertEquals(LocalDate.of(2026, 3, 22), data.getDateFin(), "La fin doit etre le dimanche de la semaine");
    }

    @Test
    void rapportMensuelCouvreDuPremierAuDernierJourDuMois() {
        LocalDate reference = LocalDate.of(2026, 2, 10);
        ReportData data = reportService.genererRapport(ReportPeriod.MENSUEL, reference);
        assertEquals(LocalDate.of(2026, 2, 1), data.getDateDebut());
        assertEquals(LocalDate.of(2026, 2, 28), data.getDateFin(), "Fevrier 2026 n'est pas bissextile");
    }

    @Test
    void rapportAnnuelCouvreTouteLannee() {
        LocalDate reference = LocalDate.of(2026, 7, 1);
        ReportData data = reportService.genererRapport(ReportPeriod.ANNUEL, reference);
        assertEquals(LocalDate.of(2026, 1, 1), data.getDateDebut());
        assertEquals(LocalDate.of(2026, 12, 31), data.getDateFin());
    }
}
