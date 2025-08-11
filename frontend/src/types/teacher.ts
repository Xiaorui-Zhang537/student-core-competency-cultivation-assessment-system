// Based on DTOs for TeacherController analytics endpoints

export interface StudentProgressData {
  studentId: number;
  studentName: string;
  completedCourses: number;
  averageScore: number;
  progressPercentage: number;
}

export interface CourseAnalyticsData {
  courseId: number;
  courseTitle: string;
  enrollmentCount: number;
  averageCompletionRate: number;
  averageScore: number;
  assignmentCount: number;
}

export interface AssignmentAnalyticsData {
  assignmentId: number;
  assignmentTitle: string;
  submissionCount: number;
  averageScore: number;
  submissionRate: number;
}

export interface ClassPerformanceData {
    courseId: number;
    courseTitle: string;
    studentPerformance: {
        studentId: number;
        studentName: string;
        score: number;
    }[];
    scoreDistribution: {
        range: string; // e.g., "90-100"
        count: number;
    }[];
}

export interface CourseStudentPerformanceItem {
  studentId: number;
  studentName: string;
  studentNo: string;
  avatar?: string;
  progress?: number;
  completedLessons?: number;
  totalLessons?: number;
  averageGrade?: number;
  activityLevel?: string;
  studyTimePerWeek?: number;
  lastActiveAt?: string;
}

export interface CourseStudentPerformanceResponse {
  courseId: number;
  courseTitle: string;
  total: number;
  page: number;
  size: number;
  items: CourseStudentPerformanceItem[];
}
