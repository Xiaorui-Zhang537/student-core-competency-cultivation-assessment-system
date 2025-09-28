<template>
  <div class="absolute inset-0 pointer-events-none" ref="root">
    <glass-badge
      v-for="(b, i) in badges"
      :key="i"
      :size="b.size"
      :initial-x="b.x"
      :initial-y="b.y"
      :icon="b.icon"
      :tilt-follow="true"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import GlassBadge from './GlassBadge.vue'

interface BadgeSpec {
  x: number
  y: number
  size: number
  icon?: string
}

interface Props {
  count?: number
  layout?: 'grid' | 'halo' | 'scatter'
  emphasis?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  count: 8,
  layout: 'scatter',
  emphasis: false
})

const root = ref<HTMLDivElement | null>(null)
const badges = ref<BadgeSpec[]>([])
let raf = 0

function generate(layout: Props['layout'], count: number) {
  badges.value = []
  const w = root.value?.clientWidth || window.innerWidth
  const h = root.value?.clientHeight || window.innerHeight
  const sizeBase = Math.min(w, h) * 0.06
  for (let i = 0; i < count; i++) {
    if (layout === 'halo') {
      const a = (i / count) * Math.PI * 2
      const r = Math.min(w, h) * 0.3
      badges.value.push({ x: w/2 + Math.cos(a)*r - sizeBase/2, y: h/2 + Math.sin(a)*r - sizeBase/2, size: sizeBase })
    } else if (layout === 'grid') {
      const cols = Math.ceil(Math.sqrt(count))
      const rows = Math.ceil(count / cols)
      const gx = (i % cols)
      const gy = Math.floor(i / cols)
      const margin = 48
      const cellW = (w - margin*2) / cols
      const cellH = (h - margin*2) / rows
      badges.value.push({ x: margin + gx*cellW + cellW/2 - sizeBase/2, y: margin + gy*cellH + cellH/2 - sizeBase/2, size: sizeBase })
    } else {
      // scatter
      const x = (0.15 + Math.random()*0.7) * w
      const y = (0.15 + Math.random()*0.7) * h
      badges.value.push({ x: x - sizeBase/2, y: y - sizeBase/2, size: sizeBase*(0.8 + Math.random()*0.6) })
    }
  }
}

function animate() {
  raf = requestAnimationFrame(animate)
  // gentle breathing/offset could be added here later
}

function handleResize() {
  generate(props.layout, props.count)
}

onMounted(() => {
  generate(props.layout, props.count)
  raf = requestAnimationFrame(animate)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  cancelAnimationFrame(raf)
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
</style>


