<template>
  <section id="motion" class="mt-16 md:mt-24 scroll-mt-24">
    <div class="mx-auto max-w-6xl px-4">
      <div class="grid md:grid-cols-12 gap-8 items-center">
        <div class="md:col-span-5">
          <h2 class="text-3xl sm:text-4xl md:text-5xl font-extrabold leading-tight text-[color:var(--color-base-content)]">
            {{ t('app.home.rive.title') }}
          </h2>
          <p class="mt-3 text-base sm:text-lg text-subtle leading-relaxed">
            {{ t('app.home.rive.subtitle') }}
          </p>

          <div class="mt-6">
            <div class="flex flex-wrap items-center gap-3">
              <Button variant="secondary" size="md" class="px-5" @click="togglePlay">
                {{ isPlaying ? t('common.pause') : t('common.play') }}
              </Button>
              <Button variant="info" size="md" class="px-5" @click="randomizeRating">
                {{ t('app.home.rive.actions.random') }}
              </Button>
              <Button variant="ghost" size="md" class="px-5" @click="resetRating">
                {{ t('app.home.rive.actions.reset') }}
              </Button>
            </div>

            <div class="mt-4">
              <div class="flex items-center justify-between gap-3">
                <div class="text-sm text-subtle">
                  {{ t('app.home.rive.rating.label') }}:
                  <span class="font-semibold text-[color:var(--color-base-content)] tabular-nums">
                    {{ rating.toFixed(1) }} / 5.0
                  </span>
                </div>
                <div class="text-xs opacity-70">
                  {{ t('app.home.rive.rating.hint') }}
                </div>
              </div>
              <input
                v-model.number="rating"
                type="range"
                min="0"
                max="5"
                step="0.1"
                class="range range-primary mt-2"
                :disabled="!isReady"
                @input="applyRating"
              />
            </div>
          </div>

          <p class="mt-4 text-xs text-subtle/70 leading-relaxed">
            {{ t('app.home.rive.note') }}
          </p>
        </div>

        <div class="md:col-span-7">
          <liquid-glass
            :radius="18"
            :frost="0.06"
            :tint="true"
            :tint-from="'var(--color-info)'"
            :tint-to="'var(--color-accent)'"
            :container-class="'relative rounded-3xl overflow-hidden'"
          >
            <border-beam :size="280" :duration="16" :border-width="1.5" :delay="1.2" />

            <div class="relative p-4 sm:p-5">
              <div class="absolute inset-0 pointer-events-none opacity-60" aria-hidden="true">
                <meteors :count="8" />
              </div>

              <div class="relative rounded-2xl overflow-hidden border border-white/12 dark:border-white/10 bg-white/20 dark:bg-white/5">
                <canvas
                  ref="canvasRef"
                  class="block w-full h-[360px] sm:h-[420px] md:h-[460px]"
                  role="img"
                  :aria-label="t('app.home.rive.canvasAria') as string"
                  @pointerdown="onCanvasPointerDown"
                />
              </div>

              <div class="relative mt-4 flex items-center justify-between gap-3">
                <div class="text-sm text-subtle">
                  {{ t('app.home.rive.status') }}:
                  <span class="font-semibold text-[color:var(--color-base-content)]">{{ statusLabel }}</span>
                </div>
                <div class="text-xs opacity-70">
                  {{ t('app.home.rive.runtime') }}: Canvas Lite
                </div>
              </div>
            </div>
          </liquid-glass>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import LiquidGlass from '@/components/ui/LiquidGlass.vue'
import BorderBeam from '@/components/ui/inspira/BorderBeam.vue'
import Meteors from '@/components/ui/inspira/Meteors.vue'
import Button from '@/components/ui/Button.vue'

const { t } = useI18n()

const canvasRef = ref<HTMLCanvasElement | null>(null)
const riveRef = ref<any>(null)
const isPlaying = ref(true)
const isReady = ref(false)
const rating = ref(3.8)
let ratingInput: any = null
const activeMode = ref<'playing' | 'paused' | 'fallback'>('fallback')
const statusLabel = computed(() => {
  if (activeMode.value === 'paused') return t('app.home.rive.modes.paused')
  if (activeMode.value === 'playing') return t('app.home.rive.modes.playing')
  return t('app.home.rive.modes.fallback')
})

let io: IntersectionObserver | null = null
let removeResize: (() => void) | null = null
let removeRO: (() => void) | null = null

function drawFallback(message?: string) {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  const w = canvas.clientWidth || 960
  const h = canvas.clientHeight || 540
  // Make sure the backing store matches CSS pixels for crisp text.
  canvas.width = Math.max(1, Math.floor(w * (window.devicePixelRatio || 1)))
  canvas.height = Math.max(1, Math.floor(h * (window.devicePixelRatio || 1)))
  ctx.scale(window.devicePixelRatio || 1, window.devicePixelRatio || 1)

  const g = ctx.createLinearGradient(0, 0, w, h)
  g.addColorStop(0, 'rgba(56,189,248,0.18)')
  g.addColorStop(1, 'rgba(217,70,239,0.14)')
  ctx.fillStyle = g
  ctx.fillRect(0, 0, w, h)

  ctx.fillStyle = 'rgba(255,255,255,0.85)'
  ctx.font = '600 16px ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial'
  ctx.fillText(t('app.home.rive.fallbackTitle') as string, 20, 34)
  ctx.fillStyle = 'rgba(255,255,255,0.65)'
  ctx.font = '400 13px ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial'
  const line = message || (t('app.home.rive.fallbackDesc') as string)
  ctx.fillText(line, 20, 58)
}

async function setupRive() {
  const canvas = canvasRef.value
  if (!canvas) return
  if (typeof window === 'undefined') return
  if (window.matchMedia?.('(prefers-reduced-motion: reduce)')?.matches) return

  let RiveCtor: any = null
  try {
    const mod: any = await import('@rive-app/canvas-lite')
    RiveCtor = mod?.Rive || mod?.default?.Rive || mod?.default || mod
  } catch {
    activeMode.value = 'fallback'
    drawFallback(t('app.home.rive.fallbackNoRuntime') as string)
    return
  }
  if (!RiveCtor) return

  // Rive community file: Star Rating (interactive input: "Rating")
  // Used as a demo that matches this system's "assessment" theme.
  const src = 'https://public.rive.app/community/runtime-files/3145-6649-star-rating.riv'

  const rive = new RiveCtor({
    src,
    canvas,
    autoplay: true,
    stateMachines: 'State Machine 1',
    // Canvas size sync: makes it crisp on DPR changes.
    onLoad: () => {
      try { rive.resizeDrawingSurfaceToCanvas?.() } catch {}
      try {
        const inputs = rive.stateMachineInputs?.('State Machine 1') || []
        ratingInput = (inputs || []).find((i: any) => i?.name === 'Rating') || null
        isReady.value = true
        activeMode.value = 'playing'
        applyRating()
      } catch {}
    },
  })
  riveRef.value = rive

  const onResize = () => { try { rive.resizeDrawingSurfaceToCanvas?.() } catch {} }
  window.addEventListener('resize', onResize, { passive: true })
  removeResize = () => window.removeEventListener('resize', onResize)

  try {
    const ro = new ResizeObserver(() => { try { rive.resizeDrawingSurfaceToCanvas?.() } catch {} })
    ro.observe(canvas)
    removeRO = () => { try { ro.disconnect() } catch {} }
  } catch {}

  // Pause when out of view to save CPU.
  try {
    io?.disconnect()
    io = new IntersectionObserver(
      (entries) => {
        const e = entries[0]
        if (!e) return
        if (e.isIntersecting) {
          if (isPlaying.value) {
            try { rive.play?.() } catch {}
          }
        } else {
          try { rive.pause?.() } catch {}
        }
      },
      { threshold: 0.2 }
    )
    io.observe(canvas)
  } catch {}
}

function applyRating() {
  try {
    const v = Math.max(0, Math.min(5, Number(rating.value)))
    rating.value = v
    if (ratingInput) ratingInput.value = v
  } catch {}
}

function resetRating() {
  rating.value = 0
  applyRating()
}

function randomizeRating() {
  const v = Math.round((Math.random() * 5) * 10) / 10
  rating.value = v
  applyRating()
}

function togglePlay() {
  const rive = riveRef.value
  if (!rive) return
  isPlaying.value = !isPlaying.value
  if (isPlaying.value) {
    if (activeMode.value === 'paused') activeMode.value = 'playing'
    try { rive.play?.() } catch {}
  } else {
    activeMode.value = 'paused'
    try { rive.pause?.() } catch {}
  }
}

function onCanvasPointerDown(e: PointerEvent) {
  const canvas = canvasRef.value
  if (!canvas || !isReady.value) return
  try {
    const rect = canvas.getBoundingClientRect()
    const x = Math.max(0, Math.min(rect.width, e.clientX - rect.left))
    const v = Math.round(((x / rect.width) * 5) * 10) / 10
    rating.value = v
    applyRating()
  } catch {}
}

onMounted(() => {
  // Draw an immediate placeholder so the section never looks "empty".
  drawFallback()
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
  ratingInput = null
})
</script>
