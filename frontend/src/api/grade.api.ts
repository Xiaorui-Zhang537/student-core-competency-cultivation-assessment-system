import { api } from './config';
import type { PaginatedResponse } from '@/types/api';
import type { Grade, GradeRequest } from '@/types/grade';

export const gradeApi = {
  // Main grading endpoint
  gradeSubmission: (data: GradeRequest): Promise<Grade> => {
    return api.post('/grades', data);
  },

  // Get specific grade details
  getGradeById: (id: string): Promise<Grade> => {
    return api.get(`/grades/${id}`);
  },

  // Get grade history
  getGradeHistory: (id: string): Promise<any[]> => {
    return api.get(`/grades/${id}/history`);
  },

  // Get grades by different criteria
  getGradeForStudentAssignment: (studentId: string, assignmentId: string): Promise<Grade> => {
    return api.get(`/grades/student/${studentId}/assignment/${assignmentId}`);
  },

  getGradesByStudent: (studentId: string, params?: { page?: number; size?: number; courseId?: string | number }): Promise<PaginatedResponse<Grade>> => {
    return api.get(`/grades/student/${studentId}/page`, { params });
  },

  getGradesByAssignment: (assignmentId: string, params?: { page?: number; size?: number }): Promise<PaginatedResponse<Grade>> => {
    return api.get(`/grades/assignment/${assignmentId}/page`, { params });
  },

  // Publish a grade
  publishGrade: (id: string): Promise<void> => {
    return api.post(`/grades/${id}/publish`);
  },

  // Return for resubmission
  returnForResubmission: (gradeId: string, data: { reason?: string; resubmitUntil?: string }): Promise<void> => {
    return api.post(`/grades/${gradeId}/return`, data);
  }
};
