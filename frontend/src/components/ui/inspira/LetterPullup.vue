<template>
  <div class="flex justify-center">
    <div
      v-for="(letter, index) in letters"
      :key="`${letter}-${index}`"
    >
      <Motion
        as="h1"
        :initial="pullupVariant.initial"
        :animate="pullupVariant.animate"
        :transition="{
          delay: index * (props.delay ? props.delay : 0.05),
        }"
        :class="[
          'font-display text-center text-3xl md:text-4xl lg:text-5xl font-bold tracking-[-0.02em] text-black dark:text-white drop-shadow-sm md:leading-[3.25rem] lg:leading-[4rem]',
          props.class,
        ]"
      >
        <span v-if="letter === ' '">&nbsp;</span>
        <span v-else>{{ letter }}</span>
      </Motion>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Motion } from '@motionone/vue'

interface LetterPullupProps {
  class?: string
  words: string
  delay?: number
}

const props = defineProps<LetterPullupProps>()

const letters = computed(() => (props.words ?? '').split(''))

const pullupVariant = {
  initial: { y: 100, opacity: 0 },
  animate: { y: 0, opacity: 1 },
}
</script>

<style scoped>
</style>


