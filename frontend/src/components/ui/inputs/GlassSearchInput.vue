<template>
  <div class="relative w-full rounded-full focus-within:ring-[1.5px] focus-within:ring-primary-500/40 focus-within:outline-none">
    <svg class="pointer-events-none w-4 h-4 absolute left-2 top-1/2 -translate-y-1/2 text-gray-400" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l3.817 3.817a.75.75 0 11-1.06 1.06l-3.818-3.816A6 6 0 012 8z" clip-rule="evenodd"/></svg>
    <input
      :id="id"
      type="text"
      class="ui-pill--input ui-pill--pl w-full pl-8"
      :placeholder="placeholder"
      :disabled="disabled"
      :value="modelValue as any"
      @input="onInput"
      v-glass="{ strength: 'regular', interactive: true }"
      :class="size==='sm' ? 'ui-pill--sm' : 'ui-pill--md'"
    />
  </div>
</template>

<script setup lang="ts">
defineOptions({ inheritAttrs: false })

interface Props {
  modelValue: string | null
  placeholder?: string
  id?: string
  disabled?: boolean
  size?: 'sm' | 'md'
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '',
  id: undefined,
  disabled: false,
  size: 'md'
})

const emit = defineEmits<{ (e: 'update:modelValue', v: string | null): void }>()

function onInput(e: Event) {
  const target = e.target as HTMLInputElement
  emit('update:modelValue', target.value)
}
</script>

<style scoped>
</style>


