package com.strata.presence.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Stockage cle-valeur simple pour les parametres applicatifs globaux
 * (ex: indicateur "assistant de premier lancement termine").
 */
public class AppSettingsDao {

    private final DatabaseManager db = DatabaseManager.getInstance();

    public Optional<String> get(String key) {
        String sql = "SELECT setting_value FROM app_settings WHERE setting_key = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.ofNullable(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture du parametre " + key, e);
        }
        return Optional.empty();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return get(key).map("true"::equalsIgnoreCase).orElse(defaultValue);
    }

    public void set(String key, String value) {
        String sql = "INSERT INTO app_settings (setting_key, setting_value) VALUES (?, ?) " +
                "ON CONFLICT(setting_key) DO UPDATE SET setting_value = excluded.setting_value";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ecriture du parametre " + key, e);
        }
    }

    public void setBoolean(String key, boolean value) {
        set(key, String.valueOf(value));
    }
}
