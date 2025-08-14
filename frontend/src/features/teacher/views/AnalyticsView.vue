<template>
  <div class="min-h-screen p-6">
    <!-- 页面标题 -->
    <div class="mb-8">
      <div class="flex items-center justify-between">
        <div>
          
          <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">{{ t('teacher.analytics.title') }}</h1>
          <p class="text-gray-600 dark:text-gray-400">{{ t('teacher.analytics.subtitle') }}</p>
        </div>
      <div class="flex items-center space-x-4">
        <select v-model="selectedCourseId" @change="onCourseChange" class="input">
            <option :value="null">{{ t('teacher.analytics.selectCourse') }}</option>
            <option v-for="course in teacherCourses" :key="course.id" :value="String(course.id)">
              {{ course.title }}
            </option>
          </select>
          <div class="flex items-center gap-3">
            <button class="btn inline-flex items-center justify-center px-6 w-36 bg-indigo-600 hover:bg-indigo-700 text-white dark:text-white whitespace-nowrap" :disabled="!selectedCourseId" @click="askAiForAnalytics">
              <SparklesIcon class="w-4 h-4 mr-2" />
              {{ t('teacher.analytics.askAi') }}
            </button>
            <button class="btn btn-primary inline-flex items-center justify-center px-6 w-36 whitespace-nowrap" @click="exportReport">
              <DocumentArrowDownIcon class="w-4 h-4 mr-2 flex-shrink-0" />
              {{ t('teacher.analytics.exportReport') }}
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
        <p class="text-sm text-gray-600 dark:text-gray-400">{{ t('teacher.analytics.cards.totalStudents') }}</p>
      </card>

      <card padding="lg" class="text-center">
        <div class="text-3xl font-bold text-green-600 dark:text-green-400 mb-2">
          {{ (safeCourseAnalytics.averageScore || 0).toFixed(1) }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">{{ t('teacher.analytics.cards.averageScore') }}</p>
      </card>

      <card padding="lg" class="text-center">
        <div class="text-3xl font-bold text-yellow-600 dark:text-yellow-400 mb-2">
          {{ (safeCourseAnalytics.completionRate || 0) }}%
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">{{ t('teacher.analytics.cards.completionRate') }}</p>
      </card>

      <card padding="lg" class="text-center">
        <div class="text-3xl font-bold text-purple-600 dark:text-purple-400 mb-2">
          {{ safeCourseAnalytics.totalAssignments }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">{{ t('teacher.analytics.cards.totalAssignments') }}</p>
      </card>
    </div>

    <!-- 图表区域 -->
    <div class="grid grid-cols-1 xl:grid-cols-2 gap-8 mb-8">
      <!-- 成绩分布图 -->
      <card padding="lg">
        <template #header>
          <div class="flex items-center justify-between h-11">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.analytics.charts.scoreDistribution') }}</h3>
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
          <div class="flex items-center justify-between h-11 relative z-10">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.analytics.charts.radar') }}</h3>
            <div class="flex items-center gap-2">
              <Button size="sm" variant="indigo" @click="openWeights = true" :disabled="!selectedCourseId">
                {{ t('teacher.analytics.charts.setWeights') }}
              </Button>
              <Button size="sm" variant="outline" :title="t('teacher.analytics.charts.refresh')" @click="loadRadar" :disabled="!selectedCourseId">
                <ArrowPathIcon class="w-4 h-4" />
              </Button>
              <Button size="sm" variant="outline" @click="exportRadar" :disabled="!selectedCourseId">
                <DocumentArrowDownIcon class="w-4 h-4 mr-1" />
                {{ t('teacher.analytics.charts.exportCsv') }}
              </Button>
            </div>
          </div>
        </template>
        <div class="flex flex-nowrap items-center gap-2 mb-3 overflow-x-auto min-h-[44px]">
          <select v-model="selectedStudentId" class="input w-44" :disabled="!selectedCourseId">
            <option :value="null">{{ t('teacher.analytics.charts.selectStudent') }}</option>
            <option v-for="s in topStudents" :key="s.studentId" :value="String(s.studentId)">{{ s.studentName }}</option>
          </select>
          <input type="date" v-model="startDate" class="input w-36" />
          <input type="date" v-model="endDate" class="input w-36" />
        </div>
        <div v-if="radarIndicators.length" class="w-full flex justify-center">
          <radar-chart :indicators="radarIndicators" :series="radarSeries" width="520px" height="320px" />
        </div>
        <div v-else class="text-sm text-gray-500 text-center">{{ t('teacher.analytics.charts.noRadar') }}</div>
      </card>
    </div>

    <!-- 详细数据表格 -->
    <div class="grid grid-cols-1 xl:grid-cols-2 gap-8">
      <!-- 学生表现排行 -->
      <card padding="lg">
        <template #header>
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.analytics.tables.studentRanking') }}</h3>
        </template>
          <div v-if="!topStudents.length" class="space-y-4 text-center text-gray-500 dark:text-gray-400">
            {{ t('teacher.analytics.tables.noRanking') }}
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
    <ability-weights-dialog :open="openWeights" :weights="weights" @close="openWeights=false" @saved="onWeightsSaved" />
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
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'

// Stores
const teacherStore = useTeacherStore()
const courseStore = useCourseStore()
const authStore = useAuthStore()
const uiStore = useUIStore()
const { t, locale } = useI18n()

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
// 保留后端原始维度名称以便在语言切换时重新本地化
const rawRadarDimensions = ref<string[]>([])

// 后端固定返回中文维度名称，这里在前端做代码映射并按当前语言本地化
const NAME_ZH_TO_CODE: Record<string, string> = {
  '道德认知': 'MORAL_COGNITION',
  '学习态度': 'LEARNING_ATTITUDE',
  '学习能力': 'LEARNING_ABILITY',
  '学习方法': 'LEARNING_METHOD',
  '学习成绩': 'ACADEMIC_GRADE'
}

function localizeDimensionName(serverName: string): string {
  const code = NAME_ZH_TO_CODE[serverName]
  return code ? t(`teacher.analytics.weights.dimensions.${code}`) : serverName
}

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
    ? dist.map(d => ({ name: d.gradeLevel ?? d.level ?? t('teacher.analytics.charts.unknown'), value: d.count ?? 0 }))
    : []

  const option = {
    title: {
      show: false
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      show: false
    },
    series: [
      {
        name: t('teacher.analytics.charts.series.distribution'),
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
    return uiStore.showNotification({ type: 'warning', title: t('teacher.analytics.messages.chooseCourse'), message: t('teacher.analytics.messages.chooseCourseMsg') })
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
    uiStore.showNotification({ type: 'error', title: t('teacher.analytics.messages.exportFailed'), message: e?.message || t('teacher.analytics.messages.exportFailedMsg') })
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
  const q = t('teacher.analytics.messages.aiPromptHeader') + "\n" + JSON.stringify(payload, null, 2)
  router.push({ path: '/teacher/ai', query: { q } })
}

const loadRadar = async () => {
  try {
    if (!selectedCourseId.value || !startDate.value || !endDate.value) return
    if (new Date(startDate.value) > new Date(endDate.value)) {
      return uiStore.showNotification({ type: 'warning', title: t('teacher.analytics.messages.timeRangeError'), message: t('teacher.analytics.messages.timeRangeMsg') })
    }
    const params: any = { courseId: selectedCourseId.value, startDate: startDate.value, endDate: endDate.value }
    if (selectedStudentId.value) params.studentId = selectedStudentId.value
    const r: any = await teacherApi.getAbilityRadar(params)
    const data: any = r?.data?.data ?? r?.data ?? r
    const dims: string[] = data?.dimensions || []
    rawRadarDimensions.value = [...dims]
    radarIndicators.value = dims.map(n => ({ name: localizeDimensionName(n), max: 100 }))
    const student = (data?.studentScores || []) as number[]
    const clazz = (data?.classAvgScores || []) as number[]
    radarSeries.value = [
      { name: t('teacher.analytics.charts.series.student'), values: student },
      { name: t('teacher.analytics.charts.series.class'), values: clazz },
    ]
  } catch (e: any) {
    uiStore.showNotification({ type: 'error', title: t('teacher.analytics.messages.refreshFailed'), message: e?.message || t('teacher.analytics.messages.refreshFailedMsg') })
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

// 语言切换时，仅根据缓存的原始维度与当前 series 数值重新本地化显示文本
function refreshRadarLocalization() {
  if (rawRadarDimensions.value.length) {
    radarIndicators.value = rawRadarDimensions.value.map(n => ({ name: localizeDimensionName(n), max: 100 }))
  }
  const studentValues = radarSeries.value[0]?.values || []
  const classValues = radarSeries.value[1]?.values || []
  radarSeries.value = [
    { name: t('teacher.analytics.charts.series.student'), values: studentValues },
    { name: t('teacher.analytics.charts.series.class'), values: classValues },
  ]
}

watch(locale, () => {
  nextTick(() => refreshRadarLocalization())
})
</script> 
