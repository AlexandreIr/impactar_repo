"use client";

import { useState } from "react";

import { Button } from "@/components/ui/button";
import { LOGIN_PATH } from "@/lib/auth";
import { authService } from "@/services/auth-service";

export function LogoutButton() {
  const [pending, setPending] = useState(false);

  async function handleLogout() {
    setPending(true);
    try {
      await authService.logout();
    } finally {
      window.location.assign(LOGIN_PATH);
    }
  }

  return (
    <Button variant="outline" size="sm" onClick={handleLogout} disabled={pending}>
      {pending ? "Saindo..." : "Sair"}
    </Button>
  );
}
