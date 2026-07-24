package com.strata.presence.model;

public enum ReportPeriod {
    JOURNALIER("Rapport journalier"),
    HEBDOMADAIRE("Rapport hebdomadaire"),
    MENSUEL("Rapport mensuel"),
    ANNUEL("Rapport annuel");

    private final String libelle;

    ReportPeriod(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
