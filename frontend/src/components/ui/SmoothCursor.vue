<template>
  <div class="pointer-events-none fixed inset-0 z-50">
    <div ref="wrapRef" class="smooth-cursor-wrap">
      <component :is="cursorComponent" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch, shallowRef } from 'vue'
import DefaultCursor from '@/components/ui/DefaultCursor.vue'

type SpringConfig = {
  damping: number
  stiffness: number
  mass: number
  restDelta: number
}

const props = withDefaults(defineProps<{
  cursor?: any
  springConfig?: SpringConfig
}>(), {
  cursor: undefined,
  springConfig: () => ({ damping: 45, stiffness: 400, mass: 1, restDelta: 0.001 }),
})

const wrapRef = ref<HTMLDivElement | null>(null)
const cursorComponent = shallowRef<any>(props.cursor || DefaultCursor)

let rafId = 0
let targetX = 0
let targetY = 0

let x = 0
let y = 0
let vx = 0
let vy = 0

let lastTs = 0

function onMove(e: MouseEvent) {
  targetX = e.clientX
  targetY = e.clientY
  if (!lastTs) lastTs = performance.now()
}

function step(ts: number) {
  const dt = Math.min(0.032, (ts - lastTs) / 1000 || 0.016)
  lastTs = ts
  const { damping, stiffness, mass, restDelta } = props.springConfig!

  const k = stiffness
  const c = damping
  const m = mass

  const dx = x - targetX
  const dy = y - targetY

  const ax = (-k * dx - c * vx) / m
  const ay = (-k * dy - c * vy) / m

  vx += ax * dt
  vy += ay * dt
  x += vx * dt
  y += vy * dt

  const angle = Math.atan2(vy, vx)

  if (wrapRef.value) {
    wrapRef.value.style.transform = `translate3d(${x}px, ${y}px, 0) rotate(${angle}rad)`
  }

  const atRest = Math.abs(dx) < restDelta && Math.abs(dy) < restDelta && Math.abs(vx) < restDelta && Math.abs(vy) < restDelta
  rafId = requestAnimationFrame(step)
}

onMounted(() => {
  document.documentElement.classList.add('cursor-none')
  window.addEventListener('mousemove', onMove, { passive: true })
  rafId = requestAnimationFrame((t) => { lastTs = t; step(t) })
})

onUnmounted(() => {
  document.documentElement.classList.remove('cursor-none')
  window.removeEventListener('mousemove', onMove)
  cancelAnimationFrame(rafId)
})

watch(() => props.cursor, (c) => {
  cursorComponent.value = c || DefaultCursor
})

// always use default if not provided
watch(() => props.cursor, (c) => { cursorComponent.value = c || DefaultCursor }, { immediate: true })
</script>

<style scoped>
.smooth-cursor-wrap { position: fixed; left: 0; top: 0; will-change: transform; transform-origin: 0 0; }
</style>


