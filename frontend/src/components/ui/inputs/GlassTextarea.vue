<template>
  <div class="relative w-full">
    <textarea
      :id="id"
      :rows="rows"
      class="input input--glass glass-regular glass-interactive rounded-2xl border border-white/30 dark:border-white/10 placeholder-muted w-full"
      :placeholder="placeholder"
      :disabled="disabled"
      :value="modelValue as any"
      @input="onInput"
      v-glass="{ strength: 'regular', interactive: true }"
    />
  </div>
</template>

<script setup lang="ts">
interface Props {
  modelValue: string | null
  placeholder?: string
  id?: string
  rows?: number
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '',
  id: undefined,
  rows: 3,
  disabled: false,
})

const emit = defineEmits<{ (e: 'update:modelValue', v: string | null): void }>()

function onInput(e: Event) {
  const target = e.target as HTMLTextAreaElement
  emit('update:modelValue', target.value)
}
</script>

<style scoped>
</style>


