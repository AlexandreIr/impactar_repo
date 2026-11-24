"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { toast } from "sonner";
import { z } from "zod";

import { FieldError } from "@/components/field-error";
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
import { ROLE_LABELS, ROLES } from "@/lib/roles";
import { userService } from "@/services/user-service";
import type { UserSummary } from "@/types/user";

const userSchema = z.object({
  name: z
    .string()
    .trim()
    .min(3, "Informe o nome completo.")
    .max(150, "O nome deve ter no máximo 150 caracteres."),
  email: z.string().trim().min(1, "Informe o e-mail.").email("E-mail inválido.").max(255, "E-mail muito longo."),
  role: z.enum(["ADMIN", "INSTRUCTOR", "STUDENT"]),
  active: z.boolean(),
});

type UserFormValues = z.infer<typeof userSchema>;

interface UserFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** Ausente = criação. */
  user?: UserSummary;
  onSaved: () => void;
}

const selectClassName =
  "flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-xs outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50";

export function UserFormDialog({ open, onOpenChange, user, onSaved }: UserFormDialogProps) {
  const isEdit = !!user;
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<UserFormValues>({
    resolver: zodResolver(userSchema),
    defaultValues: {
      name: user?.name ?? "",
      email: user?.email ?? "",
      role: user?.role ?? "STUDENT",
      active: user?.active ?? true,
    },
  });

  async function onSubmit(values: UserFormValues) {
    try {
      if (user) {
        await userService.update(user.id, { name: values.name, role: values.role, active: values.active });
        toast.success("Usuário atualizado.");
      } else {
        await userService.create({ name: values.name, email: values.email, role: values.role });
        toast.success(`Usuário criado. Acesso enviado para ${values.email}.`);
      }
      onSaved();
      onOpenChange(false);
    } catch (error) {
      toast.error(getErrorMessage(error));
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{isEdit ? "Editar usuário" : "Novo usuário"}</DialogTitle>
          <DialogDescription>
            {isEdit
              ? "O e-mail é o login e não pode ser alterado."
              : "Uma senha provisória será gerada e enviada para o e-mail informado."}
          </DialogDescription>
        </DialogHeader>

        <form id="user-form" onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
          <div className="space-y-2">
            <Label htmlFor="name">Nome completo</Label>
            <Input id="name" autoComplete="off" aria-invalid={!!errors.name} {...register("name")} />
            <FieldError message={errors.name?.message} />
          </div>

          <div className="space-y-2">
            <Label htmlFor="email">E-mail</Label>
            <Input
              id="email"
              type="email"
              autoComplete="off"
              readOnly={isEdit}
              className={isEdit ? "bg-muted" : undefined}
              aria-invalid={!!errors.email}
              {...register("email")}
            />
            <FieldError message={errors.email?.message} />
          </div>

          <div className="space-y-2">
            <Label htmlFor="role">Perfil</Label>
            <select id="role" className={selectClassName} {...register("role")}>
              {ROLES.map((role) => (
                <option key={role} value={role}>
                  {ROLE_LABELS[role]}
                </option>
              ))}
            </select>
          </div>

          {isEdit && (
            <label className="flex items-center gap-2 text-sm">
              <input type="checkbox" className="size-4 accent-primary" {...register("active")} />
              Usuário ativo
            </label>
          )}
        </form>

        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={isSubmitting}>
            Cancelar
          </Button>
          <Button type="submit" form="user-form" disabled={isSubmitting}>
            {isSubmitting ? "Salvando..." : isEdit ? "Salvar" : "Criar e enviar acesso"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
