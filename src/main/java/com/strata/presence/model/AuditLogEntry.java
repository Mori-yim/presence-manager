package com.strata.presence.model;

import java.time.LocalDateTime;

/**
 * Trace immuable d'une modification manuelle d'un champ normalement automatique
 * (typiquement l'heure d'arrivee, en cas de derogation "arrivee avant ouverture").
 */
public class AuditLogEntry {
    private int id;
    private int presenceId;
    private int userId;
    private String userFullName;
    private String champModifie;
    private String ancienneValeur;
    private String nouvelleValeur;
    private String justification;
    private LocalDateTime dateAction;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPresenceId() { return presenceId; }
    public void setPresenceId(int presenceId) { this.presenceId = presenceId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

    public String getChampModifie() { return champModifie; }
    public void setChampModifie(String champModifie) { this.champModifie = champModifie; }

    public String getAncienneValeur() { return ancienneValeur; }
    public void setAncienneValeur(String ancienneValeur) { this.ancienneValeur = ancienneValeur; }

    public String getNouvelleValeur() { return nouvelleValeur; }
    public void setNouvelleValeur(String nouvelleValeur) { this.nouvelleValeur = nouvelleValeur; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public LocalDateTime getDateAction() { return dateAction; }
    public void setDateAction(LocalDateTime dateAction) { this.dateAction = dateAction; }
}
