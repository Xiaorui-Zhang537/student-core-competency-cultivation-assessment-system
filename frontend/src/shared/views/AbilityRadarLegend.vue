<template>
  <div class="w-full">
    <div class="mb-3">
      <h4 class="text-sm font-semibold text-gray-900 dark:text-gray-100">{{ t('shared.radarLegend.labels.title') }}</h4>
    </div>

    <div class="space-y-2">
      <div
        v-for="(dim, idx) in normalizedDimensions"
        :key="idx"
        class="collapse collapse-plus glass-ultraThin border rounded-xl text-[var(--color-base-content)]"
      >
        <input type="radio" :name="groupName" :checked="idx === 0" />
        <div class="collapse-title font-semibold flex items-center gap-2">
          <span class="text-sm text-[var(--color-base-content)]">{{ dim.title }}</span>
          <span class="text-xs text-[color-mix(in_oklab,var(--color-base-content)_70%,transparent)]">{{ dim.code }}</span>
        </div>
        <div class="collapse-content text-sm">
          <div class="text-[color-mix(in_oklab,var(--color-base-content)_82%,transparent)]">
            <span class="font-medium mr-1">{{ t('shared.radarLegend.labels.what') }}:</span>
            <span>{{ dim.what }}</span>
          </div>
          <div class="mt-1 text-[color-mix(in_oklab,var(--color-base-content)_82%,transparent)]">
            <span class="font-medium mr-1">{{ t('shared.radarLegend.labels.abilities') }}:</span>
            <span>{{ dim.abilities }}</span>
          </div>
          <div class="mt-1 text-[color-mix(in_oklab,var(--color-base-content)_82%,transparent)]">
            <span class="font-medium mr-1">{{ t('shared.radarLegend.labels.scoring') }}:</span>
            <span>{{ dim.scoring }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'

const props = defineProps<{ dimensions: string[] }>()
const { t } = useI18n()
// 为了让同组件内的手风琴互斥展开，生成局部唯一的组名
const groupName = `legend-${Math.random().toString(36).slice(2, 10)}`

const NAME_ZH_TO_CODE: Record<string, string> = {
  '道德认知': 'MORAL_COGNITION',
  '学习态度': 'LEARNING_ATTITUDE',
  '学习能力': 'LEARNING_ABILITY',
  '学习方法': 'LEARNING_METHOD',
  '学习成绩': 'ACADEMIC_GRADE'
}

const normalizedDimensions = computed(() => {
  return (props.dimensions || []).map((nameZh: string) => {
    const code = NAME_ZH_TO_CODE[nameZh] || nameZh
    const base = `shared.radarLegend.dimensions.${code}`
    return {
      code,
      title: t(`${base}.title`, nameZh),
      what: t(`${base}.what`, ''),
      abilities: t(`${base}.abilities`, ''),
      scoring: t(`${base}.scoring`, '')
    }
  })
})
</script>

<style scoped>
</style>


