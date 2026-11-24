export type Role = "ADMIN" | "INSTRUCTOR" | "STUDENT";

export interface Me {
  id: string;
  name: string;
  email: string;
  role: Role;
  mustChangePassword: boolean;
}

/** Claims do JWT emitido pelo backend. */
export interface SessionPayload {
  sub: string;
  role: Role;
  name: string;
  mcp: boolean;
  exp: number;
}
