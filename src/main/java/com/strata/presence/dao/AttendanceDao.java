package com.strata.presence.dao;

import com.strata.presence.model.AttendanceRecord;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AttendanceDao {

    private final DatabaseManager db = DatabaseManager.getInstance();

    /**
     * Registre du jour "detaille" : jointure avec personnes et services pour
     * afficher directement nom/prenom, profil, service, telephone, photo et
     * signature sans requetes supplementaires - utilise par le registre du jour
     * affiche sous le formulaire d'enregistrement.
     */
    public List<AttendanceRecord> findTodayDetailed() {
        String sql = """
            SELECT p.id, p.personne_id, p.motif_id, p.date_jour, p.heure_arrivee, p.heure_depart, p.statut,
                   p.saisie_manuelle, p.enregistre_par, p.site_id, p.created_at,
                   per.nom || ' ' || per.prenom AS nom_complet,
                   per.profil, per.telephone, per.photo_path, per.signature_path,
                   s.nom AS service_nom
            FROM presences p
            JOIN personnes per ON p.personne_id = per.id
            LEFT JOIN services s ON per.service_id = s.id
            WHERE p.date_jour = ?
            ORDER BY p.heure_arrivee DESC
        """;
        List<AttendanceRecord> result = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, LocalDate.now().toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceRecord r = mapRow(rs);
                    r.setProfil(rs.getString("profil"));
                    r.setTelephone(rs.getString("telephone"));
                    r.setServiceNom(rs.getString("service_nom"));
                    r.setPhotoPath(rs.getString("photo_path"));
                    r.setSignaturePath(rs.getString("signature_path"));
                    result.add(r);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement du registre du jour", e);
        }
        return result;
    }

    public AttendanceRecord create(AttendanceRecord r) {
        String sql = """
            INSERT INTO presences (personne_id, motif_id, date_jour, heure_arrivee, heure_depart,
                                    statut, saisie_manuelle, enregistre_par, site_id, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getPersonneId());
            setNullableInt(ps, 2, r.getMotifId());
            ps.setString(3, r.getDateJour().toString());
            ps.setString(4, r.getHeureArrivee().toString());
            ps.setString(5, r.getHeureDepart() != null ? r.getHeureDepart().toString() : null);
            ps.setString(6, r.getStatut());
            ps.setInt(7, r.isSaisieManuelle() ? 1 : 0);
            ps.setInt(8, r.getEnregistrePar());
            setNullableInt(ps, 9, r.getSiteId());
            ps.setString(10, LocalDateTime.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) r.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement de la presence", e);
        }
        return r;
    }

    public void enregistrerDepart(int presenceId, LocalTime heureDepart) {
        String sql = "UPDATE presences SET heure_depart = ?, statut = 'PARTI' WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, heureDepart.toString());
            ps.setInt(2, presenceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du depart", e);
        }
    }

    /** Utilise uniquement par le mode derogation (saisie manuelle autorisee). */
    public void modifierHeureArrivee(int presenceId, LocalTime nouvelleHeure) {
        String sql = "UPDATE presences SET heure_arrivee = ?, saisie_manuelle = 1 WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nouvelleHeure.toString());
            ps.setInt(2, presenceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification manuelle de l'heure", e);
        }
    }

    /** Suppression definitive d'un enregistrement de presence. Reserve aux roles ADMIN/RESPONSABLE (verifie en amont). */
    public void delete(int presenceId) {
        String sql = "DELETE FROM presences WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, presenceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la presence", e);
        }
    }

    public Optional<AttendanceRecord> findById(int id) {
        String sql = """
            SELECT p.*, per.nom || ' ' || per.prenom AS nom_complet
            FROM presences p JOIN personnes per ON p.personne_id = per.id
            WHERE p.id = ?
        """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la presence", e);
        }
        return Optional.empty();
    }

    public List<AttendanceRecord> findByDate(LocalDate date) {
        String sql = """
            SELECT p.*, per.nom || ' ' || per.prenom AS nom_complet
            FROM presences p JOIN personnes per ON p.personne_id = per.id
            WHERE p.date_jour = ? ORDER BY p.heure_arrivee DESC
        """;
        return queryList(sql, date.toString());
    }

    public List<AttendanceRecord> findBetween(LocalDate start, LocalDate end) {
        String sql = """
            SELECT p.*, per.nom || ' ' || per.prenom AS nom_complet
            FROM presences p JOIN personnes per ON p.personne_id = per.id
            WHERE p.date_jour BETWEEN ? AND ? ORDER BY p.date_jour DESC, p.heure_arrivee DESC
        """;
        List<AttendanceRecord> result = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, start.toString());
            ps.setString(2, end.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des presences", e);
        }
        return result;
    }

    public int countPresentsAujourdHui() {
        String sql = "SELECT COUNT(*) FROM presences WHERE date_jour = ? AND statut = 'PRESENT'";
        return countWithDate(sql);
    }

    public List<AttendanceRecord> findByPersonneId(int personneId) {
        String sql = """
            SELECT p.*, per.nom || ' ' || per.prenom AS nom_complet
            FROM presences p JOIN personnes per ON p.personne_id = per.id
            WHERE p.personne_id = ? ORDER BY p.date_jour DESC, p.heure_arrivee DESC
        """;
        List<AttendanceRecord> result = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, personneId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des presences de la personne", e);
        }
        return result;
    }

    /** Les N derniers mouvements (arrivees/departs), tous types confondus - utilise pour l'activite recente du tableau de bord. */
    public List<AttendanceRecord> findRecent(int limit) {
        String sql = """
            SELECT p.*, per.nom || ' ' || per.prenom AS nom_complet
            FROM presences p JOIN personnes per ON p.personne_id = per.id
            ORDER BY p.created_at DESC LIMIT ?
        """;
        List<AttendanceRecord> result = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'activite recente", e);
        }
        return result;
    }

    public int countTotalAujourdHui() {
        String sql = "SELECT COUNT(*) FROM presences WHERE date_jour = ?";
        return countWithDate(sql);
    }

    /** Nombre total de presences pour une date donnee (utilise pour les graphiques d'evolution). */
    public int countTotalPourDate(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM presences WHERE date_jour = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de comptage", e);
        }
        return 0;
    }

    /** Repartition visiteurs / personnel pour une date donnee. */
    public int countParTypePourDate(LocalDate date, String type) {
        String sql = """
            SELECT COUNT(*) FROM presences p JOIN personnes per ON p.personne_id = per.id
            WHERE p.date_jour = ? AND per.type = ?
        """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.toString());
            ps.setString(2, type);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de comptage", e);
        }
        return 0;
    }

    private int countWithDate(String sql) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, LocalDate.now().toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de comptage", e);
        }
        return 0;
    }

    private List<AttendanceRecord> queryList(String sql, String param) {
        List<AttendanceRecord> result = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la requete", e);
        }
        return result;
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, value);
    }

    private AttendanceRecord mapRow(ResultSet rs) throws SQLException {
        AttendanceRecord r = new AttendanceRecord();
        r.setId(rs.getInt("id"));
        r.setPersonneId(rs.getInt("personne_id"));
        r.setPersonneNomComplet(rs.getString("nom_complet"));
        int motifId = rs.getInt("motif_id");
        r.setMotifId(rs.wasNull() ? null : motifId);
        r.setDateJour(LocalDate.parse(rs.getString("date_jour")));
        r.setHeureArrivee(LocalTime.parse(rs.getString("heure_arrivee")));
        String depart = rs.getString("heure_depart");
        r.setHeureDepart(depart != null ? LocalTime.parse(depart) : null);
        r.setStatut(rs.getString("statut"));
        r.setSaisieManuelle(rs.getInt("saisie_manuelle") == 1);
        r.setEnregistrePar(rs.getInt("enregistre_par"));
        int siteId = rs.getInt("site_id");
        r.setSiteId(rs.wasNull() ? null : siteId);
        r.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
        return r;
    }
}
