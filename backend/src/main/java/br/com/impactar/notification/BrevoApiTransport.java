package br.com.impactar.notification;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Envio pela API HTTP do Brevo (POST /v3/smtp/email, porta 443).
 * Funciona onde as portas SMTP estão bloqueadas (ex.: plano free do Render).
 */
public class BrevoApiTransport implements MailTransport {

    private final RestClient client;
    private final String fromEmail;
    private final String fromName;

    /** O builder já deve vir com timeouts configurados (ver MailConfig). */
    public BrevoApiTransport(RestClient.Builder builder, String baseUrl, String apiKey,
                             String fromEmail, String fromName) {
        this.client = builder
                .baseUrl(baseUrl)
                .defaultHeader("api-key", apiKey)
                .defaultHeader("accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }

    @Override
    public String name() {
        return "Brevo API";
    }

    @Override
    public void send(OutgoingEmail email) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sender", Map.of("name", fromName, "email", fromEmail));
        body.put("to", List.of(Map.of("email", email.to())));
        body.put("subject", email.subject());
        body.put("htmlContent", email.html());
        if (email.text() != null) {
            body.put("textContent", email.text());
        }
        if (email.attachment() != null) {
            body.put("attachment", List.of(Map.of(
                    "name", email.attachment().fileName(),
                    "content", Base64.getEncoder().encodeToString(email.attachment().content()))));
        }

        try {
            client.post()
                    .uri("/smtp/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            // Corpo do erro do Brevo (ex.: chave inválida, IP não autorizado, remetente não verificado)
            throw new IllegalStateException(
                    "Brevo respondeu " + ex.getStatusCode().value() + ": " + ex.getResponseBodyAsString(), ex);
        }
    }
}
