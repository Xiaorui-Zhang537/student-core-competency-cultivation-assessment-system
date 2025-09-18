<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center p-4">
    <div class="absolute inset-0" :class="backdropDark ? 'bg-black/50' : 'bg-transparent'" @click="$emit('close')"></div>
    <div
      class="relative w-full rounded-2xl glass-thin glass-interactive border shadow-xl overflow-hidden flex flex-col max-w-[95vw]"
      :class="[maxWidth, containerMaxHClass]"
      :style="wrapperStyle"
    >
      <div class="p-4" style="box-shadow: inset 0 -1px 0 rgba(255,255,255,0.18);">
        <div class="flex items-center justify-between">
          <template v-if="!reverseHeader">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ title }}</h3>
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
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ title }}</h3>
          </template>
        </div>
      </div>
      <div class="p-4 overflow-auto min-h-0 no-scrollbar" :class="[fillHeight ? 'flex-1' : '', contentMaxHClass]">
        <slot />
      </div>
      <div v-if="$slots.footer" class="p-4 flex justify-end gap-2" style="box-shadow: inset 0 1px 0 rgba(255,255,255,0.18);">
        <slot name="footer" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Button from '@/components/ui/Button.vue'
interface Props {
  title: string
  maxWidth?: string // e.g. 'max-w-md', 'max-w-lg'
  showClose?: boolean
  blur?: 'none' | 'sm' | 'md' // 控制模糊强度
  backdropDark?: boolean // 是否暗化背景
  reverseHeader?: boolean // 反转标题和关闭按钮位置
  hideScrollbar?: boolean // 隐藏内容区滚动条（已全局默认隐藏，此属性保留兼容）
  fillHeight?: boolean // 是否内容区铺满可用高度（默认自适应）
  heightVariant?: 'compact' | 'normal' | 'tall' | 'max' // 高度档位
}

const props = withDefaults(defineProps<Props>(), {
  maxWidth: 'max-w-lg',
  showClose: true,
  blur: 'sm',
  backdropDark: false,
  reverseHeader: false,
  hideScrollbar: true,
  fillHeight: false,
  heightVariant: 'normal'
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

const containerMaxHClass = computed(() => {
  switch (props.heightVariant) {
    case 'compact': return 'max-h-[60vh]'
    case 'normal': return 'max-h-[70vh]'
    case 'tall': return 'max-h-[80vh]'
    case 'max': return 'max-h-[85vh]'
    default: return 'max-h-[70vh]'
  }
})

const contentMaxHClass = computed(() => {
  switch (props.heightVariant) {
    case 'compact': return 'max-h-[48vh]'
    case 'normal': return 'max-h-[58vh]'
    case 'tall': return 'max-h-[68vh]'
    case 'max': return 'max-h-[74vh]'
    default: return 'max-h-[58vh]'
  }
})
</script>

<style scoped>
</style>


