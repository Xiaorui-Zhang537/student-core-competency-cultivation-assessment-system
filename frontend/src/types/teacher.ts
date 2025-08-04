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
