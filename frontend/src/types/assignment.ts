/**
 * 作业类型定义
 * 说明：
 * - 后端字段多为 snake_case（如 publish_at），前端部分页面会使用 camelCase（publishAt）
 * - status 在历史代码中存在大小写混用（draft/published/... 或 DRAFT/PUBLISHED/...）
 */
export type AssignmentStatus =
  | 'draft'
  | 'scheduled'
  | 'published'
  | 'closed'
  | 'DRAFT'
  | 'SCHEDULED'
  | 'PUBLISHED'
  | 'CLOSED'
  | string

export interface Assignment {
  id: string
  courseId: string
  title: string
  description: string
  /**
   * 截止时间：course_bound（无截止）可能为空
   */
  dueDate?: string | null
  /**
   * 定时发布时间（仅 normal + scheduled 使用）
   */
  publishAt?: string | null
  publish_at?: string | null

  assignmentType?: 'normal' | 'course_bound'
  status: AssignmentStatus

  createdAt?: string
  updatedAt?: string
}

// For creating a new assignment
export interface AssignmentCreationRequest {
  courseId: string
  title: string
  description: string
  dueDate?: string | null
  publishAt?: string | null
  assignmentType?: 'normal' | 'course_bound'
  status?: AssignmentStatus
}

// For updating an existing assignment
export interface AssignmentUpdateRequest {
  title?: string
  description?: string
  dueDate?: string | null
  publishAt?: string | null
  assignmentType?: 'normal' | 'course_bound'
  status?: AssignmentStatus
}
