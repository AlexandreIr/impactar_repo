package br.com.impactar.certificate;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


import org.openpdf.text.*;
import org.openpdf.text.pdf.ColumnText;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Gera o PDF do certificado (A4 paisagem) com OpenPDF.
 * Usa as fontes padrão do PDF (Helvetica): não depende de fontes do sistema, funciona no container Alpine.
 * Gerado sob demanda a partir do snapshot — nenhum arquivo é armazenado.
 */
@Component
public class CertificatePdfRenderer {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR"));

    private static final Color INK = new Color(17, 24, 39);
    private static final Color MUTED = new Color(75, 85, 99);
    private static final Color ACCENT = new Color(30, 64, 175);

    private final String validationBaseUrl;

    public CertificatePdfRenderer(@Value("${app.frontend-url}") String frontendUrl) {
        this.validationBaseUrl = frontendUrl.replaceAll("/+$", "") + "/validar/";
    }

    public byte[] render(Certificate certificate) {
        Document document = new Document(PageSize.A4.rotate(), 72, 72, 64, 72);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.addTitle("Certificado - " + certificate.getTitle());
            document.addAuthor("impactar");
            document.addCreator("impactar");
            document.open();

            drawFrame(writer.getDirectContentUnder(), document.getPageSize());

            document.add(line("impactar", font(14, Font.BOLD, ACCENT), 18, 0));
            document.add(line("Plataforma de formação de professores", font(10, Font.NORMAL, MUTED), 14, 0));

            document.add(line("CERTIFICADO", font(32, Font.BOLD, INK), 38, 24));
            document.add(line("DE CONCLUSÃO", font(11, Font.BOLD, MUTED), 14, 0));

            document.add(line("Certificamos que", font(13, Font.NORMAL, MUTED), 18, 26));
            document.add(line(certificate.getHolderName(), font(26, Font.BOLD, INK), 32, 6));

            String completed = certificate.getKind() == Certificate.Kind.TRAIL
                    ? "concluiu com aproveitamento a trilha de formação"
                    : "concluiu com aproveitamento o curso";
            document.add(line(completed, font(13, Font.NORMAL, MUTED), 18, 12));
            document.add(line(certificate.getTitle(), font(18, Font.BOLD, ACCENT), 23, 4));

            if (certificate.getWorkloadHours() != null) {
                String hours = certificate.getWorkloadHours() == 1 ? "1 hora" : certificate.getWorkloadHours() + " horas";
                document.add(line("com carga horária total de " + hours + ".", font(13, Font.NORMAL, MUTED), 18, 12));
            }

            String issuedAt = DATE.format(certificate.getIssuedAt().atZone(ZONE));
            document.add(line("Emitido em " + issuedAt + ".", font(12, Font.NORMAL, INK), 16, 24));

            drawFooter(writer.getDirectContent(), document.getPageSize(), certificate.getVerificationCode());
        } catch (DocumentException ex) {
            throw new IllegalStateException("Falha ao gerar o PDF do certificado.", ex);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        return out.toByteArray();
    }

    /** Nome de arquivo sem acentos/espaços: certificado-nome-do-curso.pdf */
    public static String fileName(Certificate certificate) {
        String slug = java.text.Normalizer.normalize(certificate.getTitle(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (slug.length() > 60) {
            slug = slug.substring(0, 60).replaceAll("-$", "");
        }
        return "certificado-" + (slug.isEmpty() ? "impactar" : slug) + ".pdf";
    }

    private static Paragraph line(String text, Font font, float leading, float spacingBefore) {
        Paragraph paragraph = new Paragraph(leading, text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingBefore(spacingBefore);
        return paragraph;
    }

    private static Font font(float size, int style, Color color) {
        return new Font(Font.HELVETICA, size, style, color);
    }

    private static void drawFrame(PdfContentByte canvas, Rectangle page) {
        canvas.saveState();
        canvas.setColorStroke(ACCENT);
        canvas.setLineWidth(4f);
        canvas.rectangle(24, 24, page.getWidth() - 48, page.getHeight() - 48);
        canvas.stroke();
        canvas.setLineWidth(0.75f);
        canvas.rectangle(34, 34, page.getWidth() - 68, page.getHeight() - 68);
        canvas.stroke();
        canvas.restoreState();
    }

    private void drawFooter(PdfContentByte canvas, Rectangle page, String code) {
        Font footer = font(9, Font.NORMAL, MUTED);
        float centerX = page.getWidth() / 2;
        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase("Código de verificação: " + code, font(10, Font.BOLD, INK)), centerX, 62, 0);
        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase("Confira a autenticidade em " + validationBaseUrl + code, footer), centerX, 48, 0);
    }
}
