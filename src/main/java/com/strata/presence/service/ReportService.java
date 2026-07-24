package com.strata.presence.service;

import com.strata.presence.model.ReportData;
import com.strata.presence.model.ReportPeriod;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * Construit les donnees de rapport pour une periode donnee, a partir d'une
 * date de reference. S'appuie sur AttendanceService pour la recuperation des
 * presences (deja soumises aux memes regles d'horodatage systeme).
 */
public class ReportService {

    private final AttendanceService attendanceService = new AttendanceService();

    public ReportData genererRapport(ReportPeriod period, LocalDate reference) {
        LocalDate debut;
        LocalDate fin;

        switch (period) {
            case JOURNALIER -> {
                debut = reference;
                fin = reference;
            }
            case HEBDOMADAIRE -> {
                debut = reference.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                fin = reference.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            }
            case MENSUEL -> {
                debut = reference.with(TemporalAdjusters.firstDayOfMonth());
                fin = reference.with(TemporalAdjusters.lastDayOfMonth());
            }
            case ANNUEL -> {
                debut = reference.with(TemporalAdjusters.firstDayOfYear());
                fin = reference.with(TemporalAdjusters.lastDayOfYear());
            }
            default -> throw new IllegalArgumentException("Periode inconnue : " + period);
        }

        return new ReportData(period, debut, fin, attendanceService.presencesEntre(debut, fin));
    }
}
