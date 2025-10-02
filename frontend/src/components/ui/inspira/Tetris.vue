<template>
  <Transition appear name="fade">
    <div
      :style="{
        '--cell-size': `${cellSize}px`,
        '--grid-rows': Math.max(1, rows - 1),
      } as any"
      :class="cn('relative w-full h-full', props.class)"
    >
      <div
        ref="el"
        class="absolute inset-0 grid justify-center -space-y-px"
        :style="{ gridTemplateRows: `repeat(var(--grid-rows), var(--cell-size))` } as any"
      >
        <div
          v-for="(row, rowIndex) in grid"
          :key="rowIndex"
          class="grid flex-1 grid-flow-col -space-x-px"
          :style="{ gridTemplateColumns: `repeat(${cols}, var(--cell-size))` } as any"
        >
          <div
            v-for="(cell, cellIndex) in row"
            :key="cellIndex"
            :style="{
              '--border-light': theme[100],
              '--border-dark': theme[900],
              '--square-light': theme[500],
              '--square-hover-light': theme[400],
              '--square-dark': theme[700],
              '--square-hover-dark': theme[600],
            } as any"
            class="relative border border-[color:var(--border-light)] dark:border-[color:var(--border-dark)] cursor-pointer"
          >
            <div
              class="absolute inset-0 bg-[color:var(--square-light)] opacity-0 transition-opacity duration-500 will-change-[opacity] hover:bg-[color:var(--square-hover-light)] dark:bg-[color:var(--square-dark)] dark:hover:bg-[color:var(--square-hover-dark)]"
              :class="[cell && 'opacity-60']"
              @click.stop="cell && removeCell(rowIndex, cellIndex)"
            />
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { useElementSize } from '@vueuse/core'
import { cn } from '@/lib/utils'
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'

interface Props {
  class?: string
  squareColor?: string
  base?: number
}

const props = withDefaults(defineProps<Props>(), {
  base: 24,
  squareColor: '#60a5fa',
})

type ShadeMap = Record<100 | 400 | 500 | 600 | 700 | 900, string>

function clamp01(v: number) { return Math.max(0, Math.min(1, v)) }
function hexToRgb(hex: string) {
  const s = hex.replace('#', '')
  const n = parseInt(s.length === 3 ? s.split('').map(c => c + c).join('') : s, 16)
  return { r: (n >> 16) & 255, g: (n >> 8) & 255, b: n & 255 }
}
function rgbToHex(r: number, g: number, b: number) {
  const to = (x: number) => x.toString(16).padStart(2, '0')
  return `#${to(r)}${to(g)}${to(b)}`
}
function rgbToHsl(r: number, g: number, b: number) {
  r /= 255; g /= 255; b /= 255
  const max = Math.max(r, g, b), min = Math.min(r, g, b)
  let h = 0, s = 0
  const l = (max + min) / 2
  const d = max - min
  if (d !== 0) {
    s = l > 0.5 ? d / (2 - max - min) : d / (max + min)
    switch (max) {
      case r: h = (g - b) / d + (g < b ? 6 : 0); break
      case g: h = (b - r) / d + 2; break
      case b: h = (r - g) / d + 4; break
    }
    h /= 6
  }
  return { h, s, l }
}
function hslToRgb(h: number, s: number, l: number) {
  let r: number, g: number, b: number
  if (s === 0) { r = g = b = l } else {
    const hue2rgb = (p: number, q: number, t: number) => {
      if (t < 0) t += 1
      if (t > 1) t -= 1
      if (t < 1/6) return p + (q - p) * 6 * t
      if (t < 1/2) return q
      if (t < 2/3) return p + (q - p) * (2/3 - t) * 6
      return p
    }
    const q = l < 0.5 ? l * (1 + s) : l + s - l * s
    const p = 2 * l - q
    r = hue2rgb(p, q, h + 1/3)
    g = hue2rgb(p, q, h)
    b = hue2rgb(p, q, h - 1/3)
  }
  return { r: Math.round(r * 255), g: Math.round(g * 255), b: Math.round(b * 255) }
}
function adjust(hex: string, dl: number): string {
  const { r, g, b } = hexToRgb(hex)
  const hsl = rgbToHsl(r, g, b)
  const l = clamp01(hsl.l + dl)
  const { r: nr, g: ng, b: nb } = hslToRgb(hsl.h, hsl.s, l)
  return rgbToHex(nr, ng, nb)
}

function buildTheme(base: string): ShadeMap {
  // create subtle shades around base color
  return {
    100: adjust(base, 0.38),
    400: adjust(base, 0.16),
    500: adjust(base, 0.0),
    600: adjust(base, -0.06),
    700: adjust(base, -0.12),
    900: adjust(base, -0.26),
  }
}

const theme = computed<ShadeMap>(() => buildTheme(props.squareColor || '#60a5fa'))

const el = ref<HTMLElement | null>(null)
const grid = ref<(boolean | null)[][]>([])
const rows = ref(0)
const cols = ref(0)

const { width, height } = useElementSize(el)
const cellSize = computed(() => (cols.value > 0 ? width.value / cols.value : 0))

function createGrid() {
  grid.value = []
  for (let i = 0; i < rows.value; i++) {
    grid.value.push(new Array(cols.value).fill(null))
  }
}

function createNewCell() {
  if (cols.value <= 0 || rows.value <= 0) return
  const x = Math.floor(Math.random() * cols.value)
  grid.value[0][x] = true
}

function moveCellsDown() {
  for (let row = rows.value - 1; row >= 0; row--) {
    for (let col = 0; col < cols.value; col++) {
      const cell = grid.value[row][col]
      const nextCell = Array.isArray(grid.value[row + 1]) ? grid.value[row + 1][col] : cell
      if (cell !== null && nextCell === null) {
        grid.value[row + 1][col] = grid.value[row][col]
        grid.value[row][col] = null
      }
    }
  }

  setTimeout(() => {
    if (rows.value <= 0) return
    const isFilled = grid.value[rows.value - 1].every((cell) => cell !== null)
    if (Array.isArray(grid.value[rows.value]) && isFilled) {
      for (let col = 0; col < cols.value; col++) {
        grid.value[rows.value][col] = null
      }
    }
  }, 500)
}

function clearColumn() {
  if (rows.value <= 0) return
  const isFilled = grid.value[rows.value - 1].every((cell) => cell === true)
  if (!isFilled) return
  for (let col = 0; col < cols.value; col++) {
    grid.value[rows.value - 1][col] = null
  }
}

function removeCell(row: number, col: number) {
  if (!grid.value[row]) return
  grid.value[row][col] = null
}

function calcGrid() {
  const cell = Math.max(6, width.value / (props.base || 24))
  rows.value = Math.max(1, Math.floor(height.value / cell))
  cols.value = Math.max(1, Math.floor(width.value / cell))
  createGrid()
}

watch(width, calcGrid)
watch(height, calcGrid)

let intervalId: number | undefined
let timeoutId: number | undefined

onMounted(() => {
  timeoutId = window.setTimeout(calcGrid, 50)
  intervalId = window.setInterval(() => {
    clearColumn()
    moveCellsDown()
    createNewCell()
  }, 1000)
  try {
    window.addEventListener('click', handleGlobalClick, { passive: true })
  } catch {}
})

onUnmounted(() => {
  if (intervalId) window.clearInterval(intervalId)
  if (timeoutId) window.clearTimeout(timeoutId)
  try { window.removeEventListener('click', handleGlobalClick as any) } catch {}
})

function handleGlobalClick(ev: MouseEvent) {
  const root = el.value as HTMLElement | null
  if (!root) return
  const rect = root.getBoundingClientRect()
  const x = ev.clientX
  const y = ev.clientY
  if (x < rect.left || x > rect.right || y < rect.top || y > rect.bottom) return
  const size = cellSize.value || 1
  const c = Math.floor((x - rect.left) / size)
  const r = Math.floor((y - rect.top) / size)
  if (!grid.value[r]) return
  grid.value[r][c] = null
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>


