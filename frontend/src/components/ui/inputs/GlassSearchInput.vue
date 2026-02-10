<template>
  <div class="relative w-full rounded-full focus-within:ring-[1.5px] focus-within:ring-primary-500/40 focus-within:outline-none">
    <!-- 颜色使用 base-content，避免某些主题下主色过浅导致“看不见图标” -->
    <svg class="pointer-events-none w-4 h-4 absolute left-2 top-1/2 -translate-y-1/2 mt-0.5 text-base-content/60" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l3.817 3.817a.75.75 0 11-1.06 1.06l-3.818-3.816A6 6 0 012 8z" clip-rule="evenodd"/></svg>
    <input
      :id="id"
      type="text"
      class="ui-pill--input ui-pill--pl w-full pl-8"
      :placeholder="placeholder"
      :disabled="disabled"
      :value="modelValue as any"
      @input="onInput"
      :class="[size==='sm' ? 'ui-pill--sm' : 'ui-pill--md', tintClass]"
    />
    <button
      v-if="showClear"
      type="button"
      class="absolute right-2 top-1/2 -translate-y-1/2 w-7 h-7 rounded-full flex items-center justify-center text-gray-500 hover:text-gray-700 dark:text-gray-300 dark:hover:text-gray-100 hover:bg-white/10 focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-500/50"
      :aria-label="clearLabel"
      @click="clear"
    >
      <svg class="w-4 h-4" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
        <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/>
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
defineOptions({ inheritAttrs: false })

interface Props {
  modelValue: string | null
  placeholder?: string
  id?: string
  disabled?: boolean
  size?: 'sm' | 'md'
  tint?: 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'danger' | 'info' | null
  clearable?: boolean
  clearLabel?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '',
  id: undefined,
  disabled: false,
  size: 'md',
  tint: null,
  clearable: true,
  clearLabel: '清空'
})

const emit = defineEmits<{ (e: 'update:modelValue', v: string | null): void }>()

const tintClass = computed(() => props.tint ? `glass-tint-${props.tint}` : '')
const showClear = computed(() => Boolean(props.clearable) && !props.disabled && String(props.modelValue || '').length > 0)

function onInput(e: Event) {
  const target = e.target as HTMLInputElement
  emit('update:modelValue', target.value)
}

function clear() {
  emit('update:modelValue', '')
}
</script>

<style scoped>
</style>


