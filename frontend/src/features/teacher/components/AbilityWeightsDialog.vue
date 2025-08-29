<template>
  <div v-if="open" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" @click.self="$emit('close')">
    <div class="modal glass-thick w-full max-w-md max-h-[85vh] overflow-y-auto p-5" v-glass="{ strength: 'thick' }">
      <h3 class="text-lg font-semibold mb-4">{{ t('teacher.analytics.weights.title') }}</h3>
      <div class="space-y-3">
        <div v-for="d in dims" :key="d.code" class="flex items-center justify-between">
          <span class="text-sm">{{ t(`teacher.analytics.weights.dimensions.${d.code}`) }}</span>
          <input type="number" step="0.1" min="0" max="10" class="input w-28 text-right"
                 v-model.number="localWeights[d.code]" />
        </div>
      </div>
      <div class="flex justify-end gap-3 mt-6">
        <Button variant="secondary" @click="$emit('close')">{{ t('teacher.analytics.weights.cancel') }}</Button>
        <Button variant="primary" @click="save" :disabled="!valid || saving">{{ t('teacher.analytics.weights.save') }}</Button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch, computed } from 'vue'
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'
import Button from '@/components/ui/Button.vue'

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
const saving = ref(false)

watch(() => props.weights, (w) => {
  Object.assign(localWeights, w || {})
}, { immediate: true, deep: true })

const valid = computed(() => Object.values(localWeights).every(v => typeof v === 'number' && v >= 0 && v <= 10))

const save = async () => {
  if (!valid.value) return
  saving.value = true
  try {
    emit('saved', { ...localWeights })
  } finally {
    saving.value = false
    emit('close')
  }
}
</script>

<style scoped></style>

