<template>
  <div class="space-y-6">
    <header>
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">{{ t('student.analytics.title') }}</h1>
      <p class="text-gray-600 dark:text-gray-400">{{ t('student.analytics.subtitle') }}</p>
    </header>

    <FilterBar>
      <template #left>
        <GlassSelect v-model="range" :options="rangeOptions" class="w-44" />
      </template>
    </FilterBar>

    <section class="grid grid-cols-1 xl:grid-cols-2 gap-4">
      <!-- 关键指标卡片 -->
      <div class="xl:col-span-2 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <KpiCard :title="t('student.analytics.kpi.avgScore')" :value="kpis.avgScore" />
        <KpiCard :title="t('student.analytics.kpi.completion')" :value="kpis.completionRate + '%'" />
        <KpiCard :title="t('student.analytics.kpi.studyHours')" :value="kpis.studyHours" />
        <KpiCard :title="t('student.analytics.kpi.activeDays')" :value="kpis.activeDays" />
      </div>
    </section>

    <!-- 趋势图区域 -->
    <section class="grid grid-cols-1 xl:grid-cols-3 gap-4">
      <div class="glass-thick rounded-2xl border border-gray-200/40 dark:border-gray-700/40 p-4">
        <div class="text-sm text-gray-700 dark:text-gray-300 font-medium mb-2">{{ t('student.analytics.trends.score') }}</div>
        <div class="w-full h-56 glass-regular rounded-lg flex items-center justify-center text-xs text-gray-500 dark:text-gray-400">{{ trend.score.length }} points</div>
      </div>
      <div class="glass-thick rounded-2xl border border-gray-200/40 dark:border-gray-700/40 p-4">
        <div class="text-sm text-gray-700 dark:text-gray-300 font-medium mb-2">{{ t('student.analytics.trends.completion') }}</div>
        <div class="w-full h-56 glass-regular rounded-lg flex items-center justify-center text-xs text-gray-500 dark:text-gray-400">{{ trend.completion.length }} points</div>
      </div>
      <div class="glass-thick rounded-2xl border border-gray-200/40 dark:border-gray-700/40 p-4">
        <div class="text-sm text-gray-700 dark:text-gray-300 font-medium mb-2">{{ t('student.analytics.trends.hours') }}</div>
        <div class="w-full h-56 glass-regular rounded-lg flex items-center justify-center text-xs text-gray-500 dark:text-gray-400">{{ trend.hours.length }} points</div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import FilterBar from '@/components/ui/filters/FilterBar.vue'
import GlassSelect from '@/components/ui/filters/GlassSelect.vue'
import { studentApi } from '@/api/student.api'

// 简化为占位，不做动态导入，避免 Vite 加载失败

const { t } = useI18n()

const range = ref('30d')
const rangeOptions = [
  { label: t('student.analytics.range.7d') as string, value: '7d' },
  { label: t('student.analytics.range.30d') as string, value: '30d' },
  { label: t('student.analytics.range.term') as string, value: 'term' }
]

const kpis = ref<{ avgScore: number | string, completionRate: number, studyHours: number, activeDays: number }>({ avgScore: '-', completionRate: 0, studyHours: 0, activeDays: 0 })
const trend = ref<{ score: Array<{ x: string, y: number }>, completion: Array<{ x: string, y: number }>, hours: Array<{ x: string, y: number }> }>({ score: [], completion: [], hours: [] })

async function fetchAll() {
  try {
    const resp: any = await studentApi.getAnalysis({ range: range.value as any })
    const data = resp?.data || resp || {}
    const kd = data.kpi || {}
    kpis.value = {
      avgScore: typeof kd.avgScore === 'number' ? kd.avgScore : '-',
      completionRate: Math.round(Number(kd.completionRate || 0) * 100) / 100,
      studyHours: Number(kd.studyHours || 0),
      activeDays: Number(kd.activeDays || 0)
    }

    const td = data.trends || {}
    trend.value = {
      score: Array.isArray(td.score) ? td.score : [],
      completion: Array.isArray(td.completion) ? td.completion : [],
      hours: Array.isArray(td.hours) ? td.hours : []
    }
  } catch {
    // 静默失败，保持空态
    kpis.value = { avgScore: '-', completionRate: 0, studyHours: 0, activeDays: 0 }
    radarValues.value = [0,0,0,0,0]
    trend.value = { score: [], completion: [], hours: [] }
  }
}

watch(range, fetchAll)
onMounted(fetchAll)
</script>

<style scoped>
</style>


