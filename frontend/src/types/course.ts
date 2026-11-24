export interface CourseSummary {
  id: string;
  title: string;
  thumbnailUrl: string | null;
  published: boolean;
  workloadHours: number | null;
  instructorName: string;
  updatedAt: string;
}

export interface Material {
  id: string;
  title: string;
  url: string;
  orderIndex: number;
}

export interface Lesson {
  id: string;
  moduleId: string;
  title: string;
  description: string | null;
  videoUrl: string | null;
  videoId: string | null;
  orderIndex: number;
  materials: Material[];
}

export interface CourseModule {
  id: string;
  title: string;
  orderIndex: number;
  lessons: Lesson[];
}

export interface CourseDetail {
  id: string;
  title: string;
  description: string | null;
  thumbnailUrl: string | null;
  published: boolean;
  workloadHours: number | null;
  instructorId: string;
  instructorName: string;
  updatedAt: string;
  modules: CourseModule[];
}

export interface CourseInput {
  title: string;
  description: string;
  thumbnailUrl: string;
  workloadHours: number;
}

export interface LessonInput {
  title: string;
  description: string;
  videoUrl: string;
}

export interface MaterialInput {
  title: string;
  url: string;
}
