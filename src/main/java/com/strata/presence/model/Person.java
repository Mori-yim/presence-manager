package com.strata.presence.model;

public class Person {
    private int id;
    private PersonType type;
    private String nom;
    private String prenom;
    private String pieceIdentite;
    private String telephone;
    private String entreprise;
    private Integer serviceId;
    private Integer siteId;
    private String photoPath;
    private String signaturePath;
    private String profil;
    private String raisonVisite;
    private String qrCode;

    public Person() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public PersonType getType() { return type; }
    public void setType(PersonType type) { this.type = type; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getPieceIdentite() { return pieceIdentite; }
    public void setPieceIdentite(String pieceIdentite) { this.pieceIdentite = pieceIdentite; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEntreprise() { return entreprise; }
    public void setEntreprise(String entreprise) { this.entreprise = entreprise; }

    public Integer getServiceId() { return serviceId; }
    public void setServiceId(Integer serviceId) { this.serviceId = serviceId; }

    public Integer getSiteId() { return siteId; }
    public void setSiteId(Integer siteId) { this.siteId = siteId; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public String getSignaturePath() { return signaturePath; }
    public void setSignaturePath(String signaturePath) { this.signaturePath = signaturePath; }

    /** Profil du visiteur (ex: Client, Fournisseur, Partenaire...) - non utilise pour employe/stagiaire. */
    public String getProfil() { return profil; }
    public void setProfil(String profil) { this.profil = profil; }

    /** Raison detaillee de la visite - non utilisee pour employe/stagiaire. */
    public String getRaisonVisite() { return raisonVisite; }
    public void setRaisonVisite(String raisonVisite) { this.raisonVisite = raisonVisite; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    @Override
    public String toString() {
        return getNomComplet() + " (" + type + ")";
    }
}
