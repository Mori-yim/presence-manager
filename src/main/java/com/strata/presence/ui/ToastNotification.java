package com.strata.presence.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Notification "toast" qui glisse depuis le bord inferieur droit de la fenetre
 * parente, reste affichee quelques secondes, puis disparait en fondu.
 * Utilisee pour les confirmations non bloquantes (badge imprime, rapport
 * exporte, site ajoute...) a la place d'une boite de dialogue JOptionPane
 * qui interrompt le flux de travail de l'agent d'accueil.
 */
public class ToastNotification {

    public enum Type {
        SUCCESS(new Color(30, 150, 80)),
        INFO(new Color(230, 126, 34)),
        ERROR(new Color(200, 60, 60));

        final Color color;
        Type(Color color) { this.color = color; }
    }

    public static void show(Window owner, String message, Type type) {
        if (owner == null) return;

        JWindow toast = new JWindow(owner);
        toast.setAlwaysOnTop(true);

        JPanel panel = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40, 40, 40, 235));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel dot = new JLabel("\u25CF");
        dot.setForeground(type.color);
        dot.setFont(dot.getFont().deriveFont(16f));

        JLabel text = new JLabel(message);
        text.setForeground(Color.WHITE);
        text.setFont(text.getFont().deriveFont(Font.PLAIN, 13f));

        panel.add(dot, BorderLayout.WEST);
        panel.add(text, BorderLayout.CENTER);

        toast.setContentPane(panel);
        toast.pack();
        toast.setSize(Math.min(380, toast.getWidth() + 20), toast.getHeight());

        Point ownerLoc = owner.getLocationOnScreen();
        int targetX = ownerLoc.x + owner.getWidth() - toast.getWidth() - 30;
        int targetY = ownerLoc.y + owner.getHeight() - toast.getHeight() - 40;
        int startY = targetY + 40;

        toast.setLocation(targetX, startY);
        try {
            toast.setOpacity(0f);
        } catch (Exception ignored) {
            // translucidite non supportee : le toast s'affichera simplement sans fondu
        }
        toast.setVisible(true);

        // Glissement + fondu d'entree
        Timer slideIn = new Timer(15, null);
        slideIn.addActionListener(new java.awt.event.ActionListener() {
            int step = 0;
            final int totalSteps = 15;
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                step++;
                float progress = step / (float) totalSteps;
                int y = (int) (startY - (startY - targetY) * progress);
                toast.setLocation(targetX, y);
                try { toast.setOpacity(Math.min(1f, progress)); } catch (Exception ignored) {}
                if (step >= totalSteps) {
                    slideIn.stop();
                    scheduleDismiss(toast);
                }
            }
        });
        slideIn.start();
    }

    private static void scheduleDismiss(JWindow toast) {
        Timer waitTimer = new Timer(2200, e -> fadeOutAndDispose(toast));
        waitTimer.setRepeats(false);
        waitTimer.start();
    }

    private static void fadeOutAndDispose(JWindow toast) {
        Timer fadeOut = new Timer(20, null);
        fadeOut.addActionListener(new java.awt.event.ActionListener() {
            float opacity = 1f;
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                opacity -= 0.08f;
                if (opacity <= 0f) {
                    fadeOut.stop();
                    toast.dispose();
                    return;
                }
                try { toast.setOpacity(opacity); } catch (Exception ignored) {}
            }
        });
        fadeOut.start();
    }
}
