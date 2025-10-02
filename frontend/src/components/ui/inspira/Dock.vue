<template>
  <div
    ref="dockRef"
    :class="[
      baseClass,
      orientation === 'vertical' ? 'flex-col w-[58px] h-max' : 'flex-row',
      dockClass,
      props.class,
    ]"
    v-glass="variant === 'glass' ? { strength: 'regular', interactive: true } : undefined"
    @mousemove="onMouseMove"
    @mouseleave="onMouseLeave"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, provide } from 'vue'
import type { DataOrientation, Direction } from './types'
import { MOUSE_X_INJECTION_KEY, MOUSE_Y_INJECTION_KEY, MAGNIFICATION_INJECTION_KEY, DISTANCE_INJECTION_KEY, ORIENTATION_INJECTION_KEY } from './injectionKeys'

interface DockProps {
  class?: string | Record<string, boolean> | string[]
  magnification?: number
  distance?: number
  direction?: Direction
  orientation?: DataOrientation
  variant?: 'glass' | 'transparent'
  paddingClass?: string
  heightClass?: string
  roundedClass?: string
  gapClass?: string
}

const props = withDefaults(defineProps<DockProps>(), {
  magnification: 60,
  distance: 140,
  direction: 'middle',
  orientation: 'horizontal',
  variant: 'glass',
  paddingClass: 'p-2',
  heightClass: 'h-[58px]',
  roundedClass: 'rounded-full',
  gapClass: 'gap-3',
})

const dockRef = ref<HTMLElement | null>(null)
const mouseX = ref(Infinity)
const mouseY = ref(Infinity)
const magnification = computed(() => props.magnification)
const distance = computed(() => props.distance)

const dockClass = computed(() => ({
  'items-start': props.direction === 'top',
  'items-center': props.direction === 'middle',
  'items-end': props.direction === 'bottom',
}))

const baseClass = computed(() => [
  'transition-all select-none flex w-max backdrop-blur-md',
  props.gapClass,
  props.roundedClass,
  props.heightClass,
  props.paddingClass,
  props.variant === 'glass' ? 'glass-regular border' : 'border-transparent bg-transparent shadow-none',
].join(' '))

function onMouseMove(e: MouseEvent) {
  requestAnimationFrame(() => {
    mouseX.value = e.pageX
    mouseY.value = e.pageY
  })
}
function onMouseLeave() {
  requestAnimationFrame(() => {
    mouseX.value = Infinity
    mouseY.value = Infinity
  })
}

provide(MOUSE_X_INJECTION_KEY, mouseX)
provide(MOUSE_Y_INJECTION_KEY, mouseY)
provide(ORIENTATION_INJECTION_KEY, props.orientation)
provide(MAGNIFICATION_INJECTION_KEY, magnification)
provide(DISTANCE_INJECTION_KEY, distance)
</script>

<style scoped>
</style>


