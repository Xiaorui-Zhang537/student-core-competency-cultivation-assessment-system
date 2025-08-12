<template>
  <canvas ref="canvasRef" class="w-full h-full"></canvas>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watchEffect } from 'vue'

interface Props {
  particleCount?: number
  connectDistance?: number
  maxSpeed?: number
  opacity?: number
}

const props = withDefaults(defineProps<Props>(), {
  particleCount: 80,
  connectDistance: 120,
  maxSpeed: 0.4,
  opacity: 0.6,
})

const canvasRef = ref<HTMLCanvasElement | null>(null)
let ctx: CanvasRenderingContext2D | null = null
let rafId = 0
let particles: Array<{ x: number; y: number; vx: number; vy: number }> = []
let width = 0
let height = 0
let dpr = 1

const isReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches

const getIsDark = (): boolean => document.documentElement.classList.contains('dark')

function resizeCanvas() {
  if (!canvasRef.value) return
  const canvas = canvasRef.value
  const rect = canvas.getBoundingClientRect()
  width = Math.floor(rect.width)
  height = Math.floor(rect.height)
  dpr = Math.min(window.devicePixelRatio || 1, 2)
  canvas.width = Math.floor(width * dpr)
  canvas.height = Math.floor(height * dpr)
  canvas.style.width = width + 'px'
  canvas.style.height = height + 'px'
  ctx = canvas.getContext('2d')
  if (ctx) ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
}

function initParticles() {
  const targetCount = Math.max(10, Math.floor(props.particleCount * (width >= 1024 ? 1 : 0.5)))
  particles = Array.from({ length: targetCount }).map(() => ({
    x: Math.random() * width,
    y: Math.random() * height,
    vx: (Math.random() * 2 - 1) * props.maxSpeed,
    vy: (Math.random() * 2 - 1) * props.maxSpeed,
  }))
}

function step() {
  if (!ctx) return
  const dark = getIsDark()
  const dotColor = dark ? 'rgba(147,197,253,0.9)' : 'rgba(59,130,246,0.9)'
  const lineBase = dark ? '147,197,253' : '59,130,246'

  ctx.clearRect(0, 0, width, height)
  // move & draw particles
  ctx.fillStyle = dotColor
  for (const p of particles) {
    p.x += p.vx
    p.y += p.vy

    if (p.x <= 0 || p.x >= width) p.vx = -p.vx
    if (p.y <= 0 || p.y >= height) p.vy = -p.vy

    ctx.beginPath()
    ctx.arc(p.x, p.y, 1.8, 0, Math.PI * 2)
    ctx.fill()
  }

  // draw connections (simple, with cap on comparisons)
  const maxDist = props.connectDistance
  for (let i = 0; i < particles.length; i++) {
    for (let j = i + 1; j < particles.length; j++) {
      const dx = particles[i].x - particles[j].x
      const dy = particles[i].y - particles[j].y
      const dist2 = dx * dx + dy * dy
      if (dist2 < maxDist * maxDist) {
        const dist = Math.max(1, Math.sqrt(dist2))
        const a = Math.max(0, props.opacity * (1 - dist / maxDist))
        ctx.strokeStyle = `rgba(${lineBase},${a})`
        ctx.lineWidth = 1
        ctx.beginPath()
        ctx.moveTo(particles[i].x, particles[i].y)
        ctx.lineTo(particles[j].x, particles[j].y)
        ctx.stroke()
      }
    }
  }

  rafId = requestAnimationFrame(step)
}

function start() {
  if (isReducedMotion) return
  resizeCanvas()
  initParticles()
  cancelAnimationFrame(rafId)
  rafId = requestAnimationFrame(step)
}

function stop() {
  cancelAnimationFrame(rafId)
}

let resizeTimer: number | undefined
function handleResize() {
  window.clearTimeout(resizeTimer)
  resizeTimer = window.setTimeout(() => {
    resizeCanvas()
    initParticles()
  }, 150)
}

function handleVisibility() {
  if (document.hidden) stop()
  else start()
}

onMounted(() => {
  // ensure canvas covers viewport (since parent will be fixed inset-0)
  start()
  window.addEventListener('resize', handleResize)
  document.addEventListener('visibilitychange', handleVisibility)
})

onUnmounted(() => {
  stop()
  window.removeEventListener('resize', handleResize)
  document.removeEventListener('visibilitychange', handleVisibility)
})

// react to theme toggle (uiStore may toggle dark class). Use watchEffect polling class
watchEffect(() => {
  // re-render picks color per frame; explicit hook not required
})
</script>

<style scoped>
:host, canvas {
  display: block;
}
</style>

