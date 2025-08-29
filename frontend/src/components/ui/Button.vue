<template>
  <button
    :class="[
      'inline-flex items-center justify-center rounded-lg font-medium transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed',
      sizeClasses,
      variantClasses,
      $attrs.class
    ]"
    :disabled="disabled || loading"
    v-bind="$attrs"
    @click="handleClick"
  >
    <svg
      v-if="loading"
      class="animate-spin -ml-1 mr-2 h-4 w-4"
      fill="none"
      viewBox="0 0 24 24"
    >
      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
    </svg>
    
    <slot />
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  variant?: 'glass' | 'glass-ghost' | 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger' | 'success' | 'warning' | 'indigo' | 'purple' | 'teal'
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl'
  loading?: boolean
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'glass',
  size: 'md',
  loading: false,
  disabled: false
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const sizeClasses = computed(() => {
  // Unified fixed-height system (width grows with content)
  const sizes = {
    xs: 'h-7 px-2 text-xs',      // ~28px
    sm: 'h-8 px-3 text-sm',      // ~32px
    md: 'h-9 px-4 text-sm',      // ~36px (default)
    lg: 'h-11 px-5 text-base',   // ~44px
    xl: 'h-12 px-6 text-lg'      // ~48px
  }
  return sizes[props.size]
})

const variantClasses = computed(() => {
  // Light-mode readability enhancements are encoded here (button-only), dark remains as before
  const variants = {
    glass: 'glass-thin glass-interactive text-gray-900 dark:text-gray-100 hover:shadow-md',
    'glass-ghost': 'glass-ultraThin glass-interactive text-gray-800 dark:text-gray-200 hover:shadow-md',
    primary: 'glass-regular glass-interactive text-white bg-primary-600/30 hover:bg-primary-600/40 focus:ring-primary-500',
    secondary: 'glass-regular glass-interactive text-gray-900 dark:text-gray-100 bg-gray-400/20 hover:bg-gray-400/30 focus:ring-gray-500',
    outline: 'glass-ultraThin glass-interactive border border-white/30 text-gray-800 dark:text-gray-200 hover:bg-white/10 focus:ring-indigo-500',
    ghost: 'glass-ultraThin glass-interactive text-gray-700 dark:text-gray-300 hover:bg-white/10 focus:ring-gray-500',
    danger: 'glass-regular glass-interactive text-white bg-red-600/30 hover:bg-red-600/40 focus:ring-red-500',
    success: 'glass-regular glass-interactive text-white bg-green-600/30 hover:bg-green-600/40 focus:ring-green-500',
    warning: 'glass-regular glass-interactive text-white bg-amber-500/30 hover:bg-amber-500/40 focus:ring-amber-500',
    info: 'glass-regular glass-interactive text-white bg-indigo-600/30 hover:bg-indigo-600/40 focus:ring-indigo-500',
    // Temporary aliases kept for compatibility during migration (will be removed after replacements)
    indigo: 'glass-regular glass-interactive text-white bg-indigo-600/30 hover:bg-indigo-600/40 focus:ring-indigo-500',
    purple: 'glass-regular glass-interactive text-white bg-purple-600/30 hover:bg-purple-600/40 focus:ring-purple-500',
    teal: 'glass-regular glass-interactive text-white bg-teal-600/30 hover:bg-teal-600/40 focus:ring-teal-500',
    // Menu-specific: left-aligned content, higher text contrast in light mode
    menu: 'glass-ultraThin glass-interactive text-gray-800 dark:text-gray-200 hover:bg-white/10 focus:ring-indigo-500 justify-start'
  }
  return variants[props.variant]
})

const handleClick = (event: MouseEvent) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script> 