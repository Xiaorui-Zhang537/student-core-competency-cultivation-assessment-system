import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  AbilityDashboard,
  AbilityReport,
  AbilityTrend,
  AbilityGoal,
  LearningRecommendation,
  AbilityAssessment,
  AbilityDimension
} from '@/types/ability'
import * as abilityApi from '@/api/ability.api'
import { useUIStore } from './ui'

export const useAbilityStore = defineStore('ability', () => {
  const uiStore = useUIStore()

  // 状态
  const dashboard = ref<AbilityDashboard | null>(null)
  const currentReport = ref<AbilityReport | null>(null)
  const reportHistory = ref<Pick<AbilityReport, 'id' | 'reportDate' | 'overallScore'>[]>([])
  const trends = ref<AbilityTrend[]>([])
  const goals = ref<AbilityGoal[]>([])
  const recommendations = ref<LearningRecommendation[]>([])
  const assessments = ref<AbilityAssessment[]>([])
  const dimensions = ref<AbilityDimension[]>([])
  const loading = ref(false)
  const selectedTimeRange = ref<'1month' | '3months' | '6months' | '1year'>('6months')
  const selectedDimension = ref<string | null>(null)

  // 计算属性
  const overallProgress = computed(() => dashboard.value?.overallProgress || 0)
  
  const latestScores = computed(() => dashboard.value?.dimensions || [])
  
  const strongestDimensions = computed(() => 
    latestScores.value
      .slice()
      .sort((a, b) => b.score - a.score)
      .slice(0, 3)
  )
  
  const weakestDimensions = computed(() =>
    latestScores.value
      .slice()
      .sort((a, b) => a.score - b.score)
      .slice(0, 3)
  )
  
  const activeGoals = computed(() => goals.value.filter(goal => goal.status === 'active'))
  
  const completedGoals = computed(() => goals.value.filter(goal => goal.status === 'completed'))
  
  const highPriorityRecommendations = computed(() =>
    recommendations.value.filter(rec => rec.priority === 'high')
  )
  
  const progressTrend = computed(() => {
    if (trends.value.length === 0) return 'stable'
    
    const recentData = trends.value[0]?.data || []
    if (recentData.length < 2) return 'stable'
    
    const latest = recentData[recentData.length - 1]?.score || 0
    const previous = recentData[recentData.length - 2]?.score || 0
    
    if (latest > previous + 5) return 'improving'
    if (latest < previous - 5) return 'declining'
    return 'stable'
  })

  // Actions
  const fetchDashboard = async () => {
    loading.value = true
    try {
      dashboard.value = await abilityApi.getAbilityDashboard()
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '获取数据失败',
        message: '获取能力仪表板数据失败'
      })
      console.error('Failed to fetch ability dashboard:', error)
    } finally {
      loading.value = false
    }
  }

  const fetchReport = async (reportId?: string) => {
    loading.value = true
    try {
      currentReport.value = await abilityApi.getAbilityReport(reportId)
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '获取数据失败',
        message: '获取能力报告失败'
      })
      console.error('Failed to fetch ability report:', error)
    } finally {
      loading.value = false
    }
  }

  const fetchReportHistory = async (page: number = 1, limit: number = 10) => {
    try {
      const response = await abilityApi.getAbilityReportHistory(page, limit)
      reportHistory.value = response.items
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '获取数据失败',
        message: '获取报告历史失败'
      })
      console.error('Failed to fetch report history:', error)
    }
  }

  const fetchTrends = async (dimensionId?: string, timeRange?: '1month' | '3months' | '6months' | '1year') => {
    loading.value = true
    try {
      const range = timeRange || selectedTimeRange.value
      trends.value = await abilityApi.getAbilityTrends(dimensionId, range)
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '获取数据失败',
        message: '获取趋势数据失败'
      })
      console.error('Failed to fetch trends:', error)
    } finally {
      loading.value = false
    }
  }

  const fetchGoals = async () => {
    try {
      goals.value = await abilityApi.getAbilityGoals()
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '获取数据失败',
        message: '获取学习目标失败'
      })
      console.error('Failed to fetch goals:', error)
    }
  }

  const fetchRecommendations = async (dimensionId?: string, limit: number = 10) => {
    try {
      recommendations.value = await abilityApi.getLearningRecommendations(dimensionId, limit)
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '获取数据失败',
        message: '获取学习建议失败'
      })
      console.error('Failed to fetch recommendations:', error)
    }
  }

  const fetchDimensions = async () => {
    try {
      dimensions.value = await abilityApi.getAbilityDimensions()
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '获取数据失败',
        message: '获取能力维度失败'
      })
      console.error('Failed to fetch dimensions:', error)
    }
  }

  const createGoal = async (goalData: Omit<AbilityGoal, 'id' | 'createdAt' | 'updatedAt'>) => {
    try {
      const newGoal = await abilityApi.createAbilityGoal(goalData)
      goals.value.push(newGoal)
      uiStore.showNotification({
        type: 'success',
        title: '操作成功',
        message: '学习目标创建成功'
      })
      return newGoal
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '操作失败',
        message: '创建学习目标失败'
      })
      console.error('Failed to create goal:', error)
      throw error
    }
  }

  const updateGoal = async (goalId: string, goalData: Partial<AbilityGoal>) => {
    try {
      const updatedGoal = await abilityApi.updateAbilityGoal(goalId, goalData)
      const index = goals.value.findIndex(g => g.id === goalId)
      if (index !== -1) {
        goals.value[index] = updatedGoal
      }
      uiStore.showNotification({
        type: 'success',
        title: '操作成功',
        message: '学习目标更新成功'
      })
      return updatedGoal
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '操作失败',
        message: '更新学习目标失败'
      })
      console.error('Failed to update goal:', error)
      throw error
    }
  }

  const deleteGoal = async (goalId: string) => {
    try {
      await abilityApi.deleteAbilityGoal(goalId)
      const index = goals.value.findIndex(g => g.id === goalId)
      if (index !== -1) {
        goals.value.splice(index, 1)
      }
      uiStore.showNotification({
        type: 'success',
        title: '操作成功',
        message: '学习目标删除成功'
      })
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '操作失败',
        message: '删除学习目标失败'
      })
      console.error('Failed to delete goal:', error)
      throw error
    }
  }

  const submitSelfAssessment = async (assessmentData: {
    dimensionId: string
    score: number
    feedback?: string
  }) => {
    try {
      const assessment = await abilityApi.submitSelfAssessment(assessmentData)
      assessments.value.unshift(assessment)
      uiStore.showNotification({
        type: 'success',
        title: '操作成功',
        message: '自评提交成功'
      })
      
      // 刷新仪表板数据
      await fetchDashboard()
      return assessment
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '操作失败',
        message: '提交自评失败'
      })
      console.error('Failed to submit self assessment:', error)
      throw error
    }
  }

  const exportReport = async (reportId: string, format: 'pdf' | 'excel' = 'pdf') => {
    try {
      const blob = await abilityApi.exportAbilityReport(reportId, format)
      
      // 创建下载链接
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `ability-report-${reportId}.${format}`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      
      uiStore.showNotification({
        type: 'success',
        title: '操作成功',
        message: '报告导出成功'
      })
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '操作失败',
        message: '导出报告失败'
      })
      console.error('Failed to export report:', error)
      throw error
    }
  }

  const setTimeRange = (range: '1month' | '3months' | '6months' | '1year') => {
    selectedTimeRange.value = range
    fetchTrends(selectedDimension.value || undefined, range)
  }

  const setSelectedDimension = (dimensionId: string | null) => {
    selectedDimension.value = dimensionId
    fetchTrends(dimensionId || undefined, selectedTimeRange.value)
    if (dimensionId) {
      fetchRecommendations(dimensionId)
    }
  }

  const initAbilityData = async () => {
    await Promise.all([
      fetchDashboard(),
      fetchTrends(),
      fetchGoals(),
      fetchRecommendations(),
      fetchDimensions()
    ])
  }

  return {
    // 状态
    dashboard,
    currentReport,
    reportHistory,
    trends,
    goals,
    recommendations,
    assessments,
    dimensions,
    loading,
    selectedTimeRange,
    selectedDimension,
    
    // 计算属性
    overallProgress,
    latestScores,
    strongestDimensions,
    weakestDimensions,
    activeGoals,
    completedGoals,
    highPriorityRecommendations,
    progressTrend,
    
    // Actions
    fetchDashboard,
    fetchReport,
    fetchReportHistory,
    fetchTrends,
    fetchGoals,
    fetchRecommendations,
    fetchDimensions,
    createGoal,
    updateGoal,
    deleteGoal,
    submitSelfAssessment,
    exportReport,
    setTimeRange,
    setSelectedDimension,
    initAbilityData
  }
}) 