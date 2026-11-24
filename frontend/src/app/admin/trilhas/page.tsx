"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Plus } from "lucide-react";
import { toast } from "sonner";

import { TrailFormDialog } from "@/components/admin/trail-form-dialog";
import { AppHeader } from "@/components/app-header";
import { Badge } from "@/components/ui/badge";
import { Button, buttonVariants } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { getErrorMessage } from "@/lib/api";
import { trailAdminService } from "@/services/trail-admin-service";
import type { TrailAdminSummary } from "@/types/trail";

export default function AdminTrailsPage() {
  const router = useRouter();
  const [trails, setTrails] = useState<TrailAdminSummary[] | null>(null);
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    trailAdminService
      .list()
      .then(setTrails)
      .catch((error) => {
        toast.error(getErrorMessage(error));
        setTrails([]);
      });
  }, []);

  return (
    <>
      <AppHeader title="Administração" />
      <main className="mx-auto max-w-6xl space-y-6 px-4 py-8">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-xl font-semibold">Trilhas</h2>
            <p className="text-sm text-muted-foreground">
              {trails ? `${trails.length} trilha(s)` : "Carregando..."}
            </p>
          </div>
          <Button onClick={() => setCreating(true)}>
            <Plus className="size-4" /> Nova trilha
          </Button>
        </div>

        <div className="overflow-x-auto rounded-md border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Título</TableHead>
                <TableHead>Cursos</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Ações</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {trails?.length === 0 && (
                <TableRow>
                  <TableCell colSpan={4} className="py-8 text-center text-muted-foreground">
                    Nenhuma trilha ainda.
                  </TableCell>
                </TableRow>
              )}
              {trails?.map((trail) => (
                <TableRow key={trail.id}>
                  <TableCell className="font-medium">{trail.title}</TableCell>
                  <TableCell>{trail.courseCount}</TableCell>
                  <TableCell>
                    <Badge variant={trail.published ? "default" : "secondary"}>
                      {trail.published ? "Publicada" : "Rascunho"}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <Link
                      href={`/admin/trilhas/${trail.id}`}
                      className={buttonVariants({ variant: "outline", size: "sm" })}
                    >
                      Editar
                    </Link>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      </main>

      {creating && (
        <TrailFormDialog
          onOpenChange={(open) => !open && setCreating(false)}
          onSaved={(trail) => router.push(`/admin/trilhas/${trail.id}`)}
        />
      )}
    </>
  );
}
