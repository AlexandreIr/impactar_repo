"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { BookOpen, Plus } from "lucide-react";
import { toast } from "sonner";

import { AppHeader } from "@/components/app-header";
import { CourseFormDialog } from "@/components/instructor/course-form-dialog";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { getErrorMessage } from "@/lib/api";
import { instructorService } from "@/services/instructor-service";
import type { CourseSummary } from "@/types/course";

export default function InstructorCoursesPage() {
  const router = useRouter();
  const [courses, setCourses] = useState<CourseSummary[] | null>(null);
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    instructorService
      .listCourses()
      .then(setCourses)
      .catch((error) => {
        toast.error(getErrorMessage(error));
        setCourses([]);
      });
  }, []);

  return (
    <>
      <AppHeader title="Área do Instrutor" />
      <main className="mx-auto max-w-6xl space-y-6 px-4 py-8">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-xl font-semibold">Cursos</h2>
            <p className="text-sm text-muted-foreground">
              {courses ? `${courses.length} curso(s)` : "Carregando..."}
            </p>
          </div>
          <Button onClick={() => setCreating(true)}>
            <Plus className="size-4" /> Novo curso
          </Button>
        </div>

        {courses?.length === 0 && (
          <Card>
            <CardContent className="py-10 text-center text-muted-foreground">
              Nenhum curso ainda. Clique em &quot;Novo curso&quot; para começar.
            </CardContent>
          </Card>
        )}

        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {courses?.map((course) => (
            <Link key={course.id} href={`/instrutor/cursos/${course.id}`} className="group">
              <Card className="h-full overflow-hidden pt-0 transition-shadow group-hover:shadow-md">
                {course.thumbnailUrl ? (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img src={course.thumbnailUrl} alt="" className="aspect-video w-full object-cover" />
                ) : (
                  <div className="flex aspect-video w-full items-center justify-center bg-muted">
                    <BookOpen className="size-10 text-muted-foreground" />
                  </div>
                )}
                <CardContent className="space-y-2">
                  <div className="flex items-start justify-between gap-2">
                    <h3 className="font-semibold leading-snug">{course.title}</h3>
                    <Badge variant={course.published ? "default" : "secondary"}>
                      {course.published ? "Publicado" : "Rascunho"}
                    </Badge>
                  </div>
                  <p className="text-sm text-muted-foreground">
                    {course.workloadHours ? `${course.workloadHours}h` : "Sem carga horária"} · {course.instructorName}
                  </p>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      </main>

      {creating && (
        <CourseFormDialog
          onOpenChange={(open) => !open && setCreating(false)}
          onSaved={(course) => router.push(`/instrutor/cursos/${course.id}`)}
        />
      )}
    </>
  );
}
