package com.strata.presence.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

/**
 * Point d'acces unique a la base de donnees (pattern Singleton).
 *
 * Par defaut : SQLite (fichier local, mode hors-ligne).
 * Pour migrer vers MySQL : il suffit de remplacer l'URL JDBC et le driver
 * dans {@link #buildDataSource()} - le reste de la couche DAO (SQL standard,
 * pas de syntaxe specifique SQLite) reste inchange.
 */
public final class DatabaseManager {

    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);
    private static DatabaseManager instance;

    private final DataSource dataSource;
    private final Path dbFolder;

    private DatabaseManager() {
        this.dbFolder = resolveDataFolder();
        this.dataSource = buildDataSource();
        initSchema();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private Path resolveDataFolder() {
        // Dossier de donnees utilisateur, a cote de l'executable (portable, hors-ligne)
        Path folder = Paths.get("data");
        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de creer le dossier de donnees", e);
        }
        return folder;
    }

    private DataSource buildDataSource() {
        Path dbFile = dbFolder.resolve("presence.db");
        HikariConfig config = new HikariConfig();
        // --- Configuration SQLite (par defaut, hors-ligne) ---
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.toAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(4);
        config.setPoolName("PresenceManagerPool");

        // --- Pour migrer vers MySQL, remplacer par exemple par : ---
        // config.setJdbcUrl("jdbc:mysql://localhost:3306/presence_manager?useSSL=false&serverTimezone=UTC");
        // config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        // config.setUsername("root");
        // config.setPassword("motdepasse");

        return new HikariDataSource(config);
    }

    private void initSchema() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             InputStream is = getClass().getResourceAsStream("/schema.sql")) {

            if (is == null) {
                throw new IllegalStateException("schema.sql introuvable dans les ressources");
            }
            String sql = new String(is.readAllBytes());
            // Execution instruction par instruction (SQLite ne supporte pas executeBatch multi-statements via un seul executeUpdate)
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.executeUpdate(trimmed);
                }
            }
            log.info("Schema de base de donnees initialise avec succes ({})", LocalDateTime.now());
        } catch (Exception e) {
            log.error("Erreur lors de l'initialisation du schema", e);
            throw new RuntimeException("Impossible d'initialiser la base de donnees", e);
        }
    }

    /**
     * Sauvegarde automatique : copie physique du fichier SQLite vers /data/backups.
     * Appelee periodiquement par un scheduler (voir service applicatif).
     */
    public void backupNow() {
        try {
            Path backups = dbFolder.resolve("backups");
            Files.createDirectories(backups);
            Path source = dbFolder.resolve("presence.db");
            String timestamp = LocalDateTime.now().toString().replace(":", "-");
            Path target = backups.resolve("presence_" + timestamp + ".db");
            Files.copy(source, target);
            log.info("Sauvegarde automatique effectuee : {}", target);
        } catch (IOException e) {
            log.error("Echec de la sauvegarde automatique", e);
        }
    }

    /** Liste les fichiers de sauvegarde disponibles, du plus recent au plus ancien. */
    public java.util.List<Path> listBackups() {
        Path backups = dbFolder.resolve("backups");
        try {
            if (!Files.exists(backups)) return java.util.List.of();
            try (var stream = Files.list(backups)) {
                return stream.filter(p -> p.toString().endsWith(".db"))
                        .sorted((a, b) -> b.getFileName().toString().compareTo(a.getFileName().toString()))
                        .toList();
            }
        } catch (IOException e) {
            log.error("Impossible de lister les sauvegardes", e);
            return java.util.List.of();
        }
    }

    /**
     * Restaure la base a partir d'un fichier de sauvegarde choisi. L'application
     * doit imperativement etre redemarree juste apres pour que le nouveau
     * fichier soit pris en compte (la connexion active pointe encore sur
     * l'ancien fichier ouvert).
     */
    public void restoreFromBackup(Path backupFile) throws IOException {
        Path target = dbFolder.resolve("presence.db");
        Files.copy(backupFile, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        log.warn("Base de donnees restauree a partir de {} - redemarrage de l'application requis", backupFile);
    }
}
