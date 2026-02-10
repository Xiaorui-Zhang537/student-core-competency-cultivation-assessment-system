<template>
  <button
    ref="rippleButtonRef"
    :class="mergedClass"
    :style="{ '--duration': String(duration) + 'ms' }"
    @click="handleClick"
  >
    <div :class="innerClass">
      <slot />
    </div>

    <span class="pointer-events-none absolute inset-0">
      <span
        v-for="ripple in buttonRipples"
        :key="ripple.key"
        class="ripple-animation absolute rounded-full opacity-30"
        :style="{
          width: ripple.size + 'px',
          height: ripple.size + 'px',
          top: ripple.y + 'px',
          left: ripple.x + 'px',
          backgroundColor: rippleColor,
          transform: 'scale(0)',
          animationDuration: String(duration) + 'ms',
        }"
      />
    </span>
  </button>
</template>

<script lang="ts" setup>
import { ref, computed, type HTMLAttributes } from 'vue'

interface RippleButtonProps {
  class?: HTMLAttributes['class']
  rippleColor?: string
  duration?: number
  icon?: boolean
  pill?: boolean
}

const props = withDefaults(defineProps<RippleButtonProps>(), {
  rippleColor: 'color-mix(in oklab, var(--color-primary) 30%, transparent)',
  duration: 600,
  icon: false,
  pill: false
})

const emit = defineEmits<{ (e: 'click', event: MouseEvent): void }>()

const rippleButtonRef = ref<HTMLButtonElement | null>(null)
const buttonRipples = ref<Array<{ x: number; y: number; size: number; key: number }>>([])

const baseClass = computed(() => {
  if (props.pill) {
    return 'relative inline-flex cursor-pointer items-center justify-center overflow-hidden rounded-full bg-background px-4 h-11 text-sm text-current'
  }
  if (props.icon) {
    return 'relative flex cursor-pointer items-center justify-center overflow-hidden rounded-full bg-background/0 p-0 w-9 h-9 text-current'
  }
  return 'relative flex cursor-pointer items-center justify-center overflow-hidden rounded-lg bg-background px-4 py-2 text-center text-[var(--color-primary-content)]'
})
const mergedClass = computed(() => [baseClass.value, props.class].filter(Boolean).join(' '))

const innerClass = computed(() => props.pill
  ? 'relative z-10 flex items-center gap-2'
  : 'relative z-10'
)

function handleClick(event: MouseEvent) {
  createRipple(event)
  emit('click', event)
}

function createRipple(event: MouseEvent) {
  if ((props.duration ?? 0) <= 0) return
  const button = rippleButtonRef.value
  if (!button) return
  const rect = button.getBoundingClientRect()
  const size = Math.max(rect.width, rect.height)
  const x = event.clientX - rect.left - size / 2
  const y = event.clientY - rect.top - size / 2
  const newRipple = { x, y, size, key: Date.now() }
  buttonRipples.value.push(newRipple)
  window.setTimeout(() => {
    buttonRipples.value = buttonRipples.value.filter(r => r.key !== newRipple.key)
  }, props.duration)
}
</script>

<style scoped>
@keyframes rippling {
  0% { opacity: 1; }
  100% { transform: scale(2); opacity: 0; }
}
.ripple-animation { animation: rippling var(--duration) ease-out; }
</style>


