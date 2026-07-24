package com.strata.presence.ui;

import com.strata.presence.model.User;
import com.strata.presence.service.AuthService;
import com.strata.presence.util.AppIcons;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Optional;

/**
 * Ecran d'authentification. Fond degrade assorti a l'ecran de demarrage,
 * carte blanche centrale avec ombre legere - identite visuelle premium
 * coherente avec le reste de l'application.
 */
public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final JLabel statusLabel = new JLabel(" ");

    public LoginFrame() {
        super("Strata Presence Manager - Connexion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        AppIcons.apply(this);

        JPanel gradientBackground = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(210, 100, 20), 0, getHeight(), new Color(245, 150, 60));
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        JPanel card = new JPanel(new MigLayout("wrap 1, insets 34 34 26 34, gap 10", "[grow, fill]")) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 35));
                g2.fill(new RoundRectangle2D.Double(4, 6, getWidth() - 8, getHeight() - 8, 22, 22));
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 8, getHeight() - 8, 22, 22));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(360, 440));

        Image icon = AppIcons.getAppIcon();
        JLabel iconLabel = new JLabel();
        if (icon != null) {
            iconLabel.setIcon(new ImageIcon(icon.getScaledInstance(56, 56, Image.SCALE_SMOOTH)));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }

        JLabel title = new JLabel("Strata Agency", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        JLabel subtitle = new JLabel("Gestion des presences - Visiteurs & Personnel", SwingConstants.CENTER);
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 11f));
        subtitle.setForeground(new Color(130, 130, 130));

        card.add(iconLabel, "gapbottom 6, align center");
        card.add(title, "gapbottom 0, align center");
        card.add(subtitle, "gapbottom 20, align center");

        card.add(new JLabel("Identifiant"));
        card.add(usernameField);
        card.add(new JLabel("Mot de passe"));
        card.add(passwordField);

        JButton loginButton = new JButton("Se connecter");
        loginButton.putClientProperty("JButton.buttonType", "roundRect");
        loginButton.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());

        statusLabel.setForeground(new Color(200, 50, 50));
        statusLabel.setFont(statusLabel.getFont().deriveFont(11f));

        card.add(loginButton, "gaptop 14, height 38");
        card.add(statusLabel);

        JLabel hint = new JLabel("Compte par defaut : admin / Admin@2026", SwingConstants.CENTER);
        hint.setFont(hint.getFont().deriveFont(Font.ITALIC, 10f));
        hint.setForeground(new Color(160, 160, 160));
        card.add(hint, "gaptop 8, align center");

        gradientBackground.add(card);
        setContentPane(gradientBackground);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        usernameField.putClientProperty("JComponent.outline", null);
        passwordField.putClientProperty("JComponent.outline", null);

        if (username.isEmpty() || password.isEmpty()) {
            if (username.isEmpty()) usernameField.putClientProperty("JComponent.outline", "error");
            if (password.isEmpty()) passwordField.putClientProperty("JComponent.outline", "error");
            statusLabel.setText("Veuillez renseigner l'identifiant et le mot de passe.");
            return;
        }

        Optional<User> userOpt = AuthService.getInstance().login(username, password);
        if (userOpt.isEmpty()) {
            usernameField.putClientProperty("JComponent.outline", "error");
            passwordField.putClientProperty("JComponent.outline", "error");
            statusLabel.setText("Identifiant ou mot de passe incorrect.");
            passwordField.setText("");
            com.strata.presence.util.SoundUtils.playError();
            return;
        }

        dispose();
        SwingUtilities.invokeLater(() -> {
            User user = userOpt.get();
            MainFrame mainFrame = new MainFrame(user);

            boolean wizardTermine = new com.strata.presence.dao.AppSettingsDao()
                    .getBoolean(SetupWizardDialog.SETTING_KEY, false);
            if (!wizardTermine && user.getRole() == com.strata.presence.model.RoleType.ADMIN) {
                mainFrame.setVisible(true);
                com.strata.presence.util.AnimationUtils.fadeIn(mainFrame, 250);
                new SetupWizardDialog(mainFrame, user).setVisible(true);
            } else {
                mainFrame.setVisible(true);
                com.strata.presence.util.AnimationUtils.fadeIn(mainFrame, 250);
            }
        });
    }
}
