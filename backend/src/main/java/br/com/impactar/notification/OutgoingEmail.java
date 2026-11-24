package br.com.impactar.notification;

/** Mensagem pronta para envio por qualquer provedor. attachment pode ser null. */
public record OutgoingEmail(
        String to,
        String subject,
        String html,
        String text,
        EmailService.Attachment attachment
) {
}
