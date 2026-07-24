package com.strata.presence.ui;

import com.strata.presence.dao.ServiceDao;
import com.strata.presence.model.Service;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Gestion des services (utilises pour classer les visiteurs/personnel a
 * l'enregistrement). Reserve au role ADMIN.
 */
public class ServiceAdminPanel extends JPanel {

    private final ServiceDao serviceDao = new ServiceDao();
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Nom du service"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    public ServiceAdminPanel() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(new PanelHeader(VectorIcons.sites(), "Gestion des services",
                "Utilises pour classer les visiteurs et le personnel a l'enregistrement"), BorderLayout.NORTH);

        TableStyler.style(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new MigLayout("insets 15", "[right][fill][]"));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JTextField nomField = new JTextField(20);
        JButton createButton = new JButton("Ajouter le service");
        createButton.putClientProperty("JButton.buttonType", "roundRect");
        JButton deleteButton = new JButton("Supprimer la selection");

        formPanel.add(new JLabel("Nom :"));
        formPanel.add(nomField);
        formPanel.add(createButton, "wrap");
        formPanel.add(deleteButton, "span 3, gaptop 5");

        createButton.addActionListener(e -> {
            String nom = nomField.getText().trim();
            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Le nom du service est obligatoire.");
                return;
            }
            serviceDao.create(nom);
            nomField.setText("");
            reload();
            ToastNotification.show(SwingUtilities.getWindowAncestor(this),
                    "Service '" + nom + "' ajoute.", ToastNotification.Type.SUCCESS);
        });

        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Selectionnez un service a supprimer.");
                return;
            }
            int id = (int) model.getValueAt(table.convertRowIndexToModel(row), 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Supprimer definitivement ce service ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                serviceDao.delete(id);
                reload();
            }
        });

        add(formPanel, BorderLayout.SOUTH);
        reload();
    }

    private void reload() {
        model.setRowCount(0);
        for (Service s : serviceDao.findAll()) {
            model.addRow(new Object[]{s.getId(), s.getNom()});
        }
    }
}
