' =====================================================================
' Strata Presence Manager - Creation d'un raccourci Bureau natif
' =====================================================================
'
' POURQUOI CE SCRIPT (et pourquoi il resout le probleme definitivement) :
' Le fichier lancer.bat est un SCRIPT (comme un .exe telecharge), et Windows
' SmartScreen bloque par prudence l'execution de TOUT script/executable
' telecharge depuis Internet et non signe numeriquement - ce comportement
' peut reapparaitre a chaque nouvelle machine, apres une mise a jour Windows,
' ou de facon inconsistante selon la configuration de securite du poste.
'
' Ce script cree, une seule fois, un RACCOURCI WINDOWS (.lnk) sur le Bureau
' qui pointe directement vers "javaw.exe" (le lanceur Java, deja present et
' deja approuve sur toutes les machines avec Java installe). Un raccourci
' vers un executable systeme deja approuve NE DECLENCHE PAS SmartScreen :
' c'est exactement la technique utilisee par les vrais installateurs
' Windows (menu Demarrer, raccourci Bureau).
'
' UTILISATION (a faire une seule fois par poste) :
' 1. Construire le projet au prealable : mvn clean package
' 2. Double-cliquer sur ce fichier (clic droit -> "Ouvrir" si besoin)
'    (si un avertissement apparait ici, une seule fois : "Informations
'    complementaires" -> "Executer quand meme", ou passer par
'    installer\unblock-files.ps1 avant)
' 3. Un raccourci "Strata Presence Manager" apparait sur le Bureau.
' 4. A partir de maintenant, DOUBLE-CLIQUER SUR CE RACCOURCI (pas sur
'    lancer.bat) : plus aucun avertissement Windows, a chaque lancement.

Dim shell, fso, scriptDir, projectDir, shortcut, jarPath, desktopPath

Set shell = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")

scriptDir = fso.GetParentFolderName(WScript.ScriptFullName)
projectDir = fso.GetParentFolderName(scriptDir)

If fso.FileExists(projectDir & "\target\presence-manager.jar") Then
    jarPath = "target\presence-manager.jar"
ElseIf fso.FileExists(projectDir & "\presence-manager.jar") Then
    jarPath = "presence-manager.jar"
Else
    MsgBox "Fichier presence-manager.jar introuvable." & vbCrLf & _
           "Veuillez d'abord executer : mvn clean package", vbExclamation, "Strata Presence Manager"
    WScript.Quit 1
End If

desktopPath = shell.SpecialFolders("Desktop")

Set shortcut = shell.CreateShortcut(desktopPath & "\Strata Presence Manager.lnk")
shortcut.TargetPath = "javaw.exe"
shortcut.Arguments = "-jar """ & jarPath & """"
shortcut.WorkingDirectory = projectDir
If fso.FileExists(projectDir & "\installer\icon.ico") Then
    shortcut.IconLocation = projectDir & "\installer\icon.ico"
End If
shortcut.Description = "Strata Presence Manager - Gestion des presences visiteurs et personnel"
shortcut.Save

MsgBox "Raccourci cree avec succes sur le Bureau : 'Strata Presence Manager'." & vbCrLf & vbCrLf & _
       "Utilisez desormais ce raccourci pour lancer l'application - " & _
       "plus aucun avertissement Windows ne devrait apparaitre.", vbInformation, "Strata Presence Manager"
