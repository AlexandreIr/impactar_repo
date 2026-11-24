import { Fragment } from "react";

import { cn } from "@/lib/utils";

// Captura links http(s); pontuação final (.,;:!?) fica fora do link.
const URL_PATTERN = /(https?:\/\/[^\s<]+[^\s<.,;:!?)\]'"])/g;

/** Texto com quebras de linha preservadas e links clicáveis (sem HTML injetado). */
export function LinkifiedText({ text, className }: { text: string; className?: string }) {
  const parts = text.split(URL_PATTERN);

  return (
    <p className={cn("whitespace-pre-line text-sm leading-relaxed", className)}>
      {parts.map((part, index) =>
        index % 2 === 1 ? (
          <a
            key={index}
            href={part}
            target="_blank"
            rel="noopener noreferrer"
            className="break-all text-primary underline underline-offset-2"
          >
            {part}
          </a>
        ) : (
          <Fragment key={index}>{part}</Fragment>
        ),
      )}
    </p>
  );
}
