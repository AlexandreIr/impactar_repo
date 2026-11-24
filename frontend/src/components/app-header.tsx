"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";

import { LogoutButton } from "@/components/logout-button";
import { NAV_BY_ROLE } from "@/lib/roles";
import { cn } from "@/lib/utils";
import { authService } from "@/services/auth-service";
import type { Me } from "@/types/auth";

export function AppHeader({ title }: { title: string }) {
  const pathname = usePathname();
  const [me, setMe] = useState<Me | null>(null);

  useEffect(() => {
    authService.me().then(setMe).catch(() => undefined);
  }, []);

  const navItems = me ? NAV_BY_ROLE[me.role] : [];

  return (
    <header className="border-b bg-background">
      <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-x-6 gap-y-2 px-4 py-3">
        <div className="flex flex-wrap items-center gap-x-6 gap-y-2">
          <div>
            <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">impactar</p>
            <h1 className="text-lg font-semibold">{title}</h1>
          </div>
          {navItems.length > 1 && (
            <nav className="flex gap-1">
              {navItems.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className={cn(
                    "rounded-md px-3 py-1.5 text-sm text-muted-foreground transition-colors hover:bg-muted hover:text-foreground",
                    pathname.startsWith(item.href) && "bg-muted font-medium text-foreground",
                  )}
                >
                  {item.label}
                </Link>
              ))}
            </nav>
          )}
        </div>
        <div className="flex items-center gap-3">
          {me && <span className="hidden text-sm text-muted-foreground sm:inline">{me.name}</span>}
          <LogoutButton />
        </div>
      </div>
    </header>
  );
}
