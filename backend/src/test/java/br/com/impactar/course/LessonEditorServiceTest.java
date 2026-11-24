package br.com.impactar.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.impactar.shared.ApiException;

import org.junit.jupiter.api.Test;

class LessonEditorServiceTest {

    @Test
    void videoUrlIsOptional() {
        assertNull(LessonEditorService.normalizeVideoUrl(null));
        assertNull(LessonEditorService.normalizeVideoUrl("   "));
    }

    @Test
    void videoUrlIsStoredInCanonicalForm() {
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                LessonEditorService.normalizeVideoUrl("https://youtu.be/dQw4w9WgXcQ?si=xyz"));
    }

    @Test
    void invalidVideoUrlIsRejected() {
        assertThrows(ApiException.class, () -> LessonEditorService.normalizeVideoUrl("https://vimeo.com/123"));
    }
}
