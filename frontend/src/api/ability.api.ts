import { api } from './config';
import type { ApiResponse } from '@/types/api';
import type { AbilityDimension, AbilityDashboardData, AbilityTrendData, AbilityRecommendation } from '@/types/ability';

export const abilityApi = {
  // Student-facing endpoints
  getAbilityDimensions: (): Promise<ApiResponse<AbilityDimension[]>> => {
    return api.get('/ability/dimensions');
  },

  getStudentDashboard: (): Promise<ApiResponse<AbilityDashboardData>> => {
    return api.get('/ability/student/dashboard');
  },

  getStudentTrends: (): Promise<ApiResponse<AbilityTrendData>> => {
    return api.get('/ability/student/trends');
  },

  getStudentRecommendations: (): Promise<ApiResponse<AbilityRecommendation[]>> => {
    return api.get('/ability/student/recommendations');
  },
  
  // Student radar (same 5 dimensions as teacher)
  getStudentRadar: (params: { courseId: string | number; classId?: string | number; startDate?: string; endDate?: string }): Promise<ApiResponse<any>> => {
    return api.get('/ability/student/radar', { params });
  },

  // Student radar compare (A/B periods or assignment sets)
  postStudentRadarCompare: (body: any): Promise<ApiResponse<any>> => {
    return api.post('/ability/student/radar/compare', body);
  },

  // Student dimension insights (text analysis)
  postStudentDimensionInsights: (body: any): Promise<ApiResponse<any>> => {
    return api.post('/ability/student/dimension-insights', body);
  },
  // Student latest ability report
  getStudentLatestReport: (): Promise<ApiResponse<any>> => {
    return api.get('/ability/student/report/latest');
  },
  // Student: latest AI report by context (prefers submissionId > assignmentId > courseId)
  getStudentLatestReportByContext: (params: { courseId?: string|number; assignmentId?: string|number; submissionId?: string|number }): Promise<ApiResponse<any>> => {
    return api.get('/ability/student/report/latest-by-context', { params });
  },
  // Teacher query student's latest AI report by context (prefers submissionId > assignmentId > courseId)
  getTeacherLatestReportOfStudent: (params: { studentId: string|number; courseId?: string|number; assignmentId?: string|number; submissionId?: string|number }): Promise<ApiResponse<any>> => {
    return api.get('/ability/teacher/report/latest', { params });
  },
  // Student ability report history (pagination)
  getStudentReportHistory: (params?: { page?: number; size?: number }): Promise<ApiResponse<any>> => {
    return api.get('/ability/student/reports', { params });
  },
  // Teacher create report from AI
  createReportFromAi: (payload: { studentId: string|number; normalizedJson: string; title?: string; courseId?: string|number; assignmentId?: string|number; submissionId?: string|number; aiHistoryId?: string|number }): Promise<ApiResponse<any>> => {
    // 使用 JSON body 避免URL过长与CORS代理异常
    return api.post('/ability/teacher/report/from-ai', payload)
  },
  
  // Teacher-facing: record an ability assessment for a student
  recordTeacherAssessment: (payload: { studentId: string | number; dimensionId: string | number; score: number; assessmentType: string; relatedId?: string | number; evidence?: string }): Promise<ApiResponse<void>> => {
    const params = new URLSearchParams()
    params.set('studentId', String(payload.studentId))
    params.set('dimensionId', String(payload.dimensionId))
    params.set('score', String(payload.score))
    params.set('assessmentType', String(payload.assessmentType))
    if (payload.relatedId != null) params.set('relatedId', String(payload.relatedId))
    if (payload.evidence != null) params.set('evidence', String(payload.evidence))
    return api.post('/ability/teacher/assessment', params)
  },
};
