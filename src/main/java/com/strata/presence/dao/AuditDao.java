package com.strata.presence.dao;

import com.strata.presence.model.AuditLogEntry;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditDao {

    private final DatabaseManager db = DatabaseManager.getInstance();

    public void log(int presenceId, int userId, String champ, String ancienne, String nouvelle, String justification) {
        String sql = """
            INSERT INTO audit_log (presence_id, user_id, champ_modifie, ancienne_valeur, nouvelle_valeur, justification, date_action)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, presenceId);
            ps.setInt(2, userId);
            ps.setString(3, champ);
            ps.setString(4, ancienne);
            ps.setString(5, nouvelle);
            ps.setString(6, justification);
            ps.setString(7, LocalDateTime.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ecriture du journal d'audit", e);
        }
    }

    public List<AuditLogEntry> findAll() {
        String sql = """
            SELECT a.*, u.full_name FROM audit_log a
            JOIN users u ON a.user_id = u.id
            ORDER BY a.date_action DESC
        """;
        List<AuditLogEntry> result = new ArrayList<>();
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                AuditLogEntry e = new AuditLogEntry();
                e.setId(rs.getInt("id"));
                e.setPresenceId(rs.getInt("presence_id"));
                e.setUserId(rs.getInt("user_id"));
                e.setUserFullName(rs.getString("full_name"));
                e.setChampModifie(rs.getString("champ_modifie"));
                e.setAncienneValeur(rs.getString("ancienne_valeur"));
                e.setNouvelleValeur(rs.getString("nouvelle_valeur"));
                e.setJustification(rs.getString("justification"));
                e.setDateAction(LocalDateTime.parse(rs.getString("date_action")));
                result.add(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement du journal d'audit", e);
        }
        return result;
    }
}
