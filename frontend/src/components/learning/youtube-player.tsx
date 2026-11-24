"use client";

import { useEffect, useRef } from "react";

// ---------------------------------------------------------------------------
// Tipos mínimos da YouTube IFrame API
// ---------------------------------------------------------------------------
interface YTPlayer {
  getCurrentTime?: () => number;
  destroy: () => void;
}

interface YTNamespace {
  Player: new (
    element: HTMLElement,
    options: {
      videoId: string;
      host?: string;
      playerVars?: Record<string, number | string>;
      events?: { onStateChange?: (event: { data: number }) => void };
    },
  ) => YTPlayer;
  PlayerState: { ENDED: number; PLAYING: number; PAUSED: number };
}

declare global {
  interface Window {
    YT?: YTNamespace;
    onYouTubeIframeAPIReady?: () => void;
  }
}

let apiPromise: Promise<YTNamespace> | null = null;

/** Carrega o script da IFrame API uma única vez. */
function loadYouTubeApi(): Promise<YTNamespace> {
  if (window.YT?.Player) return Promise.resolve(window.YT);
  if (!apiPromise) {
    apiPromise = new Promise((resolve) => {
      const previous = window.onYouTubeIframeAPIReady;
      window.onYouTubeIframeAPIReady = () => {
        previous?.();
        if (window.YT) resolve(window.YT);
      };
      const script = document.createElement("script");
      script.src = "https://www.youtube.com/iframe_api";
      script.async = true;
      document.head.appendChild(script);
    });
  }
  return apiPromise;
}

const REPORT_INTERVAL_MS = 15_000;

interface YouTubePlayerProps {
  videoId: string;
  /** Segundo em que o vídeo começa (retomada). */
  startSeconds: number;
  /** Posição atual: a cada 15 s tocando, ao pausar, ao terminar e ao sair da aula. */
  onProgress: (seconds: number) => void;
  onEnded?: () => void;
}

/**
 * Player do YouTube com retomada e registro de posição.
 * Use key={lessonId} para recriar o player ao trocar de aula.
 */
export function YouTubePlayer({ videoId, startSeconds, onProgress, onEnded }: YouTubePlayerProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const onProgressRef = useRef(onProgress);
  const onEndedRef = useRef(onEnded);

  useEffect(() => {
    onProgressRef.current = onProgress;
    onEndedRef.current = onEnded;
  });

  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    let player: YTPlayer | null = null;
    let interval: number | undefined;
    let cancelled = false;
    let lastReported = -1;

    const report = () => {
      const seconds = Math.floor(player?.getCurrentTime?.() ?? 0);
      if (seconds > 0 && seconds !== lastReported) {
        lastReported = seconds;
        onProgressRef.current(seconds);
      }
    };

    loadYouTubeApi().then((YT) => {
      if (cancelled) return;
      const target = document.createElement("div");
      container.appendChild(target); // a API substitui este elemento pelo iframe

      player = new YT.Player(target, {
        videoId,
        host: "https://www.youtube-nocookie.com",
        playerVars: { start: Math.max(0, Math.floor(startSeconds)), rel: 0, modestbranding: 1, playsinline: 1 },
        events: {
          onStateChange: (event) => {
            window.clearInterval(interval);
            if (event.data === YT.PlayerState.PLAYING) {
              interval = window.setInterval(report, REPORT_INTERVAL_MS);
            } else {
              report();
              if (event.data === YT.PlayerState.ENDED) onEndedRef.current?.();
            }
          },
        },
      });
    });

    return () => {
      cancelled = true;
      window.clearInterval(interval);
      report();
      player?.destroy();
      container.innerHTML = "";
    };
  }, [videoId, startSeconds]);

  return (
    <div
      ref={containerRef}
      className="aspect-video w-full overflow-hidden rounded-lg bg-black [&>iframe]:h-full [&>iframe]:w-full"
    />
  );
}
