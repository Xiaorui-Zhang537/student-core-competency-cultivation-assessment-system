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
    primary: 'glass-tint-primary text-[var(--color-primary-content)]',
    secondary: 'glass-tint-secondary text-[var(--color-secondary-content)]',
    success: 'glass-tint-success text-[var(--color-success-content)]',
    warning: 'glass-tint-warning text-[var(--color-warning-content)]',
    danger: 'glass-tint-error text-[var(--color-error-content)]',
    info: 'glass-tint-info text-[var(--color-info-content)]'
  }
  return variants[props.variant]
})
</script> 