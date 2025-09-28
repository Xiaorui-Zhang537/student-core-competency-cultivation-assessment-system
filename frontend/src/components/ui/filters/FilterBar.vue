<template>
  <div
    :class="[
      'ui-filter-bar',
      'glass-thin glass-interactive rounded-xl',
      tintClass,
      dense ? 'p-2' : 'p-3',
      wrap ? `flex flex-wrap ${alignClass} gap-2` : `flex ${alignClass} gap-2 overflow-x-auto no-scrollbar`
    ]"
  >
    <div :class="`flex ${alignClass} gap-2 w-full flex-wrap`">
      <slot name="left" />
      <div :class="`ml-auto flex ${alignClass} gap-2`">
        <slot name="right" />
      </div>
      <!-- 兼容旧用法：默认插槽仍然渲染 -->
      <slot />
    </div>
  </div>
  
</template>

<script setup lang="ts">
import { computed } from 'vue'
interface Props {
  dense?: boolean
  wrap?: boolean
  tint?: 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'danger' | 'info' | null
  align?: 'start' | 'center' | 'end'
}

const props = withDefaults(defineProps<Props>(), {
  dense: false,
  wrap: true,
  tint: null,
  align: 'end'
})
const tintClass = computed(() => props.tint ? `glass-tint-${props.tint}` : '')
const alignClass = computed(() => {
  switch (props.align) {
    case 'start': return 'items-start'
    case 'center': return 'items-center'
    default: return 'items-end'
  }
})
</script>


