<template>
  <div class="w-full h-64 flex items-center justify-center text-sm text-gray-500 dark:text-gray-400">
    <div class="text-center">
      <div class="font-medium mb-2">Radar Chart (stub)</div>
      <div v-if="dimensions && values && dimensions.length === values.length" class="grid grid-cols-2 gap-x-4 gap-y-1 text-left">
        <template v-for="(d, i) in dimensions" :key="i">
          <div>{{ d }}:</div>
          <div>{{ values[i] }}</div>
        </template>
      </div>
      <div v-else>no data</div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ dimensions: string[]; values: number[] }>()
</script>

<style scoped></style>


