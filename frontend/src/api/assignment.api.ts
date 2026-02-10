import { api } from './config';
import type { ApiResponse, PaginatedResponse } from '@/types/api';
import type { Assignment, AssignmentCreationRequest, AssignmentUpdateRequest } from '@/types/assignment';

const normalizeDueDate = (v?: string | null): string | undefined => {
  if (!v) return v;
  // 如果是日期字符串（YYYY-MM-DD），补全为一天结束时间，匹配后端 yyyy-MM-dd HH:mm:ss
  if (/^\d{4}-\d{2}-\d{2}$/.test(v)) return `${v} 23:59:59`;
  // 如果是 ISO 字符串，转化为 yyyy-MM-dd HH:mm:ss
  if (/T/.test(v)) {
    const d = new Date(v);
    if (!isNaN(d.getTime())) {
      const pad = (n: number) => String(n).padStart(2, '0');
      const y = d.getFullYear();
      const m = pad(d.getMonth() + 1);
      const day = pad(d.getDate());
      const hh = pad(d.getHours());
      const mm = pad(d.getMinutes());
      const ss = pad(d.getSeconds());
      return `${y}-${m}-${day} ${hh}:${mm}:${ss}`;
    }
  }
  return v;
};

export const assignmentApi = {
  // CRUD operations
  getAssignments: (params: { page?: number; size?: number; sort?: string; courseId?: string; teacherId?: string; status?: string; keyword?: string }): Promise<ApiResponse<PaginatedResponse<Assignment>>> => {
    return api.get('/assignments', { params });
  },

  getAssignmentsByCourse: (courseId: string, params: { page?: number; size?: number; sort?: string; assignmentType?: string }): Promise<ApiResponse<PaginatedResponse<Assignment>>> => {
    // 后端 AssignmentController 暴露的课程作业接口路径为 /assignments?courseId=xxx
    const merged = { ...(params||{}), courseId } as any
    return api.get('/assignments', { params: merged });
  },

  getAssignmentById: (id: string): Promise<ApiResponse<Assignment>> => {
    return api.get(`/assignments/${id}`);
  },

  createAssignment: (data: AssignmentCreationRequest): Promise<ApiResponse<Assignment>> => {
    const payload = { ...data, dueDate: normalizeDueDate(data.dueDate) };
    return api.post('/assignments', payload);
  },

  updateAssignment: (id: string, data: AssignmentUpdateRequest): Promise<ApiResponse<Assignment>> => {
    const payload = { ...data, dueDate: normalizeDueDate(data.dueDate) };
    return api.put(`/assignments/${id}`, payload);
  },

  // bind only lessonId without touching title/desc validations
  bindLesson: (id: string, lessonId: string): Promise<ApiResponse<void>> => {
    return api.put(`/assignments/${id}/lesson`, { lessonId });
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
