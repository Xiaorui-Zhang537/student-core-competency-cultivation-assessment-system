<template>
  <div class="pointer-events-none select-none">
    <div v-for="n in count" :key="n" class="meteor" :style="styleMap[n-1]"></div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
const props = withDefaults(defineProps<{ count?: number; class?: string }>(), { count: 16 })

const styleMap = computed(() => Array.from({ length: props.count }).map(() => {
  const left = Math.random() * 100
  const delay = Math.random() * 5
  const duration = 2 + Math.random() * 2
  const top = Math.random() * 100
  return {
    left: left + 'vw',
    top: top + 'vh',
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`
  } as Record<string, string>
}))
</script>

<style scoped>
.meteor {
  position: fixed;
  width: 2px;
  height: 2px;
  background: linear-gradient(90deg, rgba(255,255,255,0.9), rgba(255,255,255,0));
  box-shadow: 0 0 6px rgba(255,255,255,0.8);
  transform: translate3d(0,0,0);
  animation-name: fall;
  animation-timing-function: linear;
  animation-iteration-count: infinite;
}

@keyframes fall {
  0% { transform: translate3d(0,0,0) rotate(20deg); opacity: 0; }
  10% { opacity: 1; }
  100% { transform: translate3d(40vw, 40vh, 0) rotate(20deg); opacity: 0; }
}
</style>


