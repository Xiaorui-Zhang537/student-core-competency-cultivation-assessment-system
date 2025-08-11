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
        <select v-model="selectedCourseId" @change="onCourseChange" class="input">
            <option :value="null">请选择课程</option>
            <option v-for="course in teacherCourses" :key="course.id" :value="String(course.id)">
              {{ course.title }}
            </option>
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
          {{ safeCourseAnalytics.totalStudents }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">总学生数</p>
      </card>

      <card padding="lg" class="text-center">
        <div class="text-3xl font-bold text-green-600 dark:text-green-400 mb-2">
          {{ (safeCourseAnalytics.averageScore || 0).toFixed(1) }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">平均成绩</p>
      </card>

      <card padding="lg" class="text-center">
        <div class="text-3xl font-bold text-yellow-600 dark:text-yellow-400 mb-2">
          {{ (safeCourseAnalytics.completionRate || 0) }}%
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">作业完成率</p>
      </card>

      <card padding="lg" class="text-center">
        <div class="text-3xl font-bold text-purple-600 dark:text-purple-400 mb-2">
          {{ safeCourseAnalytics.activeStudents }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">活跃学生</p>
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
           <select v-model="selectedCourseId" @change="onCourseChange" class="input input-sm">
            <option :value="null">请选择课程</option>
            <option v-for="course in teacherCourses" :key="course.id" :value="String(course.id)">
              {{ course.title }}
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
          <div v-if="!topStudents.length" class="space-y-4 text-center text-gray-500 dark:text-gray-400">
            暂无学生排行数据
          </div>
          <ul v-else class="divide-y divide-gray-200 dark:divide-gray-700">
            <li v-for="(s, idx) in topStudents" :key="s.studentId" class="py-2 flex items-center justify-between">
              <div class="flex items-center gap-3">
                <span class="text-sm w-6 text-center font-semibold text-gray-600 dark:text-gray-300">{{ idx + 1 }}</span>
                <span class="text-sm text-gray-900 dark:text-gray-100">{{ s.studentName }}</span>
              </div>
              <div class="text-sm text-gray-700 dark:text-gray-200">{{ (s.averageGrade ?? 0).toFixed(1) }}</div>
            </li>
          </ul>
      </card>

      <!-- 课程统计 -->
      <card padding="lg">
        <template #header>
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">课程统计</h3>
        </template>
         <div class="grid grid-cols-2 gap-4">
           <div class="p-4 rounded bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700">
             <div class="text-xs text-gray-500 mb-1">总学生数</div>
             <div class="text-xl font-semibold">{{ safeClassPerformance.totalStudents }}</div>
           </div>
           <div class="p-4 rounded bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700">
             <div class="text-xs text-gray-500 mb-1">平均成绩</div>
             <div class="text-xl font-semibold">{{ (safeCourseAnalytics.averageScore || 0).toFixed(1) }}</div>
           </div>
           <div class="p-4 rounded bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700">
             <div class="text-xs text-gray-500 mb-1">作业数量</div>
             <div class="text-xl font-semibold">{{ safeCourseAnalytics.totalAssignments }}</div>
           </div>
           <div class="p-4 rounded bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700">
             <div class="text-xs text-gray-500 mb-1">完成率</div>
             <div class="text-xl font-semibold">{{ (safeCourseAnalytics.completionRate || 0) }}%</div>
           </div>
         </div>
      </card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTeacherStore } from '@/stores/teacher'
import { useCourseStore } from '@/stores/course'
import { useAuthStore } from '@/stores/auth'
import { useUIStore } from '@/stores/ui'
import { teacherApi } from '@/api/teacher.api'
import type { CourseStudentPerformanceItem } from '@/types/teacher'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Progress from '@/components/ui/Progress.vue'
import * as echarts from 'echarts'
import {
  DocumentArrowDownIcon,
  ArrowTrendingUpIcon,
  ArrowTrendingDownIcon,
  ChevronRightIcon
} from '@heroicons/vue/24/outline'

// Stores
const teacherStore = useTeacherStore()
const courseStore = useCourseStore()
const authStore = useAuthStore()
const uiStore = useUIStore()

// 状态
const selectedCourseId = ref<string | null>(null)
const topStudents = ref<CourseStudentPerformanceItem[]>([])

// 图表引用
const learningTrendRef = ref<HTMLElement>()
const scoreDistributionRef = ref<HTMLElement>()
const coursePerformanceRef = ref<HTMLElement>()

let learningTrendChart: echarts.ECharts | null = null
let scoreDistributionChart: echarts.ECharts | null = null
let coursePerformanceChart: echarts.ECharts | null = null

// 计算属性：当前教师的课程列表
const teacherCourses = computed(() => {
  if (!authStore.user?.id) return []
  return courseStore.courses.filter(c => String(c.teacherId) === String(authStore.user?.id))
})

const route = useRoute()
const router = useRouter()

const currentCourseTitle = computed(() => {
  const cid = (route.params as any)?.id || null
  if (!cid) return ''
  const found = courseStore.courses.find(c => String(c.id) === String(cid))
  return found?.title || ''
})

const safeCourseAnalytics = computed(() => {
  const ca: any = teacherStore.courseAnalytics as any
  return {
    totalStudents: (ca.totalStudents ?? ca.enrollmentCount ?? 0) as number,
    averageScore: (ca.averageScore ?? 0) as number,
    completionRate: (ca.completionRate ?? ca.averageCompletionRate ?? 0) as number,
    totalAssignments: (ca.totalAssignments ?? ca.assignmentCount ?? 0) as number,
    timeSeriesData: ca.timeSeriesData ?? []
  }
})

const safeClassPerformance = computed(() => {
  const cp: any = teacherStore.classPerformance as any
  return {
    totalStudents: cp?.totalStudents ?? 0,
    gradeStats: cp?.gradeStats ?? null,
  }
})

// 方法
const initCharts = () => {
  initLearningTrendChart()
  initScoreDistributionChart()
  initCoursePerformanceChart()
}

function getOrCreateChart(el: HTMLElement | undefined, existing: echarts.ECharts | null): echarts.ECharts | null {
  if (!el) return null
  let inst = echarts.getInstanceByDom(el)
  if (!inst) {
    inst = echarts.init(el)
  } else {
    inst.clear()
  }
  return inst
}

const initLearningTrendChart = () => {
  if (!learningTrendRef.value) return

  learningTrendChart = getOrCreateChart(learningTrendRef.value, learningTrendChart)

  const seriesData = Array.isArray(safeCourseAnalytics.value.timeSeriesData)
    ? safeCourseAnalytics.value.timeSeriesData
    : []
  const months = seriesData.map((d: any) => d.date ?? d.month ?? '')
  const studentsSeries = seriesData.map((d: any) => (d.students ?? d.value ?? 0))

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
      data: months,
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
        name: '学生人数',
        type: 'line',
        smooth: true,
        data: studentsSeries,
        lineStyle: { color: '#3b82f6', width: 3 },
        itemStyle: { color: '#3b82f6' }
      }
    ]
  }

  learningTrendChart && learningTrendChart.setOption(option)
}

const initScoreDistributionChart = () => {
  if (!scoreDistributionRef.value) return

  scoreDistributionChart = getOrCreateChart(scoreDistributionRef.value, scoreDistributionChart)

  const dist: any[] = (teacherStore.classPerformance as any)?.gradeDistribution || []
  const pieData = Array.isArray(dist)
    ? dist.map(d => ({ name: d.gradeLevel ?? d.level ?? '未知', value: d.count ?? 0 }))
    : []

  const option = {
    title: {
      show: false
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      show: false
    },
    series: [
      {
        name: '成绩分布',
        type: 'pie',
        radius: ['50%', '70%'],
        center: ['60%', '50%'],
        data: pieData,
      }
    ]
  }

  scoreDistributionChart && scoreDistributionChart.setOption(option)
}

const initCoursePerformanceChart = () => {
  if (!coursePerformanceRef.value) return

  coursePerformanceChart = getOrCreateChart(coursePerformanceRef.value, coursePerformanceChart)

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
    legend: { data: ['学生数量'], bottom: 0 },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: teacherCourses.value.map(c => c.title),
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
        data: teacherCourses.value.map(() => safeCourseAnalytics.value.totalStudents),
        itemStyle: { color: '#3b82f6' }
      }
    ]
  }

  coursePerformanceChart && coursePerformanceChart.setOption(option)
}

const onCourseChange = () => {
  if (!selectedCourseId.value) return
  // 同步URL query，统一入口 /teacher/analytics?courseId=
  router.replace({ name: 'TeacherAnalytics', query: { courseId: selectedCourseId.value } })
  teacherStore.fetchCourseAnalytics(selectedCourseId.value)
  teacherStore.fetchClassPerformance(selectedCourseId.value)
  // 获取学生表现排行（前5名，按成绩）
  teacherApi.getCourseStudentPerformance(selectedCourseId.value, { page: 1, size: 5, sortBy: 'grade' })
    .then((resp: any) => {
      const data = (resp && (resp as any).items) ? (resp as any) : resp
      topStudents.value = (data?.items ?? []) as CourseStudentPerformanceItem[]
    })
    .catch(() => { topStudents.value = [] })
  nextTick(() => initCharts())
}

const exportReport = async () => {
  if (!selectedCourseId.value) {
    return uiStore.showNotification({ type: 'warning', title: '请选择课程', message: '请先选择要导出的课程' })
  }
  try {
    const res = await teacherApi.exportCourseStudents(selectedCourseId.value)
    const blob = new Blob([res as any], { type: 'text/csv;charset=utf-8;' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `course_${selectedCourseId.value}_students.csv`
    a.click()
    window.URL.revokeObjectURL(url)
  } catch (e: any) {
    uiStore.showNotification({ type: 'error', title: '导出失败', message: e?.message || '请稍后重试' })
  }
}

const resizeCharts = () => {
  learningTrendChart?.resize()
  scoreDistributionChart?.resize()
  coursePerformanceChart?.resize()
}

// 生命周期
onMounted(async () => {
  if (!authStore.user) {
    await authStore.fetchUser()
  }
  await courseStore.fetchCourses({ page: 1, size: 100 })
  const routeCourseId = (route.query as any)?.courseId || (route.params as any)?.id || null
  if (routeCourseId) {
    selectedCourseId.value = String(routeCourseId)
  } else if (teacherCourses.value.length) {
    selectedCourseId.value = String(teacherCourses.value[0].id)
  }
  onCourseChange()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  learningTrendChart?.dispose()
  scoreDistributionChart?.dispose()
  coursePerformanceChart?.dispose()
  window.removeEventListener('resize', resizeCharts)
})
</script> 