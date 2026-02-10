<template>
  <!-- 动画分数展示（玻璃超薄风格） -->
  <div class="mt-4 glass-ultraThin rounded-xl p-4" v-glass="{ strength: 'ultraThin', interactive: true }">
    <div class="flex items-center justify-between">
      <div class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ safeValue.toFixed(1) }}</div>
      <div class="text-sm text-gray-500 dark:text-gray-400">
        / {{ safeMax }}{{ suffix }}
      </div>
    </div>
    <div class="mt-2 h-2 rounded-full bg-gray-200/60 dark:bg-gray-700/60 overflow-hidden">
      <div class="h-full rounded-full bg-primary-500/70 backdrop-blur-sm transition-all duration-300" :style="{ width: percent + '%' }"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  value: number
  max: number
  suffix?: string
}>()

const suffix = computed(() => String(props.suffix || ''))
const safeMax = computed(() => {
  const n = Number(props.max || 0)
  return Number.isFinite(n) ? n : 0
})
const safeValue = computed(() => {
  const n = Number(props.value || 0)
  if (!Number.isFinite(n)) return 0
  return Math.max(0, Math.min(safeMax.value || 0, n))
})
const percent = computed(() => {
  const max = Number(safeMax.value || 0)
  return max > 0 ? (safeValue.value / max) * 100 : 0
})
</script>

<style scoped></style>

