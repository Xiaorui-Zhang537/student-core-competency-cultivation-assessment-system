<template>
  <span
    :class="[
      'inline-flex items-center rounded-full font-medium select-none',
      'glass-ultraThin glass-interactive border',
      sizeClasses,
      variantClasses,
      $attrs.class
    ]"
    v-bind="$attrs"
  >
    <slot />
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  variant?: 'primary' | 'secondary' | 'success' | 'warning' | 'danger' | 'info'
  size?: 'sm' | 'md' | 'lg'
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md'
})

const sizeClasses = computed(() => {
  const sizes = {
    sm: 'px-2 py-0.5 text-xs',
    md: 'px-2.5 py-1 text-sm',
    lg: 'px-3 py-1.5 text-base'
  }
  return sizes[props.size]
})

const variantClasses = computed(() => {
  // 玻璃风格：统一使用超薄玻璃背景，按语义色仅调整文字与边框色
  const variants: Record<NonNullable<Props['variant']>, string> = {
    primary: 'text-primary-700 dark:text-primary-300 border-primary-500/30',
    secondary: 'text-gray-800 dark:text-gray-200 border-white/20 dark:border-white/12',
    success: 'text-emerald-700 dark:text-emerald-300 border-emerald-500/30',
    warning: 'text-amber-800 dark:text-amber-300 border-amber-500/30',
    danger: 'text-rose-700 dark:text-rose-300 border-rose-500/30',
    info: 'text-sky-700 dark:text-sky-300 border-sky-500/30'
  }
  return variants[props.variant]
})
</script> 