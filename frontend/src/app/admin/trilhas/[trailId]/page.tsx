"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { ArrowDown, ArrowLeft, ArrowUp, Plus, X } from "lucide-react";
import { toast } from "sonner";

import { TrailFormDialog } from "@/components/admin/trail-form-dialog";
import { AppHeader } from "@/components/app-header";
import { ConfirmDialog, type ConfirmDialogProps } from "@/components/confirm-dialog";
import { Thumbnail } from "@/components/learning/thumbnail";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { getErrorMessage } from "@/lib/api";
import { instructorService } from "@/services/instructor-service";
import { trailAdminService } from "@/services/trail-admin-service";
import type { CourseSummary } from "@/types/course";
import type { TrailAdminDetail, TrailCourseItem } from "@/types/trail";

type ConfirmState = Omit<ConfirmDialogProps, "onOpenChange"> | null;

const selectClassName =
  "flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-xs outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50";

export default function AdminTrailEditorPage() {
  const { trailId } = useParams<{ trailId: string }>();
  const router = useRouter();

  const [trail, setTrail] = useState<TrailAdminDetail | null>(null);
  const [allCourses, setAllCourses] = useState<CourseSummary[]>([]);
  const [selected, setSelected] = useState<TrailCourseItem[]>([]);
  const [dirty, setDirty] = useState(false);
  const [courseToAdd, setCourseToAdd] = useState("");
  const [saving, setSaving] = useState(false);
  const [editing, setEditing] = useState(false);
  const [confirm, setConfirm] = useState<ConfirmState>(null);

  useEffect(() => {
    Promise.all([trailAdminService.get(trailId), instructorService.listCourses()])
      .then(([trailData, courses]) => {
        setTrail(trailData);
        setSelected(trailData.courses);
        setAllCourses(courses);
      })
      .catch((error) => {
        toast.error(getErrorMessage(error));
        router.replace("/admin/trilhas");
      });
  }, [trailId, router]);

  if (!trail) {
    return (
      <>
        <AppHeader title="Administração" />
        <main className="mx-auto max-w-4xl px-4 py-8 text-muted-foreground">Carregando trilha...</main>
      </>
    );
  }

  const available = allCourses.filter((course) => !selected.some((item) => item.courseId === course.id));

  function updateSelected(next: TrailCourseItem[]) {
    setSelected(next);
    setDirty(true);
  }

  function move(index: number, direction: -1 | 1) {
    const target = index + direction;
    if (target < 0 || target >= selected.length) return;
    const next = [...selected];
    [next[index], next[target]] = [next[target], next[index]];
    updateSelected(next);
  }

  function addCourse() {
    const course = allCourses.find((item) => item.id === courseToAdd);
    if (!course) return;
    updateSelected([
      ...selected,
      {
        courseId: course.id,
        title: course.title,
        published: course.published,
        workloadHours: course.workloadHours,
        instructorName: course.instructorName,
      },
    ]);
    setCourseToAdd("");
  }

  async function saveCourses() {
    if (!trail) return;
    setSaving(true);
    try {
      const updated = await trailAdminService.setCourses(
        trail.id,
        selected.map((item) => item.courseId),
      );
      setTrail(updated);
      setSelected(updated.courses);
      setDirty(false);
      toast.success("Cursos da trilha salvos.");
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setSaving(false);
    }
  }

  function togglePublish() {
    if (!trail) return;
    const publish = !trail.published;
    setConfirm({
      title: publish ? "Publicar trilha" : "Despublicar trilha",
      description: publish
        ? "A trilha ficará visível para todos os alunos. Só os cursos publicados aparecem nela."
        : "A trilha sai do catálogo. As matrículas nos cursos continuam valendo.",
      confirmLabel: publish ? "Publicar" : "Despublicar",
      onConfirm: async () => {
        const updated = await trailAdminService.setPublished(trail.id, publish);
        setTrail(updated);
        toast.success(publish ? "Trilha publicada." : "Trilha despublicada.");
      },
    });
  }

  function confirmDelete() {
    if (!trail) return;
    setConfirm({
      title: "Excluir trilha",
      description: `"${trail.title}" será excluída. Os cursos e as matrículas neles não são afetados.`,
      confirmLabel: "Excluir trilha",
      destructive: true,
      onConfirm: async () => {
        await trailAdminService.remove(trail.id);
        toast.success("Trilha excluída.");
        router.replace("/admin/trilhas");
      },
    });
  }

  const totalHours = selected.reduce((sum, item) => sum + (item.workloadHours ?? 0), 0);

  return (
    <>
      <AppHeader title="Administração" />
      <main className="mx-auto max-w-4xl space-y-6 px-4 py-8">
        <Link
          href="/admin/trilhas"
          className="inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="size-4" /> Trilhas
        </Link>

        <Card>
          <CardContent className="flex flex-col gap-4 sm:flex-row">
            {trail.thumbnailUrl && (
              <Thumbnail src={trail.thumbnailUrl} kind="trail" className="rounded-md sm:w-56" />
            )}
            <div className="flex-1 space-y-2">
              <div className="flex flex-wrap items-center gap-2">
                <h2 className="text-xl font-semibold">{trail.title}</h2>
                <Badge variant={trail.published ? "default" : "secondary"}>
                  {trail.published ? "Publicada" : "Rascunho"}
                </Badge>
              </div>
              {trail.description && <p className="whitespace-pre-line text-sm">{trail.description}</p>}
              <div className="flex flex-wrap gap-2 pt-2">
                <Button variant="outline" size="sm" onClick={() => setEditing(true)}>
                  Editar dados
                </Button>
                <Button
                  size="sm"
                  variant={trail.published ? "outline" : "default"}
                  onClick={togglePublish}
                  disabled={dirty}
                  title={dirty ? "Salve os cursos antes de publicar" : undefined}
                >
                  {trail.published ? "Despublicar" : "Publicar"}
                </Button>
                {!trail.published && (
                  <Button size="sm" variant="ghost" className="text-destructive" onClick={confirmDelete}>
                    Excluir
                  </Button>
                )}
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">
              Cursos da trilha
              <span className="ml-2 font-normal text-muted-foreground">
                {selected.length} curso(s){totalHours > 0 ? ` · ${totalHours}h` : ""}
              </span>
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {selected.length === 0 ? (
              <p className="text-sm text-muted-foreground">Nenhum curso nesta trilha.</p>
            ) : (
              <ul className="divide-y rounded-md border">
                {selected.map((item, index) => (
                  <li key={item.courseId} className="flex items-center justify-between gap-2 px-3 py-2">
                    <div className="min-w-0">
                      <p className="truncate text-sm font-medium">
                        {index + 1}. {item.title}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        {item.workloadHours ? `${item.workloadHours}h · ` : ""}
                        {item.instructorName}
                        {!item.published && " · rascunho (não aparece para o aluno)"}
                      </p>
                    </div>
                    <div className="flex items-center gap-1">
                      <Button
                        size="icon"
                        variant="ghost"
                        aria-label="Subir curso"
                        disabled={index === 0}
                        onClick={() => move(index, -1)}
                      >
                        <ArrowUp className="size-4" />
                      </Button>
                      <Button
                        size="icon"
                        variant="ghost"
                        aria-label="Descer curso"
                        disabled={index === selected.length - 1}
                        onClick={() => move(index, 1)}
                      >
                        <ArrowDown className="size-4" />
                      </Button>
                      <Button
                        size="icon"
                        variant="ghost"
                        aria-label={`Remover ${item.title}`}
                        onClick={() => updateSelected(selected.filter((other) => other.courseId !== item.courseId))}
                      >
                        <X className="size-4" />
                      </Button>
                    </div>
                  </li>
                ))}
              </ul>
            )}

            <div className="flex flex-col gap-2 sm:flex-row">
              <select
                aria-label="Curso para adicionar"
                className={selectClassName}
                value={courseToAdd}
                onChange={(event) => setCourseToAdd(event.target.value)}
              >
                <option value="">Selecione um curso para adicionar...</option>
                {available.map((course) => (
                  <option key={course.id} value={course.id}>
                    {course.title}
                    {course.published ? "" : " (rascunho)"}
                  </option>
                ))}
              </select>
              <Button variant="outline" onClick={addCourse} disabled={!courseToAdd}>
                <Plus className="size-4" /> Adicionar
              </Button>
            </div>

            <div className="flex items-center justify-end gap-3 border-t pt-4">
              {dirty && <span className="text-sm text-muted-foreground">Alterações não salvas</span>}
              <Button onClick={saveCourses} disabled={!dirty || saving}>
                {saving ? "Salvando..." : "Salvar cursos"}
              </Button>
            </div>
          </CardContent>
        </Card>
      </main>

      {editing && (
        <TrailFormDialog
          trail={trail}
          onOpenChange={(open) => !open && setEditing(false)}
          onSaved={(updated) => {
            setTrail(updated);
            setSelected(updated.courses);
            setDirty(false);
          }}
        />
      )}

      {confirm && <ConfirmDialog {...confirm} onOpenChange={(open) => !open && setConfirm(null)} />}
    </>
  );
}
