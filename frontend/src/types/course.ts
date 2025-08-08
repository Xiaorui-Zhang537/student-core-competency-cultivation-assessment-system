// DTOs based on backend entities (e.g., Course.java)
export interface Course {
  id: number;
  title: string;
  description: string;
  content?: string; // Added field
  teacherId: number;
  teacherName: string; // Denormalized for convenience
  category: string;
  tags: string[];
  coverImage: string;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  createdAt: string;
  updatedAt: string;
  studentCount: number;
  averageRating: number;
  isPublished: boolean; // Added based on CourseController logic
}

export interface CourseDetailed extends Course {
  lessons: any[]; // Replace with Lesson type when available
  assignments: any[]; // Replace with Assignment type when available
  resources: any[]; // Replace with Resource type when available
}

// Request payloads for API endpoints
export interface CourseCreationRequest {
  title: string;
  description: string;
  content?: string; // Added field
  category: string;
  tags: string[] | string;
  coverImage?: string;
}

export interface CourseUpdateRequest {
  title?: string;
  description?: string;
  content?: string; // Added field
  category?: string;
  tags?: string[] | string;
  coverImage?: string;
}

export interface BatchStatusUpdateRequest {
  courseIds: number[];
  status: 'PUBLISHED' | 'UNPUBLISHED' | 'ARCHIVED';
}

// API Response Types
export interface CourseStatistics {
  totalCourses: number;
  publishedCourses: number;
  draftCourses: number;
  enrollmentCount: number;
}

export interface CourseCategory {
  id: string;
  name: string;
  courseCount: number;
}
