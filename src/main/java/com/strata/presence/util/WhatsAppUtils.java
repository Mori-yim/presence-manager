package com.strata.presence.util;

/**
 * Construit un lien WhatsApp ("wa.me") a partir d'un numero de telephone, pour
 * que le QR Code du badge ouvre directement une conversation WhatsApp avec la
 * personne (visiteur, employe ou stagiaire) au lieu d'un simple identifiant
 * technique.
 *
 * IMPORTANT : un lien wa.me ne fonctionne que si le numero est complet au
 * format international (indicatif pays inclus, sans le "+"). Un numero local
 * saisi sans indicatif (ex: "677123456") ne pointera vers aucune conversation
 * valide. Par defaut, cette classe complete automatiquement les numeros locaux
 * camerounais (9 chiffres, sans indicatif) avec l'indicatif +237.
 */
public final class WhatsAppUtils {

    /** Indicatif pays par defaut applique aux numeros locaux (sans indicatif) - Cameroun. */
    private static final String INDICATIF_PAR_DEFAUT = "237";

    private WhatsAppUtils() {}

    /**
     * @param telephone numero de telephone tel que saisi (espaces, tirets, +, 00, etc. tolerès)
     * @return un lien "https://wa.me/<numero international>" pret pour un QR Code,
     *         ou null si le numero est vide/invalide (aucun chiffre exploitable)
     */
    public static String buildWhatsAppLink(String telephone) {
        String numero = toInternationalDigits(telephone);
        if (numero == null) {
            return null;
        }
        return "https://wa.me/" + numero;
    }

    /**
     * Normalise un numero saisi en un numero international (digits uniquement,
     * sans "+"), en completant l'indicatif Cameroun (237) si le numero semble
     * saisi en format local (9 chiffres, commencant typiquement par 6).
     */
    public static String toInternationalDigits(String telephone) {
        if (telephone == null || telephone.isBlank()) {
            return null;
        }
        String digits = telephone.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return null;
        }

        // Prefixe de numerotation internationale "00" (ex: 00237677123456) -> on l'enleve
        if (digits.startsWith("00")) {
            digits = digits.substring(2);
        }

        // Deja au format international avec l'indicatif Cameroun (12 chiffres : 237 + 9 chiffres)
        if (digits.startsWith(INDICATIF_PAR_DEFAUT) && digits.length() == 12) {
            return digits;
        }

        // Numero local camerounais typique (9 chiffres, ex: 677123456) : on complete l'indicatif
        if (digits.length() == 9) {
            return INDICATIF_PAR_DEFAUT + digits;
        }

        // Autre format (numero deja international pour un autre pays, ou saisie inhabituelle) :
        // on le laisse tel quel plutot que de risquer de le corrompre.
        return digits;
    }
}
