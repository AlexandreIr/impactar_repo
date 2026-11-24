package br.com.impactar.course;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Extrai o id do vídeo de links do YouTube:
 *   youtube.com/watch?v=ID · youtu.be/ID · youtube.com/embed/ID · /shorts/ID · /live/ID
 */
public final class YouTube {

    private static final Pattern VIDEO_ID = Pattern.compile("^[A-Za-z0-9_-]{11}$");

    private YouTube() {
    }

    public static Optional<String> extractVideoId(String url) {
        if (url == null || url.isBlank()) {
            return Optional.empty();
        }
        String value = url.trim();
        if (!value.matches("(?i)^https?://.*")) {
            value = "https://" + value;
        }

        URI uri;
        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
        if (uri.getHost() == null) {
            return Optional.empty();
        }

        String host = uri.getHost().toLowerCase(Locale.ROOT).replaceFirst("^(www\\.|m\\.|music\\.)", "");
        String path = uri.getPath() == null ? "" : uri.getPath();
        String[] segments = path.split("/");
        String candidate = null;

        if (host.equals("youtu.be")) {
            candidate = segments.length > 1 ? segments[1] : null;
        } else if (host.equals("youtube.com") || host.equals("youtube-nocookie.com")) {
            if (path.equals("/watch")) {
                candidate = queryParam(uri.getRawQuery(), "v");
            } else if (segments.length > 2
                    && (segments[1].equals("embed") || segments[1].equals("shorts")
                    || segments[1].equals("live") || segments[1].equals("v"))) {
                candidate = segments[2];
            }
        }

        return candidate != null && VIDEO_ID.matcher(candidate).matches()
                ? Optional.of(candidate)
                : Optional.empty();
    }

    public static String canonicalUrl(String videoId) {
        return "https://www.youtube.com/watch?v=" + videoId;
    }

    private static String queryParam(String rawQuery, String name) {
        if (rawQuery == null) {
            return null;
        }
        for (String pair : rawQuery.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && pair.substring(0, eq).equals(name)) {
                return pair.substring(eq + 1);
            }
        }
        return null;
    }
}
