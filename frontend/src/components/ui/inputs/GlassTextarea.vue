<template>
  <div class="relative w-full">
    <LiquidGlass :radius="16" :frost="0.05" class="rounded-2xl" :container-class="containerClass">
      <textarea
        :id="id"
        :rows="rows"
        class="input rounded-2xl border placeholder-muted w-full bg-transparent"
        :placeholder="placeholder"
        :disabled="disabled"
        :value="modelValue as any"
        @input="onInput"
      />
    </LiquidGlass>
  </div>
  
</template>

<script setup lang="ts">
import { computed } from 'vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'

interface Props {
  modelValue: string | null
  placeholder?: string
  id?: string
  rows?: number
  disabled?: boolean
  tint?: 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'danger' | 'info' | null
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '',
  id: undefined,
  rows: 3,
  disabled: false,
  tint: null
})

const emit = defineEmits<{ (e: 'update:modelValue', v: string | null): void }>()

const tintClass = computed(() => props.tint ? `glass-tint-${props.tint}` : '')
const containerClass = computed(() => ['glass-regular','glass-interactive', tintClass.value].filter(Boolean).join(' '))

function onInput(e: Event) {
  const target = e.target as HTMLTextAreaElement
  emit('update:modelValue', target.value)
}
</script>

<style scoped>
</style>


