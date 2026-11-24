export interface TrailAdminSummary {
  id: string;
  title: string;
  thumbnailUrl: string | null;
  published: boolean;
  courseCount: number;
  updatedAt: string;
}

export interface TrailCourseItem {
  courseId: string;
  title: string;
  published: boolean;
  workloadHours: number | null;
  instructorName: string;
}

export interface TrailAdminDetail {
  id: string;
  title: string;
  description: string | null;
  thumbnailUrl: string | null;
  published: boolean;
  courses: TrailCourseItem[];
}

export interface TrailInput {
  title: string;
  description: string;
  thumbnailUrl: string;
}
