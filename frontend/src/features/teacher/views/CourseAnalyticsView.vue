<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <div class="max-w-7xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <nav class="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400 mb-2">
              <router-link to="/teacher/courses" class="hover:text-gray-700 dark:hover:text-gray-200">
                课程管理
              </router-link>
              <chevron-right-icon class="w-4 h-4" />
              <router-link :to="`/teacher/courses/${courseId}`" class="hover:text-gray-700 dark:hover:text-gray-200">
                {{ course.title }}
              </router-link>
              <chevron-right-icon class="w-4 h-4" />
              <span>课程分析</span>
            </nav>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">课程分析</h1>
            <p class="text-gray-600 dark:text-gray-400">深度分析课程学习效果和学生表现</p>
          </div>
          <div class="flex items-center space-x-3">
            <select v-model="timeRange" @change="updateAnalytics" class="input">
              <option value="1week">最近一周</option>
              <option value="1month">最近一月</option>
              <option value="3months">最近三月</option>
              <option value="1year">最近一年</option>
            </select>
            <button variant="outline" @click="exportReport">
              <document-arrow-down-icon class="w-4 h-4 mr-2" />
              导出报告
            </button>
          </div>
        </div>
      </div>

      <!-- 课程基本信息 -->
      <card padding="lg" class="mb-8">
        <div class="flex items-start space-x-6">
          <img
            :src="course.coverUrl || '/api/placeholder/120/80'"
            :alt="course.title"
            class="w-20 h-16 object-cover rounded-lg"
          />
          <div class="flex-1">
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-2">{{ course.title }}</h2>
            <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
              <div>
                <span class="text-gray-500 dark:text-gray-400">学生总数：</span>
                <span class="font-medium text-gray-900 dark:text-white">{{ analytics.totalStudents }}</span>
              </div>
              <div>
                <span class="text-gray-500 dark:text-gray-400">活跃学生：</span>
                <span class="font-medium text-gray-900 dark:text-white">{{ analytics.activeStudents }}</span>
              </div>
              <div>
                <span class="text-gray-500 dark:text-gray-400">总课时：</span>
                <span class="font-medium text-gray-900 dark:text-white">{{ analytics.totalLessons }}</span>
              </div>
              <div>
                <span class="text-gray-500 dark:text-gray-400">平均完成率：</span>
                <span class="font-medium text-gray-900 dark:text-white">{{ analytics.avgCompletionRate }}%</span>
              </div>
            </div>
          </div>
        </div>
      </card>

      <!-- 核心指标 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <card padding="lg" class="text-center">
          <div class="text-3xl font-bold text-blue-600 dark:text-blue-400 mb-2">
            {{ analytics.engagementRate }}%
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400 mb-2">学习参与度</p>
          <div class="flex items-center justify-center space-x-1">
            <arrow-trending-up-icon class="w-4 h-4 text-green-500" />
            <span class="text-xs text-green-600">+5.2% 相比上周</span>
          </div>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-3xl font-bold text-green-600 dark:text-green-400 mb-2">
            {{ analytics.averageScore.toFixed(1) }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400 mb-2">平均成绩</p>
          <div class="flex items-center justify-center space-x-1">
            <arrow-trending-up-icon class="w-4 h-4 text-green-500" />
            <span class="text-xs text-green-600">+2.1 分相比上周</span>
          </div>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-3xl font-bold text-yellow-600 dark:text-yellow-400 mb-2">
            {{ analytics.completionRate }}%
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400 mb-2">课程完成率</p>
          <div class="flex items-center justify-center space-x-1">
            <arrow-trending-up-icon class="w-4 h-4 text-green-500" />
            <span class="text-xs text-green-600">+3.8% 相比上周</span>
          </div>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-3xl font-bold text-purple-600 dark:text-purple-400 mb-2">
            {{ analytics.satisfactionScore.toFixed(1) }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400 mb-2">满意度评分</p>
          <div class="flex items-center justify-center space-x-1">
            <star-icon class="w-4 h-4 text-yellow-500" />
            <span class="text-xs text-gray-600">基于 {{ analytics.reviewCount }} 条评价</span>
          </div>
        </card>
      </div>

      <!-- 图表分析 -->
      <div class="grid grid-cols-1 xl:grid-cols-2 gap-8 mb-8">
        <!-- 学习进度趋势 -->
        <card padding="lg">
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">学习进度趋势</h3>
          </template>
          <div ref="progressTrendRef" class="h-80 w-full"></div>
        </card>

        <!-- 成绩分布 -->
        <card padding="lg">
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">成绩分布</h3>
          </template>
          <div ref="scoreDistributionRef" class="h-80 w-full"></div>
        </card>
      </div>

      <!-- 章节分析 -->
      <card padding="lg" class="mb-8">
        <template #header>
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">章节学习分析</h3>
        </template>
        <div ref="chapterAnalysisRef" class="h-96 w-full"></div>
      </card>

      <!-- 详细数据表格 -->
      <div class="grid grid-cols-1 xl:grid-cols-2 gap-8">
        <!-- 学生学习排行 -->
        <card padding="lg">
          <template #header>
            <div class="flex justify-between items-center">
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">学生学习排行</h3>
              <button variant="outline" size="sm" @click="viewAllStudents">
                查看全部
              </button>
            </div>
          </template>
          
          <div class="space-y-4">
            <div
              v-for="(student, index) in topStudents"
              :key="student.id"
              class="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg"
            >
              <div class="flex items-center space-x-3">
                <div class="flex-shrink-0">
                  <div class="w-8 h-8 rounded-full flex items-center justify-center text-white font-medium text-sm"
                       :class="index === 0 ? 'bg-yellow-500' : index === 1 ? 'bg-gray-400' : index === 2 ? 'bg-yellow-600' : 'bg-blue-500'">
                    {{ index + 1 }}
                  </div>
                </div>
                <div>
                  <h4 class="font-medium text-gray-900 dark:text-white">{{ student.name }}</h4>
                  <p class="text-sm text-gray-500 dark:text-gray-400">{{ student.studentId }}</p>
                </div>
              </div>
              <div class="text-right">
                <p class="text-sm font-medium text-gray-900 dark:text-white">
                  {{ student.progress }}% 完成
                </p>
                <p class="text-xs text-gray-500 dark:text-gray-400">
                  平均分: {{ student.averageScore }}
                </p>
              </div>
            </div>
          </div>
        </card>

        <!-- 课程难点分析 -->
        <card padding="lg">
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">课程难点分析</h3>
          </template>
          
          <div class="space-y-4">
            <div
              v-for="difficulty in difficultyAnalysis"
              :key="difficulty.id"
              class="border border-gray-200 dark:border-gray-600 rounded-lg p-4"
            >
              <div class="flex justify-between items-start mb-2">
                <h4 class="font-medium text-gray-900 dark:text-white">{{ difficulty.title }}</h4>
                <badge :variant="difficulty.level === 'high' ? 'danger' : difficulty.level === 'medium' ? 'warning' : 'secondary'">
                  {{ getDifficultyText(difficulty.level) }}
                </badge>
              </div>
              <p class="text-sm text-gray-600 dark:text-gray-400 mb-3">{{ difficulty.description }}</p>
              <div class="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span class="text-gray-500">完成率：</span>
                  <span class="font-medium">{{ difficulty.completionRate }}%</span>
                </div>
                <div>
                  <span class="text-gray-500">平均用时：</span>
                  <span class="font-medium">{{ difficulty.averageTime }}分钟</span>
                </div>
                <div>
                  <span class="text-gray-500">出错率：</span>
                  <span class="font-medium">{{ difficulty.errorRate }}%</span>
                </div>
                <div>
                  <span class="text-gray-500">求助次数：</span>
                  <span class="font-medium">{{ difficulty.helpRequests }}</span>
                </div>
              </div>
              <div class="mt-3">
                <progress :value="difficulty.completionRate" :max="100" size="sm" color="primary" />
              </div>
            </div>
          </div>
        </card>
      </div>

      <!-- 学习行为分析 -->
      <card padding="lg" class="mt-8">
        <template #header>
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">学习行为分析</h3>
        </template>
        
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div class="text-center">
            <div class="text-2xl font-bold text-blue-600 dark:text-blue-400 mb-2">
              {{ behaviorAnalysis.avgStudyTime }}
            </div>
            <p class="text-sm text-gray-600 dark:text-gray-400">平均学习时长</p>
            <p class="text-xs text-gray-500 mt-1">每次会话</p>
          </div>
          
          <div class="text-center">
            <div class="text-2xl font-bold text-green-600 dark:text-green-400 mb-2">
              {{ behaviorAnalysis.peakHours }}
            </div>
            <p class="text-sm text-gray-600 dark:text-gray-400">学习高峰时段</p>
            <p class="text-xs text-gray-500 mt-1">最活跃时间</p>
          </div>
          
          <div class="text-center">
            <div class="text-2xl font-bold text-purple-600 dark:text-purple-400 mb-2">
              {{ behaviorAnalysis.avgSessions }}
            </div>
            <p class="text-sm text-gray-600 dark:text-gray-400">平均学习频次</p>
            <p class="text-xs text-gray-500 mt-1">每周次数</p>
          </div>
        </div>
      </card>

      <!-- AI 分析建议 -->
      <card padding="lg" class="mt-8">
        <template #header>
          <div class="flex items-center">
            <sparkles-icon class="w-5 h-5 text-yellow-500 mr-2" />
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">AI 分析建议</h3>
          </div>
        </template>
        
        <div class="space-y-4">
          <div
            v-for="suggestion in aiSuggestions"
            :key="suggestion.id"
            class="p-4 border border-gray-200 dark:border-gray-600 rounded-lg"
          >
            <div class="flex items-start space-x-3">
              <div class="flex-shrink-0">
                <div class="w-8 h-8 rounded-full bg-yellow-100 dark:bg-yellow-900 flex items-center justify-center">
                  <light-bulb-icon class="w-4 h-4 text-yellow-600 dark:text-yellow-400" />
                </div>
              </div>
              <div class="flex-1">
                <h4 class="font-medium text-gray-900 dark:text-white mb-1">{{ suggestion.title }}</h4>
                <p class="text-sm text-gray-600 dark:text-gray-400 mb-2">{{ suggestion.description }}</p>
                <div class="flex items-center space-x-4 text-xs text-gray-500">
                  <span>置信度: {{ suggestion.confidence }}%</span>
                  <span>影响等级: {{ suggestion.impact }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </card>
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
  ChevronRightIcon,
  DocumentArrowDownIcon,
  ArrowTrendingUpIcon,
  StarIcon,
  SparklesIcon,
  LightBulbIcon
} from '@heroicons/vue/24/outline'

// Route and Router
const route = useRoute()
const router = useRouter()
const uiStore = useUIStore()

// 状态
const timeRange = ref('1month')
const courseId = route.params.id as string

// 图表引用
const progressTrendRef = ref<HTMLElement>()
const scoreDistributionRef = ref<HTMLElement>()
const chapterAnalysisRef = ref<HTMLElement>()

let progressTrendChart: echarts.ECharts | null = null
let scoreDistributionChart: echarts.ECharts | null = null
let chapterAnalysisChart: echarts.ECharts | null = null

// 课程基本信息
const course = reactive({
  id: courseId,
  title: '高等数学基础教程',
  coverUrl: ''
})

// 分析数据
const analytics = reactive({
  totalStudents: 156,
  activeStudents: 132,
  totalLessons: 45,
  avgCompletionRate: 78,
  engagementRate: 82,
  averageScore: 83.5,
  completionRate: 76,
  satisfactionScore: 4.3,
  reviewCount: 89
})

// 学习行为分析
const behaviorAnalysis = reactive({
  avgStudyTime: '45分钟',
  peakHours: '19:00-21:00',
  avgSessions: '3.2次'
})

// 学生排行
const topStudents = ref([
  { id: '1', name: '张同学', studentId: '2021001001', progress: 95, averageScore: 92 },
  { id: '2', name: '李同学', studentId: '2021001002', progress: 88, averageScore: 89 },
  { id: '3', name: '王同学', studentId: '2021001003', progress: 85, averageScore: 87 },
  { id: '4', name: '刘同学', studentId: '2021001004', progress: 82, averageScore: 85 },
  { id: '5', name: '陈同学', studentId: '2021001005', progress: 80, averageScore: 83 }
])

// 难点分析
const difficultyAnalysis = ref([
  {
    id: '1',
    title: '极限的计算',
    description: '学生在极限计算方面遇到较大困难，特别是无穷小量的比较',
    level: 'high',
    completionRate: 65,
    averageTime: 25,
    errorRate: 35,
    helpRequests: 28
  },
  {
    id: '2',
    title: '导数的应用',
    description: '导数在实际问题中的应用理解不够深入',
    level: 'medium',
    completionRate: 78,
    averageTime: 18,
    errorRate: 22,
    helpRequests: 15
  },
  {
    id: '3',
    title: '积分计算',
    description: '分部积分和换元积分掌握情况良好',
    level: 'low',
    completionRate: 92,
    averageTime: 12,
    errorRate: 8,
    helpRequests: 3
  }
])

// AI 建议
const aiSuggestions = ref([
  {
    id: '1',
    title: '增加极限计算练习',
    description: '建议在极限章节增加更多阶梯式练习题，帮助学生逐步掌握计算技巧',
    confidence: 87,
    impact: '高'
  },
  {
    id: '2',
    title: '优化课程节奏',
    description: '根据学习数据，建议将课程3-5章的进度适当放缓，增加复习时间',
    confidence: 75,
    impact: '中'
  },
  {
    id: '3',
    title: '加强互动环节',
    description: '学生参与度数据显示，增加课堂互动和小组讨论能提高学习效果',
    confidence: 82,
    impact: '高'
  }
])

// 方法
const getDifficultyText = (level: string) => {
  switch (level) {
    case 'high': return '高难度'
    case 'medium': return '中等难度'
    case 'low': return '较简单'
    default: return '未知'
  }
}

const initCharts = () => {
  initProgressTrendChart()
  initScoreDistributionChart()
  initChapterAnalysisChart()
}

const initProgressTrendChart = () => {
  if (!progressTrendRef.value) return

  progressTrendChart = echarts.init(progressTrendRef.value)

  const option = {
    title: { show: false },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['学习进度', '参与度', '完成率'],
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
      data: ['第1周', '第2周', '第3周', '第4周', '第5周', '第6周', '第7周', '第8周'],
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
        data: [15, 28, 42, 55, 68, 75, 82, 88],
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
        name: '参与度',
        type: 'line',
        smooth: true,
        data: [65, 72, 78, 85, 82, 88, 85, 90],
        lineStyle: { color: '#10b981', width: 3 },
        itemStyle: { color: '#10b981' }
      },
      {
        name: '完成率',
        type: 'line',
        smooth: true,
        data: [60, 68, 75, 82, 78, 85, 88, 92],
        lineStyle: { color: '#f59e0b', width: 3 },
        itemStyle: { color: '#f59e0b' }
      }
    ]
  }

  progressTrendChart.setOption(option)
}

const initScoreDistributionChart = () => {
  if (!scoreDistributionRef.value) return

  scoreDistributionChart = echarts.init(scoreDistributionRef.value)

  const option = {
    title: { show: false },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['学生人数'],
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
      data: ['0-59', '60-69', '70-79', '80-89', '90-100'],
      axisLine: { lineStyle: { color: '#e5e7eb' } }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      splitLine: { lineStyle: { color: '#f3f4f6' } }
    },
    series: [
      {
        name: '学生人数',
        type: 'bar',
        data: [5, 12, 28, 65, 46],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#3b82f6' },
            { offset: 1, color: '#1d4ed8' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2563eb' },
              { offset: 1, color: '#1e40af' }
            ])
          }
        }
      }
    ]
  }

  scoreDistributionChart.setOption(option)
}

const initChapterAnalysisChart = () => {
  if (!chapterAnalysisRef.value) return

  chapterAnalysisChart = echarts.init(chapterAnalysisRef.value)

  const option = {
    title: { show: false },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['完成率', '平均得分', '学习时长'],
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
      data: ['第1章', '第2章', '第3章', '第4章', '第5章', '第6章', '第7章'],
      axisLine: { lineStyle: { color: '#e5e7eb' } }
    },
    yAxis: [
      {
        type: 'value',
        name: '百分比',
        position: 'left',
        axisLine: { lineStyle: { color: '#e5e7eb' } },
        splitLine: { lineStyle: { color: '#f3f4f6' } }
      },
      {
        type: 'value',
        name: '时长(小时)',
        position: 'right',
        axisLine: { lineStyle: { color: '#e5e7eb' } },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '完成率',
        type: 'bar',
        data: [95, 88, 76, 65, 58, 42, 28],
        itemStyle: { color: '#10b981' }
      },
      {
        name: '平均得分',
        type: 'bar',
        data: [92, 85, 78, 72, 68, 65, 60],
        itemStyle: { color: '#3b82f6' }
      },
      {
        name: '学习时长',
        type: 'line',
        yAxisIndex: 1,
        data: [2.5, 3.2, 4.1, 5.8, 6.5, 7.2, 8.1],
        lineStyle: { color: '#f59e0b', width: 3 },
        itemStyle: { color: '#f59e0b' }
      }
    ]
  }

  chapterAnalysisChart.setOption(option)
}

const updateAnalytics = () => {
  // 根据时间范围更新数据
  initCharts()
}

const viewAllStudents = () => {
  router.push(`/teacher/courses/${courseId}/students`)
}

const exportReport = async () => {
  try {
    uiStore.showNotification({
      type: 'info',
      title: '导出中...',
      message: '正在生成课程分析报告'
    })

    // 模拟导出延迟
    await new Promise(resolve => setTimeout(resolve, 2000))

    uiStore.showNotification({
      type: 'success',
      title: '导出成功',
      message: '课程分析报告已下载到本地'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '导出失败',
      message: '导出报告时发生错误'
    })
  }
}

const resizeCharts = () => {
  progressTrendChart?.resize()
  scoreDistributionChart?.resize()
  chapterAnalysisChart?.resize()
}

// 生命周期
onMounted(() => {
  nextTick(() => {
    initCharts()
  })
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  progressTrendChart?.dispose()
  scoreDistributionChart?.dispose()
  chapterAnalysisChart?.dispose()
  window.removeEventListener('resize', resizeCharts)
})
</script> 