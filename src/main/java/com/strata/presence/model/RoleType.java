package com.strata.presence.model;

/**
 * Roles applicatifs disponibles dans le logiciel.
 * ADMIN         : acces total (utilisateurs, parametres, derogations horaires)
 * AGENT         : saisie des presences a l'accueil
 * RESPONSABLE   : consultation, rapports, statistiques
 */
public enum RoleType {
    ADMIN("Administrateur"),
    AGENT("Agent d'accueil"),
    RESPONSABLE("Responsable");

    private final String libelle;

    RoleType(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
