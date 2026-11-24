"use client";

import { useCallback, useEffect, useState, type FormEvent } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { ArrowLeft, Plus } from "lucide-react";
import { toast } from "sonner";

import { AppHeader } from "@/components/app-header";
import { ConfirmDialog, type ConfirmDialogProps } from "@/components/confirm-dialog";
import { CourseFormDialog } from "@/components/instructor/course-form-dialog";
import { LessonFormDialog } from "@/components/instructor/lesson-form-dialog";
import { ModuleSection } from "@/components/instructor/module-section";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { getErrorMessage } from "@/lib/api";
import { instructorService, moveId } from "@/services/instructor-service";
import type { CourseDetail, Lesson } from "@/types/course";

type LessonDialogState = { moduleId: string; lesson?: Lesson } | null;
type ConfirmState = Omit<ConfirmDialogProps, "onOpenChange"> | null;

export default function CourseEditorPage() {
  const { courseId } = useParams<{ courseId: string }>();
  const router = useRouter();

  const [course, setCourse] = useState<CourseDetail | null>(null);
  const [reloadKey, setReloadKey] = useState(0);
  const [busy, setBusy] = useState(false);
  const [newModuleTitle, setNewModuleTitle] = useState("");
  const [editingCourse, setEditingCourse] = useState(false);
  const [lessonDialog, setLessonDialog] = useState<LessonDialogState>(null);
  const [confirm, setConfirm] = useState<ConfirmState>(null);

  useEffect(() => {
    let cancelled = false;
    instructorService
      .getCourse(courseId)
      .then((data) => {
        if (!cancelled) setCourse(data);
      })
      .catch((error) => {
        if (cancelled) return;
        toast.error(getErrorMessage(error));
        router.replace("/instrutor");
      });
    return () => {
      cancelled = true;
    };
  }, [courseId, reloadKey, router]);

  const reload = useCallback(() => setReloadKey((key) => key + 1), []);

  /** Executa uma alteração, mostra erro se falhar e recarrega o curso. */
  async function run(action: () => Promise<unknown>, success?: string): Promise<boolean> {
    setBusy(true);
    try {
      await action();
      if (success) toast.success(success);
      reload();
      return true;
    } catch (error) {
      toast.error(getErrorMessage(error));
      return false;
    } finally {
      setBusy(false);
    }
  }

  if (!course) {
    return (
      <>
        <AppHeader title="Área do Instrutor" />
        <main className="mx-auto max-w-4xl px-4 py-8 text-muted-foreground">Carregando curso...</main>
      </>
    );
  }

  const lessonCount = course.modules.reduce((total, courseModule) => total + courseModule.lessons.length, 0);

  async function handleAddModule(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!newModuleTitle.trim() || !course) return;
    const ok = await run(() => instructorService.addModule(course.id, newModuleTitle.trim()), "Módulo adicionado.");
    if (ok) setNewModuleTitle("");
  }

  function togglePublish() {
    if (!course) return;
    const publish = !course.published;
    setConfirm({
      title: publish ? "Publicar curso" : "Despublicar curso",
      description: publish
        ? "O curso ficará visível no catálogo para todos os alunos."
        : "O curso sai do catálogo. Alunos matriculados perdem o acesso até ele ser publicado de novo.",
      confirmLabel: publish ? "Publicar" : "Despublicar",
      onConfirm: async () => {
        const updated = await instructorService.setPublished(course.id, publish);
        setCourse(updated);
        toast.success(publish ? "Curso publicado." : "Curso despublicado.");
      },
    });
  }

  function confirmDeleteCourse() {
    if (!course) return;
    setConfirm({
      title: "Excluir curso",
      description: `"${course.title}" e todos os módulos, aulas e materiais serão excluídos. Não é possível desfazer.`,
      confirmLabel: "Excluir curso",
      destructive: true,
      onConfirm: async () => {
        await instructorService.deleteCourse(course.id);
        toast.success("Curso excluído.");
        router.replace("/instrutor");
      },
    });
  }

  return (
    <>
      <AppHeader title="Área do Instrutor" />
      <main className="mx-auto max-w-4xl space-y-6 px-4 py-8">
        <Link
          href="/instrutor"
          className="inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="size-4" /> Cursos
        </Link>

        <Card>
          <CardContent className="flex flex-col gap-4 sm:flex-row">
            {course.thumbnailUrl && (
              // eslint-disable-next-line @next/next/no-img-element
              <img
                src={course.thumbnailUrl}
                alt=""
                className="aspect-video w-full rounded-md object-cover sm:w-56"
              />
            )}
            <div className="flex-1 space-y-2">
              <div className="flex flex-wrap items-center gap-2">
                <h2 className="text-xl font-semibold">{course.title}</h2>
                <Badge variant={course.published ? "default" : "secondary"}>
                  {course.published ? "Publicado" : "Rascunho"}
                </Badge>
              </div>
              <p className="text-sm text-muted-foreground">
                {course.workloadHours ? `${course.workloadHours}h` : "Sem carga horária"} · {course.modules.length}{" "}
                módulo(s) · {lessonCount} aula(s) · {course.instructorName}
              </p>
              {course.description && (
                <p className="whitespace-pre-line text-sm">{course.description}</p>
              )}
              <div className="flex flex-wrap gap-2 pt-2">
                <Button variant="outline" size="sm" onClick={() => setEditingCourse(true)}>
                  Editar dados
                </Button>
                <Button size="sm" variant={course.published ? "outline" : "default"} onClick={togglePublish}>
                  {course.published ? "Despublicar" : "Publicar"}
                </Button>
                {!course.published && (
                  <Button size="sm" variant="ghost" className="text-destructive" onClick={confirmDeleteCourse}>
                    Excluir
                  </Button>
                )}
              </div>
            </div>
          </CardContent>
        </Card>

        <section className="space-y-4">
          <h2 className="text-lg font-semibold">Módulos e aulas</h2>

          {course.modules.map((courseModule, index) => (
            <ModuleSection
              key={courseModule.id}
              courseModule={courseModule}
              index={index}
              total={course.modules.length}
              busy={busy}
              onRename={(title) => run(() => instructorService.renameModule(courseModule.id, title), "Módulo renomeado.")}
              onMove={(direction) =>
                run(() => instructorService.reorderModules(course.id, moveId(course.modules, index, direction)))
              }
              onDelete={() =>
                setConfirm({
                  title: "Excluir módulo",
                  description: `"${courseModule.title}" e suas ${courseModule.lessons.length} aula(s) serão excluídos, junto com o progresso dos alunos nelas.`,
                  confirmLabel: "Excluir módulo",
                  destructive: true,
                  onConfirm: async () => {
                    await instructorService.deleteModule(courseModule.id);
                    toast.success("Módulo excluído.");
                    reload();
                  },
                })
              }
              onAddLesson={() => setLessonDialog({ moduleId: courseModule.id })}
              onEditLesson={(lesson) => setLessonDialog({ moduleId: courseModule.id, lesson })}
              onMoveLesson={(lessonIndex, direction) =>
                run(() =>
                  instructorService.reorderLessons(courseModule.id, moveId(courseModule.lessons, lessonIndex, direction)),
                )
              }
              onDeleteLesson={(lesson) =>
                setConfirm({
                  title: "Excluir aula",
                  description: `"${lesson.title}" e seus materiais serão excluídos, junto com o progresso dos alunos.`,
                  confirmLabel: "Excluir aula",
                  destructive: true,
                  onConfirm: async () => {
                    await instructorService.deleteLesson(lesson.id);
                    toast.success("Aula excluída.");
                    reload();
                  },
                })
              }
            />
          ))}

          <form onSubmit={handleAddModule} className="flex gap-2">
            <Input
              placeholder="Título do novo módulo"
              value={newModuleTitle}
              onChange={(event) => setNewModuleTitle(event.target.value)}
              className="max-w-md"
            />
            <Button type="submit" variant="outline" disabled={busy || !newModuleTitle.trim()}>
              <Plus className="size-4" /> Adicionar módulo
            </Button>
          </form>
        </section>
      </main>

      {editingCourse && (
        <CourseFormDialog
          course={course}
          onOpenChange={(open) => !open && setEditingCourse(false)}
          onSaved={(updated) => setCourse(updated)}
        />
      )}

      {lessonDialog && (
        <LessonFormDialog
          key={lessonDialog.lesson?.id ?? `new-${lessonDialog.moduleId}`}
          moduleId={lessonDialog.moduleId}
          lesson={lessonDialog.lesson}
          onOpenChange={(open) => {
            if (!open) {
              setLessonDialog(null);
              reload();
            }
          }}
          onSaved={(created) => {
            reload();
            // Criação: reabre em modo edição para cadastrar os materiais
            if (created) setLessonDialog({ moduleId: lessonDialog.moduleId, lesson: created });
          }}
          onChanged={reload}
        />
      )}

      {confirm && <ConfirmDialog {...confirm} onOpenChange={(open) => !open && setConfirm(null)} />}
    </>
  );
}
