package com.strata.presence.service;

import com.strata.presence.dao.UserDao;
import com.strata.presence.model.RoleType;
import com.strata.presence.model.User;
import com.strata.presence.util.PasswordUtil;

import java.util.Optional;

/**
 * Service d'authentification. Gere aussi la session utilisateur courante
 * (pattern Singleton simplifie pour une application desktop mono-utilisateur-a-la-fois).
 */
public class AuthService {

    private static AuthService instance;
    private final UserDao userDao = new UserDao();
    private User currentUser;

    private AuthService() {}

    public static synchronized AuthService getInstance() {
        if (instance == null) instance = new AuthService();
        return instance;
    }

    public Optional<User> login(String username, String plainPassword) {
        Optional<User> userOpt = userDao.findByUsername(username);
        if (userOpt.isEmpty()) return Optional.empty();
        User user = userOpt.get();
        if (!user.isActive()) return Optional.empty();
        if (!PasswordUtil.verify(plainPassword, user.getSalt(), user.getPasswordHash())) {
            return Optional.empty();
        }
        this.currentUser = user;
        return Optional.of(user);
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean hasRole(RoleType role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    public boolean isAdmin() {
        return hasRole(RoleType.ADMIN);
    }

    /** Cree le compte admin par defaut si aucun utilisateur n'existe encore (premiere installation). */
    public void ensureDefaultAdmin() {
        if (userDao.findAll().isEmpty()) {
            userDao.create("admin", "Admin@2026", "Administrateur Systeme", RoleType.ADMIN);
        }
    }
}
