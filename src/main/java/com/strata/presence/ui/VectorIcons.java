package com.strata.presence.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.function.BiConsumer;

/**
 * Icones vectorielles minimalistes (style "outline"), dessinees directement
 * en Java2D pour eviter toute dependance a un jeu d'icones externe (SVG/PNG)
 * indisponible hors-ligne. Toutes partagent la meme epaisseur de trait et la
 * meme couleur d'accent, pour une identite visuelle coherente dans toute
 * l'application (onglets, boutons, cartes du tableau de bord).
 */
public final class VectorIcons {

    public static final Color ACCENT = new Color(230, 126, 34);
    private static final int SIZE = 18;

    private VectorIcons() {}

    private static Icon build(BiConsumer<Graphics2D, Integer> painter) {
        return build(painter, ACCENT);
    }

    private static Icon build(BiConsumer<Graphics2D, Integer> painter, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(x, y);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(color);
                painter.accept(g2, SIZE);
                g2.dispose();
            }
            @Override public int getIconWidth() { return SIZE; }
            @Override public int getIconHeight() { return SIZE; }
        };
    }

    public static Icon dashboard() {
        return build((g, s) -> {
            g.drawRoundRect(1, 1, 7, 7, 2, 2);
            g.drawRoundRect(10, 1, 7, 4, 2, 2);
            g.drawRoundRect(10, 8, 7, 9, 2, 2);
            g.drawRoundRect(1, 11, 7, 6, 2, 2);
        });
    }

    public static Icon checkin() {
        return build((g, s) -> {
            g.draw(new Ellipse2D.Double(6, 1, 6, 6));
            g.drawLine(4, 17, 4, 13);
            g.drawArc(4, 8, 10, 10, 90, 180);
            g.drawLine(14, 5, 17, 8);
            g.drawLine(17, 8, 17, 3);
            g.drawLine(17, 8, 12, 8);
        });
    }

    public static Icon history() {
        return build((g, s) -> {
            g.draw(new Ellipse2D.Double(1, 1, 16, 16));
            g.drawLine(9, 5, 9, 9);
            g.drawLine(9, 9, 13, 11);
        });
    }

    public static Icon reports() {
        return build((g, s) -> {
            g.drawRoundRect(2, 1, 14, 16, 2, 2);
            g.drawLine(5, 6, 13, 6);
            g.drawLine(5, 9, 13, 9);
            g.drawLine(5, 12, 10, 12);
        });
    }

    public static Icon audit() {
        return build((g, s) -> {
            g.draw(new Ellipse2D.Double(1, 1, 10, 10));
            g.drawLine(6, 4, 6, 6);
            g.drawLine(6, 6, 8, 7);
            g.drawLine(13, 13, 17, 17);
        });
    }

    public static Icon users() {
        return build((g, s) -> {
            g.draw(new Ellipse2D.Double(2, 2, 6, 6));
            g.drawArc(0, 9, 10, 8, 0, 180);
            g.draw(new Ellipse2D.Double(11, 3, 5, 5));
            g.drawArc(9, 9, 9, 7, 0, 180);
        });
    }

    public static Icon sites() {
        return build((g, s) -> {
            g.drawRoundRect(2, 6, 6, 11, 1, 1);
            g.drawRoundRect(10, 2, 7, 15, 1, 1);
            g.drawLine(4, 9, 4, 9);
            g.drawLine(12, 5, 12, 5);
            g.drawLine(12, 8, 12, 8);
            g.drawLine(12, 11, 12, 11);
        });
    }

    public static Icon settings() {
        return build((g, s) -> {
            g.draw(new Ellipse2D.Double(5, 5, 8, 8));
            for (int i = 0; i < 8; i++) {
                double angle = Math.toRadians(i * 45);
                int x1 = (int) (9 + 6 * Math.cos(angle));
                int y1 = (int) (9 + 6 * Math.sin(angle));
                int x2 = (int) (9 + 8.5 * Math.cos(angle));
                int y2 = (int) (9 + 8.5 * Math.sin(angle));
                g.drawLine(x1, y1, x2, y2);
            }
        });
    }

    public static Icon badge() {
        return build((g, s) -> {
            g.drawRoundRect(2, 1, 14, 16, 3, 3);
            g.draw(new Ellipse2D.Double(6, 5, 6, 6));
            g.drawArc(4, 12, 10, 6, 0, 180);
        });
    }

    public static Icon camera() {
        return build((g, s) -> {
            g.drawRoundRect(1, 5, 16, 11, 2, 2);
            g.drawLine(6, 5, 8, 2);
            g.drawLine(8, 2, 12, 2);
            g.drawLine(12, 2, 14, 5);
            g.draw(new Ellipse2D.Double(6, 8, 6, 6));
        });
    }

    public static Icon logout() {
        return build((g, s) -> {
            g.drawRoundRect(2, 2, 8, 14, 2, 2);
            g.drawLine(9, 9, 17, 9);
            g.drawLine(14, 5, 17, 9);
            g.drawLine(14, 13, 17, 9);
        });
    }

    public static Icon print() {
        return build((g, s) -> {
            g.drawRoundRect(2, 6, 14, 7, 2, 2);
            g.drawRect(5, 1, 8, 5);
            g.drawRect(5, 13, 8, 4);
        });
    }

    public static Icon search() {
        return build((g, s) -> {
            g.draw(new Ellipse2D.Double(1, 1, 10, 10));
            g.drawLine(10, 10, 16, 16);
        });
    }

    public static Icon warning(Color color) {
        return build((g, s) -> {
            g.drawLine(9, 2, 1, 16);
            g.drawLine(1, 16, 17, 16);
            g.drawLine(17, 16, 9, 2);
            g.drawLine(9, 7, 9, 11);
            g.fillOval(8, 12, 2, 2);
        }, color);
    }

    /** Petit rectangle plein arrondi, utilise pour les pastilles de statut (present/parti). */
    public static Icon dot(Color color) {
        return build((g, s) -> g.fill(new RoundRectangle2D.Double(5, 5, 8, 8, 8, 8)), color);
    }
}
