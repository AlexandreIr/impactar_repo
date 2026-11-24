"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { ArrowLeft } from "lucide-react";
import { toast } from "sonner";

import { AppHeader } from "@/components/app-header";
import { CourseCard } from "@/components/learning/course-card";
import { LinkifiedText } from "@/components/learning/linkified-text";
import { ProgressBar } from "@/components/learning/progress-bar";
import { Thumbnail } from "@/components/learning/thumbnail";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { getErrorMessage } from "@/lib/api";
import { learningService } from "@/services/learning-service";
import type { StudentTrail } from "@/types/learning";

export default function StudentTrailPage() {
  const { trailId } = useParams<{ trailId: string }>();
  const router = useRouter();
  const [trail, setTrail] = useState<StudentTrail | null>(null);
  const [enrolling, setEnrolling] = useState(false);

  useEffect(() => {
    learningService
      .trail(trailId)
      .then(setTrail)
      .catch((error) => {
        toast.error(getErrorMessage(error));
        router.replace("/cursos");
      });
  }, [trailId, router]);

  async function handleEnroll() {
    setEnrolling(true);
    try {
      setTrail(await learningService.enrollTrail(trailId));
      toast.success("Você entrou na trilha e foi matriculado em todos os cursos dela.");
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setEnrolling(false);
    }
  }

  if (!trail) {
    return (
      <>
        <AppHeader title="Cursos" />
        <main className="mx-auto max-w-6xl px-4 py-8 text-muted-foreground">Carregando trilha...</main>
      </>
    );
  }

  return (
    <>
      <AppHeader title="Cursos" />
      <main className="mx-auto max-w-6xl space-y-8 px-4 py-8">
        <Link href="/cursos" className="inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground">
          <ArrowLeft className="size-4" /> Cursos
        </Link>

        <div className="grid gap-6 md:grid-cols-[320px_1fr]">
          <Thumbnail src={trail.thumbnailUrl} kind="trail" className="rounded-lg" />
          <div className="space-y-3">
            <Badge variant="secondary">Trilha</Badge>
            <h1 className="text-2xl font-semibold">{trail.title}</h1>
            <p className="text-sm text-muted-foreground">
              {trail.courses.length} curso(s)
              {trail.workloadHours > 0 ? ` · ${trail.workloadHours}h no total` : ""}
            </p>
            {trail.description && <LinkifiedText text={trail.description} />}

            {trail.enrolled ? (
              <div className="max-w-sm space-y-1">
                <ProgressBar percent={trail.progress.percent} />
                <p className="text-xs text-muted-foreground">
                  {trail.progress.percent}% · {trail.progress.completedLessons} de {trail.progress.totalLessons} aulas
                </p>
              </div>
            ) : (
              <Button size="lg" onClick={handleEnroll} disabled={enrolling || trail.courses.length === 0}>
                {enrolling ? "Entrando..." : "Iniciar trilha"}
              </Button>
            )}
          </div>
        </div>

        <section className="space-y-3">
          <h2 className="text-lg font-semibold">Cursos da trilha</h2>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {trail.courses.map((course, index) => (
              <CourseCard key={course.id} course={course} position={index + 1} />
            ))}
          </div>
        </section>
      </main>
    </>
  );
}
