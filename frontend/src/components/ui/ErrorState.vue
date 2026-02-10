<template>
  <div class="rounded-2xl border glass-ultraThin glass-tint-warning p-8 text-center" role="alert">
    <h3 class="text-lg font-semibold text-[var(--color-base-content)]">
      <slot name="title">{{ title }}</slot>
    </h3>
    <p class="mt-2 text-sm text-muted">
      <slot name="message">{{ message }}</slot>
    </p>
    <div v-if="$slots.actions || retryLabel" class="mt-4 flex items-center justify-center gap-2">
      <slot name="actions">
        <button
          v-if="retryLabel"
          type="button"
          class="btn btn-sm btn-primary rounded-full"
          @click="$emit('retry')"
        >
          {{ retryLabel }}
        </button>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  title?: string
  message?: string
  retryLabel?: string
}>(), {
  title: '',
  message: '',
  retryLabel: ''
})

defineEmits<{
  (e: 'retry'): void
}>()
</script>
