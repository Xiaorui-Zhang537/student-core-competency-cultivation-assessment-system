import { api } from './config';
import type { ApiResponse } from '@/types/api';
import type { CourseAnalyticsData, AssignmentAnalyticsData, ClassPerformanceData, CourseStudentPerformanceResponse } from '@/types/teacher';

export const teacherApi = {
  // Note: backend currently has no '/teachers/analytics/dashboard' endpoint
  // getDashboardAnalytics: (): Promise<ApiResponse<DashboardAnalyticsData>> => {
  //   return api.get('/teachers/analytics/dashboard');
  // },

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
  // 获取课程全部学生（用于雷达图下拉），默认一次取最多10000条
  getAllCourseStudentsBasic: (courseId: string, keyword?: string) => {
    const params: any = { page: 1, size: 10000 };
    if (keyword) params.keyword = keyword;
    return api.get(`/teachers/students/basic`, { params: { ...params, courseId } });
  },
  
  exportCourseStudents: (
    courseId: string,
    params?: { search?: string; sortBy?: string; activity?: string; grade?: string; progress?: string }
  ) => {
    return api.get(`/teachers/analytics/course/${courseId}/students/export`, { params, responseType: 'blob' as any });
  },

  resetStudentCourseProgress: (courseId: string, studentId: string) => {
    return api.post(`/teachers/courses/${courseId}/students/${studentId}/progress/reset`);
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
  // Compare APIs (POST)
  postAbilityRadarCompare: (body: {
    courseId: string; studentId: string; classId?: string;
    startDateA?: string; endDateA?: string; assignmentIdsA?: string[];
    startDateB?: string; endDateB?: string; assignmentIdsB?: string[];
    includeClassAvg?: 'none'|'A'|'B'|'both';
  }) => {
    return api.post('/teachers/ability/radar/compare', body);
  },
  exportAbilityRadarCompareCsv: (body: {
    courseId: string; studentId: string; classId?: string;
    startDateA?: string; endDateA?: string; assignmentIdsA?: string[];
    startDateB?: string; endDateB?: string; assignmentIdsB?: string[];
    includeClassAvg?: 'none'|'A'|'B'|'both';
  }) => {
    return api.post('/teachers/ability/radar/compare/export', body, { responseType: 'blob' as any });
  },
  postAbilityDimensionInsights: (body: {
    courseId: string; studentId: string; classId?: string;
    startDateA?: string; endDateA?: string; assignmentIdsA?: string[];
    startDateB?: string; endDateB?: string; assignmentIdsB?: string[];
    includeClassAvg?: 'none'|'A'|'B'|'both';
  }) => {
    return api.post('/teachers/ability/dimension-insights', body);
  },
};
