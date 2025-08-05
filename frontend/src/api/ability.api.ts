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
  
  // Teacher-facing endpoints can be added here later
  // e.g., getTeacherClassStats, etc.
};
