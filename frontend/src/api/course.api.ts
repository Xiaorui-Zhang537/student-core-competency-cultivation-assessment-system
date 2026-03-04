import { api } from './config';
import type { PaginatedResponse } from '@/types/api';
import type { Course, CourseDetailed, CourseCreationRequest, CourseUpdateRequest } from '@/types/course';
import type { User } from '@/types/auth';

export const courseApi = {
  // CRUD operations
  getCourses: (params: { page?: number; size?: number; sort?: string; query?: string; status?: string; teacherId?: string; category?: string; difficulty?: string; }): Promise<PaginatedResponse<Course>> => {
    return api.get('/courses', { params });
  },

  getCourseById: (id: number): Promise<CourseDetailed> => {
    return api.get(`/courses/${id}`);
  },

  createCourse: (data: CourseCreationRequest): Promise<Course> => {
    return api.post('/courses', data);
  },

  updateCourse: (id: number, data: CourseUpdateRequest): Promise<Course> => {
    return api.put(`/courses/${id}`, data);
  },

  deleteCourse: (id: number): Promise<void> => {
    return api.delete(`/courses/${id}`);
  },

  // Enrollment management
  enrollInCourse: (courseId: number | string, enrollKey?: string): Promise<void> => {
    const payload = enrollKey ? { enrollKey } : undefined as any
    return api.post(`/courses/${courseId}/enroll`, payload);
  },

  unenrollFromCourse: (courseId: number | string): Promise<void> => {
    return api.delete(`/courses/${courseId}/enroll`);
  },

  getEnrollmentStatus: (courseId: number | string): Promise<boolean> => {
    return api.get(`/courses/${courseId}/enrollment-status`);
  },

  // Publishing
  publishCourse: (courseId: number): Promise<void> => {
    return api.post(`/courses/${courseId}/publish`);
  },

  // Enrollment listings (teacher only)
  getCourseStudents: (courseId: string, params?: { page?: number; size?: number }): Promise<PaginatedResponse<User>> => {
    return api.get(`/courses/${courseId}/students`, { params });
  },

  // Teacher invite/add students to course
  inviteStudents: (courseId: string | number, studentIds: Array<number|string>): Promise<void> => {
    const ids = (studentIds || []).map(id => Number(id)).filter(n => !Number.isNaN(n));
    return api.post(`/courses/${courseId}/students/invite`, { studentIds: ids });
  },

  // Teacher remove a student from a course
  removeStudent: (courseId: string | number, studentId: string | number): Promise<void> => {
    return api.delete(`/courses/${courseId}/students/${studentId}`);
  },

  // Teacher: set course enroll key & toggle
  setCourseEnrollKey: (courseId: string | number, require: boolean, key?: string): Promise<void> => {
    return api.put(`/courses/${courseId}/enroll-key`, { require, key });
  }
};
