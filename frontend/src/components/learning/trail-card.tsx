import Link from "next/link";

import { ProgressBar } from "@/components/learning/progress-bar";
import { Thumbnail } from "@/components/learning/thumbnail";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import type { TrailCard as TrailCardData } from "@/types/learning";

export function TrailCard({ trail }: { trail: TrailCardData }) {
  return (
    <Link href={`/trilhas/${trail.id}`} className="group block h-full">
      <Card className="h-full overflow-hidden pt-0 transition-shadow group-hover:shadow-md">
        <Thumbnail src={trail.thumbnailUrl} kind="trail" />
        <CardContent className="space-y-2">
          <div className="flex items-start justify-between gap-2">
            <h3 className="font-semibold leading-snug">{trail.title}</h3>
            <Badge variant="secondary">Trilha</Badge>
          </div>
          <p className="text-sm text-muted-foreground">
            {trail.courseCount} curso(s){trail.workloadHours > 0 ? ` · ${trail.workloadHours}h` : ""}
          </p>
          {trail.description && <p className="line-clamp-2 text-sm text-muted-foreground">{trail.description}</p>}
          {trail.enrolled && (
            <div className="space-y-1 pt-1">
              <ProgressBar percent={trail.progress.percent} />
              <p className="text-xs text-muted-foreground">{trail.progress.percent}% concluído</p>
            </div>
          )}
        </CardContent>
      </Card>
    </Link>
  );
}
