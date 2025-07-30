import apiClient from './config'
import type {
  TeacherDashboard,
  TeacherProfile,
  TeacherStats,
  StudentOverview,
  CourseAnalytics,
  AssignmentAnalytics,
  GradingTask,
  ClassPerformance,
  TeacherNotification,
  TeacherSchedule,
  TeacherCourseManagement,
  StudentProgressReport
} from '@/types/teacher'
import type { ApiResponse, PaginatedResponse } from '@/types/api'

/**
 * 获取教师仪表板数据
 */
export const getTeacherDashboard = async (): Promise<TeacherDashboard> => {
  const response = await apiClient.get<ApiResponse<TeacherDashboard>>('/api/teacher/dashboard')
  return response.data.data
}

/**
 * 获取教师统计数据
 */
export const getTeacherStats = async (): Promise<TeacherStats> => {
  const response = await apiClient.get<ApiResponse<TeacherStats>>('/api/teacher/stats')
  return response.data.data
}

/**
 * 获取教师个人信息
 */
export const getTeacherProfile = async (): Promise<TeacherProfile> => {
  const response = await apiClient.get<ApiResponse<TeacherProfile>>('/api/teacher/profile')
  return response.data.data
}

/**
 * 更新教师个人信息
 */
export const updateTeacherProfile = async (profileData: Partial<TeacherProfile>): Promise<TeacherProfile> => {
  const response = await apiClient.put<ApiResponse<TeacherProfile>>('/api/teacher/profile', profileData)
  return response.data.data
}

/**
 * 获取学生概览列表
 * @param courseId 课程ID（可选，筛选特定课程的学生）
 * @param status 学生状态筛选
 * @param page 页码
 * @param limit 每页数量
 */
export const getStudentOverview = async (
  courseId?: string,
  status?: 'active' | 'inactive' | 'at_risk',
  page: number = 1,
  limit: number = 20
): Promise<PaginatedResponse<StudentOverview>> => {
  const params = new URLSearchParams()
  if (courseId) params.append('courseId', courseId)
  if (status) params.append('status', status)
  params.append('page', page.toString())
  params.append('limit', limit.toString())

  const response = await apiClient.get<PaginatedResponse<StudentOverview>>(`/api/teacher/students?${params}`)
  return response.data
}

/**
 * 获取特定学生的详细进度报告
 */
export const getStudentProgressReport = async (
  studentId: string,
  courseId?: string
): Promise<StudentProgressReport> => {
  const params = courseId ? `?courseId=${courseId}` : ''
  const response = await apiClient.get<ApiResponse<StudentProgressReport>>(`/api/teacher/students/${studentId}/progress${params}`)
  return response.data.data
}

/**
 * 获取课程分析数据
 * @param courseId 课程ID
 * @param timeRange 时间范围
 */
export const getCourseAnalytics = async (
  courseId: string,
  timeRange: '1week' | '1month' | '3months' | '1year' = '1month'
): Promise<CourseAnalytics> => {
  const response = await apiClient.get<ApiResponse<CourseAnalytics>>(`/api/teacher/courses/${courseId}/analytics?timeRange=${timeRange}`)
  return response.data.data
}

/**
 * 获取教师所有课程的管理数据
 */
export const getTeacherCourses = async (): Promise<TeacherCourseManagement[]> => {
  const response = await apiClient.get<ApiResponse<TeacherCourseManagement[]>>('/api/teacher/courses')
  return response.data.data
}

/**
 * 获取作业分析数据
 */
export const getAssignmentAnalytics = async (
  assignmentId: string
): Promise<AssignmentAnalytics> => {
  const response = await apiClient.get<ApiResponse<AssignmentAnalytics>>(`/api/teacher/assignments/${assignmentId}/analytics`)
  return response.data.data
}

/**
 * 获取班级整体表现数据
 */
export const getClassPerformance = async (
  courseId: string,
  timeRange: '1week' | '1month' | '1semester' | '1year' = '1month'
): Promise<ClassPerformance> => {
  const response = await apiClient.get<ApiResponse<ClassPerformance>>(`/api/teacher/courses/${courseId}/performance?timeRange=${timeRange}`)
  return response.data.data
}

/**
 * 获取评分任务列表
 * @param status 任务状态筛选
 * @param priority 优先级筛选
 * @param courseId 课程筛选
 * @param page 页码
 * @param limit 每页数量
 */
export const getGradingTasks = async (
  status?: 'pending' | 'in_progress' | 'completed',
  priority?: 'low' | 'medium' | 'high',
  courseId?: string,
  page: number = 1,
  limit: number = 20
): Promise<PaginatedResponse<GradingTask>> => {
  const params = new URLSearchParams()
  if (status) params.append('status', status)
  if (priority) params.append('priority', priority)
  if (courseId) params.append('courseId', courseId)
  params.append('page', page.toString())
  params.append('limit', limit.toString())

  const response = await apiClient.get<PaginatedResponse<GradingTask>>(`/api/teacher/grading/tasks?${params}`)
  return response.data
}

/**
 * 开始评分任务
 */
export const startGradingTask = async (taskId: string): Promise<GradingTask> => {
  const response = await apiClient.post<ApiResponse<GradingTask>>(`/api/teacher/grading/tasks/${taskId}/start`)
  return response.data.data
}

/**
 * 提交评分结果
 */
export const submitGrade = async (taskId: string, gradeData: {
  score: number
  feedback: string
  strengths?: string[]
  improvements?: string[]
  rubricScores?: Record<string, number>
}): Promise<GradingTask> => {
  const response = await apiClient.post<ApiResponse<GradingTask>>(`/api/teacher/grading/tasks/${taskId}/submit`, gradeData)
  return response.data.data
}

/**
 * 批量评分
 */
export const batchGrade = async (grades: Array<{
  taskId: string
  score: number
  feedback: string
}>): Promise<GradingTask[]> => {
  const response = await apiClient.post<ApiResponse<GradingTask[]>>('/api/teacher/grading/batch', { grades })
  return response.data.data
}

/**
 * 获取AI评分建议
 */
export const getAIGradingSuggestion = async (
  submissionId: string
): Promise<{
  suggestedScore: number
  confidence: number
  strengths: string[]
  improvements: string[]
  feedback: string
  rubricAnalysis?: Record<string, { score: number; reasoning: string }>
}> => {
  const response = await apiClient.get<ApiResponse<any>>(`/api/teacher/grading/ai-suggestion/${submissionId}`)
  return response.data.data
}

/**
 * 获取教师通知
 */
export const getTeacherNotifications = async (
  page: number = 1,
  limit: number = 20,
  unreadOnly: boolean = false
): Promise<PaginatedResponse<TeacherNotification>> => {
  const params = new URLSearchParams()
  params.append('page', page.toString())
  params.append('limit', limit.toString())
  if (unreadOnly) params.append('unreadOnly', 'true')

  const response = await apiClient.get<PaginatedResponse<TeacherNotification>>(`/api/teacher/notifications?${params}`)
  return response.data
}

/**
 * 标记通知为已读
 */
export const markNotificationAsRead = async (notificationId: string): Promise<void> => {
  await apiClient.patch(`/api/teacher/notifications/${notificationId}/read`)
}

/**
 * 批量标记通知为已读
 */
export const markAllNotificationsAsRead = async (): Promise<void> => {
  await apiClient.patch('/api/teacher/notifications/mark-all-read')
}

/**
 * 获取教师日程安排
 */
export const getTeacherSchedule = async (
  startDate: string,
  endDate: string
): Promise<TeacherSchedule[]> => {
  const params = new URLSearchParams()
  params.append('startDate', startDate)
  params.append('endDate', endDate)

  const response = await apiClient.get<ApiResponse<TeacherSchedule[]>>(`/api/teacher/schedule?${params}`)
  return response.data.data
}

/**
 * 创建日程安排
 */
export const createSchedule = async (scheduleData: Omit<TeacherSchedule, 'id'>): Promise<TeacherSchedule> => {
  const response = await apiClient.post<ApiResponse<TeacherSchedule>>('/api/teacher/schedule', scheduleData)
  return response.data.data
}

/**
 * 更新日程安排
 */
export const updateSchedule = async (scheduleId: string, scheduleData: Partial<TeacherSchedule>): Promise<TeacherSchedule> => {
  const response = await apiClient.put<ApiResponse<TeacherSchedule>>(`/api/teacher/schedule/${scheduleId}`, scheduleData)
  return response.data.data
}

/**
 * 删除日程安排
 */
export const deleteSchedule = async (scheduleId: string): Promise<void> => {
  await apiClient.delete(`/api/teacher/schedule/${scheduleId}`)
}

/**
 * 导出学生成绩报告
 */
export const exportStudentGrades = async (
  courseId: string,
  format: 'excel' | 'pdf' = 'excel',
  studentIds?: string[]
): Promise<Blob> => {
  const params = new URLSearchParams()
  params.append('format', format)
  if (studentIds?.length) {
    studentIds.forEach(id => params.append('studentIds', id))
  }

  const response = await apiClient.get(`/api/teacher/courses/${courseId}/export-grades?${params}`, {
    responseType: 'blob'
  })
  return response.data
}

/**
 * 导出课程分析报告
 */
export const exportCourseAnalytics = async (
  courseId: string,
  format: 'excel' | 'pdf' = 'pdf'
): Promise<Blob> => {
  const response = await apiClient.get(`/api/teacher/courses/${courseId}/export-analytics?format=${format}`, {
    responseType: 'blob'
  })
  return response.data
}

/**
 * 获取评分历史统计
 */
export const getGradingHistory = async (
  timeRange: '1week' | '1month' | '3months' | '1year' = '1month'
): Promise<{
  totalGraded: number
  averageTime: number
  gradingEfficiency: number
  feedbackQuality: number
  monthlyStats: {
    month: string
    graded: number
    averageScore: number
    averageTime: number
  }[]
}> => {
  const response = await apiClient.get<ApiResponse<any>>(`/api/teacher/grading/history?timeRange=${timeRange}`)
  return response.data.data
}

/**
 * 发送学生消息/通知
 */
export const sendStudentMessage = async (
  studentId: string,
  message: {
    subject: string
    content: string
    type: 'general' | 'assignment' | 'grade' | 'reminder'
    courseId?: string
    assignmentId?: string
  }
): Promise<void> => {
  await apiClient.post(`/api/teacher/students/${studentId}/message`, message)
}

/**
 * 批量发送消息
 */
export const sendBulkMessage = async (
  studentIds: string[],
  message: {
    subject: string
    content: string
    type: 'general' | 'assignment' | 'grade' | 'reminder'
    courseId?: string
  }
): Promise<void> => {
  await apiClient.post('/api/teacher/students/bulk-message', {
    studentIds,
    ...message
  })
} 