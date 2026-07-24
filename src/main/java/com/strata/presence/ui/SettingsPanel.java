package com.strata.presence.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.strata.presence.dao.DatabaseManager;
import com.strata.presence.util.SoundUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;

/**
 * Ecran Parametres : apparence (clair/sombre), sons de notification, et
 * gestion des sauvegardes (sauvegarde immediate + restauration a partir
 * d'un point de sauvegarde anterieur).
 */
public class SettingsPanel extends JPanel {

    private boolean darkMode = false;
    private final DefaultListModel<String> backupsListModel = new DefaultListModel<>();
    private final JList<String> backupsList = new JList<>(backupsListModel);
    private List<Path> backupPaths = List.of();

    public SettingsPanel() {
        setLayout(new MigLayout("wrap 1, insets 20, gap 15", "[grow, fill]"));

        add(new PanelHeader(VectorIcons.settings(), "Parametres", "Apparence, sons et sauvegardes"));

        add(buildSectionLabel("Apparence"));
        JToggleButton darkModeToggle = new JToggleButton("Activer le mode sombre");
        darkModeToggle.addActionListener(e -> {
            darkMode = darkModeToggle.isSelected();
            darkModeToggle.setText(darkMode ? "Revenir au mode clair" : "Activer le mode sombre");
            appliquerTheme();
        });
        add(darkModeToggle);

        add(buildSectionLabel("Sons"));
        JCheckBox soundCheckbox = new JCheckBox("Activer les notifications sonores", SoundUtils.isEnabled());
        soundCheckbox.addActionListener(e -> SoundUtils.setEnabled(soundCheckbox.isSelected()));
        add(soundCheckbox);

        add(buildSectionLabel("Sauvegarde et restauration"));
        JLabel backupInfo = new JLabel("<html>Une sauvegarde automatique de la base est effectuee toutes les "
                + "30 minutes. Vous pouvez aussi en declencher une manuellement, ou restaurer un point "
                + "anterieur en cas de probleme.</html>");
        backupInfo.setForeground(new Color(130, 130, 130));
        add(backupInfo);

        JButton backupNowButton = new JButton("Sauvegarder maintenant", VectorIcons.print());
        backupNowButton.addActionListener(e -> {
            DatabaseManager.getInstance().backupNow();
            rechargerListeSauvegardes();
            ToastNotification.show(SwingUtilities.getWindowAncestor(this),
                    "Sauvegarde effectuee avec succes.", ToastNotification.Type.SUCCESS);
        });
        add(backupNowButton);

        backupsList.setVisibleRowCount(6);
        add(new JScrollPane(backupsList), "height 140!");

        JButton restoreButton = new JButton("Restaurer la sauvegarde selectionnee");
        restoreButton.addActionListener(e -> restaurerSauvegardeSelectionnee());
        add(restoreButton);

        rechargerListeSauvegardes();
    }

    private JLabel buildSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        return label;
    }

    private void rechargerListeSauvegardes() {
        backupPaths = DatabaseManager.getInstance().listBackups();
        backupsListModel.clear();
        if (backupPaths.isEmpty()) {
            backupsListModel.addElement("Aucune sauvegarde disponible pour le moment.");
        } else {
            for (Path p : backupPaths) {
                backupsListModel.addElement(p.getFileName().toString());
            }
        }
    }

    private void restaurerSauvegardeSelectionnee() {
        int index = backupsList.getSelectedIndex();
        if (index < 0 || backupPaths.isEmpty() || index >= backupPaths.size()) {
            JOptionPane.showMessageDialog(this, "Selectionnez d'abord une sauvegarde dans la liste.");
            return;
        }
        Path selected = backupPaths.get(index);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Restaurer la base a partir de '" + selected.getFileName() + "' ?\n\n"
                        + "ATTENTION : les donnees actuelles seront remplacees par cette sauvegarde.\n"
                        + "L'application devra etre redemarree juste apres pour prendre en compte le changement.",
                "Confirmation de restauration", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                DatabaseManager.getInstance().restoreFromBackup(selected);
                JOptionPane.showMessageDialog(this,
                        "Restauration effectuee avec succes.\n\nVeuillez fermer et relancer l'application maintenant.",
                        "Restauration terminee", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur lors de la restauration : " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void appliquerTheme() {
        try {
            if (darkMode) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }
            for (Window w : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(w);
            }
        } catch (UnsupportedLookAndFeelException e) {
            JOptionPane.showMessageDialog(this, "Impossible de changer de theme : " + e.getMessage());
        }
    }
}
