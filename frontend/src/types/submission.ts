// Based on Submission.java DTOs
export interface Submission {
  id: number;
  assignmentId: number;
  studentId: number;
  content: string;
  submittedAt: string;
  status: 'SUBMITTED' | 'LATE' | 'GRADED';
  fileIds: number[];
}

// For creating a new submission
export interface SubmissionRequest {
  content: string;
  fileIds?: number[];
}

// For saving a draft
export interface DraftRequest {
  content: string;
  fileIds?: number[];
}
