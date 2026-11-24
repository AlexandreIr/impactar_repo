package br.com.impactar.certificate;

import java.util.List;
import java.util.UUID;

import br.com.impactar.auth.CurrentUser;
import br.com.impactar.certificate.dto.CertificateResponse;
import br.com.impactar.certificate.dto.CertificateVerificationResponse;
import br.com.impactar.notification.EmailDeliveryException;
import br.com.impactar.shared.ApiException;
import br.com.impactar.shared.Text;
import br.com.impactar.user.User;
import br.com.impactar.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final CertificateIssuer issuer;
    private final CertificatePdfRenderer pdfRenderer;
    private final CertificateMailer mailer;
    private final UserRepository userRepository;

    public CertificateService(CertificateRepository certificateRepository,
                              CertificateIssuer issuer,
                              CertificatePdfRenderer pdfRenderer,
                              CertificateMailer mailer,
                              UserRepository userRepository) {
        this.certificateRepository = certificateRepository;
        this.issuer = issuer;
        this.pdfRenderer = pdfRenderer;
        this.mailer = mailer;
        this.userRepository = userRepository;
    }

    /** Lista os certificados do usuário, emitindo antes os que estiverem pendentes. */
    @Transactional
    public List<CertificateResponse> listMine(CurrentUser user) {
        issuer.issuePending(user.id());
        return certificateRepository.findByUserIdOrderByIssuedAtDesc(user.id()).stream()
                .map(CertificateResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Certificate getMine(UUID certificateId, CurrentUser user) {
        return certificateRepository.findByIdAndUserId(certificateId, user.id())
                .orElseThrow(() -> ApiException.notFound("Certificado não encontrado."));
    }

    public byte[] renderPdf(Certificate certificate) {
        return pdfRenderer.render(certificate);
    }

    /**
     * Envia o PDF para o e-mail informado ou, se vazio, para o e-mail do próprio usuário. Retorna o destino.
     * Sem transação: não segura conexão do banco durante o envio SMTP.
     */
    public String sendByEmail(UUID certificateId, String email, CurrentUser user) {
        Certificate certificate = getMine(certificateId, user);
        String to = Text.trimToNull(email);
        if (to == null) {
            to = userRepository.findById(user.id())
                    .map(User::getEmail)
                    .orElseThrow(() -> ApiException.unauthorized("Sessão inválida. Faça login novamente."));
        }

        try {
            mailer.send(to, certificate, pdfRenderer.render(certificate));
        } catch (EmailDeliveryException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Não foi possível enviar o e-mail. Tente novamente.");
        }
        return to;
    }

    @Transactional(readOnly = true)
    public CertificateVerificationResponse verify(String code) {
        String normalized = CertificateCodeGenerator.normalize(code);
        if (normalized == null) {
            throw ApiException.notFound("Certificado não encontrado.");
        }
        return certificateRepository.findByVerificationCode(normalized)
                .map(CertificateVerificationResponse::from)
                .orElseThrow(() -> ApiException.notFound("Certificado não encontrado."));
    }
}
