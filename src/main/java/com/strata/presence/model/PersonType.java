package com.strata.presence.model;

public enum PersonType {
    VISITEUR("Visiteur"),
    PERSONNEL("Employe"),
    STAGIAIRE("Stagiaire");

    private final String libelle;

    PersonType(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
