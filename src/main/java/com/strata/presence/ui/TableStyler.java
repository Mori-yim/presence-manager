package com.strata.presence.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;

/**
 * Applique un style visuel homogene a tous les tableaux de l'application :
 * lignes alternees, en-tete accentue, tri par colonne, hauteur de ligne
 * confortable. Evite de dupliquer ce code dans chaque panneau.
 */
public final class TableStyler {

    private static final Color STRIPE = new Color(246, 248, 251);
    private static final Color HEADER_BG = new Color(230, 126, 34);

    private TableStyler() {}

    public static void style(JTable table) {
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(250, 224, 190));
        table.setSelectionForeground(Color.BLACK);
        table.setAutoCreateRowSorter(false);
        table.setRowSorter(new TableRowSorter<>(table.getModel()));
        table.setFillsViewportHeight(true);

        table.getTableHeader().setBackground(HEADER_BG);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));

        DefaultTableCellRenderer stripedRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                            boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : STRIPE);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(stripedRenderer);
        }
    }
}
