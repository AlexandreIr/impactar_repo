"use client";

import { useState } from "react";
import Link from "next/link";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { MailCheck } from "lucide-react";
import { toast } from "sonner";
import { z } from "zod";

import { FieldError } from "@/components/field-error";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { getErrorMessage } from "@/lib/api";
import { LOGIN_PATH } from "@/lib/auth";
import { authService } from "@/services/auth-service";

const forgotSchema = z.object({
  email: z.string().trim().min(1, "Informe o e-mail.").email("E-mail inválido."),
});

type ForgotValues = z.infer<typeof forgotSchema>;

export default function ForgotPasswordPage() {
  const [sentTo, setSentTo] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ForgotValues>({ resolver: zodResolver(forgotSchema) });

  async function onSubmit(values: ForgotValues) {
    try {
      await authService.forgotPassword(values.email);
      setSentTo(values.email);
    } catch (error) {
      toast.error(getErrorMessage(error));
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-muted/40 px-4">
      <Card className="w-full max-w-sm">
        {sentTo ? (
          <>
            <CardHeader>
              <MailCheck className="size-8 text-primary" />
              <CardTitle className="text-2xl">Verifique seu e-mail</CardTitle>
              <CardDescription>
                Se <strong>{sentTo}</strong> estiver cadastrado, você receberá um link para criar uma nova senha. O link
                vale por <strong>10 minutos</strong> e só pode ser usado uma vez.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-3 text-sm text-muted-foreground">
              <p>Não chegou? Confira a caixa de spam ou peça um novo link em 1 minuto.</p>
              <div className="flex flex-col gap-2">
                <Button variant="outline" onClick={() => setSentTo(null)}>
                  Enviar novamente
                </Button>
                <Link href={LOGIN_PATH} className="text-center hover:text-foreground hover:underline">
                  Voltar para o login
                </Link>
              </div>
            </CardContent>
          </>
        ) : (
          <>
            <CardHeader>
              <CardTitle className="text-2xl">Esqueci minha senha</CardTitle>
              <CardDescription>Informe o e-mail da sua conta para receber um link de redefinição.</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
                <div className="space-y-2">
                  <Label htmlFor="email">E-mail</Label>
                  <Input
                    id="email"
                    type="email"
                    autoComplete="email"
                    autoFocus
                    aria-invalid={!!errors.email}
                    {...register("email")}
                  />
                  <FieldError message={errors.email?.message} />
                </div>
                <Button type="submit" className="w-full" disabled={isSubmitting}>
                  {isSubmitting ? "Enviando..." : "Enviar link"}
                </Button>
                <Link
                  href={LOGIN_PATH}
                  className="block text-center text-sm text-muted-foreground hover:text-foreground hover:underline"
                >
                  Voltar para o login
                </Link>
              </form>
            </CardContent>
          </>
        )}
      </Card>
    </main>
  );
}
