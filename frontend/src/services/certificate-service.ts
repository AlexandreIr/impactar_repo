import { api } from "@/lib/api";
import type { Certificate, CertificateVerification } from "@/types/certificate";

export const certificateService = {
  async listMine(): Promise<Certificate[]> {
    const { data } = await api.get<Certificate[]>("/learning/certificates");
    return data;
  },

  /** Link direto (mesma origem, cookie de sessão): abre no navegador ou baixa. */
  pdfUrl(id: string, download = false): string {
    return `/api/learning/certificates/${id}/pdf${download ? "?download=true" : ""}`;
  },

  /** E-mail vazio = envia para o e-mail do próprio usuário. Retorna o destino. */
  async sendByEmail(id: string, email: string): Promise<string> {
    const { data } = await api.post<{ sentTo: string }>(`/learning/certificates/${id}/email`, {
      email: email.trim() || null,
    });
    return data.sentTo;
  },

  /** Público: não exige login. */
  async verify(code: string): Promise<CertificateVerification> {
    const { data } = await api.get<CertificateVerification>(`/public/certificates/${encodeURIComponent(code)}`);
    return data;
  },
};

export function formatIssuedAt(iso: string): string {
  return new Date(iso).toLocaleDateString("pt-BR", {
    day: "numeric",
    month: "long",
    year: "numeric",
    timeZone: "America/Sao_Paulo",
  });
}
