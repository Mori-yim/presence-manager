@echo off
title Strata Presence Manager
rem Si Windows affiche "Windows a protege votre PC" en double-cliquant sur ce
rem fichier : voir installer\Creer-raccourci-bureau.vbs (solution recommandee,
rem une seule fois) ou installer\unblock-files.ps1, ou la section correspondante
rem dans README.md. Ce n'est pas un virus : c'est le comportement standard de
rem Windows pour les fichiers .bat telecharges depuis Internet (SmartScreen).
cd /d "%~dp0"
if exist "target\presence-manager.jar" (
  java -jar "target\presence-manager.jar"
) else if exist "presence-manager.jar" (
  java -jar "presence-manager.jar"
) else (
  echo Fichier presence-manager.jar introuvable.
  echo Avez-vous bien execute "mvn clean package" au prealable ?
  pause
  exit /b 1
)
if errorlevel 1 (
  echo.
  echo Une erreur est survenue. Verifiez qu'un JRE/JDK 21 est installe.
  pause
)
