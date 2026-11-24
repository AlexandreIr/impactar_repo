const VIDEO_ID = /^[A-Za-z0-9_-]{11}$/;

/** Mesmas regras do backend (YouTube.java): watch, youtu.be, embed, shorts, live. */
export function extractYouTubeId(url: string | null | undefined): string | null {
  if (!url || !url.trim()) return null;

  let value = url.trim();
  if (!/^https?:\/\//i.test(value)) value = `https://${value}`;

  let parsed: URL;
  try {
    parsed = new URL(value);
  } catch {
    return null;
  }

  const host = parsed.hostname.toLowerCase().replace(/^(www\.|m\.|music\.)/, "");
  const segments = parsed.pathname.split("/");
  let candidate: string | null = null;

  if (host === "youtu.be") {
    candidate = segments[1] ?? null;
  } else if (host === "youtube.com" || host === "youtube-nocookie.com") {
    if (parsed.pathname === "/watch") {
      candidate = parsed.searchParams.get("v");
    } else if (["embed", "shorts", "live", "v"].includes(segments[1] ?? "")) {
      candidate = segments[2] ?? null;
    }
  }

  return candidate && VIDEO_ID.test(candidate) ? candidate : null;
}

export function youTubeEmbedUrl(videoId: string): string {
  return `https://www.youtube-nocookie.com/embed/${videoId}?rel=0`;
}
