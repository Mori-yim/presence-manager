package com.strata.presence.ui;

import com.strata.presence.dao.MotifDao;
import com.strata.presence.model.Motif;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Gestion des motifs de visite (utilises a l'enregistrement). Reserve au role ADMIN.
 */
public class MotifAdminPanel extends JPanel {

    private final MotifDao motifDao = new MotifDao();
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Motif"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    public MotifAdminPanel() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(new PanelHeader(VectorIcons.reports(), "Gestion des motifs de visite",
                "Utilises pour qualifier la raison de la visite a l'enregistrement"), BorderLayout.NORTH);

        TableStyler.style(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new MigLayout("insets 15", "[right][fill][]"));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JTextField libelleField = new JTextField(20);
        JButton createButton = new JButton("Ajouter le motif");
        createButton.putClientProperty("JButton.buttonType", "roundRect");
        JButton deleteButton = new JButton("Supprimer la selection");

        formPanel.add(new JLabel("Libelle :"));
        formPanel.add(libelleField);
        formPanel.add(createButton, "wrap");
        formPanel.add(deleteButton, "span 3, gaptop 5");

        createButton.addActionListener(e -> {
            String libelle = libelleField.getText().trim();
            if (libelle.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Le libelle du motif est obligatoire.");
                return;
            }
            motifDao.create(libelle);
            libelleField.setText("");
            reload();
            ToastNotification.show(SwingUtilities.getWindowAncestor(this),
                    "Motif '" + libelle + "' ajoute.", ToastNotification.Type.SUCCESS);
        });

        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Selectionnez un motif a supprimer.");
                return;
            }
            int id = (int) model.getValueAt(table.convertRowIndexToModel(row), 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Supprimer definitivement ce motif ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                motifDao.delete(id);
                reload();
            }
        });

        add(formPanel, BorderLayout.SOUTH);
        reload();
    }

    private void reload() {
        model.setRowCount(0);
        for (Motif m : motifDao.findAll()) {
            model.addRow(new Object[]{m.getId(), m.getLibelle()});
        }
    }
}
