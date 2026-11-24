package br.com.impactar.certificate;

import br.com.impactar.notification.EmailService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/** E-mail com o certificado em PDF anexado. */
@Component
public class CertificateMailer {

    private final EmailService emailService;
    private final String validationBaseUrl;

    public CertificateMailer(EmailService emailService, @Value("${app.frontend-url}") String frontendUrl) {
        this.emailService = emailService;
        this.validationBaseUrl = frontendUrl.replaceAll("/+$", "") + "/validar/";
    }

    public void send(String to, Certificate certificate, byte[] pdf) {
        String kind = certificate.getKind() == Certificate.Kind.TRAIL ? "da trilha" : "do curso";
        String validationUrl = validationBaseUrl + certificate.getVerificationCode();

        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;max-width:520px;margin:0 auto;color:#1f2937">
                  <h2 style="margin:0 0 16px">Certificado de conclusão</h2>
                  <p>Segue em anexo o certificado de <strong>%s</strong> referente à conclusão %s
                     <strong>%s</strong>, emitido pela plataforma impactar.</p>
                  <p>Código de verificação: <strong>%s</strong></p>
                  <p style="margin:24px 0">
                    <a href="%s" style="background:#111827;color:#ffffff;padding:10px 18px;border-radius:6px;text-decoration:none">Validar certificado</a>
                  </p>
                  <p style="font-size:12px;color:#6b7280">Se você não esperava este e-mail, ignore-o.</p>
                </div>
                """.formatted(
                HtmlUtils.htmlEscape(certificate.getHolderName()),
                kind,
                HtmlUtils.htmlEscape(certificate.getTitle()),
                HtmlUtils.htmlEscape(certificate.getVerificationCode()),
                HtmlUtils.htmlEscape(validationUrl));

        String text = """
                Certificado de conclusão

                Segue em anexo o certificado de %s referente à conclusão %s "%s", emitido pela plataforma impactar.

                Código de verificação: %s
                Validar: %s
                """.formatted(certificate.getHolderName(), kind, certificate.getTitle(),
                certificate.getVerificationCode(), validationUrl);

        emailService.send(to, "Certificado: " + certificate.getTitle(), html, text,
                new EmailService.Attachment(CertificatePdfRenderer.fileName(certificate), pdf, "application/pdf"));
    }
}
