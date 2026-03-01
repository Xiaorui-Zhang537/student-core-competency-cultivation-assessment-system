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

  // Batch operations
  gradeBatchSubmissions: (grades: GradeRequest[]): Promise<Grade[]> => {
    return api.post('/grades/batch', grades);
  },

  publishBatchGrades: (gradeIds: string[]): Promise<any> => {
      return api.post('/grades/batch-publish', gradeIds);
  },
  // Return for resubmission
  returnForResubmission: (gradeId: string, data: { reason?: string; resubmitUntil?: string }): Promise<void> => {
    return api.post(`/grades/${gradeId}/return`, data);
  }
  ,
  // 课程成绩统计（均值、分布等）
  getCourseGradeStatistics: (courseId: string | number): Promise<any> => {
    return api.get(`/grades/course/${courseId}/statistics`)
  }
  ,
  // 学生成绩趋势
  getGradeTrend: (studentId: string | number, params?: { courseId?: string | number; days?: number }): Promise<any[]> => {
    return api.get(`/grades/student/${studentId}/trend`, { params })
  }
};
