<template>
  <div class="max-w-7xl mx-auto mb-6 relative z-10">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl md:text-3xl font-bold" style="color: var(--color-base-content)">{{ title }}</h1>
        <p v-if="subtitle" class="mt-1 text-muted">{{ subtitle }}</p>
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


