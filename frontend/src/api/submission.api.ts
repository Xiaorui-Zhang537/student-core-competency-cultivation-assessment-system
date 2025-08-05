import { api } from './config';
import type { ApiResponse } from '@/types/api';
import type { Submission, SubmissionRequest, DraftRequest } from '@/types/submission';
// Grade type will be needed later
// import type { Grade } from '@/types/grade';

export const submissionApi = {
  getSubmissionForAssignment: (assignmentId: string): Promise<ApiResponse<Submission>> => {
    return api.get(`/assignments/${assignmentId}/submission`);
  },

  submitAssignment: (assignmentId: string, data: SubmissionRequest): Promise<ApiResponse<Submission>> => {
    return api.post(`/assignments/${assignmentId}/submit`, data);
  },

  saveDraft: (assignmentId: string, data: DraftRequest): Promise<ApiResponse<void>> => {
    return api.post(`/assignments/${assignmentId}/draft`, data);
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
