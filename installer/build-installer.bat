@echo off
setlocal

echo ============================================
echo  Strata Presence Manager - Build installateur
echo ============================================

echo.
echo [1/2] Compilation du projet (mvn clean package)...
call mvn clean package
if errorlevel 1 (
    echo ERREUR : la compilation Maven a echoue.
    pause
    exit /b 1
)

echo.
echo [2/2] Generation de l'installateur Windows (jpackage)...
if not exist installer\output mkdir installer\output

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
  --copyright "Copyright (c) 2026 Strata Agency"

if errorlevel 1 (
    echo ERREUR : jpackage a echoue. Verifiez que WiX Toolset 3.11 est installe et dans le PATH.
    pause
    exit /b 1
)

echo.
echo Termine ! Installateur genere dans installer\output\
pause
