package com.strata.presence.dao;

import com.strata.presence.model.Person;
import com.strata.presence.model.PersonType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PersonDao {

    private final DatabaseManager db = DatabaseManager.getInstance();

    public Person create(Person p) {
        String qr = UUID.randomUUID().toString();
        String sql = """
            INSERT INTO personnes (type, nom, prenom, piece_identite, telephone, entreprise,
                                    service_id, site_id, photo_path, signature_path, profil,
                                    raison_visite, qr_code, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getType().name());
            ps.setString(2, p.getNom());
            ps.setString(3, p.getPrenom());
            ps.setString(4, p.getPieceIdentite());
            ps.setString(5, p.getTelephone());
            ps.setString(6, p.getEntreprise());
            setNullableInt(ps, 7, p.getServiceId());
            setNullableInt(ps, 8, p.getSiteId());
            ps.setString(9, p.getPhotoPath());
            ps.setString(10, p.getSignaturePath());
            ps.setString(11, p.getProfil());
            ps.setString(12, p.getRaisonVisite());
            ps.setString(13, qr);
            ps.setString(14, LocalDateTime.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getInt(1));
                    p.setQrCode(qr);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la creation de la personne", e);
        }
        return p;
    }

    public List<Person> search(String texte, PersonType type) {
        StringBuilder sql = new StringBuilder("""
            SELECT id, type, nom, prenom, piece_identite, telephone, entreprise,
                   service_id, site_id, photo_path, signature_path, profil, raison_visite, qr_code
            FROM personnes WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();
        if (texte != null && !texte.isBlank()) {
            sql.append(" AND (nom LIKE ? OR prenom LIKE ? OR piece_identite LIKE ?)");
            String like = "%" + texte + "%";
            params.add(like); params.add(like); params.add(like);
        }
        if (type != null) {
            sql.append(" AND type = ?");
            params.add(type.name());
        }
        sql.append(" ORDER BY nom, prenom");

        List<Person> result = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des personnes", e);
        }
        return result;
    }

    /**
     * Recherche une personne deja enregistree, pour eviter de recreer une fiche
     * a chaque passage (visiteur recurrent, employe, stagiaire). Priorite a la
     * piece d'identite (identifiant le plus fiable), puis repli sur nom+prenom.
     */
    public java.util.Optional<Person> findExistingMatch(String pieceIdentite, String nom, String prenom) {
        if (pieceIdentite != null && !pieceIdentite.isBlank()) {
            String sql = """
                SELECT id, type, nom, prenom, piece_identite, telephone, entreprise,
                       service_id, site_id, photo_path, signature_path, profil, raison_visite, qr_code
                FROM personnes WHERE piece_identite = ? ORDER BY id DESC LIMIT 1
            """;
            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pieceIdentite);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return java.util.Optional.of(mapRow(rs));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erreur lors de la recherche de personne existante", e);
            }
        }
        if (nom != null && !nom.isBlank() && prenom != null && !prenom.isBlank()) {
            String sql = """
                SELECT id, type, nom, prenom, piece_identite, telephone, entreprise,
                       service_id, site_id, photo_path, signature_path, profil, raison_visite, qr_code
                FROM personnes WHERE LOWER(nom) = LOWER(?) AND LOWER(prenom) = LOWER(?) ORDER BY id DESC LIMIT 1
            """;
            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nom);
                ps.setString(2, prenom);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return java.util.Optional.of(mapRow(rs));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erreur lors de la recherche de personne existante", e);
            }
        }
        return java.util.Optional.empty();
    }

    public java.util.Optional<Person> findById(int id) {
        String sql = """
            SELECT id, type, nom, prenom, piece_identite, telephone, entreprise,
                   service_id, site_id, photo_path, signature_path, profil, raison_visite, qr_code
            FROM personnes WHERE id = ?
        """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return java.util.Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la personne", e);
        }
        return java.util.Optional.empty();
    }

    /** Nombre total de personnes distinctes deja enregistrees (utilise par le repertoire et le tableau de bord). */
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM personnes";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de comptage des personnes", e);
        }
        return 0;
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, value);
    }

    private Person mapRow(ResultSet rs) throws SQLException {
        Person p = new Person();
        p.setId(rs.getInt("id"));
        p.setType(PersonType.valueOf(rs.getString("type")));
        p.setNom(rs.getString("nom"));
        p.setPrenom(rs.getString("prenom"));
        p.setPieceIdentite(rs.getString("piece_identite"));
        p.setTelephone(rs.getString("telephone"));
        p.setEntreprise(rs.getString("entreprise"));
        int serviceId = rs.getInt("service_id");
        p.setServiceId(rs.wasNull() ? null : serviceId);
        int siteId = rs.getInt("site_id");
        p.setSiteId(rs.wasNull() ? null : siteId);
        p.setPhotoPath(rs.getString("photo_path"));
        p.setSignaturePath(rs.getString("signature_path"));
        p.setProfil(rs.getString("profil"));
        p.setRaisonVisite(rs.getString("raison_visite"));
        p.setQrCode(rs.getString("qr_code"));
        return p;
    }
}
