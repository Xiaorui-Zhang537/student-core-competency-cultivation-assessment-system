<template>
  <card padding="md" :tint="tint" class="relative">
    <div class="flex items-start justify-between gap-3 mb-3">
      <div class="min-w-0">
        <div class="text-sm font-medium truncate">{{ title }}</div>
        <div v-if="subtitle" class="text-xs text-subtle mt-1 whitespace-pre-line break-words">{{ subtitle }}</div>
      </div>
      <div class="shrink-0">
        <slot name="actions" />
      </div>
    </div>

    <trend-area-chart
      :series="series"
      :xAxisData="xAxisData"
      :height="height"
      :loading="loading"
      :grid="grid"
      :legend="legend"
      :tooltip="tooltip"
    />
  </card>
</template>

<script setup lang="ts">
import Card from '@/components/ui/Card.vue'
import TrendAreaChart from '@/components/charts/TrendAreaChart.vue'

type Point = { x: string | number; y: number }
type SeriesInput = { name: string; data: Point[]; color?: string }

withDefaults(defineProps<{
  title: string
  subtitle?: string
  tint?: 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'danger' | 'info' | null
  series: SeriesInput[]
  xAxisData?: Array<string | number>
  height?: string
  loading?: boolean
  grid?: Record<string, any>
  legend?: Record<string, any>
  tooltip?: Record<string, any>
}>(), {
  tint: 'secondary',
  xAxisData: undefined,
  height: '280px',
  loading: false,
  grid: () => ({}),
  legend: () => ({}),
  tooltip: () => ({}),
})
</script>

