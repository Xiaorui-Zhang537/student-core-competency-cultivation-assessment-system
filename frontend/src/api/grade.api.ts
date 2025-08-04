import { api } from './config';
import type { ApiResponse, PaginatedResponse } from '@/types/api';
import type { Grade, GradeRequest } from '@/types/grade';

export const gradeApi = {
  // Main grading endpoint
  gradeSubmission: (data: GradeRequest): Promise<ApiResponse<Grade>> => {
    return api.post('/api/grades', data);
  },

  // Get specific grade details
  getGradeById: (id: string): Promise<ApiResponse<Grade>> => {
    return api.get(`/api/grades/${id}`);
  },

  // Get grades by different criteria
  getGradeForStudentAssignment: (studentId: string, assignmentId: string): Promise<ApiResponse<Grade>> => {
    return api.get(`/api/grades/student/${studentId}/assignment/${assignmentId}`);
  },

  getGradesByStudent: (studentId: string, params?: { page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<Grade>>> => {
    return api.get(`/api/grades/student/${studentId}/page`, { params });
  },

  getGradesByAssignment: (assignmentId: string, params?: { page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<Grade>>> => {
    return api.get(`/api/grades/assignment/${assignmentId}/page`, { params });
  },

  // Publish a grade
  publishGrade: (id: string): Promise<ApiResponse<void>> => {
    return api.post(`/api/grades/${id}/publish`);
  },

  // Batch operations
  gradeBatchSubmissions: (grades: GradeRequest[]): Promise<ApiResponse<Grade[]>> => {
    return api.post('/api/grades/batch', grades);
  },
  
  publishBatchGrades: (gradeIds: string[]): Promise<ApiResponse<void>> => {
      return api.post('/api/grades/batch-publish', { gradeIds });
  }
};
