<template>
  <div class="inline-flex flex-wrap max-w-full" :class="$props.class">
    <template v-for="(seg, sIdx) in segments" :key="`seg-${sIdx}`">
      <!-- 使用零宽空格提供换行机会，具体间距由前一个单词的 margin 决定 -->
      <span v-if="seg.type === 'space'">&#8203;</span>
      <span v-else class="inline-flex whitespace-nowrap" :class="wordMarginClass(sIdx)">
        <Motion
          v-for="(ch, idx) in seg.text.split('')"
          :key="`tg-${sIdx}-${idx}`"
          as="span"
          :initial="variant.initial"
          :animate="variant.animate"
          :transition="{
            duration: props.duration,
            delay: (props.delay / 1000) + (seg.startIndex + idx) * 0.04,
            easing: 'ease-in-out'
          }"
          class="inline-block will-change-transform"
        >
          <span>{{ ch }}</span>
        </Motion>
      </span>
    </template>
  </div>
  </template>

<script setup lang="ts">
import { computed } from 'vue'
import { Motion } from '@motionone/vue'

interface Props {
  words: string
  duration?: number
  delay?: number
  filter?: boolean
  class?: string
}

const props = withDefaults(defineProps<Props>(), {
  duration: 0.7,
  delay: 0,
  filter: true,
})

type Segment = { type: 'space' | 'word'; text: string; startIndex: number }

const segments = computed<Segment[]>(() => {
  const input = props.words ?? ''
  const parts = input.split(/(\s+)/)
  const result: Segment[] = []
  let letterCounter = 0
  for (const p of parts) {
    if (p.trim() === '') {
      result.push({ type: 'space', text: ' ', startIndex: letterCounter })
    } else {
      result.push({ type: 'word', text: p, startIndex: letterCounter })
      letterCounter += p.length
    }
  }
  return result
})

const variant = computed(() => {
  const baseInitial: Record<string, unknown> = { opacity: 0, y: 4 }
  const baseAnimate: Record<string, unknown> = { opacity: 1, y: 0 }
  if (props.filter) {
    baseInitial.filter = 'blur(4px)'
    baseAnimate.filter = 'blur(0px)'
  }
  return { initial: baseInitial, animate: baseAnimate }
})

const wordMarginClass = (index: number) => (segments.value[index + 1]?.type === 'space' ? 'mr-[clamp(0.6ch,0.8ch,1ch)]' : '')
</script>

<style scoped>
</style>


