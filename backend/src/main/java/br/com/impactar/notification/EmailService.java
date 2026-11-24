package br.com.impactar.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

/**
 * Envio de e-mail com fallback: tenta o provedor principal (Brevo) e,
 * se falhar, o secundário (Gmail). Provedores sem credenciais são ignorados.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private record Provider(String name, JavaMailSender sender, String from) {
    }

    private final List<Provider> providers = new ArrayList<>();
    private final String fromName;

    public EmailService(MailProperties properties) {
        this.fromName = properties.fromName();
        register("Brevo", properties.primary());
        register("Gmail", properties.fallback());
        if (providers.isEmpty()) {
            log.warn("Nenhum provedor SMTP configurado: e-mails não serão enviados.");
        }
    }

    public void send(String to, String subject, String html, String text) {
        MailException lastError = null;

        for (Provider provider : providers) {
            MimeMessagePreparator preparator = mimeMessage -> {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(provider.from(), fromName);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(text, html);
            };
            try {
                provider.sender().send(preparator);
                log.info("E-mail '{}' enviado para {} via {}", subject, to, provider.name());
                return;
            } catch (MailException ex) {
                lastError = ex;
                log.warn("Falha ao enviar e-mail via {}: {}", provider.name(), ex.getMessage());
            }
        }

        throw new EmailDeliveryException("Não foi possível enviar e-mail para " + to, lastError);
    }

    private void register(String name, MailProperties.SmtpAccount account) {
        if (account == null || !account.isConfigured()) {
            log.warn("Provedor SMTP {} sem credenciais: ignorado.", name);
            return;
        }
        providers.add(new Provider(name, buildSender(account), account.from()));
    }

    private static JavaMailSender buildSender(MailProperties.SmtpAccount account) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
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
        return sender;
    }
}
