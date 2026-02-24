import { api } from './config'

export interface PageResult<T> {
  items: T[]
  page: number
  size: number
  total: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
}

export interface AdminDashboardOverview {
  daysActiveWindow: number
  users: { total: number; students: number; teachers: number; admins: number }
  courses: { total: number; draft: number; published: number; archived: number }
  community: { posts: number; comments: number }
  reports: { total: number; pending: number }
  activity: { activeUsers: number; since: string }
  grades?: { A: number; B: number; C: number; D: number; E: number; F: number }
}

export interface AdminAbilityRadarDimensionOverview {
  code: string
  name: string
  value: number
  sampleSize: number
}

export interface AdminAbilityRadarOverviewResponse {
  days: number
  dimensions: AdminAbilityRadarDimensionOverview[]
}

export interface AdminAiUsageUserItem {
  userId: number
  username: string
  role: string
  conversationCount: number
  messageCount: number
  lastActiveAt?: string
}

export interface AdminAiUsageOverviewResponse {
  days: number
  limit: number
  summary: {
    conversationCount: number
    messageCount: number
    activeUsers: number
  }
  users: AdminAiUsageUserItem[]
}

export interface AdminUserListItem {
  id: number
  username: string
  email: string
  role: string
  status: string
  nickname?: string
  studentNo?: string
  teacherNo?: string
  emailVerified?: boolean
  deleted?: boolean
  createdAt?: string
}

export interface AdminUserCreateRequest {
  username: string
  email: string
  password: string
  role: 'student' | 'teacher' | 'admin'
  status?: 'active' | 'disabled'
  nickname?: string
  firstName?: string
  lastName?: string
  studentNo?: string
  teacherNo?: string
  school?: string
  subject?: string
  grade?: string
}

export interface AdminCourseDetailResponse {
  course: any
  activeEnrollments: number
}

export interface AbilityReport {
  id: number
  studentId: number
  studentName?: string
  studentNumber?: string
  reportType: string
  title: string
  overallScore?: string | number
  courseId?: number
  assignmentId?: number
  submissionId?: number
  isPublished?: boolean
  createdAt?: string
  dimensionScores?: string
  recommendations?: string
}

export interface ReportEntity {
  id: number
  reporterId: number
  reportedStudentId?: number
  courseId?: number
  assignmentId?: number
  submissionId?: number
  reason: string
  details?: string
  evidenceFileId?: number
  status: string
  createdAt?: string
  updatedAt?: string
}

export const adminApi = {
  getDashboardOverview: (days = 7): Promise<AdminDashboardOverview> => {
    return api.get('/admin/dashboard/overview', { params: { days } })
  },
  getAbilityRadarOverview: (days = 180): Promise<AdminAbilityRadarOverviewResponse> => {
    return api.get('/admin/dashboard/ability-radar-overview', { params: { days } })
  },
  getAiUsageOverview: (days = 30, limit = 20): Promise<AdminAiUsageOverviewResponse> => {
    return api.get('/admin/dashboard/ai-usage-overview', { params: { days, limit } })
  },

  pageCourses: (params: { page: number; size: number; query?: string; category?: string; difficulty?: string; status?: string; teacherId?: number }) => {
    return api.get('/admin/courses', { params })
  },
  getCourse: (id: string | number): Promise<AdminCourseDetailResponse> => api.get(`/admin/courses/${id}`),
  pageCourseStudents: (courseId: string | number, params: { page: number; size: number; search?: string; sortBy?: string; activity?: string; grade?: string; progress?: string }) => {
    return api.get(`/admin/courses/${courseId}/students`, { params })
  },

  pageUsers: (params: { page: number; size: number; keyword?: string; role?: string; status?: string; includeDeleted?: boolean }): Promise<PageResult<AdminUserListItem>> => {
    return api.get('/admin/users', { params })
  },
  createUser: (data: AdminUserCreateRequest): Promise<any> => api.post('/admin/users', data),
  updateUserRole: (id: string | number, role: string): Promise<void> => api.put(`/admin/users/${id}/role`, { role }),
  updateUserStatus: (id: string | number, status: string): Promise<void> => api.put(`/admin/users/${id}/status`, { status }),
  sendResetPasswordEmail: (id: string | number, lang = 'zh-CN'): Promise<void> => api.post(`/admin/users/${id}/password/reset-email`, undefined, { params: { lang } }),

  getStudentDetail: (id: string | number) => api.get(`/admin/people/students/${id}`),
  getTeacherDetail: (id: string | number) => api.get(`/admin/people/teachers/${id}`),

  pageAbilityReports: (params: any): Promise<PageResult<AbilityReport>> => api.get('/admin/ability-reports', { params }),
  getAbilityReport: (id: string | number): Promise<AbilityReport> => api.get(`/admin/ability-reports/${id}`),

  getAnalyticsSeriesOverview: (days = 30): Promise<any> => api.get('/admin/analytics/series/overview', { params: { days } }),

  pageCommunityPosts: (params: any): Promise<PageResult<any>> => api.get('/admin/community/posts', { params }),
  moderatePost: (id: string | number, data: any): Promise<void> => api.put(`/admin/community/posts/${id}`, data),
  pageCommunityComments: (params: any): Promise<PageResult<any>> => api.get('/admin/community/comments', { params }),
  moderateComment: (id: string | number, data: any): Promise<void> => api.put(`/admin/community/comments/${id}`, data),

  // 举报（复用现有 /reports 管理员接口）
  pageReports: (params: { status?: string; page?: number; size?: number }): Promise<PageResult<ReportEntity>> => api.get('/reports', { params }),
  updateReportStatus: (id: string | number, status: string): Promise<void> => api.put(`/reports/${id}/status`, undefined, { params: { status } }),

  // 导出（CSV Blob）
  exportUsersCsv: (params: any): Promise<Blob> => api.get('/admin/exports/users.csv', { params, responseType: 'blob' as any }),
  exportAbilityReportsCsv: (params: any): Promise<Blob> => api.get('/admin/exports/ability-reports.csv', { params, responseType: 'blob' as any }),
  exportCourseStudentsCsv: (params: { courseId: string | number; search?: string; sortBy?: string; activity?: string; grade?: string; progress?: string }): Promise<Blob> =>
    api.get('/admin/exports/course-students.csv', { params, responseType: 'blob' as any }),
  exportAiConversationsCsv: (params: { studentId: string | number; q?: string; pinned?: boolean; archived?: boolean }): Promise<Blob> =>
    api.get('/admin/exports/ai-conversations.csv', { params, responseType: 'blob' as any }),
  exportVoiceSessionsCsv: (params: { studentId: string | number; q?: string }): Promise<Blob> =>
    api.get('/admin/exports/voice-sessions.csv', { params, responseType: 'blob' as any }),
  getExportCapabilities: (): Promise<any> => api.get('/admin/exports/capabilities'),

  // 能力雷达（管理员版）
  getAbilityRadar: (params: { studentId: string | number; courseId: string | number; classId?: string | number; startDate?: string; endDate?: string }) =>
    api.get('/admin/ability/radar', { params }),
  compareAbilityRadar: (body: any, params?: { studentId?: string | number }) =>
    api.post('/admin/ability/radar/compare', body, { params }),

  // 行为洞察（管理员版）
  getBehaviorInsightLatest: (params: { studentId: string | number; courseId?: string | number; range?: string }) =>
    api.get('/admin/behavior/insights', { params }),
  generateBehaviorInsight: (params: { studentId: string | number; courseId?: string | number; range?: string; model?: string; force?: boolean }) =>
    api.post('/admin/behavior/insights/generate', null, { params }),

  // AI 问答审计（管理员版）
  listAiConversations: (params: { studentId: string | number; q?: string; pinned?: boolean; archived?: boolean; page?: number; size?: number }) =>
    api.get('/admin/ai/conversations', { params }),
  getAiConversation: (conversationId: string | number, params: { studentId: string | number }) =>
    api.get(`/admin/ai/conversations/${conversationId}`, { params }),
  listAiMessages: (conversationId: string | number, params: { studentId: string | number; page?: number; size?: number }) =>
    api.get(`/admin/ai/conversations/${conversationId}/messages`, { params }),

  // 口语训练审计（管理员版）
  listVoiceSessions: (params: { studentId: string | number; q?: string; page?: number; size?: number }) =>
    api.get('/admin/ai/voice/sessions', { params }),
  getVoiceSession: (sessionId: string | number, params: { studentId: string | number }) =>
    api.get(`/admin/ai/voice/sessions/${sessionId}`, { params }),
  listVoiceTurns: (sessionId: string | number, params: { studentId: string | number; page?: number; size?: number }) =>
    api.get(`/admin/ai/voice/sessions/${sessionId}/turns`, { params }),
}

