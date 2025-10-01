<template>
  <div class="grid md:grid-cols-2 gap-4">
    <div class="glass-regular rounded-2xl p-5">
      <h3 class="font-semibold mb-2">{{ leftTitle }}</h3>
      <div class="min-h-[120px]"><slot name="left" /></div>
    </div>
    <div class="glass-regular rounded-2xl p-5">
      <h3 class="font-semibold mb-2">{{ rightTitle }}</h3>
      <div class="min-h-[120px]"><slot name="right" /></div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ leftTitle: string; rightTitle: string }>()
</script>

<style scoped>
</style>


