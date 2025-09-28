<template>
  <div class="inline-flex items-center" :aria-label="ariaLabel">
    <svg :width="size" :height="size" :viewBox="`0 0 ${box} ${box}`" class="shrink-0">
      <circle
        :cx="center" :cy="center" :r="radius"
        :stroke-width="stroke"
        fill="none"
        stroke="currentColor"
        :style="{ color: 'color-mix(in oklab, var(--color-base-content) 18%, transparent)' }"
      />
      <circle
        :cx="center" :cy="center" :r="radius"
        :stroke-dasharray="circumference"
        :stroke-dashoffset="dashOffset"
        class="transition-[stroke-dashoffset] duration-500 ease-out"
        :stroke-width="stroke"
        fill="none"
        stroke-linecap="round"
        stroke="currentColor"
        :style="{ color: 'color-mix(in oklab, var(--color-primary) 70%, transparent)' }"
        :transform="`rotate(-90 ${center} ${center})`"
      />
      <g v-if="showCheck" :transform="`translate(${center - 5} ${center - 4})`" fill="currentColor" :style="{ color: 'color-mix(in oklab, var(--color-success) 80%, transparent)' }">
        <path d="M1.5 4.5 L3.5 6.5 L7.5 2.5" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" />
      </g>
      <text v-else-if="showLabel" :x="center" :y="center + 3" text-anchor="middle" class="fill-current text-[10px] select-none" :class="labelClass">
        {{ Math.round(normalized) }}%
      </text>
    </svg>
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  value?: number
  size?: number
  stroke?: number
  showLabel?: boolean
  showCheckAt100?: boolean
  ariaLabel?: string
  labelClass?: string | Record<string, boolean> | Array<string>
}

const props = withDefaults(defineProps<Props>(), {
  value: 0,
  size: 48,
  stroke: 6,
  showLabel: true,
  showCheckAt100: true,
  ariaLabel: 'progress circle'
})

const box = computed(() => props.size)
const center = computed(() => props.size / 2)
const radius = computed(() => Math.max(0, (props.size - props.stroke) / 2))
const circumference = computed(() => 2 * Math.PI * radius.value)
const normalized = computed(() => {
  const n = Number(props.value || 0)
  if (!Number.isFinite(n)) return 0
  return Math.min(100, Math.max(0, n))
})
const dashOffset = computed(() => circumference.value * (1 - normalized.value / 100))
const showCheck = computed(() => props.showCheckAt100 && normalized.value >= 100)
</script>

<style scoped>
svg { color: currentColor; }
</style>


