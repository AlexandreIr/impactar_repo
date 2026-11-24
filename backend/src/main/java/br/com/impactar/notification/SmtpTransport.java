package br.com.impactar.notification;

import java.util.Properties;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Envio por SMTP com STARTTLS (Gmail com senha de app).
 * Atenção: o plano free do Render bloqueia as portas SMTP (25/465/587); lá este provedor falha.
 */
public class SmtpTransport implements MailTransport {

    private final String name;
    private final JavaMailSenderImpl sender;
    private final String fromEmail;
    private final String fromName;

    public SmtpTransport(String name, MailProperties.SmtpAccount account, String fromName) {
        this.name = name;
        this.fromEmail = account.from();
        this.fromName = fromName;

        this.sender = new JavaMailSenderImpl();
        sender.setHost(account.host());
        sender.setPort(account.port());
        sender.setUsername(account.username());
        sender.setPassword(account.password());
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void send(OutgoingEmail email) {
        sender.send(mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(email.to());
            helper.setSubject(email.subject());
            helper.setText(email.text() == null ? "" : email.text(), email.html());
            if (email.attachment() != null) {
                helper.addAttachment(email.attachment().fileName(),
                        new ByteArrayResource(email.attachment().content()), email.attachment().contentType());
            }
        });
    }
}
