<template>
  <svg
    :class="props.class"
    :width="size"
    :height="size"
    viewBox="0 0 64 64"
    xmlns="http://www.w3.org/2000/svg"
  >
    <defs>
      <clipPath :id="clipId">
        <rect x="0" y="0" width="64" height="64" rx="14" ry="14" />
      </clipPath>
      <filter :id="filterId" x="-20%" y="-20%" width="140%" height="140%">
        <feTurbulence
          ref="turbRef"
          type="fractalNoise"
          :baseFrequency="baseFrequency"
          :numOctaves="1"
          :seed="seed"
          result="noise"
        />
        <feDisplacementMap :scale="displaceScale" in="SourceGraphic" in2="noise" xChannelSelector="R" yChannelSelector="G" />
      </filter>
    </defs>

    <g :filter="`url(#${filterId})`" :clip-path="`url(#${clipId})`">
      <image
        :href="props.imageUrl"
        x="0" y="0" width="64" height="64"
        preserveAspectRatio="xMidYMid slice"
      />
    </g>
  </svg>
 </template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'

interface Props {
  size?: number
  class?: string
  imageUrl?: string
  speed?: number
  liquid?: number
}

const props = withDefaults(defineProps<Props>(), {
  size: 28,
  imageUrl: '/brand/logo.png',
  speed: 1.0,
  liquid: 12
})

const size = computed(() => props.size)
const uid = String(Math.random()).slice(2)
const filterId = `liquidFilter-${uid}`
const clipId = `liquidClip-${uid}`
const turbRef = ref<SVGFEComponentTransferElement | null>(null as any)
const seed = ref(2)
const baseFrequency = ref(0.012)
const displaceScale = computed(() => String(props.liquid))

let raf = 0
let t = 0

function tick() {
  t += 0.016
  // 在 0.006 - 0.018 之间缓慢摆动（受 speed 影响）
  const bf = 0.012 + Math.sin(t * 1.4 * props.speed) * 0.006
  baseFrequency.value = Number(bf.toFixed(4))
  try {
    const el = turbRef.value as any
    if (el && typeof el.setAttribute === 'function') {
      el.setAttribute('baseFrequency', String(baseFrequency.value))
      el.setAttribute('seed', String(2 + Math.floor((Math.sin(t) + 1) * 2)))
    }
  } catch {}
  raf = requestAnimationFrame(tick)
}

onMounted(() => { raf = requestAnimationFrame(tick) })
onUnmounted(() => { if (raf) cancelAnimationFrame(raf) })
</script>


