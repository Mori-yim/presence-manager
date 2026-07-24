package com.strata.presence.util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * Notifications sonores generees a la volee (aucun fichier .wav externe requis,
 * donc rien a embarquer ni a distribuer). Utilise pour confirmer une action a
 * l'agent d'accueil sans qu'il ait besoin de regarder l'ecran en permanence.
 */
public final class SoundUtils {

    private static volatile boolean enabled = true;

    private SoundUtils() {}

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /** Petit "bip" clair et bref, joue apres un enregistrement d'arrivee reussi. */
    public static void playSuccess() {
        if (!enabled) return;
        playTone(880, 90);
    }

    /** Tonalite plus grave, jouee en cas d'erreur ou d'action refusee. */
    public static void playError() {
        if (!enabled) return;
        playTone(220, 180);
    }

    private static void playTone(int frequencyHz, int durationMs) {
        // Joue dans un thread separe pour ne jamais bloquer l'interface (EDT)
        Thread soundThread = new Thread(() -> {
            try {
                float sampleRate = 44100f;
                int samples = (int) (sampleRate * durationMs / 1000);
                byte[] buffer = new byte[samples];
                for (int i = 0; i < samples; i++) {
                    double angle = 2.0 * Math.PI * i * frequencyHz / sampleRate;
                    // Enveloppe simple (fade-out) pour eviter un clic sec en fin de son
                    double envelope = 1.0 - (double) i / samples;
                    buffer[i] = (byte) (Math.sin(angle) * 100 * envelope);
                }
                AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, true);
                try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                    line.open(format);
                    line.start();
                    line.write(buffer, 0, buffer.length);
                    line.drain();
                }
            } catch (Exception e) {
                // Environnement sans peripherique audio (ex: certains serveurs de test) :
                // on ignore silencieusement, ce n'est jamais bloquant pour l'application.
            }
        });
        soundThread.setDaemon(true);
        soundThread.start();
    }
}
