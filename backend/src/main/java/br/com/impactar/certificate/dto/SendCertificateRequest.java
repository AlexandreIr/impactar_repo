package br.com.impactar.certificate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** E-mail de destino opcional; em branco = e-mail do próprio usuário. */
public record SendCertificateRequest(
        @Email(message = "E-mail inválido.")
        @Size(max = 255, message = "E-mail muito longo.")
        String email
) {
}
