package com.strata.presence.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hachage securise des mots de passe (SHA-256 + sel aleatoire par utilisateur).
 * Aucun mot de passe n'est jamais stocke ou affiche en clair.
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String plainPassword, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Base64.getDecoder().decode(salt));
            byte[] hashed = digest.digest(plainPassword.getBytes());
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithme de hachage indisponible", e);
        }
    }

    public static boolean verify(String plainPassword, String salt, String expectedHash) {
        String computed = hash(plainPassword, salt);
        return computed.equals(expectedHash);
    }
}
