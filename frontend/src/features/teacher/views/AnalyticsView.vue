<template>
  <div class="min-h-screen p-6">
    <PageHeader :title="t('teacher.analytics.title')" :subtitle="t('teacher.analytics.subtitle')">
      <template #actions>
        <div class="flex items-center space-x-4">
          <span class="text-sm font-medium text-gray-700 dark:text-gray-300 whitespace-nowrap">
            {{ t('teacher.analytics.courseSelector.label') || '选择课程' }}
          </span>
          <GlassPopoverSelect
            v-model="selectedCourseId"
            :options="courseSelectOptions"
            :placeholder="t('teacher.analytics.selectCourse') as string"
            size="md"
            width="260px"
            :fullWidth="false"
            @change="onCourseChange"
          />
          <div class="flex items-center gap-3">
            <Button variant="primary" class="whitespace-nowrap" :disabled="!selectedCourseId" @click="askAiForAnalytics">
              <SparklesIcon class="w-4 h-4 mr-2" />
              {{ t('teacher.analytics.askAi') }}
            </Button>
            <Button variant="secondary" class="whitespace-nowrap" @click="exportReport">
              <DocumentArrowDownIcon class="w-4 h-4 mr-2 flex-shrink-0" />
              {{ t('teacher.analytics.exportReport') }}
            </Button>
          </div>
        </div>
      </template>
    </PageHeader>

    <!-- 核心指标卡片（统一为工作台小卡片样式） -->
    <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6 mb-8">
      <StartCard :label="t('teacher.analytics.cards.totalStudents') as string" :value="n(safeCourseAnalytics.totalStudents, 'integer')" tone="blue" :icon="UserGroupIcon" />
      <StartCard :label="t('teacher.analytics.cards.completionRate') as string" :value="n((safeCourseAnalytics.completionRate || 0) / 100, 'percent')" tone="emerald" :icon="CheckCircleIcon" />
      <StartCard :label="t('teacher.analytics.cards.averageScore') as string" :value="(safeCourseAnalytics.averageScore || 0).toFixed(1)" tone="violet" :icon="StarIcon" />
      <StartCard :label="t('teacher.analytics.cards.totalAssignments') as string" :value="n(safeCourseAnalytics.totalAssignments, 'integer')" tone="amber" :icon="ClipboardDocumentListIcon" />
    </div>

    <!-- 图表区域 -->
    <div class="grid grid-cols-12 gap-6 mb-2 items-start w-full">
      <!-- 左列包裹：成绩分布 + 学生表现排行（同一列内堆叠，避免被右列拉高后产生空白） -->
      <div class="col-span-12 lg:col-span-6 min-w-0 flex flex-col gap-4 self-start w-full">
        <Card padding="lg" class="self-start w-full" tint="info">
        <template #header>
            <div class="flex items-center justify-between h-11 overflow-hidden">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.analytics.charts.scoreDistribution') }}</h3>
              <div class="invisible flex items-center gap-2"><Button size="sm" variant="ghost">1</Button><Button size="sm" variant="ghost">2</Button></div>
          </div>
        </template>
        <div class="mb-3 min-h-[44px]"></div>
          <div class="w-full relative z-0">
            <div ref="scoreDistributionRef" class="h-80 w-full"></div>
          </div>
      </Card>

        <Card padding="lg" class="self-start w-full" tint="accent">
        <template #header>
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.analytics.tables.studentRanking') }}</h3>
        </template>
          <div v-if="rankingLoading" class="py-6 text-center text-sm text-gray-500 dark:text-gray-400">
            {{ t('common.loading') }}
          </div>
          <div v-else-if="!topStudents.length" class="space-y-4 text-center text-gray-500 dark:text-gray-400">
            {{ t('teacher.analytics.tables.noRanking') }}
          </div>
          <div v-else class="flex flex-col">
            <ul class="divide-y divide-gray-200 dark:divide-gray-700">
              <li v-for="(s, idx) in topStudents" :key="s.studentId" class="py-3 flex flex-col gap-2">
                <div class="flex items-start justify-between gap-4">
                  <div class="flex items-start gap-3">
                    <span class="text-sm w-6 text-right font-semibold text-gray-600 dark:text-gray-300 pt-0.5">{{ (rankingPage - 1) * rankingPageSize + idx + 1 }}</span>
                    <div class="flex flex-col gap-1">
                      <div class="flex items-center gap-2">
                        <span class="text-sm font-semibold text-gray-900 dark:text-gray-100">{{ s.studentName }}</span>
                        <Badge :variant="getRadarBadgeVariant(s.radarClassification)" size="xs" class="px-2 text-xs font-semibold uppercase tracking-wide">
                          {{ formatRadarBadgeLabel(s.radarClassification) }}
                        </Badge>
                      </div>
                      <div v-if="s.dimensionScores && Object.keys(s.dimensionScores || {}).length" class="grid grid-cols-2 gap-x-3 gap-y-1 text-xs text-gray-600 dark:text-gray-400 mt-1">
                        <span
                          v-for="([code, value], didx) in Object.entries(s.dimensionScores || {})"
                          :key="`${s.studentId}-${code}-${didx}`"
                          class="inline-flex items-center gap-1 uppercase"
                        >
                          <span class="font-medium">{{ formatDimensionLabel(code) }}</span>
                          <span class="text-gray-400">·</span>
                          <span>{{ formatRadarValue(value) }}</span>
                        </span>
                      </div>
                      <div v-else class="text-xs text-gray-400 dark:text-gray-500">
                        {{ t('teacher.students.table.noRadarData') }}
                      </div>
                    </div>
                  </div>
                  <div class="flex flex-col items-end text-sm text-gray-700 dark:text-gray-200">
                    <span class="text-sm font-semibold whitespace-nowrap text-gray-900 dark:text-gray-100">{{ t('teacher.analytics.tables.radarAreaValue', { value: formatRadarArea(s.radarArea) }) }}</span>
                  </div>
                </div>
              </li>
            </ul>
            <PaginationBar
              :page="rankingPage"
              :page-size="rankingPageSize"
              :total-pages="rankingTotalPages"
              :page-size-options="rankingPageOptions.map((o:any)=>Number(o.value || o))"
              :show-total-pages="true"
              @update:page="(p:number)=> rankingPage = p"
              @update:pageSize="handleRankingPageSizeChange"
            />
          </div>
      </Card>
      </div>

      <!-- 五维能力雷达图 -->
      <div class="col-span-12 lg:col-span-6 min-w-0 flex flex-col gap-4 w-full">
        <!-- 控制面板 -->
        <Card padding="lg" class="w-full" tint="warning">
          <template #header>
            <div class="flex items-center justify-between h-11">
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.analytics.settings.title') }}</h3>
              <div class="flex items-center gap-3">
                <div class="flex items-center gap-2 mr-1">
                  <span class="text-sm text-gray-700 dark:text-gray-300">{{ t('teacher.analytics.charts.enableCompare') }}</span>
                  <GlassSwitch v-model="compareEnabled" size="sm" />
                </div>
                <Button size="sm" variant="info" :title="t('teacher.analytics.charts.refresh')" @click="onRefreshAnalytics" :disabled="!selectedCourseId" class="shadow-sm">
                  <ArrowPathIcon class="w-4 h-4 mr-1" />
                  {{ t('teacher.analytics.charts.refresh') }}
                </Button>
                <Button size="sm" variant="secondary" @click="exportAnalytics" :disabled="!selectedCourseId" class="shadow-sm">
                  <DocumentArrowDownIcon class="w-4 h-4 mr-1" />
                  {{ t('teacher.analytics.charts.exportCsv') }}
                </Button>
              </div>
            </div>
          </template>
          <div class="flex flex-col gap-3">
            <p class="text-xs text-gray-600 dark:text-gray-400 leading-relaxed">{{ t('teacher.analytics.settings.compareNote') }}</p>
            <div class="flex flex-wrap items-center gap-4 whitespace-nowrap">
              <div class="flex items-center gap-2">
                <span class="text-xs font-medium leading-tight text-gray-700 dark:text-gray-300">{{ t('teacher.analytics.settings.studentLabel') }}</span>
                  <GlassPopoverSelect
                    v-model="selectedStudentId"
                    :options="studentSelectOptions"
                    :placeholder="t('teacher.analytics.charts.selectStudent') as string"
                    size="sm"
                    width="160px"
                    tint="primary"
                    @change="loadRadar"
                  />
              </div>
              <div class="flex items-center gap-2">
                <span class="text-xs font-medium leading-tight text-gray-700 dark:text-gray-300">{{ t('teacher.analytics.settings.classAvgLabel') }}</span>
                  <GlassPopoverSelect
                    v-model="includeClassAvg"
                    :options="[
                      { label: t('teacher.analytics.charts.classAvgBoth') as string || '班级均值: A与B', value: 'both' },
                      { label: t('teacher.analytics.charts.classAvgA') as string || '班级均值: 仅A', value: 'A' },
                      { label: t('teacher.analytics.charts.classAvgB') as string || '班级均值: 仅B', value: 'B' },
                      { label: t('teacher.analytics.charts.classAvgNone') as string || '班级均值: 关闭', value: 'none' }
                    ]"
                    size="sm"
                    width="180px"
                    tint="secondary"
                    @change="loadRadar"
                    :disabled="!compareEnabled"
                  />
              </div>
              <!-- 已移除时间筛选器：后端支持全量聚合，无需日期过滤 -->
            </div>
              <div v-if="compareEnabled" class="flex flex-col gap-2">
              <div class="flex flex-wrap items-center gap-2">
                <span class="text-sm text-gray-700 dark:text-gray-300">{{ t('teacher.analytics.settings.setA') }}</span>
                <GlassMultiSelect v-model="assignmentIdsA" :options="assignmentSelectOptions" size="sm" tint="primary" />
              </div>
              <div class="flex flex-wrap items-center gap-2">
                <span class="text-sm text-gray-700 dark:text-gray-300">{{ t('teacher.analytics.settings.setB') }}</span>
                <GlassMultiSelect v-model="assignmentIdsB" :options="assignmentSelectOptions" size="sm" tint="secondary" />
              </div>
            </div>
          </div>
        </Card>

        <!-- 雷达图展示 -->
        <Card padding="lg" tint="secondary">
          <template #header>
            <div class="flex items-center justify-between h-11 relative z-10 overflow-hidden">
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.analytics.charts.radar') }}</h3>
              <div class="flex items-center gap-2"></div>
            </div>
          </template>
          <div v-if="radarIndicators.length" class="w-full">
            <radar-chart :indicators="radarIndicators" :series="radarSeries" height="320px" :appendTooltipToBody="true" :showLegend="true" />
          </div>
          <div v-else class="text-sm text-gray-500 text-center">{{ t('teacher.analytics.charts.noRadar') }}</div>
        </Card>

        <!-- 维度图例说明（中英） -->
        <card padding="lg" v-if="rawRadarDimensions.length">
          <AbilityRadarLegend :dimensions="rawRadarDimensions" />
        </card>

        <!-- 维度解析卡片（右列堆叠） -->
        <Card padding="lg" v-if="compareEnabled || (!!selectedStudentId)" class="self-start w-full" tint="primary">
          <template #header>
            <div class="flex items-center justify-between h-11">
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.analytics.charts.dimensionInsights') || '维度解析' }}</h3>
              <Button size="sm" variant="outline" @click="loadInsights" :disabled="!selectedCourseId">{{ t('teacher.analytics.charts.refresh') || '刷新' }}</Button>
            </div>
          </template>
          <div v-if="insightsItems.length === 0" class="text-sm text-gray-500">{{ t('teacher.analytics.charts.noInsights') || '暂无解析' }}</div>
          <div v-else class="space-y-2">
            <div
              v-for="(it, idx) in insightsItems"
              :key="idx"
              class="collapse collapse-plus glass-ultraThin border rounded-xl text-[var(--color-base-content)]"
            >
              <input type="radio" name="teacher-insights" :checked="idx === 0" />
              <div class="collapse-title font-semibold flex items-center justify-between">
                <span class="text-sm text-[var(--color-base-content)]">{{ localizeDimensionName(it.name) }}</span>
                <span class="text-xs text-[color-mix(in_oklab,var(--color-base-content)_70%,transparent)]">
                  A: {{ (it.scoreA ?? 0).toFixed(1) }}
                  <template v-if="compareEnabled">
                    <span class="ml-2">B: {{ (it.scoreB ?? 0).toFixed(1) }}</span>
                    <span v-if="it.delta != null" :class="(it.delta ?? 0) >= 0 ? 'text-[var(--color-success-content)]' : 'text-[var(--color-error-content)]'" class="ml-2">
                      Δ {{ (it.delta ?? 0).toFixed(1) }}
                    </span>
                  </template>
                </span>
              </div>
              <div class="collapse-content text-sm">
                <div class="text-[color-mix(in_oklab,var(--color-base-content)_82%,transparent)] whitespace-pre-wrap">{{ getInsightTexts(it).analysis || it.analysis }}</div>
                <div class="mt-1 text-[color-mix(in_oklab,var(--color-base-content)_82%,transparent)] whitespace-pre-wrap">{{ getInsightTexts(it).suggestion || it.suggestion }}</div>
              </div>
            </div>
          </div>
        </Card>
      </div>
    
    </div>
    <!-- 权重设置已废弃：移除入口与弹窗 -->
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
import StartCard from '@/components/ui/StartCard.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import type * as echarts from 'echarts'
import { resolveEChartsTheme, glassTooltipCss, resolveThemePalette } from '@/charts/echartsTheme'
import { getOrInitChart, bindHideTipOnGlobalOut, bindTooltipVisibility, bindThemeChangeEvents } from '@/charts/echartsHelpers'
import RadarChart from '@/components/charts/RadarChart.vue'
import AbilityRadarLegend from '@/shared/views/AbilityRadarLegend.vue'
import { DocumentArrowDownIcon, SparklesIcon, ArrowPathIcon, UserGroupIcon, CheckCircleIcon, StarIcon, ClipboardDocumentListIcon } from '@heroicons/vue/24/outline'
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import GlassMultiSelect from '@/components/ui/filters/GlassMultiSelect.vue'
import GlassSwitch from '@/components/ui/inputs/GlassSwitch.vue'
import { resolveUserDisplayName } from '@/shared/utils/user'
import { downloadCsv } from '@/utils/download'
import { useCourseAssignmentsOptions } from '@/features/teacher/composables/useCourseAssignmentsOptions'
import { debounce } from '@/shared/utils/debounce'

// Stores
const teacherStore = useTeacherStore()
const courseStore = useCourseStore()
const authStore = useAuthStore()
const uiStore = useUIStore()
const { t, n, locale } = useI18n()

// 状态
const selectedCourseId = ref<string | null>(null)
const topStudents = ref<CourseStudentPerformanceItem[]>([])
const radarStudentPool = ref<CourseStudentPerformanceItem[]>([])
const studentTotal = ref<number>(0)
const rankingPage = ref(1)
const rankingPageSize = ref(10)
const rankingTotal = ref(0)
const rankingLoading = ref(false)
const rankingPageOptions = [
  { label: '10', value: 10 },
  { label: '20', value: 20 },
  { label: '50', value: 50 }
]
const rankingTotalPages = computed(() => {
  const total = Math.max(0, Number(rankingTotal.value || 0))
  const size = Math.max(1, Number(rankingPageSize.value || 1))
  const pages = Math.ceil(total / size)
  return pages > 0 ? pages : 1
})
const selectedStudentId = ref<string | null>(null)
// Compare mode states
const compareEnabled = ref<boolean>(false)
const includeClassAvg = ref<'none'|'A'|'B'|'both'>('both')
const { assignmentOptions, loadAssignments } = useCourseAssignmentsOptions(selectedCourseId, compareEnabled)
const assignmentIdsA = ref<string[]>([])
const assignmentIdsB = ref<string[]>([])
const insightsItems = ref<any[]>([])
// 权重设置已废弃：保留后端接口但本页不再使用
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
  // 与学生端统一，改用共享维度标题
  return code ? (t(`shared.radarLegend.dimensions.${code}.title`) as any) : serverName
}

function normalizeScores(raw: any): number[] {
  if (!Array.isArray(raw)) return []
  return raw.map((v: any) => {
    const num = Number(v)
    if (!Number.isFinite(num)) return 0
    return Math.round(num * 10) / 10
  })
}

function normalizePerformanceItems(items: CourseStudentPerformanceItem[] | any[]): CourseStudentPerformanceItem[] {
  return (items || []).map((i: any) => ({
    ...i,
    studentName: resolveUserDisplayName(i) || i?.studentName || i?.nickname || i?.username || `#${i?.studentId}`,
    dimensionScores: i?.dimensionScores || {},
  }))
}

const handleRankingPageSizeChange = (value: any) => {
  const parsed = Number(value)
  const normalized = Number.isFinite(parsed) && parsed > 0 ? parsed : 10
  if (rankingPageSize.value === normalized) return
  rankingPageSize.value = normalized
  rankingPage.value = 1
}

const handleRankingPrev = () => {
  if (rankingPage.value <= 1) return
  rankingPage.value -= 1
}

const handleRankingNext = () => {
  if (rankingPage.value >= rankingTotalPages.value) return
  rankingPage.value += 1
}

const loadStudentRanking = async () => {
  if (!selectedCourseId.value) {
    topStudents.value = []
    rankingTotal.value = 0
    return
  }
  rankingLoading.value = true
  try {
    const requestPage = rankingPage.value
    const requestSize = rankingPageSize.value
    const payload: any = await teacherApi.getCourseStudentPerformance(selectedCourseId.value, {
      page: requestPage,
      size: requestSize,
      sortBy: 'radar'
    } as any)
    if (rankingPage.value !== requestPage || rankingPageSize.value !== requestSize) {
      return
    }
    const items = normalizePerformanceItems(payload?.items ?? [])
    let resolvedTotal = items.length ?? 0
    if (Number.isFinite(Number(payload?.total))) {
      resolvedTotal = Number(payload?.total)
    }
    rankingTotal.value = Math.max(0, resolvedTotal)
    studentTotal.value = rankingTotal.value
    const effectiveTotalPages = Math.max(1, Math.ceil((rankingTotal.value || 0) / Math.max(1, rankingPageSize.value || 1)))
    if (rankingPage.value > effectiveTotalPages) {
      rankingPage.value = effectiveTotalPages
      return
    }
    topStudents.value = items
  } catch (e: any) {
    topStudents.value = []
    rankingTotal.value = 0
    uiStore.showNotification({
      type: 'error',
      title: t('teacher.analytics.messages.refreshFailed'),
      message: e?.message || t('teacher.analytics.messages.refreshFailedMsg')
    })
  } finally {
    rankingLoading.value = false
  }
}

const loadRadarStudentPool = async () => {
  if (!selectedCourseId.value) {
    radarStudentPool.value = []
    return
  }
  try {
    const payload: any = await teacherApi.getCourseStudentPerformance(selectedCourseId.value, {
      page: 1,
      size: 1000,
      sortBy: 'radar'
    } as any)
    radarStudentPool.value = normalizePerformanceItems(payload?.items ?? [])
    if (!rankingTotal.value && Number.isFinite(Number(payload?.total))) {
      rankingTotal.value = Number(payload?.total)
      studentTotal.value = Number(payload?.total)
    }
  } catch {
    radarStudentPool.value = []
  }
}

// 图表引用（仅保留成绩分布图）
const scoreDistributionRef = ref<HTMLElement>()

let scoreDistributionChart: echarts.ECharts | null = null
let darkObserver: { disconnect: () => void } | null = null
let reRenderScheduled = false

// 计算属性：当前教师的课程列表
const teacherCourses = computed(() => {
  if (!authStore.user?.id) return []
  return courseStore.courses.filter(c => String(c.teacherId) === String(authStore.user?.id))
})

const courseSelectOptions = computed(() => teacherCourses.value.map((c: any) => ({ label: String(c.title || ''), value: String(c.id) })))
// 学生下拉选项：对 1000 条 map 做简单缓存，避免频繁重算造成卡顿
const studentOptionsCache = ref<{ key: string; options: Array<{ label: string; value: any }> }>({ key: '', options: [] })
function studentListKey(list: CourseStudentPerformanceItem[]): string {
  // 仅用 studentId 作为稳定 key，降低计算成本
  return (list || []).map(s => String(s?.studentId ?? '')).join('|')
}
const studentSelectOptions = computed(() => {
  const base = (radarStudentPool.value.length ? radarStudentPool.value : topStudents.value) as CourseStudentPerformanceItem[]
  const k = `${String(locale.value)}::${studentListKey(base)}`
  if (studentOptionsCache.value.key === k && studentOptionsCache.value.options.length) return studentOptionsCache.value.options
  const options = [{ label: t('teacher.analytics.charts.selectStudent') as string, value: null as any }, ...base.map((s: CourseStudentPerformanceItem) => ({ label: s.studentName, value: String(s.studentId) }))]
  studentOptionsCache.value = { key: k, options }
  return options
})
const assignmentSelectOptions = computed(() => assignmentOptions.value.map((a: { id: string; title: string }) => ({ label: a.title, value: String(a.id) })))

const route = useRoute()
const router = useRouter()

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

// 方法
const initCharts = () => {
  // 仅保留成绩分布图
  initScoreDistributionChart()
}

const initScoreDistributionChart = () => {
  if (!scoreDistributionRef.value) return

  scoreDistributionChart = getOrInitChart(scoreDistributionRef.value, resolveEChartsTheme())

  const dist: any[] = (teacherStore.classPerformance as any)?.gradeDistribution || []
  const palette = resolveThemePalette()
  const pieData = Array.isArray(dist)
    ? dist.map(d => ({ name: d.gradeLevel ?? d.level ?? t('teacher.analytics.charts.unknown'), value: d.count ?? 0 }))
    : []

  const option = {
    title: {
      show: false
    },
    hoverLayerThreshold: Infinity,
    tooltip: {
      trigger: 'item',
      triggerOn: 'mousemove',
      backgroundColor: 'transparent',
      borderColor: 'transparent',
      textStyle: { color: 'var(--color-base-content)' },
      extraCssText: glassTooltipCss(),
      className: 'echarts-glass-tooltip',
      renderMode: 'html',
      enterable: false,
      confine: true,
      alwaysShowContent: false,
      appendToBody: true,
      transitionDuration: 0,
      showDelay: 0,
      hideDelay: 40,
      position: (pos: any) => [pos[0] + 12, pos[1] + 12],
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      show: false
    },
    series: [
      {
        name: t('teacher.analytics.charts.series.distribution'),
        type: 'pie',
        // 扩大半径并居中，使其尽量填充卡片
        radius: ['40%', '78%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        selectedMode: false,
        hoverAnimation: false,
        data: pieData.map((item: any, idx: number) => ({
          ...item,
          itemStyle: { color: palette[idx % palette.length] },
          // 禁用 hover/emphasis 状态，避免任何“变暗/变色”行为
          emphasis: { disabled: true },
          blur: { disabled: true },
          select: { disabled: true }
        })),
        emphasis: { disabled: true, focus: 'none', scale: false },
      }
    ]
  }

  // 完整替换 option，避免主题切换后旧状态（emphasis/颜色）残留导致 hover 变白
  scoreDistributionChart && scoreDistributionChart.setOption(option, true)
  // 初始化后立即隐藏 tooltip，避免左上角残留小空白容器
  try { scoreDistributionChart?.dispatchAction({ type: 'hideTip' } as any) } catch {}
  bindTooltipVisibility(scoreDistributionChart)
  bindHideTipOnGlobalOut(scoreDistributionChart)
}

function scheduleReinitScoreDistribution() {
  if (reRenderScheduled) return
  reRenderScheduled = true
  requestAnimationFrame(() => {
    reRenderScheduled = false
    // 轻量刷新：不销毁实例，直接重建配置并 setOption
    initScoreDistributionChart()
  })
}

// 删除课程表现图，保留接口位置以便后续启用

const onCourseChange = async () => {
  if (!selectedCourseId.value) return
  // 切换课程时重置本地总学生兜底值，避免显示上一个课程数据
  studentTotal.value = 0
  rankingTotal.value = 0
  if (rankingPage.value !== 1) rankingPage.value = 1
  topStudents.value = []
  radarStudentPool.value = []
  // 同步URL query，统一入口 /teacher/analytics?courseId=
  router.replace({ name: 'TeacherAnalytics', query: { courseId: selectedCourseId.value, studentId: selectedStudentId.value || undefined } })
  teacherStore.fetchCourseAnalytics(selectedCourseId.value)
  teacherStore.fetchClassPerformance(selectedCourseId.value)
  // 权重设置已废弃：不再初始化权重
  await loadStudentRanking()
  await loadRadarStudentPool()
  const routeStudentId: any = (route.query as any)?.studentId || null
  if (routeStudentId) {
    selectedStudentId.value = String(routeStudentId)
  }
  nextTick(() => { 
    initCharts()
    loadRadar()
  })
  // 若开启对比，切课时同步加载作业列表，避免选项为空
  if (compareEnabled.value && selectedCourseId.value) {
    loadAssignments()
  }
}

watch([rankingPage, rankingPageSize], ([newPage, newSize], [oldPage, oldSize]) => {
  if (!selectedCourseId.value) return
  if (newPage === oldPage && newSize === oldSize) return
  loadStudentRanking()
})

// 当路由中的 courseId 改变（例如从其它页面跳转）时，自动同步当前课程并刷新数据
// 添加防重复触发：若 courseId 未实际改变则跳过
watch(() => route.query.courseId, (cid, oldCid) => {
  if (cid && String(cid) !== String(oldCid || '')) {
    selectedCourseId.value = String(cid)
    onCourseChange()
  }
})

// 当路由中的 studentId 改变时，自动选中该学生并刷新雷达图
watch(() => route.query.studentId, (sid) => {
  selectedStudentId.value = sid ? String(sid) : null
  if (selectedCourseId.value) {
    scheduleRadarAndInsights()
  }
})

// 监听班级表现数据变化以刷新成绩分布图
function gradeDistributionKey(dist: any[]): string {
  const arr = Array.isArray(dist) ? dist : []
  return arr.map(d => `${String(d?.gradeLevel ?? d?.level ?? '')}:${Number(d?.count ?? 0)}`).join('|')
}
watch(() => gradeDistributionKey(((teacherStore.classPerformance as any)?.gradeDistribution || []) as any[]), () => {
  nextTick(() => initScoreDistributionChart())
})

// 学生切换时自动刷新雷达图
watch(selectedStudentId, () => {
  scheduleRadarAndInsights()
})

watch(compareEnabled, () => {
  if (compareEnabled.value) {
    if (selectedCourseId.value) loadAssignments()
  }
  scheduleRadarAndInsights()
})

const scheduleRadarAndInsights = debounce(() => {
  // 合并多处 watch 触发，避免同一次交互下重复请求
  try { loadRadar() } catch {}
  try { loadInsights() } catch {}
}, 120)

const exportReport = async () => {
  if (!selectedCourseId.value) {
    return uiStore.showNotification({ type: 'warning', title: t('teacher.analytics.messages.chooseCourse'), message: t('teacher.analytics.messages.chooseCourseMsg') })
  }
  try {
    const res = await teacherApi.exportCourseStudents(selectedCourseId.value)
    downloadCsv(res as any, `course_${selectedCourseId.value}_students.csv`)
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
  const routeStudentId = (route.query as any)?.studentId || null
  if (routeCourseId) {
    selectedCourseId.value = String(routeCourseId)
  } else if (teacherCourses.value.length) {
    selectedCourseId.value = String(teacherCourses.value[0].id)
  }
  if (routeStudentId) {
    selectedStudentId.value = String(routeStudentId)
  }
  onCourseChange()
  window.addEventListener('resize', resizeCharts)
  // 监听浅/深色切换，自动重建饼图以应用主题色
  // 使用全局主题事件，避免每个页面/组件都创建 MutationObserver 造成抖动
  if (!darkObserver) {
    darkObserver = bindThemeChangeEvents({
      onChanging: () => { try { scoreDistributionChart?.dispatchAction({ type: 'hideTip' } as any) } catch {} },
      onChanged: () => scheduleReinitScoreDistribution()
    })
  }
})

onUnmounted(() => {
  scoreDistributionChart?.dispose()
  window.removeEventListener('resize', resizeCharts)
  if (darkObserver) { try { darkObserver.disconnect() } catch {}; darkObserver = null }
  scheduleRadarAndInsights.cancel()
})

const askAiForAnalytics = () => {
  if (!selectedCourseId.value) return
  const ca = safeCourseAnalytics.value
  const dist: any[] = (teacherStore.classPerformance as any)?.gradeDistribution || []
  const rankingSource = radarStudentPool.value.length ? radarStudentPool.value : topStudents.value
  const top = (rankingSource || []).slice(0, 10).map(s => ({
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
  router.push({ path: '/teacher/assistant', query: { q } })
}

const formatRadarArea = (area?: number) => {
  if (!Number.isFinite(Number(area))) return '--'
  return `${Number(area).toFixed(1)}%`
}

const getRadarBadgeVariant = (classification?: string) => {
  switch ((classification || '').toUpperCase()) {
    case 'A': return 'success'
    case 'B': return 'warning'
    case 'C': return 'info'
    default: return 'danger'
  }
}

const formatRadarBadgeLabel = (classification?: string) => {
  const key = (classification || 'D').toUpperCase() as 'A' | 'B' | 'C' | 'D'
  const desc = t(`teacher.students.radar.classification.${key}`)
  return `${key} · ${desc}`
}

const formatDimensionLabel = (code: string) => {
  const label = t(`shared.radarLegend.dimensions.${code}.title`) as any
  return label || code
}

const formatRadarValue = (value?: number) => {
  if (!Number.isFinite(Number(value))) return '--'
  return Number(value).toFixed(1)
}

function setRadarDimensions(dims: string[]) {
  rawRadarDimensions.value = Array.isArray(dims) ? [...dims] : []
  radarIndicators.value = rawRadarDimensions.value.map(n => ({ name: localizeDimensionName(n), max: 100 }))
}

function buildRadarSingleParams(): any {
  const params: any = { courseId: selectedCourseId.value }
  if (selectedStudentId.value != null && selectedStudentId.value !== '') {
    const sid = Number(selectedStudentId.value)
    if (Number.isFinite(sid) && sid > 0) params.studentId = sid
  }
  return params
}

async function loadRadarSingle() {
  const r: any = await teacherApi.getAbilityRadar(buildRadarSingleParams())
  const data: any = r?.data?.data ?? r?.data ?? r
  setRadarDimensions(data?.dimensions || [])
  const student = normalizeScores(data?.studentScores)
  const clazz = normalizeScores(data?.classAvgScores)
  radarSeries.value = [
    { name: t('teacher.analytics.charts.series.student'), values: student },
    { name: t('teacher.analytics.charts.series.class'), values: clazz },
  ]
}

function buildRadarCompareBody(): any {
  return {
    courseId: selectedCourseId.value,
    studentId: selectedStudentId.value,
    includeClassAvg: includeClassAvg.value,
    ...(assignmentIdsA.value?.length ? { assignmentIdsA: assignmentIdsA.value } : {}),
    ...(assignmentIdsB.value?.length ? { assignmentIdsB: assignmentIdsB.value } : {}),
  }
}

async function loadRadarCompare() {
  if (!selectedStudentId.value) {
    uiStore.showNotification({
      type: 'warning',
      title: t('teacher.analytics.messages.chooseStudent') || '请选择学生',
      message: t('teacher.analytics.messages.chooseStudentMsg') || '在对比模式下需先选择学生'
    })
    return
  }
  const r: any = await teacherApi.postAbilityRadarCompare(buildRadarCompareBody())
  const data: any = r?.data?.data ?? r?.data ?? r
  setRadarDimensions(data?.dimensions || [])

  const series: { name: string; values: number[] }[] = []
  const aStu = normalizeScores(data?.seriesA?.studentScores)
  const aCls = normalizeScores(data?.seriesA?.classAvgScores)
  const bStu = normalizeScores(data?.seriesB?.studentScores)
  const bCls = normalizeScores(data?.seriesB?.classAvgScores)
  series.push({ name: 'A - ' + (t('teacher.analytics.charts.series.student') || '学生'), values: aStu })
  if (Array.isArray(aCls) && aCls.length) series.push({ name: 'A - ' + (t('teacher.analytics.charts.series.class') || '班级'), values: aCls })
  series.push({ name: 'B - ' + (t('teacher.analytics.charts.series.student') || '学生'), values: bStu })
  if (Array.isArray(bCls) && bCls.length) series.push({ name: 'B - ' + (t('teacher.analytics.charts.series.class') || '班级'), values: bCls })
  radarSeries.value = series
}

const loadRadar = async () => {
  try {
    if (!selectedCourseId.value) return
    if (!compareEnabled.value) {
      await loadRadarSingle()
    } else {
      await loadRadarCompare()
    }
  } catch (e: any) {
    uiStore.showNotification({ type: 'error', title: t('teacher.analytics.messages.refreshFailed'), message: e?.message || t('teacher.analytics.messages.refreshFailedMsg') })
  }
}

const onRefreshAnalytics = async () => {
  await loadRadar()
  await loadInsights()
}

const exportAnalytics = async () => {
  if (!selectedCourseId.value) return
  if (compareEnabled.value) {
    if (!selectedStudentId.value) return
    const body: any = {
      courseId: selectedCourseId.value,
      studentId: selectedStudentId.value,
      includeClassAvg: includeClassAvg.value,
    }
    if (assignmentIdsA.value?.length) body.assignmentIdsA = assignmentIdsA.value
    if (assignmentIdsB.value?.length) body.assignmentIdsB = assignmentIdsB.value
    const res = await teacherApi.exportAbilityRadarCompareCsv(body)
    downloadCsv(res as any, `radar_compare_${selectedCourseId.value}_${selectedStudentId.value}.csv`)
  } else {
    const params: any = { courseId: selectedCourseId.value }
    if (selectedStudentId.value) params.studentId = selectedStudentId.value
    const res = await teacherApi.exportAbilityRadarCsv(params)
    downloadCsv(res as any, `radar_${selectedCourseId.value}.csv`)
  }
}

function buildInsightsBody(): any {
  const base: any = {
    courseId: selectedCourseId.value,
    studentId: selectedStudentId.value,
    includeClassAvg: includeClassAvg.value,
  }
  // compare 开启：带上A/B作业集；未开启：仅用A作业集或留空（后端将用同组对比）
  if (compareEnabled.value) {
    if (assignmentIdsA.value?.length) base.assignmentIdsA = assignmentIdsA.value
    if (assignmentIdsB.value?.length) base.assignmentIdsB = assignmentIdsB.value
  } else {
    if (assignmentIdsA.value?.length) base.assignmentIdsA = assignmentIdsA.value
    base.assignmentIdsB = assignmentIdsA.value && assignmentIdsA.value.length ? assignmentIdsA.value : undefined
  }
  return base
}

const loadInsights = async () => {
  try {
    if (!selectedCourseId.value || !selectedStudentId.value) return
    const r: any = await teacherApi.postAbilityDimensionInsights(buildInsightsBody())
    insightsItems.value = (r?.data?.data?.items ?? r?.data?.items ?? r?.items ?? []) as any[]
  } catch {
    insightsItems.value = []
  }
}

function getInsightTexts(it: any): { analysis: string; suggestion: string } {
  try {
    const serverName = String(it?.name || '')
    const code = NAME_ZH_TO_CODE[serverName]
    const scoreNum = Number(it?.scoreA ?? it?.score ?? 0)
    const deltaNum = (it?.delta != null) ? Number(it?.delta) : NaN
    let deltaText = ''
    if (!Number.isNaN(deltaNum)) {
      const abs = Math.abs(deltaNum).toFixed(1)
      if (deltaNum > 0) deltaText = (t('shared.radarLegend.insights.delta.up', { value: abs }) as any)
      else if (deltaNum < 0) deltaText = (t('shared.radarLegend.insights.delta.down', { value: abs }) as any)
      else deltaText = (t('shared.radarLegend.insights.delta.equal') as any)
    }
    if (code) {
      const base = `shared.radarLegend.insights.${code}`
    const baseline = compareEnabled.value ? (t('shared.radarLegend.insights.baseline.setB') as any) : (t('shared.radarLegend.insights.baseline.classAvg') as any)
    const analysis = t(`${base}.analysis`, { scoreA: scoreNum.toFixed(1), scoreB: (Number.isFinite(Number(it?.scoreB)) ? Number(it?.scoreB).toFixed(1) : (t('shared.radarLegend.insights.na') as any)), delta: deltaText, baseline }) as any
    const suggestion = t(`${base}.suggestion`, { scoreA: scoreNum.toFixed(1), scoreB: (Number.isFinite(Number(it?.scoreB)) ? Number(it?.scoreB).toFixed(1) : (t('shared.radarLegend.insights.na') as any)), baseline }) as any
      if (analysis || suggestion) return { analysis, suggestion }
    }
    return { analysis: String(it?.analysis || ''), suggestion: String(it?.suggestion || '') }
  } catch {
    return { analysis: String(it?.analysis || ''), suggestion: String(it?.suggestion || '') }
  }
}

// 权重设置已废弃：移除保存逻辑

// 语言切换时，仅根据缓存的原始维度与当前 series 数值重新本地化显示文本
function refreshRadarLocalization() {
  if (rawRadarDimensions.value.length) {
    radarIndicators.value = rawRadarDimensions.value.map(n => ({ name: localizeDimensionName(n), max: 100 }))
  }
  const studentLabel = t('teacher.analytics.charts.series.student')
  const classLabel = t('teacher.analytics.charts.series.class')
  radarSeries.value = radarSeries.value.map(s => {
    let newName = s.name
    // 替换常见标签为当前语言
    newName = newName.replace(/学生|Student/gi, studentLabel)
    newName = newName.replace(/班级|Class/gi, classLabel)
    return { name: newName, values: s.values }
  })
}

watch(locale, () => {
  nextTick(() => refreshRadarLocalization())
})
</script> 

<style scoped>
.no-scrollbar::-webkit-scrollbar { display: none; }
.no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
</style>
