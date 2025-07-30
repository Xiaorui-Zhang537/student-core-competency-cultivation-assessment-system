<template>
  <div class="w-full">
    <div v-if="showLabel" class="flex justify-between items-center mb-2">
      <span class="text-sm font-medium text-gray-700 dark:text-gray-300">
        {{ label }}
      </span>
      <span class="text-sm text-gray-500 dark:text-gray-400">
        {{ Math.round(value) }}%
      </span>
    </div>
    
    <div
      :class="[
        'w-full bg-gray-200 rounded-full overflow-hidden dark:bg-gray-700',
        sizeClasses
      ]"
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
  const colors = {
    primary: 'bg-primary-600',
    success: 'bg-green-600',
    warning: 'bg-yellow-600',
    danger: 'bg-red-600'
  }
  return colors[props.color]
})
</script> 