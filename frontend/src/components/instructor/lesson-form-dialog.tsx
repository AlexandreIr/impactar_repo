"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { toast } from "sonner";
import { z } from "zod";

import { FieldError } from "@/components/field-error";
import { MaterialsEditor } from "@/components/instructor/materials-editor";
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
import { extractYouTubeId, youTubeEmbedUrl } from "@/lib/youtube";
import { instructorService } from "@/services/instructor-service";
import type { Lesson } from "@/types/course";

const lessonSchema = z.object({
  title: z.string().trim().min(1, "Informe o título da aula.").max(200, "Máximo de 200 caracteres."),
  videoUrl: z
    .string()
    .trim()
    .max(1000, "Link muito longo.")
    .refine((value) => value === "" || extractYouTubeId(value) !== null, "Link do YouTube inválido."),
  description: z.string().max(10000, "Máximo de 10000 caracteres."),
});

type LessonFormValues = z.infer<typeof lessonSchema>;

interface LessonFormDialogProps {
  moduleId: string;
  /** Ausente = criação. */
  lesson?: Lesson;
  onOpenChange: (open: boolean) => void;
  /** Chamado após salvar; na criação recebe a aula nova (o diálogo reabre em modo edição para materiais). */
  onSaved: (lesson?: Lesson) => void;
  /** Recarrega o curso sem fechar o diálogo (materiais). */
  onChanged: () => void;
}

export function LessonFormDialog({ moduleId, lesson, onOpenChange, onSaved, onChanged }: LessonFormDialogProps) {
  const isEdit = !!lesson;
  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<LessonFormValues>({
    resolver: zodResolver(lessonSchema),
    defaultValues: {
      title: lesson?.title ?? "",
      videoUrl: lesson?.videoUrl ?? "",
      description: lesson?.description ?? "",
    },
  });

  const previewId = extractYouTubeId(watch("videoUrl"));

  async function onSubmit(values: LessonFormValues) {
    try {
      if (lesson) {
        await instructorService.updateLesson(lesson.id, values);
        toast.success("Aula atualizada.");
        onSaved();
        onOpenChange(false);
      } else {
        const created = await instructorService.addLesson(moduleId, values);
        toast.success("Aula criada. Adicione materiais complementares, se houver.");
        onSaved(created);
      }
    } catch (error) {
      toast.error(getErrorMessage(error));
    }
  }

  return (
    <Dialog open onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>{isEdit ? "Editar aula" : "Nova aula"}</DialogTitle>
          <DialogDescription>
            Use vídeos do YouTube como &quot;Não listado&quot;. O vídeo é opcional; o curso precisa de ao menos uma aula
            com vídeo para ser publicado.
          </DialogDescription>
        </DialogHeader>

        <form id="lesson-form" onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
          <div className="space-y-2">
            <Label htmlFor="lesson-title">Título</Label>
            <Input id="lesson-title" aria-invalid={!!errors.title} {...register("title")} />
            <FieldError message={errors.title?.message} />
          </div>

          <div className="space-y-2">
            <Label htmlFor="videoUrl">Link do vídeo no YouTube</Label>
            <Input
              id="videoUrl"
              placeholder="https://www.youtube.com/watch?v=..."
              aria-invalid={!!errors.videoUrl}
              {...register("videoUrl")}
            />
            <FieldError message={errors.videoUrl?.message} />
            {previewId && (
              <iframe
                key={previewId}
                src={youTubeEmbedUrl(previewId)}
                title="Pré-visualização do vídeo"
                className="aspect-video w-full rounded-md border"
                allow="accelerometer; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowFullScreen
              />
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="lesson-description">Descrição e texto complementar</Label>
            <Textarea
              id="lesson-description"
              rows={6}
              placeholder="Resumo da aula, orientações e links de apoio (https://...)"
              aria-invalid={!!errors.description}
              {...register("description")}
            />
            <p className="text-xs text-muted-foreground">Links que começam com https:// ficam clicáveis para o aluno.</p>
            <FieldError message={errors.description?.message} />
          </div>
        </form>

        {lesson ? (
          <div className="border-t pt-4">
            <MaterialsEditor lessonId={lesson.id} initialMaterials={lesson.materials} onChanged={onChanged} />
          </div>
        ) : (
          <p className="text-xs text-muted-foreground">Salve a aula para adicionar materiais complementares.</p>
        )}

        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={isSubmitting}>
            {isEdit ? "Fechar" : "Cancelar"}
          </Button>
          <Button type="submit" form="lesson-form" disabled={isSubmitting}>
            {isSubmitting ? "Salvando..." : isEdit ? "Salvar aula" : "Criar aula"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
