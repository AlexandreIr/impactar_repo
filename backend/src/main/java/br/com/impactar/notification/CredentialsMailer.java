package br.com.impactar.notification;


import br.com.impactar.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/** E-mails com login e senha provisória (boas-vindas e reenvio de acesso). */
@Component
public class CredentialsMailer {

    private final EmailService emailService;
    private final String loginUrl;

    public CredentialsMailer(EmailService emailService, @Value("${app.frontend-url}") String frontendUrl) {
        this.emailService = emailService;
        this.loginUrl = frontendUrl.replaceAll("/+$", "") + "/login";
    }

    public void sendWelcome(User user, String temporaryPassword) {
        send(user, temporaryPassword,
                "Seu acesso à plataforma impactar",
                "Sua conta na plataforma impactar foi criada pela administração.");
    }

    public void sendPasswordReset(User user, String temporaryPassword) {
        send(user, temporaryPassword,
                "Nova senha provisória — impactar",
                "A administração gerou uma nova senha provisória para a sua conta.");
    }

    private void send(User user, String temporaryPassword, String subject, String intro) {
        String name = HtmlUtils.htmlEscape(user.getName());
        String email = HtmlUtils.htmlEscape(user.getEmail());
        String password = HtmlUtils.htmlEscape(temporaryPassword);
        String url = HtmlUtils.htmlEscape(loginUrl);

        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;max-width:520px;margin:0 auto;color:#1f2937">
                  <h2 style="margin:0 0 16px">Olá, %s!</h2>
                  <p>%s</p>
                  <table style="margin:16px 0;border-collapse:collapse">
                    <tr><td style="padding:4px 12px 4px 0;color:#6b7280">Login</td><td><strong>%s</strong></td></tr>
                    <tr><td style="padding:4px 12px 4px 0;color:#6b7280">Senha provisória</td>
                        <td><code style="font-size:16px;background:#f3f4f6;padding:2px 6px;border-radius:4px">%s</code></td></tr>
                  </table>
                  <p>No primeiro acesso você precisará criar uma nova senha.</p>
                  <p style="margin:24px 0">
                    <a href="%s" style="background:#111827;color:#ffffff;padding:10px 18px;border-radius:6px;text-decoration:none">Acessar a plataforma</a>
                  </p>
                  <p style="font-size:12px;color:#6b7280">Se você não esperava este e-mail, ignore-o.</p>
                </div>
                """.formatted(name, intro, email, password, url);

        String text = """
                Olá, %s!

                %s

                Login: %s
                Senha provisória: %s

                No primeiro acesso você precisará criar uma nova senha.
                Acesse: %s
                """.formatted(user.getName(), intro, user.getEmail(), temporaryPassword, loginUrl);

        emailService.send(user.getEmail(), subject, html, text);
    }
}
