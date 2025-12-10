<template>
  <div :class="stacked ? ['flex flex-col gap-1', fullWidth ? 'w-full' : 'w-auto'] : ['inline-flex items-center gap-2', fullWidth ? 'w-full' : 'w-auto']">
    <span v-if="label" class="text-xs text-gray-500 dark:text-gray-400 whitespace-nowrap">{{ label }}</span>
    <div ref="rootRef" :class="['relative', width ? '' : 'w-full']" :style="{ width: width || undefined }">
      <button
        type="button"
        class="ui-pill--select ui-pill--pr-select"
        :class="[size==='sm' ? 'ui-pill--sm ui-pill--pl' : 'ui-pill--md ui-pill--pl', tintClass]"
        :disabled="disabled"
        @click="toggle"
      >
        <span
          :class="truncateLabel ? 'truncate' : ''"
          :style="selectedLabel ? selectedLabelStyle : undefined"
        >
          {{ selectedLabel || placeholder || '' }}
        </span>
        <svg class="pointer-events-none w-4 h-4 text-gray-400 absolute right-2 top-1/2 -translate-y-1/2" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.24 4.5a.75.75 0 01-1.08 0l-4.24-4.5a.75.75 0 01.02-1.06z" clip-rule="evenodd"/></svg>
      </button>
      <!-- local (non-teleport) dropdown -->
      <div v-if="open && !teleport"
           ref="menuRef"
           class="absolute z-[9999] ui-popover-menu p-1 max-h-96 overflow-y-auto overscroll-contain left-0 w-full top-full mt-1"
           :class="tintClass">
        <button v-for="(opt, idx) in options"
                :key="String(opt.value)"
                type="button"
                class="w-full text-left px-3 py-2 rounded transition-colors"
                :class="[
                  idx===activeIndex ? 'bg-white/10 dark:bg-white/10' : 'hover:bg-white/10',
                  isSelected(opt.value) ? 'font-semibold' : 'text-gray-700 dark:text-gray-200'
                ]"
                :style="isSelected(opt.value) ? selectedOptionStyle : undefined"
                :disabled="!!opt.disabled"
                @click="choose(opt.value)"
                @mousemove="activeIndex = idx"
        >
          {{ opt.label }}
        </button>
      </div>
    </div>
  </div>
  <teleport to="body" v-if="teleport">
    <div v-if="open"
         ref="menuRef"
         class="absolute z-[10000] ui-popover-menu p-1 max-h-96 overflow-y-auto overscroll-contain"
         :class="tintClass"
         :style="{ left: pos.left + 'px', top: pos.top + 'px', width: pos.width + 'px' }"
         @keydown.stop.prevent="onKeydown"
    >
      <button v-for="(opt, idx) in options"
              :key="String(opt.value)"
              type="button"
              class="w-full text-left px-3 py-2 rounded transition-colors"
              :class="[
                idx===activeIndex ? 'bg-white/10 dark:bg-white/10' : 'hover:bg-white/10',
                isSelected(opt.value) ? 'font-semibold' : 'text-gray-700 dark:text-gray-200'
              ]"
              :style="isSelected(opt.value) ? selectedOptionStyle : undefined"
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
import { ref, computed, onMounted, onBeforeUnmount, watch, CSSProperties, nextTick } from 'vue'

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
  teleport?: boolean
  truncateLabel?: boolean
  fullWidth?: boolean
  tint?: 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'danger' | 'info' | null
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  disabled: false,
  stacked: false,
  width: undefined,
  teleport: true,
  truncateLabel: true,
  fullWidth: true,
  tint: null
})

const emit = defineEmits<{ (e:'update:modelValue', v:string|number|null):void; (e:'change', v:string|number|null):void; (e:'open'):void; (e:'close'):void }>()

const rootRef = ref<HTMLElement | null>(null)
const open = ref(false)
const pos = ref({ left: 0, top: 0, width: 280 })
const PANEL_MAX_HEIGHT = 420 // px (matches max-h-96)
const VIEWPORT_MARGIN = 8
const MENU_GAP = 6
const activeIndex = ref(0)
let scrollParents: HTMLElement[] = []
const menuRef = ref<HTMLElement | null>(null)

const tintClass = computed(() => props.tint ? `glass-tint-${props.tint}` : '')

const themeColorVar = computed(() => props.tint ? `var(--color-${props.tint})` : 'var(--color-primary)')
const themeColorContentVar = computed(() => props.tint ? `var(--color-${props.tint}-content)` : 'var(--color-primary-content)')

const selectedLabelStyle = computed<CSSProperties>(() => ({
  color: themeColorContentVar.value
}))

const selectedOptionStyle = computed<CSSProperties>(() => ({
  color: themeColorVar.value
}))

const selectedLabel = computed(() => {
  const cur = props.options.find(o => String(o.value) === String(props.modelValue ?? ''))
  return cur?.label
})

function isSelected(v: string | number) {
  return String(v) === String(props.modelValue ?? '')
}

function getPanelHeight(): number {
  if (menuRef.value) {
    const h = menuRef.value.offsetHeight || 0
    if (h > 0) {
      return Math.min(h, PANEL_MAX_HEIGHT)
    }
  }
  const viewportH = window.innerHeight || document.documentElement.clientHeight || 0
  return Math.min(PANEL_MAX_HEIGHT, Math.max(0, viewportH - VIEWPORT_MARGIN * 2))
}

function computePos() {
  const el = rootRef.value
  if (!el) return
  const rect = el.getBoundingClientRect()
  const viewportH = window.innerHeight || document.documentElement.clientHeight || 0
  const viewportW = window.innerWidth || document.documentElement.clientWidth || 0
  const scrollY = window.scrollY || window.pageYOffset || document.documentElement.scrollTop || 0
  const scrollX = window.scrollX || window.pageXOffset || document.documentElement.scrollLeft || 0
  const panelHeight = getPanelHeight()
  const downSpace = Math.max(0, viewportH - rect.bottom - MENU_GAP)
  const upSpace = Math.max(0, rect.top - MENU_GAP)
  let openDown = true
  if (downSpace < panelHeight && upSpace > downSpace) {
    openDown = false
  }
  const desiredLeft = rect.left + scrollX
  const minLeft = scrollX + VIEWPORT_MARGIN
  const maxLeft = Math.max(minLeft, scrollX + viewportW - rect.width - VIEWPORT_MARGIN)
  const left = Math.min(Math.max(desiredLeft, minLeft), maxLeft)
  let top: number
  if (openDown) {
    top = rect.bottom + scrollY + MENU_GAP
    const maxTop = scrollY + viewportH - panelHeight - VIEWPORT_MARGIN
    if (top > maxTop) {
      top = Math.max(scrollY + VIEWPORT_MARGIN, maxTop)
    }
  } else {
    top = rect.top + scrollY - panelHeight - MENU_GAP
    if (top < scrollY + VIEWPORT_MARGIN) {
      top = scrollY + VIEWPORT_MARGIN
    }
  }
  pos.value = { left, top, width: rect.width }
}

function openMenu() {
  computePos()
  open.value = true
  emit('open')
  // set active index to current selected
  const i = props.options.findIndex(o => isSelected(o.value))
  activeIndex.value = i >= 0 ? i : 0
  // attach scroll listeners to scrollable ancestors so popover follows within scrollable containers
  attachScrollParents()
  nextTick(() => computePos())
}

function closeMenu() {
  open.value = false
  emit('close')
  detachScrollParents()
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
  document.addEventListener('keydown', (e) => { if (e.key === 'Escape') closeMenu() })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onWindow)
  window.removeEventListener('scroll', onWindow)
  document.removeEventListener('click', onDocClick)
  detachScrollParents()
})

watch(() => props.modelValue, () => { /* keep */ })

function attachScrollParents() {
  detachScrollParents()
  const el = rootRef.value
  if (!el) return
  const parents: HTMLElement[] = []
  let cur: HTMLElement | null = el as HTMLElement
  while (cur && cur !== document.body) {
    const style = window.getComputedStyle(cur)
    const oy = style.overflowY
    const ox = style.overflowX
    const isScrollable = (v: string) => v === 'auto' || v === 'scroll'
    if (isScrollable(oy) || isScrollable(ox)) {
      parents.push(cur)
    }
    cur = cur.parentElement
  }
  // Also include document.scrollingElement as fallback
  const docEl = (document.scrollingElement || document.documentElement) as HTMLElement
  if (docEl && !parents.includes(docEl)) parents.push(docEl)
  scrollParents = parents
  scrollParents.forEach(p => p.addEventListener('scroll', onWindow, { passive: true }))
}

function detachScrollParents() {
  if (!scrollParents || !scrollParents.length) return
  scrollParents.forEach(p => p.removeEventListener('scroll', onWindow))
  scrollParents = []
}
</script>

<style scoped>
.no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
.no-scrollbar::-webkit-scrollbar { display: none; width: 0; height: 0; }
</style>


