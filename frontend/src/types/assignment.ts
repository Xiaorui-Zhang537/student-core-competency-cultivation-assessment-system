// Based on Assignment.java
export interface Assignment {
  id: string;
  courseId: string;
  title: string;
  description: string;
  dueDate: string;
  status: 'DRAFT' | 'PUBLISHED' | 'CLOSED';
  createdAt: string;
  updatedAt: string;
}

// For creating a new assignment
export interface AssignmentCreationRequest {
  courseId: string;
  title: string;
  description: string;
  dueDate: string;
}

// For updating an existing assignment
export interface AssignmentUpdateRequest {
  title?: string;
  description?: string;
  dueDate?: string;
}
