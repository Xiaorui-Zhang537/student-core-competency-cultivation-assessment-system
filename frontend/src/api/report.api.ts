import { api } from './config'

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
  // NOTE: axios response interceptor unwraps ApiResponse<T> => T (see frontend/src/api/config.ts)
  createReport: (data: CreateReportRequest): Promise<any> => {
    return api.post('/reports', data)
  },
}

