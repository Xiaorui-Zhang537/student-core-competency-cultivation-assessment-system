<template>
  <div :class="stacked ? 'flex flex-col gap-1 w-full' : 'inline-flex items-center gap-2 w-full'">
    <span v-if="label" class="text-xs text-gray-500 dark:text-gray-400">{{ label }}</span>
    <div class="relative w-full">
      <liquid-glass class="rounded-full" :container-class="containerClass">
        <select
          class="ui-pill--select ui-pill--pl ui-pill--pr-select bg-transparent"
          :class="size==='sm' ? 'ui-pill--sm' : 'ui-pill--md'"
          :value="modelValue ?? ''"
          :disabled="disabled"
          @change="onChange"
          v-bind="$attrs"
        >
          <option v-if="placeholder" disabled value="">{{ placeholder }}</option>
          <option v-for="opt in options" :key="String(opt.value)" :value="opt.value" :disabled="!!opt.disabled">
            {{ opt.label }}
          </option>
        </select>
      </liquid-glass>
      <svg class="pointer-events-none w-4 h-4 text-gray-400 absolute right-3 top-1/2 -translate-y-1/2" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.24 4.5a.75.75 0 01-1.08 0l-4.24-4.5a.75.75 0 01.02-1.06z" clip-rule="evenodd"/></svg>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'

interface Option { label: string; value: string | number; disabled?: boolean }
interface Props {
  modelValue: string | number | null
  options: Option[]
  placeholder?: string
  label?: string
  size?: 'sm' | 'md'
  disabled?: boolean
  stacked?: boolean
  tint?: 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'danger' | 'info' | null
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  disabled: false,
  stacked: false,
  tint: null
})

const emit = defineEmits<{ (e:'update:modelValue', v:string|number|null):void; (e:'change', v:string|number|null):void }>()

const tintClass = computed(() => props.tint ? `glass-tint-${props.tint}` : '')

const containerClass = computed(() => ['glass-regular','glass-interactive', tintClass.value].filter(Boolean).join(' '))

function onChange(e: Event) {
  const target = e.target as HTMLSelectElement
  const v = target.value === '' ? null : (isNaN(Number(target.value)) ? target.value : Number(target.value))
  emit('update:modelValue', v)
  emit('change', v)
}
 

</script>


