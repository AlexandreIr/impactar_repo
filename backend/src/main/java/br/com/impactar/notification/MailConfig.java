package br.com.impactar.notification;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** Monta o EmailService com os provedores configurados, na ordem de prioridade. */
@Configuration
public class MailConfig {

    private static final Logger log = LoggerFactory.getLogger(MailConfig.class);

    @Bean
    EmailService emailService(MailProperties properties) {
        List<MailTransport> transports = new ArrayList<>();

        if (properties.brevo() != null && properties.brevo().isConfigured()) {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(Duration.ofSeconds(5));
            requestFactory.setReadTimeout(Duration.ofSeconds(15));

            transports.add(new BrevoApiTransport(
                    RestClient.builder().requestFactory(requestFactory),
                    properties.brevo().baseUrl(),
                    properties.brevo().apiKey(),
                    properties.brevo().from(),
                    properties.fromName()));
        } else {
            log.warn("Brevo API sem credenciais (BREVO_API_KEY/BREVO_FROM): ignorado.");
        }

        if (properties.gmail() != null && properties.gmail().isConfigured()) {
            transports.add(new SmtpTransport("Gmail SMTP", properties.gmail(), properties.fromName()));
        } else {
            log.warn("Gmail SMTP sem credenciais (GMAIL_USERNAME/GMAIL_APP_PASSWORD): ignorado.");
        }

        return new EmailService(transports);
    }
}
