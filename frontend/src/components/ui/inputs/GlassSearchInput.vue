<template>
  <div class="search-wrap">
    <input
      :id="id"
      type="text"
      class="ui-pill--input w-full search-field"
      :placeholder="placeholder"
      :disabled="disabled"
      :value="modelValue as any"
      @input="onInput"
      :class="[size==='sm' ? 'ui-pill--sm' : 'ui-pill--md', tintClass]"
    />
    <!-- 搜索图标：渲染在 input 之后，用 z-index 保证始终可见 -->
    <svg class="search-icon" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true"><path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l3.817 3.817a.75.75 0 11-1.06 1.06l-3.818-3.816A6 6 0 012 8z" clip-rule="evenodd"/></svg>
    <button
      v-if="showClear"
      type="button"
      class="search-clear"
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
.search-wrap {
  position: relative;
  width: 100%;
  border-radius: 9999px;
}
.search-wrap:focus-within {
  box-shadow: 0 0 0 1.5px rgba(59, 130, 246, 0.4);
}

/* 输入框左侧留出图标空间 */
.search-field {
  padding-left: 2.25rem !important;
}

/* 搜索图标：absolute 定位 + 高 z-index 保证始终在 input 背景之上 */
.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  width: 1rem;
  height: 1rem;
  pointer-events: none;
  z-index: 10;
  color: var(--color-base-content, #666);
  opacity: 0.5;
}

/* 清除按钮 */
.search-clear {
  position: absolute;
  right: 0.5rem;
  top: 50%;
  transform: translateY(-50%);
  width: 1.75rem;
  height: 1.75rem;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-base-content, #999);
  opacity: 0.6;
  z-index: 10;
  background: transparent;
  border: none;
  cursor: pointer;
}
.search-clear:hover {
  opacity: 1;
  background: rgba(255, 255, 255, 0.1);
}
</style>
