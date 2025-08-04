import { api } from './config';
import type { ApiResponse, PaginatedResponse } from '@/types/api';
import type { StudentDashboardData, StudentCourse, StudentLesson, StudentSubmission } from '@/types/student';

export const studentApi = {
  getDashboardData: (): Promise<ApiResponse<StudentDashboardData>> => {
    return api.get('/api/students/dashboard');
  },

  getMyCourses: (params?: { page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<StudentCourse>>> => {
    return api.get('/api/students/my-courses', { params });
  },

  getCourseProgress: (courseId: string): Promise<ApiResponse<StudentCourse>> => {
    return api.get(`/api/students/courses/${courseId}/progress`);
  },

  getLessonDetails: (lessonId: string): Promise<ApiResponse<StudentLesson>> => {
    return api.get(`/api/students/lessons/${lessonId}`);
  },

  markLessonAsCompleted: (lessonId: string): Promise<ApiResponse<void>> => {
    return api.post(`/api/students/lessons/${lessonId}/complete`);
  },

  markLessonAsIncomplete: (lessonId: string): Promise<ApiResponse<void>> => {
    return api.post(`/api/students/lessons/${lessonId}/incomplete`);
  },

  getMySubmissions: (params?: { courseId?: string }): Promise<ApiResponse<StudentSubmission[]>> => {
    return api.get('/api/students/my-submissions', { params });
  },
};
