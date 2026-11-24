"use client";

import { useState, type FormEvent } from "react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { getErrorMessage } from "@/lib/api";
import { certificateService } from "@/services/certificate-service";
import type { Certificate } from "@/types/certificate";

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

interface SendCertificateDialogProps {
  certificate: Certificate;
  onOpenChange: (open: boolean) => void;
}

export function SendCertificateDialog({ certificate, onOpenChange }: SendCertificateDialogProps) {
  const [email, setEmail] = useState("");
  const [pending, setPending] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (email.trim() && !EMAIL_PATTERN.test(email.trim())) {
      toast.error("E-mail inválido.");
      return;
    }
    setPending(true);
    try {
      const sentTo = await certificateService.sendByEmail(certificate.id, email);
      toast.success(`Certificado enviado para ${sentTo}.`);
      onOpenChange(false);
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setPending(false);
    }
  }

  return (
    <Dialog open onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Enviar por e-mail</DialogTitle>
          <DialogDescription>
            O certificado de <strong>{certificate.title}</strong> será enviado em PDF, como anexo.
          </DialogDescription>
        </DialogHeader>

        <form id="send-certificate-form" onSubmit={handleSubmit} className="space-y-2" noValidate>
          <Label htmlFor="certificate-email">E-mail de destino</Label>
          <Input
            id="certificate-email"
            type="email"
            placeholder="Em branco = seu e-mail cadastrado"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            autoFocus
          />
          <p className="text-xs text-muted-foreground">
            Você pode enviar para outra pessoa, como a coordenação da sua escola.
          </p>
        </form>

        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={pending}>
            Cancelar
          </Button>
          <Button type="submit" form="send-certificate-form" disabled={pending}>
            {pending ? "Enviando..." : "Enviar"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
