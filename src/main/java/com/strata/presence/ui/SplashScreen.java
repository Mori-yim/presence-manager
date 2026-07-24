package com.strata.presence.ui;

import com.strata.presence.util.AppIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Ecran de demarrage affiche pendant l'initialisation de l'application
 * (creation/verification de la base de donnees, etc.). Ameliore la
 * perception de qualite professionnelle au lancement, en particulier
 * si le premier demarrage est un peu plus long (creation du schema).
 */
public class SplashScreen extends JWindow {

    private final JLabel statusLabel = new JLabel("Initialisation...", SwingConstants.CENTER);
    private final JProgressBar progressBar = new JProgressBar();

    public SplashScreen() {
        setSize(420, 260);
        setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(210, 100, 20), 0, getHeight(), new Color(245, 150, 60));
                g2.setPaint(gradient);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));
                g2.dispose();
            }
        };
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));

        Image icon = AppIcons.getAppIcon();
        JLabel iconLabel = new JLabel();
        if (icon != null) {
            iconLabel.setIcon(new ImageIcon(icon.getScaledInstance(72, 72, Image.SCALE_SMOOTH)));
        }
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel titleLabel = new JLabel("STRATA PRESENCE MANAGER", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));

        JLabel subtitleLabel = new JLabel("Gestion des presences - Strata Agency", SwingConstants.CENTER);
        subtitleLabel.setForeground(new Color(230, 235, 250));
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 11f));

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(iconLabel);
        centerPanel.add(Box.createVerticalStrut(12));
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(4));
        centerPanel.add(subtitleLabel);

        progressBar.setIndeterminate(true);
        statusLabel.setForeground(new Color(230, 235, 250));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 10f));

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 6));
        bottomPanel.setOpaque(false);
        bottomPanel.add(progressBar, BorderLayout.NORTH);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        content.add(centerPanel, BorderLayout.CENTER);
        content.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(content);
    }

    public void updateStatus(String text) {
        statusLabel.setText(text);
    }
}
