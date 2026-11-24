import axios from "axios";

import { LOGIN_PATH } from "@/lib/auth";

/** Cliente HTTP para componentes client-side. /api é repassado ao backend pelo next.config.ts. */
export const api = axios.create({
  baseURL: "/api",
  withCredentials: true,
  headers: { "Content-Type": "application/json" },
});

const AUTH_ENDPOINTS = ["/auth/login", "/auth/logout"];

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const status: number | undefined = error?.response?.status;
    const url: string = error?.config?.url ?? "";
    const isAuthEndpoint = AUTH_ENDPOINTS.some((endpoint) => url.includes(endpoint));

    // Sessão expirada/inválida: limpa o cookie e volta ao login.
    if (
      typeof window !== "undefined" &&
      status === 401 &&
      !isAuthEndpoint &&
      window.location.pathname !== LOGIN_PATH
    ) {
      await api.post("/auth/logout").catch(() => undefined);
      window.location.assign(LOGIN_PATH);
    }

    return Promise.reject(error);
  },
);

/** Extrai a mensagem amigável (campo "detail" do ProblemDetail do backend). */
export function getErrorMessage(error: unknown, fallback = "Erro inesperado. Tente novamente."): string {
  if (axios.isAxiosError(error)) {
    if (!error.response) return "Não foi possível conectar ao servidor.";
    const data = error.response.data as { detail?: string } | undefined;
    if (data?.detail) return data.detail;
  }
  return fallback;
}
