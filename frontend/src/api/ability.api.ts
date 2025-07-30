import apiClient from './config'
import type { 
  AbilityDashboard,
  AbilityReport,
  AbilityTrend,
  AbilityGoal,
  LearningRecommendation,
  AbilityAssessment,
  AbilityDimension
} from '@/types/ability'
import type { ApiResponse, PaginatedResponse } from '@/types/api'

/**
 * 获取学生能力发展仪表板数据
 */
export const getAbilityDashboard = async (): Promise<AbilityDashboard> => {
  const response = await apiClient.get<ApiResponse<AbilityDashboard>>('/api/student/ability/dashboard')
  return response.data.data
}

/**
 * 获取详细的能力分析报告
 */
export const getAbilityReport = async (reportId?: string): Promise<AbilityReport> => {
  const url = reportId ? `/api/student/ability/report/${reportId}` : '/api/student/ability/report/latest'
  const response = await apiClient.get<ApiResponse<AbilityReport>>(url)
  return response.data.data
}

/**
 * 获取能力发展趋势数据
 * @param dimensionId 能力维度ID（可选，不传则获取全部维度）
 * @param timeRange 时间范围（可选，默认6个月）
 */
export const getAbilityTrends = async (
  dimensionId?: string,
  timeRange: '1month' | '3months' | '6months' | '1year' = '6months'
): Promise<AbilityTrend[]> => {
  const params = new URLSearchParams()
  if (dimensionId) params.append('dimensionId', dimensionId)
  params.append('timeRange', timeRange)
  
  const response = await apiClient.get<ApiResponse<AbilityTrend[]>>(`/api/student/ability/trends?${params}`)
  return response.data.data
}

/**
 * 获取学习建议
 * @param dimensionId 能力维度ID（可选）
 * @param limit 返回数量限制
 */
export const getLearningRecommendations = async (
  dimensionId?: string,
  limit: number = 10
): Promise<LearningRecommendation[]> => {
  const params = new URLSearchParams()
  if (dimensionId) params.append('dimensionId', dimensionId)
  params.append('limit', limit.toString())
  
  const response = await apiClient.get<ApiResponse<LearningRecommendation[]>>(`/api/student/ability/recommendations?${params}`)
  return response.data.data
}

/**
 * 获取学生的能力目标
 */
export const getAbilityGoals = async (): Promise<AbilityGoal[]> => {
  const response = await apiClient.get<ApiResponse<AbilityGoal[]>>('/api/student/ability/goals')
  return response.data.data
}

/**
 * 创建新的能力目标
 */
export const createAbilityGoal = async (goalData: Omit<AbilityGoal, 'id' | 'createdAt' | 'updatedAt'>): Promise<AbilityGoal> => {
  const response = await apiClient.post<ApiResponse<AbilityGoal>>('/api/student/ability/goals', goalData)
  return response.data.data
}

/**
 * 更新能力目标
 */
export const updateAbilityGoal = async (goalId: string, goalData: Partial<AbilityGoal>): Promise<AbilityGoal> => {
  const response = await apiClient.put<ApiResponse<AbilityGoal>>(`/api/student/ability/goals/${goalId}`, goalData)
  return response.data.data
}

/**
 * 删除能力目标
 */
export const deleteAbilityGoal = async (goalId: string): Promise<void> => {
  await apiClient.delete(`/api/student/ability/goals/${goalId}`)
}

/**
 * 获取能力评估历史
 */
export const getAbilityAssessments = async (
  dimensionId?: string,
  page: number = 1,
  limit: number = 20
): Promise<PaginatedResponse<AbilityAssessment>> => {
  const params = new URLSearchParams()
  if (dimensionId) params.append('dimensionId', dimensionId)
  params.append('page', page.toString())
  params.append('limit', limit.toString())
  
  const response = await apiClient.get<PaginatedResponse<AbilityAssessment>>(`/api/student/ability/assessments?${params}`)
  return response.data
}

/**
 * 提交自评
 */
export const submitSelfAssessment = async (assessmentData: {
  dimensionId: string
  score: number
  feedback?: string
}): Promise<AbilityAssessment> => {
  const response = await apiClient.post<ApiResponse<AbilityAssessment>>('/api/student/ability/self-assessment', assessmentData)
  return response.data.data
}

/**
 * 获取能力维度定义
 */
export const getAbilityDimensions = async (): Promise<AbilityDimension[]> => {
  const response = await apiClient.get<ApiResponse<AbilityDimension[]>>('/api/ability/dimensions')
  return response.data.data
}

/**
 * 获取历史报告列表
 */
export const getAbilityReportHistory = async (
  page: number = 1,
  limit: number = 10
): Promise<PaginatedResponse<Pick<AbilityReport, 'id' | 'reportDate' | 'overallScore'>>> => {
  const params = new URLSearchParams()
  params.append('page', page.toString())
  params.append('limit', limit.toString())
  
  const response = await apiClient.get<PaginatedResponse<Pick<AbilityReport, 'id' | 'reportDate' | 'overallScore'>>>(`/api/student/ability/reports?${params}`)
  return response.data
}

/**
 * 导出能力报告
 */
export const exportAbilityReport = async (reportId: string, format: 'pdf' | 'excel' = 'pdf'): Promise<Blob> => {
  const response = await apiClient.get(`/api/student/ability/report/${reportId}/export?format=${format}`, {
    responseType: 'blob'
  })
  return response.data
} 