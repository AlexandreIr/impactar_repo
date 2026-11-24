"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { toast } from "sonner";
import { z } from "zod";

import { FieldError } from "@/components/field-error";
import { LogoutButton } from "@/components/logout-button";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { getErrorMessage } from "@/lib/api";
import { homeFor } from "@/lib/auth";
import { authService } from "@/services/auth-service";

const changePasswordSchema = z
  .object({
    currentPassword: z.string().min(1, "Informe a senha atual."),
    newPassword: z
      .string()
      .min(8, "A nova senha deve ter pelo menos 8 caracteres.")
      .max(72, "A nova senha deve ter no máximo 72 caracteres."),
    confirmPassword: z.string().min(1, "Confirme a nova senha."),
  })
  .refine((values) => values.newPassword === values.confirmPassword, {
    message: "As senhas não conferem.",
    path: ["confirmPassword"],
  })
  .refine((values) => values.newPassword !== values.currentPassword, {
    message: "A nova senha deve ser diferente da atual.",
    path: ["newPassword"],
  });

type ChangePasswordValues = z.infer<typeof changePasswordSchema>;

export default function ChangePasswordPage() {
  const [redirecting, setRedirecting] = useState(false);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ChangePasswordValues>({ resolver: zodResolver(changePasswordSchema) });

  async function onSubmit(values: ChangePasswordValues) {
    try {
      const me = await authService.changePassword(values.currentPassword, values.newPassword);
      setRedirecting(true);
      toast.success("Senha alterada com sucesso.");
      window.location.assign(homeFor(me.role));
    } catch (error) {
      toast.error(getErrorMessage(error));
    }
  }

  const busy = isSubmitting || redirecting;

  return (
    <main className="flex min-h-screen items-center justify-center bg-muted/40 px-4">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <CardTitle className="text-2xl">Crie sua senha</CardTitle>
          <CardDescription>
            No primeiro acesso é preciso trocar a senha provisória enviada por e-mail.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
            <div className="space-y-2">
              <Label htmlFor="currentPassword">Senha atual (provisória)</Label>
              <Input
                id="currentPassword"
                type="password"
                autoComplete="current-password"
                aria-invalid={!!errors.currentPassword}
                {...register("currentPassword")}
              />
              <FieldError message={errors.currentPassword?.message} />
            </div>

            <div className="space-y-2">
              <Label htmlFor="newPassword">Nova senha</Label>
              <Input
                id="newPassword"
                type="password"
                autoComplete="new-password"
                aria-invalid={!!errors.newPassword}
                {...register("newPassword")}
              />
              <FieldError message={errors.newPassword?.message} />
            </div>

            <div className="space-y-2">
              <Label htmlFor="confirmPassword">Confirme a nova senha</Label>
              <Input
                id="confirmPassword"
                type="password"
                autoComplete="new-password"
                aria-invalid={!!errors.confirmPassword}
                {...register("confirmPassword")}
              />
              <FieldError message={errors.confirmPassword?.message} />
            </div>

            <Button type="submit" className="w-full" disabled={busy}>
              {busy ? "Salvando..." : "Salvar nova senha"}
            </Button>
          </form>

          <div className="mt-4 flex justify-center">
            <LogoutButton />
          </div>
        </CardContent>
      </Card>
    </main>
  );
}
