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

  pageCourses: (params: { page: number; size: number; query?: string; category?: string; difficulty?: string; status?: string; teacherId?: number }) => {
    return api.get('/admin/courses', { params })
  },
  getCourse: (id: string | number): Promise<AdminCourseDetailResponse> => api.get(`/admin/courses/${id}`),

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
  getExportCapabilities: (): Promise<any> => api.get('/admin/exports/capabilities'),
}

