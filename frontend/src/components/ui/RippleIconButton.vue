<template>
  <button
    type="button"
    class="ripple-btn"
    :title="title"
    @click="onClick"
    @mousedown="createRipple"
    @touchstart="createRipple"
  >
    <span class="inner">
      <slot />
    </span>
  </button>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = withDefaults(defineProps<{ title?: string }>(), { title: '' })
const emit = defineEmits<{ (e:'click', ev: MouseEvent | TouchEvent): void }>()
const ripples = ref<number>(0)

function onClick(ev: any) {
  emit('click', ev)
}

function createRipple(ev: MouseEvent | TouchEvent) {
  try {
    const target = ev.currentTarget as HTMLElement
    const circle = document.createElement('span')
    circle.className = 'ripple'
    const rect = target.getBoundingClientRect()
    const clientX = (ev as TouchEvent).touches?.[0]?.clientX ?? (ev as MouseEvent).clientX
    const clientY = (ev as TouchEvent).touches?.[0]?.clientY ?? (ev as MouseEvent).clientY
    const x = clientX - rect.left
    const y = clientY - rect.top
    const d = Math.max(rect.width, rect.height)
    circle.style.left = `${x - d / 2}px`
    circle.style.top = `${y - d / 2}px`
    circle.style.width = `${d}px`
    circle.style.height = `${d}px`
    target.appendChild(circle)
    setTimeout(() => { try { circle.remove() } catch {} }, 600)
  } catch {}
}
</script>

<style scoped>
.ripple-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 9999px;
  overflow: hidden;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}
.ripple-btn .inner { display: inline-flex; align-items: center; justify-content: center; }
.ripple-btn .inner :deep(svg) { width: 18px; height: 18px; }
.ripple {
  position: absolute;
  border-radius: 50%;
  background: color-mix(in oklab, var(--color-primary) 30%, transparent);
  transform: scale(0);
  animation: ripple 600ms ease-out;
  pointer-events: none;
}
@keyframes ripple {
  to { transform: scale(2.5); opacity: 0; }
}
</style>


