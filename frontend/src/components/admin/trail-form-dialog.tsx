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
import { Textarea } from "@/components/ui/textarea";
import { getErrorMessage } from "@/lib/api";
import { trailAdminService } from "@/services/trail-admin-service";
import type { TrailAdminDetail } from "@/types/trail";

const trailSchema = z.object({
  title: z.string().trim().min(1, "Informe o título da trilha.").max(200, "Máximo de 200 caracteres."),
  thumbnailUrl: z
    .string()
    .trim()
    .max(1000, "Link muito longo.")
    .refine((value) => value === "" || /^https?:\/\//i.test(value), "Use um link http(s)."),
  description: z.string().max(5000, "Máximo de 5000 caracteres."),
});

type TrailFormValues = z.infer<typeof trailSchema>;

interface TrailFormDialogProps {
  /** Ausente = criação. */
  trail?: TrailAdminDetail;
  onOpenChange: (open: boolean) => void;
  onSaved: (trail: TrailAdminDetail) => void;
}

export function TrailFormDialog({ trail, onOpenChange, onSaved }: TrailFormDialogProps) {
  const isEdit = !!trail;
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<TrailFormValues>({
    resolver: zodResolver(trailSchema),
    defaultValues: {
      title: trail?.title ?? "",
      thumbnailUrl: trail?.thumbnailUrl ?? "",
      description: trail?.description ?? "",
    },
  });

  async function onSubmit(values: TrailFormValues) {
    try {
      const saved = trail
        ? await trailAdminService.update(trail.id, values)
        : await trailAdminService.create(values);
      toast.success(isEdit ? "Trilha atualizada." : "Trilha criada.");
      onSaved(saved);
      onOpenChange(false);
    } catch (error) {
      toast.error(getErrorMessage(error));
    }
  }

  return (
    <Dialog open onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>{isEdit ? "Editar trilha" : "Nova trilha"}</DialogTitle>
          <DialogDescription>
            {isEdit ? "Atualize os dados da trilha." : "Depois de criar, escolha os cursos e a ordem."}
          </DialogDescription>
        </DialogHeader>

        <form id="trail-form" onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
          <div className="space-y-2">
            <Label htmlFor="trail-title">Título</Label>
            <Input id="trail-title" aria-invalid={!!errors.title} {...register("title")} />
            <FieldError message={errors.title?.message} />
          </div>

          <div className="space-y-2">
            <Label htmlFor="trail-thumbnail">Imagem de capa (link, opcional)</Label>
            <Input
              id="trail-thumbnail"
              placeholder="https://..."
              aria-invalid={!!errors.thumbnailUrl}
              {...register("thumbnailUrl")}
            />
            <FieldError message={errors.thumbnailUrl?.message} />
          </div>

          <div className="space-y-2">
            <Label htmlFor="trail-description">Descrição</Label>
            <Textarea id="trail-description" rows={5} aria-invalid={!!errors.description} {...register("description")} />
            <FieldError message={errors.description?.message} />
          </div>
        </form>

        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={isSubmitting}>
            Cancelar
          </Button>
          <Button type="submit" form="trail-form" disabled={isSubmitting}>
            {isSubmitting ? "Salvando..." : isEdit ? "Salvar" : "Criar trilha"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
