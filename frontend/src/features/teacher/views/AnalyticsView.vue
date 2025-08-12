<template>
  <div class="min-h-screen p-6">
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
          <div class="flex items-center gap-3">
            <button class="btn inline-flex items-center justify-center px-6 w-36 bg-indigo-600 hover:bg-indigo-700 text-white dark:text-white" :disabled="!selectedCourseId" @click="askAiForAnalytics">
              <sparkles-icon class="w-4 h-4 mr-2" />
              询问AI
            </button>
            <button class="btn btn-primary inline-flex items-center justify-center px-6 w-36" @click="exportReport">
              <document-arrow-down-icon class="w-4 h-4 mr-2" />
              导出报告
            </button>
          </div>
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
          {{ safeCourseAnalytics.totalAssignments }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">作业数量</p>
      </card>
    </div>

    <!-- 图表区域 -->
    <div class="grid grid-cols-1 xl:grid-cols-2 gap-8 mb-8">
      <!-- 成绩分布图 -->
      <card padding="lg">
        <template #header>
          <div class="flex items-center justify-between h-11">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">成绩分布</h3>
            <!-- 保持左右卡片header高度一致（右侧有按钮），这里用占位 -->
            <div class="invisible flex items-center gap-2">
              <button class="btn">1</button>
              <button class="btn">2</button>
            </div>
          </div>
        </template>
        <!-- 对齐工具栏占位，使左右两图表起始Y对齐 -->
        <div class="mb-3 min-h-[44px]"></div>
        <div class="w-full flex justify-center">
          <div ref="scoreDistributionRef" class="h-80 w-full max-w-[520px]"></div>
        </div>
      </card>

      <!-- 五维能力雷达图 -->
      <card padding="lg">
        <template #header>
          <div class="flex items-center justify-between h-11">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">五维能力雷达</h3>
            <div class="flex items-center gap-2">
              <button class="btn" @click="openWeights = true" :disabled="!selectedCourseId">设置权重</button>
              <button class="btn" title="刷新" @click="loadRadar" :disabled="!selectedCourseId">
                <arrow-path-icon class="w-4 h-4" />
              </button>
              <button class="btn" @click="exportRadar" :disabled="!selectedCourseId">导出CSV</button>
            </div>
          </div>
        </template>
        <div class="flex flex-nowrap items-center gap-2 mb-3 overflow-x-auto min-h-[44px]">
          <select v-model="selectedStudentId" class="input w-44" :disabled="!selectedCourseId">
            <option :value="null">请选择学生</option>
            <option v-for="s in topStudents" :key="s.studentId" :value="String(s.studentId)">{{ s.studentName }}</option>
          </select>
          <input type="date" v-model="startDate" class="input w-36" />
          <input type="date" v-model="endDate" class="input w-36" />
        </div>
        <div v-if="radarIndicators.length" class="w-full flex justify-center">
          <RadarChart :indicators="radarIndicators" :series="radarSeries" width="520px" height="320px" />
        </div>
        <div v-else class="text-sm text-gray-500 text-center">暂无雷达数据</div>
      </card>
    </div>

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

    
    </div>
    <!-- 权重设置弹窗（保持在同一 <template> 内） -->
    <AbilityWeightsDialog :open="openWeights" :weights="weights" @close="openWeights=false" @saved="onWeightsSaved" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
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
import RadarChart from '@/components/charts/RadarChart.vue'
import AbilityWeightsDialog from '@/features/teacher/components/AbilityWeightsDialog.vue'
import { DocumentArrowDownIcon, SparklesIcon, ArrowPathIcon } from '@heroicons/vue/24/outline'

// Stores
const teacherStore = useTeacherStore()
const courseStore = useCourseStore()
const authStore = useAuthStore()
const uiStore = useUIStore()

// 状态
const selectedCourseId = ref<string | null>(null)
const topStudents = ref<CourseStudentPerformanceItem[]>([])
const studentTotal = ref<number>(0)
const selectedStudentId = ref<string | null>(null)
const startDate = ref<string>('2024-11-01')
const endDate = ref<string>('2024-11-30')
const openWeights = ref(false)
const weights = ref<Record<string, number> | null>(null)
const radarIndicators = ref<{ name: string; max: number }[]>([])
const radarSeries = ref<{ name: string; values: number[] }[]>([])

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
  const cp: any = teacherStore.classPerformance as any
  return {
    // 如果后端未在 courseAnalytics 提供 totalStudents，则回退到 classPerformance.totalStudents
    totalStudents: (ca.totalStudents ?? ca.enrollmentCount ?? cp?.totalStudents ?? studentTotal.value ?? 0) as number,
    averageScore: Number(ca.averageScore ?? 0) as number,
    completionRate: Number(ca.completionRate ?? ca.averageCompletionRate ?? 0) as number,
    totalAssignments: (ca.totalAssignments ?? ca.assignmentCount ?? 0) as number,
    timeSeriesData: Array.isArray(ca.timeSeriesData) ? ca.timeSeriesData : []
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
  // 仅保留成绩分布图
  initScoreDistributionChart()
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

// 删除学习趋势图，保留接口位置以便后续启用

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
        center: ['50%', '48%'],
        data: pieData,
      }
    ]
  }

  scoreDistributionChart && scoreDistributionChart.setOption(option)
}

// 删除课程表现图，保留接口位置以便后续启用

const onCourseChange = () => {
  if (!selectedCourseId.value) return
  // 切换课程时重置本地总学生兜底值，避免显示上一个课程数据
  studentTotal.value = 0
  // 同步URL query，统一入口 /teacher/analytics?courseId=
  router.replace({ name: 'TeacherAnalytics', query: { courseId: selectedCourseId.value } })
  teacherStore.fetchCourseAnalytics(selectedCourseId.value)
  teacherStore.fetchClassPerformance(selectedCourseId.value)
  // 初始化权重
  teacherApi.getAbilityWeights(selectedCourseId.value).then((r: any) => { weights.value = r?.weights || r?.data?.weights || null }).catch(() => {})
  // 获取学生表现排行（前5名，按成绩）
  teacherApi.getCourseStudentPerformance(selectedCourseId.value, { page: 1, size: 5, sortBy: 'grade' })
    .then((data: any) => {
      topStudents.value = (data?.items ?? []) as CourseStudentPerformanceItem[]
      if (typeof data?.total === 'number') studentTotal.value = data.total
    })
    .catch(() => { topStudents.value = [] })
  nextTick(() => { 
    initCharts()
    loadRadar()
  })
}

// 当路由中的 courseId 改变（例如从其它页面跳转）时，自动同步当前课程并刷新数据
watch(() => route.query.courseId, (cid) => {
  if (cid) {
    selectedCourseId.value = String(cid)
    onCourseChange()
  }
})

// 监听班级表现数据变化以刷新成绩分布图
watch(() => teacherStore.classPerformance, () => {
  nextTick(() => initScoreDistributionChart())
}, { deep: true })

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
  scoreDistributionChart?.resize()
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
  scoreDistributionChart?.dispose()
  window.removeEventListener('resize', resizeCharts)
})

const askAiForAnalytics = () => {
  if (!selectedCourseId.value) return
  const ca = safeCourseAnalytics.value
  const dist: any[] = (teacherStore.classPerformance as any)?.gradeDistribution || []
  const top = (topStudents.value || []).map(s => ({
    studentId: s.studentId,
    studentName: s.studentName,
    averageGrade: s.averageGrade,
    progress: s.progress,
    lastActiveAt: s.lastActiveAt,
  }))
  const payload = {
    courseId: Number(selectedCourseId.value),
    metrics: {
      totalStudents: ca.totalStudents,
      averageScore: ca.averageScore,
      completionRate: ca.completionRate,
      totalAssignments: ca.totalAssignments,
    },
    gradeDistribution: dist,
    topStudents: top,
  }
  const q = `请根据以下课程数据做教学洞察与建议，并指出高风险点与可执行改进项（用中文回答）。\n` +
           `数据(JSON)：\n` + JSON.stringify(payload, null, 2)
  router.push({ path: '/teacher/ai', query: { q } })
}

const loadRadar = async () => {
  try {
    if (!selectedCourseId.value || !startDate.value || !endDate.value) return
    if (new Date(startDate.value) > new Date(endDate.value)) {
      return uiStore.showNotification({ type: 'warning', title: '时间范围错误', message: '开始日期不能晚于结束日期' })
    }
    const params: any = { courseId: selectedCourseId.value, startDate: startDate.value, endDate: endDate.value }
    if (selectedStudentId.value) params.studentId = selectedStudentId.value
    const r: any = await teacherApi.getAbilityRadar(params)
    const data: any = r?.data?.data ?? r?.data ?? r
    const dims: string[] = data?.dimensions || []
    radarIndicators.value = dims.map(n => ({ name: n, max: 100 }))
    const student = (data?.studentScores || []) as number[]
    const clazz = (data?.classAvgScores || []) as number[]
    radarSeries.value = [
      { name: '学生', values: student },
      { name: '班级', values: clazz },
    ]
  } catch (e: any) {
    uiStore.showNotification({ type: 'error', title: '刷新失败', message: e?.message || '请稍后再试' })
  }
}

const exportRadar = async () => {
  if (!selectedCourseId.value || !startDate.value || !endDate.value) return
  const params: any = { courseId: selectedCourseId.value, startDate: startDate.value, endDate: endDate.value }
  if (selectedStudentId.value) params.studentId = selectedStudentId.value
  const res = await teacherApi.exportAbilityRadarCsv(params)
  const blob = new Blob([res as any], { type: 'text/csv;charset=utf-8;' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `radar_${selectedCourseId.value}.csv`
  a.click()
  window.URL.revokeObjectURL(url)
}

const onWeightsSaved = async (w: Record<string, number>) => {
  if (!selectedCourseId.value) return
  weights.value = { ...w }
  await teacherApi.updateAbilityWeights({ courseId: selectedCourseId.value, weights: weights.value as any })
  await loadRadar()
}
</script> 
