import { api } from './config';
import type { ApiResponse, PaginatedResponse } from '@/types/api';
import type { Assignment, AssignmentCreationRequest, AssignmentUpdateRequest } from '@/types/assignment';

export const assignmentApi = {
  // CRUD operations
  getAssignments: (params: { page?: number; size?: number; sort?: string }): Promise<ApiResponse<PaginatedResponse<Assignment>>> => {
    return api.get('/assignments', { params });
  },

  getAssignmentsByCourse: (courseId: string, params: { page?: number; size?: number; sort?: string }): Promise<ApiResponse<PaginatedResponse<Assignment>>> => {
    return api.get(`/courses/${courseId}/assignments`, { params });
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
};
