<template>
  <div class="grid grid-cols-2 gap-3">
    <button
      v-for="opt in options"
      :key="String(opt.value)"
      type="button"
      class="text-left rounded-2xl p-3 border transition-all duration-150"
      :class="buttonClass(opt)"
      :disabled="!!opt.disabled"
      :aria-pressed="isActive(opt.value)"
      @click="onPick(opt.value)"
    >
      <div class="flex items-start justify-between gap-3">
        <div class="min-w-0">
          <div class="flex items-center gap-2 min-w-0">
            <component v-if="opt.icon" :is="opt.icon" class="w-5 h-5 shrink-0 opacity-90" />
            <div class="font-semibold truncate">{{ opt.label }}</div>
          </div>
          <div v-if="opt.description" class="text-xs text-subtle mt-1 line-clamp-2">{{ opt.description }}</div>
        </div>
        <div v-if="opt.count != null" class="shrink-0 text-right">
          <div class="text-xs text-subtle">{{ countLabel }}</div>
          <div class="text-lg font-semibold tabular-nums">{{ opt.count }}</div>
        </div>
      </div>
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type TabOption = {
  value: string
  label: string
  description?: string
  icon?: any
  count?: number | string | null
  disabled?: boolean
}

const props = withDefaults(defineProps<{
  modelValue: string
  options: TabOption[]
  countLabel?: string
}>(), {
  modelValue: '',
  options: () => [],
  countLabel: 'Count',
})

const emit = defineEmits<{ (e: 'update:modelValue', v: string): void }>()

const countLabel = computed(() => String(props.countLabel || 'Count'))

function isActive(v: string) {
  return String(v) === String(props.modelValue || '')
}

function onPick(v: string) {
  if (String(v) === String(props.modelValue || '')) return
  const opt = props.options.find(o => String(o.value) === String(v))
  if (opt?.disabled) return
  emit('update:modelValue', String(v))
}

function buttonClass(opt: TabOption) {
  const active = isActive(String(opt.value))
  return [
    opt.disabled ? 'opacity-60 cursor-not-allowed' : 'hover:bg-white/5',
    active
      ? 'border-[color-mix(in_oklab,var(--color-primary)_32%,transparent)] bg-[color-mix(in_oklab,var(--color-primary)_12%,transparent)] ring-1 ring-[color-mix(in_oklab,var(--color-primary)_26%,transparent)]'
      : 'border-white/15 bg-white/0',
  ]
}
</script>

