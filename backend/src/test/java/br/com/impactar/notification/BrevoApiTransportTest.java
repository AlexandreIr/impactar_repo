package br.com.impactar.notification;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class BrevoApiTransportTest {

    private static final String URL = "https://api.brevo.com/v3/smtp/email";

    private MockRestServiceServer server;
    private BrevoApiTransport transport;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        transport = new BrevoApiTransport(builder, "https://api.brevo.com/v3", "chave-teste",
                "remetente@gmail.com", "impactar");
    }

    @Test
    void sendsJsonWithApiKeyAndBase64Attachment() {
        byte[] pdf = "%PDF-teste".getBytes(StandardCharsets.US_ASCII);

        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("api-key", "chave-teste"))
                .andExpect(jsonPath("$.sender.email").value("remetente@gmail.com"))
                .andExpect(jsonPath("$.sender.name").value("impactar"))
                .andExpect(jsonPath("$.to[0].email").value("ana@impactar.com.br"))
                .andExpect(jsonPath("$.subject").value("Certificado"))
                .andExpect(jsonPath("$.htmlContent").value("<p>Olá</p>"))
                .andExpect(jsonPath("$.textContent").value("Olá"))
                .andExpect(jsonPath("$.attachment[0].name").value("certificado.pdf"))
                .andExpect(jsonPath("$.attachment[0].content").value(Base64.getEncoder().encodeToString(pdf)))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"messageId\":\"<1@relay.brevo.com>\"}"));

        transport.send(new OutgoingEmail("ana@impactar.com.br", "Certificado", "<p>Olá</p>", "Olá",
                new EmailService.Attachment("certificado.pdf", pdf, "application/pdf")));

        server.verify();
    }

    @Test
    void omitsAttachmentWhenAbsent() {
        server.expect(requestTo(URL))
                .andExpect(jsonPath("$.attachment").doesNotExist())
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body("{}"));

        transport.send(new OutgoingEmail("ana@impactar.com.br", "Acesso", "<p>Oi</p>", "Oi", null));

        server.verify();
    }

    @Test
    void errorIncludesBrevoResponseBody() {
        server.expect(requestTo(URL))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":\"unauthorized\",\"message\":\"unrecognised IP address\"}"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> transport.send(new OutgoingEmail("ana@impactar.com.br", "Acesso", "<p>Oi</p>", "Oi", null)));

        assertTrue(ex.getMessage().contains("401"), ex.getMessage());
        assertTrue(ex.getMessage().contains("unrecognised IP address"), ex.getMessage());
    }
}
