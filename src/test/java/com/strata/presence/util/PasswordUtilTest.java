package com.strata.presence.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void unMemeMoteDePasseAvecMemeSelDonneLeMemeHash() {
        String salt = PasswordUtil.generateSalt();
        String hash1 = PasswordUtil.hash("MotDePasse123", salt);
        String hash2 = PasswordUtil.hash("MotDePasse123", salt);
        assertEquals(hash1, hash2, "Le hachage doit etre deterministe pour un meme sel");
    }

    @Test
    void deuxSelsDifferentsDonnentDesHashsDifferents() {
        String salt1 = PasswordUtil.generateSalt();
        String salt2 = PasswordUtil.generateSalt();
        assertNotEquals(salt1, salt2, "Chaque sel genere doit etre unique");

        String hash1 = PasswordUtil.hash("MotDePasse123", salt1);
        String hash2 = PasswordUtil.hash("MotDePasse123", salt2);
        assertNotEquals(hash1, hash2, "Un meme mot de passe avec des sels differents doit donner des hashs differents");
    }

    @Test
    void verifyReussitAvecLeBonMotDePasse() {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash("Admin@2026", salt);
        assertTrue(PasswordUtil.verify("Admin@2026", salt, hash));
    }

    @Test
    void verifyEchoueAvecUnMauvaisMotDePasse() {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash("Admin@2026", salt);
        assertFalse(PasswordUtil.verify("MauvaisMotDePasse", salt, hash));
    }

    @Test
    void leMotDePasseEnClairNestJamaisPresentDansLeHash() {
        String salt = PasswordUtil.generateSalt();
        String motDePasse = "SuperSecret2026";
        String hash = PasswordUtil.hash(motDePasse, salt);
        assertFalse(hash.contains(motDePasse), "Le hash ne doit jamais contenir le mot de passe en clair");
    }
}
