package com.strata.presence.ui;

import com.strata.presence.model.AttendanceRecord;
import com.strata.presence.model.RoleType;
import com.strata.presence.model.User;
import com.strata.presence.service.AttendanceService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Historique complet des presences avec recherche par periode et pagination
 * (utile des que le nombre d'enregistrements devient important). La
 * suppression d'une presence est reservee aux roles ADMIN et RESPONSABLE,
 * avec le meme niveau d'exigence que le mode derogation : mot de passe et
 * justification obligatoires, action systematiquement journalisee.
 */
public class HistoryPanel extends JPanel {

    private static final int PAGE_SIZE = 50;

    private final AttendanceService attendanceService = new AttendanceService();
    private final User currentUser;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Personne", "Date", "Arrivee", "Depart", "Statut", "Saisie manuelle"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel pageLabel = new JLabel();
    private final JButton previousPageButton = new JButton("< Precedent");
    private final JButton nextPageButton = new JButton("Suivant >");

    private List<AttendanceRecord> resultatsComplets = Collections.emptyList();
    private int pageCourante = 0;

    public HistoryPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        TableStyler.style(table);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));
        northPanel.add(new PanelHeader(VectorIcons.history(), "Historique des presences",
                "Recherche par periode, tri par colonne (clic sur l'en-tete)"), BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new MigLayout("insets 0", "[right][fill][right][fill][][]"));
        JSpinner startSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner endSpinner = new JSpinner(new SpinnerDateModel());
        startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "yyyy-MM-dd"));
        endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "yyyy-MM-dd"));

        JButton searchButton = new JButton("Rechercher");
        JButton todayButton = new JButton("Aujourd'hui");

        filterPanel.add(new JLabel("Du :"));
        filterPanel.add(startSpinner);
        filterPanel.add(new JLabel("Au :"));
        filterPanel.add(endSpinner);
        filterPanel.add(searchButton);
        filterPanel.add(todayButton);

        if (currentUser.getRole() == RoleType.ADMIN || currentUser.getRole() == RoleType.RESPONSABLE) {
            JButton deleteButton = new JButton("Supprimer la presence selectionnee");
            deleteButton.setToolTipText("Reserve aux administrateurs/responsables - mot de passe et justification requis, action journalisee");
            deleteButton.addActionListener(e -> supprimerPresenceSelectionnee());
            filterPanel.add(deleteButton);
        }

        searchButton.addActionListener(e -> {
            LocalDate start = ((java.util.Date) startSpinner.getValue()).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            LocalDate end = ((java.util.Date) endSpinner.getValue()).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            charger(attendanceService.presencesEntre(start, end));
        });
        todayButton.addActionListener(e -> charger(attendanceService.presencesDuJour()));

        northPanel.add(filterPanel, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        tableWrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        add(tableWrapper, BorderLayout.CENTER);

        JPanel paginationPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 15, 8));
        previousPageButton.addActionListener(e -> changerPage(pageCourante - 1));
        nextPageButton.addActionListener(e -> changerPage(pageCourante + 1));
        paginationPanel.add(previousPageButton);
        paginationPanel.add(pageLabel);
        paginationPanel.add(nextPageButton);
        add(paginationPanel, BorderLayout.SOUTH);

        charger(attendanceService.presencesDuJour());
    }

    private void supprimerPresenceSelectionnee() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selectionnez d'abord une presence dans le tableau.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        int presenceId = (int) tableModel.getValueAt(modelRow, 0);
        String personne = (String) tableModel.getValueAt(modelRow, 1);

        JTextField justificationField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPanel panel = new JPanel(new MigLayout("wrap 2", "[right][grow,fill]"));
        panel.add(new JLabel("Presence :"));
        panel.add(new JLabel(personne));
        panel.add(new JLabel("Justification :"));
        panel.add(justificationField);
        panel.add(new JLabel("Votre mot de passe :"));
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Suppression d'une presence - action journalisee",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                attendanceService.supprimerPresence(presenceId, justificationField.getText().trim(),
                        currentUser, new String(passwordField.getPassword()));
                ToastNotification.show(SwingUtilities.getWindowAncestor(this),
                        "Presence supprimee et journalisee.", ToastNotification.Type.SUCCESS);
                charger(resultatsComplets);
            } catch (SecurityException | IllegalArgumentException ex) {
                com.strata.presence.util.SoundUtils.playError();
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Suppression refusee", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Charge un nouveau resultat complet et affiche la premiere page. */
    private void charger(List<AttendanceRecord> records) {
        this.resultatsComplets = records;
        changerPage(0);
    }

    private void changerPage(int nouvellePage) {
        int totalPages = Math.max(1, (int) Math.ceil(resultatsComplets.size() / (double) PAGE_SIZE));
        pageCourante = Math.max(0, Math.min(nouvellePage, totalPages - 1));

        int debut = pageCourante * PAGE_SIZE;
        int fin = Math.min(debut + PAGE_SIZE, resultatsComplets.size());
        List<AttendanceRecord> pageActuelle = resultatsComplets.isEmpty()
                ? Collections.emptyList() : resultatsComplets.subList(debut, fin);

        tableModel.setRowCount(0);
        for (AttendanceRecord r : pageActuelle) {
            tableModel.addRow(new Object[]{
                    r.getId(),
                    r.getPersonneNomComplet(),
                    r.getDateJour(),
                    r.getHeureArrivee(),
                    r.getHeureDepart() != null ? r.getHeureDepart() : "-",
                    r.getStatut(),
                    r.isSaisieManuelle() ? "Oui" : "Non"
            });
        }

        pageLabel.setText(String.format("Page %d sur %d (%d resultat(s))",
                pageCourante + 1, totalPages, resultatsComplets.size()));
        previousPageButton.setEnabled(pageCourante > 0);
        nextPageButton.setEnabled(pageCourante < totalPages - 1);
    }
}
