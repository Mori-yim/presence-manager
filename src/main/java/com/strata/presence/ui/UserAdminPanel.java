package com.strata.presence.ui;

import com.strata.presence.dao.UserDao;
import com.strata.presence.model.RoleType;
import com.strata.presence.model.User;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Gestion des comptes utilisateurs. La creation de compte reste reservee au
 * role ADMIN, mais la suppression d'un utilisateur est desormais ouverte aux
 * roles ADMIN et RESPONSABLE. Un utilisateur ne peut pas se supprimer lui-meme.
 */
public class UserAdminPanel extends JPanel {

    private final UserDao userDao = new UserDao();
    private final User currentUser;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Identifiant", "Nom complet", "Role", "Actif"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    public UserAdminPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(new PanelHeader(VectorIcons.users(), "Gestion des utilisateurs",
                "Creation reservee a l'administrateur - suppression ouverte a l'administrateur et au responsable"),
                BorderLayout.NORTH);

        TableStyler.style(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout(0, 10));

        JPanel deletePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton deleteButton = new JButton("Supprimer l'utilisateur selectionne");
        deleteButton.setToolTipText("Suppression ouverte aux roles Administrateur et Responsable");
        deleteButton.addActionListener(e -> supprimerUtilisateurSelectionne());
        deletePanel.add(deleteButton);
        southPanel.add(deletePanel, BorderLayout.NORTH);

        if (currentUser.getRole() == RoleType.ADMIN) {
            southPanel.add(buildCreationForm(), BorderLayout.SOUTH);
        }

        add(southPanel, BorderLayout.SOUTH);
        reload();
    }

    private JPanel buildCreationForm() {
        JPanel formPanel = new JPanel(new MigLayout("insets 15", "[right][fill][right][fill][right][fill][]"));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JTextField usernameField = new JTextField(10);
        JTextField fullNameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(10);
        JComboBox<RoleType> roleCombo = new JComboBox<>(RoleType.values());
        JButton createButton = new JButton("Creer l'utilisateur");
        createButton.putClientProperty("JButton.buttonType", "roundRect");

        formPanel.add(new JLabel("Identifiant :"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Nom complet :"));
        formPanel.add(fullNameField);
        formPanel.add(new JLabel("Mot de passe :"));
        formPanel.add(passwordField);
        formPanel.add(roleCombo);
        formPanel.add(createButton, "wrap");

        createButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String fullName = fullNameField.getText().trim();
            String password = new String(passwordField.getPassword());
            if (username.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tous les champs sont obligatoires.");
                return;
            }
            userDao.create(username, password, fullName, (RoleType) roleCombo.getSelectedItem());
            usernameField.setText(""); fullNameField.setText(""); passwordField.setText("");
            reload();
            ToastNotification.show(SwingUtilities.getWindowAncestor(this),
                    "Utilisateur '" + fullName + "' cree avec succes.", ToastNotification.Type.SUCCESS);
        });

        return formPanel;
    }

    private void supprimerUtilisateurSelectionne() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selectionnez d'abord un utilisateur a supprimer.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        int userId = (int) model.getValueAt(modelRow, 0);
        String username = (String) model.getValueAt(modelRow, 1);
        String fullName = (String) model.getValueAt(modelRow, 2);

        if (userId == currentUser.getId()) {
            JOptionPane.showMessageDialog(this, "Vous ne pouvez pas supprimer votre propre compte.",
                    "Action non autorisee", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Supprimer definitivement l'utilisateur '" + fullName + "' (" + username + ") ?\n"
                        + "Cette action est irreversible.",
                "Confirmation de suppression", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            userDao.delete(userId);
            reload();
            ToastNotification.show(SwingUtilities.getWindowAncestor(this),
                    "Utilisateur '" + fullName + "' supprime.", ToastNotification.Type.SUCCESS);
        }
    }

    private void reload() {
        model.setRowCount(0);
        for (User u : userDao.findAll()) {
            model.addRow(new Object[]{
                    u.getId(), u.getUsername(), u.getFullName(), u.getRole().getLibelle(),
                    u.isActive() ? "Oui" : "Non"
            });
        }
    }
}
