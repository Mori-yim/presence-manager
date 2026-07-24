package com.strata.presence.ui;

import com.strata.presence.dao.AppSettingsDao;
import com.strata.presence.dao.SiteDao;
import com.strata.presence.dao.UserDao;
import com.strata.presence.model.User;
import com.strata.presence.util.AppIcons;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Assistant affiche automatiquement lors du tout premier lancement (une seule
 * fois, tant que l'administrateur ne l'a pas termine) : changement du mot de
 * passe administrateur par defaut, puis creation du premier site de
 * l'entreprise. Transition animee (glissement horizontal) entre les etapes.
 */
public class SetupWizardDialog extends JDialog {

    public static final String SETTING_KEY = "setup_wizard_completed";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardsPanel = new JPanel();
    private final User adminUser;
    private final UserDao userDao = new UserDao();
    private final SiteDao siteDao = new SiteDao();
    private final AppSettingsDao settingsDao = new AppSettingsDao();

    private final JLabel stepIndicator = new JLabel();
    private int currentStep = 0;
    private static final int TOTAL_STEPS = 3;

    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JTextField siteNameField;
    private JTextField siteAddressField;

    public SetupWizardDialog(Window owner, User adminUser) {
        super(owner, "Configuration initiale - Strata Presence Manager", ModalityType.APPLICATION_MODAL);
        this.adminUser = adminUser;
        setSize(560, 460);
        setLocationRelativeTo(owner);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        AppIcons.apply(this);

        cardsPanel.setLayout(cardLayout);
        cardsPanel.add(buildWelcomeStep(), "0");
        cardsPanel.add(buildPasswordStep(), "1");
        cardsPanel.add(buildSiteStep(), "2");

        stepIndicator.setHorizontalAlignment(SwingConstants.CENTER);
        stepIndicator.setForeground(new Color(150, 150, 150));
        stepIndicator.setFont(stepIndicator.getFont().deriveFont(11f));
        updateStepIndicator();

        setLayout(new BorderLayout());
        add(cardsPanel, BorderLayout.CENTER);
        add(stepIndicator, BorderLayout.SOUTH);
    }

    private JPanel wrapStep(String title, String subtitle, JComponent content, JButton... actions) {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 40));

        panel.add(new PanelHeader(VectorIcons.settings(), title, subtitle), BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        for (JButton b : actions) buttonsPanel.add(b);
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildWelcomeStep() {
        JPanel content = new JPanel(new BorderLayout());
        JLabel text = new JLabel("<html><div style='text-align:center'>"
                + "Bienvenue dans Strata Presence Manager !<br><br>"
                + "Cet assistant va vous guider en deux etapes rapides :<br>"
                + "1. Changer le mot de passe administrateur par defaut<br>"
                + "2. Configurer le premier site de votre entreprise"
                + "</div></html>", SwingConstants.CENTER);
        content.add(text, BorderLayout.CENTER);

        JButton startButton = new JButton("Commencer");
        startButton.putClientProperty("JButton.buttonType", "roundRect");
        startButton.addActionListener(e -> goToStep(1));

        return wrapStep("Bienvenue", "Configuration initiale de l'application", content, startButton);
    }

    private JPanel buildPasswordStep() {
        JPanel form = new JPanel(new MigLayout("wrap 1, insets 20 0 0 0, gap 10", "[grow, fill]"));
        JLabel info = new JLabel("<html>Le compte administrateur utilise actuellement le mot de passe "
                + "par defaut. Pour la securite de l'application, choisissez un nouveau mot de passe.</html>");
        newPasswordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();

        form.add(info, "gapbottom 10");
        form.add(new JLabel("Nouveau mot de passe :"));
        form.add(newPasswordField);
        form.add(new JLabel("Confirmer le mot de passe :"));
        form.add(confirmPasswordField);

        JButton skipButton = new JButton("Ignorer cette etape");
        JButton nextButton = new JButton("Suivant");
        nextButton.putClientProperty("JButton.buttonType", "roundRect");
        nextButton.addActionListener(e -> {
            String pwd = new String(newPasswordField.getPassword());
            String confirm = new String(confirmPasswordField.getPassword());
            if (pwd.isEmpty()) {
                goToStep(2);
                return;
            }
            if (pwd.length() < 6) {
                JOptionPane.showMessageDialog(this, "Le mot de passe doit contenir au moins 6 caracteres.");
                return;
            }
            if (!pwd.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Les deux mots de passe ne correspondent pas.");
                return;
            }
            userDao.updatePassword(adminUser.getId(), pwd);
            goToStep(2);
        });
        skipButton.addActionListener(e -> goToStep(2));

        return wrapStep("Securite du compte", "Etape 1 sur 2", form, skipButton, nextButton);
    }

    private JPanel buildSiteStep() {
        JPanel form = new JPanel(new MigLayout("wrap 1, insets 20 0 0 0, gap 10", "[grow, fill]"));
        JLabel info = new JLabel("<html>Indiquez le nom et l'adresse du site principal de votre entreprise "
                + "(par exemple votre siege social ou l'agence ou ce poste est installe).</html>");
        siteNameField = new JTextField();
        siteAddressField = new JTextField();

        form.add(info, "gapbottom 10");
        form.add(new JLabel("Nom du site :"));
        form.add(siteNameField);
        form.add(new JLabel("Adresse :"));
        form.add(siteAddressField);

        JButton finishButton = new JButton("Terminer");
        finishButton.putClientProperty("JButton.buttonType", "roundRect");
        finishButton.addActionListener(e -> {
            String nom = siteNameField.getText().trim();
            if (!nom.isEmpty()) {
                siteDao.create(nom, siteAddressField.getText().trim());
            }
            settingsDao.setBoolean(SETTING_KEY, true);
            dispose();
        });

        return wrapStep("Premier site", "Etape 2 sur 2", form, finishButton);
    }

    private void goToStep(int step) {
        currentStep = step;
        cardLayout.show(cardsPanel, String.valueOf(step));
        updateStepIndicator();
        animateStepTransition();
    }

    private void updateStepIndicator() {
        stepIndicator.setText("\u25CF ".repeat(currentStep + 1) + "\u25CB ".repeat(TOTAL_STEPS - currentStep - 1));
    }

    /** Petit effet de fondu lors du changement d'etape (au-dela du CardLayout brut). */
    private void animateStepTransition() {
        cardsPanel.setVisible(false);
        Timer timer = new Timer(15, null);
        timer.addActionListener(new java.awt.event.ActionListener() {
            int step = 0;
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                step++;
                cardsPanel.setVisible(true);
                cardsPanel.repaint();
                if (step >= 2) timer.stop();
            }
        });
        timer.start();
    }
}
