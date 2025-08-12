import { api } from './config';
import type { ApiResponse, PaginatedResponse } from '@/types/api';
import type { Course, CourseDetailed, CourseCreationRequest, CourseUpdateRequest, BatchStatusUpdateRequest, CourseStatistics } from '@/types/course';
import type { PaginatedResponse } from '@/types/api';
import type { User } from '@/types/user';

export const courseApi = {
  // CRUD operations
  getCourses: (params: { page?: number; size?: number; sort?: string; query?: string; status?: string; teacherId?: string; }): Promise<ApiResponse<PaginatedResponse<Course>>> => {
    return api.get('/courses', { params });
  },

  getCourseById: (id: number): Promise<ApiResponse<CourseDetailed>> => {
    return api.get(`/courses/${id}`);
  },

  createCourse: (data: CourseCreationRequest): Promise<ApiResponse<Course>> => {
    return api.post('/courses', data);
  },

  updateCourse: (id: number, data: CourseUpdateRequest): Promise<ApiResponse<Course>> => {
    return api.put(`/courses/${id}`, data);
  },

  deleteCourse: (id: number): Promise<ApiResponse<void>> => {
    return api.delete(`/courses/${id}`);
  },

  // Enrollment management
  enrollInCourse: (courseId: number): Promise<ApiResponse<void>> => {
    return api.post(`/courses/${courseId}/enroll`);
  },

  unenrollFromCourse: (courseId: number): Promise<ApiResponse<void>> => {
    return api.delete(`/courses/${courseId}/enroll`);
  },

  getEnrollmentStatus: (courseId: number): Promise<ApiResponse<{ isEnrolled: boolean }>> => {
    return api.get(`/courses/${courseId}/enrollment-status`);
  },

  // Publishing
  publishCourse: (courseId: number): Promise<ApiResponse<void>> => {
    return api.post(`/courses/${courseId}/publish`);
  },

  unpublishCourse: (courseId: number): Promise<ApiResponse<void>> => {
    return api.post(`/courses/${courseId}/unpublish`);
  },

  updateBatchStatus: (data: BatchStatusUpdateRequest): Promise<ApiResponse<void>> => {
      return api.put('/courses/batch-status', data);
  },

  // Search and discovery
  searchCourses: (params: { query: string; page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<Course>>> => {
    return api.get('/courses/search', { params });
  },

  getPopularCourses: (): Promise<ApiResponse<Course[]>> => {
    return api.get('/courses/popular');
  },

  getRecommendedCourses: (): Promise<ApiResponse<Course[]>> => {
    return api.get('/courses/recommended');
  },

  getCoursesByCategory: (category: string, params: { page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<Course>>> => {
    return api.get(`/courses/category/${category}`, { params });
  },

  // Statistics
  getCourseStatistics: (): Promise<ApiResponse<CourseStatistics>> => {
    return api.get('/courses/statistics');
  },

  // Enrollment listings (teacher only)
  getCourseStudents: (courseId: string, params?: { page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<User>>> => {
    return api.get(`/courses/${courseId}/students`, { params });
  },

  // Teacher invite/add students to course
  inviteStudents: (courseId: string | number, studentIds: Array<number|string>): Promise<ApiResponse<void>> => {
    const ids = (studentIds || []).map(id => Number(id)).filter(n => !Number.isNaN(n));
    return api.post(`/courses/${courseId}/students/invite`, { studentIds: ids });
  }
};
