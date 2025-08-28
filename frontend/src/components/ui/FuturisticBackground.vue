<template>
  <div ref="rootRef" class="absolute inset-0 pointer-events-none select-none" :style="rootStyle" style="z-index:1">
    <!-- Disable extra layers during diagnostics -->
    <div v-if="false" ref="metaRef" class="absolute inset-0" style="z-index:5"></div>
    <div v-if="false" ref="particleRef" class="absolute inset-0" style="position:absolute;left:0;top:0;width:100%;height:100%;z-index:999;pointer-events:none"></div>
    <div v-if="false" ref="shapeRef" class="absolute inset-0" style="z-index:3"></div>
    <!-- Optional 3D layer (put at bottom) -->
    <div ref="threeRef" v-show="showLegacyWave" class="absolute inset-0" style="z-index:2"></div>
    <!-- Aurora gradient blobs (disabled in Flow Lines version) -->
    <div ref="auroraRef" class="absolute inset-0 overflow-hidden" v-if="false">
      <div v-for="n in 3" :key="`ab-${n}`" class="aurora-blob"></div>
    </div>

    <!-- Subtle grid / circuit overlay -->
    <div ref="gridRef" class="absolute inset-0" v-if="false">
      <svg class="w-full h-full" viewBox="0 0 1000 1000" preserveAspectRatio="xMidYMid slice">
        <defs>
          <linearGradient id="sweep" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" stop-color="#ffffff" stop-opacity="0" />
            <stop offset="50%" stop-color="#ffffff" stop-opacity="0.25" />
            <stop offset="100%" stop-color="#ffffff" stop-opacity="0" />
          </linearGradient>
          <mask id="sweep-mask">
            <rect :x="sweepX" y="0" width="200" height="1000" fill="url(#sweep)" />
          </mask>
        </defs>
        <g :stroke="gridStroke" stroke-width="1" fill="none" :opacity="gridOpacity">
          <g v-for="i in 22" :key="`h-${i}`"><line :x1="0" :y1="i * 46" :x2="1000" :y2="i * 46" /></g>
          <g v-for="i in 22" :key="`v-${i}`"><line :x1="i * 46" :y1="0" :x2="i * 46" :y2="1000" /></g>
        </g>
        <rect v-if="enableSweep && hasGsap" x="0" y="0" width="1000" height="1000" fill="#ffffff" mask="url(#sweep-mask)" :opacity="0.08" />
      </svg>
    </div>

    <!-- Noise texture (temporarily disabled during diagnostics) -->
    <canvas v-if="false" ref="noiseCanvas" class="absolute inset-0" style="z-index:4"></canvas>

    <!-- Unified 2D canvas (background + particles + lines) -->
    <canvas v-if="useLegacyFlow" ref="flowCanvas" class="absolute inset-0" style="z-index:2;opacity:1"></canvas>

    <!-- Bits matrix (disabled) -->
    <canvas ref="bitsCanvas" class="absolute inset-0" v-if="false"></canvas>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { getPalette, getBackgroundGradient } from './background/palettes'
import { detectTier, getCapabilities } from './background/perf-tier'
import * as MetaballsLayer from './background/MetaballsLayer'
import * as ShapeGroupLayer from './background/ShapeGroupLayer'
import * as ParticleFieldLayer from './background/ParticleFieldLayer'

interface Props {
  theme?: 'auto' | 'light' | 'dark'
  intensity?: number
  bitsDensity?: number
  sweepFrequency?: number
  parallax?: boolean
  enable3D?: boolean
  logoGlow?: boolean
  lineCount?: number
  lineThickness?: number
  paletteOverride?: string[]
  primaryHorizontalCount?: number
  verticalCount?: number
  verticalAreaStart?: number
  primaryThickness?: number
  secondaryThickness?: number
  gridSpacing?: number
  glow?: number
  gridSpeed?: number
  interactions?: { mouseTrail?: boolean; clickRipples?: boolean }
  palette?: 'youthful' | { light: string[]; dark: string[] }
  performanceTier?: 'auto' | 'high' | 'medium' | 'low'
  emphasis?: boolean
  badgeOptions?: { count?: number; layout?: 'grid' | 'halo' | 'scatter' }
  enabled?: boolean
  respectReducedMotion?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  theme: 'auto',
  intensity: 0.26,
  bitsDensity: 0.8,
  sweepFrequency: 7,
  parallax: true,
  enable3D: true,
  logoGlow: true,
  lineCount: 6,
  lineThickness: 2.2,
  paletteOverride: () => [] as string[]
  ,
  primaryHorizontalCount: 2,
  verticalCount: 8,
  verticalAreaStart: 0.72,
  primaryThickness: 12,
  secondaryThickness: 4.2,
  gridSpacing: 84,
  glow: 32,
  gridSpeed: 0.015
  ,
  interactions: () => ({ mouseTrail: true, clickRipples: true }),
  palette: 'youthful',
  performanceTier: 'auto',
  emphasis: false,
  badgeOptions: () => ({ count: 8, layout: 'scatter' as const }),
  enabled: true,
  respectReducedMotion: true
})

const rootRef = ref<HTMLDivElement | null>(null)
const auroraRef = ref<HTMLDivElement | null>(null)
const gridRef = ref<HTMLDivElement | null>(null)
const noiseCanvas = ref<HTMLCanvasElement | null>(null)
const flowCanvas = ref<HTMLCanvasElement | null>(null)
const bitsCanvas = ref<HTMLCanvasElement | null>(null)
const threeRef = ref<HTMLDivElement | null>(null)
const metaRef = ref<HTMLDivElement | null>(null)
const particleRef = ref<HTMLDivElement | null>(null)
const shapeRef = ref<HTMLDivElement | null>(null)

let gsap: any = null
const hasGsap = ref(false)
let tlAurora: any = null
let tlGrid: any = null
let bitsRaf = 0
let ctx: CanvasRenderingContext2D | null = null
let nctx: CanvasRenderingContext2D | null = null
let fctx: CanvasRenderingContext2D | null = null
let W = 0, H = 0, DPR = 1
let three: any = null
let renderer: any = null, scene: any = null, camera: any = null, mesh: any = null
let threeRaf = 0
let visHandler: any = null
let mouseHandler: any = null
let clickHandler: any = null
let mouseUpHandler: any = null

const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
const isMobile = /Mobi|Android|iPhone|iPad|iPod/i.test(navigator.userAgent)
const goodHW = (navigator.hardwareConcurrency || 2) >= 4
const enable3DComputed = computed(() => props.enable3D && !reduceMotion && !isMobile && goodHW)
const showLegacyWave = computed(() => false)
const activeAnimations = computed(() => props.enabled && (!props.respectReducedMotion || !reduceMotion))

const baseOpacity = computed(() => Math.max(0.12, Math.min(1, props.intensity + 0.1)))
// Reactively track <html> class for theme switching
const htmlHasDark = ref<boolean>(document.documentElement.classList.contains('dark'))
const dark = computed(() => props.theme === 'dark' || (props.theme === 'auto' && htmlHasDark.value))
const gridStroke = computed(() => dark.value ? 'rgba(147,197,253,0.3)' : 'rgba(59,130,246,0.22)')
const gridOpacity = computed(() => 0.35)
const enableSweep = computed(() => !reduceMotion && !isMobile)
const sweepX = ref(0)

const palette = computed<string[]>(() => {
  const p = (props.paletteOverride && props.paletteOverride.length)
    ? props.paletteOverride
    : ['#306FB3', '#0094CC', '#00B6C8', '#00D4AB', '#91EB86', '#F9F871']
  return p
})

const rootStyle = computed(() => {
  const grad = getBackgroundGradient(dark.value ? 'dark' : 'light')
  // Light mode: 明亮渐变；Dark mode: 深靛蓝渐变，不是纯黑
  return dark.value
    ? ({ background: `linear-gradient(160deg, #0A132A 0%, #0E1A39 60%, #0A132A 100%)` } as any)
    : ({ background: `linear-gradient(140deg, ${grad.from} 0%, ${grad.via} 45%, ${grad.to} 100%)` } as any)
})

const neonColors = computed(() => palette.value)

// New: palette & perf tier
const effectivePalette = computed(() => getPalette(dark.value ? 'dark' : 'light', props.palette as any))
const tier = ref<'high' | 'medium' | 'low' | 'low-static'>(
  (props.performanceTier && props.performanceTier !== 'auto')
    ? (props.performanceTier as any)
    : detectTier()
)
const caps = computed(() => getCapabilities(tier.value))

const emphasis = computed(() => props.emphasis)
// DEBUG: 强制开启旧流线，确保背景可见
const useLegacyFlow = computed(() => true)

function auroraSetup() {
  if (!gsap || !auroraRef.value) return
  const blobs = auroraRef.value.querySelectorAll('.aurora-blob') as NodeListOf<HTMLDivElement>
  blobs.forEach((el, i) => {
    el.style.position = 'absolute'
    el.style.width = '50vw'
    el.style.height = '50vw'
    el.style.filter = 'blur(48px)'
    el.style.opacity = String(0.32 - i * 0.06)
    el.style.borderRadius = '9999px'
    const colors = dark.value ? ['#60a5fa', '#a78bfa', '#22d3ee'] : ['#93c5fd', '#c4b5fd', '#7dd3fc']
    el.style.background = `radial-gradient(circle at 30% 30%, ${colors[i % colors.length]} 0%, transparent 65%)`
  })
  tlAurora = gsap.timeline({ repeat: -1, yoyo: true })
  blobs.forEach((el, i) => {
    tlAurora.to(el, { duration: 12 + i * 2, x: (i % 2 ? -1 : 1) * 80, y: (i % 3 ? 1 : -1) * 60, scale: 1.12 + i * 0.06, ease: 'sine.inOut' }, 0)
  })
}

function gridSetup() {
  if (!gsap) return
  tlGrid = gsap.timeline({ repeat: -1 })
  tlGrid.to(sweepX, { value: 900, duration: props.sweepFrequency, ease: 'sine.inOut' })
        .to(sweepX, { value: 0, duration: props.sweepFrequency, ease: 'sine.inOut' })
}

function resizeCanvas() {
  const target = rootRef.value || flowCanvas.value || noiseCanvas.value
  if (!target) return
  const rect = (target as HTMLDivElement).getBoundingClientRect?.() || { width: window.innerWidth, height: window.innerHeight }
  W = Math.max(1, Math.floor(rect?.width || window.innerWidth))
  H = Math.max(1, Math.floor(rect?.height || window.innerHeight))
  DPR = Math.min(window.devicePixelRatio || 1, 2)
  if (noiseCanvas.value) {
    noiseCanvas.value.width = Math.floor(W * DPR)
    noiseCanvas.value.height = Math.floor(H * DPR)
    noiseCanvas.value.style.width = W + 'px'
    noiseCanvas.value.style.height = H + 'px'
    nctx = noiseCanvas.value.getContext('2d')
    if (nctx) nctx.setTransform(DPR, 0, 0, DPR, 0, 0)
  }
  if (flowCanvas.value) {
    flowCanvas.value.width = Math.floor(W * DPR)
    flowCanvas.value.height = Math.floor(H * DPR)
    flowCanvas.value.style.width = W + 'px'
    flowCanvas.value.style.height = H + 'px'
    fctx = flowCanvas.value.getContext('2d')
    if (fctx) fctx.setTransform(DPR, 0, 0, DPR, 0, 0)
  }
}

// Noise: subtle film grain to add texture
function drawNoise() {
  if (!nctx) return
  const density = dark.value ? 0.035 : 0.025
  nctx.clearRect(0, 0, W, H)
  const count = Math.floor(W * H * density * 0.002)
  nctx.fillStyle = dark.value ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.03)'
  for (let i = 0; i < count; i++) {
    const x = Math.random() * W
    const y = Math.random() * H
    nctx.fillRect(x, y, 1, 1)
  }
  // subtle neon glow veil
  const g = nctx.createRadialGradient(W*0.8, H*0.2, 0, W*0.8, H*0.2, Math.max(W, H) * 0.6)
  g.addColorStop(0, (dark.value ? 'rgba(139,92,246,0.06)' : 'rgba(124,58,237,0.05)'))
  g.addColorStop(1, 'rgba(0,0,0,0)')
  nctx.fillStyle = g as any
  nctx.fillRect(0, 0, W, H)
}

function initUnifiedParticles() {
  particles = []
  // richer palette in light mode, original in dark mode
  const baseCols = effectivePalette.value
  const cols = baseCols
  // spawn initial batch from all directions (edges) and some interior
  const total = emphasis.value ? 1700 : 1000
  for (let i = 0; i < total; i++) {
    const edgeSpawn = Math.random() < 0.5
    let x = 0, y = 0, angle = 0
    if (edgeSpawn) {
      const edge = Math.floor(Math.random() * 4)
      const margin = 10
      if (edge === 0) { x = -margin; y = Math.random() * H; angle = (Math.random() * Math.PI) - Math.PI/2 }
      else if (edge === 1) { x = W + margin; y = Math.random() * H; angle = (Math.random() * Math.PI) + Math.PI/2 }
      else if (edge === 2) { x = Math.random() * W; y = -margin; angle = (Math.random() * Math.PI) + Math.PI }
      else { x = Math.random() * W; y = H + margin; angle = (Math.random() * Math.PI) }
    } else {
      x = Math.random() * W
      y = Math.random() * H
      angle = Math.random() * Math.PI * 2
    }
    const speed = 30 + Math.random() * 70
    const vx = Math.cos(angle) * speed
    const vy = Math.sin(angle) * speed
    const q = Math.random() < 0.5 ? 1 : -1
    const c = cols[i % cols.length]
    const ttl = 4 + Math.random() * 8
    const shape = randomShape()
    const s = shapeBaseScale(shape) * randomSizeJitter()
    particles.push({ x, y, vx, vy, q, r: 2.4, c, age: 0, ttl, shape, s })
  }
}

function respawnRandom(p: ParticleEx, cols?: string[]) {
  const paletteCols = cols || effectivePalette.value
  const edgeSpawn = Math.random() < 0.5
  let angle = 0
  if (edgeSpawn) {
    const edge = Math.floor(Math.random() * 4)
    const margin = 10
    if (edge === 0) { p.x = -margin; p.y = Math.random() * H; angle = (Math.random() * Math.PI) - Math.PI/2 }
    else if (edge === 1) { p.x = W + margin; p.y = Math.random() * H; angle = (Math.random() * Math.PI) + Math.PI/2 }
    else if (edge === 2) { p.x = Math.random() * W; p.y = -margin; angle = (Math.random() * Math.PI) + Math.PI }
    else { p.x = Math.random() * W; p.y = H + margin; angle = (Math.random() * Math.PI) }
  } else {
    p.x = Math.random() * W
    p.y = Math.random() * H
    angle = Math.random() * Math.PI * 2
  }
  const speed = 30 + Math.random() * 70
  p.vx = Math.cos(angle) * speed
  p.vy = Math.sin(angle) * speed
  p.q = Math.random() < 0.5 ? 1 : -1
  p.c = paletteCols[Math.floor(Math.random() * paletteCols.length)]
  p.age = 0
  p.ttl = 4 + Math.random() * 8
  p.shape = randomShape()
  p.s = shapeBaseScale(p.shape) * randomSizeJitter()
}

function stepUnified(dt: number) {
  if (!fctx) return
  // draw background gradient first
  fctx.globalCompositeOperation = 'source-over'
  if (dark.value) {
    const g = fctx.createLinearGradient(0, 0, W, H)
    g.addColorStop(0, '#0A132A')
    g.addColorStop(0.6, '#0E1A39')
    g.addColorStop(1, '#0A132A')
    fctx.fillStyle = g as any
    fctx.fillRect(0, 0, W, H)
    // subtle dark veil center to enhance particle contrast
    const rg = fctx.createRadialGradient(W * 0.5, H * 0.4, 0, W * 0.5, H * 0.4, Math.max(W, H) * 0.8)
    rg.addColorStop(0, 'rgba(12,20,40,0.35)')
    rg.addColorStop(1, 'rgba(0,0,0,0)')
    fctx.fillStyle = rg as any
    fctx.fillRect(0, 0, W, H)
  } else {
    // slightly deeper cool-blue gradient for better contrast in light mode
    const g = fctx.createLinearGradient(0, 0, W, H)
    g.addColorStop(0, '#E7EDF7')
    g.addColorStop(0.5, '#D7E4FF')
    g.addColorStop(1, '#C8D9FE')
    fctx.fillStyle = g as any
    fctx.fillRect(0, 0, W, H)
    // add a very soft vignette to lift particle contrast
    const rg = fctx.createRadialGradient(W * 0.6, H * 0.25, 0, W * 0.6, H * 0.25, Math.max(W, H) * 0.7)
    rg.addColorStop(0, 'rgba(0,0,0,0.08)')
    rg.addColorStop(1, 'rgba(0,0,0,0)')
    fctx.fillStyle = rg as any
    fctx.fillRect(0, 0, W, H)
  }

  // particles: no global electric field; stochastic jitter + random timed impulses
  tAccum += dt
  // mouse moving decay (short)
  if (tAccum - lastMouseT > 0.25) mouseMoving = false
  // schedule random events
  if (tAccum >= nextRandomEventAt) {
    const burstCount = emphasis.value ? (2 + Math.floor(Math.random() * 3)) : (1 + Math.floor(Math.random() * 3))
    for (let b = 0; b < burstCount; b++) {
      randomEvents.push({
        x: Math.random() * W,
        y: Math.random() * H,
        power: (emphasis.value ? 90 : 60) + Math.random() * (emphasis.value ? 150 : 120),
        t: 1.0,
        sign: Math.random() < 0.5 ? 1 : -1
      })
    }
    nextRandomEventAt = tAccum + (emphasis.value ? (0.3 + Math.random() * 1.0) : (0.6 + Math.random() * 1.6))
  }
  // decay pulses
  for (let k = pulses.length - 1; k >= 0; k--) { pulses[k].t -= dt; if (pulses[k].t <= 0) pulses.splice(k, 1) }
  for (let k = randomEvents.length - 1; k >= 0; k--) { randomEvents[k].t -= dt; if (randomEvents[k].t <= 0) randomEvents.splice(k, 1) }
  fctx.globalCompositeOperation = dark.value ? 'lighter' : 'source-over'
  fctx.globalAlpha = dark.value ? 1 : 0.9

  // draw mouse trail (under particles)
  if (props.interactions?.mouseTrail && trailPoints.length > 1) {
    // decay
    for (let i = trailPoints.length - 1; i >= 0; i--) {
      trailPoints[i].life -= dt * 1.6
      if (trailPoints[i].life <= 0) trailPoints.splice(i, 1)
    }
    if (trailPoints.length > 0) {
      fctx.save()
      fctx.globalCompositeOperation = dark.value ? 'lighter' : 'source-over'
      for (let i = 0; i < trailPoints.length; i++) {
        const pnt = trailPoints[i]
        const life = Math.max(0, Math.min(1, pnt.life))
        if (life <= 0.01) continue
        const rBase = dark.value ? 3.2 : 3.4
        const r = rBase * (0.6 + 0.4 * life)
        const g = fctx.createRadialGradient(pnt.x, pnt.y, 0, pnt.x, pnt.y, r)
        if (dark.value) {
          g.addColorStop(0, `hsla(200, 80%, 65%, ${0.45 * life})`)
          g.addColorStop(1, `hsla(200, 80%, 65%, 0)`) 
        } else {
          // softer lavender for light mode (boosted center opacity for clarity)
          g.addColorStop(0, `hsla(265, 55%, 68%, ${0.52 * life})`)
          g.addColorStop(1, `hsla(265, 55%, 68%, 0)`) 
        }
        fctx.fillStyle = g as any
        fctx.beginPath()
        fctx.arc(pnt.x, pnt.y, r, 0, Math.PI * 2)
        fctx.fill()
        // extra core and ring in light mode to improve readability
        if (!dark.value) {
          // small vivid core
          fctx.fillStyle = `hsla(265, 62%, 54%, ${0.28 * life})`
          fctx.beginPath()
          fctx.arc(pnt.x, pnt.y, r * 0.42, 0, Math.PI * 2)
          fctx.fill()
          // subtle outline ring
          fctx.lineWidth = 0.7
          fctx.strokeStyle = `hsla(265, 32%, 40%, ${0.16 * life})`
          fctx.beginPath()
          fctx.arc(pnt.x, pnt.y, r, 0, Math.PI * 2)
          fctx.stroke()
        }
      }
      fctx.restore()
    }
  }

  // draw click ripples (under particles)
  if (props.interactions?.clickRipples && ripples.length) {
    fctx.save()
    fctx.globalCompositeOperation = dark.value ? 'lighter' : 'source-over'
    for (let i = ripples.length - 1; i >= 0; i--) {
      const rp = ripples[i]
      rp.r += dt * (dark.value ? 320 : 260)
      rp.life -= dt * 0.8
      const alpha = Math.max(0, rp.life)
      if (alpha <= 0.02) { ripples.splice(i, 1); continue }
      // soft radial pulse fill
      const innerR = Math.max(0, rp.r * 0.55)
      const outerR = rp.r
      const grad = fctx.createRadialGradient(rp.x, rp.y, innerR, rp.x, rp.y, outerR)
      if (dark.value) {
        grad.addColorStop(0, `hsla(200, 100%, 70%, ${0.28 * alpha})`)
        grad.addColorStop(0.7, `hsla(200, 100%, 70%, ${0.12 * alpha})`)
        grad.addColorStop(1, `hsla(200, 100%, 70%, 0)`) 
      } else {
        // softer lavender pulse for light mode
        grad.addColorStop(0, `hsla(265, 55%, 70%, ${0.28 * alpha})`)
        grad.addColorStop(0.7, `hsla(265, 55%, 70%, ${0.13 * alpha})`)
        grad.addColorStop(1, `hsla(265, 55%, 70%, 0)`) 
      }
      fctx.fillStyle = grad as any
      fctx.beginPath()
      fctx.arc(rp.x, rp.y, rp.r, 0, Math.PI * 2)
      fctx.fill()
      // subtle outline in light mode for readability
      if (!dark.value) {
        fctx.lineWidth = 1
        fctx.strokeStyle = `hsla(265, 35%, 40%, ${0.16 * alpha})`
        fctx.beginPath()
        fctx.arc(rp.x, rp.y, rp.r, 0, Math.PI * 2)
        fctx.stroke()
      }
    }
    fctx.restore()
  }

  for (const p of particles) {
    // thermal-like random jitter (lower amplitude to slow motion)
    const thermal = dark.value ? 4 : 5
    p.vx += (Math.random() - 0.5) * thermal
    p.vy += (Math.random() - 0.5) * thermal
    // weak attraction toward center to avoid edge clustering
    const cx = W * 0.5, cy = H * 0.5
    const centerPull = dark.value ? 0.0018 : 0.0015
    p.vx += (cx - p.x) * centerPull
    p.vy += (cy - p.y) * centerPull
    // mouse move: cause outward scatter (not swirl). Apply to half particles (q>0)
    if (mouseMoving && p.q > 0) {
      const dxm = p.x - mouseX
      const dym = p.y - mouseY
      const r2m = dxm*dxm + dym*dym
      const falloff = Math.exp(-r2m / 36000)
      const scatterStrength = 0.085
      const len = Math.hypot(dxm, dym) || 1
      const nx = dxm / len
      const ny = dym / len
      p.vx += nx * scatterStrength * falloff
      p.vy += ny * scatterStrength * falloff
    }
    // mouse moving: add a weaker swirl while cursor is moving
    if (mouseMoving) {
      const dxm = p.x - mouseX
      const dym = p.y - mouseY
      const r2m = dxm*dxm + dym*dym
      const falloffSwirl = Math.exp(-r2m / 22000)
      const mStrengthMove = 0.12
      p.vx += (-dym) * mStrengthMove * falloffSwirl
      p.vy += (dxm) * mStrengthMove * falloffSwirl
    }
    // left mouse held: apply vortex (swirl) around cursor
    if (mouseDownLeft) {
      const dxm = p.x - mouseX
      const dym = p.y - mouseY
      const r2m = dxm*dxm + dym*dym
      const falloff = Math.exp(-r2m / 22000)
      const mStrength = 0.12
      p.vx += (-dym) * mStrength * falloff
      p.vy += (dxm) * mStrength * falloff
    }
    // apply random timed impulses (radial)
    for (let j = 0; j < randomEvents.length; j++) {
      const ev = randomEvents[j]
      const dx = p.x - ev.x
      const dy = p.y - ev.y
      const r2 = dx*dx + dy*dy
      const falloff = Math.exp(-r2 / 3800) * ev.t
      const scale = (ev.power * 0.002) * ev.sign
      p.vx += dx * falloff * scale
      p.vy += dy * falloff * scale
    }
    // magnetic pulses
    let Bz = 0
    for (let j = 0; j < pulses.length; j++) {
      const imp = pulses[j]
      const dx = p.x - imp.x
      const dy = p.y - imp.y
      const r2 = dx*dx + dy*dy
      const w = Math.exp(-r2 / 2200) * imp.t * imp.sign
      Bz += w * imp.power
    }
    const axB = p.q * (p.vy * Bz)
    const ayB = p.q * (-p.vx * Bz)
    p.vx += axB * dt
    p.vy += ayB * dt
    if (!Number.isFinite(p.vx) || Math.abs(p.vx) > 2000) p.vx = (Math.random() - 0.5) * 200
    if (!Number.isFinite(p.vy) || Math.abs(p.vy) > 2000) p.vy = (Math.random() - 0.5) * 200
    p.vx *= 0.988; p.vy *= 0.988
    p.x += p.vx * dt
    p.y += p.vy * dt
    p.age += dt
    if (p.age > p.ttl) {
      respawnRandom(p)
    }
    const margin = 12
    if (p.x < -margin) p.x = W + margin
    if (p.x > W + margin) p.x = -margin
    if (p.y < -margin) p.y = H + margin
    if (p.y > H + margin) p.y = -margin

    // meteor-like streak behind particle based on velocity
    {
      const speed = Math.hypot(p.vx, p.vy)
      if (speed > 2) {
        const nx = p.vx / speed
        const ny = p.vy / speed
        const tail = Math.max(8, Math.min(dark.value ? 48 : 36, speed * (dark.value ? 0.08 : 0.06)))
        const sx = p.x - nx * tail
        const sy = p.y - ny * tail
        fctx.save()
        fctx.lineCap = 'round'
        fctx.strokeStyle = p.c
        fctx.globalAlpha = (dark.value ? 0.45 : 0.35) * Math.min(1, speed / 220)
        fctx.lineWidth = (dark.value ? 2.0 : 1.4)
        fctx.beginPath()
        fctx.moveTo(sx, sy)
        fctx.lineTo(p.x, p.y)
        fctx.stroke()
        fctx.restore()
      }
    }

    // draw particle shape
    const rr = (dark.value ? 2.6 : 3.2) * (p.s || 1)
    fctx.fillStyle = p.c
    const angle = Math.atan2(p.vy, p.vx)
    switch (p.shape) {
      case 'circle': {
        fctx.beginPath(); fctx.arc(p.x, p.y, rr, 0, Math.PI*2); fctx.fill();
        break
      }
      case 'triangle': {
        drawRegularPolygon(fctx, p.x, p.y, rr, 3, angle - Math.PI/2)
        fctx.fill();
        break
      }
      case 'square': {
        drawRegularPolygon(fctx, p.x, p.y, rr, 4, angle + Math.PI/4)
        fctx.fill();
        break
      }
      case 'hex': {
        drawRegularPolygon(fctx, p.x, p.y, rr, 6, angle)
        fctx.fill();
        break
      }
      case 'star': {
        drawStar(fctx, p.x, p.y, rr, rr * 0.5, 5, angle)
        fctx.fill();
        break
      }
    }
    // subtle outline in light mode for readability
    if (!dark.value) {
      fctx.lineWidth = 0.6
      fctx.strokeStyle = 'rgba(0,0,0,0.18)'
      fctx.beginPath()
      if (p.shape === 'circle') {
        fctx.arc(p.x, p.y, rr, 0, Math.PI*2)
      } else if (p.shape === 'triangle') {
        drawRegularPolygon(fctx, p.x, p.y, rr, 3, angle - Math.PI/2)
      } else if (p.shape === 'square') {
        drawRegularPolygon(fctx, p.x, p.y, rr, 4, angle + Math.PI/4)
      } else if (p.shape === 'hex') {
        drawRegularPolygon(fctx, p.x, p.y, rr, 6, angle)
      } else if (p.shape === 'star') {
        drawStar(fctx, p.x, p.y, rr, rr * 0.5, 5, angle)
      }
      fctx.stroke()
    }
  }
}

// Flow lines
type VLine = { x: number; color: string; width: number }
type HLine = { y: number; color: string; width: number }
let vLines: VLine[] = []
let hLines: HLine[] = []
let gridOffsetX = 0
let gridOffsetY = 0

// Unified 2D particles on flowCanvas
type Particle = { x: number; y: number; vx: number; vy: number; q: number; r: number; c: string; age: number; ttl: number; shape: 'circle' | 'triangle' | 'square' | 'hex' | 'star' }
// add size scale per particle
type ParticleEx = Particle & { s: number }
let particles: ParticleEx[] = []
let pulses: Array<{ x: number; y: number; power: number; t: number; sign: number }> = []
let tAccum = 0
// random impulses besides mouse clicks
let randomEvents: Array<{ x: number; y: number; power: number; t: number; sign: number }> = []
let nextRandomEventAt = 0
// mouse influence (continuous steering)
let mouseX = 0
let mouseY = 0
let mouseMoving = false
let lastMouseT = 0
let mouseDownLeft = false
// mouse trail and click ripples (visuals)
let trailPoints: Array<{ x:number; y:number; life:number }> = []
let ripples: Array<{ x:number; y:number; r:number; life:number; maxR:number }> = []
const TRAIL_MAX_POINTS = 140

// particle shape helpers
function randomShape(): Particle['shape'] {
  const r = Math.random()
  if (r < 0.55) return 'circle'
  if (r < 0.70) return 'triangle'
  if (r < 0.85) return 'square'
  if (r < 0.96) return 'hex'
  return 'star'
}

function shapeBaseScale(shape: Particle['shape']): number {
  switch (shape) {
    case 'circle': return 1.0
    case 'triangle': return 1.15
    case 'square': return 1.1
    case 'hex': return 1.25
    case 'star': return 1.35
    default: return 1.0
  }
}

function randomSizeJitter(): number {
  return 0.9 + Math.random() * 0.5 // 0.9 ~ 1.4
}

function drawRegularPolygon(ctx: CanvasRenderingContext2D, cx: number, cy: number, radius: number, sides: number, angle: number) {
  if (sides < 3) return
  ctx.beginPath()
  for (let i = 0; i < sides; i++) {
    const a = angle + (i * 2 * Math.PI / sides)
    const x = cx + Math.cos(a) * radius
    const y = cy + Math.sin(a) * radius
    if (i === 0) ctx.moveTo(x, y)
    else ctx.lineTo(x, y)
  }
  ctx.closePath()
}

function drawStar(ctx: CanvasRenderingContext2D, cx: number, cy: number, outerR: number, innerR: number, points: number, angle: number) {
  ctx.beginPath()
  const step = Math.PI / points
  for (let i = 0; i < 2 * points; i++) {
    const r = (i % 2 === 0) ? outerR : innerR
    const a = angle + i * step
    const x = cx + Math.cos(a) * r
    const y = cy + Math.sin(a) * r
    if (i === 0) ctx.moveTo(x, y)
    else ctx.lineTo(x, y)
  }
  ctx.closePath()
}

// color helpers for richer palette in light mode
function hexToRgb(hex: string) {
  const c = hex.replace('#','')
  const n = parseInt(c.length===3? c.split('').map(x=>x+x).join('') : c, 16)
  return { r: (n>>16)&255, g: (n>>8)&255, b: n&255 }
}
function rgbToHsl(r: number,g: number,b: number) {
  r/=255; g/=255; b/=255
  const max=Math.max(r,g,b), min=Math.min(r,g,b)
  let h=0,s=0
  const l=(max+min)/2
  const d=max-min
  if (d!==0){
    s = l>0.5 ? d/(2-max-min) : d/(max+min)
    switch(max){
      case r: h=(g-b)/d+(g<b?6:0); break
      case g: h=(b-r)/d+2; break
      default: h=(r-g)/d+4
    }
    h/=6
  }
  return { h:h*360, s:s*100, l:l*100 }
}
function clamp(v:number,min:number,max:number){ return Math.min(max,Math.max(min,v)) }
function jitter(v:number,amt:number){ return v + (Math.random()*2-1)*amt }
function hslCss(h:number,s:number,l:number,alpha=1){ return `hsla(${Math.round(h)}, ${Math.round(s)}%, ${Math.round(l)}%, ${alpha})` }
function buildParticleColorsLight(baseHexList: string[]): string[] {
  const out: string[] = []
  for (const hex of baseHexList) {
    const {r,g,b} = hexToRgb(hex)
    const {h,s,l} = rgbToHsl(r,g,b)
    // lighter pastel variants around each base hue
    const offsets = [-16, -6, 6, 16]
    for (let k=0;k<offsets.length;k++){
      const hh = (h + offsets[k] + (Math.random()*8-4) + 360)%360
      const ss = clamp(jitter(Math.min(72, Math.max(55, s - 12)), 6), 55, 72) // moderate saturation
      const ll = clamp(jitter(Math.max(74, l + 12), 6), 78, 90)               // higher lightness
      out.push(hslCss(hh, ss, ll))
    }
  }
  // gentle warm pastels (lighter)
  const warmPastels = [
    hslCss(38, 70, 84), // amber pastel light
    hslCss(26, 68, 86), // orange pastel light
    hslCss(15, 66, 84), // coral pastel light
    hslCss(48, 72, 86)  // gold pastel light
  ]
  for (let i=0;i<6;i++) out.push(warmPastels[i % warmPastels.length])
  // multi-hue pastel accents (light)
  const pastelAccents = [
    hslCss(0, 68, 84),    // red pastel
    hslCss(330, 66, 86),  // magenta pastel
    hslCss(280, 64, 86),  // purple pastel
    hslCss(210, 70, 82),  // azure pastel
    hslCss(190, 68, 82),  // cyan pastel
    hslCss(160, 66, 80),  // emerald pastel
    hslCss(140, 64, 82),  // green pastel
    hslCss(90, 72, 84),   // lime pastel
    hslCss(55, 76, 84),   // yellow pastel
    hslCss(30, 76, 84)    // orange pastel
  ]
  for (let i=0;i<10;i++) out.push(pastelAccents[i % pastelAccents.length])
  return out
}

function initLinesOrthogonal() {
  const colors = neonColors.value
  vLines = []
  hLines = []
  // horizontal primary lines (1-2 very thick)
  const countH = Math.max(1, Math.min(2, props.primaryHorizontalCount || 2))
  for (let i = 0; i < countH; i++) {
    const y = H * (0.35 + i * 0.18)
    hLines.push({ y, color: colors[(i + 1) % colors.length], width: (props.primaryThickness || 8) })
  }
  // vertical lines cluster on the right area
  const startX = W * (props.verticalAreaStart || 0.62)
  const vcount = Math.max(3, props.verticalCount || 8)
  const spacing = Math.max(48, (props.gridSpacing as any) || 84)
  for (let i = 0; i < vcount; i++) {
    const x = startX + i * (spacing * 0.6)
    const w = (props.secondaryThickness || 3.2) * (1 + (i % 2) * 0.2)
    vLines.push({ x, color: colors[(i + 2) % colors.length], width: w })
  }
}

function drawNeonLines(t: number) {
  if (!fctx) return
  fctx.clearRect(0, 0, W, H)
  fctx.save()
  // tiny parallax only on the canvas content
  fctx.translate(parallaxX, parallaxY)

  const alpha = Math.min(1, (props.intensity || 0.22) + 0.08)
  const glow = (props.glow as any) || 26
  const speed = (props.gridSpeed as any) || 0.015
  gridOffsetX = (gridOffsetX + speed) % (W)
  gridOffsetY = (gridOffsetY + speed * 0.6) % (H)

  // horizontal primary with soft glow halo (draw wide glow first)
  for (const h of hLines) {
    fctx.globalCompositeOperation = 'lighter'
    fctx.shadowColor = h.color
    fctx.shadowBlur = glow * 1.4
    fctx.strokeStyle = h.color
    fctx.globalAlpha = alpha
    fctx.lineWidth = h.width * 2.2
    fctx.beginPath()
    fctx.moveTo(0, (h.y + gridOffsetY * 0.2) % H)
    fctx.lineTo(W, (h.y + gridOffsetY * 0.2) % H)
    fctx.stroke()
    // core
    fctx.shadowBlur = glow
    fctx.lineWidth = h.width
    fctx.beginPath()
    fctx.moveTo(0, (h.y + gridOffsetY * 0.2) % H)
    fctx.lineTo(W, (h.y + gridOffsetY * 0.2) % H)
    fctx.stroke()
  }

  // vertical cluster (slightly moving right)
  for (let i = 0; i < vLines.length; i++) {
    const v = vLines[i]
    const x = (v.x + gridOffsetX * 0.4) % (W + 100)
    fctx.globalCompositeOperation = 'lighter'
    fctx.shadowColor = v.color
    fctx.shadowBlur = glow
    fctx.strokeStyle = v.color
    fctx.globalAlpha = alpha * 0.95
    fctx.lineWidth = v.width
    fctx.beginPath()
    fctx.moveTo(x, 0)
    fctx.lineTo(x, H)
    fctx.stroke()
  }

  fctx.restore()
}

async function setup3D() {
  if (!enable3DComputed.value || !threeRef.value) return
  try {
    three = await import('three')
    const { WebGLRenderer, Scene, PerspectiveCamera, PlaneGeometry, MeshStandardMaterial, Mesh, DirectionalLight, Color } = three as any
    renderer = new WebGLRenderer({ antialias: true, alpha: true })
    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
    renderer.setSize(W, H)
    // Ensure canvas fills container visually
    renderer.domElement.style.width = '100%'
    renderer.domElement.style.height = '100%'
    threeRef.value.appendChild(renderer.domElement)
    scene = new Scene()
    scene.background = null
    camera = new PerspectiveCamera(45, W / H, 0.1, 100)
    camera.position.set(0, 0, 8)
    const geo = new PlaneGeometry(18, 10, 64, 32)
    const mat = new MeshStandardMaterial({ color: new Color(0x274472), wireframe: false, roughness: 0.8, metalness: 0.1 })
    mesh = new Mesh(geo, mat)
    scene.add(mesh)
    const light = new DirectionalLight(0x87b5ff, 1.2)
    light.position.set(2, 3, 5)
    scene.add(light)

    const mod = (x: number, m: number) => ((x % m) + m) % m
    let t = 0
    const render = () => {
      threeRaf = requestAnimationFrame(render)
      t += 0.01
      const pos = (mesh.geometry as any).attributes.position
      for (let i = 0; i < pos.count; i++) {
        const ix = pos.getX(i)
        const iy = pos.getY(i)
        const wave = Math.sin(ix * 0.6 + t) * Math.cos(iy * 0.8 + t * 1.2)
        pos.setZ(i, wave * 0.25)
      }
      pos.needsUpdate = true
      renderer.render(scene, camera)
    }
    render()

    if (gsap) {
      gsap.to(mesh.rotation, { x: 0.02, y: -0.02, duration: 6, repeat: -1, yoyo: true, ease: 'sine.inOut' })
    }
  } catch {
    // fail silent; 3D disabled
  }
}

function destroy3D() {
  cancelAnimationFrame(threeRaf)
  if (renderer && threeRef.value) {
    try { threeRef.value.removeChild(renderer.domElement) } catch {}
  }
  renderer = null; scene = null; camera = null; mesh = null
}

let resizeTimer: number | undefined
let flowRaf = 0
let parallaxX = 0
let parallaxY = 0
let metaHandle: ReturnType<typeof MetaballsLayer.create> | null = null
let shapeHandle: ReturnType<typeof ShapeGroupLayer.create> | null = null
let particleHandle: ReturnType<typeof ParticleFieldLayer.create> | null = null

function handleResize() {
  window.clearTimeout(resizeTimer)
  resizeTimer = window.setTimeout(() => {
    resizeCanvas()
    // legacy lines init removed
    drawNoise()
    if (renderer) {
      renderer.setSize(W, H)
      if (camera) { (camera as any).aspect = W / H; (camera as any).updateProjectionMatrix() }
    }
    if (metaHandle && metaRef.value) {
      const rect = metaRef.value.getBoundingClientRect()
      metaHandle.resize(rect.width, rect.height)
    }
    if (shapeHandle && shapeRef.value) {
      const rect = shapeRef.value.getBoundingClientRect()
      shapeHandle.resize(rect.width, rect.height)
    }
  }, 120)
}

function handleVisibility() {
  if (document.hidden) {
    if (tlAurora) tlAurora.pause()
    if (tlGrid) tlGrid.pause()
  } else {
    if (tlAurora) tlAurora.play()
    if (tlGrid) tlGrid.play()
  }
}

function handleMouse(e: MouseEvent) {
  if (!props.parallax || reduceMotion || !rootRef.value) return
  const cx = window.innerWidth / 2
  const cy = window.innerHeight / 2
  parallaxX = (e.clientX - cx) * 0.006
  parallaxY = (e.clientY - cy) * 0.006
  // record mouse for continuous steering
  mouseX = e.clientX
  mouseY = e.clientY
  // treat as moving only if delta is non-trivial
  const mv = Math.hypot((e as any).movementX || 0, (e as any).movementY || 0)
  if (mv > 0.5) {
    mouseMoving = true
    lastMouseT = tAccum
  }
  if (props.interactions?.mouseTrail) {
    trailPoints.push({ x: e.clientX, y: e.clientY, life: 1 })
    if (trailPoints.length > TRAIL_MAX_POINTS) trailPoints.splice(0, trailPoints.length - TRAIL_MAX_POINTS)
  }
}

onMounted(async () => {
  // Defer to next frame to ensure layout is ready
  requestAnimationFrame(() => resizeCanvas())
  if (activeAnimations.value) {
    try { gsap = (await import('gsap')).gsap || (await import('gsap')).default } catch {}
    if (gsap) {
      hasGsap.value = true
      // no aurora/grid in Flow Lines version
    }
    // init static layers (neon lines disabled)
    // initLinesOrthogonal()
    drawNoise()
    // unified loop: particles only (neon lines removed)
    if (useLegacyFlow.value) {
      initUnifiedParticles()
    const tick = (now: number) => {
        flowRaf = requestAnimationFrame(tick)
        const dt = 1/60
        stepUnified(dt)
        // drawNeonLines(now * 0.001)
      }
      flowRaf = requestAnimationFrame(tick)
    }
    if (showLegacyWave.value) await setup3D()
    // Remove metaballs to avoid occlusion for now
    // Particle background (main)
    if (particleRef.value) {
      const r0 = particleRef.value.getBoundingClientRect()
      particleHandle = ParticleFieldLayer.create(particleRef.value, {
        palette: effectivePalette.value,
        theme: dark.value ? 'dark' : 'light',
        tier: tier.value,
        emphasis: emphasis.value
      })
      particleHandle.mount()
      particleHandle.resize(r0.width, r0.height)
      // Debug: if not ready, log dimensions to help diagnose blank screen
      setTimeout(() => {
        if (!particleHandle?.isReady?.()) {
          const r = particleRef.value?.getBoundingClientRect()
          console.warn('[ParticleLayer] not ready. container rect=', r)
          particleHandle?.fallback2D?.()
        }
      }, 200)
    }
    // Mount shape group (A方案)
    if (shapeRef.value) {
      const rect2 = shapeRef.value.getBoundingClientRect()
      shapeHandle = ShapeGroupLayer.create(shapeRef.value, {
        palette: effectivePalette.value,
        theme: dark.value ? 'dark' : 'light',
        tier: tier.value,
        emphasis: emphasis.value
      })
      shapeHandle.mount()
      shapeHandle.resize(rect2.width, rect2.height)
    }
  } else {
    // Reduced motion or disabled: draw static background once
    drawNoise()
    if (fctx) {
      fctx.globalCompositeOperation = 'source-over'
      if (dark.value) {
        const g = fctx.createLinearGradient(0, 0, W, H)
        g.addColorStop(0, '#0A132A')
        g.addColorStop(0.6, '#0E1A39')
        g.addColorStop(1, '#0A132A')
        fctx.fillStyle = g as any
      } else {
        const g = fctx.createLinearGradient(0, 0, W, H)
        g.addColorStop(0, '#E7EDF7')
        g.addColorStop(0.5, '#D7E4FF')
        g.addColorStop(1, '#C8D9FE')
        fctx.fillStyle = g as any
      }
      fctx.fillRect(0, 0, W, H)
    }
  }
  // Always mount particles and shapes regardless of reduced motion (user requires visuals) — disabled in accessibility mode
  if (activeAnimations.value && particleRef.value && !particleHandle) {
    const r0 = particleRef.value.getBoundingClientRect()
    particleHandle = ParticleFieldLayer.create(particleRef.value, {
      palette: effectivePalette.value,
      theme: dark.value ? 'dark' : 'light',
      tier: tier.value,
      emphasis: emphasis.value
    })
    particleHandle.mount()
    particleHandle.resize(r0.width || window.innerWidth, r0.height || window.innerHeight)
    // TEMP: force 2D fallback to guarantee visibility while diagnosing
    particleHandle?.fallback2D?.()
    setTimeout(() => {
      if (!particleHandle?.isReady?.()) {
        const r = particleRef.value?.getBoundingClientRect()
        console.warn('[ParticleLayer] not ready (force fallback). container rect=', r)
        particleHandle?.fallback2D?.()
      }
    }, 200)
    // If still no canvas found in DOM, force a minimal debug canvas to verify rendering
    setTimeout(() => {
      if (!document.getElementById('particle-fallback')) {
        try {
          const c = document.createElement('canvas')
          c.id = 'particle-fallback-force'
          c.style.position = 'fixed'
          c.style.left = '0'
          c.style.top = '0'
          c.style.width = '100%'
          c.style.height = '100%'
          c.style.zIndex = '2147483647'
          c.style.pointerEvents = 'none'
          const DPR = Math.min(window.devicePixelRatio || 1, 2)
          c.width = Math.max(1, Math.floor(window.innerWidth * DPR))
          c.height = Math.max(1, Math.floor(window.innerHeight * DPR))
          document.body.appendChild(c)
          const ctx = c.getContext('2d')
          if (ctx) {
            ctx.setTransform(DPR, 0, 0, DPR, 0, 0)
            let t0 = performance.now()
            const dots = Array.from({ length: 300 }, () => ({
              x: Math.random() * window.innerWidth,
              y: Math.random() * window.innerHeight,
              vx: (Math.random() - 0.5) * 120,
              vy: (Math.random() - 0.5) * 120,
              c: `hsl(${Math.random()*60+180} 100% 70%)`
            }))
            const loop = () => {
              const t = performance.now()
              const dt = Math.min(0.033, (t - t0) / 1000)
              t0 = t
              ctx.clearRect(0, 0, window.innerWidth, window.innerHeight)
              ctx.fillStyle = '#0ff'; ctx.fillRect(10, 10, 200, 8)
              ctx.fillStyle = '#fff'; ctx.fillRect(10, 24, 120, 6)
              ctx.globalCompositeOperation = 'lighter'
              for (const p of dots) {
                p.x += p.vx * dt; p.y += p.vy * dt
                p.vx *= 0.995; p.vy *= 0.995
                if (p.x < 0) p.x += window.innerWidth; if (p.x > window.innerWidth) p.x -= window.innerWidth
                if (p.y < 0) p.y += window.innerHeight; if (p.y > window.innerHeight) p.y -= window.innerHeight
                ctx.fillStyle = p.c
                ctx.beginPath(); ctx.arc(p.x, p.y, 2.6, 0, Math.PI*2); ctx.fill()
              }
              requestAnimationFrame(loop)
            }
            loop()
          }
        } catch (e) {
          console.warn('force debug canvas failed', e)
        }
      }
    }, 500)
  }
  if (activeAnimations.value && shapeRef.value && !shapeHandle) {
    const rect2 = shapeRef.value.getBoundingClientRect()
    shapeHandle = ShapeGroupLayer.create(shapeRef.value, {
      palette: effectivePalette.value,
      theme: dark.value ? 'dark' : 'light',
      tier: tier.value,
      emphasis: emphasis.value
    })
    shapeHandle.mount()
    shapeHandle.resize(rect2.width || window.innerWidth, rect2.height || window.innerHeight)
  }

  window.addEventListener('resize', handleResize)
  visHandler = () => handleVisibility()
  document.addEventListener('visibilitychange', visHandler)
  // Observe <html> class changes to update dark mode reactively
  try {
    const mo = new MutationObserver(() => {
      htmlHasDark.value = document.documentElement.classList.contains('dark')
    })
    mo.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] })
  } catch {}
  mouseHandler = (e: MouseEvent) => handleMouse(e)
  window.addEventListener('mousemove', mouseHandler, { passive: true })
  // metaballs removed
  // forward to particle field (background)
  window.addEventListener('mousemove', (e: MouseEvent) => {
    if (!particleHandle || !particleRef.value) return
    const rect = particleRef.value.getBoundingClientRect()
    const x = ((e.clientX - rect.left) / rect.width) * 2 - 1
    const y = -(((e.clientY - rect.top) / rect.height) * 2 - 1)
    particleHandle.setPointer(x, y)
  }, { passive: true })
  // forward to shape group
  window.addEventListener('mousemove', (e: MouseEvent) => {
    if (!shapeHandle || !shapeRef.value) return
    const rect = shapeRef.value.getBoundingClientRect()
    const x = ((e.clientX - rect.left) / rect.width) * 2 - 1
    const y = -(((e.clientY - rect.top) / rect.height) * 2 - 1)
    shapeHandle.setPointer(x, y)
  }, { passive: true })
  clickHandler = (e: MouseEvent) => {
    if (e.button !== 0) return
    // left-click: register vortex-like magnetic pulse
    pulses.push({ x: e.clientX, y: e.clientY, power: 20, t: 1.0, sign: (Math.random()<0.5?1:-1) })
    mouseDownLeft = true
    if (props.interactions?.clickRipples) {
      ripples.push({ x: e.clientX, y: e.clientY, r: 0, life: 1, maxR: Math.max(W, H) * 0.6 })
    }
  }
  window.addEventListener('mousedown', clickHandler, { passive: true })
  mouseUpHandler = (e: MouseEvent) => {
    if (e.button !== 0) return
    mouseDownLeft = false
  }
  window.addEventListener('mouseup', mouseUpHandler, { passive: true })
})

onUnmounted(() => {
  cancelAnimationFrame(bitsRaf)
  cancelAnimationFrame(flowRaf)
  cancelAnimationFrame(threeRaf)
  if (tlAurora) tlAurora.kill()
  if (tlGrid) tlGrid.kill()
  window.removeEventListener('resize', handleResize)
  if (visHandler) document.removeEventListener('visibilitychange', visHandler)
  if (mouseHandler) window.removeEventListener('mousemove', mouseHandler)
  if (clickHandler) window.removeEventListener('mousedown', clickHandler)
  if (mouseUpHandler) window.removeEventListener('mouseup', mouseUpHandler)
  destroy3D()
  // if (metaHandle) metaHandle.unmount()
  if (shapeHandle) shapeHandle.unmount()
})
</script>

<style scoped>
.aurora-blob {
  mix-blend-mode: screen;
}
</style>


