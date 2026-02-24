<template>
  <glass-modal v-if="open" :title="title" size="md" heightVariant="normal" @close="emit('cancel')">
    <div class="text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap">
      {{ message }}
    </div>
    <template #footer>
      <Button variant="secondary" size="sm" @click="emit('cancel')">
        {{ cancelText }}
      </Button>
      <Button :variant="confirmVariant" size="sm" @click="emit('confirm')">
        {{ confirmText }}
      </Button>
    </template>
  </glass-modal>
</template>

<script setup lang="ts">
import GlassModal from '@/components/ui/GlassModal.vue'
import Button from '@/components/ui/Button.vue'

defineProps<{
  open: boolean
  title: string
  message: string
  confirmText: string
  cancelText: string
  confirmVariant?: string
}>()

const emit = defineEmits<{
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()
</script>

<style scoped></style>

