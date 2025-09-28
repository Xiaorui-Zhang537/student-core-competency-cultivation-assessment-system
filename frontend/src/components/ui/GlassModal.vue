<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center p-4">
    <div class="absolute inset-0" :class="backdropDark ? 'bg-black/50' : 'bg-transparent'" @click="$emit('close')"></div>
    <liquid-glass
      class="relative w-full rounded-2xl overflow-hidden flex flex-col max-w-[95vw]"
      :class="[resolvedMaxWidthClass, containerMaxHClass]"
      :radius="16"
      :frost="resolvedFrost"
      :border="0.07"
      :lightness="50"
      :alpha="resolvedAlpha"
      :blur="resolvedBlur"
      :scale="resolvedScale"
      :rOffset="0"
      :gOffset="10"
      :bOffset="20"
      containerClass="ring-1 ring-white/20 dark:ring-white/10 shadow-2xl"
    >
      <div class="p-4" style="box-shadow: inset 0 -1px 0 rgba(255,255,255,0.18);">
        <div class="flex items-center justify-between">
          <template v-if="!reverseHeader">
            <h3 class="text-lg font-semibold" style="color: var(--color-base-content)">{{ title }}</h3>
            <Button v-if="showClose" variant="ghost" size="sm" @click="$emit('close')">
              <template #icon>
                <svg class="w-4 h-4" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/></svg>
              </template>
            </Button>
          </template>
          <template v-else>
            <Button v-if="showClose" variant="ghost" size="sm" @click="$emit('close')">
              <template #icon>
                <svg class="w-4 h-4" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/></svg>
              </template>
            </Button>
            <h3 class="text-lg font-semibold" style="color: var(--color-base-content)">{{ title }}</h3>
          </template>
        </div>
      </div>
      <div class="p-4 overflow-auto min-h-0 no-scrollbar modal-body-surface" :class="[fillHeight ? 'flex-1' : '', contentMaxHClass, solidBody ? 'modal-body-solid' : '']" :style="contentSurfaceStyle">
        <slot />
      </div>
      <div v-if="$slots.footer" class="p-4 flex justify-end gap-2 modal-footer-glass">
        <slot name="footer" />
      </div>
    </liquid-glass>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Button from '@/components/ui/Button.vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'
interface Props {
  title: string
  maxWidth?: string // e.g. 'max-w-md', 'max-w-lg'
  showClose?: boolean
  blur?: 'none' | 'sm' | 'md' // 控制模糊强度
  backdropDark?: boolean // 是否暗化背景
  reverseHeader?: boolean // 反转标题和关闭按钮位置
  hideScrollbar?: boolean // 隐藏内容区滚动条（已全局默认隐藏，此属性保留兼容）
  fillHeight?: boolean // 是否内容区铺满可用高度（默认自适应）
  heightVariant?: 'compact' | 'normal' | 'large' | 'tall' | 'max' // 高度档位
  tint?: 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'danger' | 'info' | null
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl' // 固定表单尺寸（优先于 maxWidth）
  clarity?: 'default' | 'high' | 'max' // 视觉清晰度（降低折射与增加内容底）
  solidBody?: boolean // 正文区域完全不折射（提供实体底色）
}

const props = withDefaults(defineProps<Props>(), {
  maxWidth: 'max-w-lg',
  showClose: true,
  blur: 'sm',
  backdropDark: false,
  reverseHeader: false,
  hideScrollbar: true,
  fillHeight: false,
  heightVariant: 'normal',
  tint: null,
  size: undefined,
  clarity: 'default',
  solidBody: false
})

const wrapperStyle = computed(() => {
  const blurMap: Record<string, string> = {
    none: 'backdrop-filter: blur(0px);',
    sm: 'backdrop-filter: blur(6px);',
    md: 'backdrop-filter: blur(12px);'
  }
  // 更亮的边框与高透视
  return `${blurMap[props.blur] || blurMap.sm}`
})

const tintClass = computed(() => '')

// 根据 clarity 调整 LiquidGlass 参数，降低折射与提高可读性
const resolvedScale = computed(() => {
  // scale 越接近 0，折射越弱；负值表示反向偏移
  if (props.clarity === 'max') return -40
  if (props.clarity === 'high') return -90
  return -180
})
const resolvedBlur = computed(() => {
  // 适度降低 blur，避免内容过软
  if (props.clarity === 'max') return 8
  if (props.clarity === 'high') return 10
  return 11
})
const resolvedAlpha = computed(() => {
  // 轻微降低背景 alpha，提升前景对比
  if (props.clarity === 'max') return 0.88
  if (props.clarity === 'high') return 0.9
  return 0.93
})
const resolvedFrost = computed(() => {
  // 减少霜度，减少泛白
  if (props.clarity === 'max') return 0.03
  if (props.clarity === 'high') return 0.04
  return 0.05
})

// 内容区增加中性玻璃底色，确保文本与控件可读
const contentSurfaceStyle = computed(() => {
  // solidBody: 完全不折射（叠加实体底），用于高密度表单/预览
  if (props.solidBody) {
    return {
      backgroundColor: 'color-mix(in oklab, var(--color-base-100) 96%, transparent)',
      boxShadow: 'inset 0 0 0 1px color-mix(in oklab, var(--color-base-content) 10%, transparent)',
      backdropFilter: 'none',
      WebkitBackdropFilter: 'none'
    } as Record<string, string>
  }
  if (props.clarity === 'default') return undefined
  // clarity≥high：增加浅底，提升文字与控件对比
  return {
    backgroundColor: 'color-mix(in oklab, var(--color-base-100) 72%, transparent)',
    boxShadow: 'inset 0 0 0 1px color-mix(in oklab, var(--color-base-content) 10%, transparent)'
  } as Record<string, string>
})

const resolvedMaxWidthClass = computed(() => {
  // 若提供固定尺寸，则优先使用固定映射
  if (props.size) {
    switch (props.size) {
      case 'xs': return 'max-w-[320px]'
      case 'sm': return 'max-w-[480px]'
      case 'md': return 'max-w-[640px]'
      case 'lg': return 'max-w-[800px]'
      case 'xl': return 'max-w-[960px]'
      default: return props.maxWidth || 'max-w-lg'
    }
  }
  return props.maxWidth || 'max-w-lg'
})

const containerMaxHClass = computed(() => {
  switch (props.heightVariant) {
    case 'compact': return 'max-h-[60vh]'
    case 'normal': return 'max-h-[70vh]'
    case 'large': return 'max-h-[72vh]'
    case 'tall': return 'max-h-[80vh]'
    case 'max': return 'max-h-[85vh]'
    default: return 'max-h-[70vh]'
  }
})

const contentMaxHClass = computed(() => {
  switch (props.heightVariant) {
    case 'compact': return 'max-h-[48vh]'
    case 'normal': return 'max-h-[58vh]'
    case 'large': return 'max-h-[60vh]'
    case 'tall': return 'max-h-[68vh]'
    case 'max': return 'max-h-[74vh]'
    default: return 'max-h-[58vh]'
  }
})
</script>

<style scoped>
</style>


