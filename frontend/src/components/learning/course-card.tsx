import Link from "next/link";

import { ProgressBar } from "@/components/learning/progress-bar";
import { Thumbnail } from "@/components/learning/thumbnail";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import type { CourseCard as CourseCardData } from "@/types/learning";

export function CourseCard({ course, position }: { course: CourseCardData; position?: number }) {
  const done = course.progress.percent === 100;

  return (
    <Link href={`/cursos/${course.id}`} className="group block h-full">
      <Card className="h-full overflow-hidden pt-0 transition-shadow group-hover:shadow-md">
        <Thumbnail src={course.thumbnailUrl} />
        <CardContent className="space-y-2">
          <div className="flex items-start justify-between gap-2">
            <h3 className="font-semibold leading-snug">
              {position !== undefined && <span className="mr-1.5 text-muted-foreground">{position}.</span>}
              {course.title}
            </h3>
            {done && <Badge>Concluído</Badge>}
          </div>
          <p className="text-sm text-muted-foreground">
            {course.workloadHours ? `${course.workloadHours}h · ` : ""}
            {course.instructorName}
          </p>
          {course.description && (
            <p className="line-clamp-2 text-sm text-muted-foreground">{course.description}</p>
          )}
          {course.enrolled ? (
            <div className="space-y-1 pt-1">
              <ProgressBar percent={course.progress.percent} />
              <p className="text-xs text-muted-foreground">
                {course.progress.percent}% · {course.progress.completedLessons} de {course.progress.totalLessons} aulas
              </p>
            </div>
          ) : (
            <p className="pt-1 text-xs text-muted-foreground">{course.progress.totalLessons} aulas</p>
          )}
        </CardContent>
      </Card>
    </Link>
  );
}
