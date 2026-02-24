<template>
  <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
    <start-card
      v-for="(k, idx) in items"
      :key="idx"
      :label="k.label"
      :value="k.value"
      :tint="k.tint || null"
      :tone="k.tone || 'blue'"
      :icon="k.icon"
      :hoverable="k.hoverable ?? true"
      :dense="k.dense ?? false"
    />
  </div>
</template>

<script setup lang="ts">
import StartCard from '@/components/ui/StartCard.vue'

interface Item {
  label: string
  value: string | number
  icon?: any
  tint?: 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'info' | null
  tone?: 'blue' | 'emerald' | 'violet' | 'amber' | 'indigo'
  hoverable?: boolean
  dense?: boolean
}

defineProps<{ items: Item[] }>()
</script>

