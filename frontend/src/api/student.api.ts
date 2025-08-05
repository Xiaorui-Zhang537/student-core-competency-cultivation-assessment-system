import { api } from './config';
import type { ApiResponse, PaginatedResponse } from '@/types/api';
import type { StudentDashboardData, StudentCourse, StudentLesson, StudentSubmission } from '@/types/student';

export const studentApi = {
  getDashboardData: (): Promise<ApiResponse<StudentDashboardData>> => {
    return api.get('/students/dashboard');
  },

  getMyCourses: (params?: { page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<StudentCourse>>> => {
    return api.get('/students/my-courses', { params });
  },

  getCourseProgress: (courseId: string): Promise<ApiResponse<StudentCourse>> => {
    return api.get(`/students/courses/${courseId}/progress`);
  },

  getLessonDetails: (lessonId: string): Promise<ApiResponse<StudentLesson>> => {
    return api.get(`/students/lessons/${lessonId}`);
  },

  markLessonAsCompleted: (lessonId: string): Promise<ApiResponse<void>> => {
    return api.post(`/students/lessons/${lessonId}/complete`);
  },

  markLessonAsIncomplete: (lessonId: string): Promise<ApiResponse<void>> => {
    return api.post(`/students/lessons/${lessonId}/incomplete`);
  },

  getMySubmissions: (params?: { courseId?: string }): Promise<ApiResponse<StudentSubmission[]>> => {
    return api.get('/students/my-submissions', { params });
  },
};
