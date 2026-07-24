# =====================================================================
# Strata Presence Manager - Deblocage des fichiers (Windows SmartScreen)
# =====================================================================
#
# Pourquoi ce script ?
# Quand vous telechargez le projet (ZIP) depuis un navigateur, Windows
# marque automatiquement tous les fichiers extraits comme provenant
# d'Internet ("Mark of the Web"). Windows Defender SmartScreen bloque
# alors leur execution avec le message :
#   "Windows a protege votre PC - SmartScreen a empeche le demarrage
#    d'une application non reconnue."
# Ce n'est PAS un virus : c'est un comportement standard de Windows pour
# TOUT fichier .bat/.exe/.ps1 telecharge, y compris des logiciels
# parfaitement legitimes non signes numeriquement.
#
# Ce script supprime cette marque sur l'ensemble des fichiers du dossier
# du projet (recursivement), en une seule fois.
#
# UTILISATION :
# 1. Clic droit sur ce fichier -> "Executer avec PowerShell"
#    (si un avertissement apparait, cliquer sur "Informations
#    complementaires" puis "Executer quand meme" - une seule fois,
#    car ce script lui-meme est aussi telecharge depuis Internet)
# 2. Une fois termine, double-cliquer normalement sur lancer.bat
#    ou sur presence-manager.jar : le blocage ne doit plus apparaitre.
#
# Alternative en une ligne (invite PowerShell, depuis le dossier du projet) :
#   Get-ChildItem -Path . -Recurse | Unblock-File

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $root

Write-Host "Deblocage des fichiers du projet : $projectRoot" -ForegroundColor Cyan

Get-ChildItem -Path $projectRoot -Recurse -File | ForEach-Object {
    try {
        Unblock-File -Path $_.FullName -ErrorAction SilentlyContinue
    } catch {
        # Ignorer les fichiers deja debloques ou verrouilles
    }
}

Write-Host ""
Write-Host "Termine. Vous pouvez maintenant lancer lancer.bat ou presence-manager.jar normalement." -ForegroundColor Green
Write-Host ""
Read-Host "Appuyez sur Entree pour fermer"
