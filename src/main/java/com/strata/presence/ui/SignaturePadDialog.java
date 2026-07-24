package com.strata.presence.ui;

import com.strata.presence.util.AppIcons;
import com.strata.presence.util.PhotoStorage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

/**
 * Fenetre de capture de signature numerique : le visiteur ou l'employe signe
 * directement a l'ecran (souris ou ecran tactile), l'image est ensuite
 * enregistree comme une photo (meme mecanisme de stockage).
 */
public class SignaturePadDialog extends JDialog {

    private String savedSignaturePath;
    private BufferedImage canvas;
    private Graphics2D canvasGraphics;
    private int lastX = -1, lastY = -1;

    public SignaturePadDialog(Window owner) {
        super(owner, "Signature numerique", ModalityType.APPLICATION_MODAL);
        setSize(520, 400);
        setLocationRelativeTo(owner);
        AppIcons.apply(this);

        canvas = new BufferedImage(480, 260, BufferedImage.TYPE_INT_ARGB);
        canvasGraphics = canvas.createGraphics();
        canvasGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvasGraphics.setColor(Color.WHITE);
        canvasGraphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvasGraphics.setColor(Color.BLACK);
        canvasGraphics.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        JPanel drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(canvas, 0, 0, null);
                g.setColor(new Color(200, 200, 200));
                g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }
        };
        drawingPanel.setPreferredSize(new Dimension(480, 260));
        drawingPanel.setBackground(Color.WHITE);

        drawingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                lastX = -1;
                lastY = -1;
            }
        });
        drawingPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastX >= 0) {
                    canvasGraphics.drawLine(lastX, lastY, e.getX(), e.getY());
                    drawingPanel.repaint();
                }
                lastX = e.getX();
                lastY = e.getY();
            }
        });

        JLabel instructions = new JLabel("Faites signer la personne ci-dessous avec la souris ou un ecran tactile.",
                SwingConstants.CENTER);
        instructions.setForeground(new Color(130, 130, 130));
        instructions.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton clearButton = new JButton("Effacer");
        clearButton.addActionListener(e -> {
            canvasGraphics.setColor(Color.WHITE);
            canvasGraphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            canvasGraphics.setColor(Color.BLACK);
            drawingPanel.repaint();
        });

        JButton validateButton = new JButton("Valider la signature");
        validateButton.putClientProperty("JButton.buttonType", "roundRect");
        validateButton.addActionListener(e -> {
            savedSignaturePath = PhotoStorage.save(canvas);
            dispose();
        });

        JButton cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttons.add(clearButton);
        buttons.add(validateButton);
        buttons.add(cancelButton);

        setLayout(new BorderLayout());
        add(instructions, BorderLayout.NORTH);
        add(drawingPanel, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    /** @return le chemin du fichier signature enregistre, ou null si l'utilisateur a annule. */
    public String getSavedSignaturePath() {
        return savedSignaturePath;
    }
}
