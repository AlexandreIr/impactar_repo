"use client";

import { useState, type ReactNode } from "react";
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

export interface ConfirmDialogProps {
  title: string;
  description: ReactNode;
  confirmLabel?: string;
  destructive?: boolean;
  onConfirm: () => Promise<void>;
  onOpenChange: (open: boolean) => void;
}

/** Diálogo de confirmação; fecha sozinho quando onConfirm termina sem erro. */
export function ConfirmDialog({
  title,
  description,
  confirmLabel = "Confirmar",
  destructive = false,
  onConfirm,
  onOpenChange,
}: ConfirmDialogProps) {
  const [pending, setPending] = useState(false);

  async function handleConfirm() {
    setPending(true);
    try {
      await onConfirm();
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
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>{description}</DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={pending}>
            Cancelar
          </Button>
          <Button
            type="button"
            variant={destructive ? "destructive" : "default"}
            onClick={handleConfirm}
            disabled={pending}
          >
            {pending ? "Aguarde..." : confirmLabel}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
