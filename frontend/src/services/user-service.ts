import { api } from "@/lib/api";
import type { PageResponse } from "@/types/api";
import type { CreateUserInput, UpdateUserInput, UserSummary } from "@/types/user";

export const userService = {
  async list(params: { search?: string; page?: number; size?: number }): Promise<PageResponse<UserSummary>> {
    const { data } = await api.get<PageResponse<UserSummary>>("/admin/users", { params });
    return data;
  },

  async create(input: CreateUserInput): Promise<UserSummary> {
    const { data } = await api.post<UserSummary>("/admin/users", input);
    return data;
  },

  async update(id: string, input: UpdateUserInput): Promise<UserSummary> {
    const { data } = await api.put<UserSummary>(`/admin/users/${id}`, input);
    return data;
  },

  async resetPassword(id: string): Promise<void> {
    await api.post(`/admin/users/${id}/reset-password`);
  },
};
