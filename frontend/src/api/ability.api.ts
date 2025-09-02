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
  
  // Teacher-facing endpoints can be added here later
  // e.g., getTeacherClassStats, etc.
};
