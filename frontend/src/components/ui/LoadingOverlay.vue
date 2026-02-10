<template>
  <div class="rounded-2xl border glass-ultraThin glass-tint-secondary p-8 text-center" role="status" aria-live="polite">
    <div class="inline-flex items-center gap-2 text-sm text-muted">
      <span class="loading loading-spinner loading-sm" aria-hidden="true"></span>
      <span>{{ text }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  text?: string
}>(), {
  text: 'Loading...'
})
</script>
