package com.strata.presence.service;

import com.strata.presence.dao.AttendanceDao;
import com.strata.presence.dao.AuditDao;
import com.strata.presence.model.AttendanceRecord;
import com.strata.presence.model.RoleType;
import com.strata.presence.model.User;
import com.strata.presence.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Service central de gestion des presences.
 *
 * REGLE FONDAMENTALE (anti-fraude) :
 * - Toute arrivee ou depart "normal" est horodate EXCLUSIVEMENT par le systeme,
 *   via LocalDate.now() / LocalTime.now(). Aucune methode publique de cette classe
 *   n'accepte une date/heure fournie par l'utilisateur pour un enregistrement standard.
 *
 * EXCEPTION CONTROLEE (mode derogation) :
 * - Si une personne est arrivee avant l'ouverture de l'accueil, un administrateur ou
 *   un responsable peut, apres re-authentification par mot de passe, saisir manuellement
 *   l'heure reelle d'arrivee via {@link #derogationSaisieManuelle}.
 * - Cette action est TOUJOURS journalisee dans audit_log (qui, ancienne valeur,
 *   nouvelle valeur, date/heure de la modification, justification).
 */
public class AttendanceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceService.class);

    private final AttendanceDao attendanceDao = new AttendanceDao();
    private final AuditDao auditDao = new AuditDao();

    /**
     * Enregistre une arrivee "normale". La date et l'heure sont TOUJOURS generees ici,
     * par le systeme, jamais transmises en parametre.
     */
    public AttendanceRecord enregistrerArrivee(int personneId, Integer motifId, Integer siteId, int enregistrePar) {
        AttendanceRecord r = new AttendanceRecord();
        r.setPersonneId(personneId);
        r.setMotifId(motifId);
        r.setSiteId(siteId);
        r.setDateJour(LocalDate.now());       // <-- genere par le systeme, non modifiable
        r.setHeureArrivee(LocalTime.now());   // <-- genere par le systeme, non modifiable
        r.setStatut("PRESENT");
        r.setSaisieManuelle(false);
        r.setEnregistrePar(enregistrePar);
        AttendanceRecord saved = attendanceDao.create(r);
        log.info("Arrivee enregistree automatiquement : personne={}, date={}, heure={}",
                personneId, saved.getDateJour(), saved.getHeureArrivee());
        return saved;
    }

    /** Enregistre un depart "normal". Heure toujours generee par le systeme. */
    public void enregistrerDepart(int presenceId) {
        LocalTime maintenant = LocalTime.now();
        attendanceDao.enregistrerDepart(presenceId, maintenant);
        log.info("Depart enregistre automatiquement : presence={}, heure={}", presenceId, maintenant);
    }

    /**
     * MODE DEROGATION : permet de corriger manuellement l'heure d'arrivee lorsque
     * la personne est arrivee avant l'ouverture de l'accueil.
     *
     * @param presenceId       identifiant de l'enregistrement de presence a corriger
     * @param nouvelleHeure    heure reelle d'arrivee saisie manuellement
     * @param justification    motif obligatoire de la derogation (ex: "Arrivee avant ouverture accueil")
     * @param acteur           utilisateur qui demande la derogation (doit etre ADMIN ou RESPONSABLE)
     * @param motDePasseConfirmation mot de passe de l'acteur, requis pour reconfirmer l'autorisation
     * @throws SecurityException si l'acteur n'a pas le role requis ou si le mot de passe est incorrect
     */
    public void derogationSaisieManuelle(int presenceId, LocalTime nouvelleHeure, String justification,
                                          User acteur, String motDePasseConfirmation) {
        if (acteur == null || (acteur.getRole() != RoleType.ADMIN && acteur.getRole() != RoleType.RESPONSABLE)) {
            throw new SecurityException("Seul un administrateur ou un responsable peut activer le mode derogation.");
        }
        if (!PasswordUtil.verify(motDePasseConfirmation, acteur.getSalt(), acteur.getPasswordHash())) {
            throw new SecurityException("Mot de passe de confirmation incorrect. Derogation refusee.");
        }
        if (justification == null || justification.isBlank()) {
            throw new IllegalArgumentException("Une justification est obligatoire pour toute saisie manuelle.");
        }

        Optional<AttendanceRecord> recordOpt = attendanceDao.findById(presenceId);
        if (recordOpt.isEmpty()) {
            throw new IllegalArgumentException("Enregistrement de presence introuvable.");
        }
        AttendanceRecord record = recordOpt.get();
        String ancienneValeur = record.getHeureArrivee().toString();

        attendanceDao.modifierHeureArrivee(presenceId, nouvelleHeure);

        // Tracabilite obligatoire et systematique
        auditDao.log(presenceId, acteur.getId(), "heure_arrivee",
                ancienneValeur, nouvelleHeure.toString(), justification);

        log.warn("DEROGATION appliquee par {} sur presence={} : {} -> {} (motif: {})",
                acteur.getUsername(), presenceId, ancienneValeur, nouvelleHeure, justification);
    }

    /**
     * Supprime definitivement un enregistrement de presence. Reserve aux roles
     * ADMIN et RESPONSABLE, avec re-confirmation par mot de passe et justification
     * obligatoire - meme niveau d'exigence que le mode derogation, car supprimer
     * une presence est une action sensible qui doit rester tracable.
     *
     * @param presenceId identifiant de la presence a supprimer
     * @param justification motif obligatoire de la suppression
     * @param acteur utilisateur qui demande la suppression (doit etre ADMIN ou RESPONSABLE)
     * @param motDePasseConfirmation mot de passe de l'acteur, requis pour reconfirmer l'autorisation
     * @throws SecurityException si l'acteur n'a pas le role requis ou si le mot de passe est incorrect
     */
    public void supprimerPresence(int presenceId, String justification, User acteur, String motDePasseConfirmation) {
        if (acteur == null || (acteur.getRole() != RoleType.ADMIN && acteur.getRole() != RoleType.RESPONSABLE)) {
            throw new SecurityException("Seul un administrateur ou un responsable peut supprimer une presence.");
        }
        if (!PasswordUtil.verify(motDePasseConfirmation, acteur.getSalt(), acteur.getPasswordHash())) {
            throw new SecurityException("Mot de passe de confirmation incorrect. Suppression refusee.");
        }
        if (justification == null || justification.isBlank()) {
            throw new IllegalArgumentException("Une justification est obligatoire pour supprimer une presence.");
        }

        Optional<AttendanceRecord> recordOpt = attendanceDao.findById(presenceId);
        if (recordOpt.isEmpty()) {
            throw new IllegalArgumentException("Enregistrement de presence introuvable.");
        }
        AttendanceRecord record = recordOpt.get();
        String resume = record.getPersonneNomComplet() + " - " + record.getDateJour()
                + " " + record.getHeureArrivee();

        // Tracabilite obligatoire et systematique - journalisee AVANT suppression physique
        // pour garder une reference valide vers la presence au moment de l'ecriture de l'audit
        auditDao.log(presenceId, acteur.getId(), "suppression_presence", resume, "SUPPRIME", justification);

        attendanceDao.delete(presenceId);

        log.warn("SUPPRESSION de presence appliquee par {} sur presence={} ({}) - motif: {}",
                acteur.getUsername(), presenceId, resume, justification);
    }

    public List<AttendanceRecord> presencesDuJour() {
        return attendanceDao.findByDate(LocalDate.now());
    }

    /** Registre du jour enrichi (nom/prenom, profil, service, telephone, photo, signature). */
    public List<AttendanceRecord> registreDuJour() {
        return attendanceDao.findTodayDetailed();
    }

    public List<AttendanceRecord> presencesDePersonne(int personneId) {
        return attendanceDao.findByPersonneId(personneId);
    }

    public List<AttendanceRecord> activiteRecente(int limit) {
        return attendanceDao.findRecent(limit);
    }

    public List<AttendanceRecord> presencesEntre(LocalDate debut, LocalDate fin) {
        return attendanceDao.findBetween(debut, fin);
    }

    public int nombrePresentsAujourdHui() {
        return attendanceDao.countPresentsAujourdHui();
    }

    public int nombreTotalAujourdHui() {
        return attendanceDao.countTotalAujourdHui();
    }

    /** Utilise par le tableau de bord pour tracer l'evolution sur les derniers jours. */
    public int nombreTotalPourDate(LocalDate date) {
        return attendanceDao.countTotalPourDate(date);
    }

    /** Repartition visiteurs / personnel pour une date donnee (graphique du tableau de bord). */
    public int nombreParTypePourDate(LocalDate date, String type) {
        return attendanceDao.countParTypePourDate(date, type);
    }
}
