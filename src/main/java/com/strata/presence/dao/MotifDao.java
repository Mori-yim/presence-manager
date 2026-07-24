package com.strata.presence.dao;

import com.strata.presence.model.Motif;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MotifDao {

    private final DatabaseManager db = DatabaseManager.getInstance();

    public List<Motif> findAll() {
        List<Motif> motifs = new ArrayList<>();
        String sql = "SELECT id, libelle FROM motifs ORDER BY libelle";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                motifs.add(new Motif(rs.getInt("id"), rs.getString("libelle")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des motifs", e);
        }
        return motifs;
    }

    public Motif create(String libelle) {
        String sql = "INSERT INTO motifs (libelle) VALUES (?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, libelle);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return new Motif(keys.getInt(1), libelle);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la creation du motif", e);
        }
        throw new IllegalStateException("Creation du motif echouee");
    }

    public void delete(int id) {
        String sql = "DELETE FROM motifs WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du motif", e);
        }
    }
}
