# Strata Presence Manager

Logiciel professionnel de gestion des presences (visiteurs & personnel) pour Strata Agency.
Application desktop Java Swing, 100% hors ligne, base SQLite embarquee.

## 1. Ce qui est livre dans cette version (noyau fonctionnel)

- Authentification securisee (SHA-256 + sel), roles ADMIN / AGENT / RESPONSABLE
- Enregistrement des visiteurs et du personnel
- Horodatage **automatique et non modifiable** (LocalDate.now() / LocalTime.now()) a l'arrivee et au depart
- **Mode derogation** protege par mot de passe pour saisir manuellement une heure d'arrivee
  (personne arrivee avant l'ouverture de l'accueil), avec **journal d'audit complet**
  (qui, ancienne valeur, nouvelle valeur, justification, date de la modification)
- Tableau de bord temps reel (horloge, effectifs du jour, presents, departs)
- Historique avec recherche par periode
- **Badges professionnels imprimables** : QR Code (ZXing), design carte avec logo,
  nom, photo placeholder, service, date/heure, statut - impression directe via
  l'API `java.awt.print` (bouton "Imprimer le badge" disponible juste apres
  l'enregistrement d'une arrivee)
- **Rapports et exports** : rapports journalier / hebdomadaire / mensuel / annuel
  (agregation automatique par periode a partir d'une date de reference), avec
  export **PDF** (OpenPDF), **Excel** (Apache POI, .xlsx) et **CSV** (OpenCSV) -
  onglet "Rapports" accessible a tous les roles
- **Capture photo webcam** : bouton "Prendre une photo" a l'enregistrement (webcam-capture),
  redimensionnement automatique (Thumbnailator), photo integree au badge (remplace
  le placeholder initiales quand une photo est disponible ; sans webcam detectee,
  message clair et fonctionnement inchange avec initiales)
- **Graphiques du tableau de bord** (JFreeChart) : evolution des presences sur les
  7 derniers jours (barres) et repartition visiteurs/personnel du jour (camembert)
- **Gestion multi-site** : creation/suppression de sites, onglet "Sites" (role ADMIN)
- **Mode clair / sombre** : bascule immediate (FlatLaf), onglet "Parametres"
- Administration des utilisateurs (creation, roles)
- **Finitions professionnelles** : icone applicative personnalisee (barre de titre,
  barre des taches, boites de dialogue), ecran de demarrage (splash screen) pendant
  l'initialisation, menu Fichier/Aide avec "A propos" et manuel utilisateur integre,
  barre de statut (utilisateur connecte, role, etat de connexion base de donnees,
  horloge), confirmation avant deconnexion/fermeture, validation visuelle des
  champs obligatoires (bordure rouge), infobulles sur les actions sensibles,
  gestionnaire d'erreurs global (aucune trace d'erreur brute affichee a
  l'utilisateur final, tout est journalise dans `logs/`)
- **Polish visuel (2e passe)** : icones vectorielles coherentes sur tous les onglets
  et boutons cles (aucun asset externe requis), tableaux harmonises (tri par colonne,
  lignes alternees, en-tete accentue) sur Historique/Rapports/Audit/Utilisateurs/Sites,
  en-tetes de panneau uniformes (icone + titre + sous-titre), ecran de connexion
  repense avec fond degrade et carte centrale (assorti au splash screen), cartes du
  tableau de bord avec code couleur par indicateur
- **Assistant de configuration au premier lancement** : affiche automatiquement a
  la premiere connexion de l'administrateur - changement guide du mot de passe
  par defaut, puis creation du premier site de l'entreprise (ne s'affiche qu'une
  seule fois, indicateur stocke en base dans `app_settings`)
- **Animations** : fondu d'apparition des fenetres (connexion, fenetre principale),
  compteurs animes ("count-up") sur les cartes du tableau de bord, notifications
  "toast" glissees + fondues (badge imprime, rapport exporte, site/utilisateur
  cree) a la place de boites de dialogue bloquantes pour les confirmations simples,
  transition douce entre les etapes de l'assistant de configuration
- **Identite visuelle orange** : la couleur d'accent est passee du bleu a l'orange
  dans toute l'application (icones, en-tetes, tableaux, badges, rapports PDF/Excel,
  icone applicative)
- **Profil "Stagiaire"** ajoute a cote de Visiteur/Employe, avec libelles d'affichage
  clairs dans tous les menus deroulants
- **Champs Profil et Raison de la visite** (Client/Fournisseur/Partenaire/Presse/
  Institution/Autre + texte libre) - affiches uniquement quand le type
  selectionne est "Visiteur", masques pour Employe/Stagiaire
- **Suppression d'utilisateur** desormais ouverte aux roles Administrateur ET
  Responsable (la creation de compte reste reservee a l'Administrateur ; un
  utilisateur ne peut pas se supprimer lui-meme)
- **QR Code du badge** pointe maintenant vers une conversation WhatsApp
  (`https://wa.me/<numero international>`) construite a partir du telephone de
  la personne - l'indicatif pays Cameroun (+237) est complete automatiquement
  pour un numero local a 9 chiffres, sinon le numero saisi est utilise tel quel ;
  si aucun telephone n'est renseigne, le QR encode l'identifiant unique en secours
- **Correction** : le formulaire d'enregistrement defile desormais correctement
  (JScrollPane) - tous les champs et le bouton "Enregistrer l'arrivee" restent
  accessibles meme sur un ecran de petite hauteur
- **Correction importante - capture photo webcam** : la detection de la webcam
  se faisait de maniere synchrone et pouvait geler l'interface sans retour
  visuel ; si aucune webcam n'etait trouvee, la fenetre se fermait avant meme
  d'etre affichee (aucun message clair pour l'agent). Desormais : detection en
  arriere-plan avec delai maximum de 5 secondes, fenetre toujours affichee
  avec un message explicite dans tous les cas, et resolution video qui
  s'adapte a ce que le pilote supporte reellement (au lieu d'une taille fixe
  pouvant provoquer une erreur selon le materiel)
- **Activite recente** sur le tableau de bord : les 10 derniers mouvements
  (arrivees/departs), tous types confondus, rafraichis automatiquement
- **Repertoire des personnes** (nouvel onglet) : recherche parmi toutes les
  fiches deja enregistrees (visiteurs/employes/stagiaires), avec acces a
  l'historique complet de chaque personne (nombre de passages, dates, heures)
- **Pagination de l'historique** (50 resultats par page) avec navigation
  Precedent/Suivant, utile des que le nombre d'enregistrements grandit
- **Verrouillage de session** (bouton "Verrouiller" ou raccourci Ctrl+L) :
  protege un poste d'accueil partage sans deconnexion complete - il faut
  ressaisir le mot de passe de l'utilisateur en cours pour deverrouiller, ou
  se deconnecter completement depuis l'ecran de verrouillage
- **Tests unitaires JUnit** : hachage de mot de passe, construction des liens
  WhatsApp (avec regression specifique sur le bug d'indicatif pays), calcul
  des bornes de periode des rapports (journalier/hebdo/mensuel/annuel)
- **Registre du jour** (nouveau, sous le formulaire d'enregistrement, onglet
  "Enregistrement") : liste toutes les arrivees/departs du jour avec nom et
  prenom, profil, service, telephone, heure d'arrivee, miniatures photo et
  signature, statut. **Comble une lacune importante** : jusque-la, aucun ecran
  ne permettait d'enregistrer un depart alors que la logique existait deja
  cote service - un bouton "Marquer le depart" est desormais disponible
  directement dans ce registre (heure generee par le systeme, comme pour
  l'arrivee)
- **Suppression d'une presence** (apres enregistrement) ouverte aux roles
  Administrateur et Responsable depuis l'onglet Historique, avec le meme niveau
  d'exigence que le mode derogation : mot de passe et justification obligatoires,
  action systematiquement tracee dans le journal d'audit
- Sauvegarde automatique de la base SQLite toutes les 30 minutes (dossier `data/backups`),
  restauration pilotable depuis l'onglet Parametres
- Architecture en couches : Model / DAO / Service / UI (MVC), Singleton (DatabaseManager,
  AuthService), pret pour migration MySQL (voir `DatabaseManager.buildDataSource()`)

## 2. Modules non couverts (ameliorations futures possibles, non bloquantes)

L'ensemble des fonctionnalites du cahier des charges initial sont livrees dans cette
version. Pistes d'amelioration ulterieures, si besoin :

- Gestion fine des services/motifs via une UI dediee (actuellement modifiable en base
  uniquement, via `schema.sql` ou un client SQLite) - la structure existe deja en base
- Signature numerique du visiteur (pad de signature tactile)
- Passage effectif a une architecture MySQL en production (la couche DAO le permet deja,
  voir section 6)

**Note technique sur les rapports** : la generation est faite par code directement
avec OpenPDF / Apache POI / OpenCSV, plutot qu'avec des templates JasperReports
(`.jrxml`). JasperReports necessite normalement de concevoir le gabarit visuellement
dans Jaspersoft Studio (outil externe), ce qui n'apporte pas de valeur ici par
rapport a une generation programmatique directe, plus simple a maintenir et sans
dependance a un outil de design externe. Si un besoin de mise en page tres avancee
(bandes repetees, sous-rapports complexes) apparait plus tard, il sera possible
d'ajouter JasperReports en complement sans tout reecrire.

**Note technique sur la webcam** : si aucune webcam n'est detectee sur le poste
(cas frequent en environnement de test/serveur), l'application affiche un message
et continue de fonctionner normalement avec le placeholder initiales sur le badge -
aucun blocage.

## 3. Compiler et lancer (double-clic)

Prerequis : JDK 21 et Maven installes, avec acces internet (telechargement des dependances
depuis Maven Central la premiere fois).

```bash
cd presence-manager
mvn clean package
```

Cela genere `target/presence-manager.jar` (jar unique, toutes dependances incluses grace
au plugin `maven-shade-plugin`).

### Lancer l'application

Trois methodes, de la plus simple au quotidien a la plus basique :

1. **Recommande** : double-cliquer une fois sur `installer/Creer-raccourci-bureau.vbs`
   pour creer un raccourci Bureau, puis utiliser ce raccourci au quotidien (voir
   section suivante "Windows a protege votre PC" pour le detail et pourquoi c'est
   la methode la plus fiable).
2. Double-clic sur `lancer.bat` (detecte automatiquement le jar dans `target/`
   ou a la racine).
3. En ligne de commande : `java -jar target/presence-manager.jar`

Compte par defaut cree automatiquement au premier lancement :
- Identifiant : `admin`
- Mot de passe : `Admin@2026`

**Important** : un assistant de configuration s'affiche automatiquement a la premiere
connexion avec ce compte et vous guide pour changer ce mot de passe et creer votre
premier site - vous n'avez rien a faire manuellement pour cela.

### "Windows a protege votre PC" en double-cliquant sur lancer.bat

C'est **normal et attendu**, ce n'est pas un virus. Windows marque automatiquement
tout fichier telecharge depuis Internet (ZIP, .bat, .exe...) et SmartScreen bloque
par prudence son execution, meme pour un logiciel parfaitement legitime et non
signe numeriquement. C'est aussi pour cela que ce blocage peut sembler "aleatoire" :
il depend de la configuration de securite du poste, d'une mise a jour Windows, etc.
Quatre solutions, de la plus rapide a la plus definitive :

1. **Solution recommandee pour l'usage quotidien a l'accueil** : double-cliquer
   une seule fois sur `installer/Creer-raccourci-bureau.vbs`. Il cree un vrai
   raccourci Windows sur le Bureau ("Strata Presence Manager") qui lance
   directement `javaw.exe` (deja approuve par Windows). **Ce raccourci ne
   declenche plus jamais SmartScreen** - c'est exactement la technique utilisee
   par les installateurs professionnels. A partir de la, l'agent d'accueil
   double-clique sur ce raccourci au quotidien, plus sur `lancer.bat`.
2. **Debloquer un seul fichier** : clic droit sur `lancer.bat` (ou sur le ZIP
   telecharge AVANT de l'extraire, ce qui est encore mieux) -> Proprietes ->
   cocher la case **"Debloquer"** en bas de l'onglet General -> Appliquer -> OK.
3. **Debloquer tout le projet en une fois** : clic droit sur
   `installer/unblock-files.ps1` -> "Executer avec PowerShell".
4. **Solution definitive pour une diffusion en entreprise** : utiliser
   l'installateur Windows signe numeriquement (voir section 4 et
   `installer/INSTALLER.md`, section "Signature du code") - un executable
   correctement signe ne declenche plus cet avertissement, meme au premier lancement.

## 4. Generer un executable natif Windows (.exe) + installateur

Voir le guide detaille **`installer/INSTALLER.md`** (prerequis WiX Toolset 3.11, toutes
les options jpackage expliquees, deploiement silencieux, signature de code, depannage).

Version rapide : sur une machine Windows avec JDK 21 + Maven + WiX 3.11 installes,
lancer simplement :

```bat
installer\build-installer.bat
```

Ce script compile le projet puis genere `installer\output\Strata Presence Manager-1.0.0.exe`.
L'utilisateur final double-clique dessus, suit Suivant / Suivant / Installer / Terminer,
et retrouve le logiciel dans le menu Demarrer avec un raccourci sur le Bureau — aucun JDK
a installer sur le poste cible (jpackage embarque un runtime Java minimal via jlink).

**A fournir avant la premiere build officielle** : un fichier `installer/icon.ico`
(icone de l'application, 256x256 recommande) — actuellement absent du depot.

## 5. Structure du projet

```
presence-manager/
  pom.xml
  src/main/java/com/strata/presence/
    Main.java
    model/        Person, User, AttendanceRecord, AuditLogEntry, enums
    dao/          DatabaseManager (singleton), PersonDao, AttendanceDao, UserDao, AuditDao
    service/      AttendanceService (regles anti-fraude), AuthService
    ui/           LoginFrame, MainFrame, DashboardPanel, CheckInPanel, HistoryPanel,
                  AuditPanel, UserAdminPanel
  src/main/resources/
    schema.sql    Script de creation automatique de la base (tables, index, donnees de reference)
    logback.xml   Configuration des logs (fichier logs/presence-manager.log)
```

## 6. Migration vers MySQL

Toute la couche DAO utilise du SQL standard (aucune syntaxe propre a SQLite). Pour migrer :
1. Ajouter la dependance `mysql-connector-j` au `pom.xml`
2. Dans `DatabaseManager.buildDataSource()`, remplacer l'URL JDBC SQLite par l'URL MySQL
   (exemple deja commente dans le fichier)
3. Adapter `schema.sql` si besoin (types `AUTOINCREMENT` -> `AUTO_INCREMENT`, etc.)

## 7. Securite de l'horodatage (regle anti-fraude)

Aucune methode de `AttendanceService` n'accepte de date/heure fournie par l'utilisateur pour
un enregistrement standard : `enregistrerArrivee()` et `enregistrerDepart()` appellent
systematiquement `LocalDate.now()` / `LocalTime.now()`. La seule exception est
`derogationSaisieManuelle()`, qui exige le role ADMIN/RESPONSABLE, une re-saisie du mot de
passe, une justification obligatoire, et journalise systematiquement l'operation dans la
table `audit_log`.
