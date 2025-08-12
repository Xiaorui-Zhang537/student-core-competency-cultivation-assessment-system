import { api } from './config';
import type { ApiResponse, PaginatedResponse } from '@/types/api';
import type { StudentProgressData, CourseAnalyticsData, AssignmentAnalyticsData, ClassPerformanceData, CourseStudentPerformanceResponse } from '@/types/teacher';

export const teacherApi = {
  // Note: backend currently has no '/teachers/analytics/dashboard' endpoint
  // getDashboardAnalytics: (): Promise<ApiResponse<DashboardAnalyticsData>> => {
  //   return api.get('/teachers/analytics/dashboard');
  // },

  getStudentProgress: (params?: { page?: number; size?: number; courseId?: string }): Promise<ApiResponse<PaginatedResponse<StudentProgressData>>> => {
    return api.get('/teachers/analytics/student-progress', { params });
  },

  getCourseAnalytics: (courseId: string): Promise<ApiResponse<CourseAnalyticsData>> => {
    return api.get(`/teachers/analytics/course/${courseId}`);
  },

  getAssignmentAnalytics: (assignmentId: string): Promise<ApiResponse<AssignmentAnalyticsData>> => {
    return api.get(`/teachers/analytics/assignment/${assignmentId}`);
  },

  getClassPerformance: (courseId: string): Promise<ApiResponse<ClassPerformanceData>> => {
    return api.get(`/teachers/analytics/class-performance/${courseId}`);
  },

  getCourseStudentPerformance: (
    courseId: string,
    params?: { page?: number; size?: number; search?: string; sortBy?: string; activity?: string; grade?: string; progress?: string }
  ): Promise<ApiResponse<CourseStudentPerformanceResponse>> => {
    return api.get(`/teachers/analytics/course/${courseId}/students`, { params });
  },
  
  exportCourseStudents: (
    courseId: string,
    params?: { search?: string; sortBy?: string; activity?: string; grade?: string; progress?: string }
  ) => {
    return api.get(`/teachers/analytics/course/${courseId}/students/export`, { params, responseType: 'blob' as any });
  },

  // Ability radar & weights
  getAbilityRadar: (params: { courseId: string; classId?: string; studentId?: string; startDate: string; endDate: string }) => {
    return api.get('/teachers/ability/radar', { params });
  },
  getAbilityWeights: (courseId: string) => {
    return api.get('/teachers/ability/weights', { params: { courseId } });
  },
  updateAbilityWeights: (payload: { courseId: string; weights: Record<string, number> }) => {
    return api.put('/teachers/ability/weights', payload);
  },
  exportAbilityRadarCsv: (params: { courseId: string; classId?: string; studentId?: string; startDate: string; endDate: string }) => {
    return api.get('/teachers/ability/radar/export', { params, responseType: 'blob' as any });
  },
};
