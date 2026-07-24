package com.strata.presence.ui;

import com.strata.presence.model.AttendanceRecord;
import com.strata.presence.model.Person;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

/**
 * Fenetre modale affichant le badge genere et permettant son impression directe.
 */
public class BadgeDialog extends JDialog {

    public BadgeDialog(Window owner, Person person, AttendanceRecord record) {
        super(owner, "Badge - " + person.getNomComplet(), ModalityType.APPLICATION_MODAL);
        setSize(460, 380);
        setLocationRelativeTo(owner);
        com.strata.presence.util.AppIcons.apply(this);

        BadgeCardPanel badgePanel = new BadgeCardPanel(person, record);
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        wrapper.add(badgePanel);

        JButton printButton = new JButton("Imprimer le badge", VectorIcons.print());
        printButton.putClientProperty("JButton.buttonType", "roundRect");
        printButton.addActionListener(e -> imprimer(badgePanel));

        JButton closeButton = new JButton("Fermer");
        closeButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttons.add(printButton);
        buttons.add(closeButton);

        setLayout(new BorderLayout());
        add(wrapper, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void imprimer(BadgeCardPanel badgePanel) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            badgePanel.printAll(g2);
            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            try {
                job.print();
                ToastNotification.show(this, "Badge envoye a l'imprimante.", ToastNotification.Type.SUCCESS);
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Erreur d'impression : " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
