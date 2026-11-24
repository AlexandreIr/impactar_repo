"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import { ShieldCheck } from "lucide-react";

import { PublicHeader } from "@/components/public-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

export default function ValidateFormPage() {
  const router = useRouter();
  const [code, setCode] = useState("");

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const value = code.trim();
    if (value) router.push(`/validar/${encodeURIComponent(value)}`);
  }

  return (
    <>
      <PublicHeader />
      <main className="mx-auto max-w-md px-4 py-12">
        <Card>
          <CardHeader>
            <ShieldCheck className="size-8 text-primary" />
            <CardTitle className="text-xl">Validar certificado</CardTitle>
            <CardDescription>Digite o código impresso no rodapé do certificado.</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="code">Código de verificação</Label>
                <Input
                  id="code"
                  placeholder="XXXX-XXXX-XXXX"
                  value={code}
                  onChange={(event) => setCode(event.target.value)}
                  className="font-mono uppercase"
                  autoComplete="off"
                />
              </div>
              <Button type="submit" className="w-full" disabled={!code.trim()}>
                Validar
              </Button>
            </form>
          </CardContent>
        </Card>
      </main>
    </>
  );
}
