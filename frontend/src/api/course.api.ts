import { api } from './config';
import type { ApiResponse, PaginatedResponse } from '@/types/api';
import type { Course, CourseDetailed, CourseCreationRequest, CourseUpdateRequest, BatchStatusUpdateRequest, CourseStatistics } from '@/types/course';

export const courseApi = {
  // CRUD operations
  getCourses: (params: { page?: number; size?: number; sort?: string }): Promise<ApiResponse<PaginatedResponse<Course>>> => {
    return api.get('/api/courses', { params });
  },

  getCourseById: (id: number): Promise<ApiResponse<CourseDetailed>> => {
    return api.get(`/api/courses/${id}`);
  },

  createCourse: (data: CourseCreationRequest): Promise<ApiResponse<Course>> => {
    return api.post('/api/courses', data);
  },

  updateCourse: (id: number, data: CourseUpdateRequest): Promise<ApiResponse<Course>> => {
    return api.put(`/api/courses/${id}`, data);
  },

  deleteCourse: (id: number): Promise<ApiResponse<void>> => {
    return api.delete(`/api/courses/${id}`);
  },

  // Enrollment management
  enrollInCourse: (courseId: number): Promise<ApiResponse<void>> => {
    return api.post(`/api/courses/${courseId}/enroll`);
  },

  unenrollFromCourse: (courseId: number): Promise<ApiResponse<void>> => {
    return api.delete(`/api/courses/${courseId}/enroll`);
  },

  getEnrollmentStatus: (courseId: number): Promise<ApiResponse<{ isEnrolled: boolean }>> => {
    return api.get(`/api/courses/${courseId}/enrollment-status`);
  },

  // Publishing
  publishCourse: (courseId: number): Promise<ApiResponse<void>> => {
    return api.post(`/api/courses/${courseId}/publish`);
  },

  unpublishCourse: (courseId: number): Promise<ApiResponse<void>> => {
    return api.post(`/api/courses/${courseId}/unpublish`);
  },
  
  updateBatchStatus: (data: BatchStatusUpdateRequest): Promise<ApiResponse<void>> => {
      return api.put('/api/courses/batch-status', data);
  },

  // Search and discovery
  searchCourses: (params: { query: string; page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<Course>>> => {
    return api.get('/api/courses/search', { params });
  },

  getPopularCourses: (): Promise<ApiResponse<Course[]>> => {
    return api.get('/api/courses/popular');
  },
  
  getRecommendedCourses: (): Promise<ApiResponse<Course[]>> => {
    return api.get('/api/courses/recommended');
  },
  
  getCoursesByCategory: (category: string, params: { page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<Course>>> => {
    return api.get(`/api/courses/category/${category}`, { params });
  },

  // Statistics
  getCourseStatistics: (): Promise<ApiResponse<CourseStatistics>> => {
    return api.get('/api/courses/statistics');
  }
};
