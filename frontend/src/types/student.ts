import type { Course } from './course';
import type { Assignment } from './assignment';

// Represents a course from the perspective of an enrolled student
export interface StudentCourse {
  id: string; // Course ID
  title: string;
  description: string;
  teacherName: string;
  category: string;
  coverImageUrl?: string;
  progress: number; // Student's progress in this course
  enrolledAt: string;
}

// Based on data returned from /api/students/dashboard
export interface StudentDashboardData {
  upcomingAssignments: Array<{ id: string; title: string; courseTitle: string; dueDate: string }>;
  activeCourses: Array<{ id: string; title: string; teacherName: string; progress: number; coverImage?: string }>;
  recentGrades: Array<{ assignmentId?: string | number; assignmentTitle: string; courseTitle: string; score: number; gradedAt?: string }>;
  overallProgress: number;
  stats: StudentDashboardStats;
}

export interface StudentDashboardStats {
  activeCourses: number;
  pendingAssignments: number;
  averageScore: number;
  totalStudyTime?: number; // minutes
  weeklyStudyTime: number; // minutes
  unreadNotifications?: number;
}
