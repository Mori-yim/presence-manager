package com.strata.presence.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * Chargement centralise de l'icone de l'application (utilisee sur toutes les
 * fenetres : barre de titre, barre des taches, boites de dialogue).
 */
public final class AppIcons {

    private static Image cached;

    private AppIcons() {}

    public static Image getAppIcon() {
        if (cached == null) {
            try (InputStream is = AppIcons.class.getResourceAsStream("/icon.png")) {
                if (is != null) {
                    cached = ImageIO.read(is);
                }
            } catch (IOException e) {
                cached = null;
            }
        }
        return cached;
    }

    public static void apply(Window window) {
        Image icon = getAppIcon();
        if (icon != null) {
            window.setIconImage(icon);
        }
    }
}
