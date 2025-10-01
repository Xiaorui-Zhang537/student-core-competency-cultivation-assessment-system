<template>
  <div class="w-full">
    <label v-if="label" class="block text-sm font-medium mb-1">{{ label }}</label>
    <div class="relative">
      <button
        :id="idAttr"
        ref="anchor"
        type="button"
        class="ui-pill--select ui-pill--pr-select w-full"
        :class="[size==='sm' ? 'ui-pill--sm ui-pill--pl' : 'ui-pill--md ui-pill--pl', tintClass]"
        @click="toggle"
      >
        <span class="truncate" :class="valueText ? 'text-gray-900 dark:text-gray-100' : 'text-gray-400'" :style="valueText ? selectedLabelStyle : undefined">
          {{ valueText || placeholderText }}
        </span>
        <svg class="w-4 h-4 absolute right-2 top-1/2 -translate-y-1/2 text-gray-400" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.24 4.5a.75.75 0 01-1.08 0l-4.24-4.5a.75.75 0 01.02-1.06z" clip-rule="evenodd"/></svg>
      </button>

      <teleport to="body">
        <div v-if="open" class="fixed inset-0 z-[999]" @click="close"></div>
        <div v-if="open" class="fixed z-[1000] popover-glass glass-regular glass-interactive rounded-2xl border border-white/20 dark:border-white/12 shadow-xl no-scrollbar overflow-visible"
             :class="tintClass"
             :style="panelStyle" v-glass="{ strength: 'regular', interactive: true }" @click.stop>
          <!-- Header: month switcher -->
          <div class="flex items-center justify-between px-4 pt-3 pb-2">
            <button class="nav-btn" @click="prevMonth">‹</button>
            <div class="text-sm font-medium">{{ panelYear }}-{{ pad(panelMonth+1) }}</div>
            <button class="nav-btn" @click="nextMonth">›</button>
          </div>
          <!-- Calendar grid -->
          <div class="px-3 pb-2 no-scrollbar">
            <div class="grid grid-cols-7 text-[11px] text-gray-500 dark:text-gray-400 mb-1">
              <div v-for="w in weekdays" :key="w" class="text-center">{{ w }}</div>
            </div>
            <div class="grid grid-cols-7 gap-1">
              <button v-for="cell in calendarCells" :key="cell.key" type="button"
                      class="day-btn"
                      :class="dayClasses(cell)"
                      @click="pickDate(cell)"
              >{{ cell.day }}</button>
            </div>
          </div>
          <!-- Time selectors -->
          <div v-if="!dateOnly" class="px-4 py-2 border-t border-white/10 flex items-center gap-3">
            <div class="flex items-center gap-2">
              <span class="text-xs text-gray-500 dark:text-gray-400">{{ t('shared.time') || '时间' }}</span>
              <div class="w-20">
                <GlassPopoverSelect :options="hourOptions" :model-value="hour" @update:modelValue="(v:any)=>hour = Number(v)" size="sm" :tint="tint" />
              </div>
              <span class="text-xs text-gray-500">:</span>
              <div class="w-20">
                <GlassPopoverSelect :options="minuteSelectOptions" :model-value="minute" @update:modelValue="(v:any)=>minute = Number(v)" size="sm" :tint="tint" />
              </div>
            </div>
            <div class="ml-auto flex items-center gap-2">
              <button class="chip" @click="setNow">{{ t('shared.now') || '此刻' }}</button>
              <button class="chip" @click="clearValue">{{ t('shared.clear') || '清除' }}</button>
              <button class="btn-confirm" @click="confirm">{{ t('shared.confirm') || '确定' }}</button>
            </div>
          </div>
        </div>
      </teleport>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, type CSSProperties } from 'vue'
// @ts-ignore
import { useI18n } from 'vue-i18n'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'

interface Props {
  modelValue?: string
  label?: string
  id?: string
  hint?: string
  minuteStep?: number
  dateOnly?: boolean
  size?: 'sm' | 'md'
  tint?: 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'danger' | 'info' | null
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  minuteStep: 5,
  dateOnly: false,
  size: 'sm',
  tint: null,
})
const emit = defineEmits<{ (e:'update:modelValue', v:string): void }>()
const { t } = useI18n()

const anchor = ref<HTMLElement | null>(null)
const open = ref(false)
const positionTick = ref(0)

const tintClass = computed(() => props.tint ? `glass-tint-${props.tint}` : '')
const themeColorVar = computed(() => props.tint ? `var(--color-${props.tint})` : 'var(--color-primary)')
const themeColorContentVar = computed(() => props.tint ? `var(--color-${props.tint}-content)` : 'var(--color-primary-content)')
const selectedLabelStyle = computed<CSSProperties>(() => ({ color: themeColorContentVar.value }))

const internal = ref<Date | null>(parseFromModel(props.modelValue))
watch(() => props.modelValue, v => internal.value = parseFromModel(v))

const panelYear = ref<number>((internal.value || new Date()).getFullYear())
const panelMonth = ref<number>((internal.value || new Date()).getMonth())
const hour = ref<number>((internal.value || new Date()).getHours())
const minute = ref<number>(roundToStep((internal.value || new Date()).getMinutes(), props.minuteStep))

const weekdays = ['日','一','二','三','四','五','六']

const valueText = computed(() => formatDisplay(internal.value))
const placeholderText = computed(() => (props.dateOnly ? (t('shared.date') as string || '选择日期') : (t('shared.datetime.hint') as string || '选择日期时间')))

const minuteSelectOptions = computed(() => {
  const arr:number[] = []
  for (let m=0; m<60; m+=props.minuteStep) arr.push(m)
  return arr.map(m => ({ label: pad(m), value: m }))
})

const hourOptions = computed(() => Array.from({length:24}, (_,i) => ({ label: pad(i), value: i })))

const panelStyle = computed(() => {
  // 依赖 open 与 positionTick，确保在打开/滚动/缩放时重新计算定位
  void open.value
  void positionTick.value
  return positionPanel()
})

function toggle() { open.value = !open.value; if (open.value) nudge() }
function close() { open.value = false }

function nudge() { positionTick.value++ }

function pad(n:number) { return String(n).padStart(2,'0') }

function roundToStep(m:number, step:number) { return Math.round(m/step)*step % 60 }

function parseFromModel(v?: string | null): Date | null {
  if (!v) return null
  try {
    if (v.includes('T')) return new Date(v)
    if (v.includes(' ')) return new Date(v.replace(' ', 'T'))
    return new Date(v)
  } catch { return null }
}

function formatModel(d: Date | null): string {
  if (!d) return ''
  if (props.dateOnly) {
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${y}-${m}-${day}`
  }
  const dt = new Date(d.getTime() - d.getTimezoneOffset()*60000)
  return dt.toISOString().slice(0,16) // YYYY-MM-DDTHH:mm
}

function formatDisplay(d: Date | null): string {
  if (!d) return ''
  return props.dateOnly ? d.toLocaleDateString() : d.toLocaleString()
}

function startOfMonth(y:number, m:number) { return new Date(y, m, 1) }
function endOfMonth(y:number, m:number) { return new Date(y, m+1, 0) }

const calendarCells = computed(() => {
  const y = panelYear.value, m = panelMonth.value
  const start = startOfMonth(y,m)
  const end = endOfMonth(y,m)
  const firstWeekDay = start.getDay()
  const daysInMonth = end.getDate()
  const cells: any[] = []
  // leading blanks
  for (let i=0;i<firstWeekDay;i++) {
    const d = new Date(y,m, i-firstWeekDay+1)
    cells.push({ key:`p${i}`, day: d.getDate(), date:d, current:false })
  }
  for (let d=1; d<=daysInMonth; d++) {
    cells.push({ key:`c${d}`, day:d, date: new Date(y,m,d), current:true })
  }
  const tail = 42 - cells.length
  for (let i=0; i<tail; i++) {
    const d = new Date(y,m+1, i+1)
    cells.push({ key:`n${i}`, day:d.getDate(), date:d, current:false })
  }
  return cells
})

function dayClasses(cell:any) {
  const classes = ['text-sm','py-1.5','rounded','transition','hover:bg-white/10','text-gray-900','dark:text-gray-100']
  if (!cell.current) classes.push('text-gray-400')
  const sameDay = internal.value && sameDate(internal.value, cell.date)
  if (sameDay) classes.push('bg-white/15')
  return classes.join(' ')
}

function sameDate(a:Date|null, b:Date|null) {
  if (!a||!b) return false
  return a.getFullYear()===b.getFullYear() && a.getMonth()===b.getMonth() && a.getDate()===b.getDate()
}

function pickDate(cell:any) {
  const base = new Date(cell.date)
  if (props.dateOnly) {
    base.setHours(0, 0, 0, 0)
  } else {
    base.setHours(hour.value, minute.value, 0, 0)
  }
  internal.value = base
  // 仅日期模式：点击即选并关闭，无需确认按钮
  if (props.dateOnly) {
    emit('update:modelValue', formatModel(internal.value))
    close()
  }
}

function prevMonth(){
  if (panelMonth.value===0){ panelMonth.value=11; panelYear.value-=1 } else panelMonth.value-=1
}
function nextMonth(){
  if (panelMonth.value===11){ panelMonth.value=0; panelYear.value+=1 } else panelMonth.value+=1
}

function setNow(){
  const n = new Date()
  hour.value = n.getHours(); minute.value = roundToStep(n.getMinutes(), props.minuteStep)
  internal.value = new Date(n.getFullYear(), n.getMonth(), n.getDate(), hour.value, minute.value, 0, 0)
}
function clearValue(){ internal.value = null; emit('update:modelValue',''); close() }
function confirm(){ emit('update:modelValue', formatModel(internal.value)); close() }

function positionPanel(){
  try {
    const el = anchor.value as HTMLElement
    const rect = el?.getBoundingClientRect()
    if (!rect) return { left:'20px', top:'20px', width:'360px' }
    // 弹窗宽度略大于输入框但限制最大值，避免在窄表单中过宽
    const widthBaseline = Math.max(rect.width + 60, 320)
    const width = Math.min(440, Math.max(widthBaseline, rect.width))
    const left = Math.min(window.innerWidth - width - 8, Math.max(8, rect.left))
    // 允许弹层随滚动：fixed + rect基于视口，无需加 scrollY
    const desiredHeight = 340
    const hasSpaceBelow = rect.bottom + 6 + desiredHeight <= window.innerHeight
    const top = hasSpaceBelow ? (rect.bottom + 6) : Math.max(8, rect.top - desiredHeight - 6)
    return { left: left+'px', top: top+'px', width: width+'px' }
  } catch { return { left:'20px', top:'20px', width:'360px' } }
}

const idAttr = computed(() => props.id || `gdtp-${Math.random().toString(36).slice(2,8)}`)

onMounted(() => {
  anchor.value = document.getElementById(idAttr.value)
  const onResize = () => { if (open.value) nudge() }
  const onScroll = () => { if (open.value) nudge() }
  window.addEventListener('resize', onResize)
  window.addEventListener('scroll', onScroll, { passive: true, capture: true })
  // 记录移除（简单做法）：在组件卸载前移除侦听
  cleanupFns.push(() => {
    window.removeEventListener('resize', onResize)
    window.removeEventListener('scroll', onScroll, { capture: true } as any)
  })
})

const cleanupFns: Array<() => void> = []

// 保持页面滚动不被锁定；仅在选择器内部控制滚动条样式

// 当用户调整小时/分钟时，实时同步到内部日期并更新展示
watch([hour, minute], ([h, m]) => {
  const base = internal.value ? new Date(internal.value) : new Date()
  base.setHours(Number(h || 0), Number(m || 0), 0, 0)
  internal.value = base
})
</script>

<style scoped>
.picker-input {
  position: relative;
  display: block;
  width: 100%;
  border: 1px solid rgba(255,255,255,0.35);
  background: linear-gradient(180deg, rgba(255,255,255,0.28), rgba(255,255,255,0.12));
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: var(--color-base-content);
  border-radius: 9999px;
  padding: 8px 28px 8px 12px;
  text-align: left;
}
@media (prefers-color-scheme: dark) {
  .picker-input { color: var(--color-base-content); border-color: rgba(255,255,255,0.25); }
}
.nav-btn {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: rgba(255,255,255,0.10);
  color: var(--color-base-content);
}
.nav-btn:hover { background: rgba(255,255,255,0.20); }
@media (prefers-color-scheme: dark) {
  .nav-btn { color: var(--color-base-content); }
}

.day-btn {
  text-align: center;
  border-radius: 8px;
  color: var(--color-base-content);
}
.day-btn:hover { background: rgba(255,255,255,0.10); }
@media (prefers-color-scheme: dark) {
  .day-btn { color: var(--color-base-content); }
}

/* 强制覆盖父级可能设置的浅色字体：统一使用主题内容色 */
.popover-glass .day-btn { color: var(--color-base-content) !important; }
.dark .popover-glass .day-btn { color: var(--color-base-content) !important; }

.glass-select {
  background: rgba(255,255,255,0.10);
  color: var(--color-base-content);
  border-radius: 6px;
  padding: 4px 8px;
  border: 1px solid rgba(255,255,255,0.20);
}
@media (prefers-color-scheme: dark) {
  .glass-select { color: var(--color-base-content); }
}

.chip {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 8px;
  background: rgba(255,255,255,0.10);
  color: var(--color-base-content);
}
.chip:hover { background: rgba(255,255,255,0.20); }
@media (prefers-color-scheme: dark) {
  .chip { color: var(--color-base-content); }
}

.btn-confirm {
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 8px;
  background: var(--primary-600, #2563eb);
  color: #ffffff;
}
.btn-confirm:hover { opacity: 0.9; }
.no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
.no-scrollbar::-webkit-scrollbar { display: none; width: 0; height: 0; }
</style>


