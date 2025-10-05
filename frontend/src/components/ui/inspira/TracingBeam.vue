<template>
  <div ref="tracingBeamRef" :class="containerClass">
    <div class="absolute -left-4 top-3 md:-left-12">
      <div
        :style="{
          boxShadow: scrollYProgress > 0 ? 'none' : 'rgba(0, 0, 0, 0.24) 0px 3px 8px',
        }"
        class="border-neutral-200 ml-[27px] flex size-4 items-center justify-center rounded-full border shadow-sm"
      >
        <div
          :style="{
            backgroundColor: scrollYProgress > 0 ? 'white' : 'var(--emerald-500)',
            borderColor: scrollYProgress > 0 ? 'white' : 'var(--emerald-600)'
          }"
          class="size-2 rounded-full border border-neutral-300 bg-white"
        />
      </div>
      <svg
        :viewBox="`0 0 20 ${svgHeight}`"
        width="20"
        :height="svgHeight"
        class="ml-4 block"
        aria-hidden="true"
      >
        <path
          :d="`M 1 0V -36 l 18 24 V ${svgHeight * 0.8} l -18 24V ${svgHeight}`"
          fill="none"
          stroke="#9091A0"
          stroke-opacity="0.16"
        ></path>
        <path
          :d="`M 1 0V -36 l 18 24 V ${svgHeight * 0.8} l -18 24V ${svgHeight}`"
          fill="none"
          stroke="url(#gradient)"
          stroke-width="1.25"
          class="motion-reduce:hidden"
        ></path>
        <defs>
          <linearGradient
            id="gradient"
            gradientUnits="userSpaceOnUse"
            x1="0"
            x2="0"
            :y1="y1"
            :y2="y2"
          >
            <stop stop-color="#18CCFC" stop-opacity="0"></stop>
            <stop stop-color="#18CCFC"></stop>
            <stop offset="0.325" stop-color="#6344F5"></stop>
            <stop offset="1" stop-color="#AE48FF" stop-opacity="0"></stop>
          </linearGradient>
        </defs>
      </svg>
    </div>
    <div ref="tracingBeamContentRef">
      <slot />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { cn } from '@/lib/utils'
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps<{ class?: string; progressContainerId?: string }>()
const containerClass = computed(() => cn('relative w-full max-w-4xl mx-auto h-full', props.class || ''))

const tracingBeamRef = ref<HTMLDivElement | null>(null)
const tracingBeamContentRef = ref<HTMLDivElement | null>(null)

const scrollYProgress = ref(0)
const svgHeight = ref(0)
const scrollPercentage = ref(0)

const y1 = computed(() =>
  mapRange(scrollYProgress.value, 0, 0.8, scrollYProgress.value, svgHeight.value) * (1.4 - scrollPercentage.value)
)
const y2 = computed(() =>
  mapRange(scrollYProgress.value, 0, 1, scrollYProgress.value, Math.max(svgHeight.value - 500, 0)) * (1.4 - scrollPercentage.value)
)

function updateScrollYProgress() {
  const el = props.progressContainerId
    ? (document.getElementById(props.progressContainerId) as HTMLElement | null)
    : tracingBeamRef.value
  if (el) {
    const boundingRect = el.getBoundingClientRect()
    const windowHeight = window.innerHeight
    const elementHeight = boundingRect.height
    scrollPercentage.value = (windowHeight - boundingRect.top) / (windowHeight + elementHeight)
    scrollYProgress.value = (boundingRect.y / windowHeight) * -1
  }
}

function updateSVGHeight() {
  if (!tracingBeamContentRef.value) return
  svgHeight.value = tracingBeamContentRef.value.offsetHeight
}

let ro: ResizeObserver | null = null
onMounted(() => {
  window.addEventListener('scroll', updateScrollYProgress)
  window.addEventListener('resize', updateScrollYProgress)
  updateScrollYProgress()
  ro = new ResizeObserver(() => updateSVGHeight())
  if (tracingBeamContentRef.value) ro.observe(tracingBeamContentRef.value)
  updateSVGHeight()
})

onUnmounted(() => {
  window.removeEventListener('scroll', updateScrollYProgress)
  window.removeEventListener('resize', updateScrollYProgress)
  ro?.disconnect()
})

function mapRange(value: number, inMin: number, inMax: number, outMin: number, outMax: number): number {
  return ((value - inMin) * (outMax - outMin)) / (inMax - inMin) + outMin
}
</script>

<style scoped>
</style>

 


