import { BookOpen, Route } from "lucide-react";

import { cn } from "@/lib/utils";

interface ThumbnailProps {
  src: string | null;
  kind?: "course" | "trail";
  className?: string;
}

/** Capa por link ou placeholder com ícone. */
export function Thumbnail({ src, kind = "course", className }: ThumbnailProps) {
  if (src) {
    // eslint-disable-next-line @next/next/no-img-element
    return <img src={src} alt="" className={cn("aspect-video w-full object-cover", className)} />;
  }
  const Icon = kind === "trail" ? Route : BookOpen;
  return (
    <div className={cn("flex aspect-video w-full items-center justify-center bg-muted", className)}>
      <Icon className="size-10 text-muted-foreground" />
    </div>
  );
}
