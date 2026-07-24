package com.strata.presence.dao;

import com.strata.presence.model.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDao {

    private final DatabaseManager db = DatabaseManager.getInstance();

    public List<Service> findAll() {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT id, nom FROM services ORDER BY nom";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                services.add(new Service(rs.getInt("id"), rs.getString("nom")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des services", e);
        }
        return services;
    }

    public Service create(String nom) {
        String sql = "INSERT INTO services (nom) VALUES (?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nom);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return new Service(keys.getInt(1), nom);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la creation du service", e);
        }
        throw new IllegalStateException("Creation du service echouee");
    }

    public void delete(int id) {
        String sql = "DELETE FROM services WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du service", e);
        }
    }
}
