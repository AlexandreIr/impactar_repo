package br.com.impactar.certificate.dto;

import java.time.Instant;
import java.util.UUID;

import br.com.impactar.certificate.Certificate;

public record CertificateResponse(
        UUID id,
        Certificate.Kind kind,
        String title,
        String holderName,
        Integer workloadHours,
        String verificationCode,
        Instant issuedAt,
        UUID courseId,
        UUID trailId
) {

    public static CertificateResponse from(Certificate certificate) {
        return new CertificateResponse(
                certificate.getId(),
                certificate.getKind(),
                certificate.getTitle(),
                certificate.getHolderName(),
                certificate.getWorkloadHours(),
                certificate.getVerificationCode(),
                certificate.getIssuedAt(),
                certificate.getCourseId(),
                certificate.getTrailId());
    }
}
