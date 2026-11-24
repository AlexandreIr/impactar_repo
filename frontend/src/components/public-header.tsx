import Link from "next/link";

/** Cabeçalho das páginas públicas (sem consulta de sessão). */
export function PublicHeader() {
  return (
    <header className="border-b bg-background">
      <div className="mx-auto flex max-w-3xl items-center justify-between px-4 py-3">
        <Link href="/validar" className="text-lg font-semibold">
          impactar
        </Link>
        <Link href="/login" className="text-sm text-muted-foreground hover:text-foreground">
          Entrar
        </Link>
      </div>
    </header>
  );
}
