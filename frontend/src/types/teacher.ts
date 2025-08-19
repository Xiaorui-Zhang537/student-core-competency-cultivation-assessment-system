// Based on DTOs for TeacherController analytics endpoints

export interface StudentProgressData {
  studentId: number;
  studentName: string;
  completedCourses: number;
  averageScore: number;
  progressPercentage: number;
}

export interface CourseAnalyticsData {
  // 兼容后端 CourseAnalyticsResponse
  courseId?: number;
  courseTitle?: string;
  enrollmentCount?: number; // 旧字段，保留
  averageCompletionRate?: number; // 旧字段，保留
  averageScore?: number;
  assignmentCount?: number; // 旧字段，保留
  // 新增与服务端对齐字段
  totalStudents?: number;
  activeStudents?: number;
  totalAssignments?: number;
  completionRate?: number;
  timeSeriesData?: any[];
}

export interface AssignmentAnalyticsData {
  assignmentId: number;
  assignmentTitle: string;
  submissionCount: number;
  averageScore: number;
  submissionRate: number;
}

export interface ClassPerformanceData {
    courseId?: number;
    courseTitle?: string;
    // 兼容后端 ClassPerformanceResponse
    totalStudents?: number;
    gradeStats?: Record<string, any> | null;
    activityStats?: Record<string, any> | null;
    // 饼图数据（后端将提供 gradeDistribution）
    gradeDistribution?: { gradeLevel: string; count: number; percentage: number }[];
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
  joinedAt?: string;
}

export interface CourseStudentPerformanceResponse {
  courseId: number;
  courseTitle: string;
  total: number;
  page: number;
  size: number;
  items: CourseStudentPerformanceItem[];
  averageProgress?: number;
  averageGrade?: number;
  activeStudents?: number;
  passRate?: number;
}
