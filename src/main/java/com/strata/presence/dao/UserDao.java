package com.strata.presence.dao;

import com.strata.presence.model.RoleType;
import com.strata.presence.model.User;
import com.strata.presence.util.PasswordUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO pour la table users. Respecte le pattern DAO / Repository :
 * aucune logique metier ici, uniquement de l'acces aux donnees.
 */
public class UserDao {

    private final DatabaseManager db = DatabaseManager.getInstance();

    public Optional<User> findByUsername(String username) {
        String sql = """
            SELECT u.id, u.username, u.password_hash, u.salt, u.full_name, u.active, u.created_at, r.code
            FROM users u JOIN roles r ON u.role_id = r.id
            WHERE u.username = ?
        """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'utilisateur", e);
        }
        return Optional.empty();
    }

    public List<User> findAll() {
        String sql = """
            SELECT u.id, u.username, u.password_hash, u.salt, u.full_name, u.active, u.created_at, r.code
            FROM users u JOIN roles r ON u.role_id = r.id ORDER BY u.full_name
        """;
        List<User> users = new ArrayList<>();
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des utilisateurs", e);
        }
        return users;
    }

    public User create(String username, String plainPassword, String fullName, RoleType role) {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(plainPassword, salt);
        String sql = """
            INSERT INTO users (username, password_hash, salt, full_name, role_id, active, created_at)
            VALUES (?, ?, ?, ?, (SELECT id FROM roles WHERE code = ?), 1, ?)
        """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, salt);
            ps.setString(4, fullName);
            ps.setString(5, role.name());
            ps.setString(6, LocalDateTime.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    User u = new User(keys.getInt(1), username, fullName, role, true);
                    return u;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la creation de l'utilisateur", e);
        }
        throw new IllegalStateException("Creation utilisateur echouee");
    }

    public void setActive(int userId, boolean active) {
        String sql = "UPDATE users SET active = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise a jour de l'utilisateur", e);
        }
    }

    /** Supprime definitivement un utilisateur. Reserve aux roles ADMIN et RESPONSABLE (verifie en amont dans l'UI). */
    public void delete(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'utilisateur", e);
        }
    }

    /** Change le mot de passe d'un utilisateur (nouveau sel genere a chaque changement). */
    public void updatePassword(int userId, String newPlainPassword) {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(newPlainPassword, salt);
        String sql = "UPDATE users SET password_hash = ?, salt = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setString(2, salt);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du changement de mot de passe", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setSalt(rs.getString("salt"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(RoleType.valueOf(rs.getString("code")));
        u.setActive(rs.getInt("active") == 1);
        u.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
        return u;
    }
}
