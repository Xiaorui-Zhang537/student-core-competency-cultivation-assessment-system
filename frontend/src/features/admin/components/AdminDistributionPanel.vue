<template>
  <card padding="md" :tint="tint">
    <div class="flex items-start justify-between gap-3 mb-3">
      <div class="min-w-0">
        <div class="text-sm font-medium truncate">{{ title }}</div>
        <div v-if="subtitle" class="text-xs text-subtle mt-1 whitespace-pre-line break-words">{{ subtitle }}</div>
      </div>
      <div class="shrink-0">
        <slot name="actions" />
      </div>
    </div>

    <pie-chart :data="data" :height="height" :loading="loading" :showLabel="showLabel" :labelPosition="labelPosition" />
  </card>
</template>

<script setup lang="ts">
import Card from '@/components/ui/Card.vue'
import PieChart from '@/components/charts/PieChart.vue'

interface PieData { name: string; value: number; color?: string }

withDefaults(defineProps<{
  title: string
  subtitle?: string
  tint?: 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'danger' | 'info' | null
  data: PieData[]
  height?: string
  loading?: boolean
  showLabel?: boolean
  labelPosition?: 'inside' | 'outside'
}>(), {
  tint: 'secondary',
  height: '320px',
  loading: false,
  showLabel: true,
  labelPosition: 'outside',
})
</script>

