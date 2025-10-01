<template>
  <div class="relative">
    <!-- 左右箭头 -->
    <button class="hidden md:flex absolute left-0 top-1/2 -translate-y-1/2 z-10 glass-regular rounded-full w-9 h-9 items-center justify-center" @click="scrollBy(-1)">‹</button>
    <button class="hidden md:flex absolute right-0 top-1/2 -translate-y-1/2 z-10 glass-regular rounded-full w-9 h-9 items-center justify-center" @click="scrollBy(1)">›</button>

    <div ref="track" class="flex gap-4 overflow-x-auto snap-x pb-2 no-scrollbar">
      <div
        v-for="(it, i) in items"
        :key="i"
        class="min-w-[260px] md:min-w-[320px] lg:min-w-[360px] snap-start glass-regular rounded-2xl p-5 shrink-0"
      >
        <h3 class="text-lg font-semibold mb-2">{{ t(it.titleKey) }}</h3>
        <p class="text-subtle">{{ t(it.descKey) }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, onUnmounted } from 'vue'
defineProps<{ items: Array<{ titleKey: string; descKey: string }> }>()
const { t } = useI18n()

const track = ref<HTMLDivElement | null>(null)
let timer: number | null = null
function scrollBy(direction: 1 | -1) {
  const el = track.value
  if (!el) return
  const cardWidth = el.firstElementChild ? (el.firstElementChild as HTMLElement).clientWidth + 16 : 320
  el.scrollBy({ left: direction * cardWidth, behavior: 'smooth' })
}

onMounted(() => {
  timer = window.setInterval(() => scrollBy(1), 4000)
})
onUnmounted(() => { if (timer) window.clearInterval(timer) })
</script>

<style scoped>
/* 隐藏横向滚动条 */
.no-scrollbar::-webkit-scrollbar { display: none; }
.no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
</style>


