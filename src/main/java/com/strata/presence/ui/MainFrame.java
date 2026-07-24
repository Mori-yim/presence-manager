package com.strata.presence.ui;

import com.strata.presence.model.RoleType;
import com.strata.presence.model.User;
import com.strata.presence.service.AuthService;
import com.strata.presence.util.AppIcons;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.format.DateTimeFormatter;

/**
 * Fenetre principale post-authentification. Organisation par onglets,
 * menu (Fichier / Affichage / Aide), et barre de statut inferieure
 * (utilisateur connecte, role, etat de la connexion base de donnees, horloge).
 */
public class MainFrame extends JFrame {

    private final User user;
    private final JLabel statusClock = new JLabel();

    public MainFrame(User user) {
        super("Strata Presence Manager - " + user.getFullName() + " (" + user.getRole().getLibelle() + ")");
        this.user = user;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1150, 760);
        setLocationRelativeTo(null);
        AppIcons.apply(this);

        setJMenuBar(buildMenuBar());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Tableau de bord", VectorIcons.dashboard(), new DashboardPanel());
        tabs.addTab("Enregistrement", VectorIcons.checkin(), new CheckInPanel(user));
        tabs.addTab("Historique", VectorIcons.history(), new HistoryPanel(user));
        tabs.addTab("Repertoire", VectorIcons.users(), new PersonDirectoryPanel());
        tabs.addTab("Rapports", VectorIcons.reports(), new ReportPanel());

        if (user.getRole() == RoleType.ADMIN || user.getRole() == RoleType.RESPONSABLE) {
            tabs.addTab("Journal d'audit", VectorIcons.audit(), new AuditPanel());
            tabs.addTab("Utilisateurs", VectorIcons.users(), new UserAdminPanel(user));
        }
        if (user.getRole() == RoleType.ADMIN) {
            tabs.addTab("Sites", VectorIcons.sites(), new SiteAdminPanel());
            tabs.addTab("Services", VectorIcons.sites(), new ServiceAdminPanel());
            tabs.addTab("Motifs", VectorIcons.reports(), new MotifAdminPanel());
        }
        tabs.addTab("Parametres", VectorIcons.settings(), new SettingsPanel());

        JPanel topBar = buildTopBar();

        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmerFermeture();
            }
        });
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JLabel welcome = new JLabel("Bienvenue, " + user.getFullName());
        welcome.setFont(welcome.getFont().deriveFont(Font.BOLD, 14f));

        JButton lockButton = new JButton("Verrouiller");
        lockButton.setToolTipText("Verrouiller la session (Ctrl+L) sans se deconnecter completement");
        lockButton.addActionListener(e -> verrouillerSession());

        JButton logoutButton = new JButton("Se deconnecter", VectorIcons.logout());
        logoutButton.setToolTipText("Fermer la session en cours et revenir a l'ecran de connexion");
        logoutButton.addActionListener(e -> deconnecter());

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtons.add(lockButton);
        rightButtons.add(logoutButton);

        topBar.add(welcome, BorderLayout.WEST);
        topBar.add(rightButtons, BorderLayout.EAST);
        return topBar;
    }

    private void verrouillerSession() {
        new LockScreenDialog(this, user, this::deconnecter).setVisible(true);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fichierMenu = new JMenu("Fichier");
        JMenuItem lockItem = new JMenuItem("Verrouiller la session");
        lockItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L,
                java.awt.event.InputEvent.CTRL_DOWN_MASK));
        lockItem.addActionListener(e -> verrouillerSession());
        JMenuItem logoutItem = new JMenuItem("Se deconnecter");
        logoutItem.addActionListener(e -> deconnecter());
        JMenuItem quitItem = new JMenuItem("Quitter");
        quitItem.addActionListener(e -> confirmerFermeture());
        fichierMenu.add(lockItem);
        fichierMenu.addSeparator();
        fichierMenu.add(logoutItem);
        fichierMenu.addSeparator();
        fichierMenu.add(quitItem);

        JMenu aideMenu = new JMenu("Aide");
        JMenuItem manuelItem = new JMenuItem("Manuel utilisateur");
        manuelItem.addActionListener(e -> afficherManuel());
        JMenuItem aproposItem = new JMenuItem("A propos de Strata Presence Manager");
        aproposItem.addActionListener(e -> afficherApropos());
        aideMenu.add(manuelItem);
        aideMenu.add(aproposItem);

        menuBar.add(fichierMenu);
        menuBar.add(aideMenu);
        return menuBar;
    }

    private JPanel buildStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 225, 225)),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));

        JLabel userInfo = new JLabel(user.getFullName() + "  -  " + user.getRole().getLibelle());
        userInfo.setFont(userInfo.getFont().deriveFont(11f));

        JLabel dbStatus = new JLabel("● Base de donnees locale connectee (SQLite)");
        dbStatus.setForeground(new Color(30, 150, 80));
        dbStatus.setFont(dbStatus.getFont().deriveFont(11f));

        statusClock.setFont(statusClock.getFont().deriveFont(11f));
        statusClock.setForeground(Color.GRAY);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(userInfo);
        leftPanel.add(dbStatus);

        statusBar.add(leftPanel, BorderLayout.WEST);
        statusBar.add(statusClock, BorderLayout.EAST);

        Timer clockTimer = new Timer(1000, e ->
                statusClock.setText(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
        clockTimer.start();

        return statusBar;
    }

    private void deconnecter() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment vous deconnecter ?", "Deconnexion",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.getInstance().logout();
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                com.strata.presence.util.AnimationUtils.fadeIn(loginFrame, 250);
            });
        }
    }

    private void confirmerFermeture() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment quitter Strata Presence Manager ?", "Confirmation",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    private void afficherApropos() {
        JOptionPane.showMessageDialog(this,
                "Strata Presence Manager\nVersion 1.0.0\n\n"
                        + "Logiciel de gestion des presences visiteurs et personnel.\n"
                        + "Developpe pour Strata Agency.\n\n"
                        + "Copyright (c) 2026 Strata Agency. Tous droits reserves.",
                "A propos", JOptionPane.INFORMATION_MESSAGE);
    }

    private void afficherManuel() {
        JOptionPane.showMessageDialog(this,
                "Manuel utilisateur (extrait) :\n\n"
                        + "1. Onglet Enregistrement : saisir les informations et enregistrer l'arrivee\n"
                        + "   (l'heure est generee automatiquement par le systeme).\n"
                        + "2. Bouton 'Prendre une photo' : capture webcam pour le badge.\n"
                        + "3. Bouton 'Imprimer le badge' : apercu et impression du badge visiteur/personnel.\n"
                        + "4. Onglet Rapports : generation et export PDF / Excel / CSV par periode.\n"
                        + "5. Mode derogation (ADMIN/RESPONSABLE) : correction manuelle d'une heure d'arrivee,\n"
                        + "   toujours protegee par mot de passe et tracee dans le journal d'audit.\n\n"
                        + "Le manuel complet est disponible dans le fichier README.md du projet.",
                "Manuel utilisateur", JOptionPane.INFORMATION_MESSAGE);
    }
}
