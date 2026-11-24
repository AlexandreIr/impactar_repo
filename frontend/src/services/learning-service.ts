import { api } from "@/lib/api";
import type { Completion, LearningHome, LessonPlayer, StudentCourse, StudentTrail } from "@/types/learning";

export const learningService = {
  async home(): Promise<LearningHome> {
    const { data } = await api.get<LearningHome>("/learning/home");
    return data;
  },

  async course(courseId: string): Promise<StudentCourse> {
    const { data } = await api.get<StudentCourse>(`/learning/courses/${courseId}`);
    return data;
  },

  async enroll(courseId: string): Promise<StudentCourse> {
    const { data } = await api.post<StudentCourse>(`/learning/courses/${courseId}/enroll`);
    return data;
  },

  async lesson(lessonId: string): Promise<LessonPlayer> {
    const { data } = await api.get<LessonPlayer>(`/learning/lessons/${lessonId}`);
    return data;
  },

  async savePosition(lessonId: string, positionSeconds: number): Promise<void> {
    await api.put(`/learning/lessons/${lessonId}/position`, { positionSeconds });
  },

  async complete(lessonId: string): Promise<Completion> {
    const { data } = await api.post<Completion>(`/learning/lessons/${lessonId}/complete`);
    return data;
  },

  async uncomplete(lessonId: string): Promise<Completion> {
    const { data } = await api.delete<Completion>(`/learning/lessons/${lessonId}/complete`);
    return data;
  },

  async trail(trailId: string): Promise<StudentTrail> {
    const { data } = await api.get<StudentTrail>(`/learning/trails/${trailId}`);
    return data;
  },

  async enrollTrail(trailId: string): Promise<StudentTrail> {
    const { data } = await api.post<StudentTrail>(`/learning/trails/${trailId}/enroll`);
    return data;
  },
};
