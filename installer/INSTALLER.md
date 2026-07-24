# Génération de l'installateur Windows (Setup.exe) avec jpackage

Ce document détaille la procédure complète pour transformer `presence-manager.jar`
en un installateur Windows professionnel : `Suivant` → `Suivant` → `Installer` → `Terminer`,
avec raccourci Bureau et entrée dans le menu Démarrer, sans que l'utilisateur final
ait besoin d'installer un JDK.

## 1. Prérequis (à installer sur la machine Windows qui construit l'installateur)

| Outil | Version | Pourquoi |
|---|---|---|
| JDK | 21 (celui utilisé pour compiler le projet) | `jpackage` est fourni dedans, aucune install séparée |
| Maven | 3.9+ | pour produire le jar via `mvn clean package` |
| **WiX Toolset** | **3.11.x** (pas 4.x, pas 5.x) | seul compatible avec `jpackage` du JDK 21 aujourd'hui |
| Icône `.ico` | 256x256 recommandé | logo affiché dans le raccourci / menu Démarrer |

Attention : générer l'installateur `.exe`/`.msi` doit se faire **sur Windows** —
`jpackage` ne peut pas produire un package Windows depuis Linux/macOS. Le reste
du projet (code, compilation `mvn package`) peut se faire n'importe où ; seule
cette étape finale demande une machine Windows.

### Installer WiX 3.11

1. Télécharger `wix311.exe` depuis https://github.com/wixtoolset/wix3/releases (release "wix311")
2. Lancer l'installeur (il ajoute lui-même WiX au `PATH`)
3. Vérifier dans une invite de commandes :
   ```
   candle.exe -?
   light.exe -?
   ```
   Si ces commandes répondent, WiX est correctement installé.

Si WiX 4/5 est déjà installé sur la machine et entre en conflit, désinstalle-le ou
assure-toi que le WiX 3.11 est bien celui trouvé en premier dans le `PATH`.

## 2. Étape 1 : construire le jar

```bash
cd presence-manager
mvn clean package
```

Résultat : `target/presence-manager.jar` (jar unique avec toutes les dépendances,
grâce au plugin `maven-shade-plugin` déjà configuré dans le `pom.xml`).

## 3. Étape 2 : construire l'installateur avec jpackage

Commande complète (à exécuter dans une invite de commandes Windows, depuis la racine
du projet) :

```bat
jpackage ^
  --input target ^
  --dest installer\output ^
  --name "Strata Presence Manager" ^
  --main-jar presence-manager.jar ^
  --main-class com.strata.presence.Main ^
  --type exe ^
  --app-version 1.0.0 ^
  --vendor "Strata Agency" ^
  --description "Gestion des presences visiteurs et personnel" ^
  --icon installer\icon.ico ^
  --win-menu ^
  --win-menu-group "Strata Agency" ^
  --win-shortcut ^
  --win-shortcut-prompt ^
  --win-dir-chooser ^
  --win-per-user-install ^
  --win-upgrade-uuid 6f2b6f2a-6b3e-4b7a-9d1a-9c8e2f6a7b10 ^
  --license-file installer\LICENSE.txt ^
  --copyright "Copyright (c) 2026 Strata Agency" ^
  --java-options "-Dfile.encoding=UTF-8"
```

### Explication des options importantes

| Option | Rôle |
|---|---|
| `--input target` | dossier contenant le jar à empaqueter |
| `--dest` | dossier de sortie du fichier `.exe` final |
| `--type exe` | produit un installateur `.exe` (mettre `msi` pour un `.msi`, ou `app-image` pour juste un dossier applicatif sans installateur) |
| `--main-jar` / `--main-class` | point d'entrée de l'application |
| `--icon` | icône `.ico` utilisée pour le raccourci et le menu Démarrer |
| `--win-menu` / `--win-menu-group` | crée une entrée dans le menu Démarrer, rangée dans le groupe "Strata Agency" |
| `--win-shortcut` | crée le raccourci sur le Bureau |
| `--win-shortcut-prompt` | laisse l'utilisateur cocher/décocher "créer un raccourci" pendant l'installation (ajoute l'écran correspondant à l'assistant) |
| `--win-dir-chooser` | ajoute l'écran "choisir le dossier d'installation" |
| `--win-per-user-install` | installation dans le profil utilisateur (pas besoin de droits admin) ; **enlever cette ligne** si tu veux une installation machine (tous les utilisateurs), auquel cas l'installateur demandera l'élévation UAC |
| `--win-upgrade-uuid` | **très important** : identifiant fixe et unique à ton application. Il faut le garder identique à chaque nouvelle version pour que l'installateur Windows sache qu'il doit **mettre à jour** l'existant plutôt que d'en installer une seconde copie en parallèle. Génère le tien une seule fois (ex: via `powershell -Command "[guid]::NewGuid()"`) et ne le change plus jamais. |
| `--app-version` | version affichée dans "Applications installées" ; incrémenter à chaque nouvelle build |
| `--license-file` | fichier texte affiché dans l'écran "Accepter la licence" de l'assistant |

Résultat : `installer\output\Strata Presence Manager-1.0.0.exe`.
Double-clic dessus → assistant standard Windows : Suivant / Suivant / Installer / Terminer.
Le runtime Java est **embarqué** dans le package (via jlink en interne) : le poste
utilisateur n'a besoin de rien installer d'autre.

## 4. Déploiement silencieux (optionnel, utile en entreprise/parc informatique)

L'installateur généré par jpackage (WiX) supporte les paramètres MSI standards :

```bat
"Strata Presence Manager-1.0.0.exe" /quiet /norestart
```

Pratique pour un déploiement via GPO ou un outil de gestion de parc (SCCM, Intune...).

## 5. Signature du code (recommandé en environnement bancaire/administratif)

Sans signature, Windows SmartScreen affichera un avertissement "Éditeur inconnu"
au premier lancement. Pour l'éviter, signer l'exécutable avec un certificat de
signature de code (Authenticode), via `signtool` (fourni avec le Windows SDK) :

```bat
signtool sign /f certificat.pfx /p motdepasse /tr http://timestamp.digicert.com /td sha256 /fd sha256 "Strata Presence Manager-1.0.0.exe"
```

Le certificat s'obtient auprès d'une autorité (DigiCert, Sectigo, etc.) — hors
périmètre de ce projet, à prévoir avec la DSI de l'entreprise cliente si le logiciel
est distribué à grande échelle.

## 6. Erreurs fréquentes

| Erreur | Cause | Solution |
|---|---|---|
| `Cannot find WiX tool (light.exe, candle.exe)` | WiX absent ou version 4/5 installée | Installer WiX **3.11** et vérifier le `PATH` |
| L'installateur crée une deuxième entrée au lieu de mettre à jour | `--win-upgrade-uuid` a changé entre deux versions | Toujours garder le même UUID |
| Icône non prise en compte | Fichier autre que `.ico`, ou mauvaise résolution | Fournir un vrai `.ico` multi-résolutions (16x16 à 256x256) |
| SmartScreen bloque le lancement | Exécutable non signé | Voir section 5, ou cliquer "Informations complémentaires > Exécuter quand même" en interne |
