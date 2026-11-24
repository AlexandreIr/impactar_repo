package br.com.impactar.notification;

/** Nenhum provedor SMTP conseguiu entregar a mensagem. */
public class EmailDeliveryException extends RuntimeException {

    public EmailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
