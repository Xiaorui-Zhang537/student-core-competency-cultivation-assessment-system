<template>
  <div class="p-6">
    <!-- Header -->
    <div class="mb-8">
      <PageHeader :title="t('student.ability.title')" :subtitle="t('student.ability.subtitle')" />
    </div>

    <!-- Loading State -->
    <div v-if="abilityStore.loading" class="text-center py-12">
      <p>{{ t('student.ability.loading') }}</p>
    </div>

    <!-- Main Content -->
    <div v-else class="space-y-8">
      <!-- Controls: Course Select + Compare & Assignment Sets -->
      <Card padding="md" class="p-6" tint="info">
        <div class="flex flex-col lg:flex-row lg:items-center gap-4">
          <div class="flex items-center gap-3">
            <span class="text-sm text-gray-700 dark:text-gray-300">{{ t('student.ability.course') }}</span>
            <GlassPopoverSelect
              v-model="selectedCourseId"
              :options="courseSelectOptions"
              :placeholder="t('student.ability.selectCourse') as string"
              size="md"
              tint="primary"
              @change="onCourseChange"
            />
          </div>
          <div class="flex items-center gap-3">
            <span class="text-sm text-gray-700 dark:text-gray-300">{{ t('student.ability.compare.title') }}</span>
            <GlassSwitch v-model="compareEnabled" size="sm" />
          </div>
          <div v-if="compareEnabled" class="flex-1 grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="flex flex-col gap-2">
              <span class="text-xs font-medium text-gray-600 dark:text-gray-400">{{ t('student.ability.compare.setA') }}</span>
              <GlassMultiSelect v-model="assignmentIdsA" :options="assignmentSelectOptions" tint="primary" />
            </div>
            <div class="flex flex-col gap-2">
              <span class="text-xs font-medium text-gray-600 dark:text-gray-400">{{ t('student.ability.compare.setB') }}</span>
              <GlassMultiSelect v-model="assignmentIdsB" :options="assignmentSelectOptions" tint="secondary" />
            </div>
            <div class="md:col-span-2 flex items-center gap-3">
              <Button variant="primary" @click="applyCompare" :disabled="applyLoading">
                <template #icon>
                  <svg class="w-4 h-4" viewBox="0 0 20 20" fill="currentColor"><path d="M10 3a1 1 0 01.894.553l5 10A1 1 0 0115 15H5a1 1 0 01-.894-1.447l5-10A1 1 0 0110 3z"/></svg>
                </template>
                {{ applyLoading ? t('student.ability.compare.applying') : t('student.ability.compare.apply') }}
              </Button>
              <Button variant="outline" @click="resetCompare" :disabled="applyLoading">
                <template #icon>
                  <svg class="w-4 h-4" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M4 4a1 1 0 011-1h2a1 1 0 110 2H6v2a1 1 0 11-2 0V4zm12 12a1 1 0 01-1 1h-2a1 1 0 110-2h1v-2a1 1 0 112 0v3zM5 13a1 1 0 100-2h10a1 1 0 100 2H5z" clip-rule="evenodd"/></svg>
                </template>
                {{ t('student.ability.compare.reset') }}
              </Button>
            </div>
          </div>
        </div>
      </Card>

      <!-- Radar Chart & Recommendations -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <Card class="lg:col-span-2 p-6">
          <h2 class="text-xl font-semibold mb-4">{{ t('student.ability.radar') }}</h2>
          <div ref="radarChartRef" class="h-96 w-full"></div>
        </Card>
        <Card class="p-6">
          <h2 class="text-xl font-semibold mb-4">{{ t('student.ability.recommendations') }}</h2>
          <div v-if="abilityStore.recommendations.length > 0" class="space-y-3 max-h-96 overflow-y-auto">
            <div v-for="rec in abilityStore.recommendations" :key="rec.id" class="p-3 bg-gray-50 rounded">
              <h4 class="font-medium">{{ rec.dimension }}</h4>
              <p class="text-sm text-gray-600">{{ rec.recommendationText }}</p>
            </div>
          </div>
          <p v-else class="text-gray-500">{{ t('student.ability.noRecommendations') }}</p>
        </Card>
      </div>

      <!-- 维度图例说明（中英） -->
      <Card class="p-6" v-if="rawRadarLabels.length">
        <AbilityRadarLegend :dimensions="rawRadarLabels" />
      </Card>

      <!-- Trends Chart -->
      <Card class="p-6">
        <h2 class="text-xl font-semibold mb-4">{{ t('student.ability.trend') }}</h2>
        <div ref="trendChartRef" class="h-96 w-full"></div>
      </Card>

      <!-- Dimension Insights -->
      <div v-if="compareEnabled" class="card p-6" v-glass="{ strength: 'regular', interactive: true }">
        <h2 class="text-xl font-semibold mb-4">{{ t('student.ability.insights.title') }}</h2>
        <div v-if="insightsItems.length === 0" class="text-sm text-gray-500">{{ t('student.ability.insights.empty') }}</div>
        <div v-else class="divide-y divide-gray-200 dark:divide-gray-700">
          <div v-for="(it, idx) in insightsItems" :key="idx" class="py-3">
            <div class="flex items-center justify-between">
              <div class="text-sm font-medium text-gray-900 dark:text-gray-100">{{ localizeDimensionName(it.name) }}</div>
              <div class="text-sm text-gray-600 dark:text-gray-300">
                <span class="mr-2">A: {{ (it.scoreA ?? 0).toFixed(1) }}</span>
                <span class="mr-2">B: {{ (it.scoreB ?? 0).toFixed(1) }}</span>
                <span v-if="it.delta != null" :class="(it.delta ?? 0) >= 0 ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'">
                  Δ {{ (it.delta ?? 0).toFixed(1) }}
                </span>
              </div>
            </div>
            <div class="mt-1 text-sm text-gray-700 dark:text-gray-300">{{ it.analysis }}</div>
            <div class="mt-1 text-sm text-gray-700 dark:text-gray-300">{{ it.suggestion }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick, computed } from 'vue';
import { useAbilityStore } from '@/stores/ability';
import { abilityApi } from '@/api/ability.api';
import { useCourseStore } from '@/stores/course';
import * as echarts from 'echarts';
import { resolveEChartsTheme } from '@/charts/echartsTheme'
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'
import GlassMultiSelect from '@/components/ui/filters/GlassMultiSelect.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import GlassSwitch from '@/components/ui/inputs/GlassSwitch.vue'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import AbilityRadarLegend from '@/shared/views/AbilityRadarLegend.vue'

const abilityStore = useAbilityStore();
const courseStore = useCourseStore();
const { t, locale } = useI18n()

const radarChartRef = ref<HTMLElement | null>(null);
const trendChartRef = ref<HTMLElement | null>(null);
let radarChart: echarts.ECharts | null = null;
let trendChart: echarts.ECharts | null = null;
// 主题刷新由布局层统一处理

// 后端固定中文维度名，这里做代码映射并按当前语言本地化（与教师端一致）
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

const rawRadarLabels = ref<string[]>([])
const radarValues = ref<number[]>([])
const compareEnabled = ref<boolean>(false)
const assignmentOptions = ref<{ id: string; title: string }[]>([])
const assignmentIdsA = ref<string[]>([])
const assignmentIdsB = ref<string[]>([])
const applyLoading = ref(false)
const insightsItems = ref<any[]>([])
const selectedCourseId = ref<string | null>(null)

async function loadStudentRadar() {
  if (!courseStore.courses.length) {
    await courseStore.fetchCourses({ page: 1, size: 50 })
  }
  const courseId = selectedCourseId.value || (courseStore.courses[0] ? String(courseStore.courses[0].id) : null)
  if (!courseId) return
  const end = new Date()
  const start = new Date(end)
  start.setDate(end.getDate() - 29)
  const toStr = (d: Date) => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
  const params = { courseId: courseId, startDate: toStr(start), endDate: toStr(end) }
  const res: any = await abilityApi.getStudentRadar(params)
  const data = res?.data ?? res
  const dims: string[] = data?.dimensions || []
  rawRadarLabels.value = Array.isArray(dims) ? [...dims] : []
  radarValues.value = Array.isArray(data?.studentScores) ? data.studentScores as number[] : []
  nextTick(initRadarChart)
}

async function fetchAssignmentsForFirstCourse() {
  if (!courseStore.courses.length) return
  const cid = selectedCourseId.value || (courseStore.courses[0] ? String(courseStore.courses[0].id) : null)
  if (!cid) return
  try {
    const { assignmentApi } = await import('@/api/assignment.api')
    const res: any = await assignmentApi.getAssignmentsByCourse(String(cid), { page: 1, size: 1000 } as any)
    const items = res?.data?.items?.items || res?.data?.items || res?.items || []
    assignmentOptions.value = (items || []).map((it: any) => ({ id: String(it.id), title: it.title || (`#${it.id}`) }))
  } catch {
    assignmentOptions.value = []
  }
}

const assignmentSelectOptions = computed(() => assignmentOptions.value.map(a => ({ label: a.title, value: a.id })))

async function applyCompare() {
  if (!courseStore.courses.length) return
  const cid = selectedCourseId.value || (courseStore.courses[0] ? String(courseStore.courses[0].id) : null)
  if (!cid) return
  applyLoading.value = true
  try {
    const body: any = { courseId: String(cid) }
    if (assignmentIdsA.value?.length) body.assignmentIdsA = assignmentIdsA.value.map(id => Number(id))
    if (assignmentIdsB.value?.length) body.assignmentIdsB = assignmentIdsB.value.map(id => Number(id))
    const rCmp: any = await abilityApi.postStudentRadarCompare(body)
    const dCmp = rCmp?.data ?? rCmp
    const dims: string[] = dCmp?.dimensions || []
    rawRadarLabels.value = Array.isArray(dims) ? [...dims] : []
    const seriesA: number[] = dCmp?.seriesA?.studentScores || []
    const seriesB: number[] = dCmp?.seriesB?.studentScores || []
    radarValues.value = seriesA
    // 绘制 A/B 两条曲线
    if (radarChartRef.value) {
      const inst = echarts.getInstanceByDom(radarChartRef.value) || echarts.init(radarChartRef.value)
      const option = {
        radar: { indicator: rawRadarLabels.value.map(n => ({ name: localizeDimensionName(n), max: 100 })) },
        series: [{ type: 'radar', data: [
          { value: seriesA, name: 'A' },
          { value: seriesB, name: 'B' }
        ] }]
      }
      inst.setOption(option)
    }
    // 加载洞察
    const rIns: any = await abilityApi.postStudentDimensionInsights(body)
    insightsItems.value = (rIns?.data?.items ?? rIns?.items ?? []) as any[]
  } finally {
    applyLoading.value = false
  }
}

function resetCompare() {
  assignmentIdsA.value = []
  assignmentIdsB.value = []
  insightsItems.value = []
  // 回到默认最近30天雷达
  loadStudentRadar()
}

const initRadarChart = () => {
  if (!radarChartRef.value) return
  const theme = resolveEChartsTheme()
  radarChart = echarts.getInstanceByDom(radarChartRef.value) || echarts.init(radarChartRef.value as HTMLDivElement, theme as any)
  const option = {
    radar: { indicator: rawRadarLabels.value.map(n => ({ name: localizeDimensionName(n), max: 100 })) },
    series: [{ type: 'radar', data: [{ value: radarValues.value, name: t('teacher.analytics.charts.series.student') }] }]
  }
  radarChart.setOption(option)
}

const initTrendChart = () => {
  if (!trendChartRef.value || !abilityStore.trendsData) return;
  const theme2 = resolveEChartsTheme()
  trendChart = echarts.getInstanceByDom(trendChartRef.value) || echarts.init(trendChartRef.value as HTMLDivElement, theme2 as any);
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: abilityStore.trendsData.dates },
    yAxis: { type: 'value' },
    series: abilityStore.trendsData.dimensions.map(dim => ({ name: dim.name, type: 'line', data: dim.scores, smooth: true }))
  };
  trendChart.setOption(option);
};

const resizeCharts = () => { radarChart?.resize(); trendChart?.resize() };

watch(() => abilityStore.trendsData, (newData) => { if (newData) nextTick(initTrendChart) }, { deep: true });

onMounted(async () => {
  await Promise.all([abilityStore.fetchTrends(), abilityStore.fetchRecommendations()])
  await loadStudentRadar()
  await fetchAssignmentsForFirstCourse()
  window.addEventListener('resize', resizeCharts);
  // 不在页面层监听主题变化
});

const courseSelectOptions = computed(() => courseStore.courses.map((c: any) => ({ label: c.title, value: String(c.id) })))
function onCourseChange() {
  assignmentIdsA.value = []
  assignmentIdsB.value = []
  insightsItems.value = []
  loadStudentRadar()
  fetchAssignmentsForFirstCourse()
}

onUnmounted(() => { radarChart?.dispose(); trendChart?.dispose(); window.removeEventListener('resize', resizeCharts) });

// 语言切换时重绘雷达维度名称
watch(locale, () => { if (radarChart) initRadarChart() })
</script>
