"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { ShieldCheck, ShieldX } from "lucide-react";

import { PublicHeader } from "@/components/public-header";
import { buttonVariants } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { getErrorStatus } from "@/lib/api";
import { certificateService, formatIssuedAt } from "@/services/certificate-service";
import type { CertificateVerification } from "@/types/certificate";

type State =
  | { status: "loading" }
  | { status: "valid"; certificate: CertificateVerification }
  | { status: "not-found" }
  | { status: "error" };

export default function ValidateCertificatePage() {
  const { code } = useParams<{ code: string }>();
  const [state, setState] = useState<State>({ status: "loading" });

  useEffect(() => {
    certificateService
      .verify(decodeURIComponent(code))
      .then((certificate) => setState({ status: "valid", certificate }))
      .catch((error) => setState({ status: getErrorStatus(error) === 404 ? "not-found" : "error" }));
  }, [code]);

  return (
    <>
      <PublicHeader />
      <main className="mx-auto max-w-xl space-y-4 px-4 py-12">
        {state.status === "loading" && <p className="text-center text-muted-foreground">Validando...</p>}

        {state.status === "valid" && (
          <Card>
            <CardContent className="space-y-5 py-6 text-center">
              <ShieldCheck className="mx-auto size-12 text-primary" />
              <div>
                <h1 className="text-xl font-semibold">Certificado válido</h1>
                <p className="text-sm text-muted-foreground">Emitido pela plataforma impactar.</p>
              </div>
              <dl className="space-y-3 text-left text-sm">
                <div>
                  <dt className="text-muted-foreground">Titular</dt>
                  <dd className="font-medium">{state.certificate.holderName}</dd>
                </div>
                <div>
                  <dt className="text-muted-foreground">{state.certificate.kind === "TRAIL" ? "Trilha" : "Curso"}</dt>
                  <dd className="font-medium">{state.certificate.title}</dd>
                </div>
                {state.certificate.workloadHours && (
                  <div>
                    <dt className="text-muted-foreground">Carga horária</dt>
                    <dd className="font-medium">{state.certificate.workloadHours} horas</dd>
                  </div>
                )}
                <div>
                  <dt className="text-muted-foreground">Data de emissão</dt>
                  <dd className="font-medium">{formatIssuedAt(state.certificate.issuedAt)}</dd>
                </div>
                <div>
                  <dt className="text-muted-foreground">Código</dt>
                  <dd className="font-mono">{state.certificate.verificationCode}</dd>
                </div>
              </dl>
            </CardContent>
          </Card>
        )}

        {(state.status === "not-found" || state.status === "error") && (
          <Card>
            <CardContent className="space-y-3 py-8 text-center">
              <ShieldX className="mx-auto size-12 text-destructive" />
              <h1 className="text-xl font-semibold">
                {state.status === "not-found" ? "Certificado não encontrado" : "Não foi possível validar agora"}
              </h1>
              <p className="text-sm text-muted-foreground">
                {state.status === "not-found"
                  ? "Confira se o código foi digitado exatamente como aparece no certificado."
                  : "Tente novamente em alguns instantes."}
              </p>
            </CardContent>
          </Card>
        )}

        {state.status !== "loading" && (
          <div className="text-center">
            <Link href="/validar" className={buttonVariants({ variant: "outline" })}>
              Validar outro código
            </Link>
          </div>
        )}
      </main>
    </>
  );
}
