<template>
  <glass-modal
    v-if="open"
    :title="title"
    size="lg"
    heightVariant="tall"
    @close="emit('update:open', false)"
  >
    <div class="space-y-3">
      <label class="text-sm">{{ reasonLabel }}</label>
      <glass-textarea v-model="reasonModel" :rows="4" />
      <label class="text-sm">{{ untilLabel }}</label>
      <glass-date-time-picker v-model="untilModel" size="md" :minute-step="5" tint="info" />
    </div>
    <template #footer>
      <Button variant="secondary" size="sm" @click="emit('update:open', false)">{{ cancelText }}</Button>
      <Button variant="danger" size="sm" @click="emit('confirm')">{{ confirmText }}</Button>
    </template>
  </glass-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
import GlassDateTimePicker from '@/components/ui/inputs/GlassDateTimePicker.vue'
import Button from '@/components/ui/Button.vue'

const props = defineProps<{
  open: boolean
  title: string
  reasonLabel: string
  untilLabel: string
  cancelText: string
  confirmText: string
  reason: string
  resubmitUntil: string
}>()

const emit = defineEmits<{
  (e: 'update:open', v: boolean): void
  (e: 'update:reason', v: string): void
  (e: 'update:resubmitUntil', v: string): void
  (e: 'confirm'): void
}>()

const reasonModel = computed({
  get: () => props.reason,
  set: (v: any) => emit('update:reason', String(v ?? ''))
})
const untilModel = computed({
  get: () => props.resubmitUntil,
  set: (v: any) => emit('update:resubmitUntil', String(v ?? ''))
})
</script>

<style scoped></style>

