<template>
  <card :hoverable="true" padding="md" class="w-full" tint="primary">
    <template #header>
      <div class="flex items-start gap-3">
        <div class="flex-1 min-w-0">
          <div class="text-xl font-semibold truncate">{{ assignment?.title }}</div>
          <div v-if="assignment?.description" class="text-sm text-gray-600 dark:text-gray-300 mt-1 whitespace-pre-line break-words prose-readable">{{ assignment?.description }}</div>
        </div>
        <span class="px-2 py-0.5 rounded-full text-xs" :class="statusBadgeClass">{{ statusText }}</span>
      </div>
    </template>

    <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
      <div class="text-sm">
        <div class="text-gray-500 dark:text-gray-400">{{ i18nText('student.assignments.publish', '发布日期') }}</div>
        <div class="mt-0.5">{{ displayPublished }}</div>
      </div>
      <div class="text-sm">
        <div class="text-gray-500 dark:text-gray-400">{{ i18nText('student.assignments.due', '截止日期') }}</div>
        <div class="mt-0.5">{{ displayDue }}</div>
      </div>
    </div>
  </card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Card from '@/components/ui/Card.vue'
// @ts-ignore
import { useI18n } from 'vue-i18n'

interface Props {
  assignment: any | null
  effectiveDue?: string | number | Date | null
  status?: string | undefined
}

const props = defineProps<Props>()
const { t } = useI18n()

function i18nText(key: string, fallback: string): string {
  try {
    const msg = (t(key) as any) as string
    return (msg && msg !== key) ? msg : fallback
  } catch {
    return fallback
  }
}

const displayPublished = computed(() => {
  const ts = (props.assignment as any)?.publishedAt || (props.assignment as any)?.createdAt
  if (!ts) return '-'
  const d = new Date(ts)
  return isNaN(d.getTime()) ? String(ts) : d.toLocaleString()
})

const displayDue = computed(() => {
  const ts = props.effectiveDue || (props.assignment as any)?.dueDate
  if (!ts) return '-'
  const d = new Date(ts as any)
  return isNaN(d.getTime()) ? String(ts) : d.toLocaleString()
})

const statusText = computed(() => {
  const s = String(props.status || (props.assignment as any)?.status || 'UNKNOWN').toUpperCase()
  if (s === 'DRAFT') return i18nText('teacher.assignments.status.draft', '草稿')
  if (s === 'PUBLISHED') return i18nText('teacher.assignments.status.published', '已发布')
  if (s === 'CLOSED') return i18nText('teacher.assignments.status.closed', '已关闭')
  if (s === 'SUBMITTED') return i18nText('student.assignments.status.submitted', '已提交')
  if (s === 'GRADED') return i18nText('student.assignments.status.graded', '已评分')
  if (s === 'RETURNED') return i18nText('student.assignments.status.returned', '已退回')
  return i18nText('student.assignments.status.unknown', '未知')
})

const statusBadgeClass = computed(() => {
  const s = String(props.status || (props.assignment as any)?.status || 'UNKNOWN').toUpperCase()
  if (s === 'DRAFT') return 'bg-gray-100 text-gray-800 dark:bg-white/10 dark:text-gray-200'
  if (s === 'PUBLISHED') return 'bg-blue-100 text-blue-800 dark:bg-blue-500/20 dark:text-blue-200'
  if (s === 'CLOSED') return 'bg-gray-200 text-gray-800 dark:bg-white/10 dark:text-gray-200'
  if (s === 'SUBMITTED') return 'bg-blue-100 text-blue-800 dark:bg-blue-500/20 dark:text-blue-200'
  if (s === 'GRADED') return 'bg-green-100 text-green-800 dark:bg-green-500/20 dark:text-green-200'
  if (s === 'RETURNED') return 'bg-amber-100 text-amber-800 dark:bg-amber-500/20 dark:text-amber-200'
  return 'bg-gray-100 text-gray-800 dark:bg-white/10 dark:text-gray-200'
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>


