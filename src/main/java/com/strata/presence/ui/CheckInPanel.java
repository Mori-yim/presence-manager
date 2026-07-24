package com.strata.presence.ui;

import com.strata.presence.dao.MotifDao;
import com.strata.presence.dao.PersonDao;
import com.strata.presence.dao.ServiceDao;
import com.strata.presence.dao.SiteDao;
import com.strata.presence.model.Motif;
import com.strata.presence.model.Person;
import com.strata.presence.model.PersonType;
import com.strata.presence.model.RoleType;
import com.strata.presence.model.Service;
import com.strata.presence.model.Site;
import com.strata.presence.model.User;
import com.strata.presence.service.AttendanceService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalTime;
import java.util.List;

/**
 * Panneau principal utilise a l'accueil : enregistrement d'un nouveau visiteur/membre
 * du personnel/stagiaire, puis marquage automatique de l'arrivee (date/heure systeme).
 * Les champs "Profil" et "Raison de la visite" ne sont affiches que pour les visiteurs.
 * Inclut l'acces au mode derogation (arrivee avant ouverture de l'accueil).
 */
public class CheckInPanel extends JPanel {

    private final User currentUser;
    private final PersonDao personDao = new PersonDao();
    private final AttendanceService attendanceService = new AttendanceService();
    private final ServiceDao serviceDao = new ServiceDao();
    private final MotifDao motifDao = new MotifDao();
    private final SiteDao siteDao = new SiteDao();

    private final JTextField nomField = new JTextField(15);
    private final JTextField prenomField = new JTextField(15);
    private final JTextField pieceField = new JTextField(15);
    private final JTextField telField = new JTextField(15);
    private final JTextField entrepriseField = new JTextField(15);
    private final JComboBox<PersonType> typeCombo = new JComboBox<>(PersonType.values());
    private final JComboBox<Service> serviceCombo = new JComboBox<>();
    private final JComboBox<Motif> motifCombo = new JComboBox<>();
    private final JComboBox<Site> siteCombo = new JComboBox<>();

    // Champs specifiques aux visiteurs uniquement
    private final JLabel profilLabel = new JLabel("Profil du visiteur :");
    private final JComboBox<String> profilCombo = new JComboBox<>(
            new String[]{"Client", "Fournisseur", "Partenaire", "Presse", "Institution", "Autre"});
    private final JLabel raisonLabel = new JLabel("Raison de la visite :");
    private final JTextField raisonField = new JTextField(20);

    private final JLabel resultLabel = new JLabel(" ");
    private final JButton badgeButton = new JButton("Imprimer le badge", VectorIcons.print());
    private final JLabel photoPreview = new JLabel("Aucune photo", SwingConstants.CENTER);
    private final JLabel signaturePreview = new JLabel("Aucune signature", SwingConstants.CENTER);
    private String photoPathCapturee;
    private String signaturePathCapturee;
    private Person derniersPersonne;
    private com.strata.presence.model.AttendanceRecord derniereePresence;
    private final TodayRegistryPanel todayRegistryPanel = new TodayRegistryPanel();

    public CheckInPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new MigLayout("wrap 2, insets 20, gap 15", "[right][grow, fill]"));

        formPanel.add(new PanelHeader(VectorIcons.checkin(), "Enregistrement d'une arrivee",
                "La date et l'heure sont generees automatiquement par le systeme"), "span 2, gapbottom 10");

        formPanel.add(new JLabel("Type :"));
        formPanel.add(typeCombo);
        formPanel.add(new JLabel("Nom :"));
        formPanel.add(nomField);
        formPanel.add(new JLabel("Prenom :"));
        formPanel.add(prenomField);
        formPanel.add(new JLabel("Piece d'identite :"));
        formPanel.add(pieceField);
        formPanel.add(new JLabel("Telephone :"));
        formPanel.add(telField);
        formPanel.add(new JLabel("Entreprise / Precision :"));
        formPanel.add(entrepriseField);
        formPanel.add(new JLabel("Service concerne :"));
        formPanel.add(serviceCombo);
        formPanel.add(new JLabel("Motif de la visite :"));
        formPanel.add(motifCombo);
        formPanel.add(new JLabel("Site :"));
        formPanel.add(siteCombo);

        profilCombo.setEditable(true);
        formPanel.add(profilLabel);
        formPanel.add(profilCombo);
        formPanel.add(raisonLabel);
        formPanel.add(raisonField);

        typeCombo.addActionListener(e -> mettreAJourVisibiliteChampsVisiteur());
        mettreAJourVisibiliteChampsVisiteur();

        chargerListesReference();

        photoPreview.setPreferredSize(new Dimension(100, 100));
        photoPreview.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        JButton photoButton = new JButton("Prendre une photo (webcam)", VectorIcons.camera());
        photoButton.addActionListener(e -> capturerPhoto());
        formPanel.add(new JLabel("Photo :"));
        formPanel.add(photoButton);
        formPanel.add(new JLabel(""));
        formPanel.add(photoPreview, "width 100!, height 100!");

        signaturePreview.setPreferredSize(new Dimension(200, 80));
        signaturePreview.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        JButton signatureButton = new JButton("Signature numerique", VectorIcons.badge());
        signatureButton.setToolTipText("Faire signer la personne directement a l'ecran (souris ou ecran tactile)");
        signatureButton.addActionListener(e -> capturerSignature());
        formPanel.add(new JLabel("Signature :"));
        formPanel.add(signatureButton);
        formPanel.add(new JLabel(""));
        formPanel.add(signaturePreview, "width 200!, height 80!");

        JButton registerButton = new JButton("Enregistrer l'arrivee (heure systeme)");
        registerButton.putClientProperty("JButton.buttonType", "roundRect");
        registerButton.setToolTipText("La date et l'heure sont generees automatiquement par le systeme et ne sont jamais modifiables ici");
        registerButton.addActionListener(e -> enregistrerArrivee());
        formPanel.add(registerButton, "span 2, height 42, gaptop 15");

        badgeButton.setEnabled(false);
        badgeButton.setToolTipText("Disponible juste apres l'enregistrement d'une arrivee");
        badgeButton.addActionListener(e -> ouvrirBadge());
        formPanel.add(badgeButton, "span 2, height 36");

        if (currentUser.getRole() == RoleType.ADMIN || currentUser.getRole() == RoleType.RESPONSABLE) {
            JButton derogationButton = new JButton("Mode derogation (arrivee avant ouverture)");
            derogationButton.setToolTipText("Reserve aux administrateurs/responsables - necessite votre mot de passe et une justification, action journalisee");
            derogationButton.addActionListener(e -> ouvrirDerogation());
            formPanel.add(derogationButton, "span 2, height 36");
        }

        resultLabel.setForeground(new Color(30, 130, 60));
        formPanel.add(resultLabel, "span 2, gaptop 10, gapbottom 20");

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, todayRegistryPanel);
        splitPane.setResizeWeight(0.55);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);
    }

    /** Le profil et la raison de la visite ne concernent que les visiteurs, pas le personnel/stagiaires. */
    private void mettreAJourVisibiliteChampsVisiteur() {
        boolean estVisiteur = typeCombo.getSelectedItem() == PersonType.VISITEUR;
        profilLabel.setVisible(estVisiteur);
        profilCombo.setVisible(estVisiteur);
        raisonLabel.setVisible(estVisiteur);
        raisonField.setVisible(estVisiteur);
    }

    private void chargerListesReference() {
        serviceCombo.removeAllItems();
        for (Service s : serviceDao.findAll()) serviceCombo.addItem(s);

        motifCombo.removeAllItems();
        for (Motif m : motifDao.findAll()) motifCombo.addItem(m);

        siteCombo.removeAllItems();
        for (Site s : siteDao.findAll()) siteCombo.addItem(s);
    }

    private void enregistrerArrivee() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        nomField.putClientProperty("JComponent.outline", null);
        prenomField.putClientProperty("JComponent.outline", null);

        if (nom.isEmpty() || prenom.isEmpty()) {
            if (nom.isEmpty()) nomField.putClientProperty("JComponent.outline", "error");
            if (prenom.isEmpty()) prenomField.putClientProperty("JComponent.outline", "error");
            resultLabel.setForeground(new Color(200, 50, 50));
            resultLabel.setText("Le nom et le prenom sont obligatoires.");
            com.strata.presence.util.SoundUtils.playError();
            return;
        }

        PersonType type = (PersonType) typeCombo.getSelectedItem();
        boolean estVisiteur = type == PersonType.VISITEUR;

        // Evite de recreer une fiche a chaque passage : recherche d'une personne
        // deja connue (par piece d'identite en priorite, sinon nom+prenom).
        java.util.Optional<Person> existante = personDao.findExistingMatch(
                pieceField.getText().trim(), nom, prenom);

        Person saved;
        if (existante.isPresent()) {
            int choix = JOptionPane.showConfirmDialog(this,
                    "Une fiche existe deja pour " + existante.get().getNomComplet()
                            + " (" + existante.get().getType().getLibelle() + ").\n\n"
                            + "Utiliser cette fiche existante (recommande) ?\n"
                            + "Non = creer une toute nouvelle fiche distincte.",
                    "Personne deja connue", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choix == JOptionPane.CANCEL_OPTION || choix == JOptionPane.CLOSED_OPTION) {
                return;
            }
            if (choix == JOptionPane.YES_OPTION) {
                saved = existante.get();
            } else {
                saved = creerNouvellePersonne(type, nom, prenom, estVisiteur);
            }
        } else {
            saved = creerNouvellePersonne(type, nom, prenom, estVisiteur);
        }

        Service selectedService = (Service) serviceCombo.getSelectedItem();
        Site selectedSite = (Site) siteCombo.getSelectedItem();
        Motif selectedMotif = (Motif) motifCombo.getSelectedItem();
        Integer motifId = selectedMotif != null ? selectedMotif.getId() : null;
        Integer siteId = selectedSite != null ? selectedSite.getId() : null;

        // L'heure et la date d'arrivee sont generees exclusivement par le systeme (voir AttendanceService)
        com.strata.presence.model.AttendanceRecord presence =
                attendanceService.enregistrerArrivee(saved.getId(), motifId, siteId, currentUser.getId());

        derniersPersonne = saved;
        derniereePresence = presence;
        badgeButton.setEnabled(true);
        com.strata.presence.util.SoundUtils.playSuccess();
        todayRegistryPanel.rafraichir();

        resultLabel.setForeground(new Color(30, 130, 60));
        resultLabel.setText("Arrivee enregistree pour " + saved.getNomComplet()
                + " a " + LocalTime.now().withNano(0) + " (heure systeme).");

        nomField.setText(""); prenomField.setText(""); pieceField.setText("");
        telField.setText(""); entrepriseField.setText(""); raisonField.setText("");
        photoPathCapturee = null;
        photoPreview.setIcon(null);
        photoPreview.setText("Aucune photo");
        signaturePathCapturee = null;
        signaturePreview.setIcon(null);
        signaturePreview.setText("Aucune signature");
    }

    /** Cree une toute nouvelle fiche personne a partir des champs actuellement saisis. */
    private Person creerNouvellePersonne(PersonType type, String nom, String prenom, boolean estVisiteur) {
        Person person = new Person();
        person.setType(type);
        person.setNom(nom);
        person.setPrenom(prenom);
        person.setPieceIdentite(pieceField.getText().trim());
        person.setTelephone(telField.getText().trim());
        person.setEntreprise(entrepriseField.getText().trim());
        person.setPhotoPath(photoPathCapturee);
        person.setSignaturePath(signaturePathCapturee);

        Service selectedService = (Service) serviceCombo.getSelectedItem();
        person.setServiceId(selectedService != null ? selectedService.getId() : null);
        Site selectedSite = (Site) siteCombo.getSelectedItem();
        person.setSiteId(selectedSite != null ? selectedSite.getId() : null);

        if (estVisiteur) {
            Object profilValue = profilCombo.getSelectedItem();
            person.setProfil(profilValue != null ? profilValue.toString().trim() : null);
            person.setRaisonVisite(raisonField.getText().trim());
        }

        return personDao.create(person);
    }

    private void capturerPhoto() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        WebcamCaptureDialog dialog = new WebcamCaptureDialog(owner);
        dialog.setVisible(true);
        String path = dialog.getSavedPhotoPath();
        if (path != null) {
            photoPathCapturee = path;
            BufferedImage img = com.strata.presence.util.PhotoStorage.load(path);
            if (img != null) {
                Image scaled = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                photoPreview.setIcon(new ImageIcon(scaled));
                photoPreview.setText(null);
            }
        }
    }

    private void capturerSignature() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        SignaturePadDialog dialog = new SignaturePadDialog(owner);
        dialog.setVisible(true);
        String path = dialog.getSavedSignaturePath();
        if (path != null) {
            signaturePathCapturee = path;
            BufferedImage img = com.strata.presence.util.PhotoStorage.load(path);
            if (img != null) {
                Image scaled = img.getScaledInstance(200, 80, Image.SCALE_SMOOTH);
                signaturePreview.setIcon(new ImageIcon(scaled));
                signaturePreview.setText(null);
            }
        }
    }

    /**
     * Ouvre la boite de dialogue de derogation : necessite le mot de passe de
     * l'utilisateur courant (ADMIN ou RESPONSABLE) et une justification obligatoire.
     * Toute action ici est journalisee automatiquement dans le journal d'audit.
     */
    private void ouvrirDerogation() {
        List<com.strata.presence.model.AttendanceRecord> presencesDuJour = attendanceService.presencesDuJour();
        if (presencesDuJour.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucune presence enregistree aujourd'hui a corriger.");
            return;
        }

        JComboBox<com.strata.presence.model.AttendanceRecord> recordCombo = new JComboBox<>(presencesDuJour.toArray(new com.strata.presence.model.AttendanceRecord[0]));
        recordCombo.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new JLabel(value.getPersonneNomComplet() + " - arrivee actuelle : " + value.getHeureArrivee()));

        JSpinner heureSpinner = new JSpinner(new SpinnerDateModel());
        heureSpinner.setEditor(new JSpinner.DateEditor(heureSpinner, "HH:mm"));

        JTextField justificationField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        JPanel panel = new JPanel(new MigLayout("wrap 2", "[right][grow,fill]"));
        panel.add(new JLabel("Presence a corriger :"));
        panel.add(recordCombo);
        panel.add(new JLabel("Heure reelle d'arrivee :"));
        panel.add(heureSpinner);
        panel.add(new JLabel("Justification :"));
        panel.add(justificationField);
        panel.add(new JLabel("Votre mot de passe :"));
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Mode derogation - saisie manuelle de l'heure",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                com.strata.presence.model.AttendanceRecord selected =
                        (com.strata.presence.model.AttendanceRecord) recordCombo.getSelectedItem();
                java.util.Date date = (java.util.Date) heureSpinner.getValue();
                LocalTime nouvelleHeure = date.toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);

                attendanceService.derogationSaisieManuelle(
                        selected.getId(), nouvelleHeure,
                        justificationField.getText().trim(),
                        currentUser, new String(passwordField.getPassword()));

                resultLabel.setForeground(new Color(30, 130, 60));
                resultLabel.setText("Derogation appliquee et journalisee pour " + selected.getPersonneNomComplet() + ".");
                todayRegistryPanel.rafraichir();
            } catch (SecurityException | IllegalArgumentException ex) {
                com.strata.presence.util.SoundUtils.playError();
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Derogation refusee", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void ouvrirBadge() {
        if (derniersPersonne == null || derniereePresence == null) {
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        new BadgeDialog(owner, derniersPersonne, derniereePresence).setVisible(true);
    }
}
