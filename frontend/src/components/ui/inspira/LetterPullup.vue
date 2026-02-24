<template>
  <div :class="['flex flex-wrap w-full min-w-0 max-w-full break-words', justifyClass]">
    <template v-for="(seg, sIdx) in segments" :key="`seg-${sIdx}`">
      <span v-if="seg.type === 'space'"></span>
      <span v-else class="inline-flex flex-wrap min-w-0 sm:whitespace-nowrap whitespace-normal" :class="wordMarginClass(sIdx)">
        <motion
          v-for="(ch, idx) in seg.text.split('')"
          :key="`ch-${sIdx}-${idx}`"
          as="h1"
          :initial="pullupVariant.initial"
          :animate="pullupVariant.animate"
          :transition="{
            delay: (seg.startIndex + idx) * (props.delay ? props.delay : 0.05),
          }"
          :class="[
            'font-display font-bold tracking-[-0.02em] text-inherit drop-shadow-sm',
            textAlignClass,
            props.class,
          ]"
        >
          <span>{{ ch }}</span>
        </motion>
      </span>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Motion } from '@motionone/vue'

  interface LetterPullupProps {
  class?: string
  words: string
  delay?: number
    align?: 'left' | 'center' | 'right'
}

  const props = withDefaults(defineProps<LetterPullupProps>(), { align: 'center' })

  type Segment = { type: 'space' | 'word'; text: string; startIndex: number }

  const segments = computed<Segment[]>(() => {
    const input = props.words ?? ''
    const parts = input.split(/(\s+)/)
    const result: Segment[] = []
    let letterCounter = 0
    for (const p of parts) {
      if (p.trim() === '') {
        // 仅作为分隔符存在；实际空隙通过单词的右外边距实现
        result.push({ type: 'space', text: ' ', startIndex: letterCounter })
      } else {
        result.push({ type: 'word', text: p, startIndex: letterCounter })
        letterCounter += p.length
      }
    }
    return result
  })

  const wordMarginClass = (index: number) => (segments.value[index + 1]?.type === 'space' ? 'mr-[clamp(0.8ch,1.1ch,1.4ch)]' : '')

const pullupVariant = {
  initial: { y: 100, opacity: 0 },
  animate: { y: 0, opacity: 1 },
}

  const justifyClass = computed(() => {
    if (props.align === 'left') return 'justify-start'
    if (props.align === 'right') return 'justify-end'
    return 'justify-center'
  })

  const textAlignClass = computed(() => {
    if (props.align === 'left') return 'text-left'
    if (props.align === 'right') return 'text-right'
    return 'text-center'
  })
</script>

<style scoped>
</style>


