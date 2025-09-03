<template>
  <div class="max-w-7xl mx-auto mb-6 relative z-10">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl md:text-3xl font-bold text-gray-900 dark:text-white">{{ title }}</h1>
        <p v-if="subtitle" class="text-gray-600 dark:text-gray-400 mt-1">{{ subtitle }}</p>
      </div>
      <div class="shrink-0">
        <slot name="actions" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title: string; subtitle?: string }>()
</script>

<style scoped>
</style>


