import type { Role, SessionPayload } from "@/types/auth";

export const AUTH_COOKIE = "impactar_token";
export const LOGIN_PATH = "/login";
export const CHANGE_PASSWORD_PATH = "/alterar-senha";

const HOME_BY_ROLE: Record<Role, string> = {
  ADMIN: "/admin",
  INSTRUCTOR: "/instrutor",
  STUDENT: "/cursos",
};

export function homeFor(role: Role): string {
  return HOME_BY_ROLE[role];
}

function isRole(value: unknown): value is Role {
  return value === "ADMIN" || value === "INSTRUCTOR" || value === "STUDENT";
}

/**
 * Lê o payload do JWT sem validar a assinatura.
 * Serve só para roteamento no front; quem valida o token é o backend em toda chamada.
 */
export function decodeToken(token: string): SessionPayload | null {
  try {
    const payload = token.split(".")[1];
    if (!payload) return null;

    const base64 = payload
      .replace(/-/g, "+")
      .replace(/_/g, "/")
      .padEnd(Math.ceil(payload.length / 4) * 4, "=");
    const bytes = Uint8Array.from(atob(base64), (char) => char.charCodeAt(0));
    const data = JSON.parse(new TextDecoder().decode(bytes)) as Partial<SessionPayload>;

    if (typeof data.exp !== "number" || data.exp * 1000 <= Date.now()) return null;
    if (!isRole(data.role) || typeof data.sub !== "string") return null;

    return {
      sub: data.sub,
      role: data.role,
      name: data.name ?? "",
      mcp: data.mcp === true,
      exp: data.exp,
    };
  } catch {
    return null;
  }
}

/** Aceita apenas caminhos internos (evita open redirect via ?next=). */
export function safeNextPath(value: string | null): string | null {
  if (!value || !value.startsWith("/") || value.startsWith("//")) return null;
  return value;
}
