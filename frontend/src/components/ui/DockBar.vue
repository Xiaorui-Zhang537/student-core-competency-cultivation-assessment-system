<template>
  <div class="dock-wrap" :style="wrapStyle" ref="containerRef">
    <teleport to="body">
      <!-- Dock 主体：液态玻璃药丸容器，固定宽度避免被父级裁剪 -->
      <div :style="wrapStyle" ref="teleportRoot" v-show="visible">
        <liquid-glass
          class="dock"
          effect="occlusionBlur"
          :radius="dockRadius"
          :frost="0.14"
          :tint="false"
          containerClass="dock-container"
          :style="dockInlineStyle"
        >
          <div class="dock-items">
            <glass-tooltip
              v-for="(item, idx) in items"
              :key="item.key"
              :content="item.label"
              placement="top"
            >
              <button
                class="dock-item"
                :ref="itemRefSetter(idx)"
                :class="{ active: isActive(item) }"
                :aria-label="item.label"
                @click="onClick(item.key)"
              >
                <component :is="item.icon" class="icon" />
                <span class="label">{{ item.label }}</span>
              </button>
            </glass-tooltip>
          </div>
        </liquid-glass>
      </div>
    </teleport>
  </div>
 </template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch, nextTick, type ComponentPublicInstance } from 'vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'
import GlassTooltip from '@/components/ui/GlassTooltip.vue'

export interface DockItem {
  key: string
  label: string
  icon: any
}

const props = withDefaults(defineProps<{
  items: DockItem[]
  modelValue?: string
  bottomOffset?: number
  maxWidth?: number
  visible?: boolean
}>(), {
  modelValue: '',
  bottomOffset: 24,
  maxWidth: Infinity,
  visible: true
})

const emit = defineEmits<{ (e:'update:modelValue', v:string): void; (e:'select', v:string): void }>()

const dockRadius = 28

const wrapStyle = computed(() => ({
  position: 'fixed',
  left: '0',
  right: '0',
  bottom: `calc(env(safe-area-inset-bottom, 0px) + ${(props as any).bottomOffset || 0}px)`,
  display: 'flex',
  justifyContent: 'center',
  zIndex: 1000,
  pointerEvents: 'none'
}))

// 固定宽度：由真实项宽度相加 + 间隙 + 内边距得到，适配 5/6 项差异
const containerWidth = ref<number | null>(null)
const gapPx = 12
const paddingX = 32
// 自适应模式下不再额外扩张每个项目宽度，取 0 保证“正好”包裹内容
const extraPerItem = 0

// 视口宽度用于在超出屏幕时按比例等比缩放，不造成遮蔽
const windowWidth = ref<number>(typeof window !== 'undefined' ? window.innerWidth : 0)

const dockInlineStyle = computed(() => {
  const desired = Math.max(0, Math.round(containerWidth.value || 0))
  const viewportBound = Math.max(0, windowWidth.value ? (windowWidth.value - 24) : desired)
  const bound = Math.min((props as any).maxWidth || Infinity, viewportBound)
  const scale = desired > 0 ? Math.min(1, bound / desired) : 1
  return {
    pointerEvents: 'auto',
    margin: '0 auto',
    width: desired ? `${desired}px` : 'max-content',
    transform: scale < 1 ? `scale(${scale})` : undefined,
    transformOrigin: 'center bottom',
    willChange: 'transform'
  } as Record<string, string>
})

const sliderX = ref(0)
const sliderW = ref(0)
const isDragging = ref(false)
const dragBounds = ref({ left: 0, right: 0 })
const containerRef = ref<HTMLElement | null>(null)
const itemRefs = ref<HTMLElement[]>([])
const teleportRoot = ref<HTMLElement | null>(null)
const itemRects = ref<Array<{ x: number; width: number; center: number }>>([])

function setItemRef(el: Element | ComponentPublicInstance | null, idx: number) {
  itemRefs.value[idx] = (el as HTMLElement) || undefined as any
}

function itemRefSetter(idx: number) {
  return (el: Element | ComponentPublicInstance | null) => setItemRef(el, idx)
}

function onClick(key: string) {
  try { emit('update:modelValue', key) } catch {}
  try { emit('select', key) } catch {}
}

const activeIndex = computed(() => {
  const idx = Math.max(0, props.items.findIndex(i => String(i.key) === String(props.modelValue || '')))
  return idx === -1 ? 0 : idx
})

const sliderStyle = computed(() => ({
  position: 'absolute',
  left: `${sliderX.value}px`,
  top: '50%',
  width: `${sliderW.value}px`,
  height: '44px',
  transform: 'translateY(-50%)',
  zIndex: 0
}))

function isActive(item: DockItem): boolean {
  return String(item.key) === String(props.modelValue || '')
}

function updateMeasurements() {
  try {
    const dockEl = (teleportRoot.value?.querySelector('.dock-container') || document.querySelector('.dock-container')) as HTMLElement
    if (!dockEl) return
    const rect = dockEl.getBoundingClientRect()
    // collect item rects relative to dock container
    const list = (itemRefs.value || []).map((el) => {
      if (!el) return { x: 0, width: 0, center: 0 }
      const r = el.getBoundingClientRect()
      const x = r.left - rect.left
      const width = r.width
      return { x, width, center: x + width / 2 }
    })
    itemRects.value = list
    // 容器理想宽度 = 所有项宽度之和 + gap * (n-1) + paddingX
    const total = list.reduce((s, r) => s + r.width, 0)
    const n = Math.max(0, list.length)
    containerWidth.value = total + Math.max(0, n - 1) * gapPx + paddingX + n * extraPerItem
    // 不再使用滑块
    dragBounds.value = { left: 0, right: Math.max(0, rect.width - 96) }
  } catch {}
}

function onDragStart(e: MouseEvent | TouchEvent) {}
function onDragMove(e: MouseEvent | TouchEvent) {}
function onDragEnd() {}

function onResize() {
  try {
    windowWidth.value = window.innerWidth
    updateMeasurements()
  } catch {}
}

onMounted(async () => {
  try {
    await nextTick()
    updateMeasurements()
    window.addEventListener('resize', onResize)
  } catch {}
})
onUnmounted(() => {
  try { window.removeEventListener('resize', onResize) } catch {}
})

watch(() => props.items, async () => { await nextTick(); updateMeasurements() })
watch(() => props.modelValue, async () => { await nextTick(); updateMeasurements() })
</script>

<style scoped>
.dock-wrap { pointer-events: none; overflow: visible; }
/* 子组件 LiquidGlass 根节点上的类，需要使用 :deep 才能命中 */
:deep(.dock) { pointer-events: auto; position: relative; display: inline-flex; }
:deep(.dock-container) { display: inline-flex; align-items: center; padding: 12px 16px; min-height: 72px; overflow: visible; width: max-content; white-space: nowrap; }
:deep(.dock-container.effect) {
  /* background 由 LiquidGlass 提供（遮蔽层），不要强制透明 */
  /* 移除 LiquidGlass 默认的内阴影（内框线），仅保留柔和外投影 */
  background: light-dark(hsl(0 0% 100% / var(--frost, 0)), hsl(0 0% 0% / var(--frost, 0))) !important;
  box-shadow:
    0px 2px 10px rgba(17, 17, 26, 0.04),
    0px 6px 16px rgba(17, 17, 26, 0.04),
    0px 12px 28px rgba(17, 17, 26, 0.04) !important;
  border: 1px solid var(--glass-border-color) !important;
}
:deep(.dock .slot-container) {
  background: transparent !important;
  box-shadow: none !important;
}
.dock-items { position: relative; z-index: 1; display: inline-flex; flex-direction: row; align-items: center; gap: 12px; background: transparent; white-space: nowrap; }
.dock-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8px 6px; /* 控制每项的水平占位 */
  border-radius: 16px;
  transition: transform 160ms ease, background-color 160ms ease;
  background: transparent; /* 禁止白色底遮蔽玻璃 */
  border: 0;
}
.dock-item .icon { width: 28px; height: 28px; }
.dock-item .label { margin-top: 4px; font-size: 13px; }
.dock-item:hover { transform: translateY(-2px); }
.dock-item { position: relative; }
.dock-item > * { position: relative; z-index: 1; }
.dock-item.active { background-color: transparent; }
.dock-item.active::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  pointer-events: none;
  z-index: 0;
  /* 统一用主题语义文本色进行遮蔽：
     - 浅色主题下 var(--color-base-content) 偏深色 → 产生暗化
     - 暗黑主题下 var(--color-base-content) 偏浅色 → 产生提亮 */
  box-shadow: inset 0 0 0 9999px color-mix(in oklab, var(--color-base-content) 18%, transparent);
}

/* Dock 内部元素去白底：避免按钮/子元素自己再盖一层底色 */
:deep(.dock-container),
:deep(.dock-container *),
:deep(.dock .slot-container) {
  background: transparent !important;
}

/* 焦点可视化：半透明描边代替底色 */
.dock-item:focus-visible {
  outline: none;
  box-shadow: 0 0 0 2px color-mix(in oklab, var(--color-primary) 40%, transparent);
}

:deep(.slider) { pointer-events: auto; position: absolute; mix-blend-mode: plus-lighter; }
</style>


