package com.strata.presence.ui;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.strata.presence.util.AppIcons;
import com.strata.presence.util.PhotoStorage;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Fenetre de capture photo pour les visiteurs/employes/stagiaires, via la
 * webcam de la machine.
 *
 * Points de robustesse importants (suite a un bug ou la capture ne demarrait
 * jamais sur certains postes) :
 * - La detection de la webcam se fait EN ARRIERE-PLAN (SwingWorker), jamais
 *   sur l'Event Dispatch Thread : Webcam.getWebcams() peut prendre plusieurs
 *   secondes selon le pilote, et bloquerait sinon toute l'interface sans
 *   aucun retour visuel pour l'agent.
 * - Un delai maximum de 5 secondes est applique a la detection, pour ne
 *   jamais rester bloque indefiniment si le pilote ne repond pas.
 * - La fenetre est TOUJOURS affichee (jamais fermee avant meme d'apparaitre) :
 *   le resultat de la detection (webcam trouvee ou non) est affiche a
 *   l'interieur de la fenetre elle-meme, avec un message clair dans les deux cas.
 * - La resolution video n'est plus imposee arbitrairement (ce qui provoque une
 *   exception sur de nombreux pilotes qui ne supportent pas toutes les
 *   tailles) : on utilise une resolution effectivement supportee par la webcam.
 */
public class WebcamCaptureDialog extends JDialog {

    private String savedPhotoPath;
    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private final JPanel contentContainer = new JPanel(new BorderLayout());
    private final JButton captureButton = new JButton("Capturer la photo");

    public WebcamCaptureDialog(Window owner) {
        super(owner, "Prise de photo", ModalityType.APPLICATION_MODAL);
        setSize(520, 480);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        AppIcons.apply(this);
        setLayout(new BorderLayout());

        JLabel loadingLabel = new JLabel("Recherche de la webcam en cours...", SwingConstants.CENTER);
        loadingLabel.setForeground(new Color(130, 130, 130));
        contentContainer.add(loadingLabel, BorderLayout.CENTER);
        add(contentContainer, BorderLayout.CENTER);

        captureButton.putClientProperty("JButton.buttonType", "roundRect");
        captureButton.setEnabled(false);
        captureButton.addActionListener(e -> capturer());

        JButton cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(e -> {
            fermerWebcam();
            dispose();
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttons.add(captureButton);
        buttons.add(cancelButton);
        add(buttons, BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                fermerWebcam();
            }
        });

        detecterWebcamEnArrierePlan();
    }

    /** Recherche la webcam sans jamais geler l'interface, avec un delai maximum de 5 secondes. */
    private void detecterWebcamEnArrierePlan() {
        SwingWorker<Webcam, Void> worker = new SwingWorker<>() {
            @Override
            protected Webcam doInBackground() {
                try {
                    List<Webcam> webcams = Webcam.getWebcams(5, TimeUnit.SECONDS);
                    return webcams.isEmpty() ? null : webcams.get(0);
                } catch (Exception ex) {
                    // Pilote indisponible, timeout, ou tout autre probleme materiel :
                    // on continue sans photo plutot que de faire planter l'application.
                    return null;
                }
            }

            @Override
            protected void done() {
                Webcam webcamTrouvee;
                try {
                    webcamTrouvee = get();
                } catch (Exception ex) {
                    webcamTrouvee = null;
                }
                afficherResultatDetection(webcamTrouvee);
            }
        };
        worker.execute();
    }

    private void afficherResultatDetection(Webcam webcamTrouvee) {
        contentContainer.removeAll();

        if (webcamTrouvee == null) {
            JLabel errorLabel = new JLabel("<html><div style='text-align:center'>"
                    + "Aucune webcam detectee ou accessible sur ce poste.<br><br>"
                    + "Vous pouvez continuer sans photo : le badge affichera<br>"
                    + "les initiales par defaut a la place."
                    + "</div></html>", SwingConstants.CENTER);
            errorLabel.setForeground(new Color(130, 130, 130));
            contentContainer.add(errorLabel, BorderLayout.CENTER);
            captureButton.setEnabled(false);
        } else {
            webcam = webcamTrouvee;
            appliquerResolutionSecurisee(webcam);

            webcamPanel = new WebcamPanel(webcam);
            webcamPanel.setFPSDisplayed(false);
            webcamPanel.setMirrored(true);
            contentContainer.add(webcamPanel, BorderLayout.CENTER);
            captureButton.setEnabled(true);
        }

        contentContainer.revalidate();
        contentContainer.repaint();
    }

    /**
     * Applique une resolution video reellement supportee par le pilote de la
     * webcam. Une resolution arbitraire (ex: 400x300) peut ne pas etre
     * supportee et provoquer une exception selon le materiel/pilote - on se
     * rabat donc sur des tailles standard connues (VGA, QVGA). Si aucune
     * n'est acceptee, on laisse la webcam sur sa resolution par defaut plutot
     * que de risquer une exception bloquante.
     */
    private void appliquerResolutionSecurisee(Webcam webcam) {
        Dimension[] taillesStandard = {
                WebcamResolution.VGA.getSize(),
                WebcamResolution.QVGA.getSize()
        };
        for (Dimension taille : taillesStandard) {
            try {
                webcam.setViewSize(taille);
                return;
            } catch (Exception ignored) {
                // Cette taille n'est pas supportee par le pilote, on essaie la suivante
            }
        }
    }

    private void capturer() {
        if (webcam == null) {
            return;
        }
        BufferedImage image = webcam.getImage();
        if (image == null) {
            JOptionPane.showMessageDialog(this, "Impossible de capturer l'image, reessayez.");
            return;
        }
        savedPhotoPath = PhotoStorage.save(image);
        fermerWebcam();
        dispose();
    }

    private void fermerWebcam() {
        if (webcamPanel != null) webcamPanel.stop();
        if (webcam != null && webcam.isOpen()) webcam.close();
    }

    /** @return le chemin du fichier photo enregistre, ou null si l'utilisateur a annule / pas de webcam. */
    public String getSavedPhotoPath() {
        return savedPhotoPath;
    }
}
