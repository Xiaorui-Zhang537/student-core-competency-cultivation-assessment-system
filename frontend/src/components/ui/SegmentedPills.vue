<template>
  <div class="inline-flex whitespace-nowrap rounded-2xl overflow-hidden border border-white/30 dark:border-white/10 glass-ultraThin" v-glass="{ strength: 'ultraThin', interactive: false }">
    <button
      v-for="opt in options"
      :key="String(opt.value)"
      type="button"
      :disabled="!!opt.disabled"
      :class="buttonClass(opt.value, !!opt.disabled)"
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
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  options: () => [],
  size: 'md'
})

const emit = defineEmits<{ (e: 'update:modelValue', v: string | number | null): void }>()

const baseSizeClass = computed(() => (props.size === 'sm' ? 'px-3 py-1.5 text-sm' : 'px-3 py-2 text-sm'))

function isActive(val: string | number): boolean {
  return String(val) === String(props.modelValue ?? '')
}

function buttonClass(val: string | number, disabled: boolean): Array<string> {
  const active = isActive(val)
  return [
    baseSizeClass.value,
    'transition-colors focus:outline-none',
    disabled ? 'opacity-60 cursor-not-allowed' : '',
    active ? 'text-white bg-primary-500/30' : 'text-gray-800 dark:text-gray-200 hover:bg-white/10'
  ]
}

function onPick(v: string | number) {
  if (String(v) === String(props.modelValue ?? '')) return
  emit('update:modelValue', v)
}
</script>

<style scoped>
</style>


