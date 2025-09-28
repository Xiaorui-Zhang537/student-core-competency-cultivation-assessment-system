<template>
  <div class="inline-flex whitespace-nowrap rounded-2xl overflow-hidden border border-white/30 dark:border-white/10 glass-ultraThin" v-glass="{ strength: 'ultraThin', interactive: false }">
    <button
      v-for="opt in options"
      :key="String(opt.value)"
      type="button"
      :disabled="!!opt.disabled"
      :class="buttonClass(opt.value, !!opt.disabled)"
      :style="isActive(opt.value) ? activeStyle : undefined"
      @click="onPick(opt.value)"
    >
      {{ opt.label }}
    </button>
  </div>
  </template>

<script setup lang="ts">
import { computed } from 'vue'

interface OptionItem { label: string; value: string | number; disabled?: boolean }
interface Props {
  modelValue: string | number | null
  options: OptionItem[]
  size?: 'sm' | 'md'
  variant?: 'primary'|'secondary'|'accent'|'success'|'warning'|'danger'|'info'|'neutral'
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  options: () => [],
  size: 'md',
  variant: 'primary'
})

const emit = defineEmits<{ (e: 'update:modelValue', v: string | number | null): void }>()

const baseSizeClass = computed(() => (props.size === 'sm' ? 'px-3 py-1.5 text-sm' : 'px-3 py-2 text-sm'))

const activeStyle = computed(() => {
  const v = String(props.variant || 'primary')
  const colorVar = {
    primary: '--color-primary',
    secondary: '--color-secondary',
    accent: '--color-accent',
    success: '--color-success',
    warning: '--color-warning',
    danger: '--color-error',
    info: '--color-info',
    neutral: '--color-base-content'
  }[v] || '--color-primary'
  const contentVar = {
    primary: '--color-primary-content',
    secondary: '--color-secondary-content',
    accent: '--color-accent-content',
    success: '--color-success-content',
    warning: '--color-warning-content',
    danger: '--color-error-content',
    info: '--color-info-content',
    neutral: '--color-base-content'
  }[v] || '--color-primary-content'
  return {
    backgroundColor: `color-mix(in oklab, var(${colorVar}) 22%, transparent)`,
    color: `var(${contentVar})`,
    boxShadow: `0 0 0 1px color-mix(in oklab, var(${colorVar}) 36%, transparent) inset`
  } as Record<string, string>
})

function isActive(val: string | number): boolean {
  return String(val) === String(props.modelValue ?? '')
}

function buttonClass(val: string | number, disabled: boolean): Array<string> {
  const active = isActive(val)
  return [
    baseSizeClass.value,
    'transition-colors focus:outline-none',
    disabled ? 'opacity-60 cursor-not-allowed' : '',
    active ? 'text-base-content' : 'text-gray-800 dark:text-gray-200 hover:bg-white/10'
  ]
}

function onPick(v: string | number) {
  if (String(v) === String(props.modelValue ?? '')) return
  emit('update:modelValue', v)
}
</script>

<style scoped>
</style>


