<template>
  <div class="pointer-events-none fixed inset-0 z-50">
    <component
      :is="current"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUIStore } from '@/stores/ui'
import TailedCursor from '@/components/ui/TailedCursor.vue'
import FluidCursor from '@/components/ui/FluidCursor.vue'
import SmoothCursor from '@/components/ui/SmoothCursor.vue'

const ui = useUIStore()

const current = computed(() => {
  if (ui.cursorTrailMode === 'fluid') return FluidCursor
  if (ui.cursorTrailMode === 'smooth') return SmoothCursor
  if (ui.cursorTrailMode === 'tailed') return TailedCursor
  return null as any
})
</script>

<style scoped>
</style>


