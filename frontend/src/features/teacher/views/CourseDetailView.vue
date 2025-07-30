<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <div class="max-w-7xl mx-auto">
      <!-- 课程头部信息 -->
      <div class="mb-8">
        <div class="flex items-start justify-between">
          <div class="flex items-start space-x-6">
            <!-- 课程封面 -->
            <div class="flex-shrink-0">
              <img
                :src="course.coverUrl || '/api/placeholder/200/150'"
                :alt="course.title"
                class="w-48 h-32 object-cover rounded-lg shadow-sm"
              />
            </div>
            
            <!-- 课程基本信息 -->
            <div class="flex-1">
              <div class="flex items-center space-x-3 mb-2">
                <h1 class="text-3xl font-bold text-gray-900 dark:text-white">{{ course.title }}</h1>
                <badge :variant="course.status === 'published' ? 'success' : course.status === 'draft' ? 'warning' : 'secondary'">
                  {{ getStatusText(course.status) }}
                </badge>
              </div>
              
              <p class="text-gray-600 dark:text-gray-400 mb-4 max-w-2xl">{{ course.description }}</p>
              
              <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                <div>
                  <span class="text-gray-500 dark:text-gray-400">分类：</span>
                  <span class="font-medium text-gray-900 dark:text-white">{{ getCategoryText(course.category) }}</span>
                </div>
                <div>
                  <span class="text-gray-500 dark:text-gray-400">难度：</span>
                  <span class="font-medium text-gray-900 dark:text-white">{{ getLevelText(course.level) }}</span>
                </div>
                <div>
                  <span class="text-gray-500 dark:text-gray-400">价格：</span>
                  <span class="font-medium text-gray-900 dark:text-white">
                    {{ course.price === 0 ? '免费' : `¥${course.price}` }}
                  </span>
                </div>
                <div>
                  <span class="text-gray-500 dark:text-gray-400">创建时间：</span>
                  <span class="font-medium text-gray-900 dark:text-white">{{ formatDate(course.createdAt) }}</span>
                </div>
              </div>
              
              <!-- 课程标签 -->
              <div class="flex flex-wrap gap-2 mt-4">
                <badge v-for="tag in course.tags" :key="tag" variant="secondary" size="sm">
                  {{ tag }}
                </badge>
              </div>
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="flex items-center space-x-3">
            <button variant="outline" @click="previewCourse">
              <eye-icon class="w-4 h-4 mr-2" />
              预览
            </button>
            <button variant="outline" @click="editCourse">
              <pencil-icon class="w-4 h-4 mr-2" />
              编辑
            </button>
            <button variant="primary" @click="viewStudents">
              <users-icon class="w-4 h-4 mr-2" />
              学生管理
            </button>
          </div>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <card padding="lg" class="text-center">
          <div class="text-3xl font-bold text-blue-600 dark:text-blue-400 mb-2">
            {{ stats.totalStudents }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">已报名学生</p>
          <div class="mt-2 flex items-center justify-center space-x-1">
            <arrow-trending-up-icon class="w-4 h-4 text-green-500" />
            <span class="text-xs text-green-600">+{{ stats.newStudentsThisWeek }} 本周新增</span>
          </div>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-3xl font-bold text-green-600 dark:text-green-400 mb-2">
            {{ stats.completionRate }}%
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">平均完成率</p>
          <div class="mt-2">
            <progress :value="stats.completionRate" :max="100" size="sm" color="primary" />
          </div>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-3xl font-bold text-yellow-600 dark:text-yellow-400 mb-2">
            {{ stats.averageScore.toFixed(1) }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">平均成绩</p>
          <div class="mt-2 flex items-center justify-center space-x-1">
            <star-icon class="w-4 h-4 text-yellow-500" />
            <span class="text-xs text-gray-600">{{ stats.rating }}/5 评分</span>
          </div>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-3xl font-bold text-purple-600 dark:text-purple-400 mb-2">
            {{ stats.totalRevenue.toLocaleString() }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">总收入 (¥)</p>
          <div class="mt-2 flex items-center justify-center space-x-1">
            <currency-dollar-icon class="w-4 h-4 text-green-500" />
            <span class="text-xs text-green-600">+{{ stats.revenueGrowth }}% 本月</span>
          </div>
        </card>
      </div>

      <div class="grid grid-cols-1 xl:grid-cols-3 gap-8">
        <!-- 左侧内容 -->
        <div class="xl:col-span-2 space-y-8">
          <!-- 学习进度分析 -->
          <card padding="lg">
            <template #header>
              <div class="flex justify-between items-center">
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">学习进度分析</h2>
                <select v-model="progressTimeRange" @change="updateProgressChart" class="input input-sm">
                  <option value="7d">最近7天</option>
                  <option value="30d">最近30天</option>
                  <option value="90d">最近3个月</option>
                </select>
              </div>
            </template>
            <div ref="progressChartRef" class="h-80 w-full"></div>
          </card>

          <!-- 课程章节管理 -->
          <card padding="lg">
            <template #header>
              <div class="flex justify-between items-center">
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">课程内容</h2>
                <div class="flex items-center space-x-2">
                  <button variant="outline" size="sm" @click="addChapter">
                    <plus-icon class="w-4 h-4 mr-2" />
                    添加章节
                  </button>
                  <button variant="outline" size="sm" @click="reorderChapters">
                    <arrows-up-down-icon class="w-4 h-4 mr-2" />
                    重新排序
                  </button>
                </div>
              </div>
            </template>

            <div class="space-y-4">
              <div v-for="(chapter, index) in course.chapters" 
                   :key="chapter.id"
                   class="border border-gray-200 dark:border-gray-600 rounded-lg">
                <!-- 章节头部 -->
                <div class="p-4 bg-gray-50 dark:bg-gray-700 rounded-t-lg">
                  <div class="flex justify-between items-start">
                    <div class="flex-1">
                      <div class="flex items-center space-x-3">
                        <span class="text-sm font-medium text-gray-500 dark:text-gray-400">
                          第{{ index + 1 }}章
                        </span>
                        <h3 class="text-lg font-semibold text-gray-900 dark:text-white">
                          {{ chapter.title }}
                        </h3>
                        <badge variant="secondary" size="sm">
                          {{ chapter.lessons.length }} 课时
                        </badge>
                      </div>
                      <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
                        {{ chapter.description }}
                      </p>
                      
                      <!-- 章节统计 -->
                      <div class="grid grid-cols-3 gap-4 mt-3 text-sm">
                        <div>
                          <span class="text-gray-500">总时长：</span>
                          <span class="font-medium">{{ getTotalDuration(chapter.lessons) }}分钟</span>
                        </div>
                        <div>
                          <span class="text-gray-500">完成率：</span>
                          <span class="font-medium">{{ getChapterCompletionRate(chapter.id) }}%</span>
                        </div>
                        <div>
                          <span class="text-gray-500">平均评分：</span>
                          <span class="font-medium">{{ getChapterRating(chapter.id) }}/5</span>
                        </div>
                      </div>
                    </div>
                    
                    <div class="flex items-center space-x-2">
                      <button variant="ghost" size="sm" @click="editChapter(chapter)">
                        <pencil-icon class="w-4 h-4" />
                      </button>
                      <button variant="ghost" size="sm" @click="duplicateChapter(chapter)">
                        <document-duplicate-icon class="w-4 h-4" />
                      </button>
                      <button variant="ghost" size="sm" @click="deleteChapter(chapter.id)" class="text-red-600">
                        <trash-icon class="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                </div>
                
                <!-- 课时列表 -->
                <div class="p-4">
                  <div class="space-y-3">
                    <div v-for="(lesson, lessonIndex) in chapter.lessons"
                         :key="lesson.id"
                         class="flex items-center justify-between p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-600 rounded-lg">
                      <div class="flex items-center space-x-3">
                        <div class="flex-shrink-0">
                          <div class="w-8 h-8 rounded-full bg-primary-100 dark:bg-primary-900 flex items-center justify-center">
                            <span class="text-sm font-medium text-primary-600 dark:text-primary-400">
                              {{ lessonIndex + 1 }}
                            </span>
                          </div>
                        </div>
                        <div class="flex-1">
                          <h4 class="font-medium text-gray-900 dark:text-white">{{ lesson.title }}</h4>
                          <div class="flex items-center space-x-4 text-sm text-gray-500">
                            <span class="flex items-center">
                              <clock-icon class="w-3 h-3 mr-1" />
                              {{ lesson.duration }}分钟
                            </span>
                            <span class="flex items-center">
                              <tag-icon class="w-3 h-3 mr-1" />
                              {{ getLessonTypeText(lesson.type) }}
                            </span>
                            <span class="flex items-center">
                              <check-circle-icon class="w-3 h-3 mr-1" />
                              {{ getLessonCompletionRate(lesson.id) }}% 完成
                            </span>
                          </div>
                        </div>
                      </div>
                      
                      <div class="flex items-center space-x-2">
                        <button variant="ghost" size="sm" @click="viewLessonAnalytics(lesson)">
                          <chart-bar-icon class="w-4 h-4" />
                        </button>
                        <button variant="ghost" size="sm" @click="editLesson(lesson)">
                          <pencil-icon class="w-4 h-4" />
                        </button>
                        <button variant="ghost" size="sm" @click="deleteLesson(chapter.id, lesson.id)" class="text-red-600">
                          <x-mark-icon class="w-4 h-4" />
                        </button>
                      </div>
                    </div>
                    
                    <!-- 添加课时按钮 -->
                    <button
                      @click="addLesson(chapter.id)"
                      class="w-full p-3 border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg text-gray-500 hover:border-primary-500 hover:text-primary-600 transition-colors"
                    >
                      <plus-icon class="w-5 h-5 mx-auto mb-1" />
                      <span class="text-sm">添加课时</span>
                    </button>
                  </div>
                </div>
              </div>
              
              <!-- 添加章节占位符 -->
              <div v-if="course.chapters.length === 0" class="text-center py-12">
                <book-open-icon class="w-16 h-16 text-gray-400 mx-auto mb-4" />
                <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">暂无课程内容</h3>
                <p class="text-gray-600 dark:text-gray-400 mb-4">开始添加章节和课时来构建您的课程</p>
                <button @click="addChapter">
                  <plus-icon class="w-4 h-4 mr-2" />
                  添加第一个章节
                </button>
              </div>
            </div>
          </card>
        </div>

        <!-- 右侧侧边栏 -->
        <div class="space-y-6">
          <!-- 快速操作 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">快速操作</h3>
            </template>
            
            <div class="space-y-3">
              <button variant="outline" class="w-full justify-start" @click="viewAnalytics">
                <chart-bar-icon class="w-4 h-4 mr-3" />
                查看详细分析
              </button>
              <button variant="outline" class="w-full justify-start" @click="exportStudentData">
                <document-arrow-down-icon class="w-4 h-4 mr-3" />
                导出学生数据
              </button>
              <button variant="outline" class="w-full justify-start" @click="sendAnnouncement">
                <speaker-wave-icon class="w-4 h-4 mr-3" />
                发送公告
              </button>
              <button variant="outline" class="w-full justify-start" @click="manageCertificates">
                <academic-cap-icon class="w-4 h-4 mr-3" />
                证书管理
              </button>
              <button variant="outline" class="w-full justify-start" @click="courseSettings">
                <cog-icon class="w-4 h-4 mr-3" />
                课程设置
              </button>
            </div>
          </card>

          <!-- 最新学生活动 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">最新活动</h3>
            </template>
            
            <div class="space-y-4">
              <div v-for="activity in recentActivities" 
                   :key="activity.id"
                   class="flex items-start space-x-3">
                <div class="flex-shrink-0">
                  <div class="w-8 h-8 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
                    <user-icon class="w-4 h-4 text-blue-600 dark:text-blue-400" />
                  </div>
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm text-gray-900 dark:text-white">
                    <span class="font-medium">{{ activity.studentName }}</span>
                    {{ activity.action }}
                  </p>
                  <p class="text-xs text-gray-500 dark:text-gray-400">
                    {{ formatRelativeTime(activity.timestamp) }}
                  </p>
                </div>
              </div>
            </div>
            
            <div class="mt-4 pt-4 border-t border-gray-200 dark:border-gray-600">
              <button variant="outline" size="sm" class="w-full" @click="viewAllActivities">
                查看所有活动
              </button>
            </div>
          </card>

          <!-- 课程收入统计 -->
          <card padding="lg" v-if="course.price > 0">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">收入统计</h3>
            </template>
            
            <div class="space-y-4">
              <div class="text-center">
                <div class="text-2xl font-bold text-green-600 dark:text-green-400">
                  ¥{{ stats.totalRevenue.toLocaleString() }}
                </div>
                <p class="text-sm text-gray-600 dark:text-gray-400">总收入</p>
              </div>
              
              <div class="grid grid-cols-2 gap-4 text-sm">
                <div class="text-center">
                  <div class="font-semibold text-gray-900 dark:text-white">
                    ¥{{ stats.monthlyRevenue.toLocaleString() }}
                  </div>
                  <p class="text-gray-600 dark:text-gray-400">本月收入</p>
                </div>
                <div class="text-center">
                  <div class="font-semibold text-gray-900 dark:text-white">
                    {{ stats.paidStudents }}
                  </div>
                  <p class="text-gray-600 dark:text-gray-400">付费学生</p>
                </div>
              </div>
              
              <button variant="outline" size="sm" class="w-full" @click="viewRevenueDetails">
                查看收入详情
              </button>
            </div>
          </card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Progress from '@/components/ui/Progress.vue'
import * as echarts from 'echarts'
import {
  EyeIcon,
  PencilIcon,
  UsersIcon,
  ArrowTrendingUpIcon,
  StarIcon,
  CurrencyDollarIcon,
  PlusIcon,
  ArrowsUpDownIcon,
  TrashIcon,
  DocumentDuplicateIcon,
  ClockIcon,
  TagIcon,
  CheckCircleIcon,
  ChartBarIcon,
  XMarkIcon,
  BookOpenIcon,
  DocumentArrowDownIcon,
  SpeakerWaveIcon,
  AcademicCapIcon,
  CogIcon,
  UserIcon
} from '@heroicons/vue/24/outline'

// Route and Router
const route = useRoute()
const router = useRouter()
const uiStore = useUIStore()

// 状态
const progressTimeRange = ref('30d')
const progressChartRef = ref<HTMLElement>()
let progressChart: echarts.ECharts | null = null

// 模拟课程数据
const course = reactive({
  id: route.params.id as string,
  title: '高等数学基础教程',
  description: '系统学习高等数学的基础概念和应用，适合理工科学生和数学爱好者',
  content: '本课程涵盖极限、导数、积分等核心内容...',
  category: 'science',
  level: 'intermediate',
  status: 'published',
  price: 199,
  coverUrl: '',
  tags: ['数学', '高等数学', '理工科', '基础教程'],
  createdAt: '2024-01-15',
  chapters: [
    {
      id: '1',
      title: '函数与极限',
      description: '学习函数的基本概念和极限理论',
      lessons: [
        { id: '1-1', title: '函数的概念', type: 'video', duration: 45 },
        { id: '1-2', title: '极限的定义', type: 'video', duration: 50 },
        { id: '1-3', title: '极限的计算', type: 'video', duration: 55 }
      ]
    },
    {
      id: '2',
      title: '导数与微分',
      description: '掌握导数的计算方法和应用',
      lessons: [
        { id: '2-1', title: '导数的定义', type: 'video', duration: 40 },
        { id: '2-2', title: '导数的计算法则', type: 'video', duration: 60 },
        { id: '2-3', title: '导数的应用', type: 'quiz', duration: 30 }
      ]
    }
  ]
})

// 统计数据
const stats = reactive({
  totalStudents: 156,
  newStudentsThisWeek: 12,
  completionRate: 78,
  averageScore: 82.5,
  rating: 4.6,
  totalRevenue: 31044,
  monthlyRevenue: 5980,
  revenueGrowth: 15.2,
  paidStudents: 156
})

// 最新活动
const recentActivities = ref([
  {
    id: '1',
    studentName: '张同学',
    action: '完成了第2章的学习',
    timestamp: '2024-01-15T10:30:00Z'
  },
  {
    id: '2',
    studentName: '李同学',
    action: '提交了作业',
    timestamp: '2024-01-15T09:15:00Z'
  },
  {
    id: '3',
    studentName: '王同学',
    action: '加入了课程',
    timestamp: '2024-01-15T08:45:00Z'
  }
])

// 方法
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    published: '已发布',
    draft: '草稿',
    archived: '已归档'
  }
  return statusMap[status] || status
}

const getCategoryText = (category: string) => {
  const categoryMap: Record<string, string> = {
    programming: '编程开发',
    design: '设计创意',
    business: '商业管理',
    science: '科学技术'
  }
  return categoryMap[category] || category
}

const getLevelText = (level: string) => {
  const levelMap: Record<string, string> = {
    beginner: '初级',
    intermediate: '中级',
    advanced: '高级'
  }
  return levelMap[level] || level
}

const getLessonTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    video: '视频',
    document: '文档',
    quiz: '测验'
  }
  return typeMap[type] || type
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const formatRelativeTime = (timestamp: string) => {
  const now = new Date()
  const time = new Date(timestamp)
  const diff = now.getTime() - time.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  return `${days}天前`
}

const getTotalDuration = (lessons: any[]) => {
  return lessons.reduce((total, lesson) => total + lesson.duration, 0)
}

const getChapterCompletionRate = (chapterId: string) => {
  // 模拟数据
  return Math.floor(Math.random() * 30) + 70
}

const getChapterRating = (chapterId: string) => {
  // 模拟数据
  return (Math.random() * 1.5 + 3.5).toFixed(1)
}

const getLessonCompletionRate = (lessonId: string) => {
  // 模拟数据
  return Math.floor(Math.random() * 40) + 60
}

const initProgressChart = () => {
  if (!progressChartRef.value) return

  progressChart = echarts.init(progressChartRef.value)

  const option = {
    title: { show: false },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['学习进度', '测验成绩', '活跃度'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['第1周', '第2周', '第3周', '第4周', '第5周', '第6周', '第7周'],
      axisLine: { lineStyle: { color: '#e5e7eb' } }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      splitLine: { lineStyle: { color: '#f3f4f6' } }
    },
    series: [
      {
        name: '学习进度',
        type: 'line',
        smooth: true,
        data: [65, 72, 78, 82, 85, 88, 92],
        lineStyle: { color: '#3b82f6', width: 3 },
        itemStyle: { color: '#3b82f6' },
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
        name: '测验成绩',
        type: 'line',
        smooth: true,
        data: [70, 75, 80, 85, 82, 87, 89],
        lineStyle: { color: '#10b981', width: 3 },
        itemStyle: { color: '#10b981' }
      },
      {
        name: '活跃度',
        type: 'line',
        smooth: true,
        data: [60, 68, 75, 78, 80, 83, 86],
        lineStyle: { color: '#f59e0b', width: 3 },
        itemStyle: { color: '#f59e0b' }
      }
    ]
  }

  progressChart.setOption(option)
}

const updateProgressChart = () => {
  // 根据时间范围更新图表
  initProgressChart()
}

// 操作方法
const previewCourse = () => {
  window.open(`/preview/course/${course.id}`, '_blank')
}

const editCourse = () => {
  router.push(`/teacher/courses/${course.id}/edit`)
}

const viewStudents = () => {
  router.push(`/teacher/courses/${course.id}/students`)
}

const viewAnalytics = () => {
  router.push(`/teacher/courses/${course.id}/analytics`)
}

const addChapter = () => {
  // 实现添加章节逻辑
  uiStore.showNotification({
    type: 'info',
    title: '添加章节',
    message: '章节添加功能开发中...'
  })
}

const editChapter = (chapter: any) => {
  // 实现编辑章节逻辑
  uiStore.showNotification({
    type: 'info',
    title: '编辑章节',
    message: `编辑章节: ${chapter.title}`
  })
}

const deleteChapter = (chapterId: string) => {
  // 实现删除章节逻辑
  if (confirm('确定要删除这个章节吗？')) {
    const index = course.chapters.findIndex(c => c.id === chapterId)
    if (index > -1) {
      course.chapters.splice(index, 1)
      uiStore.showNotification({
        type: 'success',
        title: '删除成功',
        message: '章节已删除'
      })
    }
  }
}

const addLesson = (chapterId: string) => {
  // 实现添加课时逻辑
  uiStore.showNotification({
    type: 'info',
    title: '添加课时',
    message: '课时添加功能开发中...'
  })
}

const exportStudentData = async () => {
  try {
    uiStore.showNotification({
      type: 'info',
      title: '导出中...',
      message: '正在生成学生数据报告'
    })
    
    // 模拟导出
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    uiStore.showNotification({
      type: 'success',
      title: '导出成功',
      message: '学生数据已下载到本地'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '导出失败',
      message: '导出数据时发生错误'
    })
  }
}

const resizeChart = () => {
  progressChart?.resize()
}

// 生命周期
onMounted(() => {
  nextTick(() => {
    initProgressChart()
  })
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  progressChart?.dispose()
  window.removeEventListener('resize', resizeChart)
})
</script> 