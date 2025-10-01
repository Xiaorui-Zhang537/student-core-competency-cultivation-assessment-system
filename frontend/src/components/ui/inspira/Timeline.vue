<template>
  <ol class="relative border-s border-base-300">
    <li v-for="(it, i) in items" :key="i" class="mb-8 ms-6">
      <span class="absolute -start-3 flex h-6 w-6 items-center justify-center rounded-full bg-primary-500/20">
        <span class="h-2.5 w-2.5 rounded-full bg-primary-500"></span>
      </span>
      <h3 class="font-semibold">{{ it.title }} <span class="text-subtle font-normal">· {{ it.date }}</span></h3>
      <p class="text-subtle mt-1">{{ it.desc || it.descKey }}</p>
    </li>
  </ol>
</template>

<script setup lang="ts">
defineProps<{ items: Array<{ title: string; date?: string; desc?: string; descKey?: string }> }>()
</script>

<style scoped>
</style>


