package com.strata.presence.ui;

import com.strata.presence.model.AttendanceRecord;
import com.strata.presence.service.AttendanceService;
import com.strata.presence.util.PhotoStorage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Registre du jour, affiche sous le formulaire d'enregistrement : liste de
 * toutes les arrivees/departs du jour avec nom/prenom, profil, service,
 * telephone, heure d'arrivee, photo et signature (miniatures). Permet aussi de
 * marquer le depart d'une personne encore presente - fonctionnalite qui
 * n'existait auparavant dans aucun ecran de l'application.
 */
public class TodayRegistryPanel extends JPanel {

    private static final int PHOTO_SIZE = 36;
    private static final int SIGNATURE_WIDTH = 70;
    private static final int SIGNATURE_HEIGHT = 28;

    private final AttendanceService attendanceService = new AttendanceService();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Nom et prenom", "Profil", "Service", "Telephone",
                    "Heure d'arrivee", "Photo", "Signature", "Statut"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 6 || columnIndex == 7) return Icon.class;
            return Object.class;
        }
    };
    private final JTable table = new JTable(model);

    public TodayRegistryPanel() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(new PanelHeader(VectorIcons.history(), "Registre du jour",
                "Toutes les arrivees et departs enregistres aujourd'hui"), BorderLayout.NORTH);

        TableStyler.style(table);
        table.setRowHeight(46);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        if (table.getRowSorter() instanceof javax.swing.table.TableRowSorter<?> sorter) {
            sorter.setSortable(6, false);
            sorter.setSortable(7, false);
        }

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton departButton = new JButton("Marquer le depart de la personne selectionnee");
        departButton.putClientProperty("JButton.buttonType", "roundRect");
        departButton.addActionListener(e -> marquerDepartSelectionne());
        JButton refreshButton = new JButton("Actualiser");
        refreshButton.addActionListener(e -> rafraichir());
        actionsPanel.add(departButton);
        actionsPanel.add(refreshButton);
        add(actionsPanel, BorderLayout.SOUTH);

        rafraichir();

        // Rafraichissement automatique periodique (au cas ou plusieurs postes utilisent la meme base)
        Timer autoRefresh = new Timer(20_000, e -> rafraichir());
        autoRefresh.start();
    }

    public void rafraichir() {
        int selectedId = -1;
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            selectedId = (int) model.getValueAt(table.convertRowIndexToModel(selectedRow), 0);
        }

        model.setRowCount(0);
        List<AttendanceRecord> records = attendanceService.registreDuJour();
        for (AttendanceRecord r : records) {
            model.addRow(new Object[]{
                    r.getId(),
                    r.getPersonneNomComplet(),
                    r.getProfil() != null && !r.getProfil().isBlank() ? r.getProfil() : "-",
                    r.getServiceNom() != null ? r.getServiceNom() : "-",
                    r.getTelephone() != null && !r.getTelephone().isBlank() ? r.getTelephone() : "-",
                    r.getHeureArrivee().withNano(0),
                    buildThumbnail(r.getPhotoPath(), PHOTO_SIZE, PHOTO_SIZE),
                    buildThumbnail(r.getSignaturePath(), SIGNATURE_WIDTH, SIGNATURE_HEIGHT),
                    "PRESENT".equals(r.getStatut()) ? "Present" : "Parti"
            });
        }

        // Reselectionne la meme ligne si possible, pour ne pas perdre le contexte lors du rafraichissement auto
        if (selectedId != -1) {
            for (int i = 0; i < model.getRowCount(); i++) {
                if ((int) model.getValueAt(i, 0) == selectedId) {
                    int viewRow = table.convertRowIndexToView(i);
                    if (viewRow >= 0) table.setRowSelectionInterval(viewRow, viewRow);
                    break;
                }
            }
        }
    }

    private Icon buildThumbnail(String path, int width, int height) {
        if (path == null || path.isBlank()) return null;
        BufferedImage img = PhotoStorage.load(path);
        if (img == null) return null;
        Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private void marquerDepartSelectionne() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selectionnez d'abord une personne dans le registre.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        int presenceId = (int) model.getValueAt(modelRow, 0);
        String statut = (String) model.getValueAt(modelRow, 8);
        String nom = (String) model.getValueAt(modelRow, 1);

        if ("Parti".equals(statut)) {
            JOptionPane.showMessageDialog(this, nom + " a deja ete marque comme parti.");
            return;
        }

        attendanceService.enregistrerDepart(presenceId);
        com.strata.presence.util.SoundUtils.playSuccess();
        ToastNotification.show(SwingUtilities.getWindowAncestor(this),
                "Depart enregistre pour " + nom + " (heure systeme).", ToastNotification.Type.SUCCESS);
        rafraichir();
    }
}
