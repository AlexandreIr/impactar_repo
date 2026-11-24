"use client";

import { useEffect, useState, type ReactNode } from "react";
import Link from "next/link";
import { PlayCircle } from "lucide-react";
import { toast } from "sonner";

import { AppHeader } from "@/components/app-header";
import { CourseCard } from "@/components/learning/course-card";
import { ProgressBar } from "@/components/learning/progress-bar";
import { Thumbnail } from "@/components/learning/thumbnail";
import { TrailCard } from "@/components/learning/trail-card";
import { buttonVariants } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { getErrorMessage } from "@/lib/api";
import { cn } from "@/lib/utils";
import { learningService } from "@/services/learning-service";
import type { LearningHome } from "@/types/learning";

function Section({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="space-y-3">
      <h2 className="text-lg font-semibold">{title}</h2>
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">{children}</div>
    </section>
  );
}

export default function StudentHomePage() {
  const [home, setHome] = useState<LearningHome | null>(null);

  useEffect(() => {
    learningService
      .home()
      .then(setHome)
      .catch((error) => toast.error(getErrorMessage(error)));
  }, []);

  const resume = home?.continueLearning;
  const isEmpty =
    home &&
    !home.myTrails.length &&
    !home.myCourses.length &&
    !home.availableTrails.length &&
    !home.availableCourses.length;

  return (
    <>
      <AppHeader title="Cursos" />
      <main className="mx-auto max-w-6xl space-y-10 px-4 py-8">
        {!home && <p className="text-muted-foreground">Carregando...</p>}

        {resume && (
          <Card className="overflow-hidden py-0">
            <CardContent className="flex flex-col gap-4 p-0 sm:flex-row">
              <Thumbnail src={resume.thumbnailUrl} className="sm:w-72" />
              <div className="flex flex-1 flex-col justify-center gap-3 p-5 sm:pl-0">
                <p className="text-xs font-semibold uppercase tracking-wide text-primary">Continuar de onde parou</p>
                <div>
                  <h2 className="text-xl font-semibold">{resume.courseTitle}</h2>
                  <p className="text-sm text-muted-foreground">Próxima aula: {resume.lessonTitle}</p>
                </div>
                <div className="max-w-sm space-y-1">
                  <ProgressBar percent={resume.progress.percent} />
                  <p className="text-xs text-muted-foreground">{resume.progress.percent}% concluído</p>
                </div>
                <Link
                  href={`/cursos/${resume.courseId}/aulas/${resume.lessonId}`}
                  className={cn(buttonVariants(), "w-fit")}
                >
                  <PlayCircle className="size-4" /> Continuar
                </Link>
              </div>
            </CardContent>
          </Card>
        )}

        {isEmpty && (
          <Card>
            <CardContent className="py-10 text-center text-muted-foreground">
              Nenhum curso disponível ainda. Volte em breve!
            </CardContent>
          </Card>
        )}

        {!!home?.myTrails.length && (
          <Section title="Minhas trilhas">
            {home.myTrails.map((trail) => (
              <TrailCard key={trail.id} trail={trail} />
            ))}
          </Section>
        )}

        {!!home?.myCourses.length && (
          <Section title="Meus cursos">
            {home.myCourses.map((course) => (
              <CourseCard key={course.id} course={course} />
            ))}
          </Section>
        )}

        {!!home?.availableTrails.length && (
          <Section title="Trilhas disponíveis">
            {home.availableTrails.map((trail) => (
              <TrailCard key={trail.id} trail={trail} />
            ))}
          </Section>
        )}

        {!!home?.availableCourses.length && (
          <Section title="Cursos disponíveis">
            {home.availableCourses.map((course) => (
              <CourseCard key={course.id} course={course} />
            ))}
          </Section>
        )}
      </main>
    </>
  );
}
