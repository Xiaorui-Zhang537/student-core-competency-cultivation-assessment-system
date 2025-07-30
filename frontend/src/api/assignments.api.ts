import { api } from './config'
import type { Assignment, AssignmentSubmission, AssignmentGrade } from '@/types/assignment'
import type { ApiResponse, PaginatedResponse } from '@/types/api'

export const assignmentsAPI = {
  // 获取作业列表
  getAssignments: (params?: { courseId?: string; page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<Assignment>>> => {
    return api.get<PaginatedResponse<Assignment>>('/api/assignments', { params })
  },

  // 获取我的作业提交
  getMySubmissions: (): Promise<ApiResponse<AssignmentSubmission[]>> => {
    return api.get<AssignmentSubmission[]>('/api/assignments/my-submissions')
  },

  // 获取作业详情
  getAssignment: (id: string): Promise<ApiResponse<Assignment>> => {
    return api.get<Assignment>(`/api/assignments/${id}`)
  },

  // 获取作业提交详情
  getSubmission: (assignmentId: string): Promise<ApiResponse<AssignmentSubmission>> => {
    return api.get<AssignmentSubmission>(`/api/assignments/${assignmentId}/submission`)
  },

  // 提交作业
  submitAssignment: (assignmentId: string, data: FormData): Promise<ApiResponse<AssignmentSubmission>> => {
    return api.post<AssignmentSubmission>(`/api/assignments/${assignmentId}/submit`, data, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 保存草稿
  saveDraft: (assignmentId: string, content: string): Promise<ApiResponse<AssignmentSubmission>> => {
    return api.post<AssignmentSubmission>(`/api/assignments/${assignmentId}/draft`, { content })
  },

  // 获取成绩
  getGrades: (): Promise<ApiResponse<AssignmentGrade[]>> => {
    return api.get<AssignmentGrade[]>('/api/assignments/grades')
  },

  // 获取作业成绩详情
  getGradeDetail: (submissionId: string): Promise<ApiResponse<AssignmentGrade>> => {
    return api.get<AssignmentGrade>(`/api/assignments/submissions/${submissionId}/grade`)
  }
} 