"use client";

import { useState } from "react";
import Link from "next/link";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { toast } from "sonner";
import { z } from "zod";

import { FieldError } from "@/components/field-error";
import { PasswordInput } from "@/components/password-input";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { getErrorMessage } from "@/lib/api";
import { CHANGE_PASSWORD_PATH, homeFor, safeNextPath } from "@/lib/auth";
import { authService } from "@/services/auth-service";

const loginSchema = z.object({
  email: z.string().trim().min(1, "Informe o e-mail.").email("E-mail inválido."),
  password: z.string().min(1, "Informe a senha."),
});

type LoginValues = z.infer<typeof loginSchema>;

export default function LoginPage() {
  const [redirecting, setRedirecting] = useState(false);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginValues>({ resolver: zodResolver(loginSchema) });

  async function onSubmit(values: LoginValues) {
    try {
      const me = await authService.login(values.email, values.password);
      setRedirecting(true);

      if (me.mustChangePassword) {
        window.location.assign(CHANGE_PASSWORD_PATH);
        return;
      }
      const next = safeNextPath(new URLSearchParams(window.location.search).get("next"));
      window.location.assign(next ?? homeFor(me.role));
    } catch (error) {
      toast.error(getErrorMessage(error));
    }
  }

  const busy = isSubmitting || redirecting;

  return (
    <main className="flex min-h-screen items-center justify-center bg-muted/40 px-4">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <CardTitle className="text-2xl">impactar</CardTitle>
          <CardDescription>Entre com o e-mail e a senha recebidos da administração.</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
            <div className="space-y-2">
              <Label htmlFor="email">E-mail</Label>
              <Input
                id="email"
                type="email"
                autoComplete="email"
                aria-invalid={!!errors.email}
                {...register("email")}
              />
              <FieldError message={errors.email?.message} />
            </div>

            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <Label htmlFor="password">Senha</Label>

              </div>
              <PasswordInput
                id="password"
                autoComplete="current-password"
                aria-invalid={!!errors.password}
                {...register("password")}
              />
              <FieldError message={errors.password?.message} />
              <Link
                  href="/esqueci-senha"
                  className="text-sm text-muted-foreground underline-offset-4 hover:text-foreground hover:underline"
              >
                Esqueci minha senha
              </Link>
            </div>
            <Button type="submit" className="w-full" disabled={busy}>
              {busy ? "Entrando..." : "Entrar"}
            </Button>
          </form>
        </CardContent>
      </Card>
    </main>
  );
}
