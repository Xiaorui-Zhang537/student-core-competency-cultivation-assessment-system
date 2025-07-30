import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { StudentProfile, Course, Assignment } from '../types/student'
import { api } from '@/api/config'

export const useStudentStore = defineStore('student', () => {
  // 状态
  const loading = ref(false)
  const profile = ref<StudentProfile | null>(null)
  const activeCourses = ref(0)
  const pendingAssignments = ref(0)
  const averageScore = ref(0)
  const totalStudyTime = ref(0) // 分钟
  const weeklyStudyTime = ref(0) // 分钟
  const overallProgress = ref(0)
  const recentCourses = ref<Course[]>([])
  const assignments = ref<Assignment[]>([])

  // 初始化学生数据
  const initStudentData = async () => {
    loading.value = true
    try {
      // 获取学生概况数据
      const response = await api.get('/student/dashboard')
      const data = response.data as any
      
      activeCourses.value = data.activeCourses || 0
      pendingAssignments.value = data.pendingAssignments || 0
      averageScore.value = data.averageScore || 0
      totalStudyTime.value = data.totalStudyTime || 0
      weeklyStudyTime.value = data.weeklyStudyTime || 0
      overallProgress.value = data.overallProgress || 0
      
    } catch (error) {
      console.error('初始化学生数据失败:', error)
      // 发生错误时设置默认值
      activeCourses.value = 0
      pendingAssignments.value = 0
      averageScore.value = 0
      totalStudyTime.value = 0
      weeklyStudyTime.value = 0
      overallProgress.value = 0
    } finally {
      loading.value = false
    }
  }

  // 获取学生简介
  const fetchProfile = async () => {
    try {
      const response = await api.get('/student/profile')
      profile.value = response.data as StudentProfile
    } catch (error) {
      console.error('获取学生简介失败:', error)
    }
  }

  // 获取课程列表
  const fetchCourses = async () => {
    try {
      const response = await api.get('/student/courses')
      recentCourses.value = response.data as Course[]
    } catch (error) {
      console.error('获取课程列表失败:', error)
      recentCourses.value = []
    }
  }

  // 获取作业列表
  const fetchAssignments = async () => {
    try {
      const response = await api.get('/student/assignments')
      assignments.value = response.data as Assignment[]
    } catch (error) {
      console.error('获取作业列表失败:', error)
      assignments.value = []
    }
  }

  // 更新学习进度
  const updateProgress = async (courseId: string, progress: number) => {
    try {
      await api.put(`/student/courses/${courseId}/progress`, { progress })
      const course = recentCourses.value.find((c: Course) => c.id === courseId)
      if (course) {
        course.progress = progress
      }
    } catch (error) {
      console.error('更新学习进度失败:', error)
    }
  }

  return {
    // 状态
    loading,
    profile,
    activeCourses,
    pendingAssignments,
    averageScore,
    totalStudyTime,
    weeklyStudyTime,
    overallProgress,
    recentCourses,
    assignments,
    
    // 方法
    initStudentData,
    fetchProfile,
    fetchCourses,
    fetchAssignments,
    updateProgress
  }
}) 