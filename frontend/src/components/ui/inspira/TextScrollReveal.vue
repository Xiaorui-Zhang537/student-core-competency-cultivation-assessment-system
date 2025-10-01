<template>
  <div ref="root" class="leading-relaxed" :class="cls">
    <span v-for="(w, i) in words" :key="i" class="inline-block transition-opacity duration-700 will-change-[opacity,transform]"
      :style="{ opacity: revealIndex >= i ? 1 : 0.2, transform: revealIndex >= i ? 'translateY(0)' : 'translateY(6px)' }">
      {{ w }}
    </span>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'

const props = defineProps<{ text: string; class?: string }>()
const cls = computed(() => props.class || '')

const words = computed(() => (props.text || '').split(/\s+/))
const revealIndex = ref(0)
const root = ref<HTMLElement | null>(null)

function onScroll() {
  const el = root.value
  if (!el) return
  const rect = el.getBoundingClientRect()
  const vh = window.innerHeight || 800
  const visible = Math.max(0, Math.min(rect.bottom, vh) - Math.max(rect.top, 0))
  const ratio = Math.min(1, Math.max(0, visible / Math.max(rect.height, 1)))
  revealIndex.value = Math.floor(ratio * words.value.length)
}

onMounted(() => {
  onScroll()
  window.addEventListener('scroll', onScroll, { passive: true })
})
onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<style scoped>
</style>


