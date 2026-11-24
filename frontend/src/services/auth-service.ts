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

  async updateProfile(name: string): Promise<Me> {
    const { data } = await api.put<Me>("/auth/profile", { name });
    return data;
  },

  /** Sempre resolve (o backend não revela se o e-mail existe). */
  async forgotPassword(email: string): Promise<void> {
    await api.post("/auth/forgot-password", { email });
  },

  async validateResetToken(token: string): Promise<void> {
    await api.post("/auth/reset-password/validate", { token });
  },

  async resetPassword(token: string, newPassword: string): Promise<void> {
    await api.post("/auth/reset-password", { token, newPassword });
  },
};
