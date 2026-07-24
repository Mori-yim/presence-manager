package com.strata.presence.ui;

import com.strata.presence.dao.AuditDao;
import com.strata.presence.model.AuditLogEntry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Consultation du journal d'audit : toute derogation de saisie manuelle d'heure
 * y figure de maniere immuable (qui, quand, ancienne/nouvelle valeur, justification).
 */
public class AuditPanel extends JPanel {

    public AuditPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Date/Heure action", "Utilisateur", "Champ modifie", "Ancienne valeur",
                        "Nouvelle valeur", "Justification"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        for (AuditLogEntry entry : new AuditDao().findAll()) {
            model.addRow(new Object[]{
                    entry.getDateAction(), entry.getUserFullName(), entry.getChampModifie(),
                    entry.getAncienneValeur(), entry.getNouvelleValeur(), entry.getJustification()
            });
        }

        JTable table = new JTable(model);
        TableStyler.style(table);
        add(new PanelHeader(VectorIcons.audit(), "Journal d'audit",
                "Toutes les derogations horaires (saisie manuelle) sont tracees ici de maniere immuable"),
                BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
}
