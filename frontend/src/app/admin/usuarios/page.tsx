"use client";

import { useEffect, useState, type FormEvent } from "react";
import { toast } from "sonner";

import { ResetPasswordDialog } from "@/components/admin/reset-password-dialog";
import { UserFormDialog } from "@/components/admin/user-form-dialog";
import { AppHeader } from "@/components/app-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { getErrorMessage } from "@/lib/api";
import { ROLE_LABELS } from "@/lib/roles";
import { userService } from "@/services/user-service";
import type { PageResponse } from "@/types/api";
import type { UserSummary } from "@/types/user";

const PAGE_SIZE = 20;

type FormDialogState = { mode: "create" } | { mode: "edit"; user: UserSummary } | null;

function StatusBadge({ user }: { user: UserSummary }) {
  if (!user.active) return <Badge variant="destructive">Inativo</Badge>;
  if (user.mustChangePassword) return <Badge variant="secondary">Aguardando 1º acesso</Badge>;
  return <Badge variant="outline">Ativo</Badge>;
}

export default function AdminUsersPage() {
  const [searchInput, setSearchInput] = useState("");
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(0);
  const [reloadKey, setReloadKey] = useState(0);
  const [result, setResult] = useState<PageResponse<UserSummary> | null>(null);
  const [loading, setLoading] = useState(true);
  const [formDialog, setFormDialog] = useState<FormDialogState>(null);
  const [resetTarget, setResetTarget] = useState<UserSummary | null>(null);

  useEffect(() => {
    let cancelled = false;
    userService
      .list({ search: query || undefined, page, size: PAGE_SIZE })
      .then((data) => {
        if (!cancelled) setResult(data);
      })
      .catch((error) => {
        if (!cancelled) toast.error(getErrorMessage(error));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [query, page, reloadKey]);

  function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setPage(0);
    setQuery(searchInput.trim());
    setReloadKey((key) => key + 1);
  }

  function goToPage(nextPage: number) {
    setLoading(true);
    setPage(nextPage);
  }

  function reload() {
    setLoading(true);
    setReloadKey((key) => key + 1);
  }

  const users = result?.content ?? [];
  const totalPages = result?.totalPages ?? 0;

  return (
    <>
      <AppHeader title="Administração" />
      <main className="mx-auto max-w-6xl space-y-6 px-4 py-8">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-xl font-semibold">Usuários</h2>
            <p className="text-sm text-muted-foreground">
              {result ? `${result.totalElements} usuário(s)` : "Carregando..."}
            </p>
          </div>
          <Button onClick={() => setFormDialog({ mode: "create" })}>Novo usuário</Button>
        </div>

        <form onSubmit={handleSearch} className="flex gap-2">
          <Input
            placeholder="Buscar por nome ou e-mail"
            value={searchInput}
            onChange={(event) => setSearchInput(event.target.value)}
            className="max-w-sm"
          />
          <Button type="submit" variant="outline">
            Buscar
          </Button>
        </form>

        <div className="overflow-x-auto rounded-md border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nome</TableHead>
                <TableHead>E-mail</TableHead>
                <TableHead>Perfil</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Ações</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {!loading && users.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} className="py-8 text-center text-muted-foreground">
                    Nenhum usuário encontrado.
                  </TableCell>
                </TableRow>
              )}
              {users.map((user) => (
                <TableRow key={user.id} className={loading ? "opacity-60" : undefined}>
                  <TableCell className="font-medium">{user.name}</TableCell>
                  <TableCell>{user.email}</TableCell>
                  <TableCell>{ROLE_LABELS[user.role]}</TableCell>
                  <TableCell>
                    <StatusBadge user={user} />
                  </TableCell>
                  <TableCell className="space-x-2 whitespace-nowrap text-right">
                    <Button size="sm" variant="outline" onClick={() => setFormDialog({ mode: "edit", user })}>
                      Editar
                    </Button>
                    <Button size="sm" variant="ghost" disabled={!user.active} onClick={() => setResetTarget(user)}>
                      Reenviar acesso
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>

        {totalPages > 1 && (
          <div className="flex items-center justify-end gap-2">
            <span className="text-sm text-muted-foreground">
              Página {page + 1} de {totalPages}
            </span>
            <Button size="sm" variant="outline" disabled={loading || page === 0} onClick={() => goToPage(page - 1)}>
              Anterior
            </Button>
            <Button
              size="sm"
              variant="outline"
              disabled={loading || page + 1 >= totalPages}
              onClick={() => goToPage(page + 1)}
            >
              Próxima
            </Button>
          </div>
        )}
      </main>

      {formDialog && (
        <UserFormDialog
          key={formDialog.mode === "edit" ? formDialog.user.id : "new"}
          open
          user={formDialog.mode === "edit" ? formDialog.user : undefined}
          onOpenChange={(open) => !open && setFormDialog(null)}
          onSaved={reload}
        />
      )}

      {resetTarget && (
        <ResetPasswordDialog
          user={resetTarget}
          onOpenChange={(open) => !open && setResetTarget(null)}
          onDone={reload}
        />
      )}
    </>
  );
}
