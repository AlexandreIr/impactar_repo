"use client";

import { useState, type FormEvent } from "react";
import { ExternalLink, Trash2 } from "lucide-react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { getErrorMessage } from "@/lib/api";
import { instructorService } from "@/services/instructor-service";
import type { Material } from "@/types/course";

interface MaterialsEditorProps {
  lessonId: string;
  initialMaterials: Material[];
  onChanged: () => void;
}

/** Materiais complementares (links do Google Drive). Salva cada alteração na hora. */
export function MaterialsEditor({ lessonId, initialMaterials, onChanged }: MaterialsEditorProps) {
  const [materials, setMaterials] = useState<Material[]>(initialMaterials);
  const [title, setTitle] = useState("");
  const [url, setUrl] = useState("");
  const [pending, setPending] = useState(false);

  async function handleAdd(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!title.trim() || !/^https?:\/\//i.test(url.trim())) {
      toast.error("Informe o nome e um link http(s) para o material.");
      return;
    }
    setPending(true);
    try {
      const created = await instructorService.addMaterial(lessonId, { title: title.trim(), url: url.trim() });
      setMaterials((current) => [...current, created]);
      setTitle("");
      setUrl("");
      onChanged();
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setPending(false);
    }
  }

  async function handleDelete(material: Material) {
    setPending(true);
    try {
      await instructorService.deleteMaterial(material.id);
      setMaterials((current) => current.filter((item) => item.id !== material.id));
      onChanged();
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setPending(false);
    }
  }

  return (
    <div className="space-y-3">
      <div>
        <h3 className="text-sm font-medium">Materiais complementares</h3>
        <p className="text-xs text-muted-foreground">
          Cole o link de compartilhamento do Google Drive (acesso &quot;qualquer pessoa com o link&quot;).
        </p>
      </div>

      {materials.length > 0 && (
        <ul className="divide-y rounded-md border">
          {materials.map((material) => (
            <li key={material.id} className="flex items-center justify-between gap-2 px-3 py-2 text-sm">
              <a
                href={material.url}
                target="_blank"
                rel="noopener noreferrer"
                className="flex min-w-0 items-center gap-1.5 hover:underline"
              >
                <ExternalLink className="size-3.5 shrink-0" />
                <span className="truncate">{material.title}</span>
              </a>
              <Button
                type="button"
                size="icon"
                variant="ghost"
                aria-label={`Remover ${material.title}`}
                disabled={pending}
                onClick={() => handleDelete(material)}
              >
                <Trash2 className="size-4" />
              </Button>
            </li>
          ))}
        </ul>
      )}

      <form onSubmit={handleAdd} className="grid gap-2 sm:grid-cols-[1fr_1.5fr_auto] sm:items-end">
        <div className="space-y-1">
          <Label htmlFor="material-title" className="text-xs">
            Nome
          </Label>
          <Input id="material-title" value={title} onChange={(event) => setTitle(event.target.value)} />
        </div>
        <div className="space-y-1">
          <Label htmlFor="material-url" className="text-xs">
            Link
          </Label>
          <Input
            id="material-url"
            placeholder="https://drive.google.com/..."
            value={url}
            onChange={(event) => setUrl(event.target.value)}
          />
        </div>
        <Button type="submit" variant="outline" disabled={pending}>
          Adicionar
        </Button>
      </form>
    </div>
  );
}
