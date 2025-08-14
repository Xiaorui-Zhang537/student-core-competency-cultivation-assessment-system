<template>
  <div v-if="open" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
    <div class="bg-white dark:bg-gray-800 rounded-lg w-full max-w-md p-5">
      <h3 class="text-lg font-semibold mb-4">{{ t('teacher.analytics.weights.title') }}</h3>
      <div class="space-y-3">
        <div v-for="d in dims" :key="d.code" class="flex items-center justify-between">
          <span class="text-sm">{{ t(`teacher.analytics.weights.dimensions.${d.code}`) }}</span>
          <input type="number" step="0.1" min="0" max="10" class="input w-28 text-right"
                 v-model.number="localWeights[d.code]" />
        </div>
      </div>
      <div class="flex justify-end gap-3 mt-6">
        <button class="btn" @click="$emit('close')">{{ t('teacher.analytics.weights.cancel') }}</button>
        <button class="btn btn-primary" @click="save" :disabled="saving">{{ t('teacher.analytics.weights.save') }}</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, watch, computed } from 'vue'
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'

const props = defineProps<{ open: boolean; weights: Record<string, number> | null }>()
const emit = defineEmits<{ (e: 'close'): void; (e: 'saved', weights: Record<string, number>): void }>()

const { t } = useI18n()

const dims = [
  { code: 'MORAL_COGNITION' },
  { code: 'LEARNING_ATTITUDE' },
  { code: 'LEARNING_ABILITY' },
  { code: 'LEARNING_METHOD' },
  { code: 'ACADEMIC_GRADE' }
]

const localWeights = reactive<Record<string, number>>({})
const saving = reactive({ v: false })

watch(() => props.weights, (w) => {
  Object.assign(localWeights, w || {})
}, { immediate: true, deep: true })

const valid = computed(() => Object.values(localWeights).every(v => typeof v === 'number' && v >= 0 && v <= 10))

const save = async () => {
  if (!valid.value) return
  saving.v = true
  try {
    emit('saved', { ...localWeights })
  } finally {
    saving.v = false
    emit('close')
  }
}
</script>

<style scoped></style>

