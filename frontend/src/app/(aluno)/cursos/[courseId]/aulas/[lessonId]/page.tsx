"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { ArrowLeft, ArrowRight, CheckCircle2, ExternalLink } from "lucide-react";
import { toast } from "sonner";

import { AppHeader } from "@/components/app-header";
import { LessonOutline } from "@/components/learning/lesson-outline";
import { LinkifiedText } from "@/components/learning/linkified-text";
import { YouTubePlayer } from "@/components/learning/youtube-player";
import { Button, buttonVariants } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { getErrorMessage, getErrorStatus } from "@/lib/api";
import { learningService } from "@/services/learning-service";
import type { LessonPlayer, StudentCourse } from "@/types/learning";

export default function LessonPlayerPage() {
  const { courseId, lessonId } = useParams<{ courseId: string; lessonId: string }>();
  const router = useRouter();

  const [lesson, setLesson] = useState<LessonPlayer | null>(null);
  const [completed, setCompleted] = useState(false);
  const [course, setCourse] = useState<StudentCourse | null>(null);
  const [courseReloadKey, setCourseReloadKey] = useState(0);
  const [saving, setSaving] = useState(false);

  // Aula atual (ignora a anterior enquanto a nova carrega)
  useEffect(() => {
    let cancelled = false;
    learningService
      .lesson(lessonId)
      .then((data) => {
        if (cancelled) return;
        setLesson(data);
        setCompleted(data.completed);
        // registra o acesso: base do "continuar de onde parou"
        learningService.savePosition(data.id, data.lastPositionSeconds).catch(() => undefined);
      })
      .catch((error) => {
        if (cancelled) return;
        toast.error(getErrorMessage(error));
        router.replace(getErrorStatus(error) === 403 ? `/cursos/${courseId}` : "/cursos");
      });
    return () => {
      cancelled = true;
    };
  }, [lessonId, courseId, router]);

  // Sumário do curso (recarrega ao concluir aulas)
  useEffect(() => {
    learningService
      .course(courseId)
      .then(setCourse)
      .catch(() => undefined);
  }, [courseId, courseReloadKey]);

  const current = lesson?.id === lessonId ? lesson : null;

  const markCompleted = useCallback(
    async (value: boolean) => {
      if (!current) return;
      setSaving(true);
      try {
        const result = value
          ? await learningService.complete(current.id)
          : await learningService.uncomplete(current.id);
        setCompleted(result.completed);
        setCourseReloadKey((key) => key + 1);

        if (!value) return;
        if (result.certificateIssued) {
          toast.success("Parabéns! Seu certificado foi emitido.", {
            action: { label: "Ver certificados", onClick: () => router.push("/certificados") },
          });
        } else if (result.courseProgress.percent === 100) {
          toast.success("Parabéns! Você concluiu o curso.");
        } else if (result.nextLessonId) {
          const nextPath = `/cursos/${current.courseId}/aulas/${result.nextLessonId}`;
          toast.success("Aula concluída!", {
            action: { label: "Próxima aula", onClick: () => router.push(nextPath) },
          });
        } else {
          toast.success("Aula concluída!");
        }
      } catch (error) {
        toast.error(getErrorMessage(error));
      } finally {
        setSaving(false);
      }
    },
    [current, router],
  );

  if (!current) {
    return (
      <>
        <AppHeader title="Cursos" />
        <main className="mx-auto max-w-7xl px-4 py-8 text-muted-foreground">Carregando aula...</main>
      </>
    );
  }

  return (
    <>
      <AppHeader title="Cursos" />
      <main className="mx-auto grid max-w-7xl gap-6 px-4 py-6 lg:grid-cols-[1fr_340px]">
        <div className="min-w-0 space-y-5">
          <Link
            href={`/cursos/${current.courseId}`}
            className="inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground"
          >
            <ArrowLeft className="size-4" /> {current.courseTitle}
          </Link>

          {current.videoId ? (
            <YouTubePlayer
              key={current.id}
              videoId={current.videoId}
              startSeconds={current.completed ? 0 : current.lastPositionSeconds}
              onProgress={(seconds) => learningService.savePosition(current.id, seconds).catch(() => undefined)}
              onEnded={() => {
                if (!completed) markCompleted(true);
              }}
            />
          ) : (
            <div className="flex aspect-video w-full items-center justify-center rounded-lg bg-muted text-sm text-muted-foreground">
              Esta aula não tem vídeo. Confira o conteúdo abaixo.
            </div>
          )}

          <div className="flex flex-wrap items-start justify-between gap-3">
            <div>
              <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">{current.moduleTitle}</p>
              <h1 className="text-xl font-semibold">{current.title}</h1>
            </div>
            <Button
              variant={completed ? "secondary" : "default"}
              onClick={() => markCompleted(!completed)}
              disabled={saving}
            >
              <CheckCircle2 className="size-4" />
              {completed ? "Concluída (desmarcar)" : "Marcar como concluída"}
            </Button>
          </div>

          <div className="flex justify-between gap-2">
            {current.previousLessonId ? (
              <Link
                href={`/cursos/${current.courseId}/aulas/${current.previousLessonId}`}
                className={buttonVariants({ variant: "outline", size: "sm" })}
              >
                <ArrowLeft className="size-4" /> Aula anterior
              </Link>
            ) : (
              <span />
            )}
            {current.nextLessonId && (
              <Link
                href={`/cursos/${current.courseId}/aulas/${current.nextLessonId}`}
                className={buttonVariants({ variant: "outline", size: "sm" })}
              >
                Próxima aula <ArrowRight className="size-4" />
              </Link>
            )}
          </div>

          {current.description && (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Sobre esta aula</CardTitle>
              </CardHeader>
              <CardContent>
                <LinkifiedText text={current.description} />
              </CardContent>
            </Card>
          )}

          {current.materials.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Materiais complementares</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2">
                  {current.materials.map((material) => (
                    <li key={material.id}>
                      <a
                        href={material.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="inline-flex items-center gap-2 text-sm text-primary hover:underline"
                      >
                        <ExternalLink className="size-4" /> {material.title}
                      </a>
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          )}
        </div>

        <aside>
          <Card className="lg:sticky lg:top-4">
            <CardHeader>
              <CardTitle className="text-base">Conteúdo do curso</CardTitle>
            </CardHeader>
            <CardContent className="max-h-[70vh] overflow-y-auto">
              {course ? (
                <LessonOutline course={course} currentLessonId={current.id} linkLessons />
              ) : (
                <p className="text-sm text-muted-foreground">Carregando...</p>
              )}
            </CardContent>
          </Card>
        </aside>
      </main>
    </>
  );
}
