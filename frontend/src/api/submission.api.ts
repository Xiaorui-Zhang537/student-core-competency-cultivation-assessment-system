import { api } from './config';
import type { ApiResponse, PaginatedResponse } from '@/types/api';
import type { Submission, SubmissionRequest, DraftRequest } from '@/types/submission';
// Grade type will be needed later
// import type { Grade } from '@/types/grade';

export const submissionApi = {
  // Teacher fetches a submission by id
  getSubmissionById: (submissionId: string): Promise<ApiResponse<Submission>> => {
    return api.get(`/submissions/${submissionId}`);
  },
  getSubmissionForAssignment: (assignmentId: string): Promise<ApiResponse<Submission>> => {
    return api.get(`/assignments/${assignmentId}/submission`);
  },

  // Teacher fetch submissions of an assignment (paginated)
  getSubmissionsByAssignment: (assignmentId: string, params?: { page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<Submission>>> => {
    return api.get(`/assignments/${assignmentId}/submissions`, { params });
  },

  submitAssignment: (assignmentId: string, data: SubmissionRequest): Promise<ApiResponse<Submission>> => {
    const payload = {
      ...data,
      fileIds: Array.isArray(data?.fileIds)
        ? (data.fileIds as any[]).map((v) => {
            const n = Number(v as any);
            return Number.isFinite(n) ? n : undefined;
          }).filter((v) => typeof v === 'number')
        : undefined,
    } as any;
    return api.post(`/assignments/${assignmentId}/submit`, payload);
  },

  saveDraft: (assignmentId: string, data: DraftRequest): Promise<ApiResponse<void>> => {
    // 优先使用 x-www-form-urlencoded 作为 POST body 传参，兼容部分后端仅从 request body 取 @RequestParam 的实现
    const body = new URLSearchParams();
    body.set('content', String(data?.content ?? ''));
    return api.post(`/assignments/${assignmentId}/draft`, body, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    });
  },

  // Get grade summary for a submission (map with grade_id etc.)
  getSubmissionGrade: (submissionId: string): Promise<ApiResponse<any>> => {
    return api.get(`/submissions/${submissionId}/grade`);
  },

  // Export submission as ZIP (authorized blob)
  exportSubmission: (submissionId: string): Promise<Blob> => {
    return import('./config').then(({ default: apiClient }: any) =>
      apiClient.get(`/submissions/${submissionId}/export`, { responseType: 'blob' })
        .then((response: any) => response as Blob)
    );
  },

  // NOTE: These endpoints are related to grades, and will be used when Grade module is refactored.
  /*
  getMyGradedSubmissions: (): Promise<ApiResponse<any[]>> => {
    return api.get('/submissions/my/grades');
  },

  getGradeForSubmission: (submissionId: string): Promise<ApiResponse<Grade>> => {
    return api.get(`/submissions/${submissionId}/grade`);
  },
  */
};
