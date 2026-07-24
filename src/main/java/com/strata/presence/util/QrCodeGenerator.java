package com.strata.presence.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

/**
 * Generation de QR Code pour les badges (visiteurs, employes, stagiaires).
 * Le contenu encode est, par ordre de priorite : un lien WhatsApp ("wa.me")
 * construit a partir du numero de telephone de la personne (voir
 * {@link com.strata.presence.util.WhatsAppUtils}), ou a defaut l'identifiant
 * unique (UUID) genere a la creation de la personne.
 */
public final class QrCodeGenerator {

    private QrCodeGenerator() {}

    public static BufferedImage generate(String content, int size) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            return MatrixToImageWriter.toBufferedImage(matrix);
        } catch (WriterException e) {
            throw new RuntimeException("Erreur lors de la generation du QR Code", e);
        }
    }
}
