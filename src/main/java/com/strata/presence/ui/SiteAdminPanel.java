package com.strata.presence.ui;

import com.strata.presence.dao.SiteDao;
import com.strata.presence.model.Site;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Gestion des sites (multi-site) : creation, suppression. Reserve au role ADMIN.
 * Chaque presence est rattachee a un site (voir table presences.site_id).
 */
public class SiteAdminPanel extends JPanel {

    private final SiteDao siteDao = new SiteDao();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Nom du site", "Adresse"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    public SiteAdminPanel() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(new PanelHeader(VectorIcons.sites(), "Gestion multi-site",
                "Chaque presence enregistree est rattachee a un site"), BorderLayout.NORTH);

        TableStyler.style(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new MigLayout("insets 15", "[right][fill][right][fill][]"));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JTextField nomField = new JTextField(15);
        JTextField adresseField = new JTextField(25);
        JButton createButton = new JButton("Ajouter le site");
        createButton.putClientProperty("JButton.buttonType", "roundRect");
        JButton deleteButton = new JButton("Supprimer le site selectionne");

        formPanel.add(new JLabel("Nom :"));
        formPanel.add(nomField);
        formPanel.add(new JLabel("Adresse :"));
        formPanel.add(adresseField);
        formPanel.add(createButton, "wrap");
        formPanel.add(deleteButton, "span 5, gaptop 5");

        createButton.addActionListener(e -> {
            String nom = nomField.getText().trim();
            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Le nom du site est obligatoire.");
                return;
            }
            siteDao.create(nom, adresseField.getText().trim());
            nomField.setText(""); adresseField.setText("");
            reload();
            ToastNotification.show(SwingUtilities.getWindowAncestor(this),
                    "Site '" + nom + "' ajoute avec succes.", ToastNotification.Type.SUCCESS);
        });

        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Selectionnez un site a supprimer.");
                return;
            }
            int id = (int) model.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Supprimer definitivement ce site ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                siteDao.delete(id);
                reload();
            }
        });

        add(formPanel, BorderLayout.SOUTH);
        reload();
    }

    private void reload() {
        model.setRowCount(0);
        for (Site s : siteDao.findAll()) {
            model.addRow(new Object[]{s.getId(), s.getNom(), s.getAdresse()});
        }
    }
}
