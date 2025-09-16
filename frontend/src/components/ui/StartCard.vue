<template>
  <div class="rounded-2xl glass-regular glass-interactive border border-gray-200/40 dark:border-gray-700/40 p-5 text-center" v-glass>
    <div :class="['text-2xl font-bold', colorClass]">{{ value }}</div>
    <p class="text-sm text-gray-600 dark:text-gray-300 mt-1">{{ label }}</p>
  </div>
  
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  value: { type: [String, Number], required: true },
  label: { type: String, required: true },
  color: { type: String, default: 'blue' }
})

const colorClass = computed(() => {
  switch (props.color) {
    case 'green': return 'text-green-600'
    case 'purple': return 'text-purple-600'
    default: return 'text-blue-600'
  }
})
</script>

<style scoped>
</style>


