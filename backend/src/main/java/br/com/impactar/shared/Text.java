package br.com.impactar.shared;

public final class Text {

    private Text() {
    }

    /** Remove espaços nas pontas; texto vazio vira null. */
    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
