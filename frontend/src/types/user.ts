import type { Role } from "@/types/auth";

export interface UserSummary {
  id: string;
  name: string;
  email: string;
  role: Role;
  active: boolean;
  mustChangePassword: boolean;
  createdAt: string;
}

export interface CreateUserInput {
  name: string;
  email: string;
  role: Role;
}

export interface UpdateUserInput {
  name: string;
  role: Role;
  active: boolean;
}
