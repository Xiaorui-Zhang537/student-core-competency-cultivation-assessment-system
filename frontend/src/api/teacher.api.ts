import { api } from './config';
import type { ApiResponse, PaginatedResponse } from '@/types/api';
import type { StudentProgressData, CourseAnalyticsData, AssignmentAnalyticsData, ClassPerformanceData } from '@/types/teacher';

export const teacherApi = {
  getStudentProgress: (params?: { page?: number; size?: number; courseId?: string }): Promise<ApiResponse<PaginatedResponse<StudentProgressData>>> => {
    return api.get('/api/teachers/analytics/student-progress', { params });
  },

  getCourseAnalytics: (courseId: string): Promise<ApiResponse<CourseAnalyticsData>> => {
    return api.get(`/api/teachers/analytics/course/${courseId}`);
  },

  getAssignmentAnalytics: (assignmentId: string): Promise<ApiResponse<AssignmentAnalyticsData>> => {
    return api.get(`/api/teachers/analytics/assignment/${assignmentId}`);
  },

  getClassPerformance: (courseId: string): Promise<ApiResponse<ClassPerformanceData>> => {
    return api.get(`/api/teachers/analytics/class-performance/${courseId}`);
  },
};
