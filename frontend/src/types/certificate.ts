export type CertificateKind = "COURSE" | "TRAIL";

export interface Certificate {
  id: string;
  kind: CertificateKind;
  title: string;
  holderName: string;
  workloadHours: number | null;
  verificationCode: string;
  issuedAt: string;
  courseId: string | null;
  trailId: string | null;
}

export interface CertificateVerification {
  verificationCode: string;
  kind: CertificateKind;
  holderName: string;
  title: string;
  workloadHours: number | null;
  issuedAt: string;
}
