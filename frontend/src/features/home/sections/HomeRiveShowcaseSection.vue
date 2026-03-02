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

          <div class="mt-6 flex flex-wrap gap-3">
            <Button variant="primary" size="md" class="px-5" @click="playIdle">
              {{ t('app.home.rive.actions.idle') }}
            </Button>
            <Button variant="info" size="md" class="px-5" @click="playWipers">
              {{ t('app.home.rive.actions.wipers') }}
            </Button>
            <Button variant="secondary" size="md" class="px-5" @click="togglePlay">
              {{ isPlaying ? t('common.pause') : t('common.play') }}
            </Button>
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
const activeMode = ref<'idle' | 'wipers' | 'paused'>('idle')
const statusLabel = computed(() => {
  if (activeMode.value === 'paused') return t('app.home.rive.modes.paused')
  if (activeMode.value === 'wipers') return t('app.home.rive.modes.wipers')
  return t('app.home.rive.modes.idle')
})

let io: IntersectionObserver | null = null
let removeResize: (() => void) | null = null

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
    return
  }
  if (!RiveCtor) return

  const src = 'https://cdn.rive.app/animations/vehicles.riv'

  const rive = new RiveCtor({
    src,
    canvas,
    autoplay: true,
    animations: 'idle',
    // Canvas size sync: makes it crisp on DPR changes.
    onLoad: () => {
      try { rive.resizeDrawingSurfaceToCanvas?.() } catch {}
    },
  })
  riveRef.value = rive

  const onResize = () => {
    try { rive.resizeDrawingSurfaceToCanvas?.() } catch {}
  }
  window.addEventListener('resize', onResize, { passive: true })
  removeResize = () => window.removeEventListener('resize', onResize)

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

function playIdle() {
  const rive = riveRef.value
  if (!rive) return
  activeMode.value = 'idle'
  isPlaying.value = true
  try {
    rive.stop?.()
    rive.play?.('idle')
  } catch {}
}

function playWipers() {
  const rive = riveRef.value
  if (!rive) return
  activeMode.value = 'wipers'
  isPlaying.value = true
  try {
    rive.stop?.()
    rive.play?.('windshield_wipers')
  } catch {}
}

function togglePlay() {
  const rive = riveRef.value
  if (!rive) return
  isPlaying.value = !isPlaying.value
  if (isPlaying.value) {
    if (activeMode.value === 'paused') activeMode.value = 'idle'
    try { rive.play?.() } catch {}
  } else {
    activeMode.value = 'paused'
    try { rive.pause?.() } catch {}
  }
}

onMounted(() => {
  setupRive()
})

onUnmounted(() => {
  try { io?.disconnect() } catch {}
  io = null
  try { removeResize?.() } catch {}
  removeResize = null
  const rive = riveRef.value
  if (rive) {
    try { rive.cleanup?.() } catch {}
    try { rive.pause?.() } catch {}
  }
  riveRef.value = null
})
</script>
