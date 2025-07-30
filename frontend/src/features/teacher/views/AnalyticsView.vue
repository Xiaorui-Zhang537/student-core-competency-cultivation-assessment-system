<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <!-- 页面标题 -->
    <div class="mb-8">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">数据分析</h1>
          <p class="text-gray-600 dark:text-gray-400">全面了解教学效果和学生学习情况</p>
        </div>
        <div class="flex items-center space-x-4">
          <select v-model="timeRange" @change="updateCharts" class="input">
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

    <!-- 核心指标卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <card padding="lg" class="text-center">
        <div class="text-3xl font-bold text-blue-600 dark:text-blue-400 mb-2">
          {{ analyticsData.totalStudents }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">总学生数</p>
        <div class="mt-2 flex items-center justify-center space-x-1">
          <arrow-trending-up-icon class="w-4 h-4 text-green-500" />
          <span class="text-xs text-green-600">+12% 相比上月</span>
        </div>
      </card>

      <card padding="lg" class="text-center">
        <div class="text-3xl font-bold text-green-600 dark:text-green-400 mb-2">
          {{ analyticsData.averageScore.toFixed(1) }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">平均成绩</p>
        <div class="mt-2 flex items-center justify-center space-x-1">
          <arrow-trending-up-icon class="w-4 h-4 text-green-500" />
          <span class="text-xs text-green-600">+5.2% 相比上月</span>
        </div>
      </card>

      <card padding="lg" class="text-center">
        <div class="text-3xl font-bold text-yellow-600 dark:text-yellow-400 mb-2">
          {{ analyticsData.completionRate }}%
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">作业完成率</p>
        <div class="mt-2 flex items-center justify-center space-x-1">
          <arrow-trending-down-icon class="w-4 h-4 text-red-500" />
          <span class="text-xs text-red-600">-2.1% 相比上月</span>
        </div>
      </card>

      <card padding="lg" class="text-center">
        <div class="text-3xl font-bold text-purple-600 dark:text-purple-400 mb-2">
          {{ analyticsData.engagementScore }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">参与度评分</p>
        <div class="mt-2 flex items-center justify-center space-x-1">
          <arrow-trending-up-icon class="w-4 h-4 text-green-500" />
          <span class="text-xs text-green-600">+8.3% 相比上月</span>
        </div>
      </card>
    </div>

    <!-- 图表区域 -->
    <div class="grid grid-cols-1 xl:grid-cols-2 gap-8 mb-8">
      <!-- 学习趋势图 -->
      <card padding="lg">
        <template #header>
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">学习趋势分析</h3>
        </template>
        <div ref="learningTrendRef" class="h-80 w-full"></div>
      </card>

      <!-- 成绩分布图 -->
      <card padding="lg">
        <template #header>
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">成绩分布</h3>
        </template>
        <div ref="scoreDistributionRef" class="h-80 w-full"></div>
      </card>
    </div>

    <!-- 课程表现分析 -->
    <card padding="lg" class="mb-8">
      <template #header>
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">课程表现分析</h3>
          <select v-model="selectedCourse" @change="updateCourseAnalytics" class="input input-sm">
            <option value="">全部课程</option>
            <option v-for="course in courses" :key="course.id" :value="course.id">
              {{ course.name }}
            </option>
          </select>
        </div>
      </template>
      <div ref="coursePerformanceRef" class="h-96 w-full"></div>
    </card>

    <!-- 详细数据表格 -->
    <div class="grid grid-cols-1 xl:grid-cols-2 gap-8">
      <!-- 学生表现排行 -->
      <card padding="lg">
        <template #header>
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">学生表现排行</h3>
        </template>
        <div class="space-y-4">
          <div v-for="(student, index) in topStudents" 
               :key="student.id"
               class="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
            <div class="flex items-center space-x-3">
              <div class="flex-shrink-0">
                <div :class="[
                  'w-8 h-8 rounded-full flex items-center justify-center text-white font-medium text-sm',
                  index === 0 ? 'bg-yellow-500' :
                  index === 1 ? 'bg-gray-400' :
                  index === 2 ? 'bg-yellow-600' : 'bg-blue-500'
                ]">
                  {{ index + 1 }}
                </div>
              </div>
              <div>
                <h4 class="font-medium text-gray-900 dark:text-white">{{ student.name }}</h4>
                <p class="text-sm text-gray-500 dark:text-gray-400">{{ student.course }}</p>
              </div>
            </div>
            <div class="text-right">
              <p class="text-sm font-medium text-gray-900 dark:text-white">
                {{ student.score }} 分
              </p>
              <p class="text-xs text-gray-500 dark:text-gray-400">
                {{ student.improvement }}% 提升
              </p>
            </div>
          </div>
        </div>
      </card>

      <!-- 课程统计 -->
      <card padding="lg">
        <template #header>
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">课程统计</h3>
        </template>
        <div class="space-y-4">
          <div v-for="course in courseStats" 
               :key="course.id"
               class="border border-gray-200 dark:border-gray-600 rounded-lg p-4">
            <div class="flex justify-between items-start mb-3">
              <div>
                <h4 class="font-medium text-gray-900 dark:text-white">{{ course.name }}</h4>
                <p class="text-sm text-gray-500 dark:text-gray-400">{{ course.students }} 名学生</p>
              </div>
              <badge :variant="course.performance >= 80 ? 'success' : course.performance >= 60 ? 'warning' : 'danger'">
                {{ course.performance }}%
              </badge>
            </div>
            <div class="grid grid-cols-2 gap-4 text-sm">
              <div>
                <span class="text-gray-600 dark:text-gray-400">完成率：</span>
                <span class="font-medium text-gray-900 dark:text-white">{{ course.completionRate }}%</span>
              </div>
              <div>
                <span class="text-gray-600 dark:text-gray-400">平均分：</span>
                <span class="font-medium text-gray-900 dark:text-white">{{ course.averageScore }}</span>
              </div>
              <div>
                <span class="text-gray-600 dark:text-gray-400">参与度：</span>
                <span class="font-medium text-gray-900 dark:text-white">{{ course.engagement }}%</span>
              </div>
              <div>
                <span class="text-gray-600 dark:text-gray-400">满意度：</span>
                <span class="font-medium text-gray-900 dark:text-white">{{ course.satisfaction }}/5</span>
              </div>
            </div>
            <div class="mt-3">
              <progress :value="course.performance" :max="100" size="sm" color="primary" />
            </div>
          </div>
        </div>
      </card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useTeacherStore } from '@/stores/teacher'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Progress from '@/components/ui/Progress.vue'
import * as echarts from 'echarts'
import {
  DocumentArrowDownIcon,
  ArrowTrendingUpIcon,
  ArrowTrendingDownIcon
} from '@heroicons/vue/24/outline'

// Stores
const teacherStore = useTeacherStore()
const uiStore = useUIStore()

// 状态
const timeRange = ref('1month')
const selectedCourse = ref('')

// 图表引用
const learningTrendRef = ref<HTMLElement>()
const scoreDistributionRef = ref<HTMLElement>()
const coursePerformanceRef = ref<HTMLElement>()

let learningTrendChart: echarts.ECharts | null = null
let scoreDistributionChart: echarts.ECharts | null = null
let coursePerformanceChart: echarts.ECharts | null = null

// 模拟数据
const analyticsData = ref({
  totalStudents: 156,
  averageScore: 82.7,
  completionRate: 87,
  engagementScore: 4.2
})

const courses = ref([
  { id: '1', name: '高等数学' },
  { id: '2', name: '线性代数' },
  { id: '3', name: '概率论与数理统计' },
  { id: '4', name: '离散数学' }
])

const topStudents = ref([
  { id: '1', name: '张同学', course: '高等数学', score: 95, improvement: 12 },
  { id: '2', name: '李同学', course: '线性代数', score: 92, improvement: 8 },
  { id: '3', name: '王同学', course: '概率论', score: 90, improvement: 15 },
  { id: '4', name: '刘同学', course: '离散数学', score: 88, improvement: 6 },
  { id: '5', name: '陈同学', course: '高等数学', score: 87, improvement: 10 }
])

const courseStats = ref([
  {
    id: '1',
    name: '高等数学',
    students: 45,
    performance: 85,
    completionRate: 92,
    averageScore: 83.5,
    engagement: 88,
    satisfaction: 4.3
  },
  {
    id: '2',
    name: '线性代数',
    students: 38,
    performance: 78,
    completionRate: 85,
    averageScore: 79.2,
    engagement: 82,
    satisfaction: 4.1
  },
  {
    id: '3',
    name: '概率论与数理统计',
    students: 41,
    performance: 82,
    completionRate: 89,
    averageScore: 81.8,
    engagement: 85,
    satisfaction: 4.2
  },
  {
    id: '4',
    name: '离散数学',
    students: 32,
    performance: 75,
    completionRate: 80,
    averageScore: 76.4,
    engagement: 79,
    satisfaction: 3.9
  }
])

// 方法
const initCharts = () => {
  initLearningTrendChart()
  initScoreDistributionChart()
  initCoursePerformanceChart()
}

const initLearningTrendChart = () => {
  if (!learningTrendRef.value) return

  learningTrendChart = echarts.init(learningTrendRef.value)

  const option = {
    title: {
      show: false
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['平均成绩', '完成率', '参与度'],
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
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月'],
      axisLine: {
        lineStyle: { color: '#e5e7eb' }
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: { color: '#e5e7eb' }
      },
      splitLine: {
        lineStyle: { color: '#f3f4f6' }
      }
    },
    series: [
      {
        name: '平均成绩',
        type: 'line',
        smooth: true,
        data: [75, 78, 82, 85, 83, 87, 85],
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
        name: '完成率',
        type: 'line',
        smooth: true,
        data: [82, 85, 88, 90, 87, 92, 89],
        lineStyle: { color: '#10b981', width: 3 },
        itemStyle: { color: '#10b981' }
      },
      {
        name: '参与度',
        type: 'line',
        smooth: true,
        data: [70, 75, 80, 85, 82, 88, 86],
        lineStyle: { color: '#f59e0b', width: 3 },
        itemStyle: { color: '#f59e0b' }
      }
    ]
  }

  learningTrendChart.setOption(option)
}

const initScoreDistributionChart = () => {
  if (!scoreDistributionRef.value) return

  scoreDistributionChart = echarts.init(scoreDistributionRef.value)

  const option = {
    title: {
      show: false
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '成绩分布',
        type: 'pie',
        radius: ['50%', '70%'],
        center: ['60%', '50%'],
        data: [
          { value: 35, name: '优秀(90-100分)', itemStyle: { color: '#10b981' } },
          { value: 45, name: '良好(80-89分)', itemStyle: { color: '#3b82f6' } },
          { value: 15, name: '中等(70-79分)', itemStyle: { color: '#f59e0b' } },
          { value: 5, name: '需改进(<70分)', itemStyle: { color: '#ef4444' } }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        label: {
          show: true,
          formatter: '{b}: {d}%'
        }
      }
    ]
  }

  scoreDistributionChart.setOption(option)
}

const initCoursePerformanceChart = () => {
  if (!coursePerformanceRef.value) return

  coursePerformanceChart = echarts.init(coursePerformanceRef.value)

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
      data: ['学生数量', '平均成绩', '完成率'],
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
      data: ['高等数学', '线性代数', '概率论', '离散数学'],
      axisLine: {
        lineStyle: { color: '#e5e7eb' }
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '人数',
        position: 'left',
        axisLine: {
          lineStyle: { color: '#e5e7eb' }
        },
        splitLine: {
          lineStyle: { color: '#f3f4f6' }
        }
      },
      {
        type: 'value',
        name: '百分比',
        position: 'right',
        axisLine: {
          lineStyle: { color: '#e5e7eb' }
        },
        splitLine: {
          show: false
        }
      }
    ],
    series: [
      {
        name: '学生数量',
        type: 'bar',
        data: [45, 38, 41, 32],
        itemStyle: { color: '#3b82f6' }
      },
      {
        name: '平均成绩',
        type: 'line',
        yAxisIndex: 1,
        data: [83.5, 79.2, 81.8, 76.4],
        lineStyle: { color: '#10b981', width: 3 },
        itemStyle: { color: '#10b981' }
      },
      {
        name: '完成率',
        type: 'line',
        yAxisIndex: 1,
        data: [92, 85, 89, 80],
        lineStyle: { color: '#f59e0b', width: 3 },
        itemStyle: { color: '#f59e0b' }
      }
    ]
  }

  coursePerformanceChart.setOption(option)
}

const updateCharts = () => {
  // 根据时间范围更新图表数据
  initCharts()
}

const updateCourseAnalytics = () => {
  // 根据选中课程更新分析数据
  initCoursePerformanceChart()
}

const exportReport = async () => {
  try {
    uiStore.showNotification({
      type: 'info',
      title: '导出中...',
      message: '正在生成分析报告'
    })

    // 模拟导出延迟
    await new Promise(resolve => setTimeout(resolve, 2000))

    uiStore.showNotification({
      type: 'success',
      title: '导出成功',
      message: '分析报告已下载到本地'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '导出失败',
      message: '导出分析报告时发生错误'
    })
  }
}

const resizeCharts = () => {
  learningTrendChart?.resize()
  scoreDistributionChart?.resize()
  coursePerformanceChart?.resize()
}

// 生命周期
onMounted(() => {
  nextTick(() => {
    initCharts()
  })
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  learningTrendChart?.dispose()
  scoreDistributionChart?.dispose()
  coursePerformanceChart?.dispose()
  window.removeEventListener('resize', resizeCharts)
})
</script> 