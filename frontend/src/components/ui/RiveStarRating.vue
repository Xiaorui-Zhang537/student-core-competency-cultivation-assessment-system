<template>
  <canvas
    ref="canvasRef"
    class="block"
    :class="{ 'cursor-pointer': interactive }"
    :style="{ width: `${width}px`, height: `${height}px` }"
    role="img"
    :aria-label="ariaLabel"
    @pointerdown="handlePointerDown"
  />
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'

const props = withDefaults(defineProps<{
  value: number
  width?: number
  height?: number
  interactive?: boolean
  ariaLabel?: string
  useRive?: boolean
  activeColor?: string
  inactiveColor?: string
}>(), {
  width: 96,
  height: 28,
  interactive: false,
  ariaLabel: 'Star rating',
  useRive: true,
  activeColor: '#f59e0b',
  inactiveColor: 'rgba(148, 163, 184, 0.55)'
})

const emit = defineEmits<{
  (e: 'update:value', value: number): void
  (e: 'change', value: number): void
}>()

const canvasRef = ref<HTMLCanvasElement | null>(null)
const riveRef = ref<any>(null)
const ratingInput = ref<any>(null)
const currentValue = ref(clampRating(props.value))

let io: IntersectionObserver | null = null
let removeResize: (() => void) | null = null
let removeRO: (() => void) | null = null

watch(() => props.value, (next) => {
  currentValue.value = clampRating(next)
  renderCurrentState()
}, { immediate: true })

function clampRating(value: number) {
  const safe = Number.isFinite(Number(value)) ? Number(value) : 0
  return Math.max(0, Math.min(5, Math.round(safe * 10) / 10))
}

function snapInteractiveRating(value: number) {
  const clamped = clampRating(value)
  return Math.max(0, Math.min(5, Math.round(clamped)))
}

function syncCanvasSize() {
  const canvas = canvasRef.value
  if (!canvas) return
  const dpr = window.devicePixelRatio || 1
  const width = Math.max(1, Math.floor(props.width * dpr))
  const height = Math.max(1, Math.floor(props.height * dpr))
  if (canvas.width !== width) {
    canvas.width = width
  }
  if (canvas.height !== height) {
    canvas.height = height
  }
}

function drawFallback() {
  const canvas = canvasRef.value
  if (!canvas) return
  syncCanvasSize()
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const dpr = window.devicePixelRatio || 1
  ctx.setTransform(1, 0, 0, 1, 0, 0)
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  ctx.scale(dpr, dpr)

  const width = props.width
  const height = props.height
  const ratio = currentValue.value / 5
  const stars = '★★★★★'
  const fontSize = Math.max(14, Math.floor(height * 0.72))
  const font = `700 ${fontSize}px ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial`
  const baselineY = height * 0.78
  const activeColor = resolveCanvasColor(props.activeColor, '#f59e0b')
  const inactiveColor = resolveCanvasColor(props.inactiveColor, 'rgba(148, 163, 184, 0.55)')

  ctx.font = font
  ctx.textAlign = 'left'
  ctx.textBaseline = 'alphabetic'
  ctx.fillStyle = inactiveColor
  ctx.fillText(stars, 0, baselineY)

  ctx.save()
  ctx.beginPath()
  ctx.rect(0, 0, width * ratio, height)
  ctx.clip()
  ctx.fillStyle = activeColor
  ctx.fillText(stars, 0, baselineY)
  ctx.restore()
}

function resolveCanvasColor(value: string | undefined, fallback: string) {
  const raw = String(value || '').trim()
  if (!raw) return fallback
  if (raw.startsWith('--') && typeof window !== 'undefined') {
    const resolved = getComputedStyle(document.documentElement).getPropertyValue(raw).trim()
    return resolved || fallback
  }
  return raw
}

function renderCurrentState() {
  const rive = riveRef.value
  const input = ratingInput.value
  if (rive && input) {
    try {
      input.value = currentValue.value
      return
    } catch {}
  }
  drawFallback()
}

async function setupRive() {
  const canvas = canvasRef.value
  if (!canvas || typeof window === 'undefined') {
    return
  }

  drawFallback()

  if (!props.useRive) {
    return
  }

  let RiveCtor: any = null
  try {
    const mod: any = await import('@rive-app/canvas-lite')
    RiveCtor = mod?.Rive || mod?.default?.Rive || mod?.default || mod
  } catch {
    return
  }
  if (!RiveCtor) {
    return
  }

  const rive = new RiveCtor({
    src: 'https://public.rive.app/community/runtime-files/3145-6649-star-rating.riv',
    canvas,
    autoplay: true,
    stateMachines: 'State Machine 1',
    onLoad: () => {
      try { rive.resizeDrawingSurfaceToCanvas?.() } catch {}
      try {
        const inputs = rive.stateMachineInputs?.('State Machine 1') || []
        ratingInput.value = (inputs || []).find((item: any) => item?.name === 'Rating') || null
      } catch {}
      renderCurrentState()
    }
  })
  riveRef.value = rive

  const onResize = () => {
    drawFallback()
    try { rive.resizeDrawingSurfaceToCanvas?.() } catch {}
    renderCurrentState()
  }
  window.addEventListener('resize', onResize, { passive: true })
  removeResize = () => window.removeEventListener('resize', onResize)

  try {
    const ro = new ResizeObserver(() => onResize())
    ro.observe(canvas)
    removeRO = () => { try { ro.disconnect() } catch {} }
  } catch {}

  try {
    io = new IntersectionObserver((entries) => {
      const entry = entries[0]
      if (!entry) return
      if (entry.isIntersecting) {
        try { rive.play?.() } catch {}
        renderCurrentState()
      } else {
        try { rive.pause?.() } catch {}
      }
    }, { threshold: 0.15 })
    io.observe(canvas)
  } catch {}
}

function handlePointerDown(event: PointerEvent) {
  if (!props.interactive) {
    return
  }
  const canvas = canvasRef.value
  if (!canvas) {
    return
  }
  const rect = canvas.getBoundingClientRect()
  if (!rect.width) {
    return
  }
  const next = snapInteractiveRating(((event.clientX - rect.left) / rect.width) * 5)
  currentValue.value = next
  renderCurrentState()
  emit('update:value', next)
  emit('change', next)
}

onMounted(() => {
  setupRive()
})

onUnmounted(() => {
  try { io?.disconnect() } catch {}
  io = null
  try { removeResize?.() } catch {}
  removeResize = null
  try { removeRO?.() } catch {}
  removeRO = null
  const rive = riveRef.value
  if (rive) {
    try { rive.cleanup?.() } catch {}
    try { rive.pause?.() } catch {}
  }
  riveRef.value = null
  ratingInput.value = null
})
</script>
