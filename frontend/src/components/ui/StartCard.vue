<template>
  <component
    :is="rootTag"
    :class="[
      'rounded-2xl border shadow-sm transition-all duration-200 relative z-10 opacity-100 glass-regular glass-interactive',
      tintClass,
      interactiveClass,
      dense ? 'p-4' : 'p-6'
    ]"
    :type="rootTag === 'button' ? 'button' : undefined"
    :disabled="rootTag === 'button' ? disabled : undefined"
    :aria-pressed="clickable ? (active ? 'true' : 'false') : undefined"
    @click="onClick"
  >
    <div class="flex items-start justify-between gap-3 mb-2">
      <div class="min-w-0">
        <div :class="['text-sm truncate', labelColor]">{{ label }}</div>
        <div v-if="description" class="text-xs text-subtle mt-1 line-clamp-2">{{ description }}</div>
      </div>
      <div class="shrink-0 flex items-center gap-2">
        <div v-if="meta != null" class="text-xs text-subtle tabular-nums">{{ meta }}</div>
        <component v-if="icon" :is="icon" :class="['w-5 h-5', iconColor]" />
      </div>
    </div>
    <div v-if="hasValue" :class="['text-3xl font-bold', valueColor]">
      <slot name="value">
        {{ value }}
      </slot>
    </div>
  </component>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue'

interface Props {
  label: string
  value?: string | number
  description?: string
  meta?: string | number | null
  tone?: 'blue' | 'emerald' | 'violet' | 'amber' | 'indigo'
  tint?: 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'info' | null
  icon?: any
  hoverable?: boolean
  dense?: boolean
  clickable?: boolean
  active?: boolean
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  tone: 'blue',
  tint: null,
  hoverable: true,
  dense: false,
  clickable: false,
  active: false,
  disabled: false
})
const emit = defineEmits<{ (e: 'click', event: MouseEvent): void }>()
const slots = useSlots()

// 兼容旧 tone，映射到新主题变体
const resolvedTint = computed(() => {
  if (props.tint) return props.tint
  const map: Record<string, 'primary'|'success'|'accent'|'warning'|'info'> = {
    blue: 'primary',
    emerald: 'success',
    violet: 'accent',
    amber: 'warning',
    indigo: 'info'
  }
  return map[props.tone] || 'primary'
})

const tintClass = computed(() => `glass-tint-${resolvedTint.value}`)

const labelColor = computed(() => `text-[var(--color-${resolvedTint.value}-content)]`)
const valueColor = labelColor
const iconColor = computed(() => labelColor.value)

const rootTag = computed(() => (props.clickable ? 'button' : 'div'))

const hasValue = computed(() => {
  if (slots.value) return true
  return props.value !== undefined && props.value !== null && String(props.value) !== ''
})

const interactiveClass = computed(() => {
  if (props.disabled) return 'opacity-60 cursor-not-allowed'
  if (props.clickable) {
    return props.active
      ? 'cursor-pointer text-left w-full scale-[1.01] shadow-md ring-1 ring-white/25 border-white/30'
      : 'cursor-pointer text-left w-full hover:shadow-md hover:scale-[1.02] hover:bg-white/5'
  }
  return props.hoverable ? 'hover:shadow-md hover:scale-[1.02]' : ''
})

function onClick(event: MouseEvent) {
  if (props.disabled) return
  if (!props.clickable) return
  emit('click', event)
}
</script>

<style scoped>
</style>


