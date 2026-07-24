package com.strata.presence;

import com.formdev.flatlaf.FlatLightLaf;
import com.strata.presence.dao.DatabaseManager;
import com.strata.presence.service.AuthService;
import com.strata.presence.ui.LoginFrame;
import com.strata.presence.ui.SplashScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Point d'entree de Strata Presence Manager.
 * Lancement : double-clic sur presence-manager.jar (ou via le raccourci Windows cree par l'installateur).
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        installGlobalExceptionHandler();

        FlatLightLaf.setup();
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("TabbedPane.showTabSeparators", true);

        SplashScreen splash = new SplashScreen();
        splash.setVisible(true);

        // L'initialisation (I/O disque, creation eventuelle du schema) se fait hors de l'EDT
        // pour que le splash screen s'affiche et s'anime immediatement.
        SwingWorker<Void, String> initWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    publish("Verification de la base de donnees...");
                    DatabaseManager.getInstance();
                    publish("Verification des comptes utilisateurs...");
                    AuthService.getInstance().ensureDefaultAdmin();
                    log.info("Strata Presence Manager demarre.");
                } catch (Exception e) {
                    log.error("Erreur fatale au demarrage", e);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null,
                                "Impossible de demarrer l'application :\n" + e.getMessage(),
                                "Erreur critique", JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    });
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                if (!chunks.isEmpty()) {
                    splash.updateStatus(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                splash.dispose();
                demarrerSauvegardeAutomatique();
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                com.strata.presence.util.AnimationUtils.fadeIn(loginFrame, 250);
            }
        };
        initWorker.execute();
    }

    private static void demarrerSauvegardeAutomatique() {
        Timer backupTimer = new Timer(true);
        backupTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                DatabaseManager.getInstance().backupNow();
            }
        }, 60_000L, 30 * 60_000L);
    }

    /**
     * Capture toute exception non geree survenant sur l'Event Dispatch Thread (Swing) ou
     * dans un thread applicatif, afin d'eviter qu'un utilisateur final ne voie une trace
     * d'erreur brute (Java) ou un plantage silencieux. L'erreur est journalisee (logs/)
     * et un message clair, professionnel, est affiche.
     */
    private static void installGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            log.error("Erreur non geree dans le thread {}", thread.getName(), throwable);
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "Une erreur inattendue est survenue.\n\n"
                            + "Details techniques : " + throwable.getMessage() + "\n\n"
                            + "L'incident a ete enregistre dans le journal de l'application ("
                            + LocalDateTime.now().withNano(0) + ").\n"
                            + "Si le probleme persiste, contactez votre administrateur systeme.",
                    "Strata Presence Manager - Erreur", JOptionPane.ERROR_MESSAGE));
        });

        System.setProperty("sun.awt.exception.handler", GlobalAwtExceptionHandler.class.getName());
    }

    /** Necessaire en complement du handler ci-dessus pour intercepter les exceptions de l'EDT Swing. */
    public static class GlobalAwtExceptionHandler {
        public void handle(Throwable throwable) {
            log.error("Erreur non geree sur l'Event Dispatch Thread", throwable);
            JOptionPane.showMessageDialog(null,
                    "Une erreur inattendue est survenue.\n\nDetails techniques : " + throwable.getMessage()
                            + "\n\nL'incident a ete enregistre dans le journal de l'application.",
                    "Strata Presence Manager - Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
