package com.strata.presence.util;

import javax.swing.*;
import java.awt.*;
import java.util.function.IntConsumer;

/**
 * Petites animations reutilisables (fondu d'apparition de fenetre, compteurs
 * numeriques animes) pour donner une sensation plus "premium" a l'application,
 * au-dela du rendu Swing statique par defaut. Implementees avec javax.swing.Timer
 * (aucune bibliotheque d'animation externe necessaire).
 */
public final class AnimationUtils {

    private AnimationUtils() {}

    /**
     * Fait apparaitre une fenetre en fondu (opacite 0 -> 1) sur la duree indiquee.
     * Necessite que la fenetre supporte la translucidite (cas general sous
     * Windows/macOS/Linux avec un gestionnaire de fenetres compatible ; si non
     * supporte, la fenetre s'affiche simplement a pleine opacite sans erreur).
     */
    public static void fadeIn(Window window, int durationMs) {
        try {
            window.setOpacity(0f);
        } catch (UnsupportedOperationException | IllegalComponentStateException e) {
            return; // translucidite non supportee sur ce systeme : pas d'animation, pas d'erreur
        }

        final int stepMs = 15;
        final int steps = Math.max(1, durationMs / stepMs);
        final float increment = 1f / steps;

        Timer timer = new Timer(stepMs, null);
        timer.addActionListener(new java.awt.event.ActionListener() {
            float opacity = 0f;
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                opacity += increment;
                if (opacity >= 1f) {
                    opacity = 1f;
                    timer.stop();
                }
                try {
                    window.setOpacity(opacity);
                } catch (Exception ex) {
                    timer.stop();
                }
            }
        });
        timer.start();
    }

    /**
     * Anime une valeur numerique entiere de {@code from} a {@code to} sur la
     * duree indiquee, en appelant {@code onUpdate} a chaque etape. Utilise pour
     * les compteurs du tableau de bord (effet "count-up").
     */
    public static void animateValue(int from, int to, int durationMs, IntConsumer onUpdate) {
        if (from == to) {
            onUpdate.accept(to);
            return;
        }
        final int stepMs = 20;
        final int steps = Math.max(1, durationMs / stepMs);
        final double increment = (to - from) / (double) steps;

        Timer timer = new Timer(stepMs, null);
        timer.addActionListener(new java.awt.event.ActionListener() {
            int currentStep = 0;
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                currentStep++;
                int value = (int) Math.round(from + increment * currentStep);
                if (currentStep >= steps) {
                    value = to;
                    timer.stop();
                }
                onUpdate.accept(value);
            }
        });
        timer.start();
    }
}
