<template>
  <div class="glass-regular rounded-xl p-4 border border-gray-200/40 dark:border-gray-700/40">
    <div class="text-sm text-gray-500 dark:text-gray-400">{{ title || 'KPI' }}</div>
    <div class="text-2xl font-semibold text-gray-900 dark:text-white mt-1">{{ value }}</div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title?: string; value?: string | number }>()
</script>

<style scoped></style>


