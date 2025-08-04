import { api } from './config';
import type { ApiResponse, PaginatedResponse } from '@/types/api';
import type { StudentDashboardData } from '@/types/student';
import type { Course } from '@/types/course';
import type { Assignment } from '@/types/assignment';

export const studentApi = {
  getDashboard: (): Promise<ApiResponse<StudentDashboardData>> => {
    return api.get('/api/students/dashboard');
  },

  getMyCourses: (params?: { page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<Course>>> => {
    return api.get('/api/students/my/courses', { params });
  },

  getMyAssignments: (params?: { page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<Assignment>>> => {
    return api.get('/api/students/my/assignments', { params });
  },

  getMyPendingAssignments: (): Promise<ApiResponse<Assignment[]>> => {
    return api.get('/api/students/my/assignments/pending');
  },
};
