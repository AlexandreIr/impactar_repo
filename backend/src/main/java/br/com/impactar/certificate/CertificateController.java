package br.com.impactar.certificate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import br.com.impactar.auth.CurrentUser;
import br.com.impactar.certificate.dto.CertificateResponse;
import br.com.impactar.certificate.dto.SendCertificateRequest;
import jakarta.validation.Valid;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Todos os perfis; cada usuário só acessa os próprios certificados.
 *
 * GET  /api/learning/certificates
 * GET  /api/learning/certificates/{id}/pdf?download=true|false
 * POST /api/learning/certificates/{id}/email   { "email": "opcional" }
 */
@RestController
@RequestMapping("/learning/certificates")
public class CertificateController {

    private final CertificateService service;

    public CertificateController(CertificateService service) {
        this.service = service;
    }

    @GetMapping
    public List<CertificateResponse> list(CurrentUser user) {
        return service.listMine(user);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable("id") UUID id,
                                      @RequestParam(name = "download", defaultValue = "false") boolean download,
                                      CurrentUser user) {
        Certificate certificate = service.getMine(id, user);
        ContentDisposition disposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(CertificatePdfRenderer.fileName(certificate), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .cacheControl(CacheControl.noStore())
                .body(service.renderPdf(certificate));
    }

    @PostMapping("/{id}/email")
    public Map<String, String> sendByEmail(@PathVariable("id") UUID id,
                                           @Valid @RequestBody SendCertificateRequest request,
                                           CurrentUser user) {
        return Map.of("sentTo", service.sendByEmail(id, request.email(), user));
    }
}
