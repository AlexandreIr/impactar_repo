import { api } from "@/lib/api";
import type { Me } from "@/types/auth";

export const authService = {
  async login(email: string, password: string): Promise<Me> {
    const { data } = await api.post<Me>("/auth/login", { email, password });
    return data;
  },

  async logout(): Promise<void> {
    await api.post("/auth/logout");
  },

  async me(): Promise<Me> {
    const { data } = await api.get<Me>("/auth/me");
    return data;
  },

  async changePassword(currentPassword: string, newPassword: string): Promise<Me> {
    const { data } = await api.post<Me>("/auth/change-password", { currentPassword, newPassword });
    return data;
  },
};
