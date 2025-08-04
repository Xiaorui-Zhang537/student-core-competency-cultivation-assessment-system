// Based on Grade.java DTO
export interface Grade {
  id: number;
  submissionId: number;
  studentId: number;
  teacherId: number;
  score: number;
  feedback: string;
  gradedAt: string;
  isPublished: boolean;
}

// For creating or updating a grade
export interface GradeRequest {
  submissionId: number;
  studentId: number;
  score: number;
  feedback: string;
}

// For publishing a grade
export interface GradePublishRequest {
    publish: boolean;
}
