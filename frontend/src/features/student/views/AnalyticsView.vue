<template>
  <div class="space-y-6">
    <header>
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">{{ t('student.analytics.title') || '数据分析' }}</h1>
      <p class="text-gray-600 dark:text-gray-400">{{ t('student.analytics.subtitle') || '仅展示你的个人学习数据' }}</p>
    </header>

    <FilterBar>
      <template #left>
        <GlassSelect v-model="range" :options="rangeOptions" class="w-44" />
      </template>
    </FilterBar>

    <section class="grid grid-cols-1 xl:grid-cols-3 gap-4">
      <!-- 雷达图 -->
      <div class="xl:col-span-1 glass-thick glass-interactive rounded-2xl border border-gray-200/40 dark:border-gray-700/40 p-4" v-glass="{ strength: 'thick', interactive: true }">
        <h3 class="text-base font-semibold text-gray-900 dark:text-white mb-3">{{ t('student.analytics.radar.title') || '能力雷达' }}</h3>
        <div class="w-full h-64 glass-regular rounded-lg flex items-center justify-center text-sm text-gray-500 dark:text-gray-400">
          <div>Radar (placeholder) — {{ radarDimensions.length }} dims</div>
        </div>
      </div>

      <!-- 关键指标卡片 -->
      <div class="xl:col-span-2 grid grid-cols-1 md:grid-cols-2 gap-4">
        <KpiCard :title="t('student.analytics.kpi.avgScore') || '平均成绩'" :value="kpis.avgScore" />
        <KpiCard :title="t('student.analytics.kpi.completion') || '作业完成率'" :value="kpis.completionRate + '%'" />
        <KpiCard :title="t('student.analytics.kpi.studyHours') || '学习时长(h)'" :value="kpis.studyHours" />
        <KpiCard :title="t('student.analytics.kpi.activeDays') || '活跃天数'" :value="kpis.activeDays" />
      </div>
    </section>

    <!-- 趋势图区域 -->
    <section class="grid grid-cols-1 xl:grid-cols-3 gap-4">
      <div class="glass-thick rounded-2xl border border-gray-200/40 dark:border-gray-700/40 p-4">
        <div class="text-sm text-gray-700 dark:text-gray-300 font-medium mb-2">{{ t('student.analytics.trends.score') || '成绩走势' }}</div>
        <div class="w-full h-56 glass-regular rounded-lg flex items-center justify-center text-xs text-gray-500 dark:text-gray-400">{{ trend.score.length }} points</div>
      </div>
      <div class="glass-thick rounded-2xl border border-gray-200/40 dark:border-gray-700/40 p-4">
        <div class="text-sm text-gray-700 dark:text-gray-300 font-medium mb-2">{{ t('student.analytics.trends.completion') || '完成率趋势' }}</div>
        <div class="w-full h-56 glass-regular rounded-lg flex items-center justify-center text-xs text-gray-500 dark:text-gray-400">{{ trend.completion.length }} points</div>
      </div>
      <div class="glass-thick rounded-2xl border border-gray-200/40 dark:border-gray-700/40 p-4">
        <div class="text-sm text-gray-700 dark:text-gray-300 font-medium mb-2">{{ t('student.analytics.trends.hours') || '时长趋势' }}</div>
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

// 简化为占位，不做动态导入，避免 Vite 加载失败

const { t } = useI18n()

const range = ref('30d')
const rangeOptions = [
  { label: t('student.analytics.range.7d') || '近7天', value: '7d' },
  { label: t('student.analytics.range.30d') || '近30天', value: '30d' },
  { label: t('student.analytics.range.term') || '本学期', value: 'term' }
]

const radarDimensions = ref<string[]>([t('student.analytics.dimensions.invest') || '投入', t('student.analytics.dimensions.quality') || '质量', t('student.analytics.dimensions.mastery') || '掌握', t('student.analytics.dimensions.stability') || '稳定', t('student.analytics.dimensions.growth') || '成长'])
const radarValues = ref<number[]>([0,0,0,0,0])

const kpis = ref<{ avgScore: number | string, completionRate: number, studyHours: number, activeDays: number }>({ avgScore: '-', completionRate: 0, studyHours: 0, activeDays: 0 })
const trend = ref<{ score: Array<{ x: string, y: number }>, completion: Array<{ x: string, y: number }>, hours: Array<{ x: string, y: number }> }>({ score: [], completion: [], hours: [] })

async function fetchAll() {
  try {
    const [{ analyticsApi }] = await Promise.all([
      import('@/api/report.api')
    ])
    // 若 report.api 不含学生个人接口，可临时复用并在后端补学生专属端点
    const rRadar: any = await (analyticsApi as any).getStudentRadar?.({ range: range.value })
    const rKpi: any = await (analyticsApi as any).getStudentSummary?.({ range: range.value })
    const rTrends: any = await (analyticsApi as any).getStudentTrends?.({ range: range.value })

    const rd = rRadar?.data || rRadar || {}
    radarValues.value = [rd.invest||0, rd.quality||0, rd.mastery||0, rd.stability||0, rd.growth||0]

    const kd = rKpi?.data || rKpi || {}
    kpis.value = {
      avgScore: kd.avgScore ?? '-',
      completionRate: Math.round((kd.completionRate || 0) * 100) / 100,
      studyHours: kd.studyHours || 0,
      activeDays: kd.activeDays || 0
    }

    const td = rTrends?.data || rTrends || {}
    trend.value = {
      score: td.score || [],
      completion: td.completion || [],
      hours: td.hours || []
    }
  } catch {
    // 保持静默，页面显示空态
  }
}

watch(range, fetchAll)
onMounted(fetchAll)
</script>

<style scoped>
</style>


