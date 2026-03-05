<template>
  <div class="flex items-center gap-2">
    <span v-if="showLabel" class="whitespace-nowrap text-sm text-gray-700 dark:text-gray-300">{{ labelText }}</span>
    <glass-popover-select
      :model-value="modelValue"
      :options="options"
      :size="size"
      :width="width"
      :tint="tint"
      @update:model-value="onUpdate"
      @change="onChange"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'

type BehaviorRange = '7d' | '30d' | '180d' | '365d'

interface Props {
  modelValue: BehaviorRange
  mode?: 'student' | 'full'
  showLabel?: boolean
  label?: string
  size?: 'xs' | 'sm' | 'md' | 'lg'
  width?: string
  tint?: 'primary' | 'secondary' | 'accent' | 'info' | 'success' | 'warning' | 'danger' | 'neutral'
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'full',
  showLabel: true,
  label: '',
  size: 'sm',
  width: '160px',
  tint: 'secondary'
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: BehaviorRange): void
  (e: 'change', value: BehaviorRange): void
}>()

const { t } = useI18n()

const labelText = computed(() => props.label || String(t('shared.behaviorEvidence.range') || '行为时间窗'))

const options = computed(() => {
  const all = [
    { label: String(t('shared.behaviorEvidence.range7d') || '近一周'), value: '7d' as const },
    { label: String(t('shared.behaviorEvidence.range30d') || '近一月'), value: '30d' as const },
    { label: String(t('shared.behaviorEvidence.range180d') || '近半年'), value: '180d' as const },
    { label: String(t('shared.behaviorEvidence.range365d') || '近一年'), value: '365d' as const }
  ]
  return props.mode === 'student' ? all.filter(o => o.value === '7d' || o.value === '30d') : all
})

function normalize(value: unknown): BehaviorRange {
  const raw = String(value || '').trim() as BehaviorRange
  if (raw === '7d' || raw === '30d' || raw === '180d' || raw === '365d') return raw
  return props.mode === 'student' ? '30d' : '7d'
}

function onUpdate(value: unknown) {
  emit('update:modelValue', normalize(value))
}

function onChange(value: unknown) {
  emit('change', normalize(value))
}
</script>
