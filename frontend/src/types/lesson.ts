export interface Lesson {
  id: string;
  title: string;
  description: string;
  courseId: string;
  order?: number;
  orderIndex?: number;
  videoUrl?: string;
  content?: string;
  allowScrubbing?: boolean;
  allowSpeedChange?: boolean;
  isPublished: boolean;
  createdAt: string;
  updatedAt: string;
}

// Based on student-specific progress DTOs if available,
// or combined with Lesson for student views.
export interface StudentLesson extends Lesson {
  progress: number; // 0-100
  isCompleted: boolean;
}
