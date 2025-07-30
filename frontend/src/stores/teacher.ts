import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
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
import * as teacherApi from '@/api/teacher.api'
import { useUIStore } from './ui'

export const useTeacherStore = defineStore('teacher', () => {
  const uiStore = useUIStore()

  // 状态
  const dashboard = ref<TeacherDashboard | null>(null)
  const profile = ref<TeacherProfile | null>(null)
  const stats = ref<TeacherStats | null>(null)
  const students = ref<StudentOverview[]>([])
  const courses = ref<TeacherCourseManagement[]>([])
  const gradingTasks = ref<GradingTask[]>([])
  const notifications = ref<TeacherNotification[]>([])
  const schedule = ref<TeacherSchedule[]>([])
  const courseAnalytics = ref<Record<string, CourseAnalytics>>({})
  const classPerformance = ref<Record<string, ClassPerformance>>({})
  const currentStudentReport = ref<StudentProgressReport | null>(null)
  const loading = ref(false)
  const gradingLoading = ref(false)

  // 筛选状态
  const studentFilter = ref({
    courseId: '',
    status: '' as '' | 'active' | 'inactive' | 'at_risk',
    search: ''
  })

  const gradingFilter = ref({
    status: '' as '' | 'pending' | 'in_progress' | 'completed',
    priority: '' as '' | 'low' | 'medium' | 'high',
    courseId: ''
  })

  // 初始化Mock数据
  const initializeMockData = () => {
    
    if (!profile.value) {
      profile.value = {
        id: '1',
        userId: '1',
        teacherCode: 'T001',
        name: '张老师',
        email: 'teacher@example.com',
        department: '计算机科学系',
        subject: ['前端开发', 'JavaScript'],
        title: '高级讲师',
        education: '硕士研究生',
        experience: 5,
        rating: 4.8,
        reviewCount: 120,
        joinDate: '2020-01-01',
        status: 'active',
        permissions: ['course_manage', 'student_manage']
      }
    }

    if (!stats.value) {
      stats.value = {
        totalCourses: 8,
        activeCourses: 6,
        totalStudents: 156,
        activeStudents: 142,
        pendingAssignments: 23,
        gradedAssignments: 89,
        averageRating: 4.8,
        teachingHours: 240,
        onlineHours: 180,
        responseTime: 2.5
      }
    }

    if (courses.value.length === 0) {
      courses.value = [
        {
          courseId: '1',
          courseName: 'Vue.js 前端开发',
          status: 'published',
          enrolledStudents: [],
          analytics: {
            courseId: '1',
            courseName: 'Vue.js 前端开发',
            enrollmentCount: 0,
            completionRate: 0,
            averageProgress: 0,
            averageScore: 0,
            engagementRate: 0,
            dropoutRate: 0,
            studentFeedback: { rating: 0, reviewCount: 0, satisfaction: 0 },
            weeklyProgress: [],
          },
          pendingTasks: [],
          recentActivity: [],
        },
      ];
    }
  }

  // 页面加载时初始化Mock数据
  initializeMockData()

  // 计算属性
  const totalStudents = computed(() => stats.value?.totalStudents || 0)
  const activeCourses = computed(() => stats.value?.activeCourses || 0)
  const pendingGradingCount = computed(() => stats.value?.pendingAssignments || 0)
  const averageRating = computed(() => stats.value?.averageRating || 0)

  const filteredStudents = computed(() => {
    let filtered = students.value

    if (studentFilter.value.courseId) {
      // 注意：这里需要根据实际数据结构调整筛选逻辑
      filtered = filtered.filter(student => {
        // 假设学生数据中包含课程信息，实际实现需要根据API返回的数据结构调整
        return true // 临时返回true，实际需要实现课程筛选逻辑
      })
    }

    if (studentFilter.value.status) {
      filtered = filtered.filter(student => student.status === studentFilter.value.status)
    }

    if (studentFilter.value.search) {
      const search = studentFilter.value.search.toLowerCase()
      filtered = filtered.filter(student => 
        student.name.toLowerCase().includes(search) ||
        student.studentCode.toLowerCase().includes(search) ||
        student.major.toLowerCase().includes(search)
      )
    }

    return filtered
  })

  const filteredGradingTasks = computed(() => {
    let filtered = gradingTasks.value

    if (gradingFilter.value.status) {
      filtered = filtered.filter(task => task.status === gradingFilter.value.status)
    }

    if (gradingFilter.value.priority) {
      filtered = filtered.filter(task => task.priority === gradingFilter.value.priority)
    }

    if (gradingFilter.value.courseId) {
      filtered = filtered.filter(task => task.courseId === gradingFilter.value.courseId)
    }

    return filtered.sort((a, b) => {
      // 按优先级和截止时间排序
      const priorityOrder = { high: 3, medium: 2, low: 1 }
      const priorityDiff = priorityOrder[b.priority] - priorityOrder[a.priority]
      if (priorityDiff !== 0) return priorityDiff
      
      return new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime()
    })
  })

  const unreadNotifications = computed(() => 
    notifications.value.filter(n => !n.isRead)
  )

  const urgentNotifications = computed(() => 
    unreadNotifications.value.filter(n => n.priority === 'high' && n.actionRequired)
  )

  const atRiskStudents = computed(() => 
    students.value.filter(student => student.status === 'at_risk')
  )

  const todaySchedule = computed(() => {
    const today = new Date().toISOString().split('T')[0]
    return schedule.value.filter(item => item.startTime.startsWith(today))
  })

  // Actions
  const fetchDashboard = async () => {
    loading.value = true
    try {
      dashboard.value = await teacherApi.getTeacherDashboard()
      profile.value = dashboard.value.teacher
      stats.value = dashboard.value.stats
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '加载失败',
        message: '获取仪表板数据失败'
      })
      console.error('Failed to fetch teacher dashboard:', error)
    } finally {
      loading.value = false
    }
  }

  const fetchProfile = async () => {
    try {
      profile.value = await teacherApi.getTeacherProfile()
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '加载失败',
        message: '获取教师信息失败'
      })
      console.error('Failed to fetch teacher profile:', error)
    }
  }

  const updateProfile = async (profileData: Partial<TeacherProfile>) => {
    try {
      profile.value = await teacherApi.updateTeacherProfile(profileData)
      uiStore.showNotification({
        type: 'success',
        title: '更新成功',
        message: '教师信息已更新'
      })
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '更新失败',
        message: '教师信息更新失败'
      })
      console.error('Failed to update teacher profile:', error)
      throw error
    }
  }

  const fetchStudents = async (courseId?: string, status?: 'active' | 'inactive' | 'at_risk') => {
    try {
      const response = await teacherApi.getStudentOverview(courseId, status)
      students.value = response.items
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '加载失败',
        message: '获取学生信息失败'
      })
      console.error('Failed to fetch students:', error)
    }
  }

  const fetchStudentProgress = async (studentId: string, courseId?: string) => {
    try {
      currentStudentReport.value = await teacherApi.getStudentProgressReport(studentId, courseId)
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '加载失败',
        message: '获取学生进度报告失败'
      })
      console.error('Failed to fetch student progress:', error)
    }
  }

  const fetchCourses = async () => {
    try {
      courses.value = await teacherApi.getTeacherCourses()
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '加载失败',
        message: '获取课程信息失败'
      })
      console.error('Failed to fetch courses:', error)
    }
  }

  const fetchCourseAnalytics = async (courseId: string, timeRange?: '1week' | '1month' | '3months' | '1year') => {
    try {
      const analytics = await teacherApi.getCourseAnalytics(courseId, timeRange)
      courseAnalytics.value[courseId] = analytics
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '加载失败',
        message: '获取课程分析数据失败'
      })
      console.error('Failed to fetch course analytics:', error)
    }
  }

  const fetchClassPerformance = async (courseId: string, timeRange?: '1week' | '1month' | '1semester' | '1year') => {
    try {
      const performance = await teacherApi.getClassPerformance(courseId, timeRange)
      classPerformance.value[courseId] = performance
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '加载失败',
        message: '获取班级表现数据失败'
      })
      console.error('Failed to fetch class performance:', error)
    }
  }

  const fetchGradingTasks = async () => {
    gradingLoading.value = true
    try {
      const response = await teacherApi.getGradingTasks(
        gradingFilter.value.status || undefined,
        gradingFilter.value.priority || undefined,
        gradingFilter.value.courseId || undefined
      )
      gradingTasks.value = response.items
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '加载失败',
        message: '获取评分任务失败'
      })
      console.error('Failed to fetch grading tasks:', error)
    } finally {
      gradingLoading.value = false
    }
  }

  const startGrading = async (taskId: string) => {
    try {
      const updatedTask = await teacherApi.startGradingTask(taskId)
      const index = gradingTasks.value.findIndex(task => task.id === taskId)
      if (index !== -1) {
        gradingTasks.value[index] = updatedTask
      }
      uiStore.showNotification({
        type: 'success',
        title: '开始评分',
        message: '评分任务已开始'
      })
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '操作失败',
        message: '开始评分失败'
      })
      console.error('Failed to start grading task:', error)
      throw error
    }
  }

  const submitGrade = async (taskId: string, gradeData: {
    score: number
    feedback: string
    strengths?: string[]
    improvements?: string[]
    rubricScores?: Record<string, number>
  }) => {
    try {
      const updatedTask = await teacherApi.submitGrade(taskId, gradeData)
      const index = gradingTasks.value.findIndex(task => task.id === taskId)
      if (index !== -1) {
        gradingTasks.value[index] = updatedTask
      }
      
      // 更新统计数据
      if (stats.value) {
        stats.value.pendingAssignments -= 1
        stats.value.gradedAssignments += 1
      }

      uiStore.showNotification({
        type: 'success',
        title: '评分完成',
        message: '作业评分已提交'
      })
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '提交失败',
        message: '评分提交失败'
      })
      console.error('Failed to submit grade:', error)
      throw error
    }
  }

  const fetchNotifications = async (unreadOnly: boolean = false) => {
    try {
      const response = await teacherApi.getTeacherNotifications(1, 50, unreadOnly)
      notifications.value = response.items
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '加载失败',
        message: '获取通知失败'
      })
      console.error('Failed to fetch notifications:', error)
    }
  }

  const markNotificationAsRead = async (notificationId: string) => {
    try {
      await teacherApi.markNotificationAsRead(notificationId)
      const notification = notifications.value.find(n => n.id === notificationId)
      if (notification) {
        notification.isRead = true
      }
    } catch (error) {
      console.error('Failed to mark notification as read:', error)
    }
  }

  const markAllNotificationsAsRead = async () => {
    try {
      await teacherApi.markAllNotificationsAsRead()
      notifications.value.forEach(notification => {
        notification.isRead = true
      })
      uiStore.showNotification({
        type: 'success',
        title: '操作成功',
        message: '所有通知已标记为已读'
      })
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '操作失败',
        message: '标记通知失败'
      })
      console.error('Failed to mark all notifications as read:', error)
    }
  }

  const fetchSchedule = async (startDate: string, endDate: string) => {
    try {
      schedule.value = await teacherApi.getTeacherSchedule(startDate, endDate)
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '加载失败',
        message: '获取日程安排失败'
      })
      console.error('Failed to fetch schedule:', error)
    }
  }

  const exportStudentGrades = async (courseId: string, format: 'excel' | 'pdf' = 'excel', studentIds?: string[]) => {
    try {
      const blob = await teacherApi.exportStudentGrades(courseId, format, studentIds)
      
      // 创建下载链接
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `student-grades-${courseId}.${format === 'excel' ? 'xlsx' : 'pdf'}`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      
      uiStore.showNotification({
        type: 'success',
        title: '导出成功',
        message: '学生成绩报告已导出'
      })
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '导出失败',
        message: '学生成绩报告导出失败'
      })
      console.error('Failed to export student grades:', error)
      throw error
    }
  }

  const sendStudentMessage = async (studentId: string, message: {
    subject: string
    content: string
    type: 'general' | 'assignment' | 'grade' | 'reminder'
    courseId?: string
    assignmentId?: string
  }) => {
    try {
      await teacherApi.sendStudentMessage(studentId, message)
      uiStore.showNotification({
        type: 'success',
        title: '发送成功',
        message: '消息已发送给学生'
      })
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '发送失败',
        message: '消息发送失败'
      })
      console.error('Failed to send student message:', error)
      throw error
    }
  }

  // 筛选器方法
  const setStudentFilter = (filter: Partial<typeof studentFilter.value>) => {
    Object.assign(studentFilter.value, filter)
  }

  const setGradingFilter = (filter: Partial<typeof gradingFilter.value>) => {
    Object.assign(gradingFilter.value, filter)
    fetchGradingTasks() // 自动重新加载数据
  }

  const clearFilters = () => {
    studentFilter.value = { courseId: '', status: '', search: '' }
    gradingFilter.value = { status: '', priority: '', courseId: '' }
  }

  // 初始化数据
  const initTeacherData = async () => {
    await Promise.all([
      fetchDashboard(),
      fetchCourses(),
      fetchGradingTasks(),
      fetchNotifications()
    ])
  }

  return {
    // 状态
    dashboard,
    profile,
    stats,
    students,
    courses,
    gradingTasks,
    notifications,
    schedule,
    courseAnalytics,
    classPerformance,
    currentStudentReport,
    loading,
    gradingLoading,
    studentFilter,
    gradingFilter,

    // 计算属性
    totalStudents,
    activeCourses,
    pendingGradingCount,
    averageRating,
    filteredStudents,
    filteredGradingTasks,
    unreadNotifications,
    urgentNotifications,
    atRiskStudents,
    todaySchedule,

    // Actions
    fetchDashboard,
    fetchProfile,
    updateProfile,
    fetchStudents,
    fetchStudentProgress,
    fetchCourses,
    fetchCourseAnalytics,
    fetchClassPerformance,
    fetchGradingTasks,
    startGrading,
    submitGrade,
    fetchNotifications,
    markNotificationAsRead,
    markAllNotificationsAsRead,
    fetchSchedule,
    exportStudentGrades,
    sendStudentMessage,
    setStudentFilter,
    setGradingFilter,
    clearFilters,
    initTeacherData
  }
}) 