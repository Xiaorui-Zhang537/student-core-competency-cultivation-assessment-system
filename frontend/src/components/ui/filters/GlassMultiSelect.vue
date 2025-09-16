<template>
  <div class="w-full">
    <div class="flex items-center gap-2 mb-1" v-if="label">
      <span class="text-xs text-gray-500 dark:text-gray-400">{{ label }}</span>
      <button v-if="clearable && modelValue.length" type="button" class="text-xs text-primary-600 dark:text-primary-300" @click="$emit('update:modelValue', [])">{{ clearText }}</button>
    </div>
    <div ref="rootRef" class="relative w-full">
      <button type="button" class="input input--glass w-full min-h-[2.5rem] text-left pr-8" @click="toggle">
        <div class="flex flex-wrap gap-1">
          <span v-if="!modelValue.length" class="text-gray-400">{{ placeholder }}</span>
          <span
            v-for="v in displayValues"
            :key="String(v.value)"
            class="px-2 py-0.5 rounded-full text-xs bg-white/30 dark:bg-white/10 border border-white/40 dark:border-white/10 cursor-pointer hover:bg-white/40 dark:hover:bg-white/20"
            @click.stop="toggleValue(v.value)"
            :title="'点击移除: ' + v.label"
          >
            {{ v.label }}
          </span>
        </div>
        <svg class="pointer-events-none w-4 h-4 text-gray-400 absolute right-2 top-1/2 -translate-y-1/2" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.24 4.5a.75.75 0 01-1.08 0l-4.24-4.5a.75.75 0 01.02-1.06z" clip-rule="evenodd"/></svg>
      </button>
    </div>
  </div>
  <teleport to="body">
    <div v-if="open" ref="menuRef" class="fixed z-[9999] popover-glass border border-white/20 dark:border-white/12 shadow-md p-2 max-h-48 overflow-y-auto no-scrollbar" :style="{ left: pos.left+'px', top: pos.top+'px', width: pos.width+'px' }" @click.stop>
      <div class="space-y-1">
        <label v-for="opt in options" :key="String(opt.value)" class="flex items-center gap-2 px-2 py-1 rounded hover:bg-white/10">
          <input type="checkbox" :checked="checkedMap.get(String(opt.value))===true" :disabled="!!opt.disabled" @change="toggleValue(opt.value)" />
          <span class="text-sm">{{ opt.label }}</span>
        </label>
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'

interface Option { label: string; value: string | number; disabled?: boolean }
interface Props {
  modelValue: Array<string | number>
  options: Option[]
  label?: string
  placeholder?: string
  clearable?: boolean
  clearText?: string
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请选择…',
  clearable: true,
  clearText: '清空'
})

const emit = defineEmits<{ (e:'update:modelValue', v:(string|number)[]):void }>()
const rootRef = ref<HTMLElement|null>(null)
const menuRef = ref<HTMLElement|null>(null)
const open = ref(false)
const pos = ref({ left: 0, top: 0, width: 280 })

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
</script>


