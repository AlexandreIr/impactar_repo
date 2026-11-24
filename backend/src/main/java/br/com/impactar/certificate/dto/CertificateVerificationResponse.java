package br.com.impactar.certificate.dto;

import java.time.Instant;

import br.com.impactar.certificate.Certificate;

/** Dados públicos de um certificado válido (página /validar). Sem ids internos nem e-mail. */
public record CertificateVerificationResponse(
        String verificationCode,
        Certificate.Kind kind,
        String holderName,
        String title,
        Integer workloadHours,
        Instant issuedAt
) {

    public static CertificateVerificationResponse from(Certificate certificate) {
        return new CertificateVerificationResponse(
                certificate.getVerificationCode(),
                certificate.getKind(),
                certificate.getHolderName(),
                certificate.getTitle(),
                certificate.getWorkloadHours(),
                certificate.getIssuedAt());
    }
}
