import { api } from './config';
import type { ApiResponse, PaginatedResponse } from '@/types/api';
import type { Assignment, AssignmentCreationRequest, AssignmentUpdateRequest } from '@/types/assignment';

export const assignmentApi = {
  // CRUD operations
  getAssignments: (params: { page?: number; size?: number; sort?: string }): Promise<ApiResponse<PaginatedResponse<Assignment>>> => {
    return api.get('/api/assignments', { params });
  },
  
  getAssignmentsByCourse: (courseId: number, params: { page?: number; size?: number; sort?: string }): Promise<ApiResponse<PaginatedResponse<Assignment>>> => {
    return api.get(`/api/courses/${courseId}/assignments`, { params });
  },

  getAssignmentById: (id: number): Promise<ApiResponse<Assignment>> => {
    return api.get(`/api/assignments/${id}`);
  },

  createAssignment: (data: AssignmentCreationRequest): Promise<ApiResponse<Assignment>> => {
    return api.post('/api/assignments', data);
  },

  updateAssignment: (id: number, data: AssignmentUpdateRequest): Promise<ApiResponse<Assignment>> => {
    return api.put(`/api/assignments/${id}`, data);
  },

  deleteAssignment: (id: number): Promise<ApiResponse<void>> => {
    return api.delete(`/api/assignments/${id}`);
  },

  // Status changes
  publishAssignment: (id: number): Promise<ApiResponse<void>> => {
    return api.post(`/api/assignments/${id}/publish`);
  },

  closeAssignment: (id: number): Promise<ApiResponse<void>> => {
    return api.post(`/api/assignments/${id}/close`);
  },
};
