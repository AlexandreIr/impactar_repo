package br.com.impactar.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class YouTubeTest {

    private static final String ID = "dQw4w9WgXcQ";

    @Test
    void extractsIdFromSupportedFormats() {
        List<String> urls = List.of(
                "https://www.youtube.com/watch?v=" + ID,
                "https://youtube.com/watch?feature=share&v=" + ID + "&t=42",
                "http://m.youtube.com/watch?v=" + ID,
                "https://youtu.be/" + ID,
                "https://youtu.be/" + ID + "?si=abc",
                "https://www.youtube.com/embed/" + ID,
                "https://www.youtube-nocookie.com/embed/" + ID,
                "https://www.youtube.com/shorts/" + ID,
                "https://www.youtube.com/live/" + ID,
                "www.youtube.com/watch?v=" + ID,
                "  https://youtu.be/" + ID + "  ");

        for (String url : urls) {
            assertEquals(Optional.of(ID), YouTube.extractVideoId(url), url);
        }
    }

    @Test
    void rejectsInvalidLinks() {
        List<String> urls = List.of(
                "",
                "   ",
                "https://vimeo.com/123456",
                "https://www.youtube.com/watch?v=curto",
                "https://www.youtube.com/channel/UCxyz",
                "https://drive.google.com/file/d/abc/view",
                "não é um link");

        for (String url : urls) {
            assertTrue(YouTube.extractVideoId(url).isEmpty(), url);
        }
    }

    @Test
    void buildsCanonicalUrl() {
        assertEquals("https://www.youtube.com/watch?v=" + ID, YouTube.canonicalUrl(ID));
    }
}
