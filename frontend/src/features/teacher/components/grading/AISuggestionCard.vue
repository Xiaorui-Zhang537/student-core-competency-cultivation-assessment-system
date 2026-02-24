<template>
  <card padding="lg" tint="warning" v-if="suggestion">
    <template #header>
      <div class="flex items-center justify-between">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.grading.ai.title') }}</h2>
        <div class="flex items-center gap-2">
          <button variant="outline" size="sm" @click="$emit('apply')">
            <check-icon class="w-4 h-4 mr-1" />
            {{ t('teacher.grading.ai.applySuggestion') || '一键应用' }}
          </button>
          <button variant="indigo" size="sm" @click="$emit('viewDetail')">
            <eye-icon class="w-4 h-4 mr-1" />
            {{ t('teacher.grading.ai.viewDetail') || '查看详情' }}
          </button>
          <button size="sm" variant="primary" class="ml-1" :loading="loading" @click="$emit('openAi')">
            <sparkles-icon class="w-4 h-4 mr-1" />
            {{ t('teacher.grading.ai.open') || 'AI 批改' }}
          </button>
        </div>
      </div>
    </template>

    <div class="space-y-4">
      <!-- 分数 + 维度进度条 -->
      <div class="flex items-start gap-6">
        <div class="min-w-[160px] text-center mt-6 md:mt-8">
          <div class="text-xs text-gray-500 mb-1">{{ t('teacher.grading.ai.suggestedScore') }}</div>
          <div class="text-3xl font-extrabold text-theme-primary">{{ suggestion.suggestedScore }}</div>
        </div>
        <div class="flex-1 grid grid-cols-1 md:grid-cols-2 gap-4">
          <div v-for="dim in dimensions" :key="dim.key" class="space-y-2">
            <div class="text-xs text-gray-500">{{ t(dim.label) }}</div>
            <progress :value="(dim.value ?? 0) * 20" size="sm" color="primary" />
            <div class="text-xs text-right text-gray-500">{{ (dim.value ?? 0).toFixed(1) }}/5</div>
          </div>
        </div>
      </div>

      <!-- 分析 -->
      <div>
        <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ t('teacher.grading.ai.analysis') }}</h4>
        <ul class="text-sm text-gray-600 dark:text-gray-400 space-y-1">
          <li v-for="(line, idx) in analysisLines" :key="idx" class="flex items-start">
            <span class="whitespace-pre-line">{{ line }}</span>
          </li>
        </ul>
      </div>

      <!-- 改进建议 -->
      <div>
        <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ t('teacher.grading.ai.improvements') }}</h4>
        <ul class="text-sm text-gray-600 dark:text-gray-400 space-y-1">
          <li v-for="item in suggestion.improvements" :key="item" class="flex items-start">
            <span class="text-theme-primary mr-2">&bull;</span>{{ item }}
          </li>
        </ul>
      </div>
    </div>
  </card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Progress from '@/components/ui/Progress.vue'
import { CheckIcon, EyeIcon, SparklesIcon } from '@heroicons/vue/24/outline'

const { t } = useI18n()

const props = defineProps<{
  suggestion: {
    suggestedScore: number | string
    dims?: { moral?: number; attitude?: number; ability?: number; strategy?: number }
    analysis?: string
    improvements?: string[]
  } | null
  loading?: boolean
}>()

defineEmits<{
  (e: 'apply'): void
  (e: 'viewDetail'): void
  (e: 'openAi'): void
}>()

const dimensions = computed(() => {
  const d = props.suggestion?.dims
  return [
    { key: 'moral', label: 'teacher.aiGrading.render.moral_reasoning', value: d?.moral ?? 0 },
    { key: 'attitude', label: 'teacher.aiGrading.render.attitude_development', value: d?.attitude ?? 0 },
    { key: 'ability', label: 'teacher.aiGrading.render.ability_growth', value: d?.ability ?? 0 },
    { key: 'strategy', label: 'teacher.aiGrading.render.strategy_optimization', value: d?.strategy ?? 0 },
  ]
})

const analysisLines = computed(() => {
  const raw = props.suggestion?.analysis || ''
  return raw.split('\n').filter((l: string) => l.trim())
})
</script>
