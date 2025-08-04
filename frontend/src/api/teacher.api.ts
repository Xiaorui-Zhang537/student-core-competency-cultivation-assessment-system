import { api } from './config';
import type { ApiResponse, PaginatedResponse } from '@/types/api';
import type { StudentProgressData, CourseAnalyticsData, AssignmentAnalyticsData, ClassPerformanceData } from '@/types/teacher';

export const teacherApi = {
  getStudentProgress: (params?: { page?: number; size?: number; courseId?: number }): Promise<ApiResponse<PaginatedResponse<StudentProgressData>>> => {
    return api.get('/api/teachers/analytics/student-progress', { params });
  },

  getCourseAnalytics: (courseId: number): Promise<ApiResponse<CourseAnalyticsData>> => {
    return api.get(`/api/teachers/analytics/course/${courseId}`);
  },

  getAssignmentAnalytics: (assignmentId: number): Promise<ApiResponse<AssignmentAnalyticsData>> => {
    return api.get(`/api/teachers/analytics/assignment/${assignmentId}`);
  },

  getClassPerformance: (courseId: number): Promise<ApiResponse<ClassPerformanceData>> => {
    return api.get(`/api/teachers/analytics/class-performance/${courseId}`);
  },
};
