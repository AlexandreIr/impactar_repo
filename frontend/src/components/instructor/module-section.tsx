"use client";

import { useState, type FormEvent } from "react";
import { ArrowDown, ArrowUp, Paperclip, Pencil, Plus, Trash2, Video } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import type { CourseModule, Lesson } from "@/types/course";

interface ModuleSectionProps {
  courseModule: CourseModule;
  index: number;
  total: number;
  busy: boolean;
  onRename: (title: string) => Promise<boolean>;
  onMove: (direction: -1 | 1) => void;
  onDelete: () => void;
  onAddLesson: () => void;
  onEditLesson: (lesson: Lesson) => void;
  onMoveLesson: (lessonIndex: number, direction: -1 | 1) => void;
  onDeleteLesson: (lesson: Lesson) => void;
}

export function ModuleSection({
  courseModule,
  index,
  total,
  busy,
  onRename,
  onMove,
  onDelete,
  onAddLesson,
  onEditLesson,
  onMoveLesson,
  onDeleteLesson,
}: ModuleSectionProps) {
  const [editing, setEditing] = useState(false);
  const [title, setTitle] = useState(courseModule.title);

  async function handleRename(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!title.trim()) return;
    if (await onRename(title.trim())) setEditing(false);
  }

  return (
    <Card>
      <CardHeader className="flex flex-row flex-wrap items-center justify-between gap-2 space-y-0">
        {editing ? (
          <form onSubmit={handleRename} className="flex flex-1 items-center gap-2">
            <Input value={title} onChange={(event) => setTitle(event.target.value)} autoFocus className="max-w-md" />
            <Button type="submit" size="sm" disabled={busy}>
              Salvar
            </Button>
            <Button
              type="button"
              size="sm"
              variant="ghost"
              onClick={() => {
                setTitle(courseModule.title);
                setEditing(false);
              }}
            >
              Cancelar
            </Button>
          </form>
        ) : (
          <h3 className="font-semibold">
            <span className="mr-2 text-muted-foreground">Módulo {index + 1}</span>
            {courseModule.title}
          </h3>
        )}

        {!editing && (
          <div className="flex items-center gap-1">
            <Button size="icon" variant="ghost" aria-label="Subir módulo" disabled={busy || index === 0} onClick={() => onMove(-1)}>
              <ArrowUp className="size-4" />
            </Button>
            <Button
              size="icon"
              variant="ghost"
              aria-label="Descer módulo"
              disabled={busy || index === total - 1}
              onClick={() => onMove(1)}
            >
              <ArrowDown className="size-4" />
            </Button>
            <Button size="icon" variant="ghost" aria-label="Renomear módulo" onClick={() => setEditing(true)}>
              <Pencil className="size-4" />
            </Button>
            <Button size="icon" variant="ghost" aria-label="Excluir módulo" disabled={busy} onClick={onDelete}>
              <Trash2 className="size-4" />
            </Button>
          </div>
        )}
      </CardHeader>

      <CardContent className="space-y-3">
        {courseModule.lessons.length === 0 ? (
          <p className="text-sm text-muted-foreground">Nenhuma aula neste módulo.</p>
        ) : (
          <ul className="divide-y rounded-md border">
            {courseModule.lessons.map((lesson, lessonIndex) => (
              <li key={lesson.id} className="flex flex-wrap items-center justify-between gap-2 px-3 py-2">
                <div className="min-w-0">
                  <p className="truncate text-sm font-medium">
                    {lessonIndex + 1}. {lesson.title}
                  </p>
                  <div className="mt-1 flex flex-wrap gap-1.5">
                    {lesson.videoId ? (
                      <Badge variant="secondary" className="gap-1">
                        <Video className="size-3" /> Vídeo
                      </Badge>
                    ) : (
                      <Badge variant="outline">Sem vídeo</Badge>
                    )}
                    {lesson.materials.length > 0 && (
                      <Badge variant="outline" className="gap-1">
                        <Paperclip className="size-3" /> {lesson.materials.length} material(is)
                      </Badge>
                    )}
                  </div>
                </div>
                <div className="flex items-center gap-1">
                  <Button
                    size="icon"
                    variant="ghost"
                    aria-label="Subir aula"
                    disabled={busy || lessonIndex === 0}
                    onClick={() => onMoveLesson(lessonIndex, -1)}
                  >
                    <ArrowUp className="size-4" />
                  </Button>
                  <Button
                    size="icon"
                    variant="ghost"
                    aria-label="Descer aula"
                    disabled={busy || lessonIndex === courseModule.lessons.length - 1}
                    onClick={() => onMoveLesson(lessonIndex, 1)}
                  >
                    <ArrowDown className="size-4" />
                  </Button>
                  <Button size="sm" variant="outline" onClick={() => onEditLesson(lesson)}>
                    Editar
                  </Button>
                  <Button
                    size="icon"
                    variant="ghost"
                    aria-label="Excluir aula"
                    disabled={busy}
                    onClick={() => onDeleteLesson(lesson)}
                  >
                    <Trash2 className="size-4" />
                  </Button>
                </div>
              </li>
            ))}
          </ul>
        )}

        <Button variant="outline" size="sm" onClick={onAddLesson}>
          <Plus className="size-4" /> Adicionar aula
        </Button>
      </CardContent>
    </Card>
  );
}
