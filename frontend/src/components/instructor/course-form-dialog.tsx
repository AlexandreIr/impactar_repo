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
import { instructorService } from "@/services/instructor-service";
import type { CourseDetail } from "@/types/course";

const courseSchema = z.object({
  title: z.string().trim().min(1, "Informe o título do curso.").max(200, "Máximo de 200 caracteres."),
  workloadHours: z
    .number({ message: "Informe a carga horária." })
    .int("Use um número inteiro de horas.")
    .min(1, "A carga horária deve ser de pelo menos 1 hora.")
    .max(1000, "A carga horária deve ser de no máximo 1000 horas."),
  thumbnailUrl: z
    .string()
    .trim()
    .max(1000, "Link muito longo.")
    .refine((value) => value === "" || /^https?:\/\//i.test(value), "Use um link http(s)."),
  description: z.string().max(5000, "Máximo de 5000 caracteres."),
});

type CourseFormValues = z.infer<typeof courseSchema>;

interface CourseFormDialogProps {
  /** Ausente = criação. */
  course?: CourseDetail;
  onOpenChange: (open: boolean) => void;
  onSaved: (course: CourseDetail) => void;
}

export function CourseFormDialog({ course, onOpenChange, onSaved }: CourseFormDialogProps) {
  const isEdit = !!course;
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<CourseFormValues>({
    resolver: zodResolver(courseSchema),
    defaultValues: {
      title: course?.title ?? "",
      workloadHours: course?.workloadHours ?? undefined,
      thumbnailUrl: course?.thumbnailUrl ?? "",
      description: course?.description ?? "",
    },
  });

  async function onSubmit(values: CourseFormValues) {
    try {
      const saved = course
        ? await instructorService.updateCourse(course.id, values)
        : await instructorService.createCourse(values);
      toast.success(isEdit ? "Curso atualizado." : "Curso criado.");
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
          <DialogTitle>{isEdit ? "Editar curso" : "Novo curso"}</DialogTitle>
          <DialogDescription>
            {isEdit ? "Atualize os dados do curso." : "Depois de criar, adicione módulos e aulas."}
          </DialogDescription>
        </DialogHeader>

        <form id="course-form" onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
          <div className="space-y-2">
            <Label htmlFor="title">Título</Label>
            <Input id="title" aria-invalid={!!errors.title} {...register("title")} />
            <FieldError message={errors.title?.message} />
          </div>

          <div className="space-y-2">
            <Label htmlFor="workloadHours">Carga horária (horas)</Label>
            <Input
              id="workloadHours"
              type="number"
              min={1}
              max={1000}
              className="w-32"
              aria-invalid={!!errors.workloadHours}
              {...register("workloadHours", { valueAsNumber: true })}
            />
            <p className="text-xs text-muted-foreground">Aparece no certificado.</p>
            <FieldError message={errors.workloadHours?.message} />
          </div>

          <div className="space-y-2">
            <Label htmlFor="thumbnailUrl">Imagem de capa (link, opcional)</Label>
            <Input
              id="thumbnailUrl"
              placeholder="https://..."
              aria-invalid={!!errors.thumbnailUrl}
              {...register("thumbnailUrl")}
            />
            <FieldError message={errors.thumbnailUrl?.message} />
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">Descrição</Label>
            <Textarea id="description" rows={5} aria-invalid={!!errors.description} {...register("description")} />
            <FieldError message={errors.description?.message} />
          </div>
        </form>

        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={isSubmitting}>
            Cancelar
          </Button>
          <Button type="submit" form="course-form" disabled={isSubmitting}>
            {isSubmitting ? "Salvando..." : isEdit ? "Salvar" : "Criar curso"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
