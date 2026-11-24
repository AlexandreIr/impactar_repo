package br.com.impactar.notification;

/** Um provedor de envio (API HTTP do Brevo, SMTP do Gmail...). Lança RuntimeException se falhar. */
public interface MailTransport {

    String name();

    void send(OutgoingEmail email);
}
