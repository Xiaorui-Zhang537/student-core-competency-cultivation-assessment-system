<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800 p-6">
    <!-- 页面标题 -->
    <div class="mb-8">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-4xl font-bold text-gray-900 dark:text-white mb-2">
            欢迎回来，{{ studentStore.profile?.name || '同学' }}！
          </h1>
          <p class="text-lg text-gray-600 dark:text-gray-400">
            继续您的学习之旅，每一天都是新的进步
          </p>
        </div>
        <div class="flex items-center space-x-4">
          <!-- 当前时间 -->
          <div class="text-right">
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ currentDate }}</p>
            <p class="text-lg font-medium text-gray-900 dark:text-white">{{ currentTime }}</p>
          </div>
          <!-- 快速操作 -->
          <button variant="primary" @click="$router.push('/student/courses')">
            <plus-icon class="w-5 h-5 mr-2" />
            开始学习
          </button>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="studentStore.loading" class="flex justify-center items-center h-64">
      <div class="animate-spin rounded-full h-16 w-16 border-b-2 border-primary-500"></div>
    </div>

    <!-- 主要内容 -->
    <div v-else class="space-y-8">
      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <card padding="lg" hoverable class="text-center">
          <div class="flex items-center justify-center w-12 h-12 bg-blue-100 dark:bg-blue-900 rounded-lg mx-auto mb-4">
            <book-open-icon class="w-6 h-6 text-blue-600 dark:text-blue-400" />
          </div>
          <h3 class="text-2xl font-bold text-gray-900 dark:text-white mb-1">
            {{ studentStore.activeCourses }}
          </h3>
          <p class="text-sm text-gray-500 dark:text-gray-400">进行中的课程</p>
          <div class="mt-2">
            <span class="text-xs text-green-600 dark:text-green-400">
              +{{ Math.floor(Math.random() * 3) + 1 }} 本周新增
            </span>
          </div>
        </card>

        <card padding="lg" hoverable class="text-center">
          <div class="flex items-center justify-center w-12 h-12 bg-orange-100 dark:bg-orange-900 rounded-lg mx-auto mb-4">
            <clipboard-document-list-icon class="w-6 h-6 text-orange-600 dark:text-orange-400" />
          </div>
          <h3 class="text-2xl font-bold text-gray-900 dark:text-white mb-1">
            {{ studentStore.pendingAssignments }}
          </h3>
          <p class="text-sm text-gray-500 dark:text-gray-400">待完成作业</p>
          <div class="mt-2">
            <button variant="ghost" size="sm" @click="$router.push('/student/grades')">
              查看成绩
            </button>
          </div>
        </card>

        <card padding="lg" hoverable class="text-center">
          <div class="flex items-center justify-center w-12 h-12 bg-green-100 dark:bg-green-900 rounded-lg mx-auto mb-4">
            <trophy-icon class="w-6 h-6 text-green-600 dark:text-green-400" />
          </div>
          <h3 class="text-2xl font-bold text-gray-900 dark:text-white mb-1">
            {{ studentStore.averageScore.toFixed(1) }}
          </h3>
          <p class="text-sm text-gray-500 dark:text-gray-400">平均成绩</p>
          <div class="mt-2 flex justify-center">
            <div class="flex space-x-1">
              <star-icon v-for="i in 5" 
                        :key="i" 
                        :class="[
                          'w-3 h-3',
                          i <= Math.floor(studentStore.averageScore / 20) 
                            ? 'text-yellow-400 fill-current' 
                            : 'text-gray-300 dark:text-gray-600'
                        ]" />
            </div>
          </div>
        </card>

        <card padding="lg" hoverable class="text-center">
          <div class="flex items-center justify-center w-12 h-12 bg-purple-100 dark:bg-purple-900 rounded-lg mx-auto mb-4">
            <clock-icon class="w-6 h-6 text-purple-600 dark:text-purple-400" />
          </div>
          <h3 class="text-2xl font-bold text-gray-900 dark:text-white mb-1">
            {{ formatStudyTime(studentStore.totalStudyTime) }}
          </h3>
          <p class="text-sm text-gray-500 dark:text-gray-400">学习时长</p>
          <div class="mt-2">
            <span class="text-xs text-blue-600 dark:text-blue-400">
              本周 {{ formatStudyTime(studentStore.weeklyStudyTime) }}
            </span>
          </div>
        </card>
      </div>

      <!-- 主要内容区域 -->
      <div class="grid grid-cols-1 xl:grid-cols-3 gap-8">
        <!-- 左侧：学习进度和图表 -->
        <div class="xl:col-span-2 space-y-8">
          <!-- 学习进度分析 -->
          <card padding="lg">
            <template #header>
              <div class="flex justify-between items-center">
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white">学习进度分析</h3>
                <select v-model="selectedTimeRange" class="input input-sm max-w-xs">
                  <option value="1week">最近一周</option>
                  <option value="1month">最近一月</option>
                  <option value="3months">最近三月</option>
                </select>
              </div>
            </template>
            <div ref="progressChartRef" class="h-80 w-full"></div>
          </card>

          <!-- 课程列表 -->
          <card padding="lg">
            <template #header>
              <div class="flex justify-between items-center">
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white">我的课程</h3>
                <button variant="outline" size="sm" @click="$router.push('/student/courses')">
                  查看全部
                </button>
              </div>
            </template>
            
            <div class="space-y-4">
              <div v-for="course in studentStore.recentCourses.slice(0, 3)" 
                   :key="course.id"
                   @click="$router.push(`/student/courses/${course.id}`)"
                   class="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-600 transition-colors cursor-pointer">
                <div class="flex items-center space-x-4">
                  <div class="w-12 h-12 bg-gradient-to-r from-blue-500 to-purple-600 rounded-lg flex items-center justify-center text-white font-medium">
                    {{ course.title.charAt(0) }}
                  </div>
                  <div>
                    <h4 class="font-medium text-gray-900 dark:text-white">{{ course.title }}</h4>
                    <p class="text-sm text-gray-500 dark:text-gray-400">
                      {{ course.instructor }} · {{ course.totalLessons }} 课时
                    </p>
                  </div>
                </div>
                <div class="text-right">
                  <p class="text-sm font-medium text-gray-900 dark:text-white">
                    {{ Math.round(course.progress) }}% 完成
                  </p>
                  <progress :value="course.progress" color="primary" size="sm" class="w-24 mt-1" />
                </div>
              </div>
              
              <div v-if="studentStore.recentCourses.length === 0" 
                   class="text-center py-8 text-gray-500 dark:text-gray-400">
                <book-open-icon class="w-12 h-12 mx-auto mb-4 opacity-50" />
                <p>还没有报名任何课程</p>
                <button variant="primary" class="mt-4" @click="$router.push('/student/courses')">
                  浏览课程
                </button>
              </div>
            </div>
          </card>
        </div>

        <!-- 右侧：待办事项和最近活动 -->
        <div class="space-y-8">
          <!-- 今日待办 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">今日待办</h3>
            </template>
            
            <div class="space-y-4">
              <!-- 紧急作业 -->
              <div v-if="urgentAssignments.length > 0">
                <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">紧急作业</h4>
                <div class="space-y-2">
                  <div v-for="assignment in urgentAssignments.slice(0, 2)" 
                       :key="assignment.id"
                       class="flex items-center justify-between p-3 bg-red-50 dark:bg-red-900/20 rounded-lg">
                    <div class="flex-1">
                      <p class="text-sm font-medium text-gray-900 dark:text-white">
                        {{ assignment.title }}
                      </p>
                      <p class="text-xs text-gray-500 dark:text-gray-400">
                        {{ assignment.courseName }}
                      </p>
                    </div>
                    <badge variant="danger" size="sm">
                      {{ getDaysLeft(assignment.dueDate) }}天截止
                    </badge>
                  </div>
                </div>
              </div>

              <!-- 今日课程 -->
              <div v-if="todaySchedule.length > 0">
                <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">今日课程</h4>
                <div class="space-y-2">
                  <div v-for="item in todaySchedule" 
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

              <!-- 学习提醒 -->
              <div>
                <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">学习提醒</h4>
                <div class="space-y-2">
                  <div v-for="reminder in learningReminders.slice(0, 3)" 
                       :key="reminder.id"
                       class="p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                    <div class="flex justify-between items-start mb-1">
                      <h5 class="text-sm font-medium text-gray-900 dark:text-white">
                        {{ reminder.title }}
                      </h5>
                      <badge :variant="getReminderTypeVariant(reminder.type)" size="sm">
                        {{ getReminderTypeText(reminder.type) }}
                      </badge>
                    </div>
                    <p class="text-xs text-gray-500 dark:text-gray-400">
                      {{ reminder.description }}
                    </p>
                  </div>
                </div>
              </div>

              <div class="pt-4 border-t border-gray-200 dark:border-gray-600">
                <button variant="primary" size="sm" class="w-full" @click="$router.push('/student/grades')">
                  查看所有成绩
                </button>
              </div>
            </div>
          </card>

          <!-- 能力成长 -->
          <card padding="lg">
            <template #header>
              <div class="flex justify-between items-center">
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white">能力成长</h3>
                <button variant="outline" size="sm" @click="$router.push('/student/ability')">
                  详细分析
                </button>
              </div>
            </template>
            
            <div class="space-y-4">
              <div class="text-center">
                <div class="text-2xl font-bold text-primary-600 dark:text-primary-400 mb-2">
                  {{ studentStore.overallProgress || 75 }}%
                </div>
                <p class="text-sm text-gray-600 dark:text-gray-400 mb-4">综合能力进步</p>
                <progress :value="studentStore.overallProgress || 75" color="primary" size="lg" />
                <div class="mt-3 flex items-center justify-center">
                  <arrow-trending-up-icon class="w-4 h-4 text-green-500 mr-1" />
                  <span class="text-sm text-green-600 dark:text-green-400">+5.2% 本周提升</span>
                </div>
              </div>
              
              <div class="space-y-3">
                <div v-for="ability in topAbilities" :key="ability.name" class="flex justify-between items-center">
                  <span class="text-sm text-gray-600 dark:text-gray-400">{{ ability.name }}</span>
                  <div class="flex items-center space-x-2">
                    <progress :value="ability.score" color="primary" size="sm" class="w-16" />
                    <span class="text-sm font-medium text-gray-900 dark:text-white">{{ ability.score }}%</span>
                  </div>
                </div>
              </div>
            </div>
          </card>

          <!-- 最近活动 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">最近活动</h3>
            </template>
            
            <div class="space-y-4">
              <div v-for="activity in recentActivities.slice(0, 4)" 
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
              
              <div v-if="recentActivities.length === 0" 
                   class="text-center py-4 text-gray-500 dark:text-gray-400">
                暂无最近活动
              </div>
            </div>
          </card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useStudentStore } from '@/stores/student'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Progress from '@/components/ui/Progress.vue'
import * as echarts from 'echarts'
import type { Assignment } from '@/types/student'
import {
  PlusIcon,
  BookOpenIcon,
  ClipboardDocumentListIcon,
  TrophyIcon,
  ClockIcon,
  StarIcon,
  ArrowTrendingUpIcon,
  AcademicCapIcon,
  CheckCircleIcon,
  ExclamationTriangleIcon
} from '@heroicons/vue/24/outline'

// Stores & Router
const router = useRouter()
const studentStore = useStudentStore()
const uiStore = useUIStore()

// 状态
const selectedTimeRange = ref('1month')
const progressChartRef = ref<HTMLElement>()
let progressChart: echarts.ECharts | null = null

// 时间相关
const currentDate = ref('')
const currentTime = ref('')

// 计算属性
const urgentAssignments = computed(() => {
  const now = new Date()
  return studentStore.assignments?.filter((assignment: Assignment) => {
    const dueDate = new Date(assignment.dueDate)
    const daysLeft = Math.ceil((dueDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24))
    return daysLeft <= 3 && daysLeft > 0 && assignment.status === 'pending'
  }) || []
})

const topAbilities = computed(() => [
  { name: '逻辑思维', score: 85 },
  { name: '创新能力', score: 78 },
  { name: '沟通表达', score: 82 },
  { name: '团队协作', score: 76 }
])

// 模拟数据
const todaySchedule = ref([
  {
    id: '1',
    title: '高等数学',
    startTime: '2024-01-15T09:00:00Z',
    endTime: '2024-01-15T10:30:00Z'
  },
  {
    id: '2',
    title: '数据结构',
    startTime: '2024-01-15T14:00:00Z',
    endTime: '2024-01-15T15:30:00Z'
  }
])

const learningReminders = ref([
  {
    id: '1',
    title: '复习微积分第三章',
    description: '明天有随堂测试',
    type: 'test'
  },
  {
    id: '2',
    title: '预习线性代数',
    description: '下周开始新章节',
    type: 'preview'
  },
  {
    id: '3',
    title: '完成编程练习',
    description: '本周作业截止',
    type: 'assignment'
  }
])

const recentActivities = ref([
  {
    id: '1',
    title: '完成了《数据结构》第五章学习',
    description: '恭喜您掌握了二叉树的基本概念',
    type: 'course_completed',
    timestamp: '2024-01-15T10:30:00Z'
  },
  {
    id: '2',
    title: '提交了高等数学作业',
    description: '第三章练习题已提交，等待老师评分',
    type: 'assignment_submitted',
    timestamp: '2024-01-14T16:20:00Z'
  },
  {
    id: '3',
    title: '获得了"积极学习者"徽章',
    description: '连续7天完成学习任务',
    type: 'achievement_earned',
    timestamp: '2024-01-14T09:15:00Z'
  },
  {
    id: '4',
    title: '加入了学习小组',
    description: '数学互助小组',
    type: 'group_joined',
    timestamp: '2024-01-13T14:45:00Z'
  }
])

// 方法
const updateTime = () => {
  const now = new Date()
  currentDate.value = now.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    weekday: 'long'
  })
  currentTime.value = now.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit'
  })
}

const formatStudyTime = (minutes: number) => {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0) {
    return `${hours}h${mins > 0 ? mins + 'm' : ''}`
  }
  return `${mins}m`
}

const formatTime = (timeString: string) => {
  const date = new Date(timeString)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const formatRelativeTime = (timestamp: string) => {
  const now = new Date()
  const date = new Date(timestamp)
  const diffInHours = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60))
  
  if (diffInHours < 1) return '刚刚'
  if (diffInHours < 24) return `${diffInHours}小时前`
  if (diffInHours < 48) return '昨天'
  return `${Math.floor(diffInHours / 24)}天前`
}

const getDaysLeft = (dueDate: string) => {
  const now = new Date()
  const due = new Date(dueDate)
  return Math.ceil((due.getTime() - now.getTime()) / (1000 * 60 * 60 * 24))
}

const getReminderTypeVariant = (type: string) => {
  switch (type) {
    case 'test': return 'danger'
    case 'assignment': return 'warning'
    case 'preview': return 'info'
    default: return 'secondary'
  }
}

const getReminderTypeText = (type: string) => {
  switch (type) {
    case 'test': return '测试'
    case 'assignment': return '作业'
    case 'preview': return '预习'
    default: return '提醒'
  }
}

const getActivityIcon = (type: string) => {
  switch (type) {
    case 'course_completed': return CheckCircleIcon
    case 'assignment_submitted': return ClipboardDocumentListIcon
    case 'achievement_earned': return TrophyIcon
    case 'group_joined': return AcademicCapIcon
    default: return BookOpenIcon
  }
}

const initProgressChart = () => {
  if (!progressChartRef.value) return
  
  progressChart = echarts.init(progressChartRef.value)
  
  // 模拟学习进度数据
  const mockData = {
    studyTime: [45, 52, 38, 67, 55, 48, 62],
    completedTasks: [3, 5, 2, 8, 4, 6, 7],
    categories: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
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
      data: ['学习时长(分钟)', '完成任务数'],
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
    yAxis: [
      {
        type: 'value',
        name: '学习时长',
        axisLine: {
          lineStyle: {
            color: '#e5e7eb'
          }
        },
        axisLabel: {
          color: '#6b7280'
        },
        splitLine: {
          lineStyle: {
            color: '#f3f4f6'
          }
        }
      },
      {
        type: 'value',
        name: '任务数',
        axisLine: {
          lineStyle: {
            color: '#e5e7eb'
          }
        },
        axisLabel: {
          color: '#6b7280'
        }
      }
    ],
    series: [
      {
        name: '学习时长(分钟)',
        type: 'bar',
        data: mockData.studyTime,
        itemStyle: {
          color: '#3b82f6'
        }
      },
      {
        name: '完成任务数',
        type: 'line',
        yAxisIndex: 1,
        data: mockData.completedTasks,
        smooth: true,
        lineStyle: {
          color: '#10b981',
          width: 3
        },
        itemStyle: {
          color: '#10b981'
        }
      }
    ]
  }
  
  progressChart.setOption(option)
}

const resizeCharts = () => {
  progressChart?.resize()
}

// 监听时间范围变化
watch(selectedTimeRange, (newRange: string) => {
  // 根据选择的时间范围更新图表数据
  console.log('时间范围变更:', newRange)
})

// 生命周期
onMounted(async () => {
  // 初始化时间显示
  updateTime()
  const timeInterval = setInterval(updateTime, 60000) // 每分钟更新一次
  
  // 初始化学生数据
  await studentStore.initStudentData()
  
  nextTick(() => {
    initProgressChart()
  })
  
  window.addEventListener('resize', resizeCharts)
  
  // 清理定时器
  onUnmounted(() => {
    clearInterval(timeInterval)
    progressChart?.dispose()
    window.removeEventListener('resize', resizeCharts)
  })
})
</script> 