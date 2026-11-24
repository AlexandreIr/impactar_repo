import type { Material } from "@/types/course";

export interface ProgressInfo {
  completedLessons: number;
  totalLessons: number;
  percent: number;
}

export interface CourseCard {
  id: string;
  title: string;
  description: string | null;
  thumbnailUrl: string | null;
  workloadHours: number | null;
  instructorName: string;
  enrolled: boolean;
  progress: ProgressInfo;
}

export interface TrailCard {
  id: string;
  title: string;
  description: string | null;
  thumbnailUrl: string | null;
  courseCount: number;
  workloadHours: number;
  enrolled: boolean;
  progress: ProgressInfo;
}

export interface ContinueLearning {
  courseId: string;
  courseTitle: string;
  thumbnailUrl: string | null;
  lessonId: string;
  lessonTitle: string;
  progress: ProgressInfo;
}

export interface LearningHome {
  continueLearning: ContinueLearning | null;
  myTrails: TrailCard[];
  myCourses: CourseCard[];
  availableTrails: TrailCard[];
  availableCourses: CourseCard[];
}

export interface OutlineLesson {
  id: string;
  title: string;
  hasVideo: boolean;
  completed: boolean;
}

export interface OutlineModule {
  id: string;
  title: string;
  lessons: OutlineLesson[];
}

export interface StudentCourse {
  id: string;
  title: string;
  description: string | null;
  thumbnailUrl: string | null;
  workloadHours: number | null;
  instructorName: string;
  enrolled: boolean;
  progress: ProgressInfo;
  resumeLessonId: string | null;
  modules: OutlineModule[];
}

export interface LessonPlayer {
  id: string;
  title: string;
  description: string | null;
  videoId: string | null;
  materials: Material[];
  completed: boolean;
  lastPositionSeconds: number;
  courseId: string;
  courseTitle: string;
  moduleTitle: string;
  previousLessonId: string | null;
  nextLessonId: string | null;
}

export interface Completion {
  completed: boolean;
  courseProgress: ProgressInfo;
  nextLessonId: string | null;
  certificateIssued: boolean;
}

export interface StudentTrail {
  id: string;
  title: string;
  description: string | null;
  thumbnailUrl: string | null;
  workloadHours: number;
  enrolled: boolean;
  progress: ProgressInfo;
  courses: CourseCard[];
}
