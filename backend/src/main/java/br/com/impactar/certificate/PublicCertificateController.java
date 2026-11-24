package br.com.impactar.certificate;

import br.com.impactar.certificate.dto.CertificateVerificationResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Público (sem login): GET /api/public/certificates/{codigo}
 * Usado pela página /validar para conferir a autenticidade de um certificado.
 */
@RestController
@RequestMapping("/public/certificates")
public class PublicCertificateController {

    private final CertificateService service;

    public PublicCertificateController(CertificateService service) {
        this.service = service;
    }

    @GetMapping("/{code}")
    public CertificateVerificationResponse verify(@PathVariable("code") String code) {
        return service.verify(code);
    }
}
