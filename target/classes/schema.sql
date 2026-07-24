-- =====================================================================
-- Strata Presence Manager - Schema de base de donnees (SQLite)
-- Compatible avec une migration future vers MySQL (types standards)
-- =====================================================================

CREATE TABLE IF NOT EXISTS app_settings (
    setting_key     VARCHAR(50) PRIMARY KEY,
    setting_value   VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS roles (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    code            VARCHAR(30)  NOT NULL UNIQUE,   -- ADMIN, AGENT, RESPONSABLE
    libelle         VARCHAR(60)  NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    salt            VARCHAR(64)  NOT NULL,
    full_name       VARCHAR(120) NOT NULL,
    role_id         INTEGER NOT NULL,
    active          INTEGER NOT NULL DEFAULT 1,
    created_at      TEXT NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE IF NOT EXISTS services (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    nom             VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS motifs (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    libelle         VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS sites (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    nom             VARCHAR(100) NOT NULL UNIQUE,
    adresse         VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS personnes (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    type            VARCHAR(10)  NOT NULL,          -- VISITEUR / PERSONNEL / STAGIAIRE
    nom             VARCHAR(80)  NOT NULL,
    prenom          VARCHAR(80)  NOT NULL,
    piece_identite  VARCHAR(50),
    telephone       VARCHAR(30),
    entreprise      VARCHAR(120),                   -- pour visiteurs
    service_id      INTEGER,
    site_id         INTEGER,
    photo_path      VARCHAR(255),
    signature_path  VARCHAR(255),
    profil          VARCHAR(100),
    raison_visite   VARCHAR(255),
    qr_code         VARCHAR(100) UNIQUE,
    created_at      TEXT NOT NULL,
    FOREIGN KEY (service_id) REFERENCES services(id),
    FOREIGN KEY (site_id) REFERENCES sites(id)
);
CREATE INDEX IF NOT EXISTS idx_personnes_nom ON personnes(nom, prenom);
CREATE INDEX IF NOT EXISTS idx_personnes_type ON personnes(type);

CREATE TABLE IF NOT EXISTS presences (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    personne_id     INTEGER NOT NULL,
    motif_id        INTEGER,
    date_jour       TEXT NOT NULL,                  -- ISO LocalDate genere par le systeme
    heure_arrivee   TEXT NOT NULL,                  -- ISO LocalTime genere par le systeme
    heure_depart    TEXT,
    statut          VARCHAR(20) NOT NULL DEFAULT 'PRESENT',  -- PRESENT / PARTI
    saisie_manuelle INTEGER NOT NULL DEFAULT 0,      -- 1 si heure derogatoire saisie manuellement
    enregistre_par  INTEGER NOT NULL,                -- user_id de l'agent qui a fait l'enregistrement
    site_id         INTEGER,
    created_at      TEXT NOT NULL,
    FOREIGN KEY (personne_id) REFERENCES personnes(id),
    FOREIGN KEY (motif_id) REFERENCES motifs(id),
    FOREIGN KEY (enregistre_par) REFERENCES users(id),
    FOREIGN KEY (site_id) REFERENCES sites(id)
);
CREATE INDEX IF NOT EXISTS idx_presences_date ON presences(date_jour);
CREATE INDEX IF NOT EXISTS idx_presences_personne ON presences(personne_id);

-- Journal d'audit : trace toute modification manuelle d'heure/date (derogation)
CREATE TABLE IF NOT EXISTS audit_log (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    presence_id     INTEGER NOT NULL,
    user_id         INTEGER NOT NULL,               -- qui a fait la modification
    champ_modifie   VARCHAR(30) NOT NULL,           -- ex: heure_arrivee
    ancienne_valeur VARCHAR(60),
    nouvelle_valeur VARCHAR(60),
    justification   VARCHAR(255),
    date_action     TEXT NOT NULL,                  -- horodatage systeme de l'action elle-meme
    FOREIGN KEY (presence_id) REFERENCES presences(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Donnees de reference initiales
INSERT OR IGNORE INTO roles (id, code, libelle) VALUES
 (1, 'ADMIN', 'Administrateur'),
 (2, 'AGENT', 'Agent d''accueil'),
 (3, 'RESPONSABLE', 'Responsable');

INSERT OR IGNORE INTO services (id, nom) VALUES
 (1, 'Direction Generale'),
 (2, 'Ressources Humaines'),
 (3, 'Informatique'),
 (4, 'Comptabilite'),
 (5, 'Accueil');

INSERT OR IGNORE INTO motifs (id, libelle) VALUES
 (1, 'Rendez-vous'),
 (2, 'Entretien'),
 (3, 'Livraison'),
 (4, 'Reunion'),
 (5, 'Autre');

INSERT OR IGNORE INTO sites (id, nom, adresse) VALUES
 (1, 'Siege - Douala', 'Akwa, Douala, Cameroun');
