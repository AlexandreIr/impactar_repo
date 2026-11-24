package br.com.impactar.notification;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Envio de e-mail com fallback: tenta cada provedor na ordem (Brevo API -> Gmail SMTP)
 * até um funcionar. Criado em MailConfig com os provedores que têm credenciais.
 */
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    /** Arquivo anexado ao e-mail (ex.: certificado em PDF). */
    public record Attachment(String fileName, byte[] content, String contentType) {
    }

    private final List<MailTransport> transports;

    public EmailService(List<MailTransport> transports) {
        this.transports = List.copyOf(transports);
        if (this.transports.isEmpty()) {
            log.warn("Nenhum provedor de e-mail configurado: e-mails não serão enviados.");
        } else {
            log.info("Provedores de e-mail, em ordem: {}",
                    this.transports.stream().map(MailTransport::name).toList());
        }
    }

    public void send(String to, String subject, String html, String text) {
        send(to, subject, html, text, null);
    }

    public void send(String to, String subject, String html, String text, Attachment attachment) {
        OutgoingEmail email = new OutgoingEmail(to, subject, html, text, attachment);
        RuntimeException lastError = null;

        for (MailTransport transport : transports) {
            try {
                transport.send(email);
                log.info("E-mail '{}' enviado para {} via {}", subject, to, transport.name());
                return;
            } catch (RuntimeException ex) {
                lastError = ex;
                log.warn("Falha ao enviar e-mail via {}: {}", transport.name(), ex.getMessage());
            }
        }

        throw new EmailDeliveryException("Não foi possível enviar e-mail para " + to, lastError);
    }
}
