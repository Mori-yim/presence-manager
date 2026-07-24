package com.strata.presence.ui;

import com.strata.presence.dao.PersonDao;
import com.strata.presence.model.AttendanceRecord;
import com.strata.presence.model.Person;
import com.strata.presence.model.PersonType;
import com.strata.presence.service.AttendanceService;
import com.strata.presence.util.PhotoStorage;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Repertoire de toutes les personnes deja enregistrees (visiteurs, employes,
 * stagiaires), avec recherche et acces a l'historique complet de chacune.
 * Utile pour retrouver rapidement une fiche existante et consulter la
 * frequence de passage d'un visiteur ou l'assiduite d'un employe/stagiaire.
 */
public class PersonDirectoryPanel extends JPanel {

    private final PersonDao personDao = new PersonDao();
    private final AttendanceService attendanceService = new AttendanceService();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Photo", "Nom et prenom", "Type", "Telephone", "Entreprise/Precision"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 1) return Icon.class;
            return Object.class;
        }
    };
    private final JTable table = new JTable(model);
    private final JTextField searchField = new JTextField(20);
    private final JComboBox<String> typeFilterCombo = new JComboBox<>(
            new String[]{"Tous les types", "Visiteur", "Employe", "Stagiaire"});

    public PersonDirectoryPanel() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(new PanelHeader(VectorIcons.users(), "Repertoire des personnes",
                "Toutes les fiches deja enregistrees - recherchez pour consulter l'historique d'une personne"),
                BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new MigLayout("insets 0", "[right][fill][right][fill][]"));
        searchPanel.add(new JLabel("Rechercher :"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Type :"));
        searchPanel.add(typeFilterCombo);
        JButton searchButton = new JButton("Rechercher", VectorIcons.search());
        searchButton.addActionListener(e -> rechercher());
        searchField.addActionListener(e -> rechercher());
        searchPanel.add(searchButton);
        northPanel.add(searchPanel, BorderLayout.CENTER);

        add(northPanel, BorderLayout.NORTH);

        TableStyler.style(table);
        table.setRowHeight(40);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        if (table.getRowSorter() instanceof javax.swing.table.TableRowSorter<?> sorter) {
            sorter.setSortable(1, false);
        }
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton historiqueButton = new JButton("Voir l'historique de la personne selectionnee");
        historiqueButton.putClientProperty("JButton.buttonType", "roundRect");
        historiqueButton.addActionListener(e -> voirHistoriqueSelectionne());
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        southPanel.add(historiqueButton);
        add(southPanel, BorderLayout.SOUTH);

        rechercher();
    }

    private void rechercher() {
        String texte = searchField.getText().trim();
        PersonType type = switch ((String) typeFilterCombo.getSelectedItem()) {
            case "Visiteur" -> PersonType.VISITEUR;
            case "Employe" -> PersonType.PERSONNEL;
            case "Stagiaire" -> PersonType.STAGIAIRE;
            default -> null;
        };

        model.setRowCount(0);
        List<Person> personnes = personDao.search(texte.isEmpty() ? null : texte, type);
        for (Person p : personnes) {
            Icon thumbnail = null;
            if (p.getPhotoPath() != null) {
                BufferedImage img = PhotoStorage.load(p.getPhotoPath());
                if (img != null) {
                    thumbnail = new ImageIcon(img.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
                }
            }
            model.addRow(new Object[]{
                    p.getId(), thumbnail, p.getNomComplet(), p.getType().getLibelle(),
                    p.getTelephone() != null && !p.getTelephone().isBlank() ? p.getTelephone() : "-",
                    p.getEntreprise() != null && !p.getEntreprise().isBlank() ? p.getEntreprise() : "-"
            });
        }
    }

    private void voirHistoriqueSelectionne() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selectionnez d'abord une personne dans le repertoire.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        int personneId = (int) model.getValueAt(modelRow, 0);
        String nom = (String) model.getValueAt(modelRow, 2);

        List<AttendanceRecord> historique = attendanceService.presencesDePersonne(personneId);

        DefaultTableModel historyModel = new DefaultTableModel(
                new Object[]{"Date", "Arrivee", "Depart", "Statut", "Saisie manuelle"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        for (AttendanceRecord r : historique) {
            historyModel.addRow(new Object[]{
                    r.getDateJour(), r.getHeureArrivee(),
                    r.getHeureDepart() != null ? r.getHeureDepart() : "-",
                    r.getStatut(), r.isSaisieManuelle() ? "Oui" : "Non"
            });
        }
        JTable historyTable = new JTable(historyModel);
        TableStyler.style(historyTable);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setPreferredSize(new Dimension(560, 400));
        content.add(new JLabel("Historique de " + nom + " (" + historique.size() + " passage(s))"), BorderLayout.NORTH);
        content.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Historique - " + nom,
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        com.strata.presence.util.AppIcons.apply(dialog);
        dialog.setVisible(true);
    }
}
