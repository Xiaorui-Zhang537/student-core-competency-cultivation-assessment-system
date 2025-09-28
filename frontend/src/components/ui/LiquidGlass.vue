<template>
  <div
    ref="liquidGlassRoot"
    class="effect"
    :class="[props.containerClass]"
    :style="baseStyle"
  >
    <div :class="['slot-container', props.class]">
      <slot />
    </div>

    <svg
      class="filter"
      xmlns="http://www.w3.org/2000/svg"
    >
      <defs>
        <filter
          id="displacementFilter"
          color-interpolation-filters="sRGB"
        >
          <feImage
            x="0"
            y="0"
            width="100%"
            height="100%"
            :href="displacementDataUri"
            result="map"
          />
          <!-- 使用单次位移，保留折射而不产生色散（不再拆分 RGB 通道） -->
          <feDisplacementMap
            in="SourceGraphic"
            in2="map"
            :xChannelSelector="xChannel"
            :yChannelSelector="yChannel"
            :scale="scale"
            result="output"
          />
        </filter>
      </defs>
    </svg>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, type HTMLAttributes } from 'vue'

interface Props {
  radius?: number
  border?: number
  lightness?: number
  displace?: number
  blend?: string
  xChannel?: 'R' | 'G' | 'B'
  yChannel?: 'R' | 'G' | 'B'
  alpha?: number
  blur?: number
  rOffset?: number
  gOffset?: number
  bOffset?: number
  scale?: number
  frost?: number
  class?: HTMLAttributes['class']
  containerClass?: HTMLAttributes['class']
  tint?: boolean
  bgTransparent?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  radius: 16,
  border: 0.07,
  lightness: 50,
  blend: 'difference',
  xChannel: 'R',
  yChannel: 'B',
  alpha: 0.93,
  blur: 11,
  rOffset: 0,
  gOffset: 0,
  bOffset: 0,
  scale: -120,
  frost: 0.05,
  displace: 1,
  tint: true,
  bgTransparent: false
})

const liquidGlassRoot = ref<HTMLElement | null>(null)
const dimensions = reactive({ width: 0, height: 0 })
let observer: ResizeObserver | null = null

const baseStyle = computed(() => {
  return {
    '--frost': String(props.frost),
    'border-radius': `${props.radius}px`,
    ...(props.bgTransparent ? { background: 'transparent' } : {})
  } as Record<string, string>
})

const displacementImage = computed(() => {
  const border = Math.min(dimensions.width, dimensions.height) * (props.border * 0.5)
  const yBorder = Math.min(dimensions.width, dimensions.height) * (props.border * 0.5)

  return `
    <svg viewBox="0 0 ${dimensions.width} ${dimensions.height}" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="red" x1="100%" y1="0%" x2="0%" y2="0%">
          <stop offset="0%" stop-color="#0000"/>
          <stop offset="100%" stop-color="red"/>
        </linearGradient>
        <linearGradient id="blue" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#0000"/>
          <stop offset="100%" stop-color="blue"/>
        </linearGradient>
      </defs>
      <rect x="0" y="0" width="${dimensions.width}" height="${dimensions.height}" fill="black"></rect>
      ${props.tint ? `<rect x="0" y="0" width="${dimensions.width}" height="${dimensions.height}" rx="${props.radius}" fill="url(#red)" />` : ''}
      ${props.tint ? `<rect x="0" y="0" width="${dimensions.width}" height="${dimensions.height}" rx="${props.radius}" fill="url(#blue)" style="mix-blend-mode: ${props.blend}" />` : ''}
      <rect 
        x="${border}" 
        y="${yBorder}" 
        width="${dimensions.width - border * 2}" 
        height="${dimensions.height - border * 2}" 
        rx="${props.radius}" 
        fill="hsl(0 0% ${props.lightness}% / ${props.alpha})" 
        style="filter:blur(${props.blur}px)" 
      />
    </svg>
  `
})

const displacementDataUri = computed(() => {
  const encoded = encodeURIComponent(displacementImage.value)
  return `data:image/svg+xml,${encoded}`
})

onMounted(() => {
  if (!liquidGlassRoot.value) return

  observer = new ResizeObserver((entries) => {
    const entry = entries[0]
    if (!entry) return

    let width = 0
    let height = 0

    if ((entry as any).borderBoxSize && (entry as any).borderBoxSize?.length) {
      width = (entry as any).borderBoxSize[0].inlineSize
      height = (entry as any).borderBoxSize[0].blockSize
    } else if (entry.contentRect) {
      width = entry.contentRect.width
      height = entry.contentRect.height
    }

    dimensions.width = width
    dimensions.height = height
  })

  observer.observe(liquidGlassRoot.value)
})

onUnmounted(() => {
  observer?.disconnect()
})
</script>

<style scoped>
.effect {
  position: relative;
  display: block;
  opacity: 1;
  border-radius: inherit;
  backdrop-filter: url(#displacementFilter);
  background: light-dark(hsl(0 0% 100% / var(--frost, 0)), hsl(0 0% 0% / var(--frost, 0)));
  box-shadow:
    0 0 1px 0
      light-dark(
        color-mix(in oklch, canvasText, #0000 90%),
        color-mix(in oklch, canvasText, #0000 95%)
      )
      inset,
    0 0 6px 2px
      light-dark(
        color-mix(in oklch, canvasText, #0000 94%),
        color-mix(in oklch, canvasText, #0000 97%)
      )
      inset,
    0px 2px 10px rgba(17, 17, 26, 0.04),
    0px 6px 16px rgba(17, 17, 26, 0.04),
    0px 12px 28px rgba(17, 17, 26, 0.04),
    0px 2px 10px rgba(17, 17, 26, 0.04) inset,
    0px 6px 16px rgba(17, 17, 26, 0.04) inset;
}

.slot-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: inherit;
}

.filter {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}
</style>


