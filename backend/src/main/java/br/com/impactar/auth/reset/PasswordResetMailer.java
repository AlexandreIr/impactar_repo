package br.com.impactar.auth.reset;

import br.com.impactar.notification.EmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/**
 * E-mail com o link de redefinição. Assíncrono: a resposta de "esqueci minha senha" leva o mesmo
 * tempo com ou sem conta cadastrada (não revela quais e-mails existem). Falhas vão só para o log.
 */
@Component
public class PasswordResetMailer {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetMailer.class);

    private final EmailService emailService;

    public PasswordResetMailer(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    public void send(String to, String name, String resetLink, long validMinutes) {
        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;max-width:520px;margin:0 auto;color:#1f2937">
                  <h2 style="margin:0 0 16px">Olá, %s!</h2>
                  <p>Recebemos um pedido para redefinir a senha da sua conta na plataforma impactar.</p>
                  <p style="margin:24px 0">
                    <a href="%s" style="background:#111827;color:#ffffff;padding:10px 18px;border-radius:6px;text-decoration:none">Redefinir senha</a>
                  </p>
                  <p>O link é válido por <strong>%d minutos</strong> e só pode ser usado uma vez.</p>
                  <p style="font-size:12px;color:#6b7280">Se você não pediu a redefinição, ignore este e-mail: sua senha continua a mesma.</p>
                </div>
                """.formatted(HtmlUtils.htmlEscape(name), HtmlUtils.htmlEscape(resetLink), validMinutes);

        String text = """
                Olá, %s!

                Recebemos um pedido para redefinir a senha da sua conta na plataforma impactar.
                Acesse o link abaixo (válido por %d minutos, uso único):

                %s

                Se você não pediu a redefinição, ignore este e-mail: sua senha continua a mesma.
                """.formatted(name, validMinutes, resetLink);

        try {
            emailService.send(to, "Redefinição de senha — impactar", html, text);
        } catch (RuntimeException ex) {
            log.error("Falha ao enviar e-mail de redefinição de senha para {}", to, ex);
        }
    }
}
