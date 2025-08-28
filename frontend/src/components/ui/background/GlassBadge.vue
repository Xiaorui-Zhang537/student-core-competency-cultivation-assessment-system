<template>
  <div
    class="glass-badge select-none"
    :style="badgeStyle"
    ref="root"
    aria-hidden="true"
  >
    <div class="glass-inner"></div>
    <div class="glass-icon" v-if="icon">
      <slot>
        <img :src="icon" alt="" />
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

interface Props {
  icon?: string
  size?: number
  initialX?: number
  initialY?: number
  tiltFollow?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  icon: undefined,
  size: 64,
  initialX: 0,
  initialY: 0,
  tiltFollow: true
})

const root = ref<HTMLDivElement | null>(null)
let pointer = { x: 0, y: 0 }
let frame = 0

const badgeStyle = computed(() => ({
  width: props.size + 'px',
  height: props.size + 'px',
  transform: `translate(${props.initialX}px, ${props.initialY}px)`
}))

function handleMove(e: MouseEvent) {
  const rc = root.value?.getBoundingClientRect()
  if (!rc) return
  const cx = rc.left + rc.width / 2
  const cy = rc.top + rc.height / 2
  const dx = (e.clientX - cx) / rc.width
  const dy = (e.clientY - cy) / rc.height
  pointer.x = dx
  pointer.y = dy
}

function loop() {
  frame = requestAnimationFrame(loop)
  if (!props.tiltFollow || !root.value) return
  // Soft tilt and highlight sweep
  const maxTilt = 10
  const rx = (-pointer.y) * maxTilt
  const ry = (pointer.x) * maxTilt
  root.value.style.setProperty('--tilt-x', rx.toFixed(3) + 'deg')
  root.value.style.setProperty('--tilt-y', ry.toFixed(3) + 'deg')
}

onMounted(() => {
  window.addEventListener('mousemove', handleMove, { passive: true })
  frame = requestAnimationFrame(loop)
})

onUnmounted(() => {
  cancelAnimationFrame(frame)
  window.removeEventListener('mousemove', handleMove)
})
</script>

<style scoped>
.glass-badge {
  position: absolute;
  border-radius: 14px;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  background: linear-gradient(180deg, rgba(255,255,255,0.28), rgba(255,255,255,0.12));
  border: 1px solid rgba(255,255,255,0.35);
  box-shadow: 0 8px 24px rgba(16, 24, 40, 0.12);
  transform-style: preserve-3d;
  transform: rotateX(var(--tilt-x, 0deg)) rotateY(var(--tilt-y, 0deg));
}

.glass-inner {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background: radial-gradient(120% 120% at 20% 10%, rgba(255,255,255,0.45) 0%, rgba(255,255,255,0.0) 60%),
              radial-gradient(100% 100% at 80% 90%, rgba(255,255,255,0.18) 0%, rgba(255,255,255,0.0) 60%);
  pointer-events: none;
}

.glass-icon {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
}

.glass-icon img {
  width: 50%;
  height: 50%;
  object-fit: contain;
  opacity: 0.9;
}
</style>


