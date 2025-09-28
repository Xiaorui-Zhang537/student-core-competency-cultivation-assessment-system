<template>
  <div
    :class="[
      'rounded-2xl border shadow-sm transition-all duration-200 relative z-10 opacity-100 glass-regular glass-interactive',
      tintClass,
      hoverable ? 'hover:shadow-md hover:scale-[1.02]' : '',
      dense ? 'p-4' : 'p-6'
    ]"
  >
    <div class="flex items-center justify-between mb-2">
      <span :class="['text-sm', labelColor]">{{ label }}</span>
      <component v-if="icon" :is="icon" :class="['w-5 h-5', iconColor]" />
    </div>
    <div :class="['text-3xl font-bold', valueColor]">
      <slot name="value">
        {{ value }}
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  label: string
  value?: string | number
  tone?: 'blue' | 'emerald' | 'violet' | 'amber' | 'indigo'
  tint?: 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'info' | null
  icon?: any
  hoverable?: boolean
  dense?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  tone: 'blue',
  tint: null,
  hoverable: true,
  dense: false
})

// 兼容旧 tone，映射到新主题变体
const resolvedTint = computed(() => {
  if (props.tint) return props.tint
  const map: Record<string, 'primary'|'success'|'accent'|'warning'|'info'> = {
    blue: 'primary',
    emerald: 'success',
    violet: 'accent',
    amber: 'warning',
    indigo: 'info'
  }
  return map[props.tone] || 'primary'
})

const tintClass = computed(() => `glass-tint-${resolvedTint.value}`)

const labelColor = computed(() => `text-[var(--color-${resolvedTint.value}-content)]`)
const valueColor = labelColor
const iconColor = computed(() => labelColor.value)
</script>

<style scoped>
</style>


