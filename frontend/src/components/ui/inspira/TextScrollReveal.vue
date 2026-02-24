<template>
  <div
    ref="textScrollRevealRef"
    :class="containerClass"
  >
    <div :class="innerStickyClass">
      <p
        class="flex flex-wrap p-5 text-2xl font-bold text-black/20 xl:text-5xl lg:p-10 lg:text-4xl md:p-8 md:text-3xl dark:text-white/20"
      >
        <scroll-word
          v-for="(word, i) in words"
          :key="i"
          :word="word"
          :progress="effectiveProgress"
          :range="[i / words.length, (i + 1) / words.length]"
        />
      </p>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { cn } from '@/lib/utils'
import ScrollWord from './ScrollWord.vue'

interface Props {
  class?: string;
  text: string;
  sticky?: boolean;
  heightClass?: string; // only used when sticky=true
  progressContainerId?: string; // optional external container for progress
  revealPortion?: number; // 0..1 portion of scroll to complete reveal, remainder holds
}

const props = defineProps<Props>()

const textScrollRevealRef = ref<HTMLElement | null>(null)

const words = computed(() => props.text.split(' '))

const scrollYProgress = ref(0)
const effectiveProgress = computed(() => {
  const portion = typeof props.revealPortion === 'number' && props.revealPortion > 0
    ? Math.min(Math.max(props.revealPortion, 0.1), 1)
    : 0.6
  const p = scrollYProgress.value / portion
  return Math.min(Math.max(p, 0), 1)
})

const containerClass = computed(() => {
  const base = props.sticky !== false
    ? cn('relative z-0', props.heightClass || 'h-[200vh]')
    : cn('relative z-0 h-auto')
  return cn(base, props.class)
})

const innerStickyClass = computed(() => {
  return props.sticky !== false
    ? 'sticky top-0 mx-auto flex h-1/2 max-w-4xl items-center bg-transparent px-4 py-20'
    : 'mx-auto flex max-w-4xl items-center bg-transparent px-4 py-8'
})

function updateScrollYProgress() {
  const el = props.progressContainerId
    ? (document.getElementById(props.progressContainerId) as HTMLElement | null)
    : textScrollRevealRef.value
  if (el) {
    const boundingRect = el.getBoundingClientRect()
    const windowHeight = window.innerHeight

    scrollYProgress.value = (boundingRect.y / windowHeight) * -1
  }
}

onMounted(() => {
  window.addEventListener('scroll', updateScrollYProgress)
  window.addEventListener('resize', updateScrollYProgress)
  updateScrollYProgress()
})

onUnmounted(() => {
  window.removeEventListener('scroll', updateScrollYProgress)
  window.removeEventListener('resize', updateScrollYProgress)
})
</script>

<style scoped>
</style>


