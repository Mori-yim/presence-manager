package com.strata.presence.ui;

import javax.swing.*;
import java.awt.*;

/**
 * En-tete visuel standard reutilise par tous les panneaux principaux :
 * icone, titre, sous-titre optionnel. Garantit une identite visuelle
 * coherente dans toute l'application plutot que des titres ad-hoc disparates.
 */
public class PanelHeader extends JPanel {

    public PanelHeader(Icon icon, String title, String subtitle) {
        setLayout(new BorderLayout(10, 2));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 17f));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(titleLabel);

        if (subtitle != null && !subtitle.isBlank()) {
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 11f));
            subtitleLabel.setForeground(new Color(130, 130, 130));
            subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            textPanel.add(subtitleLabel);
        }

        add(iconLabel, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);
    }
}
