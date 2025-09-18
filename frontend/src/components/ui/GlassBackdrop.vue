<template>
  <div :style="styleObject"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props { blur?: number; opacity?: number }
const props = withDefaults(defineProps<Props>(), { blur: 10, opacity: 0.86 })

const styleObject = computed(() => ({
  position: 'absolute',
  inset: '0',
  pointerEvents: 'none',
  backdropFilter: `blur(${props.blur}px)`,
  WebkitBackdropFilter: `blur(${props.blur}px)`,
  backgroundColor: `rgba(255,255,255,${props.opacity})`
}))
</script>

<style scoped></style>


