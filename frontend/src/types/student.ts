import type { Course } from './course';
import type { Assignment } from './assignment';

// Represents a course from the perspective of an enrolled student
export interface StudentCourse {
  id: string; // Course ID
  title: string;
  description: string;
  instructorName: string;
  category: string;
  coverImageUrl?: string;
  progress: number; // Student's progress in this course
  enrolledAt: string;
}

// Based on data returned from /api/students/dashboard
export interface StudentDashboardData {
  upcomingAssignments: Assignment[];
  activeCourses: Course[]; // This might be a summary, StudentCourse provides more detail
  recentGrades: {
    assignmentTitle: string;
    courseTitle: string;
    score: number;
  }[];
  overallProgress: number;
}
