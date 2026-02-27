<template>
  <div class="w-full">
    <div class="flex items-center gap-2 mb-1" v-if="label">
      <span class="text-xs text-gray-500 dark:text-gray-400">{{ label }}</span>
      <button v-if="clearable && modelValue.length" type="button" class="text-xs text-primary-600 dark:text-primary-300" @click="$emit('update:modelValue', [])">{{ clearText }}</button>
    </div>
    <div ref="rootRef" class="relative w-full">
      <Button type="button" :class="['ui-pill--select','ui-pill--pl','ui-pill--pr-select', size==='sm' ? 'ui-pill--sm' : 'ui-pill--md']" @click="toggle">
        <div class="flex flex-wrap items-center gap-1">
          <span v-if="!modelValue.length" class="text-gray-400">{{ placeholder }}</span>
          <span
            v-for="v in displayValues"
            :key="String(v.value)"
            class="ui-chip"
            :style="chipStyle"
            @click.stop="toggleValue(v.value)"
            :title="'点击移除: ' + v.label"
          >
            {{ v.label }}
          </span>
        </div>
        <svg class="pointer-events-none w-4 h-4 text-gray-400 absolute right-3 top-1/2 -translate-y-1/2" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.24 4.5a.75.75 0 01-1.08 0l-4.24-4.5a.75.75 0 01.02-1.06z" clip-rule="evenodd"/></svg>
      </Button>
    </div>
  </div>
  <teleport to="body">
    <div v-if="open" ref="menuRef" class="fixed z-[9999] ui-popover-menu p-2 max-h-48 overflow-y-auto" :class="tintClass" :style="{ left: pos.left+'px', top: pos.top+'px', width: pos.width+'px' }" @click.stop>
      <div class="space-y-1">
        <label v-for="opt in options" :key="String(opt.value)" class="flex items-center gap-2 px-2 py-1 rounded transition-colors hover:bg-white/10">
          <input
            type="checkbox"
            :checked="isChecked(opt.value)"
            :disabled="!!opt.disabled"
            @change="toggleValue(opt.value)"
            class="sr-only"
          />
          <span
            :class="['inline-flex h-5 w-5 items-center justify-center rounded-md border transition-all duration-150']"
            :style="checkboxBoxStyle(opt.value)"
          >
            <svg v-if="isChecked(opt.value)" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="h-3.5 w-3.5">
              <path fill-rule="evenodd" d="M16.704 5.29a1 1 0 00-1.408-1.42l-6.29 6.24-2.302-2.286a1 1 0 10-1.404 1.424l3.001 2.98a1 1 0 001.408-.004l6.995-6.934z" clip-rule="evenodd" />
            </svg>
          </span>
          <span class="text-sm" :style="isChecked(opt.value) ? selectedOptionStyle : undefined">{{ opt.label }}</span>
        </label>
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount, type CSSProperties } from 'vue'
import Button from '@/components/ui/Button.vue'

interface Option { label: string; value: string | number; disabled?: boolean }
interface Props {
  modelValue: Array<string | number>
  options: Option[]
  label?: string
  placeholder?: string
  clearable?: boolean
  clearText?: string
  size?: 'sm' | 'md'
  tint?: 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'danger' | 'info' | null
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请选择…',
  clearable: true,
  clearText: '清空',
  size: 'sm',
  tint: null
})

const emit = defineEmits<{ (e:'update:modelValue', v:(string|number)[]):void }>()
const rootRef = ref<HTMLElement|null>(null)
const menuRef = ref<HTMLElement|null>(null)
const open = ref(false)
const pos = ref({ left: 0, top: 0, width: 280 })

const tintClass = computed(() => props.tint ? `glass-tint-${props.tint}` : '')
const themeColorVar = computed(() => props.tint ? `var(--color-${props.tint})` : 'var(--color-primary)')
const themeColorContentVar = computed(() => props.tint ? `var(--color-${props.tint}-content)` : 'var(--color-primary-content)')
const selectedOptionStyle = computed<CSSProperties>(() => ({ color: themeColorVar.value }))
const chipStyle = computed<CSSProperties>(() => ({
  color: themeColorContentVar.value,
  background: `color-mix(in oklab, ${themeColorVar.value} 22%, transparent)`,
  borderColor: `color-mix(in oklab, ${themeColorVar.value} 36%, transparent)`
}))
const checkboxAccent = computed(() => themeColorVar.value)

const checkedMap = computed(() => {
  const m = new Map<string, boolean>()
  for (const v of props.modelValue) m.set(String(v), true)
  return m
})

const displayValues = computed(() => {
  return props.modelValue.map(v => ({ value: v, label: props.options.find(o => String(o.value)===String(v))?.label || String(v) }))
})

function computePos() {
  const el = rootRef.value
  if (!el) return
  const rect = el.getBoundingClientRect()
  pos.value = { left: rect.left, top: rect.bottom + 6, width: rect.width }
}

function toggle() {
  if (open.value) { open.value = false } else { computePos(); open.value = true }
}

function toggleValue(v: string | number) {
  const s = new Set(props.modelValue.map(x => String(x)))
  const key = String(v)
  if (s.has(key)) {
    s.delete(key)
  } else {
    s.add(key)
  }
  emit('update:modelValue', Array.from(s).map(x => isNaN(Number(x)) ? x : Number(x)))
}

function onWindow(ev: Event) { if (!open.value) return; if (ev.type==='resize'||ev.type==='scroll') computePos() }
function onDocClick(e: MouseEvent) {
  if (!open.value) return
  const root = rootRef.value
  const menu = menuRef.value
  const t = e.target as Node
  const insideRoot = !!(root && root.contains(t))
  const insideMenu = !!(menu && menu.contains(t))
  if (!insideRoot && !insideMenu) open.value = false
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') open.value = false
}

const isChecked = (val: string | number) => checkedMap.value.get(String(val)) === true

const checkboxBoxStyle = (val: string | number): CSSProperties => {
  const accent = checkboxAccent.value
  if (isChecked(val)) {
    return {
      background: `color-mix(in oklab, ${accent} 28%, transparent)` ,
      borderColor: `color-mix(in oklab, ${accent} 48%, transparent)` ,
      color: themeColorContentVar.value,
      boxShadow: `0 0 0 1px color-mix(in oklab, ${accent} 42%, transparent) inset`
    }
  }
  return {
    background: `color-mix(in oklab, ${accent} 8%, transparent)`,
    borderColor: `color-mix(in oklab, ${accent} 24%, transparent)`
  }
}

onMounted(() => {
  window.addEventListener('resize', onWindow, { passive: true })
  window.addEventListener('scroll', onWindow, { passive: true })
  document.addEventListener('click', onDocClick)
  document.addEventListener('keydown', onKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onWindow)
  window.removeEventListener('scroll', onWindow)
  document.removeEventListener('click', onDocClick)
  document.removeEventListener('keydown', onKeydown)
})
</script>


