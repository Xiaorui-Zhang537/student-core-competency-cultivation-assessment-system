<template>
  <div class="grid md:grid-cols-3 gap-6">
    <div v-for="(root, idx) in roots" :key="idx" class="glass-regular rounded-2xl p-5">
      <h3 class="font-semibold mb-3">{{ root.name }}</h3>
      <ul class="space-y-1 text-sm">
        <li v-for="(child, i) in root.children" :key="i" class="flex items-center gap-2">
          <span class="w-1.5 h-1.5 rounded-full bg-primary-500/70"></span>
          <span class="text-subtle">{{ child.name }}</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ roots: Array<{ name: string; children: Array<{ name: string }> }> }>()
</script>

<style scoped>
</style>


