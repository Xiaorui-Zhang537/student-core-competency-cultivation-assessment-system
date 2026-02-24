<template>
  <div class="space-y-4">
    <card padding="md" tint="secondary">
      <div class="flex items-start justify-between gap-3">
        <div class="min-w-0">
          <div class="text-sm font-medium line-clamp-2">{{ report.title || '-' }}</div>
          <div class="text-xs text-subtle mt-1">
            <span class="mr-2">#{{ report.id }}</span>
            <span v-if="(report as any).studentName || (report as any).studentId" class="mr-2">
              · {{ t('common.columns.student') || '学生' }}: {{ (report as any).studentName || '-' }} (#{{ (report as any).studentId || '-' }})
            </span>
            <span v-if="report.reportType" class="mr-2">· {{ report.reportType }}</span>
            <span v-if="report.createdAt">· {{ report.createdAt }}</span>
          </div>
        </div>
        <div class="shrink-0 text-right">
          <div class="text-xs text-subtle">{{ t('admin.moderation.score') || '分数' }}</div>
          <div class="text-2xl font-semibold">{{ overallScoreText }}</div>
        </div>
      </div>

      <div class="mt-3">
        <div class="h-2 w-full rounded-md overflow-hidden border border-gray-300/70 dark:border-white/10 bg-gray-200/60 dark:bg-white/10 shadow-inner">
          <div class="h-full bg-gradient-to-r from-primary/70 to-accent/70" :style="{ width: scoreBarWidth }"></div>
        </div>
      </div>
    </card>

    <card padding="md" tint="secondary">
      <div class="text-sm font-medium mb-2">{{ t('teacher.aiGrading.render.dimension_averages') || t('admin.student360.radarTitle') || '维度得分' }}</div>
      <div v-if="dimensionRows.length" class="space-y-2">
        <div v-for="row in dimensionRows" :key="row.key" class="flex items-center gap-3">
          <div class="w-44 text-xs text-gray-700 dark:text-gray-300 truncate" :title="row.label">
            {{ row.label }}
          </div>
          <div class="h-2 flex-1 rounded-md overflow-hidden border border-gray-300/70 dark:border-white/10 bg-gray-200/60 dark:bg-white/10 shadow-inner">
            <div class="h-full bg-gradient-to-r from-info/70 to-success/70" :style="{ width: `${Math.max(0, Math.min(100, row.value))}%` }"></div>
          </div>
          <div class="w-12 text-right text-xs text-subtle">{{ row.valueText }}</div>
        </div>
      </div>
      <empty-state v-else :title="String(t('common.empty') || '暂无数据')" />
    </card>

    <card padding="md" tint="secondary">
      <div class="text-sm font-medium mb-2">{{ t('admin.moderation.recommendations') || '建议' }}</div>
      <div class="text-sm whitespace-pre-wrap break-words">{{ report.recommendations || '-' }}</div>
    </card>

    <card v-if="rawDimensionScoresText" padding="md" tint="secondary">
      <div class="text-xs text-subtle mb-2">dimensionScores(JSON)</div>
      <pre class="text-xs whitespace-pre-wrap break-words opacity-90">{{ rawDimensionScoresText }}</pre>
    </card>
  </div>
</template>

<script setup lang="ts">
/**
 * AbilityReportViewer
 *
 * 用途：将 ability_report 的通用展示逻辑抽成可复用组件，供管理员端/教师端复用。
 * 兼容维度得分为 JSON 字符串或对象两种形态。
 */
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

type AnyReport = {
  id: number | string
  studentId?: number | string
  studentName?: string
  title?: string
  reportType?: string
  createdAt?: string
  overallScore?: string | number
  dimensionScores?: any
  recommendations?: string
}

const props = defineProps<{
  report: AnyReport
}>()

const { t } = useI18n()

const overallScoreNumber = computed(() => {
  const v = Number((props.report as any)?.overallScore)
  return Number.isFinite(v) ? v : NaN
})

const overallScoreText = computed(() => {
  if (!Number.isFinite(overallScoreNumber.value)) return '--'
  // 默认按 0-100 显示；若本身是 0-5 也会自然展示
  return Number.isInteger(overallScoreNumber.value) ? String(overallScoreNumber.value) : overallScoreNumber.value.toFixed(1)
})

const scoreBarWidth = computed(() => {
  if (!Number.isFinite(overallScoreNumber.value)) return '0%'
  const v = Math.max(0, Math.min(100, overallScoreNumber.value))
  return `${v}%`
})

const rawDimensionScoresText = computed(() => {
  const ds = (props.report as any)?.dimensionScores
  if (ds == null || ds === '') return ''
  if (typeof ds === 'string') return ds
  try { return JSON.stringify(ds, null, 2) } catch { return String(ds) }
})

function parseDimensionScores(): Record<string, number> {
  const ds = (props.report as any)?.dimensionScores
  if (!ds) return {}
  let obj: any = ds
  if (typeof ds === 'string') {
    try { obj = JSON.parse(ds) } catch { return {} }
  }
  if (!obj || typeof obj !== 'object') return {}
  const out: Record<string, number> = {}
  for (const [k, v] of Object.entries(obj)) {
    const n = Number(v as any)
    if (Number.isFinite(n)) out[String(k)] = n
  }
  return out
}

const dimensionRows = computed(() => {
  const map = parseDimensionScores()
  const preferredOrder = [
    'MORAL_COGNITION',
    'LEARNING_ATTITUDE',
    'LEARNING_ABILITY',
    'LEARNING_METHOD',
    'ACADEMIC_GRADE',
  ]

  const keys = Object.keys(map)
  keys.sort((a, b) => {
    const ia = preferredOrder.indexOf(a)
    const ib = preferredOrder.indexOf(b)
    if (ia === -1 && ib === -1) return a.localeCompare(b)
    if (ia === -1) return 1
    if (ib === -1) return -1
    return ia - ib
  })

  return keys.map((k) => {
    const label = (t(`shared.radarLegend.dimensions.${k}.title`) as any) || k
    const val = map[k]
    return {
      key: k,
      label: String(label),
      value: Math.max(0, Math.min(100, Number(val))),
      valueText: Number.isInteger(val) ? String(val) : Number(val).toFixed(1),
    }
  })
})
</script>

