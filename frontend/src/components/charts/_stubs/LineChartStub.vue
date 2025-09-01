<template>
  <div class="w-full h-64">
    <div class="text-sm text-gray-700 dark:text-gray-300 font-medium mb-2">{{ title || 'Line Chart' }}</div>
    <div class="w-full h-[calc(100%-1.5rem)] glass-regular rounded-lg flex items-center justify-center text-xs text-gray-500 dark:text-gray-400">
      <div>Line Chart (stub) — {{ (dataPoints || []).length }} points</div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title?: string; dataPoints?: Array<{ x: string; y: number }> }>()
</script>

<style scoped></style>


