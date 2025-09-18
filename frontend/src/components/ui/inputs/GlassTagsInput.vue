<template>
  <div class="w-full">
    <div class="relative w-full rounded-full px-2 py-1.5 glass-regular glass-interactive border border-white/30 dark:border-white/12 focus-within:ring-2 focus-within:ring-primary-500/60 focus-within:border-primary-500/40" v-glass="{ strength: 'regular', interactive: true }">
      <div class="flex flex-wrap gap-1.5 items-center">
        <span v-for="(tag, idx) in internal" :key="idx" class="ui-chip">
          <span>{{ tag }}</span>
          <button type="button" class="ml-1 text-xs opacity-70 hover:opacity-100" @click="remove(idx)">×</button>
        </span>
        <input
          ref="inputRef"
          v-model="draft"
          :placeholder="placeholder"
          :class="['bg-transparent outline-none text-sm flex-1 min-w-[6rem]', draft ? 'text-gray-900 dark:text-gray-100' : 'text-gray-400']"
          @keydown="onKeydown"
          @blur="commit()"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'

interface Props {
  modelValue: string[]
  placeholder?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => [],
  placeholder: '添加标签（回车确认）'
})
const emit = defineEmits<{ (e:'update:modelValue', v:string[]): void }>()

const internal = ref<string[]>(Array.isArray(props.modelValue) ? [...props.modelValue] : [])
const draft = ref('')
const inputRef = ref<HTMLInputElement|null>(null)

watch(() => props.modelValue, v => { internal.value = Array.isArray(v) ? [...v] : [] })

function commit() {
  const raw = (draft.value || '').split(',').map(s => s.trim()).filter(Boolean)
  if (raw.length === 0) return
  const set = new Set(internal.value)
  for (const r of raw) set.add(r)
  internal.value = Array.from(set)
  draft.value = ''
  emit('update:modelValue', internal.value)
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter') { e.preventDefault(); commit(); return }
  if (e.key === ',' || e.key === 'Comma') { e.preventDefault(); commit(); return }
}

function remove(idx: number) {
  internal.value.splice(idx, 1)
  emit('update:modelValue', [...internal.value])
}

onMounted(() => { /* no-op */ })
</script>

<style scoped>
</style>


