<template>
  <div class="min-h-screen bg-gradient-to-br from-indigo-50 to-blue-100 dark:from-gray-900 dark:to-gray-800 p-6">
    <!-- 页面标题 -->
    <div class="mb-8">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-4xl font-bold text-gray-900 dark:text-white mb-2">
            教师工作台
          </h1>
          <p class="text-lg text-gray-600 dark:text-gray-400">
            欢迎回来，{{ teacherStore.profile?.name || '老师' }}！
          </p>
        </div>
        <div class="flex items-center space-x-4">
          <!-- 通知按钮 -->
          <div class="relative">
            <button
              variant="outline" 
              size="lg"
              @click="showNotifications = !showNotifications"
              class="relative"
            >
              <bell-icon class="w-5 h-5" />
              <span v-if="teacherStore.unreadNotifications.length > 0" 
                    class="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full h-6 w-6 flex items-center justify-center">
                {{ teacherStore.unreadNotifications.length }}
              </span>
            </button>
            
            <!-- 通知下拉面板 -->
            <div v-if="showNotifications" 
                 class="absolute right-0 mt-2 w-80 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-600 z-50">
              <div class="p-4 border-b border-gray-200 dark:border-gray-600">
                <div class="flex justify-between items-center">
                  <h3 class="font-medium text-gray-900 dark:text-white">通知</h3>
                  <button 
                    variant="ghost" 
                    size="sm" 
                    @click="teacherStore.markAllNotificationsAsRead()"
                    v-if="teacherStore.unreadNotifications.length > 0"
                  >
                    全部已读
                  </button>
                </div>
              </div>
              <div class="max-h-80 overflow-y-auto">
                <div v-for="notification in teacherStore.notifications.slice(0, 5)" 
                     :key="notification.id"
                     class="p-4 border-b border-gray-100 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer"
                     @click="handleNotificationClick(notification)">
                  <div class="flex items-start space-x-3">
                    <div class="flex-shrink-0">
                      <component :is="getNotificationIcon(notification.type)" 
                                 class="w-5 h-5 text-blue-500" />
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium text-gray-900 dark:text-white">
                        {{ notification.title }}
                      </p>
                      <p class="text-sm text-gray-500 dark:text-gray-400 line-clamp-2">
                        {{ notification.message }}
                      </p>
                      <p class="text-xs text-gray-400 dark:text-gray-500 mt-1">
                        {{ formatRelativeTime(notification.timestamp) }}
                      </p>
                    </div>
                    <div v-if="!notification.isRead" class="w-2 h-2 bg-blue-500 rounded-full"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 快速操作按钮 -->
          <button variant="primary" size="lg" @click="showQuickActions = !showQuickActions">
            <plus-icon class="w-5 h-5 mr-2" />
            快速操作
          </button>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="teacherStore.loading" class="flex justify-center items-center h-64">
      <div class="animate-spin rounded-full h-16 w-16 border-b-2 border-primary-500"></div>
    </div>

    <!-- 主要内容 -->
    <div v-else class="space-y-8">
      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <card padding="lg" hoverable class="text-center">
          <div class="flex items-center justify-center w-12 h-12 bg-blue-100 dark:bg-blue-900 rounded-lg mx-auto mb-4">
            <academic-cap-icon class="w-6 h-6 text-blue-600 dark:text-blue-400" />
          </div>
          <h3 class="text-2xl font-bold text-gray-900 dark:text-white mb-1">
            {{ teacherStore.activeCourses }}
          </h3>
          <p class="text-sm text-gray-500 dark:text-gray-400">活跃课程</p>
          <div class="mt-2">
            <span class="text-xs text-green-600 dark:text-green-400">
              +{{ Math.floor(Math.random() * 5) + 1 }} 本月新增
            </span>
          </div>
        </card>

        <card padding="lg" hoverable class="text-center">
          <div class="flex items-center justify-center w-12 h-12 bg-green-100 dark:bg-green-900 rounded-lg mx-auto mb-4">
            <users-icon class="w-6 h-6 text-green-600 dark:text-green-400" />
          </div>
          <h3 class="text-2xl font-bold text-gray-900 dark:text-white mb-1">
            {{ teacherStore.totalStudents }}
          </h3>
          <p class="text-sm text-gray-500 dark:text-gray-400">学生总数</p>
          <div class="mt-2">
            <span class="text-xs text-blue-600 dark:text-blue-400">
              {{ teacherStore.atRiskStudents.length }} 需要关注
            </span>
          </div>
        </card>

        <card padding="lg" hoverable class="text-center">
          <div class="flex items-center justify-center w-12 h-12 bg-yellow-100 dark:bg-yellow-900 rounded-lg mx-auto mb-4">
            <clipboard-document-check-icon class="w-6 h-6 text-yellow-600 dark:text-yellow-400" />
          </div>
          <h3 class="text-2xl font-bold text-gray-900 dark:text-white mb-1">
            {{ teacherStore.pendingGradingCount }}
          </h3>
          <p class="text-sm text-gray-500 dark:text-gray-400">待评分作业</p>
          <div class="mt-2">
            <button variant="ghost" size="sm" @click="$router.push('/teacher/grading')">
              去评分
            </button>
          </div>
        </card>

        <card padding="lg" hoverable class="text-center">
          <div class="flex items-center justify-center w-12 h-12 bg-purple-100 dark:bg-purple-900 rounded-lg mx-auto mb-4">
            <star-icon class="w-6 h-6 text-purple-600 dark:text-purple-400" />
          </div>
          <h3 class="text-2xl font-bold text-gray-900 dark:text-white mb-1">
            {{ teacherStore.averageRating.toFixed(1) }}
          </h3>
          <p class="text-sm text-gray-500 dark:text-gray-400">平均评分</p>
          <div class="mt-2 flex justify-center">
            <div class="flex space-x-1">
              <star-icon v-for="i in 5" 
                        :key="i" 
                        :class="[
                          'w-3 h-3',
                          i <= Math.floor(teacherStore.averageRating) 
                            ? 'text-yellow-400 fill-current' 
                            : 'text-gray-300 dark:text-gray-600'
                        ]" />
            </div>
          </div>
        </card>
      </div>

      <!-- 主要内容区域 -->
      <div class="grid grid-cols-1 xl:grid-cols-3 gap-8">
        <!-- 左侧：课程分析和学生概览 -->
        <div class="xl:col-span-2 space-y-8">
          <!-- 课程表现分析 -->
          <card padding="lg">
            <template #header>
              <div class="flex justify-between items-center">
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white">课程表现分析</h3>
                <select v-model="selectedCourseForChart" class="input input-sm max-w-xs">
                  <option value="">选择课程</option>
                  <option v-for="course in teacherStore.courses" 
                          :key="course.courseId" 
                          :value="course.courseId">
                    {{ course.courseName }}
                  </option>
                </select>
              </div>
            </template>
            <div ref="courseChartRef" class="h-80 w-full"></div>
          </card>

          <!-- 学生概览 -->
          <card padding="lg">
            <template #header>
              <div class="flex justify-between items-center">
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white">学生概览</h3>
                <div class="flex space-x-2">
                  <select v-model="studentStatusFilter" class="input input-sm">
                    <option value="">全部状态</option>
                    <option value="active">活跃</option>
                    <option value="at_risk">需要关注</option>
                    <option value="inactive">不活跃</option>
                  </select>
                  <button variant="outline" size="sm" @click="$router.push('/teacher/students')">
                    查看全部
                  </button>
                </div>
              </div>
            </template>
            
            <div class="space-y-4">
              <div v-for="student in displayedStudents" 
                   :key="student.id"
                   class="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-600 transition-colors cursor-pointer"
                   @click="viewStudentDetail(student)">
                <div class="flex items-center space-x-4">
                  <div class="w-10 h-10 bg-gradient-to-r from-blue-500 to-purple-600 rounded-full flex items-center justify-center text-white font-medium">
                    {{ student.name.charAt(0) }}
                  </div>
                  <div>
                    <h4 class="font-medium text-gray-900 dark:text-white">{{ student.name }}</h4>
                    <p class="text-sm text-gray-500 dark:text-gray-400">
                      {{ student.studentCode }} · {{ student.major }}
                    </p>
                  </div>
                </div>
                <div class="flex items-center space-x-4">
                  <div class="text-right">
                    <p class="text-sm font-medium text-gray-900 dark:text-white">
                      平均分：{{ student.averageScore }}
                    </p>
                    <p class="text-xs text-gray-500 dark:text-gray-400">
                      {{ formatRelativeTime(student.lastActivity) }}
                    </p>
                  </div>
                  <badge :variant="getStudentStatusColor(student.status)">
                    {{ getStudentStatusText(student.status) }}
                  </badge>
                </div>
              </div>
              
              <div v-if="teacherStore.students.length === 0" 
                   class="text-center py-8 text-gray-500 dark:text-gray-400">
                暂无学生数据
              </div>
            </div>
          </card>
        </div>

        <!-- 右侧：待办事项和日程安排 -->
        <div class="space-y-8">
          <!-- 今日待办 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">今日待办</h3>
            </template>
            
            <div class="space-y-4">
              <!-- 评分任务 -->
              <div v-if="urgentGradingTasks.length > 0">
                <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">紧急评分</h4>
                <div class="space-y-2">
                  <div v-for="task in urgentGradingTasks.slice(0, 3)" 
                       :key="task.id"
                       class="flex items-center justify-between p-3 bg-red-50 dark:bg-red-900/20 rounded-lg">
                    <div class="flex-1">
                      <p class="text-sm font-medium text-gray-900 dark:text-white">
                        {{ task.assignmentTitle }}
                      </p>
                      <p class="text-xs text-gray-500 dark:text-gray-400">
                        {{ task.studentName }} · {{ task.courseName }}
                      </p>
                    </div>
                    <badge variant="danger" size="sm">
                      {{ getDaysOverdue(task.dueDate) }}天逾期
                    </badge>
                  </div>
                </div>
              </div>

              <!-- 今日日程 -->
              <div v-if="teacherStore.todaySchedule.length > 0">
                <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">今日日程</h4>
                <div class="space-y-2">
                  <div v-for="item in teacherStore.todaySchedule" 
                       :key="item.id"
                       class="flex items-center space-x-3 p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                    <clock-icon class="w-4 h-4 text-blue-500" />
                    <div class="flex-1">
                      <p class="text-sm font-medium text-gray-900 dark:text-white">
                        {{ item.title }}
                      </p>
                      <p class="text-xs text-gray-500 dark:text-gray-400">
                        {{ formatTime(item.startTime) }} - {{ formatTime(item.endTime) }}
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 课程提醒 -->
              <div>
                <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">课程提醒</h4>
                <div class="space-y-2">
                  <div v-for="course in teacherStore.courses.slice(0, 2)" 
                       :key="course.courseId"
                       class="p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                    <div class="flex justify-between items-start mb-2">
                      <h5 class="text-sm font-medium text-gray-900 dark:text-white">
                        {{ course.courseName }}
                      </h5>
                                             <badge variant="warning" size="sm">
                         {{ course.pendingTasks.reduce((sum: number, task: any) => sum + task.count, 0) }} 待处理
                       </badge>
                    </div>
                    <div class="space-y-1">
                      <div v-for="task in course.pendingTasks" :key="task.type" class="text-xs text-gray-500 dark:text-gray-400">
                        {{ getTaskTypeText(task.type) }}：{{ task.count }} 项
                        <span v-if="task.urgent > 0" class="text-red-500">({{ task.urgent }} 紧急)</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="pt-4 border-t border-gray-200 dark:border-gray-600">
                <button variant="primary" size="sm" class="w-full" @click="$router.push('/teacher/grading')">
                  查看所有待办
                </button>
              </div>
            </div>
          </card>

          <!-- 快捷操作 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">快捷操作</h3>
            </template>
            
            <div class="grid grid-cols-2 gap-3">
              <button variant="outline" size="sm" @click="$router.push('/teacher/courses/create')" class="flex flex-col items-center p-4 h-auto">
                <plus-icon class="w-6 h-6 mb-2" />
                <span class="text-xs">创建课程</span>
              </button>
              
                              <button variant="outline" size="sm" @click="$router.push('/teacher/courses/create')" class="flex flex-col items-center p-4 h-auto">
                <document-plus-icon class="w-6 h-6 mb-2" />
                <span class="text-xs">发布作业</span>
              </button>
              
              <button variant="outline" size="sm" @click="$router.push('/teacher/grading')" class="flex flex-col items-center p-4 h-auto">
                <clipboard-document-check-icon class="w-6 h-6 mb-2" />
                <span class="text-xs">批量评分</span>
              </button>
              
              <button variant="outline" size="sm" @click="$router.push('/teacher/analytics')" class="flex flex-col items-center p-4 h-auto">
                <chart-bar-icon class="w-6 h-6 mb-2" />
                <span class="text-xs">数据分析</span>
              </button>
              
              <button variant="outline" size="sm" @click="$router.push('/teacher/students')" class="flex flex-col items-center p-4 h-auto">
                <users-icon class="w-6 h-6 mb-2" />
                <span class="text-xs">学生管理</span>
              </button>
              
              <button variant="outline" size="sm" @click="$router.push('/teacher/messages')" class="flex flex-col items-center p-4 h-auto">
                <chat-bubble-left-right-icon class="w-6 h-6 mb-2" />
                <span class="text-xs">消息中心</span>
              </button>
            </div>
          </card>

          <!-- 最近活动 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">最近活动</h3>
            </template>
            
            <div class="space-y-4">
              <div v-for="activity in teacherStore.dashboard?.recentActivities.slice(0, 5)" 
                   :key="activity.id"
                   class="flex items-start space-x-3">
                <div class="flex-shrink-0 w-8 h-8 bg-blue-100 dark:bg-blue-900 rounded-full flex items-center justify-center">
                  <component :is="getActivityIcon(activity.type)" class="w-4 h-4 text-blue-600 dark:text-blue-400" />
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm text-gray-900 dark:text-white">
                    {{ activity.title }}
                  </p>
                  <p class="text-xs text-gray-500 dark:text-gray-400">
                    {{ activity.description }}
                  </p>
                  <p class="text-xs text-gray-400 dark:text-gray-500 mt-1">
                    {{ formatRelativeTime(activity.timestamp) }}
                  </p>
                </div>
              </div>
              
              <div v-if="!teacherStore.dashboard?.recentActivities.length" 
                   class="text-center py-4 text-gray-500 dark:text-gray-400">
                暂无最近活动
              </div>
            </div>
          </card>
        </div>
      </div>
    </div>

    <!-- 快速操作弹窗 -->
    <div v-if="showQuickActions" 
         class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
         @click.self="showQuickActions = false">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md mx-4">
        <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">快速操作</h3>
        <div class="space-y-3">
          <button variant="outline" class="w-full justify-start" @click="createCourse">
            <plus-icon class="w-4 h-4 mr-3" />
            创建新课程
          </button>
          <button variant="outline" class="w-full justify-start" @click="createAssignment">
            <document-plus-icon class="w-4 h-4 mr-3" />
            发布作业
          </button>
          <button variant="outline" class="w-full justify-start" @click="sendBulkMessage">
            <chat-bubble-left-right-icon class="w-4 h-4 mr-3" />
            群发消息
          </button>
          <button variant="outline" class="w-full justify-start" @click="exportReports">
            <document-arrow-down-icon class="w-4 h-4 mr-3" />
            导出报告
          </button>
        </div>
        <div class="mt-6 flex justify-end">
          <button variant="ghost" @click="showQuickActions = false">
            取消
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useTeacherStore } from '@/stores/teacher'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import * as echarts from 'echarts'
import type { StudentOverview, TeacherNotification, GradingTask } from '@/types/teacher'
import {
  BellIcon,
  PlusIcon,
  AcademicCapIcon,
  UsersIcon,
  ClipboardDocumentCheckIcon,
  StarIcon,
  ClockIcon,
  ChartBarIcon,
  DocumentPlusIcon,
  ChatBubbleLeftRightIcon,
  DocumentArrowDownIcon,
  UserGroupIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  DocumentTextIcon
} from '@heroicons/vue/24/outline'

// Stores & Router
const router = useRouter()
const teacherStore = useTeacherStore()
const uiStore = useUIStore()

// 状态
const showNotifications = ref(false)
const showQuickActions = ref(false)
const selectedCourseForChart = ref('')
const studentStatusFilter = ref('')
const courseChartRef = ref<HTMLElement>()
let courseChart: echarts.ECharts | null = null

// 计算属性
const displayedStudents = computed(() => {
  let filtered = teacherStore.students
  
  if (studentStatusFilter.value) {
    filtered = filtered.filter(student => student.status === studentStatusFilter.value)
  }
  
  return filtered.slice(0, 5) // 只显示前5个学生
})

const urgentGradingTasks = computed(() => {
  const now = new Date()
  return teacherStore.gradingTasks.filter((task: GradingTask) => {
    const dueDate = new Date(task.dueDate)
    return dueDate < now && task.status === 'pending'
  })
})

// 方法
const formatRelativeTime = (timestamp: string) => {
  const now = new Date()
  const date = new Date(timestamp)
  const diffInHours = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60))
  
  if (diffInHours < 1) return '刚刚'
  if (diffInHours < 24) return `${diffInHours}小时前`
  if (diffInHours < 48) return '昨天'
  return `${Math.floor(diffInHours / 24)}天前`
}

const formatTime = (timeString: string) => {
  const date = new Date(timeString)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const getDaysOverdue = (dueDate: string) => {
  const now = new Date()
  const due = new Date(dueDate)
  return Math.floor((now.getTime() - due.getTime()) / (1000 * 60 * 60 * 24))
}

const getStudentStatusColor = (status: string) => {
  switch (status) {
    case 'active': return 'success'
    case 'at_risk': return 'warning'
    case 'inactive': return 'secondary'
    default: return 'secondary'
  }
}

const getStudentStatusText = (status: string) => {
  switch (status) {
    case 'active': return '活跃'
    case 'at_risk': return '需关注'
    case 'inactive': return '不活跃'
    default: return '未知'
  }
}

const getNotificationIcon = (type: string) => {
  switch (type) {
    case 'assignment_submitted': return DocumentTextIcon
    case 'student_question': return ChatBubbleLeftRightIcon
    case 'system_update': return ExclamationTriangleIcon
    case 'deadline_reminder': return ClockIcon
    case 'performance_alert': return ExclamationTriangleIcon
    default: return BellIcon
  }
}

const getActivityIcon = (type: string) => {
  switch (type) {
    case 'assignment_submitted': return DocumentTextIcon
    case 'student_enrolled': return UserGroupIcon
    case 'course_completed': return CheckCircleIcon
    case 'feedback_received': return ChatBubbleLeftRightIcon
    default: return DocumentTextIcon
  }
}

const getTaskTypeText = (type: string) => {
  switch (type) {
    case 'grading': return '评分'
    case 'feedback': return '反馈'
    case 'content_review': return '内容审查'
    case 'student_inquiry': return '学生咨询'
    default: return '其他'
  }
}

const initCourseChart = () => {
  if (!courseChartRef.value) return
  
  courseChart = echarts.init(courseChartRef.value)
  
  // 模拟课程数据
  const mockData = {
    enrollmentData: [85, 92, 78, 88, 95, 82, 90],
    completionData: [72, 85, 68, 76, 88, 74, 82],
    categories: ['第1周', '第2周', '第3周', '第4周', '第5周', '第6周', '第7周']
  }
  
  const option = {
    title: {
      show: false
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['入学率', '完成率'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: mockData.categories,
      axisLine: {
        lineStyle: {
          color: '#e5e7eb'
        }
      },
      axisLabel: {
        color: '#6b7280'
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLine: {
        lineStyle: {
          color: '#e5e7eb'
        }
      },
      axisLabel: {
        color: '#6b7280',
        formatter: '{value}%'
      },
      splitLine: {
        lineStyle: {
          color: '#f3f4f6'
        }
      }
    },
    series: [
      {
        name: '入学率',
        type: 'line',
        data: mockData.enrollmentData,
        smooth: true,
        lineStyle: {
          color: '#3b82f6',
          width: 3
        },
        itemStyle: {
          color: '#3b82f6'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(59, 130, 246, 0.3)' },
              { offset: 1, color: 'rgba(59, 130, 246, 0.05)' }
            ]
          }
        }
      },
      {
        name: '完成率',
        type: 'line',
        data: mockData.completionData,
        smooth: true,
        lineStyle: {
          color: '#10b981',
          width: 3
        },
        itemStyle: {
          color: '#10b981'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(16, 185, 129, 0.3)' },
              { offset: 1, color: 'rgba(16, 185, 129, 0.05)' }
            ]
          }
        }
      }
    ]
  }
  
  courseChart.setOption(option)
}

const resizeCharts = () => {
  courseChart?.resize()
}

const handleNotificationClick = (notification: TeacherNotification) => {
  teacherStore.markNotificationAsRead(notification.id)
  showNotifications.value = false
  
  if (notification.metadata?.url) {
    router.push(notification.metadata.url)
  }
}

const viewStudentDetail = (student: StudentOverview) => {
  router.push(`/teacher/students/${student.id}`)
}

const createCourse = () => {
  showQuickActions.value = false
  router.push('/teacher/courses/create')
}

const createAssignment = () => {
  showQuickActions.value = false
  router.push('/teacher/grading')
}

const sendBulkMessage = () => {
  showQuickActions.value = false
  router.push('/teacher/messages/compose')
}

const exportReports = () => {
  showQuickActions.value = false
  router.push('/teacher/reports')
}

// 监听选中课程变化，更新图表
watch(selectedCourseForChart, (newCourseId: string) => {
  if (newCourseId) {
    teacherStore.fetchCourseAnalytics(newCourseId)
  }
})

// 生命周期
onMounted(async () => {
  await teacherStore.initTeacherData()
  
  // 获取今日日程
  const today = new Date()
  const startDate = today.toISOString().split('T')[0]
  const endDate = startDate
  await teacherStore.fetchSchedule(startDate, endDate)
  
  // 获取学生数据
  await teacherStore.fetchStudents()
  
  nextTick(() => {
    initCourseChart()
  })
  
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  courseChart?.dispose()
  window.removeEventListener('resize', resizeCharts)
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style> 