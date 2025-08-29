<template>
  <div ref="root" class="absolute inset-0 pointer-events-none" :style="styleObj"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watchEffect } from 'vue'

interface Props {
  blur?: number
  opacity?: number
}

const props = withDefaults(defineProps<Props>(), {
  blur: 10,
  opacity: 0.86
})

const root = ref<HTMLDivElement | null>(null)
let raf = 0
let canvas: HTMLCanvasElement | null = null
let ctx: CanvasRenderingContext2D | null = null

const styleObj = {
  zIndex: 0 as any
}

function draw() {
  if (!root.value || !canvas || !ctx) return
  const rect = (root.value.parentElement || root.value).getBoundingClientRect()
  const DPR = Math.min(window.devicePixelRatio || 1, 2)
  const w = Math.max(1, Math.floor(rect.width * DPR))
  const h = Math.max(1, Math.floor(rect.height * DPR))
  if (canvas.width !== w || canvas.height !== h) {
    canvas.width = w
    canvas.height = h
    canvas.style.width = rect.width + 'px'
    canvas.style.height = rect.height + 'px'
  }
  // Fill with semi-opaque neutral, then blur via CSS filter on canvas element
  ctx.clearRect(0, 0, w, h)
  ctx.fillStyle = `rgba(255,255,255,${props.opacity})`
  ctx.fillRect(0, 0, w, h)
  ;(canvas.style as any).filter = `blur(${props.blur}px)`
}

function loop() {
  draw()
  raf = requestAnimationFrame(loop)
}

onMounted(() => {
  if (!root.value) return
  canvas = document.createElement('canvas')
  canvas.style.position = 'absolute'
  canvas.style.inset = '0'
  canvas.style.pointerEvents = 'none'
  root.value.appendChild(canvas)
  ctx = canvas.getContext('2d')
  loop()
})

onUnmounted(() => {
  cancelAnimationFrame(raf)
  if (root.value && canvas) try { root.value.removeChild(canvas) } catch {}
  canvas = null
  ctx = null
})

watchEffect(() => { draw() })
</script>

<style scoped>
:host, div[ref="root"] { display: contents; }
</style>


