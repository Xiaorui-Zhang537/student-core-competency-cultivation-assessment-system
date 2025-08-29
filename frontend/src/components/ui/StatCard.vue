<template>
  <div
    :class="[
      'rounded-2xl border bg-gradient-to-br shadow-sm transition-all duration-200',
      toneClasses,
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
  tone?: 'blue' | 'emerald' | 'violet' | 'amber'
  icon?: any
  hoverable?: boolean
  dense?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  tone: 'blue',
  hoverable: true,
  dense: false
})

const toneClasses = computed(() => {
  const map: Record<string, string> = {
    blue: 'from-blue-50 to-blue-100 dark:from-blue-900/20 dark:to-blue-800/20 border-blue-100 dark:border-blue-800',
    emerald: 'from-emerald-50 to-emerald-100 dark:from-emerald-900/20 dark:to-emerald-800/20 border-emerald-100 dark:border-emerald-800',
    violet: 'from-violet-50 to-violet-100 dark:from-violet-900/20 dark:to-violet-800/20 border-violet-100 dark:border-violet-800',
    amber: 'from-amber-50 to-amber-100 dark:from-amber-900/20 dark:to-amber-800/20 border-amber-100 dark:border-amber-800'
  }
  return map[props.tone] || map.blue
})

const labelColor = computed(() => {
  const map: Record<string, string> = {
    blue: 'text-blue-700 dark:text-blue-300',
    emerald: 'text-emerald-700 dark:text-emerald-300',
    violet: 'text-violet-700 dark:text-violet-300',
    amber: 'text-amber-700 dark:text-amber-300'
  }
  return map[props.tone] || map.blue
})

const valueColor = labelColor
const iconColor = computed(() => labelColor.value.replace('text-', 'text-'))
</script>


