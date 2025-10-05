<template>
  <span class="xl:lg-3 mx-1 lg:mx-2.5">
    <span :style="{ opacity: computedOpacity, color: computedColor }" class="text-black dark:text-white">
      {{ word }}
    </span>
  </span>
</template>

<script lang="ts" setup>
import { computed } from 'vue'

interface Props {
  word: string
  progress: number
  range: Array<number>
}

const props = defineProps<Props>()

const computedOpacity = computed(() => {
  const [start, end] = props.range
  const progress = props.progress

  if (progress < start) return 0
  if (progress > end) return 1

  return (progress - start) / (end - start)
})

const computedColor = computed(() => {
  const [start, end] = props.range
  const p = (() => {
    const pr = props.progress
    if (pr <= start) return 0
    if (pr >= end) return 1
    return (pr - start) / (end - start)
  })()
  const pct = Math.round(p * 100)
  const inv = 100 - pct
  // Use CSS native color-mix to interpolate between base-content and primary
  return `color-mix(in oklch, var(--color-base-content) ${inv}%, var(--color-primary) ${pct}%)`
})
</script>

<style scoped>
</style>


