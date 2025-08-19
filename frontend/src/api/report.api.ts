import { api } from './config'
import type { ApiResponse } from '@/types/api'

export interface CreateReportRequest {
  reportedStudentId: string | number
  courseId?: string | number
  assignmentId?: string | number
  submissionId?: string | number
  reason: string
  details?: string
  evidenceFileId?: string | number
}

export const reportApi = {
  createReport: (data: CreateReportRequest): Promise<ApiResponse<{ id: number }>> => {
    return api.post('/reports', data)
  },
}


