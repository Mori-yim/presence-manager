package com.strata.presence.util;

import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Sauvegarde les photos capturees (webcam) sur disque, dans data/photos,
 * apres redimensionnement (Thumbnailator) pour limiter la taille des fichiers
 * et garder un rendu net dans les badges.
 */
public final class PhotoStorage {

    private static final int MAX_SIZE = 400; // pixels, cote le plus grand
    private static final Path PHOTOS_DIR = Paths.get("data", "photos");

    private PhotoStorage() {}

    public static String save(BufferedImage image) {
        try {
            Files.createDirectories(PHOTOS_DIR);
            String filename = UUID.randomUUID() + ".jpg";
            File target = PHOTOS_DIR.resolve(filename).toFile();
            Thumbnails.of(image)
                    .size(MAX_SIZE, MAX_SIZE)
                    .outputFormat("jpg")
                    .outputQuality(0.9)
                    .toFile(target);
            return target.getPath();
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'enregistrer la photo", e);
        }
    }

    public static BufferedImage load(String path) {
        try {
            if (path == null || path.isBlank()) return null;
            File file = new File(path);
            if (!file.exists()) return null;
            return ImageIO.read(file);
        } catch (IOException e) {
            return null;
        }
    }
}
