"use client";

import { useState } from "react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { getErrorMessage } from "@/lib/api";
import { userService } from "@/services/user-service";
import type { UserSummary } from "@/types/user";

interface ResetPasswordDialogProps {
  user: UserSummary;
  onOpenChange: (open: boolean) => void;
  onDone: () => void;
}

export function ResetPasswordDialog({ user, onOpenChange, onDone }: ResetPasswordDialogProps) {
  const [pending, setPending] = useState(false);

  async function handleConfirm() {
    setPending(true);
    try {
      await userService.resetPassword(user.id);
      toast.success(`Nova senha provisória enviada para ${user.email}.`);
      onDone();
      onOpenChange(false);
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setPending(false);
    }
  }

  return (
    <Dialog open onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Reenviar acesso</DialogTitle>
          <DialogDescription>
            Uma nova senha provisória será gerada para <strong>{user.name}</strong> e enviada para{" "}
            <strong>{user.email}</strong>. A senha atual deixa de funcionar.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={pending}>
            Cancelar
          </Button>
          <Button type="button" onClick={handleConfirm} disabled={pending}>
            {pending ? "Enviando..." : "Gerar e enviar"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
