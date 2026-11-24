import Link from "next/link";
import { CheckCircle2, Circle, PlayCircle } from "lucide-react";

import { ProgressBar } from "@/components/learning/progress-bar";
import { cn } from "@/lib/utils";
import type { StudentCourse } from "@/types/learning";

interface LessonOutlineProps {
  course: StudentCourse;
  currentLessonId?: string;
  /** Sem matrícula as aulas aparecem sem link. */
  linkLessons: boolean;
}

export function LessonOutline({ course, currentLessonId, linkLessons }: LessonOutlineProps) {
  return (
    <div className="space-y-4">
      {course.enrolled && (
        <div className="space-y-1.5">
          <ProgressBar percent={course.progress.percent} />
          <p className="text-xs text-muted-foreground">
            {course.progress.percent}% · {course.progress.completedLessons} de {course.progress.totalLessons} aulas
            concluídas
          </p>
        </div>
      )}

      {course.modules.map((courseModule, moduleIndex) => (
        <div key={courseModule.id} className="space-y-1">
          <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
            Módulo {moduleIndex + 1} · {courseModule.title}
          </p>
          <ul className="space-y-0.5">
            {courseModule.lessons.map((lesson) => {
              const current = lesson.id === currentLessonId;
              const Icon = lesson.completed ? CheckCircle2 : current ? PlayCircle : Circle;
              const content = (
                <>
                  <Icon
                    className={cn(
                      "size-4 shrink-0",
                      lesson.completed ? "text-primary" : "text-muted-foreground",
                    )}
                  />
                  <span className="line-clamp-2">{lesson.title}</span>
                </>
              );
              const className = cn(
                "flex items-start gap-2 rounded-md px-2 py-1.5 text-sm",
                current && "bg-muted font-medium",
                linkLessons && !current && "hover:bg-muted/60",
              );

              return (
                <li key={lesson.id}>
                  {linkLessons ? (
                    <Link
                      href={`/cursos/${course.id}/aulas/${lesson.id}`}
                      className={className}
                      aria-current={current ? "page" : undefined}
                    >
                      {content}
                    </Link>
                  ) : (
                    <div className={className}>{content}</div>
                  )}
                </li>
              );
            })}
          </ul>
        </div>
      ))}
    </div>
  );
}
