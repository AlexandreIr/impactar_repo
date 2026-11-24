"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { Award, Download, Mail, Printer } from "lucide-react";
import { toast } from "sonner";

import { AppHeader } from "@/components/app-header";
import { SendCertificateDialog } from "@/components/learning/send-certificate-dialog";
import { Badge } from "@/components/ui/badge";
import { Button, buttonVariants } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { getErrorMessage } from "@/lib/api";
import { certificateService, formatIssuedAt } from "@/services/certificate-service";
import type { Certificate } from "@/types/certificate";

export default function CertificatesPage() {
  const [certificates, setCertificates] = useState<Certificate[] | null>(null);
  const [sending, setSending] = useState<Certificate | null>(null);

  useEffect(() => {
    certificateService
      .listMine()
      .then(setCertificates)
      .catch((error) => {
        toast.error(getErrorMessage(error));
        setCertificates([]);
      });
  }, []);

  return (
    <>
      <AppHeader title="Certificados" />
      <main className="mx-auto max-w-6xl space-y-6 px-4 py-8">
        <div>
          <h2 className="text-xl font-semibold">Meus certificados</h2>
          <p className="text-sm text-muted-foreground">
            Emitidos automaticamente quando você conclui todas as aulas de um curso ou trilha.
          </p>
        </div>

        {!certificates && <p className="text-muted-foreground">Carregando...</p>}

        {certificates?.length === 0 && (
          <Card>
            <CardContent className="space-y-3 py-10 text-center">
              <Award className="mx-auto size-10 text-muted-foreground" />
              <p className="text-muted-foreground">Você ainda não tem certificados.</p>
              <Link href="/cursos" className={buttonVariants({ variant: "outline" })}>
                Ver cursos
              </Link>
            </CardContent>
          </Card>
        )}

        <div className="grid gap-4 md:grid-cols-2">
          {certificates?.map((certificate) => (
            <Card key={certificate.id}>
              <CardContent className="space-y-4">
                <div className="flex items-start gap-3">
                  <div className="rounded-full bg-primary/10 p-2.5">
                    <Award className="size-6 text-primary" />
                  </div>
                  <div className="min-w-0 flex-1 space-y-1">
                    <Badge variant="secondary">{certificate.kind === "TRAIL" ? "Trilha" : "Curso"}</Badge>
                    <h3 className="font-semibold leading-snug">{certificate.title}</h3>
                    <p className="text-sm text-muted-foreground">
                      Emitido em {formatIssuedAt(certificate.issuedAt)}
                      {certificate.workloadHours ? ` · ${certificate.workloadHours}h` : ""}
                    </p>
                    <p className="font-mono text-xs text-muted-foreground">Código: {certificate.verificationCode}</p>
                  </div>
                </div>

                <div className="flex flex-wrap gap-2">
                  <a
                    href={certificateService.pdfUrl(certificate.id)}
                    target="_blank"
                    rel="noopener noreferrer"
                    className={buttonVariants({ variant: "outline", size: "sm" })}
                  >
                    <Printer className="size-4" /> Visualizar / imprimir
                  </a>
                  <a
                    href={certificateService.pdfUrl(certificate.id, true)}
                    className={buttonVariants({ variant: "outline", size: "sm" })}
                  >
                    <Download className="size-4" /> Baixar PDF
                  </a>
                  <Button size="sm" variant="outline" onClick={() => setSending(certificate)}>
                    <Mail className="size-4" /> Enviar por e-mail
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </main>

      {sending && (
        <SendCertificateDialog certificate={sending} onOpenChange={(open) => !open && setSending(null)} />
      )}
    </>
  );
}
