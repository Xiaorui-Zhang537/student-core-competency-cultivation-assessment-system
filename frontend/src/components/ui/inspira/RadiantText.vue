<template>
  <p
    :style="styleVar"
    :class="[
      'inline-block text-transparent',
      // Radiant effect
      'radiant-animation bg-clip-text bg-no-repeat [background-position:0_0] [background-size:var(--radiant-width)_100%]',
      // Radiant gradient
      'bg-gradient-to-r from-transparent via-black via-50% to-transparent dark:via-white',
      $props.class,
    ]"
  >
    <slot />
  </p>
  </template>

<script lang="ts" setup>
import { computed } from 'vue'

const props = defineProps({
  duration: { type: Number, default: 10 },
  radiantWidth: { type: Number, default: 100 },
  class: String,
})

const styleVar = computed(() => ({
  '--radiant-anim-duration': `${props.duration}s`,
  '--radiant-width': `${props.radiantWidth}px`,
}))
</script>

<style scoped>
@keyframes radiant {
  0%   { background-position: calc(-120% - var(--radiant-width)) 0; }
  100% { background-position: calc(120% + var(--radiant-width)) 0; }
}

.radiant-animation {
  animation: radiant var(--radiant-anim-duration) linear infinite alternate;
}
</style>


