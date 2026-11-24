package br.com.impactar.certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CertificatePdfRendererTest {

    private final CertificatePdfRenderer renderer = new CertificatePdfRenderer("https://impactar.com.br/");

    @Test
    void rendersValidPdfWithAccents() {
        byte[] pdf = renderer.render(certificate("Avaliação Formativa na Educação Básica", 20));

        assertTrue(pdf.length > 1000);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    void rendersLongTitleAndTrailWithoutWorkload() {
        Certificate trail = Certificate.forTrail(UUID.randomUUID(), UUID.randomUUID(), "ABCD-EFGH-JKLM",
                "Maria Aparecida dos Santos Oliveira", "Trilha ".repeat(28).trim(), null);
        ReflectionTestUtils.setField(trail, "issuedAt", Instant.parse("2026-11-24T15:00:00Z"));

        byte[] pdf = renderer.render(trail);

        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    void buildsAsciiFileName() {
        assertEquals("certificado-avaliacao-formativa-na-educacao-basica.pdf",
                CertificatePdfRenderer.fileName(certificate("Avaliação Formativa na Educação Básica!", 10)));
    }

    private static Certificate certificate(String title, Integer hours) {
        Certificate certificate = Certificate.forCourse(UUID.randomUUID(), UUID.randomUUID(), "ABCD-EFGH-JKLM",
                "João da Silva", title, hours);
        ReflectionTestUtils.setField(certificate, "issuedAt", Instant.parse("2026-11-24T15:00:00Z"));
        return certificate;
    }
}
