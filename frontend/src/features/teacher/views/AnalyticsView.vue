<template>
  <div class="min-h-screen p-6">
    <PageHeader :title="t('teacher.analytics.title')" :subtitle="t('teacher.analytics.subtitle')">
      <template #actions>
        <div class="flex items-center space-x-4">
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
                <Button size="sm" variant="info" @click="openWeights = true" :disabled="!selectedCourseId" class="shadow-sm">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-4 h-4 mr-1">
                    <path d="M11.25 3.5a.75.75 0 0 1 .75-.75h1.5a.75.75 0 0 1 .75.75v1.19a5.25 5.25 0 0 1 2.56 1.06l.84-.84a.75.75 0 0 1 1.06 0l1.06 1.06a.75.75 0 0 1 0 1.06l-.84.84a5.25 5.25 0 0 1 1.06 2.56h1.19a.75.75 0 0 1 .75.75v1.5a.75.75 0 0 1-.75.75h-1.19a5.25 5.25 0 0 1-1.06 2.56l.84.84a.75.75 0 0 1 0 1.06l-1.06 1.06a.75.75 0 0 1-1.06 0l-.84-.84a5.25 5.25 0 0 1-2.56 1.06v1.19a.75.75 0 0 1-.75.75h-1.5a.75.75 0 0 1-.75-.75v-1.19a5.25 5.25 0 0 1-2.56-1.06l-.84.84a.75.75 0 0 1-1.06 0L5.27 17a.75.75 0 0 1 0-1.06l.84-.84a5.25 5.25 0 0 1-1.06-2.56H3.86a.75.75 0 0 1-.75-.75v-1.5c0-.414.336-.75.75-.75h1.19a5.25 5.25 0 0 1 1.06-2.56l-.84-.84a.75.75 0 0 1 0-1.06l1.06-1.06a.75.75 0 0 1 1.06 0l.84.84a5.25 5.25 0 0 1 2.56-1.06V3.5Z"/>
                    <path d="M12 8.75a3.25 3.25 0 1 0 0 6.5 3.25 3.25 0 0 0 0-6.5Z"/>
                  </svg>
                  {{ t('teacher.analytics.charts.setWeights') }}
                </Button>
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
              <div v-if="!compareEnabled" class="flex items-center gap-2">
                <span class="text-xs font-medium leading-tight text-gray-700 dark:text-gray-300">{{ t('teacher.analytics.settings.timeFilter') }}</span>
                <GlassDateTimePicker v-model="startDate" :dateOnly="true" size="sm" tint="accent" />
                <span class="text-xs text-gray-500">-</span>
                <GlassDateTimePicker v-model="endDate" :dateOnly="true" size="sm" tint="accent" />
              </div>
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
import { assignmentApi } from '@/api/assignment.api'
import type { CourseStudentPerformanceItem } from '@/types/teacher'
import Card from '@/components/ui/Card.vue'
import StartCard from '@/components/ui/StartCard.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Progress from '@/components/ui/Progress.vue'
import * as echarts from 'echarts'
import { resolveEChartsTheme, glassTooltipCss, resolveThemePalette } from '@/charts/echartsTheme'
import RadarChart from '@/components/charts/RadarChart.vue'
import AbilityRadarLegend from '@/shared/views/AbilityRadarLegend.vue'
import AbilityWeightsDialog from '@/features/teacher/components/AbilityWeightsDialog.vue'
import { DocumentArrowDownIcon, SparklesIcon, ArrowPathIcon, UserGroupIcon, CheckCircleIcon, StarIcon, ClipboardDocumentListIcon } from '@heroicons/vue/24/outline'
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'
import GlassSelect from '@/components/ui/filters/GlassSelect.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import GlassDateTimePicker from '@/components/ui/inputs/GlassDateTimePicker.vue'
import GlassMultiSelect from '@/components/ui/filters/GlassMultiSelect.vue'
import GlassSwitch from '@/components/ui/inputs/GlassSwitch.vue'

// Stores
const teacherStore = useTeacherStore()
const courseStore = useCourseStore()
const authStore = useAuthStore()
const uiStore = useUIStore()
const { t, n, locale } = useI18n()

// 状态
const selectedCourseId = ref<string | null>(null)
const topStudents = ref<CourseStudentPerformanceItem[]>([])
const studentTotal = ref<number>(0)
const selectedStudentId = ref<string | null>(null)
function fmt(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}
const today = new Date()
const start30 = new Date(today)
start30.setDate(start30.getDate() - 29)
const startDate = ref<string>(fmt(start30))
const endDate = ref<string>(fmt(today))
// Compare mode states
const compareEnabled = ref<boolean>(false)
const includeClassAvg = ref<'none'|'A'|'B'|'both'>('both')
const startDateA = ref<string>(fmt(start30))
const endDateA = ref<string>(fmt(today))
const startDateB = ref<string>(fmt(start30))
const endDateB = ref<string>(fmt(today))
const assignmentOptions = ref<{ id: string; title: string }[]>([])
const assignmentIdsA = ref<string[]>([])
const assignmentIdsB = ref<string[]>([])
const insightsItems = ref<any[]>([])
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
  // 与学生端统一，改用共享维度标题
  return code ? (t(`shared.radarLegend.dimensions.${code}.title`) as any) : serverName
}

// 图表引用
const learningTrendRef = ref<HTMLElement>()
const scoreDistributionRef = ref<HTMLElement>()
const coursePerformanceRef = ref<HTMLElement>()

let learningTrendChart: echarts.ECharts | null = null
let scoreDistributionChart: echarts.ECharts | null = null
let darkObserver: MutationObserver | null = null
let lastIsDark: boolean | null = null
let reRenderScheduled = false
let coursePerformanceChart: echarts.ECharts | null = null

// 计算属性：当前教师的课程列表
const teacherCourses = computed(() => {
  if (!authStore.user?.id) return []
  return courseStore.courses.filter(c => String(c.teacherId) === String(authStore.user?.id))
})

const courseSelectOptions = computed(() => teacherCourses.value.map((c: any) => ({ label: String(c.title || ''), value: String(c.id) })))
const studentSelectOptions = computed(() => [{ label: t('teacher.analytics.charts.selectStudent') as string, value: null as any }, ...topStudents.value.map((s: CourseStudentPerformanceItem) => ({ label: s.studentName, value: String(s.studentId) }))])
const assignmentSelectOptions = computed(() => assignmentOptions.value.map((a: { id: string; title: string }) => ({ label: a.title, value: String(a.id) })))

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
    const theme = resolveEChartsTheme()
    inst = echarts.init(el as HTMLDivElement, theme as any)
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
          itemStyle: { color: resolveThemePalette()[idx % resolveThemePalette().length] },
          emphasis: { itemStyle: { opacity: 0.85 } },
          blur: { itemStyle: { opacity: 1 } }
        })),
        emphasis: { focus: 'none', scale: false },
      }
    ]
  }

  scoreDistributionChart && scoreDistributionChart.setOption(option)
  // 初始化后立即隐藏 tooltip，避免左上角残留小空白容器
  try { scoreDistributionChart?.dispatchAction({ type: 'hideTip' } as any) } catch {}
  try { (scoreDistributionChart as any).off && (scoreDistributionChart as any).off('globalout') } catch {}
  try {
    scoreDistributionChart?.on('globalout', () => {
      try { scoreDistributionChart?.dispatchAction({ type: 'hideTip' } as any) } catch {}
    })
  } catch {}
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

const onCourseChange = () => {
  if (!selectedCourseId.value) return
  // 切换课程时重置本地总学生兜底值，避免显示上一个课程数据
  studentTotal.value = 0
  // 同步URL query，统一入口 /teacher/analytics?courseId=
  router.replace({ name: 'TeacherAnalytics', query: { courseId: selectedCourseId.value, studentId: selectedStudentId.value || undefined } })
  teacherStore.fetchCourseAnalytics(selectedCourseId.value)
  teacherStore.fetchClassPerformance(selectedCourseId.value)
  // 初始化权重
  teacherApi.getAbilityWeights(selectedCourseId.value).then((r: any) => { weights.value = r?.weights || r?.data?.weights || null }).catch(() => {})
  // 获取学生表现排行（前5名，按成绩）
  // 获取课程全部学生，用于雷达图下拉选择任意学生
  teacherApi.getAllCourseStudentsBasic(selectedCourseId.value)
    .then((data: any) => {
      topStudents.value = (data?.items ?? []) as CourseStudentPerformanceItem[]
      if (typeof data?.total === 'number') studentTotal.value = data.total
      // 若路由携带 studentId，则自动选中该学生
      const routeStudentId: any = (route.query as any)?.studentId || null
      if (routeStudentId) {
        selectedStudentId.value = String(routeStudentId)
      }
    })
    .catch(() => { topStudents.value = [] })
  nextTick(() => { 
    initCharts()
    loadRadar()
  })
  // 若开启对比，切课时同步加载作业列表，避免选项为空
  if (compareEnabled.value && selectedCourseId.value) {
    assignmentOptions.value = []
    assignmentApi.getAssignmentsByCourse(selectedCourseId.value, { page: 1, size: 1000 } as any)
      .then((res: any) => {
        const items = res?.data?.items?.items || res?.data?.items || res?.items || []
        assignmentOptions.value = (items || []).map((it: any) => ({ id: String(it.id), title: it.title || (`#${it.id}`) }))
      })
      .catch(() => { assignmentOptions.value = [] })
  }
}

// 当路由中的 courseId 改变（例如从其它页面跳转）时，自动同步当前课程并刷新数据
watch(() => route.query.courseId, (cid) => {
  if (cid) {
    selectedCourseId.value = String(cid)
    onCourseChange()
  }
})

// 当路由中的 studentId 改变时，自动选中该学生并刷新雷达图
watch(() => route.query.studentId, (sid) => {
  selectedStudentId.value = sid ? String(sid) : null
  if (selectedCourseId.value) {
    loadRadar()
  }
})

// 监听班级表现数据变化以刷新成绩分布图
watch(() => teacherStore.classPerformance, () => {
  nextTick(() => initScoreDistributionChart())
}, { deep: true })

// 学生切换时自动刷新雷达图
watch(selectedStudentId, () => {
  loadRadar()
  // 学生选择变化时，自动刷新维度解析
  loadInsights()
})

watch(compareEnabled, () => {
  if (compareEnabled.value) {
    // 加载课程作业
    if (selectedCourseId.value) {
      assignmentOptions.value = []
      assignmentApi.getAssignmentsByCourse(selectedCourseId.value, { page: 1, size: 1000 } as any)
        .then((res: any) => {
          const items = res?.data?.items?.items || res?.data?.items || res?.items || []
          assignmentOptions.value = (items || []).map((it: any) => ({ id: String(it.id), title: it.title || (`#${it.id}`) }))
        })
        .catch(() => { assignmentOptions.value = [] })
    }
    loadInsights()
  }
  loadRadar()
})

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
    const onChanging = () => { try { scoreDistributionChart?.dispatchAction({ type: 'hideTip' } as any) } catch {} }
    const onChanged = () => scheduleReinitScoreDistribution()
    darkObserver = {
      disconnect() {
        try { window.removeEventListener('theme:changing', onChanging) } catch {}
        try { window.removeEventListener('theme:changed', onChanged) } catch {}
      }
    } as any
    try { window.addEventListener('theme:changing', onChanging) } catch {}
    try { window.addEventListener('theme:changed', onChanged) } catch {}
  }
})

onUnmounted(() => {
  scoreDistributionChart?.dispose()
  window.removeEventListener('resize', resizeCharts)
  if (darkObserver) { try { darkObserver.disconnect() } catch {}; darkObserver = null }
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
  router.push({ path: '/teacher/assistant', query: { q } })
}

const loadRadar = async () => {
  try {
    if (!selectedCourseId.value) return
    if (!compareEnabled.value) {
      if (!startDate.value || !endDate.value) return
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
    } else {
      if (!selectedStudentId.value) {
        return uiStore.showNotification({ type: 'warning', title: t('teacher.analytics.messages.chooseStudent') || '请选择学生', message: t('teacher.analytics.messages.chooseStudentMsg') || '在对比模式下需先选择学生' })
      }
      const body: any = {
        courseId: selectedCourseId.value,
        studentId: selectedStudentId.value,
        includeClassAvg: includeClassAvg.value,
      }
      if (startDateA.value && endDateA.value) { body.startDateA = startDateA.value; body.endDateA = endDateA.value }
      if (startDateB.value && endDateB.value) { body.startDateB = startDateB.value; body.endDateB = endDateB.value }
      if (assignmentIdsA.value?.length) body.assignmentIdsA = assignmentIdsA.value
      if (assignmentIdsB.value?.length) body.assignmentIdsB = assignmentIdsB.value
      const r: any = await teacherApi.postAbilityRadarCompare(body)
      const data: any = r?.data?.data ?? r?.data ?? r
      const dims: string[] = data?.dimensions || []
      rawRadarDimensions.value = [...dims]
      radarIndicators.value = dims.map(n => ({ name: localizeDimensionName(n), max: 100 }))
      const series: { name: string; values: number[] }[] = []
      const aStu = (data?.seriesA?.studentScores || []) as number[]
      const aCls = (data?.seriesA?.classAvgScores || []) as number[]
      const bStu = (data?.seriesB?.studentScores || []) as number[]
      const bCls = (data?.seriesB?.classAvgScores || []) as number[]
      series.push({ name: 'A - ' + (t('teacher.analytics.charts.series.student') || '学生'), values: aStu })
      if (Array.isArray(aCls) && aCls.length) series.push({ name: 'A - ' + (t('teacher.analytics.charts.series.class') || '班级'), values: aCls })
      series.push({ name: 'B - ' + (t('teacher.analytics.charts.series.student') || '学生'), values: bStu })
      if (Array.isArray(bCls) && bCls.length) series.push({ name: 'B - ' + (t('teacher.analytics.charts.series.class') || '班级'), values: bCls })
      radarSeries.value = series
    }
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
    const blob = new Blob([res as any], { type: 'text/csv;charset=utf-8;' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `radar_compare_${selectedCourseId.value}_${selectedStudentId.value}.csv`
    a.click()
    window.URL.revokeObjectURL(url)
  } else {
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
}

const loadInsights = async () => {
  try {
    if (!selectedCourseId.value || !selectedStudentId.value) return
    const body: any = {
      courseId: selectedCourseId.value,
      studentId: selectedStudentId.value,
      includeClassAvg: includeClassAvg.value,
    }
    // compare 开启：带上A/B作业集；未开启：仅用A作业集或留空（后端将用同组对比）
    if (compareEnabled.value) {
      if (assignmentIdsA.value?.length) body.assignmentIdsA = assignmentIdsA.value
      if (assignmentIdsB.value?.length) body.assignmentIdsB = assignmentIdsB.value
    } else {
      if (assignmentIdsA.value?.length) body.assignmentIdsA = assignmentIdsA.value
      body.assignmentIdsB = assignmentIdsA.value && assignmentIdsA.value.length ? assignmentIdsA.value : undefined
    }
    const r: any = await teacherApi.postAbilityDimensionInsights(body)
    insightsItems.value = (r?.data?.data?.items ?? r?.data?.items ?? r?.items ?? []) as any[]
  } catch (e: any) {
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
