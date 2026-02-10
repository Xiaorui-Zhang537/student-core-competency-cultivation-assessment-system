import type { Lesson } from '@/types/lesson';
import type { ApiResponse } from '@/types/api';
import { api } from './config';

export const lessonApi = {
  getLessonsByCourse(courseId: string): Promise<ApiResponse<Lesson[]>> {
    return api.get(`/lessons/course/${courseId}`);
  },

  // 课程整体进度（百分比，后端 BigDecimal 0-100）
  getCourseProgressPercent(courseId: string, studentId?: string): Promise<ApiResponse<number>> {
    const params: any = {}
    if (studentId) params.studentId = studentId
    return api.get(`/lessons/course/${courseId}/progress-percentage`, { params })
  },

  getLesson(lessonId: string): Promise<ApiResponse<Lesson>> {
    return api.get(`/lessons/${lessonId}`);
  },

  // 学生在某课程下所有课节的进度列表
  getStudentCourseProgressList(courseId: string, studentId?: string): Promise<ApiResponse<any[]>> {
    const params: any = {}
    if (studentId) params.studentId = studentId
    return api.get(`/lessons/course/${courseId}/student-progress`, { params })
  },

  // New: get lesson materials via association table (方案A)
  getLessonMaterials(lessonId: string): Promise<ApiResponse<any[]>> {
    return api.get(`/lessons/${lessonId}/materials`);
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

  // 设置章节内容（视频URL与资料文件绑定）
  updateLessonContent(lessonId: string, payload: { videoUrl?: string; materialFileIds?: number[]; allowScrubbing?: boolean; allowSpeedChange?: boolean }): Promise<ApiResponse<any>> {
    return api.put(`/lessons/${lessonId}/content`, payload);
  },

  updateLessonOrder(lessonId: string, order: number): Promise<ApiResponse<void>> {
    return api.put(`/lessons/${lessonId}/order`, { order });
  },

  // Student actions
  completeLesson(lessonId: string): Promise<ApiResponse<void>> {
    return api.post(`/lessons/${lessonId}/complete`, {});
  },

  updateLessonProgress(lessonId: string, payload: { progress?: number; studyTime?: number; lastPosition?: number }): Promise<ApiResponse<void>> {
    return api.post(`/lessons/${lessonId}/progress`, payload);
  },

  addLessonNotes(lessonId: string, notes: string): Promise<ApiResponse<void>> {
    return api.post(`/lessons/${lessonId}/notes`, { notes });
  },

  getLessonNotes(lessonId: string): Promise<ApiResponse<{ notes: string }>> {
    return api.get(`/lessons/${lessonId}/notes`);
  }
};
