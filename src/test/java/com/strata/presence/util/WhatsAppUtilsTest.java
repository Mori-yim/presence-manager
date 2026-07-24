package com.strata.presence.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de non-regression pour WhatsAppUtils, suite au bug ou les numeros
 * locaux (sans indicatif pays) produisaient des liens wa.me invalides.
 */
class WhatsAppUtilsTest {

    @Test
    void numeroLocalCamerounaisRecoitLindicatif237() {
        String lien = WhatsAppUtils.buildWhatsAppLink("677123456");
        assertEquals("https://wa.me/237677123456", lien);
    }

    @Test
    void numeroAvecEspacesEtTiretsEstNettoye() {
        String lien = WhatsAppUtils.buildWhatsAppLink("677 12-34-56");
        assertEquals("https://wa.me/237677123456", lien);
    }

    @Test
    void numeroDejaAuFormatInternationalNestPasDouble() {
        String lien = WhatsAppUtils.buildWhatsAppLink("+237677123456");
        assertEquals("https://wa.me/237677123456", lien);
    }

    @Test
    void numeroAvecPrefixe00EstNormalise() {
        String lien = WhatsAppUtils.buildWhatsAppLink("00237677123456");
        assertEquals("https://wa.me/237677123456", lien);
    }

    @Test
    void numeroVideOuNullRetourneNull() {
        assertNull(WhatsAppUtils.buildWhatsAppLink(null));
        assertNull(WhatsAppUtils.buildWhatsAppLink(""));
        assertNull(WhatsAppUtils.buildWhatsAppLink("   "));
    }

    @Test
    void numeroSansAucunChiffreRetourneNull() {
        assertNull(WhatsAppUtils.buildWhatsAppLink("abc-def"));
    }
}
