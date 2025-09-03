<template>
  <div class="p-6">
    <PageHeader :title="t('student.analysis.title')" :subtitle="t('student.analysis.subtitle')" />

    <div v-if="loading" class="text-center py-12">{{ t('student.analysis.loading') }}</div>

    <div v-else class="space-y-8">
      <!-- KPIs -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard :label="t('student.analysis.kpiAvgScore')" :value="formatScore(kpi.avgScore)" tone="blue" />
        <StatCard :label="t('student.analysis.kpiCompletion')" :value="formatPercent(kpi.completionRate)" tone="emerald" />
        <StatCard :label="t('student.analysis.kpiHours')" :value="String(kpi.studyHours)" tone="violet" />
        <StatCard :label="t('student.analysis.kpiActiveDays')" :value="String(kpi.activeDays)" tone="amber" />
      </div>

      <!-- Trends -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div class="p-6 glass-regular rounded-lg shadow" v-glass="{ strength: 'regular', interactive: true }">
          <h3 class="text-lg font-semibold mb-4">{{ t('student.analysis.trendScore') }}</h3>
          <TrendAreaChart :series="scoreSeriesPoints" :xAxisData="scoreXAxis" height="280px" />
        </div>
        <div class="p-6 glass-regular rounded-lg shadow" v-glass="{ strength: 'regular', interactive: true }">
          <h3 class="text-lg font-semibold mb-4">{{ t('student.analysis.trendCompletion') }}</h3>
          <TrendAreaChart :series="completionSeriesPoints" :xAxisData="completionXAxis" height="280px" />
        </div>
        <div class="p-6 glass-regular rounded-lg shadow" v-glass="{ strength: 'regular', interactive: true }">
          <h3 class="text-lg font-semibold mb-4">{{ t('student.analysis.trendHours') }}</h3>
          <TrendAreaChart :series="hoursSeriesPoints" :xAxisData="hoursXAxis" height="280px" />
        </div>
      </div>
      
      <div v-if="empty" class="text-center text-gray-500">{{ t('student.analysis.empty') }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { studentApi } from '@/api/student.api'
import { useI18n } from 'vue-i18n'
import { i18n, loadLocaleMessages } from '@/i18n'
import StatCard from '@/components/ui/StatCard.vue'
import TrendAreaChart from '@/components/charts/TrendAreaChart.vue'
import PageHeader from '@/components/ui/PageHeader.vue'

const { t } = useI18n()

type Point = { x: string; y: number }

const loading = ref(false)
const kpi = ref<{ avgScore: number; completionRate: number; studyHours: number; activeDays: number }>({ avgScore: 0, completionRate: 0, studyHours: 0, activeDays: 0 })
// Removed radar from AnalysisView to avoid duplication with AbilityView
const trends = ref<{ score: Point[]; completion: Point[]; hours: Point[] }>({ score: [], completion: [], hours: [] })

// no radar indicators/values in this view

const empty = computed(() =>
  (trends.value.score.length + trends.value.completion.length + trends.value.hours.length) === 0
)

const toSeriesPoints = (name: string, arr: Point[]) => [{ name, data: Array.isArray(arr) ? arr.map(p => ({ x: String(p?.x ?? ''), y: Number(p?.y) || 0 })) : [] }]
const toXAxis = (arr: Point[]) => (Array.isArray(arr) ? arr.map(p => String(p?.x ?? '')) : [])

const scoreSeriesPoints = computed(() => toSeriesPoints(t('student.analysis.series.score') as string, trends.value.score))
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
  // radar removed here; ability radar lives in AbilityView
  trends.value = data.trends || trends.value
}

const formatScore = (v: number) => `${Math.round(Number(v || 0))}`
const formatPercent = (v: number) => `${Math.round(Number(v || 0))}%`

onMounted(async () => {
  // 防御：确保当前 locale 的 student 命名空间已加载
  const locRaw = String(i18n.global.locale.value)
  const loc = (locRaw === 'zh' ? 'zh-CN' : (locRaw === 'en' ? 'en-US' : (locRaw as 'zh-CN' | 'en-US')))
  await loadLocaleMessages(loc, ['student'])
  load()
})
</script>
