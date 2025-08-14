// Based on Grade.java DTO
export interface Grade {
  id: string;
  submissionId: string;
  studentId: string;
  teacherId: string;
  score: number;
  feedback: string;
  gradedAt: string;
  isPublished: boolean;
  // Denormalized fields for UI convenience
  assignmentTitle?: string;
  courseTitle?: string;
  teacherName?: string;
}

// For creating or updating a grade
export interface GradeRequest {
  submissionId: string;
  studentId: string;
  score: number;
  feedback?: string;
  assignmentId: string;
  maxScore: number;
  status?: 'draft' | 'published';
  publishImmediately?: boolean;
}

// For publishing a grade
export interface GradePublishRequest {
    publish: boolean;
}
