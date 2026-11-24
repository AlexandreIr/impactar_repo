"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { ArrowLeft, PlayCircle } from "lucide-react";
import { toast } from "sonner";

import { AppHeader } from "@/components/app-header";
import { LessonOutline } from "@/components/learning/lesson-outline";
import { LinkifiedText } from "@/components/learning/linkified-text";
import { Thumbnail } from "@/components/learning/thumbnail";
import { Button, buttonVariants } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { getErrorMessage } from "@/lib/api";
import { learningService } from "@/services/learning-service";
import type { StudentCourse } from "@/types/learning";

function lessonPath(course: StudentCourse) {
  return course.resumeLessonId ? `/cursos/${course.id}/aulas/${course.resumeLessonId}` : null;
}

export default function StudentCoursePage() {
  const { courseId } = useParams<{ courseId: string }>();
  const router = useRouter();
  const [course, setCourse] = useState<StudentCourse | null>(null);
  const [enrolling, setEnrolling] = useState(false);

  useEffect(() => {
    learningService
      .course(courseId)
      .then(setCourse)
      .catch((error) => {
        toast.error(getErrorMessage(error));
        router.replace("/cursos");
      });
  }, [courseId, router]);

  async function handleEnroll() {
    setEnrolling(true);
    try {
      const enrolled = await learningService.enroll(courseId);
      toast.success("Matrícula realizada. Bons estudos!");
      const path = lessonPath(enrolled);
      if (path) router.push(path);
      else setCourse(enrolled);
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setEnrolling(false);
    }
  }

  if (!course) {
    return (
      <>
        <AppHeader title="Cursos" />
        <main className="mx-auto max-w-6xl px-4 py-8 text-muted-foreground">Carregando curso...</main>
      </>
    );
  }

  const path = lessonPath(course);
  const ctaLabel =
    course.progress.percent === 100 ? "Revisar curso" : course.progress.completedLessons > 0 ? "Continuar" : "Começar";

  return (
    <>
      <AppHeader title="Cursos" />
      <main className="mx-auto max-w-6xl space-y-6 px-4 py-8">
        <Link href="/cursos" className="inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground">
          <ArrowLeft className="size-4" /> Cursos
        </Link>

        <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
          <div className="space-y-4">
            <Thumbnail src={course.thumbnailUrl} className="rounded-lg" />
            <div>
              <h1 className="text-2xl font-semibold">{course.title}</h1>
              <p className="text-sm text-muted-foreground">
                {course.workloadHours ? `${course.workloadHours}h · ` : ""}
                {course.progress.totalLessons} aulas · {course.instructorName}
              </p>
            </div>

            {course.enrolled ? (
              path && (
                <Link href={path} className={buttonVariants({ size: "lg" })}>
                  <PlayCircle className="size-4" /> {ctaLabel}
                </Link>
              )
            ) : (
              <Button size="lg" onClick={handleEnroll} disabled={enrolling || !path}>
                {enrolling ? "Matriculando..." : path ? "Matricular-se" : "Curso sem aulas"}
              </Button>
            )}

            {course.description && <LinkifiedText text={course.description} />}
          </div>

          <Card className="h-fit">
            <CardHeader>
              <CardTitle className="text-base">Conteúdo do curso</CardTitle>
            </CardHeader>
            <CardContent>
              <LessonOutline course={course} linkLessons={course.enrolled} />
            </CardContent>
          </Card>
        </div>
      </main>
    </>
  );
}
