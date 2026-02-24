<template>
  <div
    class="overflow-x-auto no-scrollbar rounded-xl overflow-hidden glass-regular glass-interactive border border-gray-200/40 dark:border-gray-700/40"
  >
    <table :class="tableClassComputed">
      <thead class="bg-white/5 dark:bg-white/5 backdrop-blur-sm">
        <slot name="head" />
      </thead>
      <tbody class="bg-transparent divide-y divide-white/10">
        <slot name="body" />
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  /** 额外的 table class（默认带 divide 与最小宽度） */
  tableClass?: string
}>(), {
  tableClass: '',
})

const tableClassComputed = computed(() => {
  const base = 'min-w-full divide-y divide-white/10'
  return props.tableClass ? `${base} ${props.tableClass}` : base
})
</script>

