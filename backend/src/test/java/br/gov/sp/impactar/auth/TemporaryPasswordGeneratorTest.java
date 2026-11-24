package br.gov.sp.impactar.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import br.com.impactar.auth.TemporaryPasswordGenerator;
import org.junit.jupiter.api.Test;

class TemporaryPasswordGeneratorTest {

    private final TemporaryPasswordGenerator generator = new TemporaryPasswordGenerator();

    @Test
    void generatesPasswordsWithRequiredCharacterClasses() {
        for (int i = 0; i < 200; i++) {
            String password = generator.generate();
            assertEquals(TemporaryPasswordGenerator.LENGTH, password.length());
            assertTrue(password.chars().anyMatch(Character::isUpperCase), password);
            assertTrue(password.chars().anyMatch(Character::isLowerCase), password);
            assertTrue(password.chars().anyMatch(Character::isDigit), password);
            assertTrue(password.chars().noneMatch(c -> "0O1lI".indexOf(c) >= 0), password);
        }
    }

    @Test
    void generatesDistinctPasswords() {
        Set<String> passwords = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            passwords.add(generator.generate());
        }
        assertEquals(200, passwords.size());
    }
}
