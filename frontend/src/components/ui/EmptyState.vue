<template>
  <div class="rounded-2xl border glass-ultraThin p-8 text-center" :class="tintClass" role="status">
    <div v-if="$slots.icon" class="mx-auto mb-3 w-fit">
      <slot name="icon" />
    </div>
    <h3 class="text-lg font-semibold text-[var(--color-base-content)]">
      <slot name="title">{{ title }}</slot>
    </h3>
    <p v-if="description || $slots.description" class="mt-2 text-sm text-muted">
      <slot name="description">{{ description }}</slot>
    </p>
    <div v-if="$slots.actions" class="mt-4 flex items-center justify-center gap-2">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  title?: string
  description?: string
  tint?: 'primary' | 'secondary' | 'info' | 'warning' | 'accent'
}>(), {
  title: '',
  description: '',
  tint: 'info'
})

const tintClass = computed(() => `glass-tint-${props.tint}`)
</script>
