<template>
  <div :class="stacked ? 'flex flex-col gap-1 w-full' : 'inline-flex items-center gap-2 w-full'">
    <span v-if="label" class="text-xs text-gray-500 dark:text-gray-400">{{ label }}</span>
    <div ref="rootRef" :class="['relative', width ? '' : 'w-full']" :style="{ width: width || undefined }">
      <button
        type="button"
        class="input input--glass w-full pr-8 text-left"
        :class="size==='sm' ? 'h-9 text-sm' : 'h-10'"
        :disabled="disabled"
        @click="toggle"
      >
        <span class="truncate">{{ selectedLabel || placeholder || '' }}</span>
        <svg class="pointer-events-none w-4 h-4 text-gray-400 absolute right-2 top-1/2 -translate-y-1/2" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.24 4.5a.75.75 0 01-1.08 0l-4.24-4.5a.75.75 0 01.02-1.06z" clip-rule="evenodd"/></svg>
      </button>
    </div>
  </div>
  <teleport to="body">
    <div v-if="open"
         class="fixed z-[9999] popover-glass border border-white/20 dark:border-white/12 shadow-md p-1 max-h-80 overflow-y-auto overscroll-contain"
         :style="{ left: pos.left + 'px', top: pos.top + 'px', width: pos.width + 'px' }"
         @keydown.stop.prevent="onKeydown"
    >
      <button v-for="(opt, idx) in options"
              :key="String(opt.value)"
              type="button"
              class="w-full text-left px-3 py-2 rounded transition-colors"
              :class="[
                idx===activeIndex ? 'bg-white/10 dark:bg-white/10' : 'hover:bg-white/10',
                isSelected(opt.value) ? 'text-primary-600 dark:text-primary-300' : 'text-gray-700 dark:text-gray-200'
              ]"
              :disabled="!!opt.disabled"
              @click="choose(opt.value)"
              @mousemove="activeIndex = idx"
      >
        {{ opt.label }}
      </button>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'

interface Option { label: string; value: string | number; disabled?: boolean }
interface Props {
  modelValue: string | number | null
  options: Option[]
  placeholder?: string
  label?: string
  size?: 'sm' | 'md'
  disabled?: boolean
  stacked?: boolean
  width?: string
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  disabled: false,
  stacked: false,
  width: undefined
})

const emit = defineEmits<{ (e:'update:modelValue', v:string|number|null):void; (e:'change', v:string|number|null):void; (e:'open'):void; (e:'close'):void }>()

const rootRef = ref<HTMLElement | null>(null)
const open = ref(false)
const pos = ref({ left: 0, top: 0, width: 280 })
const PANEL_MAX_HEIGHT = 320 // px (matches max-h-80)
const activeIndex = ref(0)

const selectedLabel = computed(() => {
  const cur = props.options.find(o => String(o.value) === String(props.modelValue ?? ''))
  return cur?.label
})

function isSelected(v: string | number) {
  return String(v) === String(props.modelValue ?? '')
}

function computePos() {
  const el = rootRef.value
  if (!el) return
  const rect = el.getBoundingClientRect()
  const viewportH = window.innerHeight || document.documentElement.clientHeight || 0
  // Prefer opening downward; if not enough room (below), place upward with a safe margin
  const downTop = rect.bottom + 6
  const upTop = Math.max(8, rect.top - PANEL_MAX_HEIGHT - 6)
  const top = (downTop + PANEL_MAX_HEIGHT + 8 <= viewportH) ? downTop : upTop
  pos.value = { left: rect.left, top, width: rect.width }
}

function openMenu() {
  computePos()
  open.value = true
  emit('open')
  // set active index to current selected
  const i = props.options.findIndex(o => isSelected(o.value))
  activeIndex.value = i >= 0 ? i : 0
}

function closeMenu() {
  open.value = false
  emit('close')
}

function toggle() {
  if (open.value) {
    closeMenu();
  } else {
    openMenu();
  }
}

function choose(v: string | number) {
  emit('update:modelValue', v)
  emit('change', v)
  closeMenu()
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') return closeMenu()
  if (e.key === 'ArrowDown') { activeIndex.value = Math.min(props.options.length - 1, activeIndex.value + 1); return }
  if (e.key === 'ArrowUp') { activeIndex.value = Math.max(0, activeIndex.value - 1); return }
  if (e.key === 'Enter') {
    const opt = props.options[activeIndex.value]
    if (opt && !opt.disabled) choose(opt.value)
  }
}

function onWindow(ev: Event) {
  if (!open.value) return
  if (ev.type === 'resize' || ev.type === 'scroll') computePos()
}

function onDocClick(e: MouseEvent) {
  if (!open.value) return
  const root = rootRef.value
  const t = e.target as Node
  if (root && !root.contains(t)) closeMenu()
}

onMounted(() => {
  window.addEventListener('resize', onWindow, { passive: true })
  window.addEventListener('scroll', onWindow, { passive: true })
  document.addEventListener('click', onDocClick)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onWindow)
  window.removeEventListener('scroll', onWindow)
  document.removeEventListener('click', onDocClick)
})

watch(() => props.modelValue, () => { /* keep */ })
</script>


