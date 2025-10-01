<template>
  <div class="relative">
    <div class="absolute left-0 top-0 bottom-0 w-px bg-gradient-to-b from-primary-500/60 via-primary-500/20 to-transparent"></div>
    <div class="space-y-8 pl-6">
      <div v-for="(it, i) in items" :key="i" class="relative">
        <div class="absolute -left-6 top-2 w-3 h-3 rounded-full bg-primary-500 shadow-[0_0_12px_rgba(59,130,246,0.6)]"></div>
        <h3 class="font-semibold">{{ it.title }}</h3>
        <p class="text-subtle mt-1">{{ it.content }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ items: Array<{ title: string; content: string }> }>()
</script>

<style scoped>
</style>


