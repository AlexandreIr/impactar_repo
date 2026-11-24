package br.com.impactar.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class EmailServiceTest {

    /** Provedor falso: registra os envios ou falha sempre. */
    private static final class FakeTransport implements MailTransport {
        private final String name;
        private final boolean fails;
        private final List<OutgoingEmail> sent = new ArrayList<>();

        FakeTransport(String name, boolean fails) {
            this.name = name;
            this.fails = fails;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public void send(OutgoingEmail email) {
            if (fails) {
                throw new IllegalStateException(name + " fora do ar");
            }
            sent.add(email);
        }
    }

    @Test
    void usesFirstProviderWhenItWorks() {
        FakeTransport brevo = new FakeTransport("Brevo", false);
        FakeTransport gmail = new FakeTransport("Gmail", false);

        new EmailService(List.of(brevo, gmail)).send("ana@impactar.com.br", "Assunto", "<p>x</p>", "x");

        assertEquals(1, brevo.sent.size());
        assertEquals(0, gmail.sent.size());
    }

    @Test
    void fallsBackToNextProvider() {
        FakeTransport brevo = new FakeTransport("Brevo", true);
        FakeTransport gmail = new FakeTransport("Gmail", false);

        new EmailService(List.of(brevo, gmail)).send("ana@impactar.com.br", "Assunto", "<p>x</p>", "x");

        assertEquals(1, gmail.sent.size());
        assertEquals("ana@impactar.com.br", gmail.sent.getFirst().to());
    }

    @Test
    void throwsWhenAllProvidersFail() {
        EmailService service = new EmailService(List.of(new FakeTransport("Brevo", true), new FakeTransport("Gmail", true)));

        assertThrows(EmailDeliveryException.class,
                () -> service.send("ana@impactar.com.br", "Assunto", "<p>x</p>", "x"));
    }

    @Test
    void throwsWhenNoProviderConfigured() {
        assertThrows(EmailDeliveryException.class,
                () -> new EmailService(List.of()).send("ana@impactar.com.br", "Assunto", "<p>x</p>", "x"));
    }
}
