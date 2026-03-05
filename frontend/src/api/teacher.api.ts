import { api } from './config';
import type { CourseAnalyticsData, ClassPerformanceData, CourseStudentPerformanceResponse } from '@/types/teacher';

export const teacherApi = {
  // Note: backend currently has no '/teachers/analytics/dashboard' endpoint
  // getDashboardAnalytics: (): Promise<ApiResponse<DashboardAnalyticsData>> => {
  //   return api.get('/teachers/analytics/dashboard');
  // },

  getCourseAnalytics: (courseId: string): Promise<CourseAnalyticsData> => {
    return api.get(`/teachers/analytics/course/${courseId}`);
  },

  getClassPerformance: (courseId: string): Promise<ClassPerformanceData> => {
    return api.get(`/teachers/analytics/class-performance/${courseId}`);
  },

  getCourseStudentPerformance: (
    courseId: string,
    params?: { page?: number; size?: number; search?: string; sortBy?: string; activity?: string; grade?: string; progress?: string }
  ): Promise<CourseStudentPerformanceResponse> => {
    return api.get(`/teachers/analytics/course/${courseId}/students`, { params });
  },
  exportCourseStudents: (
    courseId: string,
    params?: { search?: string; sortBy?: string; activity?: string; grade?: string; progress?: string }
  ): Promise<Blob> => {
    return api.get(`/teachers/analytics/course/${courseId}/students/export`, { params, responseType: 'blob' as any });
  },

  resetStudentCourseProgress: (courseId: string, studentId: string): Promise<void> => {
    return api.post(`/teachers/courses/${courseId}/students/${studentId}/progress/reset`);
  },

  // 教师：我的课程
  getMyCourses: (): Promise<any[]> => {
    return api.get('/teachers/my-courses');
  },

  // 教师：联系人聚合（可选，用于一次性拉全通讯录）
  getContacts: (params?: { keyword?: string }): Promise<any> => {
    return api.get('/teachers/contacts', { params });
  },

  // Ability radar & weights
  getAbilityRadar: (params: { courseId: string; classId?: string; studentId?: string; startDate?: string; endDate?: string }): Promise<any> => {
    return api.get('/teachers/ability/radar', { params });
  },
  exportAbilityRadarCsv: (params: { courseId: string; classId?: string; studentId?: string; startDate?: string; endDate?: string; scope?: 'single' | 'all' }): Promise<Blob> => {
    return api.get('/teachers/ability/radar/export', { params, responseType: 'blob' as any });
  },
  // Compare APIs (POST)
  postAbilityRadarCompare: (body: {
    courseId: string; studentId?: string; classId?: string;
    startDateA?: string; endDateA?: string; assignmentIdsA?: string[];
    startDateB?: string; endDateB?: string; assignmentIdsB?: string[];
    includeClassAvg?: 'none'|'A'|'B'|'both';
  }): Promise<any> => {
    return api.post('/teachers/ability/radar/compare', body);
  },
  exportAbilityRadarCompareCsv: (body: {
    courseId: string; studentId?: string; classId?: string;
    startDateA?: string; endDateA?: string; assignmentIdsA?: string[];
    startDateB?: string; endDateB?: string; assignmentIdsB?: string[];
    includeClassAvg?: 'none'|'A'|'B'|'both';
  }, scope: 'single' | 'all' = 'single'): Promise<Blob> => {
    return api.post('/teachers/ability/radar/compare/export', body, { params: { scope }, responseType: 'blob' as any });
  },
  postAbilityDimensionInsights: (body: {
    courseId: string; studentId: string; classId?: string;
    startDateA?: string; endDateA?: string; assignmentIdsA?: string[];
    startDateB?: string; endDateB?: string; assignmentIdsB?: string[];
    includeClassAvg?: 'none'|'A'|'B'|'both';
  }): Promise<any> => {
    return api.post('/teachers/ability/dimension-insights', body);
  },
};
