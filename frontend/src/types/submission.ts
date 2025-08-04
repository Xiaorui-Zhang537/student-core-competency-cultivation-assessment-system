// Based on Submission.java DTOs
export interface Submission {
  id: string;
  assignmentId: string;
  studentId: string;
  content: string;
  submittedAt: string;
  status: 'SUBMITTED' | 'LATE' | 'GRADED';
  fileIds: string[];
}

// For creating a new submission
export interface SubmissionRequest {
  content: string;
  fileIds?: string[];
}

// For saving a draft
export interface DraftRequest {
  content: string;
  fileIds?: string[];
}
