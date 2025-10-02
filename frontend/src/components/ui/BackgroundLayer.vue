<template>
  <div class="fixed inset-0 z-0 overflow-hidden">
    <!-- 明亮模式背景 -->
    <component
      v-if="!uiStore.isDarkMode"
      :is="lightComponent"
      class="absolute inset-0"
    />
    <!-- 暗黑模式背景 -->
    <component
      v-else
      :is="darkComponent"
      class="absolute inset-0"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue'
import { useUIStore } from '@/stores/ui'

const uiStore = useUIStore()

const AuroraBackground = defineAsyncComponent(() => import('@/components/ui/inspira/AuroraBackground.vue'))
const Tetris = defineAsyncComponent(() => import('@/components/ui/inspira/Tetris.vue'))
const Meteors = defineAsyncComponent(() => import('@/components/ui/inspira/Meteors.vue'))
const NeuralBackground = defineAsyncComponent(() => import('@/components/ui/inspira/NeuralBackground.vue'))

const lightComponent = computed(() => {
  switch (uiStore.backgroundLight) {
    case 'aurora': return AuroraBackground
    case 'tetris': return Tetris
    default: return null
  }
})

const darkComponent = computed(() => {
  switch (uiStore.backgroundDark) {
    case 'meteors': return Meteors
    case 'neural': return NeuralBackground
    default: return null
  }
})
</script>

<style scoped>
</style>


