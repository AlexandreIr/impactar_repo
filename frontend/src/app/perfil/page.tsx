"use client";

import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { LogOut } from "lucide-react";
import { toast } from "sonner";
import { z } from "zod";

import { AppHeader } from "@/components/app-header";
import { FieldError } from "@/components/field-error";
import { PasswordInput } from "@/components/password-input";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { getErrorMessage } from "@/lib/api";
import { LOGIN_PATH } from "@/lib/auth";
import { newPasswordField } from "@/lib/password";
import { ROLE_LABELS } from "@/lib/roles";
import { authService } from "@/services/auth-service";
import type { Me } from "@/types/auth";

const profileSchema = z.object({
  name: z
    .string()
    .trim()
    .min(3, "Informe o nome completo.")
    .max(150, "O nome deve ter no máximo 150 caracteres."),
});

const passwordSchema = z
  .object({
    currentPassword: z.string().min(1, "Informe a senha atual."),
    newPassword: newPasswordField,
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

type ProfileValues = z.infer<typeof profileSchema>;
type PasswordValues = z.infer<typeof passwordSchema>;

function ProfileForm({ me, onSaved }: { me: Me; onSaved: (me: Me) => void }) {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting, isDirty },
  } = useForm<ProfileValues>({ resolver: zodResolver(profileSchema), defaultValues: { name: me.name } });

  async function onSubmit(values: ProfileValues) {
    try {
      onSaved(await authService.updateProfile(values.name));
      toast.success("Dados atualizados.");
    } catch (error) {
      toast.error(getErrorMessage(error));
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
      <div className="space-y-2">
        <Label htmlFor="name">Nome completo</Label>
        <Input id="name" autoComplete="name" aria-invalid={!!errors.name} {...register("name")} />
        <p className="text-xs text-muted-foreground">É o nome impresso nos próximos certificados.</p>
        <FieldError message={errors.name?.message} />
      </div>
      <div className="space-y-2">
        <Label htmlFor="email">E-mail (login)</Label>
        <Input id="email" value={me.email} readOnly disabled className="bg-muted" />
        <p className="text-xs text-muted-foreground">Para alterar o e-mail, fale com a administração.</p>
      </div>
      <div className="flex justify-end">
        <Button type="submit" disabled={isSubmitting || !isDirty}>
          {isSubmitting ? "Salvando..." : "Salvar"}
        </Button>
      </div>
    </form>
  );
}

function PasswordForm() {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<PasswordValues>({ resolver: zodResolver(passwordSchema) });

  async function onSubmit(values: PasswordValues) {
    try {
      await authService.changePassword(values.currentPassword, values.newPassword);
      reset({ currentPassword: "", newPassword: "", confirmPassword: "" });
      toast.success("Senha alterada com sucesso.");
    } catch (error) {
      toast.error(getErrorMessage(error));
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
      <div className="space-y-2">
        <Label htmlFor="currentPassword">Senha atual</Label>
        <PasswordInput
          id="currentPassword"
          autoComplete="current-password"
          aria-invalid={!!errors.currentPassword}
          {...register("currentPassword")}
        />
        <FieldError message={errors.currentPassword?.message} />
      </div>
      <div className="grid gap-4 sm:grid-cols-2">
        <div className="space-y-2">
          <Label htmlFor="newPassword">Nova senha</Label>
          <PasswordInput
            id="newPassword"
            autoComplete="new-password"
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
      </div>
      <div className="flex justify-end">
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Salvando..." : "Alterar senha"}
        </Button>
      </div>
    </form>
  );
}

export default function ProfilePage() {
  const [me, setMe] = useState<Me | null>(null);
  const [headerKey, setHeaderKey] = useState(0);
  const [loggingOut, setLoggingOut] = useState(false);

  useEffect(() => {
    authService
      .me()
      .then(setMe)
      .catch((error) => toast.error(getErrorMessage(error)));
  }, []);

  async function handleLogout() {
    setLoggingOut(true);
    try {
      await authService.logout();
    } finally {
      window.location.assign(LOGIN_PATH);
    }
  }

  return (
    <>
      {/* key: recria o cabeçalho para exibir o nome atualizado */}
      <AppHeader key={headerKey} title="Meu perfil" />
      <main className="mx-auto max-w-3xl space-y-6 px-4 py-8">
        {!me && <p className="text-muted-foreground">Carregando...</p>}

        {me && (
          <>
            <Card>
              <CardHeader>
                <div className="flex flex-wrap items-center gap-2">
                  <CardTitle className="text-base">Dados pessoais</CardTitle>
                  <Badge variant="secondary">{ROLE_LABELS[me.role]}</Badge>
                </div>
              </CardHeader>
              <CardContent>
                <ProfileForm
                  key={me.name}
                  me={me}
                  onSaved={(updated) => {
                    setMe(updated);
                    setHeaderKey((key) => key + 1);
                  }}
                />
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-base">Alterar senha</CardTitle>
                <CardDescription>Use pelo menos 8 caracteres.</CardDescription>
              </CardHeader>
              <CardContent>
                <PasswordForm />
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-base">Sair da conta</CardTitle>
                <CardDescription>Encerra a sessão neste navegador.</CardDescription>
              </CardHeader>
              <CardContent>
                <Button variant="outline" onClick={handleLogout} disabled={loggingOut}>
                  <LogOut className="size-4" /> {loggingOut ? "Saindo..." : "Sair"}
                </Button>
              </CardContent>
            </Card>
          </>
        )}
      </main>
    </>
  );
}
