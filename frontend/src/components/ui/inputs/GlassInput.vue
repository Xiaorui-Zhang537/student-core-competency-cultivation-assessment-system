<template>
  <div class="relative">
    <input
      :id="id"
      :type="type"
      :class="['input input--glass glass-regular glass-interactive rounded-2xl border border-white/30 dark:border-white/10 placeholder-muted', fullWidth ? 'w-full' : '']"
      :placeholder="placeholder"
      :disabled="disabled"
      :value="modelValue as any"
      @input="onInput"
      v-glass="{ strength: 'regular', interactive: true }"
      v-bind="$attrs"
    />
  </div>
</template>

<script setup lang="ts">
interface Props {
  modelValue: string | number | null
  placeholder?: string
  id?: string
  type?: string
  disabled?: boolean
  fullWidth?: boolean
}

defineOptions({ inheritAttrs: false })

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '',
  id: undefined,
  type: 'text',
  disabled: false,
  fullWidth: true,
})

const emit = defineEmits<{ (e: 'update:modelValue', v: string | number | null): void }>()

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


