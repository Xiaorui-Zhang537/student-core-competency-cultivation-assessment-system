export interface Assignment {
  id: string
  courseId: string
  title: string
  description: string
  instructions: string
  dueDate: string
  maxScore: number
  submissionType: 'file' | 'text' | 'link' | 'multiple'
  allowedFileTypes?: string[]
  maxFileSize?: number
  status: 'draft' | 'published' | 'closed'
  createdAt: string
  updatedAt: string
}

export interface AssignmentSubmission {
  id: string
  assignmentId: string
  studentId: string
  title?: string
  content?: string
  files: SubmissionFile[]
  submittedAt: string
  status: 'draft' | 'submitted' | 'graded' | 'returned'
  score?: number
  feedback?: string
  gradedAt?: string
  gradedBy?: string
}

export interface SubmissionFile {
  id: string
  name: string
  type: string
  size: number
  url: string
  uploadedAt: string
}

export interface AssignmentGrade {
  id: string
  submissionId: string
  score: number
  feedback: string
  rubric?: GradeRubric[]
  gradedBy: string
  gradedAt: string
  aiAnalysis?: AIGradeAnalysis
}

export interface GradeRubric {
  criterion: string
  score: number
  maxScore: number
  feedback: string
}

export interface AIGradeAnalysis {
  overallScore: number
  strengths: string[]
  improvements: string[]
  suggestions: string[]
  confidence: number
} 