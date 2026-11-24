// Next.js 16+: arquivo src/proxy.ts com função `proxy`.
// Next.js 15: renomeie para src/middleware.ts e a função para `middleware`.
import { NextResponse, type NextRequest } from "next/server";

import { AUTH_COOKIE, CHANGE_PASSWORD_PATH, LOGIN_PATH, decodeToken, homeFor } from "@/lib/auth";

/** Páginas acessíveis com ou sem login. */
const PUBLIC_PATHS = ["/validar", "/esqueci-senha", "/redefinir-senha"];

export function proxy(request: NextRequest) {
  const { pathname, search } = request.nextUrl;

  if (PUBLIC_PATHS.some((path) => pathname === path || pathname.startsWith(`${path}/`))) {
    return NextResponse.next();
  }

  const token = request.cookies.get(AUTH_COOKIE)?.value;
  const session = token ? decodeToken(token) : null;
  const redirectTo = (path: string) => NextResponse.redirect(new URL(path, request.url));

  // Sem sessão: só o login é acessível.
  if (!session) {
    if (pathname === LOGIN_PATH) return NextResponse.next();
    const loginUrl = new URL(LOGIN_PATH, request.url);
    if (pathname !== "/") loginUrl.searchParams.set("next", pathname + search);
    return NextResponse.redirect(loginUrl);
  }

  // Primeiro acesso: troca de senha obrigatória.
  if (session.mcp) {
    return pathname === CHANGE_PASSWORD_PATH ? NextResponse.next() : redirectTo(CHANGE_PASSWORD_PATH);
  }

  const home = homeFor(session.role);

  if (pathname === "/" || pathname === LOGIN_PATH || pathname === CHANGE_PASSWORD_PATH) {
    return redirectTo(home);
  }

  // Acesso por perfil (o backend aplica as mesmas regras na API).
  if (pathname.startsWith("/admin") && session.role !== "ADMIN") {
    return redirectTo(home);
  }
  if (pathname.startsWith("/instrutor") && session.role === "STUDENT") {
    return redirectTo(home);
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/((?!api|_next/static|_next/image|favicon.ico|.*\\.(?:png|jpg|jpeg|svg|gif|webp|ico)$).*)"],
};
