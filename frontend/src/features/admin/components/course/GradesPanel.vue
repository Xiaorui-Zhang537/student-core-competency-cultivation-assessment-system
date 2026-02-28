<template>
  <div class="space-y-6">
    <card padding="md" tint="warning">
      <div class="flex items-center justify-between gap-3">
        <div class="text-sm font-medium">{{ t('admin.courses.tabs.grades') || '成绩' }}</div>
        <div class="flex items-center gap-2">
          <Button size="sm" variant="primary" :disabled="loading" @click="exportRadarCsvAll">
            <document-arrow-down-icon class="w-4 h-4 mr-1" />
            {{ t('admin.courses.exportAllCsv') || '全部导出' }}
          </Button>
          <Button size="sm" variant="secondary" :disabled="loading || !selectedStudentId" @click="exportRadarCsvSingle">
            <document-arrow-down-icon class="w-4 h-4 mr-1" />
            {{ t('admin.courses.exportCurrentStudentCsv') || '导出当前学生' }}
          </Button>
        </div>
      </div>
    </card>

    <loading-overlay v-if="loading" :text="String(t('common.loading') || '加载中…')" />
    <error-state v-else-if="error" :title="String(t('common.error') || '加载失败')" :message="error" :actionText="String(t('common.retry') || '重试')" @action="reloadAll" />

    <div v-else>
      <!-- KPI -->
      <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
        <start-card :label="t('teacher.analytics.cards.totalStudents') as string" :value="String(kpis.totalStudents)" tone="blue" />
        <start-card :label="t('teacher.analytics.cards.completionRate') as string" :value="completionRateText" tone="emerald" />
        <start-card :label="t('teacher.analytics.cards.averageScore') as string" :value="averageScoreText" tone="violet" />
        <start-card :label="t('teacher.analytics.cards.totalAssignments') as string" :value="String(kpis.totalAssignments)" tone="amber" />
      </div>

      <div class="grid grid-cols-12 gap-6 mt-6 items-start w-full">
        <!-- 左：分布 + 排行 -->
        <div class="col-span-12 lg:col-span-6 min-w-0 flex flex-col gap-4 self-start w-full">
          <admin-distribution-panel
            :title="t('teacher.analytics.charts.scoreDistribution') || t('admin.courses.gradeDistTitle') || '成绩分布'"
            :subtitle="t('admin.courses.gradeDistSubtitle') || ''"
            tint="info"
            :data="gradeDistPie"
            height="360px"
          />

          <card padding="lg" class="self-start w-full" tint="accent">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.analytics.tables.studentRanking') || '学生排行' }}</h3>
            </template>
            <div v-if="rankingLoading" class="py-6 text-center text-sm text-gray-500 dark:text-gray-400">{{ t('common.loading') || '加载中…' }}</div>
            <div v-else-if="!topStudents.length" class="py-6 text-center text-sm text-gray-500 dark:text-gray-400">{{ t('common.empty') || '暂无数据' }}</div>
            <div v-else class="flex flex-col">
              <ul class="divide-y divide-gray-200 dark:divide-gray-700">
                <li v-for="(s, idx) in topStudents" :key="String(s.studentId)" class="py-3 flex flex-col gap-2">
                  <div class="flex items-start justify-between gap-4">
                    <div class="flex items-start gap-3 min-w-0">
                      <span class="text-sm w-6 text-right font-semibold text-gray-600 dark:text-gray-300 pt-0.5">{{ (rankingPage - 1) * rankingPageSize + idx + 1 }}</span>
                      <div class="flex flex-col gap-1 min-w-0">
                        <div class="flex items-center gap-2 min-w-0">
                          <span class="text-sm font-semibold text-gray-900 dark:text-gray-100 truncate">{{ s.studentName || `#${s.studentId}` }}</span>
                          <badge :variant="getRadarBadgeVariant(s.radarClassification)" size="xs" class="px-2 text-xs font-semibold uppercase tracking-wide">
                            {{ formatRadarBadgeLabel(s.radarClassification) }}
                          </badge>
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
                          {{ t('teacher.students.table.noRadarData') || '暂无雷达数据' }}
                        </div>
                      </div>
                    </div>
                    <div class="flex flex-col items-end text-sm text-gray-700 dark:text-gray-200">
                      <span class="text-sm font-semibold whitespace-nowrap text-gray-900 dark:text-gray-100">{{ t('teacher.analytics.tables.radarAreaValue', { value: formatRadarArea(s.radarArea) }) || `面积 ${formatRadarArea(s.radarArea)}` }}</span>
                    </div>
                  </div>
                  <div class="shrink-0 self-end">
                    <Button
                      size="sm"
                      :variant="isSelectedStudent(s) ? 'primary' : 'outline'"
                      @click="() => pickStudent(String(s.studentId))"
                    >
                      <check-circle-icon v-if="isSelectedStudent(s)" class="w-4 h-4 mr-1" />
                      <user-icon v-else class="w-4 h-4 mr-1" />
                      {{ isSelectedStudent(s) ? (t('admin.courses.pickStudentActive') || '已选为分析对象') : (t('admin.courses.pickStudent') || '选为分析对象') }}
                    </Button>
                  </div>
                </li>
              </ul>

              <pagination-bar
                class="mt-2"
                :page="rankingPage"
                :page-size="rankingPageSize"
                :total-pages="rankingTotalPages"
                :total-items="rankingTotal"
                :disabled="rankingLoading"
                @update:page="(p:number)=> { rankingPage = p; loadStudentRanking() }"
                @update:pageSize="(s:number)=> { rankingPageSize = s; rankingPage = 1; loadStudentRanking() }"
              />
            </div>
          </card>
        </div>

        <!-- 右：雷达 + 解析 -->
        <div class="col-span-12 lg:col-span-6 min-w-0 flex flex-col gap-4 w-full">
          <card padding="lg" class="w-full" tint="warning">
            <template #header>
              <div class="flex items-center justify-between h-11">
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.analytics.settings.title') || '分析设置' }}</h3>
                <div class="flex items-center gap-3">
                  <div class="flex items-center gap-2 mr-1">
                    <span class="text-sm text-gray-700 dark:text-gray-300">{{ t('teacher.analytics.charts.enableCompare') || '对比模式' }}</span>
                    <glass-switch v-model="compareEnabled" size="sm" />
                  </div>
                  <Button size="sm" variant="info" :disabled="!selectedStudentId" @click="onRefreshRadarAndInsights">
                    <arrow-path-icon class="w-4 h-4 mr-1" />
                    {{ t('teacher.analytics.charts.refresh') || t('common.refresh') || '刷新' }}
                  </Button>
                </div>
              </div>
            </template>

            <div class="flex flex-col gap-3">
              <div class="flex items-center gap-4 whitespace-nowrap flex-nowrap">
                <div class="flex items-center gap-2 shrink-0">
                  <span class="text-xs font-medium leading-tight text-gray-700 dark:text-gray-300">{{ t('teacher.analytics.settings.studentLabel') || '学生' }}</span>
                  <glass-popover-select
                    v-model="selectedStudentId"
                    :options="studentSelectOptions"
                    :placeholder="t('teacher.analytics.charts.selectStudent') as string"
                    size="sm"
                    width="200px"
                    tint="primary"
                    @change="loadRadar"
                  />
                </div>

                <div class="flex items-center gap-2 shrink-0">
                  <span class="text-xs font-medium leading-tight text-gray-700 dark:text-gray-300">{{ t('teacher.analytics.settings.classAvgLabel') || '班级均值' }}</span>
                  <glass-popover-select
                    v-model="includeClassAvg"
                    :options="classAvgOptions"
                    size="sm"
                    width="200px"
                    tint="secondary"
                    @change="loadRadar"
                    :disabled="!compareEnabled"
                  />
                </div>
              </div>

              <div v-if="compareEnabled" class="flex flex-col gap-2">
                <div class="flex flex-wrap items-center gap-2">
                  <span class="text-sm text-gray-700 dark:text-gray-300">{{ t('teacher.analytics.settings.setA') || '集合A' }}</span>
                  <glass-multi-select v-model="assignmentIdsA" :options="assignmentSelectOptions" size="sm" tint="primary" />
                </div>
                <div class="flex flex-wrap items-center gap-2">
                  <span class="text-sm text-gray-700 dark:text-gray-300">{{ t('teacher.analytics.settings.setB') || '集合B' }}</span>
                  <glass-multi-select v-model="assignmentIdsB" :options="assignmentSelectOptions" size="sm" tint="secondary" />
                </div>
              </div>
            </div>
          </card>

          <card padding="lg" tint="secondary">
            <template #header>
              <div class="flex items-center justify-between h-11">
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.analytics.charts.radar') || '能力雷达' }}</h3>
              </div>
            </template>
            <div v-if="radarIndicators.length" class="w-full">
              <radar-chart :indicators="radarIndicators" :series="radarSeries" height="320px" :appendTooltipToBody="true" :showLegend="true" />
            </div>
            <div v-else class="text-sm text-gray-500 text-center">{{ t('teacher.analytics.charts.noRadar') || '暂无雷达数据' }}</div>
          </card>

          <card padding="lg" v-if="rawRadarDimensions.length">
            <ability-radar-legend :dimensions="rawRadarDimensions" />
          </card>

          <card padding="lg" v-if="compareEnabled || (!!selectedStudentId)" class="self-start w-full" tint="primary">
            <template #header>
              <div class="flex items-center justify-between h-11">
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.analytics.charts.dimensionInsights') || '维度解析' }}</h3>
                <Button size="sm" variant="outline" @click="loadInsights" :disabled="!selectedStudentId">
                  <arrow-path-icon class="w-4 h-4 mr-1" />
                  {{ t('teacher.analytics.charts.refresh') || '刷新' }}
                </Button>
              </div>
            </template>
            <div v-if="insightsItems.length === 0" class="text-sm text-gray-500">{{ t('teacher.analytics.charts.noInsights') || '暂无解析' }}</div>
            <div v-else class="space-y-2">
              <div
                v-for="(it, idx) in insightsItems"
                :key="idx"
                class="collapse collapse-plus glass-ultraThin border rounded-xl text-[var(--color-base-content)]"
              >
                <input type="radio" name="admin-insights" :checked="idx === 0" />
                <div class="collapse-title font-semibold flex items-center gap-2">
                  <span class="text-sm text-[var(--color-base-content)]">{{ localizeDimensionName(String(it?.name || '')) }}</span>
                  <span class="text-xs text-subtle">{{ Number(it?.scoreA ?? it?.score ?? 0).toFixed(1) }}</span>
                </div>
                <div class="collapse-content text-sm space-y-2">
                  <div class="text-[color-mix(in_oklab,var(--color-base-content)_82%,transparent)]">
                    <span class="font-medium mr-1">{{ t('shared.radarLegend.insights.labels.analysis') || '分析' }}:</span>
                    <span>{{ getInsightTexts(it).analysis }}</span>
                  </div>
                  <div class="text-[color-mix(in_oklab,var(--color-base-content)_82%,transparent)]">
                    <span class="font-medium mr-1">{{ t('shared.radarLegend.insights.labels.suggestion') || '建议' }}:</span>
                    <span>{{ getInsightTexts(it).suggestion }}</span>
                  </div>
                </div>
              </div>
            </div>
          </card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import StartCard from '@/components/ui/StartCard.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import GlassMultiSelect from '@/components/ui/filters/GlassMultiSelect.vue'
import GlassSwitch from '@/components/ui/inputs/GlassSwitch.vue'
import AdminDistributionPanel from '@/features/admin/components/AdminDistributionPanel.vue'
import RadarChart from '@/components/charts/RadarChart.vue'
import AbilityRadarLegend from '@/shared/views/AbilityRadarLegend.vue'
import LoadingOverlay from '@/components/ui/LoadingOverlay.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import { teacherApi } from '@/api/teacher.api'
import { assignmentApi } from '@/api/assignment.api'
import type { ClassPerformanceData, CourseAnalyticsData, CourseStudentPerformanceItem } from '@/types/teacher'
import { downloadCsv } from '@/utils/download'
import { useUIStore } from '@/stores/ui'
import { ArrowPathIcon, CheckCircleIcon, DocumentArrowDownIcon, UserIcon } from '@heroicons/vue/24/outline'

const props = withDefaults(defineProps<{ courseId: string; active?: boolean }>(), { active: true })
const emit = defineEmits<{ (e: 'counts', v: { totalAssignments: number; totalStudents: number }): void }>()

const { t, locale } = useI18n()
const uiStore = useUIStore()

const loading = ref(false)
const error = ref<string | null>(null)

const courseAnalytics = ref<CourseAnalyticsData | null>(null)
const classPerformance = ref<ClassPerformanceData | null>(null)
const assignments = ref<any[]>([])

const kpis = computed(() => {
  const ca: any = courseAnalytics.value || {}
  const cp: any = classPerformance.value || {}
  return {
    totalStudents: Number(ca.totalStudents ?? ca.enrollmentCount ?? cp.totalStudents ?? 0) || 0,
    totalAssignments: Number(ca.totalAssignments ?? ca.assignmentCount ?? 0) || 0,
    completionRate: Number(ca.completionRate ?? ca.averageCompletionRate ?? 0) || 0,
    averageScore: Number(ca.averageScore ?? 0) || 0,
  }
})

const completionRateText = computed(() => `${(Number(kpis.value.completionRate || 0) / 100 * 100).toFixed(1)}%`)
const averageScoreText = computed(() => Number(kpis.value.averageScore || 0).toFixed(1))

const gradeDistPie = computed(() => {
  const dist: any[] = (classPerformance.value as any)?.gradeDistribution || []
  if (!Array.isArray(dist)) return []
  return dist.map((d: any) => ({ name: String(d?.gradeLevel ?? d?.level ?? 'N/A'), value: Number(d?.count ?? 0) }))
})

// ranking
const topStudents = ref<CourseStudentPerformanceItem[]>([])
const rankingPage = ref(1)
const rankingPageSize = ref(10)
const rankingTotal = ref(0)
const rankingLoading = ref(false)
const rankingTotalPages = computed(() => {
  const total = Math.max(0, Number(rankingTotal.value || 0))
  const size = Math.max(1, Number(rankingPageSize.value || 1))
  const pages = Math.ceil(total / size)
  return pages > 0 ? pages : 1
})

function getRadarBadgeVariant(classification?: string) {
  switch ((classification || '').toUpperCase()) {
    case 'A': return 'success'
    case 'B': return 'warning'
    case 'C': return 'info'
    default: return 'danger'
  }
}
function formatRadarArea(v?: number) {
  const n = Number(v)
  if (!Number.isFinite(n)) return '--'
  return n.toFixed(1)
}
function formatRadarBadgeLabel(classification?: string) {
  const key = (classification || 'D').toUpperCase() as 'A' | 'B' | 'C' | 'D'
  const desc = t(`teacher.students.radar.classification.${key}`) as string
  return `${key} · ${desc}`
}
function formatDimensionLabel(code: string) {
  const label = t(`shared.radarLegend.dimensions.${code}.title`) as string
  return label || code
}
function formatRadarValue(value?: number) {
  if (!Number.isFinite(Number(value))) return '--'
  return Number(value).toFixed(1)
}
function isSelectedStudent(s: CourseStudentPerformanceItem) {
  return String(s?.studentId ?? '') === String(selectedStudentId.value ?? '')
}

function resolveUserDisplayName(i: any): string {
  return String(i?.studentName || i?.nickname || i?.username || '')
}

function normalizePerformanceItems(items: any[]): CourseStudentPerformanceItem[] {
  return (items || []).map((i: any) => ({
    ...i,
    studentName: resolveUserDisplayName(i) || i?.studentName || `#${i?.studentId}`,
    dimensionScores: i?.dimensionScores || {},
  }))
}

const selectedStudentId = ref<string | null>(null)
const compareEnabled = ref(false)
const includeClassAvg = ref<'none' | 'A' | 'B' | 'both'>('both')
const classAvgOptions = computed(() => ([
  { label: String(t('teacher.analytics.charts.classAvgBoth') || '班级均值: A与B'), value: 'both' },
  { label: String(t('teacher.analytics.charts.classAvgA') || '班级均值: 仅A'), value: 'A' },
  { label: String(t('teacher.analytics.charts.classAvgB') || '班级均值: 仅B'), value: 'B' },
  { label: String(t('teacher.analytics.charts.classAvgNone') || '班级均值: 关闭'), value: 'none' },
]))

const assignmentIdsA = ref<string[]>([])
const assignmentIdsB = ref<string[]>([])
const assignmentSelectOptions = computed(() => (assignments.value || []).map((a: any) => ({ label: String(a.title || `#${a.id}`), value: String(a.id) })))

// radar
const radarIndicators = ref<{ name: string; max: number }[]>([])
const radarSeries = ref<{ name: string; values: number[] }[]>([])
const rawRadarDimensions = ref<string[]>([])
const insightsItems = ref<any[]>([])

const NAME_ZH_TO_CODE: Record<string, string> = {
  '道德认知': 'MORAL_COGNITION',
  '学习态度': 'LEARNING_ATTITUDE',
  '学习能力': 'LEARNING_ABILITY',
  '学习方法': 'LEARNING_METHOD',
  '学习成绩': 'ACADEMIC_GRADE',
}
function localizeDimensionName(serverName: string): string {
  const code = NAME_ZH_TO_CODE[serverName]
  return code ? (t(`shared.radarLegend.dimensions.${code}.title`) as any) : serverName
}
function setRadarDimensions(dims: string[]) {
  rawRadarDimensions.value = Array.isArray(dims) ? [...dims] : []
  radarIndicators.value = rawRadarDimensions.value.map((n) => ({ name: localizeDimensionName(n), max: 100 }))
}
function normalizeScores(raw: any): number[] {
  if (!Array.isArray(raw)) return []
  return raw.map((v: any) => {
    const num = Number(v)
    if (!Number.isFinite(num)) return 0
    return Math.round(num * 10) / 10
  })
}

const studentOptionsCache = ref<{ key: string; options: Array<{ label: string; value: any }> }>({ key: '', options: [] })
function studentListKey(list: CourseStudentPerformanceItem[]): string {
  return (list || []).map((s) => String(s?.studentId ?? '')).join('|')
}
const studentSelectOptions = computed(() => {
  const base = topStudents.value as CourseStudentPerformanceItem[]
  const k = `${String(locale.value)}::${studentListKey(base)}`
  if (studentOptionsCache.value.key === k && studentOptionsCache.value.options.length) return studentOptionsCache.value.options
  const options = [{ label: t('teacher.analytics.charts.selectStudent') as string, value: null as any }, ...base.map((s) => ({ label: s.studentName, value: String(s.studentId) }))]
  studentOptionsCache.value = { key: k, options }
  return options
})

async function loadStudentRanking() {
  rankingLoading.value = true
  try {
    const payload: any = await teacherApi.getCourseStudentPerformance(props.courseId, {
      page: rankingPage.value,
      size: rankingPageSize.value,
      sortBy: 'radar',
    } as any)
    const items = normalizePerformanceItems(payload?.items ?? [])
    topStudents.value = items
    rankingTotal.value = Number.isFinite(Number(payload?.total)) ? Number(payload?.total) : items.length
    if (!selectedStudentId.value && items.length) selectedStudentId.value = String(items[0].studentId)
  } catch (e: any) {
    topStudents.value = []
    rankingTotal.value = 0
    uiStore.showNotification?.({ type: 'error', title: String(t('common.error') || '错误'), message: e?.message || 'Failed' })
  } finally {
    rankingLoading.value = false
  }
}

async function loadCore() {
  const [ca, cp] = await Promise.all([
    teacherApi.getCourseAnalytics(props.courseId),
    teacherApi.getClassPerformance(props.courseId),
  ])
  courseAnalytics.value = (ca as any)?.data ?? (ca as any)
  classPerformance.value = (cp as any)?.data ?? (cp as any)
  emit('counts', { totalAssignments: kpis.value.totalAssignments, totalStudents: kpis.value.totalStudents })
}

async function loadAssignments() {
  try {
    const res: any = await assignmentApi.getAssignmentsByCourse(props.courseId, { page: 1, size: 200, sort: 'createdAt,desc' } as any)
    const items = res?.items || res?.data?.items || res || []
    assignments.value = Array.isArray(items) ? items : (items?.items || [])
  } catch {
    assignments.value = []
  }
}

function buildRadarSingleParams(): any {
  const params: any = { courseId: props.courseId }
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
    { name: t('teacher.analytics.charts.series.student') as any, values: student },
    { name: t('teacher.analytics.charts.series.class') as any, values: clazz },
  ]
}

function buildRadarCompareBody(): any {
  return {
    courseId: props.courseId,
    studentId: selectedStudentId.value,
    includeClassAvg: includeClassAvg.value,
    ...(assignmentIdsA.value?.length ? { assignmentIdsA: assignmentIdsA.value } : {}),
    ...(assignmentIdsB.value?.length ? { assignmentIdsB: assignmentIdsB.value } : {}),
  }
}

async function loadRadarCompare() {
  if (!selectedStudentId.value) return
  const r: any = await teacherApi.postAbilityRadarCompare(buildRadarCompareBody())
  const data: any = r?.data?.data ?? r?.data ?? r
  setRadarDimensions(data?.dimensions || [])
  const series: { name: string; values: number[] }[] = []
  const aStu = normalizeScores(data?.seriesA?.studentScores)
  const aCls = normalizeScores(data?.seriesA?.classAvgScores)
  const bStu = normalizeScores(data?.seriesB?.studentScores)
  const bCls = normalizeScores(data?.seriesB?.classAvgScores)
  series.push({ name: 'A - ' + String(t('teacher.analytics.charts.series.student') || '学生'), values: aStu })
  if (Array.isArray(aCls) && aCls.length) series.push({ name: 'A - ' + String(t('teacher.analytics.charts.series.class') || '班级'), values: aCls })
  series.push({ name: 'B - ' + String(t('teacher.analytics.charts.series.student') || '学生'), values: bStu })
  if (Array.isArray(bCls) && bCls.length) series.push({ name: 'B - ' + String(t('teacher.analytics.charts.series.class') || '班级'), values: bCls })
  radarSeries.value = series
}

async function loadRadar() {
  try {
    if (!selectedStudentId.value) return
    if (!compareEnabled.value) await loadRadarSingle()
    else await loadRadarCompare()
  } catch (e: any) {
    uiStore.showNotification?.({ type: 'error', title: String(t('common.error') || '错误'), message: e?.message || 'Failed' })
  }
}

function buildInsightsBody(): any {
  const base: any = {
    courseId: props.courseId,
    studentId: selectedStudentId.value,
    includeClassAvg: includeClassAvg.value,
  }
  if (compareEnabled.value) {
    if (assignmentIdsA.value?.length) base.assignmentIdsA = assignmentIdsA.value
    if (assignmentIdsB.value?.length) base.assignmentIdsB = assignmentIdsB.value
  } else {
    if (assignmentIdsA.value?.length) base.assignmentIdsA = assignmentIdsA.value
    base.assignmentIdsB = assignmentIdsA.value && assignmentIdsA.value.length ? assignmentIdsA.value : undefined
  }
  return base
}

async function loadInsights() {
  try {
    if (!selectedStudentId.value) return
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
      const analysis = t(`${base}.analysis`, {
        scoreA: scoreNum.toFixed(1),
        scoreB: (Number.isFinite(Number(it?.scoreB)) ? Number(it?.scoreB).toFixed(1) : (t('shared.radarLegend.insights.na') as any)),
        delta: deltaText,
        baseline,
      }) as any
      const suggestion = t(`${base}.suggestion`, {
        scoreA: scoreNum.toFixed(1),
        scoreB: (Number.isFinite(Number(it?.scoreB)) ? Number(it?.scoreB).toFixed(1) : (t('shared.radarLegend.insights.na') as any)),
        baseline,
      }) as any
      if (analysis || suggestion) return { analysis, suggestion }
    }
    return { analysis: String(it?.analysis || ''), suggestion: String(it?.suggestion || '') }
  } catch {
    return { analysis: String(it?.analysis || ''), suggestion: String(it?.suggestion || '') }
  }
}

function pickStudent(id: string) {
  selectedStudentId.value = id
  nextTick(() => loadRadar())
  nextTick(() => loadInsights())
}

async function exportRadarCsvSingle() {
  if (!selectedStudentId.value) return
  if (compareEnabled.value) {
    const body: any = {
      courseId: props.courseId,
      studentId: selectedStudentId.value,
      includeClassAvg: includeClassAvg.value,
    }
    if (assignmentIdsA.value?.length) body.assignmentIdsA = assignmentIdsA.value
    if (assignmentIdsB.value?.length) body.assignmentIdsB = assignmentIdsB.value
    const res = await teacherApi.exportAbilityRadarCompareCsv(body, 'single')
    downloadCsv(res as any, `radar_compare_single_${props.courseId}_${selectedStudentId.value}.csv`)
  } else {
    const params: any = { courseId: props.courseId, studentId: selectedStudentId.value, scope: 'single' }
    const res = await teacherApi.exportAbilityRadarCsv(params)
    downloadCsv(res as any, `radar_single_${props.courseId}_${selectedStudentId.value}.csv`)
  }
}

async function exportRadarCsvAll() {
  if (compareEnabled.value) {
    const body: any = {
      courseId: props.courseId,
      includeClassAvg: includeClassAvg.value,
    }
    if (assignmentIdsA.value?.length) body.assignmentIdsA = assignmentIdsA.value
    if (assignmentIdsB.value?.length) body.assignmentIdsB = assignmentIdsB.value
    const res = await teacherApi.exportAbilityRadarCompareCsv(body, 'all')
    downloadCsv(res as any, `radar_compare_all_${props.courseId}.csv`)
  } else {
    const res = await teacherApi.exportAbilityRadarCsv({ courseId: props.courseId, scope: 'all' } as any)
    downloadCsv(res as any, `radar_all_${props.courseId}.csv`)
  }
}

async function onRefreshRadarAndInsights() {
  await loadRadar()
  await loadInsights()
}

async function reloadAll() {
  loading.value = true
  error.value = null
  try {
    await Promise.all([loadCore(), loadAssignments(), loadStudentRanking()])
    await loadRadar()
    await loadInsights()
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

watch(
  () => props.active,
  (v) => {
    if (v) reloadAll()
  },
  { immediate: true }
)

watch(compareEnabled, () => {
  // compare 切换时刷新雷达与解析
  nextTick(() => loadRadar())
  nextTick(() => loadInsights())
})

watch(selectedStudentId, () => {
  emit('counts', { totalAssignments: kpis.value.totalAssignments, totalStudents: kpis.value.totalStudents })
})

onMounted(() => {
  if (props.active !== false) reloadAll()
})
</script>

