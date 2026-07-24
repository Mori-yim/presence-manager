package com.strata.presence.ui;

import com.strata.presence.model.AttendanceRecord;
import com.strata.presence.model.ReportData;
import com.strata.presence.model.ReportPeriod;
import com.strata.presence.report.CsvReportExporter;
import com.strata.presence.report.ExcelReportExporter;
import com.strata.presence.report.PdfReportExporter;
import com.strata.presence.service.ReportService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Panneau "Rapports" : selection de la periode (journalier/hebdo/mensuel/annuel),
 * apercu des donnees et export PDF / Excel / CSV.
 */
public class ReportPanel extends JPanel {

    private final ReportService reportService = new ReportService();
    private final PdfReportExporter pdfExporter = new PdfReportExporter();
    private final ExcelReportExporter excelExporter = new ExcelReportExporter();
    private final CsvReportExporter csvExporter = new CsvReportExporter();

    private final JComboBox<ReportPeriod> periodCombo = new JComboBox<>(ReportPeriod.values());
    private final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
    private final JLabel statsLabel = new JLabel(" ");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Personne", "Date", "Arrivee", "Depart", "Statut", "Saisie manuelle"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private ReportData currentReport;

    public ReportPanel() {
        setLayout(new java.awt.BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        TableStyler.style(table);

        JPanel northPanel = new JPanel(new java.awt.BorderLayout(0, 10));
        northPanel.add(new PanelHeader(VectorIcons.reports(), "Rapports et exports",
                "Journalier, hebdomadaire, mensuel ou annuel - export PDF, Excel ou CSV"),
                java.awt.BorderLayout.NORTH);

        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

        JPanel controlPanel = new JPanel(new MigLayout("insets 12", "[right][fill][right][fill][][][][]"));
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        JButton generateButton = new JButton("Generer le rapport");
        generateButton.putClientProperty("JButton.buttonType", "roundRect");
        JButton pdfButton = new JButton("Exporter PDF", VectorIcons.reports());
        JButton excelButton = new JButton("Exporter Excel", VectorIcons.reports());
        JButton csvButton = new JButton("Exporter CSV", VectorIcons.reports());

        controlPanel.add(new JLabel("Periode :"));
        controlPanel.add(periodCombo);
        controlPanel.add(new JLabel("Date de reference :"));
        controlPanel.add(dateSpinner);
        controlPanel.add(generateButton);
        controlPanel.add(pdfButton);
        controlPanel.add(excelButton);
        controlPanel.add(csvButton);

        northPanel.add(controlPanel, java.awt.BorderLayout.CENTER);

        statsLabel.setFont(statsLabel.getFont().deriveFont(Font.BOLD, 12f));
        statsLabel.setBorder(BorderFactory.createEmptyBorder(8, 4, 0, 4));

        add(northPanel, java.awt.BorderLayout.NORTH);
        add(new JScrollPane(table), java.awt.BorderLayout.CENTER);
        add(statsLabel, java.awt.BorderLayout.SOUTH);

        generateButton.addActionListener(e -> genererRapport());
        pdfButton.addActionListener(e -> exporter("pdf"));
        excelButton.addActionListener(e -> exporter("xlsx"));
        csvButton.addActionListener(e -> exporter("csv"));

        genererRapport();
    }

    private void genererRapport() {
        LocalDate reference = ((Date) dateSpinner.getValue()).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        ReportPeriod period = (ReportPeriod) periodCombo.getSelectedItem();
        currentReport = reportService.genererRapport(period, reference);

        tableModel.setRowCount(0);
        for (AttendanceRecord r : currentReport.getRecords()) {
            tableModel.addRow(new Object[]{
                    r.getPersonneNomComplet(), r.getDateJour(), r.getHeureArrivee().withNano(0),
                    r.getHeureDepart() != null ? r.getHeureDepart().withNano(0) : "-",
                    r.getStatut(), r.isSaisieManuelle() ? "Oui" : "Non"
            });
        }

        statsLabel.setText(String.format(
                "Total : %d   |   Presents : %d   |   Partis : %d   |   Derogations horaires : %d   (periode : %s au %s)",
                currentReport.getTotal(), currentReport.getTotalPresents(), currentReport.getTotalPartis(),
                currentReport.getTotalDerogations(), currentReport.getDateDebut(), currentReport.getDateFin()));
    }

    private void exporter(String format) {
        if (currentReport == null) {
            JOptionPane.showMessageDialog(this, "Veuillez d'abord generer un rapport.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        String defaultName = "rapport_" + currentReport.getPeriod().name().toLowerCase()
                + "_" + currentReport.getDateDebut() + "." + format;
        chooser.setSelectedFile(new File(defaultName));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        String path = chooser.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith("." + format)) {
            path = path + "." + format;
        }

        try {
            switch (format) {
                case "pdf" -> pdfExporter.export(currentReport, path);
                case "xlsx" -> excelExporter.export(currentReport, path);
                case "csv" -> csvExporter.export(currentReport, path);
                default -> throw new IllegalArgumentException("Format non supporte : " + format);
            }
            ToastNotification.show(SwingUtilities.getWindowAncestor(this),
                    "Rapport exporte : " + new File(path).getName(), ToastNotification.Type.SUCCESS);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'export : " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
