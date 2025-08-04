import { api } from './config';
import type { ApiResponse } from '@/types/api';
import type { Submission, SubmissionRequest, DraftRequest } from '@/types/submission';
// Grade type will be needed later
// import type { Grade } from '@/types/grade';

export const submissionApi = {
  getSubmissionForAssignment: (assignmentId: number): Promise<ApiResponse<Submission>> => {
    return api.get(`/api/assignments/${assignmentId}/submission`);
  },

  submitAssignment: (assignmentId: number, data: SubmissionRequest): Promise<ApiResponse<Submission>> => {
    return api.post(`/api/assignments/${assignmentId}/submit`, data);
  },

  saveDraft: (assignmentId: number, data: DraftRequest): Promise<ApiResponse<void>> => {
    return api.post(`/api/assignments/${assignmentId}/draft`, data);
  },
  
  // NOTE: These endpoints are related to grades, and will be used when Grade module is refactored.
  /*
  getMyGradedSubmissions: (): Promise<ApiResponse<any[]>> => {
    return api.get('/api/submissions/my/grades');
  },

  getGradeForSubmission: (submissionId: number): Promise<ApiResponse<Grade>> => {
    return api.get(`/api/submissions/${submissionId}/grade`);
  },
  */
};
