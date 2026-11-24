import { api } from "@/lib/api";
import type {
  CourseDetail,
  CourseInput,
  CourseModule,
  CourseSummary,
  Lesson,
  LessonInput,
  Material,
  MaterialInput,
} from "@/types/course";

export const instructorService = {
  // ---------------------------------------------------------------- cursos
  async listCourses(): Promise<CourseSummary[]> {
    const { data } = await api.get<CourseSummary[]>("/instructor/courses");
    return data;
  },

  async getCourse(id: string): Promise<CourseDetail> {
    const { data } = await api.get<CourseDetail>(`/instructor/courses/${id}`);
    return data;
  },

  async createCourse(input: CourseInput): Promise<CourseDetail> {
    const { data } = await api.post<CourseDetail>("/instructor/courses", input);
    return data;
  },

  async updateCourse(id: string, input: CourseInput): Promise<CourseDetail> {
    const { data } = await api.put<CourseDetail>(`/instructor/courses/${id}`, input);
    return data;
  },

  async setPublished(id: string, published: boolean): Promise<CourseDetail> {
    const { data } = await api.patch<CourseDetail>(`/instructor/courses/${id}/publish`, { published });
    return data;
  },

  async deleteCourse(id: string): Promise<void> {
    await api.delete(`/instructor/courses/${id}`);
  },

  // ---------------------------------------------------------------- módulos
  async addModule(courseId: string, title: string): Promise<CourseModule> {
    const { data } = await api.post<CourseModule>(`/instructor/courses/${courseId}/modules`, { title });
    return data;
  },

  async renameModule(moduleId: string, title: string): Promise<CourseModule> {
    const { data } = await api.put<CourseModule>(`/instructor/modules/${moduleId}`, { title });
    return data;
  },

  async deleteModule(moduleId: string): Promise<void> {
    await api.delete(`/instructor/modules/${moduleId}`);
  },

  async reorderModules(courseId: string, ids: string[]): Promise<void> {
    await api.put(`/instructor/courses/${courseId}/modules/order`, { ids });
  },

  // ---------------------------------------------------------------- aulas
  async addLesson(moduleId: string, input: LessonInput): Promise<Lesson> {
    const { data } = await api.post<Lesson>(`/instructor/modules/${moduleId}/lessons`, input);
    return data;
  },

  async updateLesson(lessonId: string, input: LessonInput): Promise<Lesson> {
    const { data } = await api.put<Lesson>(`/instructor/lessons/${lessonId}`, input);
    return data;
  },

  async deleteLesson(lessonId: string): Promise<void> {
    await api.delete(`/instructor/lessons/${lessonId}`);
  },

  async reorderLessons(moduleId: string, ids: string[]): Promise<void> {
    await api.put(`/instructor/modules/${moduleId}/lessons/order`, { ids });
  },

  // ---------------------------------------------------------------- materiais
  async addMaterial(lessonId: string, input: MaterialInput): Promise<Material> {
    const { data } = await api.post<Material>(`/instructor/lessons/${lessonId}/materials`, input);
    return data;
  },

  async deleteMaterial(materialId: string): Promise<void> {
    await api.delete(`/instructor/materials/${materialId}`);
  },
};

/** Move o item `index` uma posição para cima (-1) ou para baixo (+1) e devolve os ids na nova ordem. */
export function moveId<T extends { id: string }>(items: T[], index: number, direction: -1 | 1): string[] {
  const ids = items.map((item) => item.id);
  const target = index + direction;
  if (target < 0 || target >= ids.length) return ids;
  [ids[index], ids[target]] = [ids[target], ids[index]];
  return ids;
}
