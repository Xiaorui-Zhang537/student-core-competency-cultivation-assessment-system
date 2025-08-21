import { api } from './config';
import type { ApiResponse, PaginatedResponse } from '@/types/api';
import type { Grade, GradeRequest } from '@/types/grade';

export const gradeApi = {
  // Main grading endpoint
  gradeSubmission: (data: GradeRequest): Promise<ApiResponse<Grade>> => {
    return api.post('/grades', data);
  },

  // Get specific grade details
  getGradeById: (id: string): Promise<ApiResponse<Grade>> => {
    return api.get(`/grades/${id}`);
  },

  // Get grade history
  getGradeHistory: (id: string): Promise<ApiResponse<any[]>> => {
    return api.get(`/grades/${id}/history`);
  },

  // Get grades by different criteria
  getGradeForStudentAssignment: (studentId: string, assignmentId: string): Promise<ApiResponse<Grade>> => {
    return api.get(`/grades/student/${studentId}/assignment/${assignmentId}`);
  },

  getGradesByStudent: (studentId: string, params?: { page?: number; size?: number; courseId?: string | number }): Promise<ApiResponse<PaginatedResponse<Grade>>> => {
    return api.get(`/grades/student/${studentId}/page`, { params });
  },

  getGradesByAssignment: (assignmentId: string, params?: { page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<Grade>>> => {
    return api.get(`/grades/assignment/${assignmentId}/page`, { params });
  },

  // Publish a grade
  publishGrade: (id: string): Promise<ApiResponse<void>> => {
    return api.post(`/grades/${id}/publish`);
  },

  // Batch operations
  gradeBatchSubmissions: (grades: GradeRequest[]): Promise<ApiResponse<Grade[]>> => {
    return api.post('/grades/batch', grades);
  },

  publishBatchGrades: (gradeIds: string[]): Promise<ApiResponse<any>> => {
      return api.post('/grades/batch-publish', gradeIds);
  }
};
