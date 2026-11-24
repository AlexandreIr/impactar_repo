import type { Role } from "@/types/auth";

export const ROLES: Role[] = ["STUDENT", "INSTRUCTOR", "ADMIN"];

export const ROLE_LABELS: Record<Role, string> = {
  ADMIN: "Administrador",
  INSTRUCTOR: "Instrutor",
  STUDENT: "Aluno",
};

export interface NavItem {
  href: string;
  label: string;
}

/** Menu do cabeçalho por perfil. */
export const NAV_BY_ROLE: Record<Role, NavItem[]> = {
  ADMIN: [
    { href: "/admin/usuarios", label: "Usuários" },
    { href: "/instrutor", label: "Cursos" },
    { href: "/cursos", label: "Área do aluno" },
  ],
  INSTRUCTOR: [
    { href: "/instrutor", label: "Meus cursos" },
    { href: "/cursos", label: "Área do aluno" },
  ],
  STUDENT: [{ href: "/cursos", label: "Cursos" }],
};
