import { z } from "zod";

/** Regras da nova senha (iguais às do backend: 8 a 72 caracteres). */
export const newPasswordField = z
  .string()
  .min(8, "A nova senha deve ter pelo menos 8 caracteres.")
  .max(72, "A nova senha deve ter no máximo 72 caracteres.");
