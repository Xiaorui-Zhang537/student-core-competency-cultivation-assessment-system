<template>
  <div class="w-full">
    <div v-if="showLabel" class="flex justify-between items-center mb-2">
      <span class="text-sm font-medium text-strong">
        {{ label }}
      </span>
      <span class="text-sm text-muted">
        {{ Math.round(value) }}%
      </span>
    </div>
    
    <div
      :class="[
        'w-full rounded-full overflow-hidden',
        sizeClasses
      ]"
      :style="{ backgroundColor: 'color-mix(in oklab, var(--color-base-content) 18%, transparent)' }"
    >
      <div
        :class="[
          'h-full transition-all duration-300 ease-out rounded-full',
          colorClasses
        ]"
        :style="{ width: `${Math.min(100, Math.max(0, value))}%` }"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  value: number
  label?: string
  showLabel?: boolean
  size?: 'sm' | 'md' | 'lg'
  color?: 'primary' | 'success' | 'warning' | 'danger'
}

const props = withDefaults(defineProps<Props>(), {
  value: 0,
  showLabel: false,
  size: 'md',
  color: 'primary'
})

const sizeClasses = computed(() => {
  const sizes = {
    sm: 'h-1',
    md: 'h-2',
    lg: 'h-3'
  }
  return sizes[props.size]
})

const colorClasses = computed(() => {
  const colors: Record<NonNullable<Props['color']>, string> = {
    primary: 'bg-[color-mix(in_oklab,var(--color-primary)_70%,transparent)]',
    success: 'bg-[color-mix(in_oklab,var(--color-success)_70%,transparent)]',
    warning: 'bg-[color-mix(in_oklab,var(--color-warning)_70%,transparent)]',
    danger: 'bg-[color-mix(in_oklab,var(--color-error)_70%,transparent)]'
  }
  return colors[props.color]
})
</script> 