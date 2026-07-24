package com.strata.presence.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represente un enregistrement de presence (arrivee / depart) pour une personne
 * (visiteur ou personnel) a une date donnee.
 *
 * IMPORTANT : dateJour, heureArrivee et heureDepart sont, par defaut, toujours
 * generes par le systeme (LocalDate.now() / LocalTime.now()) au niveau du service.
 * Ils ne sont modifiables manuellement que via la procedure de derogation
 * (voir AttendanceService.saisieManuelleArrivee), qui exige une autorisation
 * et journalise systematiquement l'operation dans audit_log.
 */
public class AttendanceRecord {
    private int id;
    private int personneId;
    private String personneNomComplet; // pour affichage direct dans les tables
    private Integer motifId;
    private LocalDate dateJour;
    private LocalTime heureArrivee;
    private LocalTime heureDepart;
    private String statut; // PRESENT / PARTI
    private boolean saisieManuelle;
    private int enregistrePar;
    private Integer siteId;
    private LocalDateTime createdAt;

    // Champs enrichis (renseignes uniquement par les requetes "detaillees" du registre du jour) :
    // profil et telephone de la personne, nom du service, chemins photo/signature.
    private String profil;
    private String telephone;
    private String serviceNom;
    private String photoPath;
    private String signaturePath;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPersonneId() { return personneId; }
    public void setPersonneId(int personneId) { this.personneId = personneId; }

    public String getPersonneNomComplet() { return personneNomComplet; }
    public void setPersonneNomComplet(String personneNomComplet) { this.personneNomComplet = personneNomComplet; }

    public Integer getMotifId() { return motifId; }
    public void setMotifId(Integer motifId) { this.motifId = motifId; }

    public LocalDate getDateJour() { return dateJour; }
    public void setDateJour(LocalDate dateJour) { this.dateJour = dateJour; }

    public LocalTime getHeureArrivee() { return heureArrivee; }
    public void setHeureArrivee(LocalTime heureArrivee) { this.heureArrivee = heureArrivee; }

    public LocalTime getHeureDepart() { return heureDepart; }
    public void setHeureDepart(LocalTime heureDepart) { this.heureDepart = heureDepart; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public boolean isSaisieManuelle() { return saisieManuelle; }
    public void setSaisieManuelle(boolean saisieManuelle) { this.saisieManuelle = saisieManuelle; }

    public int getEnregistrePar() { return enregistrePar; }
    public void setEnregistrePar(int enregistrePar) { this.enregistrePar = enregistrePar; }

    public Integer getSiteId() { return siteId; }
    public void setSiteId(Integer siteId) { this.siteId = siteId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getProfil() { return profil; }
    public void setProfil(String profil) { this.profil = profil; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getServiceNom() { return serviceNom; }
    public void setServiceNom(String serviceNom) { this.serviceNom = serviceNom; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public String getSignaturePath() { return signaturePath; }
    public void setSignaturePath(String signaturePath) { this.signaturePath = signaturePath; }
}
