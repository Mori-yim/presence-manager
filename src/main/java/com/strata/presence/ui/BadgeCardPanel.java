package com.strata.presence.ui;

import com.strata.presence.model.AttendanceRecord;
import com.strata.presence.model.Person;
import com.strata.presence.util.QrCodeGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Rendu visuel d'un badge professionnel (visiteur ou personnel) :
 * logo, nom, photo (placeholder tant que le module webcam n'est pas branche),
 * QR Code, service, date, heure et statut. Taille fixe adaptee a une impression
 * au format carte (85mm x 54mm, agrandie a l'ecran pour la lisibilite).
 */
public class BadgeCardPanel extends JPanel {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 250;
    private static final Color ACCENT = new Color(230, 126, 34);
    private static final Color PRESENT_GREEN = new Color(30, 150, 80);
    private static final Color DEPART_GRAY = new Color(140, 140, 140);

    private final Person person;
    private final AttendanceRecord record;

    public BadgeCardPanel(Person person, AttendanceRecord record) {
        this.person = person;
        this.record = record;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fond de la carte avec coins arrondis + ombre legere
        g2.setColor(new Color(0, 0, 0, 25));
        g2.fill(new RoundRectangle2D.Double(4, 6, WIDTH - 8, HEIGHT - 8, 20, 20));
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Double(2, 2, WIDTH - 8, HEIGHT - 8, 20, 20));

        // Bandeau superieur (couleur d'accent)
        g2.setColor(ACCENT);
        g2.fill(new RoundRectangle2D.Double(2, 2, WIDTH - 8, 46, 20, 20));
        g2.fillRect(2, 28, WIDTH - 8, 20);

        g2.setColor(Color.WHITE);
        g2.setFont(getFont().deriveFont(Font.BOLD, 16f));
        g2.drawString("STRATA AGENCY", 16, 30);

        g2.setFont(getFont().deriveFont(Font.PLAIN, 10f));
        g2.drawString("Badge visiteur / employe / stagiaire", 16, 44);

        // Photo (webcam) si disponible, sinon placeholder avec initiales
        int photoSize = 70;
        int photoX = 16, photoY = 60;
        BufferedImage photo = person.getPhotoPath() != null
                ? com.strata.presence.util.PhotoStorage.load(person.getPhotoPath())
                : null;

        if (photo != null) {
            g2.setClip(new java.awt.geom.Ellipse2D.Double(photoX, photoY, photoSize, photoSize));
            g2.drawImage(photo, photoX, photoY, photoSize, photoSize, null);
            g2.setClip(null);
        } else {
            g2.setColor(new Color(250, 235, 220));
            g2.fillOval(photoX, photoY, photoSize, photoSize);
            g2.setColor(ACCENT);
            g2.setFont(getFont().deriveFont(Font.BOLD, 24f));
            String initials = "" + safeChar(person.getPrenom()) + safeChar(person.getNom());
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(initials);
            g2.drawString(initials, photoX + (photoSize - textW) / 2, photoY + photoSize / 2 + 8);
        }

        // Informations texte
        int textX = photoX + photoSize + 16;
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(getFont().deriveFont(Font.BOLD, 15f));
        g2.drawString(person.getNomComplet(), textX, 82);

        g2.setFont(getFont().deriveFont(Font.PLAIN, 11f));
        g2.setColor(Color.GRAY);
        int y = 100;
        g2.drawString("Type : " + person.getType(), textX, y); y += 16;
        if (person.getEntreprise() != null && !person.getEntreprise().isBlank()) {
            g2.drawString("Service/Societe : " + person.getEntreprise(), textX, y); y += 16;
        }
        g2.drawString("Date : " + record.getDateJour(), textX, y); y += 16;
        g2.drawString("Heure d'arrivee : " + record.getHeureArrivee().withNano(0), textX, y); y += 16;

        // Pastille de statut
        boolean present = "PRESENT".equals(record.getStatut());
        g2.setColor(present ? PRESENT_GREEN : DEPART_GRAY);
        g2.fillRoundRect(textX, y, 90, 20, 10, 10);
        g2.setColor(Color.WHITE);
        g2.setFont(getFont().deriveFont(Font.BOLD, 11f));
        g2.drawString(present ? "PRESENT" : "PARTI", textX + 12, y + 14);

        // QR Code - pointe vers la conversation WhatsApp du numero de telephone si disponible,
        // sinon vers l'identifiant unique de la personne (secours si pas de telephone saisi)
        String whatsappLink = com.strata.presence.util.WhatsAppUtils.buildWhatsAppLink(person.getTelephone());
        String qrContent = whatsappLink != null ? whatsappLink
                : (person.getQrCode() != null ? person.getQrCode() : String.valueOf(person.getId()));
        BufferedImage qr = QrCodeGenerator.generate(qrContent, 90);
        g2.drawImage(qr, WIDTH - 106, HEIGHT - 106, null);

        if (whatsappLink != null) {
            g2.setColor(Color.GRAY);
            g2.setFont(getFont().deriveFont(Font.PLAIN, 8f));
            g2.drawString("Scanner : WhatsApp", WIDTH - 106, HEIGHT - 10);
        }

        g2.dispose();
    }

    private char safeChar(String s) {
        return (s != null && !s.isBlank()) ? Character.toUpperCase(s.charAt(0)) : '?';
    }
}
