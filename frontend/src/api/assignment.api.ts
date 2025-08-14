import { api } from './config';
import type { ApiResponse, PaginatedResponse } from '@/types/api';
import type { Assignment, AssignmentCreationRequest, AssignmentUpdateRequest } from '@/types/assignment';

export const assignmentApi = {
  // CRUD operations
  getAssignments: (params: { page?: number; size?: number; sort?: string; courseId?: string; teacherId?: string; status?: string; keyword?: string }): Promise<ApiResponse<PaginatedResponse<Assignment>>> => {
    return api.get('/assignments', { params });
  },

  getAssignmentsByCourse: (courseId: string, params: { page?: number; size?: number; sort?: string }): Promise<ApiResponse<PaginatedResponse<Assignment>>> => {
    // 后端 AssignmentController 暴露的课程作业接口路径为 /assignments?courseId=xxx
    const merged = { ...(params||{}), courseId } as any
    return api.get('/assignments', { params: merged });
  },

  getAssignmentById: (id: string): Promise<ApiResponse<Assignment>> => {
    return api.get(`/assignments/${id}`);
  },

  createAssignment: (data: AssignmentCreationRequest): Promise<ApiResponse<Assignment>> => {
    return api.post('/assignments', data);
  },

  updateAssignment: (id: string, data: AssignmentUpdateRequest): Promise<ApiResponse<Assignment>> => {
    return api.put(`/assignments/${id}`, data);
  },

  deleteAssignment: (id: string): Promise<ApiResponse<void>> => {
    return api.delete(`/assignments/${id}`);
  },

  // Status changes
  publishAssignment: (id: string): Promise<ApiResponse<void>> => {
    return api.post(`/assignments/${id}/publish`);
  },

  closeAssignment: (id: string): Promise<ApiResponse<void>> => {
    return api.post(`/assignments/${id}/close`);
  },

  // Submission stats for a specific assignment
  getAssignmentSubmissionStats: (id: string): Promise<ApiResponse<{ assignmentId: string; courseId: string; totalEnrolled: number; submittedCount: number; unsubmittedCount: number }>> => {
    return api.get(`/assignments/${id}/submission-stats`);
  },

  // Remind unsubmitted students
  remindUnsubmitted: (id: string, message?: string): Promise<ApiResponse<{ sent: number; failed: number }>> => {
    const payload = message ? { message } : undefined as any
    return api.post(`/assignments/${id}/remind-unsubmitted`, payload);
  },
};
