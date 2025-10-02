<template>
  <div
    ref="iconRef"
    :class="[
      'flex cursor-pointer items-center justify-center rounded-full transition-all duration-200 ease-out',
      square && 'aspect-square'
    ]"
    :style="styleObject"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
import { ref, inject, computed } from 'vue'
import { MOUSE_X_INJECTION_KEY, MOUSE_Y_INJECTION_KEY, MAGNIFICATION_INJECTION_KEY, DISTANCE_INJECTION_KEY, ORIENTATION_INJECTION_KEY } from './injectionKeys'

const props = withDefaults(defineProps<{ square?: boolean; baseSize?: number; ml?: number; mr?: number }>(), { square: true, baseSize: 40, ml: 0, mr: 0 })
const square = computed(() => props.square)
const iconRef = ref<HTMLDivElement | null>(null)
const mouseX = inject(MOUSE_X_INJECTION_KEY, ref(Infinity))
const mouseY = inject(MOUSE_Y_INJECTION_KEY, ref(Infinity))
const distance = inject(DISTANCE_INJECTION_KEY)!
const orientation = inject(ORIENTATION_INJECTION_KEY, 'horizontal')
const magnification = inject(MAGNIFICATION_INJECTION_KEY)!
const isVertical = computed(() => orientation === 'vertical')
const margin = ref(0)

function calculateDistance(val: number) {
  if (isVertical.value) {
    const bounds = iconRef.value?.getBoundingClientRect() || { y: 0, height: 0 } as any
    return val - bounds.y - bounds.height / 2
  }
  const bounds = iconRef.value?.getBoundingClientRect() || { x: 0, width: 0 } as any
  return val - bounds.x - bounds.width / 2
}

const iconWidth = computed(() => {
  const dist = isVertical.value ? calculateDistance(mouseY.value) : calculateDistance(mouseX.value)
  const base = props.baseSize || 40
  if (!distance?.value || !magnification?.value) return base
  if (Math.abs(dist) < distance.value) {
    return (1 - Math.abs(dist) / distance.value) * magnification.value + base
  }
  return base
})

const styleObject = computed(() => {
  const w = iconWidth.value
  const ml = (props.ml || 0) + margin.value
  const mr = (props.mr || 0) + margin.value
  if (square.value) {
    return { width: w + 'px', height: w + 'px', marginLeft: ml + 'px', marginRight: mr + 'px' }
  }
  // 非正方形：高度跟随放大，宽度自适应内容
  return { height: w + 'px', marginLeft: ml + 'px', marginRight: mr + 'px', paddingLeft: '2px', paddingRight: '2px' }
})
</script>

<style scoped>
</style>


