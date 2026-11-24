package br.com.impactar.certificate;

import java.security.SecureRandom;
import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * Código de verificação no formato XXXX-XXXX-XXXX (sem 0/O, 1/I para facilitar a digitação).
 * 32^12 combinações: colisão é praticamente impossível, mas o emissor confere mesmo assim.
 */
@Component
public class CertificateCodeGenerator {

    static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int GROUPS = 3;
    private static final int GROUP_SIZE = 4;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder raw = new StringBuilder(GROUPS * GROUP_SIZE);
        for (int i = 0; i < GROUPS * GROUP_SIZE; i++) {
            raw.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return format(raw.toString());
    }

    /**
     * Aceita o código digitado com ou sem hífens, espaços e em minúsculas.
     * Retorna null se não tiver 12 caracteres válidos.
     */
    public static String normalize(String input) {
        if (input == null) {
            return null;
        }
        String raw = input.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        return raw.length() == GROUPS * GROUP_SIZE ? format(raw) : null;
    }

    private static String format(String raw) {
        return raw.substring(0, 4) + "-" + raw.substring(4, 8) + "-" + raw.substring(8, 12);
    }
}
