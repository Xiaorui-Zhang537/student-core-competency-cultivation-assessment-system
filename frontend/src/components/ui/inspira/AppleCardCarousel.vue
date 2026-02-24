<template>
  <div class="relative w-full pb-8 md:pb-10">
    <div
      ref="carouselRef"
      class="flex w-full overflow-x-scroll overscroll-x-auto scroll-smooth py-6 [scrollbar-width:none] md:py-8"
      @scroll="checkScrollability"
    >
      <!-- 移除顶部渐变遮罩与多余修饰 -->
      <div :class="cn('flex flex-row justify-start gap-4 pl-4','mx-auto max-w-7xl')">
        <slot />
      </div>
    </div>
    <!-- bottom-right controls (single group) -->
    <div
      v-if="showControls"
      class="absolute bottom-1 right-3 z-40 flex items-center gap-2"
    >
      <button
        class="flex size-11 items-center justify-center rounded-full glass-regular backdrop-blur-md ring-1 ring-white/25 text-[color:var(--color-base-content)] hover:ring-white/40 disabled:opacity-50"
        :disabled="!canScrollLeft"
        @click="scrollLeft"
        aria-label="scroll left"
      >
        <icon :icon="'tabler:arrow-narrow-left'" class="size-6" />
      </button>
      <button
        class="flex size-11 items-center justify-center rounded-full glass-regular backdrop-blur-md ring-1 ring-white/25 text-[color:var(--color-base-content)] hover:ring-white/40 disabled:opacity-50"
        :disabled="!canScrollRight"
        @click="scrollRight"
        aria-label="scroll right"
      >
        <icon :icon="'tabler:arrow-narrow-right'" class="size-6" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, provide, computed } from 'vue'
import { cn } from '@/lib/utils'
import { CarouselKey } from './AppleCarouselContext'
import { Icon } from '@iconify/vue'

interface Props { initialScroll?: number }
const props = withDefaults(defineProps<Props>(), { initialScroll: 0 })

const carouselRef = ref<HTMLDivElement | null>(null)
const canScrollLeft = ref(false)
const canScrollRight = ref(true)
const showControls = ref(false)
const currentIndex = ref(0)

onMounted(() => {
  if (carouselRef.value) {
    carouselRef.value.scrollLeft = props.initialScroll
    checkScrollability()
  }
  window.addEventListener('resize', checkScrollability)
})

watch(() => props.initialScroll, (v) => { if (carouselRef.value) { carouselRef.value.scrollLeft = v; checkScrollability() } })

function checkScrollability() {
  if (!carouselRef.value) return
  const { scrollLeft, scrollWidth, clientWidth } = carouselRef.value
  canScrollLeft.value = scrollLeft > 0
  canScrollRight.value = scrollLeft < scrollWidth - clientWidth
  showControls.value = scrollWidth > clientWidth
}
function scrollLeft() { carouselRef.value?.scrollBy({ left: -300, behavior: 'smooth' }) }
function scrollRight() { carouselRef.value?.scrollBy({ left: 300, behavior: 'smooth' }) }

function handleCardClose(index: number) {
  if (!carouselRef.value) return
  const cardWidth = isMobile.value ? 230 : 384
  const gap = isMobile.value ? 4 : 8
  const left = (cardWidth + gap) * (index + 1)
  carouselRef.value.scrollTo({ left, behavior: 'smooth' })
  currentIndex.value = index
}

const isMobile = computed(() => typeof window !== 'undefined' && window.innerWidth < 768)

provide(CarouselKey, { onCardClose: handleCardClose, currentIndex })

onUnmounted(() => { window.removeEventListener('resize', checkScrollability) })
</script>

 


