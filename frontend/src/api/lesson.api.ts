import type { Lesson } from '@/types/lesson';
import type { ApiResponse } from '@/types/api';
import api from './axios';

export const lessonApi = {
  getLessonsByCourse(courseId: string): Promise<ApiResponse<Lesson[]>> {
    return api.get(`/lessons/course/${courseId}`);
  },

  getLesson(lessonId: string): Promise<ApiResponse<Lesson>> {
    return api.get(`/lessons/${lessonId}`);
  },

  // Teacher actions
  createLesson(data: Partial<Lesson>): Promise<ApiResponse<Lesson>> {
    return api.post('/lessons', data);
  },

  updateLesson(lessonId: string, data: Partial<Lesson>): Promise<ApiResponse<Lesson>> {
    return api.put(`/lessons/${lessonId}`, data);
  },

  deleteLesson(lessonId: string): Promise<ApiResponse<void>> {
    return api.delete(`/lessons/${lessonId}`);
  },

  // Student actions
  completeLesson(lessonId: string): Promise<ApiResponse<void>> {
    return api.post(`/lessons/${lessonId}/complete`, {});
  },

  updateLessonProgress(lessonId: string, progress: number): Promise<ApiResponse<void>> {
    return api.post(`/lessons/${lessonId}/progress`, { progress });
  }
};
