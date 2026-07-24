package com.strata.presence.dao;

import com.strata.presence.model.Site;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SiteDao {

    private final DatabaseManager db = DatabaseManager.getInstance();

    public List<Site> findAll() {
        List<Site> sites = new ArrayList<>();
        String sql = "SELECT id, nom, adresse FROM sites ORDER BY nom";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sites.add(new Site(rs.getInt("id"), rs.getString("nom"), rs.getString("adresse")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des sites", e);
        }
        return sites;
    }

    public Site create(String nom, String adresse) {
        String sql = "INSERT INTO sites (nom, adresse) VALUES (?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nom);
            ps.setString(2, adresse);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Site(keys.getInt(1), nom, adresse);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la creation du site", e);
        }
        throw new IllegalStateException("Creation du site echouee");
    }

    public void delete(int id) {
        String sql = "DELETE FROM sites WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du site", e);
        }
    }
}
