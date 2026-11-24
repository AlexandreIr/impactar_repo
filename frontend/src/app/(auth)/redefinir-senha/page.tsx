"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { ShieldX } from "lucide-react";
import { toast } from "sonner";
import { z } from "zod";

import { FieldError } from "@/components/field-error";
import { PasswordInput } from "@/components/password-input";
import { Button, buttonVariants } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { getErrorMessage } from "@/lib/api";
import { LOGIN_PATH } from "@/lib/auth";
import { newPasswordField } from "@/lib/password";
import { authService } from "@/services/auth-service";

const resetSchema = z
  .object({
    newPassword: newPasswordField,
    confirmPassword: z.string().min(1, "Confirme a nova senha."),
  })
  .refine((values) => values.newPassword === values.confirmPassword, {
    message: "As senhas não conferem.",
    path: ["confirmPassword"],
  });

type ResetValues = z.infer<typeof resetSchema>;
type LinkState = "checking" | "valid" | "invalid";

export default function ResetPasswordPage() {
  const router = useRouter();
  const [token, setToken] = useState<string | null>(null);
  const [linkState, setLinkState] = useState<LinkState>("checking");
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ResetValues>({ resolver: zodResolver(resetSchema) });

  // Lê o token da URL no cliente (evita exigir Suspense para useSearchParams)
  useEffect(() => {
    const value = new URLSearchParams(window.location.search).get("token");
    if (!value) {
      setLinkState("invalid");
      return;
    }
    setToken(value);
    authService
      .validateResetToken(value)
      .then(() => setLinkState("valid"))
      .catch(() => setLinkState("invalid"));
  }, []);

  async function onSubmit(values: ResetValues) {
    if (!token) return;
    try {
      await authService.resetPassword(token, values.newPassword);
      toast.success("Senha redefinida. Entre com a nova senha.");
      router.replace(LOGIN_PATH);
    } catch (error) {
      toast.error(getErrorMessage(error));
      setLinkState("invalid");
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-muted/40 px-4">
      <Card className="w-full max-w-sm">
        {linkState === "checking" && (
          <CardContent className="py-10 text-center text-sm text-muted-foreground">Verificando link...</CardContent>
        )}

        {linkState === "invalid" && (
          <>
            <CardHeader>
              <ShieldX className="size-8 text-destructive" />
              <CardTitle className="text-2xl">Link inválido ou expirado</CardTitle>
              <CardDescription>
                Os links de redefinição valem por 10 minutos e só podem ser usados uma vez. Peça um novo.
              </CardDescription>
            </CardHeader>
            <CardContent className="flex flex-col gap-2">
              <Link href="/esqueci-senha" className={buttonVariants()}>
                Pedir novo link
              </Link>
              <Link
                href={LOGIN_PATH}
                className="text-center text-sm text-muted-foreground hover:text-foreground hover:underline"
              >
                Voltar para o login
              </Link>
            </CardContent>
          </>
        )}

        {linkState === "valid" && (
          <>
            <CardHeader>
              <CardTitle className="text-2xl">Nova senha</CardTitle>
              <CardDescription>Crie uma nova senha para acessar a plataforma.</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
                <div className="space-y-2">
                  <Label htmlFor="newPassword">Nova senha</Label>
                  <PasswordInput
                    id="newPassword"
                    autoComplete="new-password"
                    autoFocus
                    aria-invalid={!!errors.newPassword}
                    {...register("newPassword")}
                  />
                  <FieldError message={errors.newPassword?.message} />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="confirmPassword">Confirme a nova senha</Label>
                  <PasswordInput
                    id="confirmPassword"
                    autoComplete="new-password"
                    aria-invalid={!!errors.confirmPassword}
                    {...register("confirmPassword")}
                  />
                  <FieldError message={errors.confirmPassword?.message} />
                </div>
                <Button type="submit" className="w-full" disabled={isSubmitting}>
                  {isSubmitting ? "Salvando..." : "Redefinir senha"}
                </Button>
              </form>
            </CardContent>
          </>
        )}
      </Card>
    </main>
  );
}
