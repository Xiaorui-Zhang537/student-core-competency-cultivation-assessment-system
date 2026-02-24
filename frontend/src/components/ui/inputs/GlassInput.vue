<template>
  <div class="relative">
    <liquid-glass :radius="16" :frost="0.05" class="rounded-2xl" :container-class="containerClass">
      <input
        :id="id"
        :type="type"
        class="input rounded-2xl border-none outline-none ring-0 focus:outline-none focus:ring-0 placeholder-muted bg-transparent"
        :class="[fullWidth ? 'w-full' : '']"
        :placeholder="placeholder"
        :disabled="disabled"
        :value="modelValue as any"
        @input="onInput"
        v-bind="$attrs"
      />
    </liquid-glass>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'
interface Props {
  modelValue: string | number | null
  placeholder?: string
  id?: string
  type?: string
  disabled?: boolean
  fullWidth?: boolean
  tint?: 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'danger' | 'info' | null
}

defineOptions({ inheritAttrs: false })

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '',
  id: undefined,
  type: 'text',
  disabled: false,
  fullWidth: true,
  tint: null,
})

const emit = defineEmits<{ (e: 'update:modelValue', v: string | number | null): void }>()

const tintClass = computed(() => props.tint ? `glass-tint-${props.tint}` : '')
const containerClass = computed(() => ['glass-regular','glass-interactive', tintClass.value].filter(Boolean).join(' '))

function onInput(e: Event) {
  const target = e.target as HTMLInputElement
  if (props.type === 'number') {
    const num = target.value === '' ? null : (Number(target.value))
    emit('update:modelValue', (isNaN(num as any) ? null : num) as any)
  } else {
    emit('update:modelValue', target.value)
  }
}
</script>

<style scoped>
</style>


