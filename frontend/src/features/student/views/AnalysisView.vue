<template>
  <div class="p-6">
    <div class="mb-8">
      <h1 class="text-3xl font-bold">{{ t('student.analysis.title') }}</h1>
      <p class="text-gray-500">{{ t('student.analysis.subtitle') }}</p>
    </div>

    <div v-if="loading" class="text-center py-12">{{ t('student.analysis.loading') }}</div>

    <div v-else class="space-y-8">
      <!-- KPIs -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard :label="t('student.analysis.kpiAvgScore')" :value="formatScore(kpi.avgScore)" tone="blue" />
        <StatCard :label="t('student.analysis.kpiCompletion')" :value="formatPercent(kpi.completionRate)" tone="emerald" />
        <StatCard :label="t('student.analysis.kpiHours')" :value="String(kpi.studyHours)" tone="violet" />
        <StatCard :label="t('student.analysis.kpiActiveDays')" :value="String(kpi.activeDays)" tone="amber" />
      </div>

      <!-- Radar -->
      <div class="p-6 glass-regular rounded-lg shadow" v-glass="{ strength: 'regular', interactive: true }">
        <h2 class="text-xl font-semibold mb-4">{{ t('student.analysis.radarTitle') }}</h2>
        <RadarChart
          :indicators="radarIndicators"
          :series="[{ name: 'Me', values: radarValues }]"
          height="360px"
        />
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
import StatCard from '@/components/ui/StatCard.vue'
import RadarChart from '@/components/charts/RadarChart.vue'
import TrendAreaChart from '@/components/charts/TrendAreaChart.vue'

const { t } = useI18n()

type Point = { x: string; y: number }

const loading = ref(false)
const kpi = ref<{ avgScore: number; completionRate: number; studyHours: number; activeDays: number }>({ avgScore: 0, completionRate: 0, studyHours: 0, activeDays: 0 })
const radar = ref<{ invest: number; quality: number; mastery: number; stability: number; growth: number }>({ invest: 0, quality: 0, mastery: 0, stability: 0, growth: 0 })
const trends = ref<{ score: Point[]; completion: Point[]; hours: Point[] }>({ score: [], completion: [], hours: [] })

const radarIndicators = computed(() => [
  { name: 'Invest', max: 100 },
  { name: 'Quality', max: 100 },
  { name: 'Mastery', max: 100 },
  { name: 'Stability', max: 100 },
  { name: 'Growth', max: 100 }
])

const radarValues = computed(() => [
  radar.value.invest || 0,
  radar.value.quality || 0,
  radar.value.mastery || 0,
  radar.value.stability || 0,
  radar.value.growth || 0
])

const empty = computed(() =>
  (trends.value.score.length + trends.value.completion.length + trends.value.hours.length) === 0
)

const toSeriesPoints = (name: string, arr: Point[]) => [{ name, data: Array.isArray(arr) ? arr.map(p => ({ x: String(p?.x ?? ''), y: Number(p?.y) || 0 })) : [] }]
const toXAxis = (arr: Point[]) => (Array.isArray(arr) ? arr.map(p => String(p?.x ?? '')) : [])

const scoreSeriesPoints = computed(() => toSeriesPoints('Score', trends.value.score))
const scoreXAxis = computed(() => toXAxis(trends.value.score))
const completionSeriesPoints = computed(() => toSeriesPoints('Completion', trends.value.completion))
const completionXAxis = computed(() => toXAxis(trends.value.completion))
const hoursSeriesPoints = computed(() => toSeriesPoints('Hours', trends.value.hours))
const hoursXAxis = computed(() => toXAxis(trends.value.hours))

const load = async () => {
  loading.value = true
  const res: any = await studentApi.getAnalysis()
  loading.value = false
  if (!res) return
  const data = res
  kpi.value = data.kpi || kpi.value
  radar.value = data.radar || radar.value
  trends.value = data.trends || trends.value
}

const formatScore = (v: number) => `${Math.round(Number(v || 0))}`
const formatPercent = (v: number) => `${Math.round(Number(v || 0))}%`

onMounted(() => {
  load()
})
</script>
