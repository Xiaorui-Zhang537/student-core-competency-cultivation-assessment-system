<template>
  <glass-modal v-if="open" :title="t('teacher.analytics.weights.title') as string" :maxWidth="'max-w-[1600px]'" heightVariant="normal" @close="$emit('close')">
    <div class="space-y-3">
      <div v-for="d in dims" :key="d.code" class="flex items-center justify-between">
        <span class="text-sm">{{ t(`teacher.analytics.weights.dimensions.${d.code}`) }}</span>
        <input type="number" step="0.1" min="0" max="10" class="input input--glass w-28 text-right" v-model.number="localWeights[d.code]" />
      </div>
    </div>
    <template #footer>
      <Button variant="secondary" @click="$emit('close')">{{ t('teacher.analytics.weights.cancel') }}</Button>
      <Button variant="primary" @click="save" :disabled="!valid || saving">{{ t('teacher.analytics.weights.save') }}</Button>
    </template>
  </glass-modal>
</template>

<script setup lang="ts">
import { reactive, ref, watch, computed } from 'vue'
import GlassModal from '@/components/ui/GlassModal.vue'
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

