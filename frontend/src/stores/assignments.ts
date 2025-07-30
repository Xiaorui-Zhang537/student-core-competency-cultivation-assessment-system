import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Assignment, AssignmentSubmission } from '@/types/assignment'
import { assignmentsAPI } from '@/api/assignments.api'

// 本地类型定义
interface AssignmentFilter {
  status?: string
  courseId?: string
  search?: string
}

interface AssignmentStats {
  total: number
  completed: number
  pending: number
  average: number
}

export const useAssignmentsStore = defineStore('assignments', () => {
  // 状态
  const assignments = ref<Assignment[]>([])
  const currentAssignment = ref<Assignment | null>(null)
  const submissions = ref<AssignmentSubmission[]>([])
  const mySubmissions = ref<AssignmentSubmission[]>([])
  const stats = ref<AssignmentStats | null>(null)
  const loading = ref(false)
  const filter = ref<AssignmentFilter>({})

  // 计算属性
  const filteredAssignments = computed(() => {
    let result = assignments.value

    if (filter.value.status) {
      result = result.filter(assignment => assignment.status === filter.value.status)
    }

    if (filter.value.courseId) {
      result = result.filter(assignment => assignment.courseId === filter.value.courseId)
    }

    if (filter.value.search) {
      const search = filter.value.search.toLowerCase()
      result = result.filter(assignment => 
        assignment.title.toLowerCase().includes(search) ||
        assignment.description.toLowerCase().includes(search)
      )
    }

    return result
  })

  const pendingAssignments = computed(() => 
    assignments.value.filter((assignment: any) => assignment.status === 'pending')
  )

  const submittedAssignments = computed(() => 
    assignments.value.filter((assignment: any) => assignment.status === 'submitted')
  )

  const gradedAssignments = computed(() => 
    assignments.value.filter((assignment: any) => assignment.status === 'graded')
  )

  const averageScore = computed(() => {
    if (gradedAssignments.value.length === 0) return 0
    const total = gradedAssignments.value.reduce((sum: number, assignment: any) => {
      return sum + (assignment.score || 0)
    }, 0)
    return Math.round(total / gradedAssignments.value.length)
  })

  // 方法
  const fetchAssignments = async (params?: any) => {
    loading.value = true
    try {
      const response = await assignmentsAPI.getAssignments(params)
      assignments.value = response.data.items || response.data
      return response
    } catch (error) {
      console.error('获取作业列表失败:', error)
      assignments.value = []
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchMyAssignments = async () => {
    loading.value = true
    try {
      const response = await assignmentsAPI.getAssignments() // 使用现有方法
      assignments.value = response.data.items || response.data
      return response
    } catch (error) {
      console.error('获取我的作业失败:', error)
      assignments.value = []
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchAssignment = async (id: string) => {
    loading.value = true
    try {
      const response = await assignmentsAPI.getAssignment(id)
      currentAssignment.value = response.data
      return response
    } catch (error) {
      console.error('获取作业详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchStats = async () => {
    try {
      // 使用模拟数据或计算统计
      stats.value = {
        total: assignments.value.length,
        completed: gradedAssignments.value.length,
        pending: pendingAssignments.value.length,
        average: 85
      }
      return { data: stats.value }
    } catch (error) {
      console.error('获取作业统计失败:', error)
      throw error
    }
  }

  const submitAssignment = async (assignmentId: string, data: any) => {
    try {
      const response = await assignmentsAPI.submitAssignment(assignmentId, data)
      
      // 更新本地作业状态
      const assignmentIndex = assignments.value.findIndex(a => a.id === assignmentId)
      if (assignmentIndex !== -1) {
        (assignments.value[assignmentIndex] as any).status = 'submitted'
      }
      
      return response
    } catch (error) {
      console.error('提交作业失败:', error)
      throw error
    }
  }

  const fetchMySubmissions = async () => {
    try {
      // 使用现有的 submissions 或调用相关 API
      mySubmissions.value = submissions.value
      return { data: mySubmissions.value }
    } catch (error) {
      console.error('获取我的提交失败:', error)
      throw error
    }
  }

  const saveDraft = async (assignmentId: string, content: string) => {
    try {
      // 这里可以调用 API 保存草稿，暂时用本地存储模拟
      const draftData = {
        id: Date.now().toString(),
        assignmentId,
        content,
        status: 'draft',
        submittedAt: new Date().toISOString()
      }
      
      // 模拟保存到服务器
      console.log('保存草稿:', draftData)
      return { data: draftData }
    } catch (error) {
      console.error('保存草稿失败:', error)
      throw error
    }
  }

  const fetchGrades = async () => {
    try {
      // 获取已评分的作业，这里可以调用相关的API
      await fetchAssignments({ status: 'graded' })
      return { data: gradedAssignments.value }
    } catch (error) {
      console.error('获取成绩失败:', error)
      throw error
    }
  }

  const updateFilter = (newFilter: Partial<AssignmentFilter>) => {
    filter.value = { ...filter.value, ...newFilter }
  }

  const clearFilter = () => {
    filter.value = {}
  }

  return {
    // 状态
    assignments,
    currentAssignment,
    submissions,
    mySubmissions,
    stats,
    loading,
    filter,
    // 计算属性
    filteredAssignments,
    pendingAssignments,
    submittedAssignments,
    gradedAssignments,
    averageScore,
    // 方法
    fetchAssignments,
    fetchMyAssignments,
    fetchAssignment,
    fetchMySubmissions,
    fetchGrades,
    fetchStats,
    submitAssignment,
    saveDraft,
    updateFilter,
    clearFilter
  }
}) 