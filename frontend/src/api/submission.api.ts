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
    return api.post(`/assignments/${assignmentId}/submit`, data);
  },

  saveDraft: (assignmentId: string, data: DraftRequest): Promise<ApiResponse<void>> => {
    return api.post(`/assignments/${assignmentId}/draft`, data);
  },

  // Get grade summary for a submission (map with grade_id etc.)
  getSubmissionGrade: (submissionId: string): Promise<ApiResponse<any>> => {
    return api.get(`/submissions/${submissionId}/grade`);
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
