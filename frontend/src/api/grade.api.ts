import { api } from './config';
import type { ApiResponse, PaginatedResponse } from '@/types/api';
import type { Grade, GradeRequest } from '@/types/grade';

export const gradeApi = {
  // Main grading endpoint
  gradeSubmission: (data: GradeRequest): Promise<ApiResponse<Grade>> => {
    return api.post('/api/grades', data);
  },

  // Get specific grade details
  getGradeById: (id: number): Promise<ApiResponse<Grade>> => {
    return api.get(`/api/grades/${id}`);
  },

  // Get grades by different criteria
  getGradeForStudentAssignment: (studentId: number, assignmentId: number): Promise<ApiResponse<Grade>> => {
    return api.get(`/api/grades/student/${studentId}/assignment/${assignmentId}`);
  },

  getGradesByStudent: (studentId: number, params?: { page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<Grade>>> => {
    return api.get(`/api/grades/student/${studentId}/page`, { params });
  },

  getGradesByAssignment: (assignmentId: number, params?: { page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<Grade>>> => {
    return api.get(`/api/grades/assignment/${assignmentId}/page`, { params });
  },

  // Publish a grade
  publishGrade: (id: number): Promise<ApiResponse<void>> => {
    return api.post(`/api/grades/${id}/publish`);
  },

  // Batch operations
  gradeBatchSubmissions: (grades: GradeRequest[]): Promise<ApiResponse<Grade[]>> => {
    return api.post('/api/grades/batch', grades);
  },
  
  publishBatchGrades: (gradeIds: number[]): Promise<ApiResponse<void>> => {
      return api.post('/api/grades/batch-publish', { gradeIds });
  }
};
