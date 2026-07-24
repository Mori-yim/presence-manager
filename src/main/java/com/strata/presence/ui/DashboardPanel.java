package com.strata.presence.ui;

import com.strata.presence.service.AttendanceService;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Tableau de bord : indicateurs cles du jour, horloge temps reel, et deux
 * graphiques JFreeChart (evolution des 7 derniers jours + repartition du jour
 * visiteurs / personnel).
 */
public class DashboardPanel extends JPanel {

    private final AttendanceService attendanceService = new AttendanceService();
    private final JLabel clockLabel = new JLabel();
    private final JLabel totalLabel = new JLabel();
    private final JLabel presentLabel = new JLabel();
    private final JLabel departsLabel = new JLabel();
    private final JPanel chartsContainer = new JPanel(new GridLayout(1, 2, 15, 0));
    private final DefaultListModel<com.strata.presence.model.AttendanceRecord> activityListModel = new DefaultListModel<>();
    private final JList<com.strata.presence.model.AttendanceRecord> activityList = new JList<>(activityListModel);
    private int lastTotal = -1;
    private int lastPresents = -1;
    private int lastDeparts = -1;

    public DashboardPanel() {
        setLayout(new MigLayout("wrap 3, insets 20, gap 20", "[grow, fill][grow, fill][grow, fill]"));

        clockLabel.setFont(clockLabel.getFont().deriveFont(Font.BOLD, 20f));
        add(new PanelHeader(VectorIcons.dashboard(), "Tableau de bord", "Vue d'ensemble en temps reel"), "span 3");
        add(clockLabel, "span 3, gapbottom 10");

        add(buildStatCard("Visiteurs / Personnel aujourd'hui", totalLabel, VectorIcons.ACCENT));
        add(buildStatCard("Presents actuellement", presentLabel, new Color(30, 150, 80)));
        add(buildStatCard("Departs enregistres", departsLabel, new Color(150, 150, 150)));

        chartsContainer.setPreferredSize(new Dimension(100, 300));
        add(chartsContainer, "span 3, grow");

        add(buildActivityPanel(), "span 3, grow, height 180!");

        Timer refreshTimer = new Timer(1000, e -> refreshClockAndStats());
        refreshTimer.start();

        Timer chartRefreshTimer = new Timer(60_000, e -> { refreshCharts(); refreshActivity(); });
        chartRefreshTimer.start();

        refreshClockAndStats();
        refreshCharts();
        refreshActivity();
    }

    /** Panneau "Activite recente" : les derniers mouvements (arrivees/departs), tous types confondus. */
    private JPanel buildActivityPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        JLabel title = new JLabel("Activite recente");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        panel.add(title, BorderLayout.NORTH);

        activityList.setCellRenderer((list, record, index, isSelected, cellHasFocus) -> {
            String heure = record.getHeureDepart() != null && "PARTI".equals(record.getStatut())
                    ? record.getHeureDepart().withNano(0).toString()
                    : record.getHeureArrivee().withNano(0).toString();
            String action = "PARTI".equals(record.getStatut()) ? "Depart" : "Arrivee";
            JLabel label = new JLabel(String.format("%s  -  %s  -  %s (%s)",
                    heure, record.getPersonneNomComplet(), action, record.getDateJour()));
            label.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            label.setForeground("Depart".equals(action) ? new Color(120, 120, 120) : new Color(40, 40, 40));
            return label;
        });
        panel.add(new JScrollPane(activityList), BorderLayout.CENTER);
        return panel;
    }

    private void refreshActivity() {
        activityListModel.clear();
        for (com.strata.presence.model.AttendanceRecord r : attendanceService.activiteRecente(10)) {
            activityListModel.addElement(r);
        }
    }

    private JPanel buildStatCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, accent),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                        BorderFactory.createEmptyBorder(14, 16, 14, 16))));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 12f));
        titleLabel.setForeground(new Color(120, 120, 120));
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 28f));
        valueLabel.setForeground(accent);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void refreshClockAndStats() {
        clockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy - HH:mm:ss")));
        int total = attendanceService.nombreTotalAujourdHui();
        int presents = attendanceService.nombrePresentsAujourdHui();
        int departs = total - presents;

        if (total != lastTotal) {
            com.strata.presence.util.AnimationUtils.animateValue(Math.max(0, lastTotal), total, 400,
                    v -> totalLabel.setText(String.valueOf(v)));
            lastTotal = total;
        }
        if (presents != lastPresents) {
            com.strata.presence.util.AnimationUtils.animateValue(Math.max(0, lastPresents), presents, 400,
                    v -> presentLabel.setText(String.valueOf(v)));
            lastPresents = presents;
        }
        if (departs != lastDeparts) {
            com.strata.presence.util.AnimationUtils.animateValue(Math.max(0, lastDeparts), departs, 400,
                    v -> departsLabel.setText(String.valueOf(v)));
            lastDeparts = departs;
        }
    }

    private void refreshCharts() {
        chartsContainer.removeAll();
        chartsContainer.add(buildTrendChart());
        chartsContainer.add(buildPieChart());
        chartsContainer.revalidate();
        chartsContainer.repaint();
    }

    /** Graphique en barres : evolution des presences sur les 7 derniers jours. */
    private ChartPanel buildTrendChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            int count = attendanceService.nombreTotalPourDate(date);
            dataset.addValue(count, "Presences", date.format(dayFmt));
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Evolution des presences (7 derniers jours)", "Date", "Nombre",
                dataset, PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        chart.getPlot().setBackgroundPaint(new Color(250, 250, 252));
        return new ChartPanel(chart);
    }

    /** Graphique circulaire : repartition visiteurs / personnel du jour. */
    private ChartPanel buildPieChart() {
        LocalDate today = LocalDate.now();
        int visiteurs = attendanceService.nombreParTypePourDate(today, "VISITEUR");
        int personnel = attendanceService.nombreParTypePourDate(today, "PERSONNEL");
        int stagiaires = attendanceService.nombreParTypePourDate(today, "STAGIAIRE");

        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        dataset.setValue("Visiteurs", visiteurs);
        dataset.setValue("Employes", personnel);
        dataset.setValue("Stagiaires", stagiaires);

        JFreeChart chart = ChartFactory.createPieChart(
                "Repartition du jour", dataset, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        return new ChartPanel(chart);
    }
}
