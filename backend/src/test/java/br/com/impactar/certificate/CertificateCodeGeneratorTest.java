package br.com.impactar.certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class CertificateCodeGeneratorTest {

    private final CertificateCodeGenerator generator = new CertificateCodeGenerator();

    @Test
    void generatesFormattedCodesWithoutAmbiguousCharacters() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            String code = generator.generate();
            assertTrue(code.matches("^[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}$"), code);
            codes.add(code);
        }
        assertEquals(500, codes.size());
    }

    @Test
    void normalizesTypedCodes() {
        assertEquals("ABCD-EFGH-JKLM", CertificateCodeGenerator.normalize("abcd-efgh-jklm"));
        assertEquals("ABCD-EFGH-JKLM", CertificateCodeGenerator.normalize(" ABCDEFGHJKLM "));
        assertEquals("ABCD-EFGH-JKLM", CertificateCodeGenerator.normalize("abcd efgh jklm"));
    }

    @Test
    void rejectsInvalidCodes() {
        assertNull(CertificateCodeGenerator.normalize(null));
        assertNull(CertificateCodeGenerator.normalize(""));
        assertNull(CertificateCodeGenerator.normalize("ABCD-EFGH"));
        assertNull(CertificateCodeGenerator.normalize("ABCD-EFGH-JKLM-NPQR"));
    }
}
