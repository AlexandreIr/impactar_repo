import { api } from "@/lib/api";
import type { TrailAdminDetail, TrailAdminSummary, TrailInput } from "@/types/trail";

export const trailAdminService = {
  async list(): Promise<TrailAdminSummary[]> {
    const { data } = await api.get<TrailAdminSummary[]>("/admin/trails");
    return data;
  },

  async get(id: string): Promise<TrailAdminDetail> {
    const { data } = await api.get<TrailAdminDetail>(`/admin/trails/${id}`);
    return data;
  },

  async create(input: TrailInput): Promise<TrailAdminDetail> {
    const { data } = await api.post<TrailAdminDetail>("/admin/trails", input);
    return data;
  },

  async update(id: string, input: TrailInput): Promise<TrailAdminDetail> {
    const { data } = await api.put<TrailAdminDetail>(`/admin/trails/${id}`, input);
    return data;
  },

  async setPublished(id: string, published: boolean): Promise<TrailAdminDetail> {
    const { data } = await api.patch<TrailAdminDetail>(`/admin/trails/${id}/publish`, { published });
    return data;
  },

  async remove(id: string): Promise<void> {
    await api.delete(`/admin/trails/${id}`);
  },

  /** Lista completa de cursos, na ordem desejada. */
  async setCourses(id: string, courseIds: string[]): Promise<TrailAdminDetail> {
    const { data } = await api.put<TrailAdminDetail>(`/admin/trails/${id}/courses`, { courseIds });
    return data;
  },
};
