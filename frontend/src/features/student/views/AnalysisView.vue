<template>
  <div class="p-6">
    <PageHeader :title="t('student.analysis.title')" :subtitle="t('student.analysis.subtitle')" />

    <div v-if="loading" class="text-center py-12">{{ t('student.analysis.loading') }}</div>

    <div v-else class="space-y-8">
      <!-- KPIs -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <StartCard :label="t('student.analysis.kpiAvgScore')" :value="formatScore(kpi.avgScore)" tone="blue" :icon="StarIcon" />
        <StartCard :label="t('student.analysis.kpiCompletion')" :value="formatPercent(kpi.completionRate)" tone="emerald" :icon="CheckCircleIcon" />
        <StartCard :label="t('student.analysis.kpiHours')" :value="String(kpi.studyHours)" tone="violet" :icon="ClockIcon" />
        <StartCard :label="t('student.analysis.kpiActiveDays')" :value="String(kpi.activeDays)" tone="amber" :icon="BoltIcon" />
      </div>

      <!-- Controls: 课程选择（仅查看本人参与课程）+ 对比开关 同行显示 -->
      <Card padding="md" tint="info" class="mt-2">
        <div class="flex items-center flex-wrap gap-3">
          <div class="flex items-center gap-2">
            <span class="whitespace-nowrap text-sm text-gray-700 dark:text-gray-300">{{ t('student.ability.course') || t('teacher.analytics.selectCourse') || '请选择课程' }}</span>
            <GlassPopoverSelect
              v-model="selectedCourseId"
              :options="courseSelectOptions"
              :placeholder="(t('student.ability.selectCourse') as string) || (t('teacher.analytics.selectCourse') as string)"
              size="sm"
              width="280px"
              tint="primary"
              @change="onCourseChange"
            />
          </div>
          <div class="flex items-center gap-2">
            <span class="text-sm text-gray-700 dark:text-gray-300">{{ t('student.ability.compare') || '对比模式' }}</span>
            <GlassSwitch v-model="compareEnabled" size="sm" />
          </div>
          <div class="flex items-center gap-2" v-if="compareEnabled">
            <span class="whitespace-nowrap text-xs font-medium leading-tight text-gray-700 dark:text-gray-300">{{ t('teacher.analytics.settings.classAvgLabel') || '班级均值' }}</span>
            <GlassPopoverSelect
              v-model="includeClassAvg"
              :options="[
                { label: (t('teacher.analytics.charts.classAvgBoth') as string) || '班级均值: A与B', value: 'both' },
                { label: (t('teacher.analytics.charts.classAvgA') as string) || '班级均值: 仅A', value: 'A' },
                { label: (t('teacher.analytics.charts.classAvgB') as string) || '班级均值: 仅B', value: 'B' },
                { label: (t('teacher.analytics.charts.classAvgNone') as string) || '班级均值: 关闭', value: 'none' }
              ]"
              size="sm"
              width="160px"
              tint="secondary"
              @change="onCompareParamsChange"
            />
          </div>
          <div class="flex items-center gap-2" v-if="compareEnabled">
            <span class="text-xs font-medium leading-tight text-gray-700 dark:text-gray-300">{{ t('student.ability.compareSetA') || '集合A' }}</span>
            <div class="ms-compact" style="width: 160px">
              <GlassMultiSelect v-model="assignmentIdsA" :options="assignmentSelectOptions" :placeholder="t('common.pleaseSelect') as string" tint="accent" />
            </div>
          </div>
          <div class="flex items-center gap-2" v-if="compareEnabled">
            <span class="text-xs font-medium leading-tight text-gray-700 dark:text-gray-300">{{ t('student.ability.compareSetB') || '集合B' }}</span>
            <div class="ms-compact" style="width: 160px">
              <GlassMultiSelect v-model="assignmentIdsB" :options="assignmentSelectOptions" :placeholder="t('common.pleaseSelect') as string" tint="accent" />
            </div>
          </div>
        </div>
      </Card>

      <!-- 能力雷达 + 维度评语（学生本人，左右各占一半） -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card padding="md" tint="primary">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold">{{ t('teacher.analytics.charts.radar') }}</h3>
            <div class="flex items-center gap-2">
              <Button size="sm" variant="secondary" class="whitespace-nowrap" @click="exportRadarCsv">
                <DocumentArrowDownIcon class="w-4 h-4 mr-1" />
                {{ t('common.exportCsv') || '导出CSV' }}
              </Button>
              <Button size="sm" variant="secondary" class="whitespace-nowrap" @click="exportRadarPng">
                <ArrowDownTrayIcon class="w-4 h-4 mr-1" />
                {{ t('common.exportPng') || '导出PNG' }}
              </Button>
              <Button size="sm" variant="primary" class="whitespace-nowrap" @click="askAiOnRadar">
                <SparklesIcon class="w-4 h-4 mr-1" />
                {{ t('common.askAi') || '询问AI' }}
              </Button>
            </div>
          </div>
          <div v-if="radarIndicators.length" class="w-full">
            <div class="max-w-[1040px] mx-auto mt-10 md:mt-12 lg:mt-14 pb-2">
              <RadarChart ref="radarRef" :indicators="radarIndicators" :series="radarSeries" height="460px" />
            </div>
          </div>
          <div v-else class="text-sm text-gray-500 text-center">{{ t('teacher.analytics.charts.noRadar') }}</div>
        </Card>
        <Card padding="md" tint="secondary">
          <h3 class="text-lg font-semibold mb-4">{{ t('student.ability.insights.title') || '维度评语' }}</h3>
          <div v-if="insightsItems.length === 0" class="text-sm text-gray-500">{{ t('student.ability.insights.empty') || '暂无评语' }}</div>
          <div v-else class="space-y-2">
            <div
              v-for="(it, idx) in insightsItems"
              :key="idx"
              class="collapse collapse-plus glass-ultraThin border rounded-xl text-[var(--color-base-content)]"
            >
              <input type="radio" name="student-insights" :checked="idx === 0" />
              <div class="collapse-title font-semibold flex items-center justify-between">
                <span class="text-sm text-[var(--color-base-content)]">{{ localizeDimensionName(it.name) }}</span>
                <span class="text-xs text-[color-mix(in_oklab,var(--color-base-content)_70%,transparent)]">
                  {{ t('teacher.analytics.charts.series.student') }}: {{ (it.scoreA ?? it.score ?? 0).toFixed(1) }}
                  <template v-if="it.delta != null">
                    <span :class="(it.delta ?? 0) >= 0 ? 'text-[var(--color-success-content)]' : 'text-[var(--color-error-content)]'" class="ml-2">
                      Δ {{ (it.delta ?? 0).toFixed(1) }}
                    </span>
                  </template>
                </span>
              </div>
              <div class="collapse-content text-sm">
                <div class="text-[color-mix(in_oklab,var(--color-base-content)_82%,transparent)] whitespace-pre-wrap">{{ it.analysisText || it.analysis }}</div>
                <div class="mt-1 text-[color-mix(in_oklab,var(--color-base-content)_82%,transparent)] whitespace-pre-wrap">{{ it.suggestionText || it.suggestion }}</div>
              </div>
            </div>
          </div>
        </Card>
      </div>

      <!-- 维度说明（玻璃样式，与雷达同页显示） -->
      <Card v-if="rawRadarDimensions.length" padding="md" tint="info">
        <AbilityRadarLegend :dimensions="rawRadarDimensions" />
      </Card>

      <!-- 行为洞察（阶段二：AI解释与建议，不算分；学生7天仅一次） -->
      <BehaviorInsightSection
        :student-id="String(auth?.user?.id || '')"
        :course-id="selectedCourseId || undefined"
        range="7d"
        :allow-student-generate="true"
      />

      <!-- 行为证据（阶段一：纯代码聚合，不调用AI，不算分） -->
      <BehaviorEvidenceSection :course-id="selectedCourseId || undefined" range="7d" />

      <!-- Trends 区域：左侧仪表盘，右侧两张趋势图（去掉完成率） -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 左：课程平均分仪表盘（动画） + 作业列表 -->
        <Card padding="md" tint="accent">
          <div class="w-full max-w-[420px] mx-auto">
            <div class="text-sm font-semibold text-gray-700 dark:text-gray-200 mb-2 text-center">{{ t('student.analysis.kpiAvgScore') }}</div>
            <GaugeChart :value="Number(courseAvgScore || 0)" :title="(t('student.analysis.kpiAvgScore') as any)" height="260px" />
          </div>
          <div class="mt-4">
            <div class="flex items-center justify-between mb-2">
              <div class="text-sm font-semibold text-gray-700 dark:text-gray-200">{{ t('student.grades.title') || '我的成绩' }}</div>
              <div class="text-xs text-gray-500 dark:text-gray-400 mr-2">{{ t('student.grades.total') }} ({{ courseAssignments.length }})</div>
            </div>
            <div class="divide-y divide-gray-200 dark:divide-gray-700 rounded-lg border border-gray-100 dark:border-gray-700 overflow-hidden max-h-[260px] overflow-y-auto">
              <Button
                v-for="a in courseAssignments"
                :key="String(a.id)"
                variant="menu"
                class="w-full text-left py-2"
                @click="goAssignment(a)"
              >
                <div class="flex items-center gap-3 w-full pl-4 pr-3">
                  <div class="min-w-0">
                    <div class="truncate text-sm text-gray-900 dark:text-gray-100">{{ a.title || ('#' + a.id) }}</div>
                    <div class="text-xs text-gray-500 truncate" v-if="a.courseTitle">{{ a.courseTitle }}</div>
                  </div>
                  <div class="ml-auto shrink-0 flex items-center gap-2">
                    <div class="h-2 w-24 bg-gray-200 dark:bg-gray-700 rounded overflow-hidden">
                      <div class="h-2" :style="{ width: ((typeof a.score === 'number' ? Number(a.score) : 0)) + '%', backgroundColor: getThemeCoreColors().primary }"></div>
                    </div>
                    <span class="text-xs font-medium text-gray-700 dark:text-gray-200">
                      {{ typeof a.score === 'number' ? Math.round(Number(a.score)) : (t('student.grades.ungraded') as string) }}
                    </span>
                  </div>
                </div>
              </Button>
              <div v-if="courseAssignments.length === 0" class="px-3 py-6 text-center text-sm text-gray-500">{{ t('student.grades.emptyTitle') || '暂无已评分的作业' }}</div>
            </div>
          </div>
        </Card>
        <!-- 右：成绩趋势图 + 最近一次作业教师评价（上下堆叠） -->
        <div class="lg:col-span-2 grid grid-cols-1 gap-6">
          <Card padding="md" tint="success">
            <div class="text-sm font-semibold text-gray-700 dark:text-gray-200 mb-2">{{ t('student.analysis.trendScore') }}</div>
            <TrendAreaChart
              :series="scoreSeriesPoints"
              :xAxisData="scoreXAxis"
              height="420px"
              :grid="{ top: '10%', left: '6%', right: '6%', bottom: '16%' }"
              :legend="{ bottom: 0 }"
              :tooltip="{ confine: true }"
            />
          </Card>
          <Card padding="md" tint="warning">
            <div class="flex items-center justify-between mb-2">
              <div class="text-sm font-semibold text-gray-700 dark:text-gray-200">{{ t('student.analysis.latestFeedback') || '最近作业评价' }}</div>
              <div v-if="latestFeedback?.gradedAt" class="text-xs text-gray-500">{{ String(latestFeedback?.gradedAt).toString().substring(0,10) }}</div>
            </div>
            <div v-if="!latestFeedback">
              <div class="text-sm text-gray-500">{{ t('common.empty') || '暂无评价' }}</div>
            </div>
            <div v-else>
              <div class="text-sm text-gray-900 dark:text-gray-100 mb-1">{{ latestFeedback.title }}</div>
              <p class="text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap">{{ latestFeedback.feedback }}</p>
            </div>
          </Card>
        </div>
      </div>
      
      <div v-if="empty" class="text-center text-gray-500">{{ t('student.analysis.empty') }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { resolveEChartsTheme } from '@/charts/echartsTheme'
import { studentApi } from '@/api/student.api'
import { useI18n } from 'vue-i18n'
import { i18n, loadLocaleMessages } from '@/i18n'
import StartCard from '@/components/ui/StartCard.vue'
import TrendAreaChart from '@/components/charts/TrendAreaChart.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import Card from '@/components/ui/Card.vue'
import RadarChart from '@/components/charts/RadarChart.vue'
import GaugeChart from '@/components/charts/GaugeChart.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import GlassMultiSelect from '@/components/ui/filters/GlassMultiSelect.vue'
import { useCourseStore } from '@/stores/course'
import { abilityApi } from '@/api/ability.api'
import BehaviorEvidenceSection from '@/features/shared/views/BehaviorEvidenceSection.vue'
import BehaviorInsightSection from '@/features/shared/views/BehaviorInsightSection.vue'
import { useRouter } from 'vue-router'
import { useAIStore } from '@/stores/ai'
import Button from '@/components/ui/Button.vue'
import { DocumentArrowDownIcon, ArrowDownTrayIcon, SparklesIcon } from '@heroicons/vue/24/outline'
import { StarIcon, CheckCircleIcon, ClockIcon, BoltIcon } from '@heroicons/vue/24/outline'
import { gradeApi } from '@/api/grade.api'
import { useAuthStore } from '@/stores/auth'
import { api as http } from '@/api/config'
import GlassSwitch from '@/components/ui/inputs/GlassSwitch.vue'
import AbilityRadarLegend from '@/shared/views/AbilityRadarLegend.vue'
import { getThemeCoreColors, rgba } from '@/utils/theme'

const { t } = useI18n()
const router = useRouter()
const ai = useAIStore()
const auth = useAuthStore()

type Point = { x: string; y: number }

type EnrolledCourseItem = { id: string; title: string }

const loading = ref(false)
const kpi = ref<{ avgScore: number; completionRate: number; studyHours: number; activeDays: number }>({ avgScore: 0, completionRate: 0, studyHours: 0, activeDays: 0 })
const trends = ref<{ score: Point[]; completion: Point[]; hours: Point[] }>({ score: [], completion: [], hours: [] })

// 课程选择（仅本人已报名） + 雷达与维度评语
const courseStore = useCourseStore()
const enrolledCourses = ref<EnrolledCourseItem[]>([])
const selectedCourseId = ref<string | null>(null)
const radarIndicators = ref<{ name: string; max: number }[]>([])
const radarSeries = ref<{ name: string; values: number[] }[]>([])
// 保留后端原始维度名称以便在语言切换时重新本地化
const rawRadarDimensions = ref<string[]>([])
const insightsItems = ref<any[]>([])
let radarRefreshScheduled = false
const radarRef = ref<any>(null)
const latestFeedback = ref<{ title: string; feedback: string; gradedAt?: string } | null>(null)
const themeVersion = ref(0)
let themeListenerBound = false
let themeChangedHandler: (() => void) | null = null

// 对比模式
const compareEnabled = ref<boolean>(false)
const includeClassAvg = ref<'none'|'A'|'B'|'both'>('both')
const assignmentOptions = ref<{ id: string; title: string }[]>([])
const assignmentIdsA = ref<string[]>([])
const assignmentIdsB = ref<string[]>([])

// 课程作业及课程平均分
const courseAssignments = ref<Array<{ id: string|number; title: string; score?: number; courseTitle?: string }>>([])
const courseAvgScore = computed(() => {
  const list = courseAssignments.value
  const nums = list
    .map(a => a.score)
    .filter((v): v is number => typeof v === 'number' && !isNaN(v))
  if (!nums.length) return 0
  const sum = nums.reduce((a,b)=>a+b,0)
  return Math.max(0, Math.min(100, sum / nums.length))
})

async function fetchCourseAssignments() {
  courseAssignments.value = []
  if (!selectedCourseId.value) return
  try {
    const courseIdStr = String(selectedCourseId.value)
    // 并行：课程作业 + 该课程下我的成绩
    const [{ assignmentApi }, gradesResp] = await Promise.all([
      import('@/api/assignment.api'),
      auth.user?.id ? gradeApi.getGradesByStudent(String(auth.user.id), { page: 1, size: 500, courseId: selectedCourseId.value as any }) : Promise.resolve(null as any)
    ])

    const [assignRes, gradeRes] = await Promise.all([
      assignmentApi.getAssignmentsByCourse(courseIdStr, { page: 1, size: 1000 } as any),
      Promise.resolve(gradesResp)
    ])

    const unwrapItems = (res: any) => res?.data?.items?.items ?? res?.data?.items ?? res?.items ?? (Array.isArray(res) ? res : [])

    const assignments = unwrapItems(assignRes) as any[]
    const gradeList = unwrapItems(gradeRes) as any[]

    const gradeMap = new Map<string, number>()
    // 同时提取最近一次带教师评语的作业
    let latest: { title: string; feedback: string; gradedAt?: string } | null = null
    for (const g of gradeList || []) {
      const aid = String(g.assignmentId || g.assignment?.id || g.submissionId || '')
      const raw = g.score ?? g.percentage
      const fb = (g as any)?.feedback || (g as any)?.teacherFeedback || ''
      const gradedAt = String((g as any)?.gradedAt || (g as any)?.updatedAt || (g as any)?.updated_at || '')
      if (aid && raw !== null && raw !== undefined) {
        const sc = Number(raw)
        if (!isNaN(sc)) gradeMap.set(aid, sc)
      }
      if (typeof fb === 'string' && fb.trim() !== '') {
        if (!latest || new Date(gradedAt).getTime() > new Date(latest.gradedAt || 0).getTime()) {
          latest = { title: String(g.assignment?.title || `#${aid}`), feedback: String(fb), gradedAt }
        }
      }
    }

    courseAssignments.value = (assignments || []).map((it: any) => {
      const idStr = String(it.id)
      const has = gradeMap.has(idStr)
      return {
        id: idStr,
        title: it.title || ('#' + it.id),
        score: has ? (gradeMap.get(idStr) as number) : undefined,
        courseTitle: ''
      }
    })
    latestFeedback.value = latest
  } catch {
    courseAssignments.value = []
    latestFeedback.value = null
  }
}

function goAssignment(a: { id: string|number }) {
  router.push({ name: 'StudentAssignmentSubmit', params: { id: String(a.id) } })
}

// 后端固定中文维度名 → 映射代码并本地化显示
const NAME_ZH_TO_CODE: Record<string, string> = {
  '道德认知': 'MORAL_COGNITION',
  '学习态度': 'LEARNING_ATTITUDE',
  '学习能力': 'LEARNING_ABILITY',
  '学习方法': 'LEARNING_METHOD',
  '学习成绩': 'ACADEMIC_GRADE'
}
function localizeDimensionName(serverName: string): string {
  const code = NAME_ZH_TO_CODE[serverName]
  // 使用共享维度说明的标题键，支持中英文
  // @ts-ignore
  return code ? ((t(`shared.radarLegend.dimensions.${code}.title`) as any) || serverName) : serverName
}

function normalizeScores(raw: any): number[] {
  if (!Array.isArray(raw)) return []
  return raw.map((v: any) => {
    const num = Number(v)
    if (!Number.isFinite(num)) return 0
    return Math.round(num * 10) / 10
  })
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

function recomputeInsightTexts() {
  try {
    insightsItems.value = (insightsItems.value || []).map((it: any) => {
      const texts = getInsightTexts(it)
      return { ...it, analysisText: texts.analysis || it.analysis, suggestionText: texts.suggestion || it.suggestion }
    })
  } catch { /* ignore */ }
}

const empty = computed(() =>
  (trends.value.score.length + trends.value.completion.length + trends.value.hours.length) === 0
)

// 为保证与主题色一致，这里为成绩趋势指定主题主色
// 主题刷新由布局层统一处理
const toSeriesPoints = (name: string, arr: Point[]) => {
  const primary = getThemeCoreColors().primary
  return [{ name, data: Array.isArray(arr) ? arr.map(p => ({ x: String(p?.x ?? ''), y: Number(p?.y) || 0 })) : [], color: primary }]
}
const toXAxis = (arr: Point[]) => (Array.isArray(arr) ? arr.map(p => String(p?.x ?? '')) : [])

const scoreSeriesPoints = computed(() => {
  // 依赖 themeVersion 以在主题切换时重算颜色
  return toSeriesPoints(t('student.analysis.series.score') as string, trends.value.score)
})
const scoreXAxis = computed(() => toXAxis(trends.value.score))
const completionSeriesPoints = computed(() => toSeriesPoints(t('student.analysis.series.completion') as string, trends.value.completion))
const completionXAxis = computed(() => toXAxis(trends.value.completion))
const hoursSeriesPoints = computed(() => toSeriesPoints(t('student.analysis.series.hours') as string, trends.value.hours))
const hoursXAxis = computed(() => toXAxis(trends.value.hours))

const load = async () => {
  loading.value = true
  const res: any = await studentApi.getAnalysis()
  loading.value = false
  if (!res) return
  const data = res
  kpi.value = data.kpi || kpi.value
  // 不再用聚合接口覆盖趋势，趋势改为严格按课程在 loadScoreTrend/loadHoursTrend 中加载
}

const formatScore = (v: number) => `${Math.round(Number(v || 0))}`
const formatPercent = (v: number) => `${Math.round(Number(v || 0))}%`

// 仅展示已报名课程
const courseSelectOptions = computed(() => enrolledCourses.value.map(c => ({ label: c.title, value: c.id })))

async function fetchEnrolledCourses() {
  try {
    const resp: any = await studentApi.getMyCourses({ page: 1, size: 1000 })
    const items = resp?.data?.items?.items || resp?.data?.items || resp?.items || []
    enrolledCourses.value = (items || []).map((it: any) => ({ id: String(it.id), title: it.title || `#${it.id}` }))
  } catch {
    enrolledCourses.value = []
  }
}

function onCourseChange() {
  // 清理对比参数与结果，避免切换课程造成权限错误
  assignmentIdsA.value = []
  assignmentIdsB.value = []
  insightsItems.value = []
  // 若当前处于对比模式，刷新可选作业集合
  if (compareEnabled.value) fetchAssignmentsForCourse()
  // 合并雷达与评语刷新，避免重复触发
  scheduleRadarRefresh()
}

function scheduleRadarRefresh() {
  if (radarRefreshScheduled) return
  radarRefreshScheduled = true
  requestAnimationFrame(async () => {
    radarRefreshScheduled = false
    await Promise.allSettled([loadRadar(), loadInsights()])
  })
}

function toggleCompare() {
  compareEnabled.value = !compareEnabled.value
  if (compareEnabled.value) {
    fetchAssignmentsForCourse()
  }
  loadRadar()
  loadInsights()
}

// 解析松散 JSON（复用学生提交页的宽容解析）
function safeJsonParseLoose(raw: any): any {
  try {
    if (!raw) return null
    if (typeof raw === 'object') return raw
    let s = String(raw || '')
    s = s.replace(/^\uFEFF/, '').trim()
    const fence = /```(?:json)?\s*([\s\S]*?)```/i
    const m = s.match(fence)
    if (m && m[1]) s = m[1].trim()
    const first = s.indexOf('{')
    const last = s.lastIndexOf('}')
    if (first >= 0 && last > first) s = s.substring(first, last + 1)
    s = s.replace(/,\s*}/g, '}').replace(/,\s*]/g, ']')
    return JSON.parse(s)
  } catch { return null }
}

function getOverall(obj: any): any {
  if (!obj || typeof obj !== 'object') return null
  if ((obj as any).overall) return (obj as any).overall
  if ((obj as any).final_score || (obj as any).holistic_feedback || (obj as any).dimension_averages) return obj
  return null
}

async function fetchLatestAiFeedback() {
  try {
    if (!selectedCourseId.value) { return }
    const selectedId = String(selectedCourseId.value)
    const ctx: any = { courseId: selectedId }
    const rep: any = await abilityApi.getStudentLatestReportByContext(ctx)
    const data: any = (rep && rep.data !== undefined) ? rep.data : rep
    if (!data) { return }
    // 若后端返回了 courseId 且与当前选择不一致，则不展示
    const dataCourseId = String(
      (data as any)?.courseId || (data as any)?.course_id || (data as any)?.context?.courseId || (data as any)?.course?.id || ''
    )
    if (dataCourseId && dataCourseId !== selectedId) { return }
    const raw = data.trendsAnalysis || data.trends_analysis || data.normalizedJson || data.rawJson || data.raw_json || null
    const obj = safeJsonParseLoose(raw)
    let feedback = ''
    if (obj) {
      const ov = getOverall(obj)
      feedback = String(ov?.holistic_feedback || '')
    }
    if (!feedback) feedback = String(data.recommendations || '')
    if (feedback) {
      latestFeedback.value = { title: String(data.title || 'AI 能力报告'), feedback, gradedAt: String(data.createdAt || data.updatedAt || '') }
    }
  } catch { /* ignore */ }
}

function onCompareParamsChange() { scheduleRadarRefresh() }

async function fetchAssignmentsForCourse() {
  if (!selectedCourseId.value) { assignmentOptions.value = []; return }
  try {
    // 学生端应使用本人可见的作业列表或“我的提交”，避免权限导致取不到
    const res: any = await studentApi.getAssignments({ courseId: String(selectedCourseId.value), page: 1, size: 1000 } as any)
    const unwrap = (r: any) => r?.data?.items?.items ?? r?.data?.items ?? r?.items ?? (Array.isArray(r) ? r : [])
    let items: any[] = unwrap(res)
    // 兜底：若接口未返回或权限受限，回退到“我的提交”列表提取 assignmentId
    if (!items || items.length === 0) {
      try {
        const ms: any = await studentApi.getMySubmissions({ courseId: String(selectedCourseId.value) })
        const subs = unwrap(ms)
        const map = new Map<string, { id: string; title: string }>()
        for (const s of subs || []) {
          const aid = String(s.assignmentId || s.assignment?.id || '')
          if (!aid) continue
          const title = String(s.assignment?.title || `#${aid}`)
          if (!map.has(aid)) map.set(aid, { id: aid, title })
        }
        items = Array.from(map.values())
      } catch { /* ignore */ }
    }
    assignmentOptions.value = (items || []).map((it: any) => ({ id: String(it.id), title: it.title || (`#${it.id}`) }))
  } catch {
    assignmentOptions.value = []
  }
}

const assignmentSelectOptions = computed(() => assignmentOptions.value.map(a => ({ label: a.title, value: a.id })))

async function loadRadar() {
  if (!selectedCourseId.value) { radarIndicators.value = []; radarSeries.value = []; return }
  try {
    if (!compareEnabled.value) {
      // 默认最近30天
      const end = new Date()
      const start = new Date(end)
      start.setDate(end.getDate() - 29)
      const fmt = (d: Date) => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
      const r: any = await abilityApi.getStudentRadar({ courseId: selectedCourseId.value, startDate: fmt(start), endDate: fmt(end) })
      const data: any = r?.data ?? r
      const dims: string[] = data?.dimensions || []
      const nextDims = [...dims]
      const nextIndicators = nextDims.map(n => ({ name: localizeDimensionName(n), max: 100 }))
      const student = normalizeScores(data?.studentScores)
      const clazz = normalizeScores(data?.classAvgScores)
      const nextSeries = [
        { name: t('teacher.analytics.charts.series.student') as string, values: student },
        { name: t('teacher.analytics.charts.series.class') as string, values: clazz },
      ]
      const sameDims = JSON.stringify(rawRadarDimensions.value) === JSON.stringify(nextDims)
      const sameInd = JSON.stringify(radarIndicators.value) === JSON.stringify(nextIndicators)
      const sameSeries = JSON.stringify(radarSeries.value) === JSON.stringify(nextSeries)
      if (!sameDims) rawRadarDimensions.value = nextDims
      if (!sameInd) radarIndicators.value = nextIndicators
      if (!sameSeries) radarSeries.value = nextSeries
    } else {
      const body: any = { courseId: String(selectedCourseId.value), includeClassAvg: includeClassAvg.value }
      if (assignmentIdsA.value?.length) body.assignmentIdsA = assignmentIdsA.value.map(id => Number(id))
      if (assignmentIdsB.value?.length) body.assignmentIdsB = assignmentIdsB.value.map(id => Number(id))
      const rCmp: any = await abilityApi.postStudentRadarCompare(body)
      const dCmp = rCmp?.data ?? rCmp
      const dims: string[] = dCmp?.dimensions || []
      const nextDims = [...dims]
      const nextIndicators = nextDims.map(n => ({ name: localizeDimensionName(n), max: 100 }))
      const series: { name: string; values: number[] }[] = []
      const aStu = normalizeScores(dCmp?.seriesA?.studentScores)
      const aCls = normalizeScores(dCmp?.seriesA?.classAvgScores)
      const bStu = normalizeScores(dCmp?.seriesB?.studentScores)
      const bCls = normalizeScores(dCmp?.seriesB?.classAvgScores)
      series.push({ name: 'A - ' + (t('teacher.analytics.charts.series.student') || '学生'), values: aStu })
      if (Array.isArray(aCls) && aCls.length) series.push({ name: 'A - ' + (t('teacher.analytics.charts.series.class') || '班级'), values: aCls })
      series.push({ name: 'B - ' + (t('teacher.analytics.charts.series.student') || '学生'), values: bStu })
      if (Array.isArray(bCls) && bCls.length) series.push({ name: 'B - ' + (t('teacher.analytics.charts.series.class') || '班级'), values: bCls })
      const sameDims = JSON.stringify(rawRadarDimensions.value) === JSON.stringify(nextDims)
      const sameInd = JSON.stringify(radarIndicators.value) === JSON.stringify(nextIndicators)
      const sameSeries = JSON.stringify(radarSeries.value) === JSON.stringify(series)
      if (!sameDims) rawRadarDimensions.value = nextDims
      if (!sameInd) radarIndicators.value = nextIndicators
      if (!sameSeries) radarSeries.value = series
    }
  } catch {
    radarIndicators.value = []
    radarSeries.value = []
  }
}

async function loadInsights() {
  if (!selectedCourseId.value) { insightsItems.value = []; return }
  try {
    const body: any = { courseId: selectedCourseId.value, includeClassAvg: includeClassAvg.value }
    if (compareEnabled.value) {
      if (assignmentIdsA.value?.length) body.assignmentIdsA = assignmentIdsA.value
      if (assignmentIdsB.value?.length) body.assignmentIdsB = assignmentIdsB.value
    } else {
      if (assignmentIdsA.value?.length) body.assignmentIdsA = assignmentIdsA.value
      body.assignmentIdsB = assignmentIdsA.value && assignmentIdsA.value.length ? assignmentIdsA.value : undefined
    }
    const r: any = await abilityApi.postStudentDimensionInsights(body)
    const list = (r?.data?.data?.items ?? r?.data?.items ?? r?.items ?? []) as any[]
    insightsItems.value = (list || []).map((it: any) => {
      const texts = getInsightTexts(it)
      return { ...it, analysisText: texts.analysis || it.analysis, suggestionText: texts.suggestion || it.suggestion }
    })
  } catch (e: any) {
    // 对 400 权限错误静默处理（仅本人课程）
    insightsItems.value = []
  }
}

async function exportRadarPng() {
  try {
    const dataUrl = radarRef.value?.getDataURL('png')
    if (!dataUrl) return
    const a = document.createElement('a')
    a.href = dataUrl
    a.download = `student_radar_${selectedCourseId.value || 'all'}.png`
    a.click()
  } catch {}
}

function exportRadarCsv() {
  if (!radarIndicators.value.length || !radarSeries.value.length) return
  const studentSeries = radarSeries.value.find(s => /学生|Student/i.test(s.name)) || radarSeries.value[0]
  const classSeries = radarSeries.value.find(s => /班级|Class/i.test(s.name))
  const header = ['dimension','student','class']
  const lines: string[] = []
  lines.push(header.join(','))
  for (let i = 0; i < radarIndicators.value.length; i++) {
    const dim = `"${radarIndicators.value[i].name.replace(/"/g,'""')}"`
    const s = studentSeries?.values?.[i] ?? ''
    const c = classSeries?.values?.[i] ?? ''
    lines.push([dim, s, c].join(','))
  }
  const csv = lines.join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `student_radar_${selectedCourseId.value || 'all'}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

async function askAiOnRadar() {
  if (!radarIndicators.value.length) return
  const payload = {
    courseId: selectedCourseId.value ? Number(selectedCourseId.value) : undefined,
    radar: {
      indicators: radarIndicators.value.map(i => i.name),
      series: radarSeries.value
    },
    insights: insightsItems.value,
    compare: compareEnabled.value ? { includeClassAvg: includeClassAvg.value, assignmentIdsA: assignmentIdsA.value, assignmentIdsB: assignmentIdsB.value } : undefined
  }
  const header = (t('teacher.analytics.messages.aiPromptHeader') as any) || '请基于以下我的个人能力雷达与维度解析，给出学习建议：'
  const prompt = header + "\n" + JSON.stringify(payload, null, 2)
  try {
    const conv = await ai.ensureDraftConversation('Radar analysis')
    if (conv?.id) {
      ai.activeConversationId = conv.id
      ai.saveDraft(conv.id, prompt)
    }
  } catch {}
  router.push({ path: '/student/assistant' })
}

// 使用封装的 GaugeChart 组件，不再手动管理实例

function runIdle(task: () => void) {
  try {
    const ric: any = (window as any).requestIdleCallback
    if (typeof ric === 'function') ric(() => task(), { timeout: 800 })
    else setTimeout(task, 50)
  } catch { setTimeout(task, 50) }
}

// 课程切换：刷新作业与趋势（仪表盘交由 GaugeChart 自身监听大小/可见性）
watch(() => selectedCourseId.value, async () => {
  await fetchCourseAssignments()
  loadScoreTrend()
  runIdle(() => { loadHoursTrend() })
  runIdle(() => { fetchLatestAiFeedback() })
})

async function loadScoreTrend() {
  try {
    const studentId = auth.user?.id
    if (!studentId || !selectedCourseId.value) { trends.value.score = []; return }
    const resp: any = await gradeApi.getGradesByStudent(String(studentId), { page: 1, size: 1000, courseId: selectedCourseId.value as any })
    const unwrap = (r: any) => r?.data?.items?.items ?? r?.data?.items ?? r?.items ?? (Array.isArray(r) ? r : [])
    const list = unwrap(resp) as any[]

    // 仅取已发布或有评分时间的记录
    const filtered = (list || []).filter(g => g?.isPublished === true || g?.status === 'published' || !!g?.gradedAt)

    // 每个作业仅保留最新一条成绩
    const latestByAssignment = new Map<string, any>()
    for (const g of filtered) {
      const aid = String(g.assignmentId || g.assignment?.id || g.submissionId || '')
      if (!aid) continue
      const cur = latestByAssignment.get(aid)
      const timeOf = (x: any) => new Date(x?.gradedAt || x?.updatedAt || x?.updated_at || x?.createdAt || 0).getTime()
      if (!cur || timeOf(g) >= timeOf(cur)) latestByAssignment.set(aid, g)
    }

    const finals = Array.from(latestByAssignment.values())
      .sort((a, b) => new Date(a?.gradedAt || a?.updatedAt || a?.updated_at || 0).getTime() - new Date(b?.gradedAt || b?.updatedAt || b?.updated_at || 0).getTime())

    trends.value.score = finals.map(g => ({
      x: String((g?.gradedAt || g?.updatedAt || g?.updated_at || '').toString().substring(0, 10)),
      y: Number(g?.score ?? g?.percentage ?? 0) || 0
    }))
  } catch {
    trends.value.score = []
  }
}

async function loadHoursTrend() {
  try {
    // 目前后端未实现基于课程的学习时长趋势，这里先复用 students/analysis 返回的 hours 或置空
    // 若有 /students/analysis?courseId=... 可切换为按课程获取
    // 占位：清空后由 load() 聚合填充
    trends.value.hours = []
  } catch {
    trends.value.hours = []
  }
}

onMounted(async () => {
  // 防御：确保当前 locale 的 student 命名空间已加载
  const locRaw = String(i18n.global.locale.value)
  const loc = (locRaw === 'zh' ? 'zh-CN' : (locRaw === 'en' ? 'en-US' : (locRaw as 'zh-CN' | 'en-US')))
  await loadLocaleMessages(loc, ['student'])
  // 仅加载学生自己报名课程
  await fetchEnrolledCourses()
  if (!selectedCourseId.value) {
    selectedCourseId.value = enrolledCourses.value[0] ? String(enrolledCourses.value[0].id) : null
  }
  // 仍可加载课程 store 的趋势配套数据
  if (!courseStore.courses.length) {
    try { await courseStore.fetchCourses({ page: 1, size: 100 }) } catch {}
  }
  await fetchCourseAssignments()
  // 先不急于渲染，等待一次 tick 确保容器挂载
  await nextTick()
  await Promise.all([load(), loadRadar(), loadInsights()])
  // 数据入位后再次稳态渲染（避免首次 loading 切换造成实例被销毁）
  await nextTick()
  await loadScoreTrend()
  runIdle(() => { loadHoursTrend() })
  runIdle(() => { fetchLatestAiFeedback() })
  // 监听全局主题事件：切换中隐藏可能的 tooltip；切换完成后重绘仪表并重算颜色
  if (!themeListenerBound) {
    themeChangedHandler = () => { themeVersion.value++ }
    window.addEventListener('theme:changed', themeChangedHandler as any)
    themeListenerBound = true
  }
  // 兜底：延时再次渲染一次，规避异步布局导致的初始化失败
  // GaugeChart 内部已包含兜底刷新，无需这里重复
})

watch(assignmentIdsA, () => { if (compareEnabled.value) scheduleRadarRefresh() }, { deep: true })
watch(assignmentIdsB, () => { if (compareEnabled.value) scheduleRadarRefresh() }, { deep: true })

// 监听对比模式开关：开启时加载可选作业集合，并刷新雷达与评语
watch(() => compareEnabled.value, (v) => {
  if (v) fetchAssignmentsForCourse()
  scheduleRadarRefresh()
})

function refreshRadarLocalization() {
  if (rawRadarDimensions.value.length) {
    radarIndicators.value = rawRadarDimensions.value.map(n => ({ name: localizeDimensionName(n), max: 100 }))
  }
  const studentLabel = t('teacher.analytics.charts.series.student') as string
  const classLabel = t('teacher.analytics.charts.series.class') as string
  radarSeries.value = radarSeries.value.map(s => {
    let newName = s.name
    newName = newName.replace(/学生|Student/gi, studentLabel)
    newName = newName.replace(/班级|Class/gi, classLabel)
    return { name: newName, values: s.values }
  })
}

watch(() => i18n.global.locale.value, () => {
  nextTick(() => {
    refreshRadarLocalization()
    recomputeInsightTexts()
  })
})

onUnmounted(() => {
  if (themeListenerBound && themeChangedHandler) { try { window.removeEventListener('theme:changed', themeChangedHandler as any) } catch {}; themeListenerBound = false; themeChangedHandler = null }
})
</script>

<style scoped>
.ms-compact :deep(.input) { min-height: 2rem; padding-top: 0.25rem; padding-bottom: 0.25rem; }
</style>

