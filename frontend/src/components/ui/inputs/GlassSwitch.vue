<template>
  <button
    type="button"
    class="relative inline-block select-none glass-interactive align-middle"
    :class="[rootSize, disabled ? 'opacity-60 cursor-not-allowed' : 'cursor-pointer']"
    :aria-pressed="modelValue ? 'true' : 'false'"
    :disabled="disabled"
    @click="onToggle"
  >
    <span
      class="absolute inset-0 rounded-full transition-colors duration-200 border shadow-inner"
      :class="trackClass"
    ></span>
    <span
      class="absolute rounded-full bg-white transition-transform duration-200 shadow"
      :class="thumbWH"
      :style="thumbStyle"
    ></span>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  modelValue: boolean
  disabled?: boolean
  size?: 'sm' | 'md'
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  disabled: false,
  size: 'md'
})

const emit = defineEmits<{ (e:'update:modelValue', v:boolean):void }>()

function onToggle() {
  if (props.disabled) return
  emit('update:modelValue', !props.modelValue)
}

const rootSize = computed(() => props.size === 'sm' ? 'w-10 h-6' : 'w-12 h-7')

const thumbWH = computed(() => props.size === 'sm' ? 'w-4 h-4' : 'w-5 h-5')

const thumbStyle = computed(() => {
  // base left padding 4px; translate-x handles the rest
  const base = 'left:4px; top:50%; transform: translateY(-50%) '
  const tx = props.modelValue
    ? (props.size === 'sm' ? 'translateX(16px);' : 'translateX(20px);')
    : 'translateX(0px);'
  return base + tx
})

const trackClass = computed(() => props.modelValue
  ? 'glass-thin bg-gradient-to-r from-primary-400/60 to-primary-500/60 border-white/40'
  : 'glass-thin bg-white/25 border-white/30'
)
</script>

<style scoped>
</style>


