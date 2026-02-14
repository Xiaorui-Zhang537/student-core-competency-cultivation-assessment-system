<template>
  <!-- display: contents 避免改变布局结构（DockBar 等场景很关键） -->
  <span
    ref="wrapRef"
    class="glass-tooltip-wrap"
    @mouseover="onMouseOver"
    @mouseout="onMouseOut"
    @focusin="onFocusIn"
    @focusout="onFocusOut"
  >
    <slot />
  </span>

  <teleport to="body">
    <div
      v-show="visible"
      :id="tooltipId"
      role="tooltip"
      class="glass-tooltip-layer"
      :style="layerStyle"
    >
      <liquid-glass
        effect="occlusionBlur"
        :radius="12"
        :frost="0.06"
        containerClass="rounded-xl shadow-lg"
        class="px-3 py-2 text-xs text-base-content"
      >
        <div class="max-w-[260px] leading-snug">
          {{ content }}
        </div>
      </liquid-glass>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref } from 'vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'

type Placement = 'top' | 'bottom' | 'left' | 'right'

const props = withDefaults(defineProps<{
  content: string
  placement?: Placement
  offset?: number
  delay?: number
  disabled?: boolean
}>(), {
  placement: 'top',
  offset: 10,
  delay: 420,
  disabled: false,
})

const wrapRef = ref<HTMLElement | null>(null)
const visible = ref(false)
const layerStyle = ref<Record<string, string>>({ left: '0px', top: '0px' })
const tooltipId = `glass-tooltip-${Math.random().toString(36).slice(2)}`

let showTimer: number | null = null
let rafId: number | null = null
let activeTrigger: HTMLElement | null = null

const hoverCapable = computed(() => {
  try { return window.matchMedia && window.matchMedia('(hover: hover)').matches } catch { return true }
})

function clearShowTimer() {
  if (showTimer != null) {
    window.clearTimeout(showTimer)
    showTimer = null
  }
}

function cancelRaf() {
  if (rafId != null) {
    cancelAnimationFrame(rafId)
    rafId = null
  }
}

function isDisabled() {
  if (props.disabled) return true
  if (!props.content) return true
  // 移动端/触屏默认不弹 tooltip，避免遮挡（可后续加 allowTouch）
  if (!hoverCapable.value) return true
  return false
}

function pickTriggerEl(from: EventTarget | null): HTMLElement | null {
  const wrap = wrapRef.value
  if (!wrap) return null
  const raw = from instanceof HTMLElement ? from : null
  const candidate = raw?.closest?.('button,a,[role="button"],input,select,textarea') as HTMLElement | null
  if (candidate && wrap.contains(candidate)) return candidate
  const first = wrap.querySelector('button,a,[role="button"],input,select,textarea') as HTMLElement | null
  return first
}

function showSoon(trigger: HTMLElement | null) {
  if (isDisabled()) return
  if (!trigger) return
  activeTrigger = trigger
  clearShowTimer()
  showTimer = window.setTimeout(() => {
    showNow()
  }, Math.max(0, props.delay))
}

function showNow() {
  clearShowTimer()
  if (isDisabled()) return
  if (!activeTrigger) return
  visible.value = true
  attachAria()
  updatePosition()
  attachGlobalListeners()
}

function hide() {
  clearShowTimer()
  cancelRaf()
  if (!visible.value) return
  visible.value = false
  detachAria()
  detachGlobalListeners()
}

function attachAria() {
  try {
    if (!activeTrigger) return
    activeTrigger.setAttribute('aria-describedby', tooltipId)
  } catch {}
}
function detachAria() {
  try {
    if (!activeTrigger) return
    if (activeTrigger.getAttribute('aria-describedby') === tooltipId) {
      activeTrigger.removeAttribute('aria-describedby')
    }
  } catch {}
  activeTrigger = null
}

function clamp(n: number, min: number, max: number) {
  return Math.max(min, Math.min(max, n))
}

async function updatePosition() {
  cancelRaf()
  rafId = requestAnimationFrame(async () => {
    try {
      if (!visible.value) return
      if (!activeTrigger) return
      await nextTick()
      const tipEl = document.getElementById(tooltipId) as HTMLElement | null
      if (!tipEl) return

      const margin = 8
      const rect = activeTrigger.getBoundingClientRect()
      const tipRect = tipEl.getBoundingClientRect()

      let left = 0
      let top = 0
      const off = Number(props.offset) || 0

      if (props.placement === 'bottom') {
        left = rect.left + rect.width / 2 - tipRect.width / 2
        top = rect.bottom + off
      } else if (props.placement === 'left') {
        left = rect.left - tipRect.width - off
        top = rect.top + rect.height / 2 - tipRect.height / 2
      } else if (props.placement === 'right') {
        left = rect.right + off
        top = rect.top + rect.height / 2 - tipRect.height / 2
      } else {
        // top
        left = rect.left + rect.width / 2 - tipRect.width / 2
        top = rect.top - tipRect.height - off
      }

      const vw = window.innerWidth
      const vh = window.innerHeight
      left = clamp(left, margin, Math.max(margin, vw - tipRect.width - margin))
      top = clamp(top, margin, Math.max(margin, vh - tipRect.height - margin))

      layerStyle.value = {
        position: 'fixed',
        left: `${Math.round(left)}px`,
        top: `${Math.round(top)}px`,
        zIndex: '2000',
        pointerEvents: 'none',
      }
    } catch {}
  })
}

function isLeavingWrap(e: MouseEvent) {
  const wrap = wrapRef.value
  const rt = e.relatedTarget as Node | null
  if (!wrap) return true
  if (!rt) return true
  return !wrap.contains(rt)
}

function onMouseOver(e: MouseEvent) {
  if (isDisabled()) return
  const trigger = pickTriggerEl(e.target)
  showSoon(trigger)
}
function onMouseOut(e: MouseEvent) {
  if (!isLeavingWrap(e)) return
  hide()
}
function onFocusIn(e: FocusEvent) {
  if (isDisabled()) return
  const trigger = pickTriggerEl(e.target)
  showSoon(trigger)
}
function onFocusOut() {
  hide()
}

function onKeydown(ev: KeyboardEvent) {
  if (ev.key === 'Escape') hide()
}
function onScrollOrResize() {
  updatePosition()
}

function attachGlobalListeners() {
  try { window.addEventListener('keydown', onKeydown, { capture: true }) } catch {}
  try { window.addEventListener('scroll', onScrollOrResize, { capture: true, passive: true }) } catch {}
  try { window.addEventListener('resize', onScrollOrResize, { passive: true }) } catch {}
}
function detachGlobalListeners() {
  try { window.removeEventListener('keydown', onKeydown, { capture: true } as any) } catch {}
  try { window.removeEventListener('scroll', onScrollOrResize, { capture: true } as any) } catch {}
  try { window.removeEventListener('resize', onScrollOrResize as any) } catch {}
}

onBeforeUnmount(() => {
  hide()
})
</script>

<style scoped>
.glass-tooltip-wrap {
  display: contents;
}
.glass-tooltip-layer {
  position: fixed;
  inset: auto;
}
</style>

