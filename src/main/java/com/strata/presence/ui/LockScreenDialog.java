package com.strata.presence.ui;

import com.strata.presence.model.User;
import com.strata.presence.util.AppIcons;
import com.strata.presence.util.PasswordUtil;
import com.strata.presence.util.SoundUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Ecran de verrouillage : protege un poste d'accueil partage sans avoir a se
 * deconnecter completement (l'agent garde son contexte de travail). Il faut
 * ressaisir le mot de passe de l'utilisateur courant pour deverrouiller, ou
 * choisir de se deconnecter completement a la place.
 */
public class LockScreenDialog extends JDialog {

    public LockScreenDialog(Window owner, User currentUser, Runnable onLogoutRequested) {
        super(owner, "Session verrouillee", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setSize(owner.getWidth(), owner.getHeight());
        setLocation(owner.getLocationOnScreen());
        AppIcons.apply(this);

        JPanel background = new JPanel(new GridBagLayout()) {
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
        card.setPreferredSize(new Dimension(360, 320));

        Image icon = AppIcons.getAppIcon();
        JLabel iconLabel = new JLabel();
        if (icon != null) {
            iconLabel.setIcon(new ImageIcon(icon.getScaledInstance(56, 56, Image.SCALE_SMOOTH)));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }

        JLabel title = new JLabel("Session verrouillee", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        JLabel subtitle = new JLabel(currentUser.getFullName(), SwingConstants.CENTER);
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));
        subtitle.setForeground(new Color(130, 130, 130));

        card.add(iconLabel, "gapbottom 6, align center");
        card.add(title, "gapbottom 0, align center");
        card.add(subtitle, "gapbottom 20, align center");

        JPasswordField passwordField = new JPasswordField();
        JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(200, 50, 50));
        statusLabel.setFont(statusLabel.getFont().deriveFont(11f));

        card.add(new JLabel("Mot de passe pour deverrouiller :"));
        card.add(passwordField);

        JButton unlockButton = new JButton("Deverrouiller");
        unlockButton.putClientProperty("JButton.buttonType", "roundRect");
        Runnable tenterDeverrouillage = () -> {
            String saisi = new String(passwordField.getPassword());
            if (PasswordUtil.verify(saisi, currentUser.getSalt(), currentUser.getPasswordHash())) {
                dispose();
            } else {
                SoundUtils.playError();
                statusLabel.setText("Mot de passe incorrect.");
                passwordField.setText("");
            }
        };
        unlockButton.addActionListener(e -> tenterDeverrouillage.run());
        passwordField.addActionListener(e -> tenterDeverrouillage.run());

        card.add(unlockButton, "gaptop 10, height 38");
        card.add(statusLabel);

        JButton logoutButton = new JButton("Se deconnecter completement");
        logoutButton.addActionListener(e -> {
            dispose();
            onLogoutRequested.run();
        });
        card.add(logoutButton, "gaptop 10, height 30");

        background.add(card);
        setContentPane(background);
    }
}
