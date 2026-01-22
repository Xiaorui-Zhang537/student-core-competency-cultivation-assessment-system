<template>
  <Card padding="md" tint="info">
    <div class="flex items-center justify-between gap-3 mb-3">
      <div class="min-w-0">
        <h3 class="text-lg font-semibold truncate">{{ tf('shared.behaviorEvidence.title', '行为证据（不算分）') }}</h3>
        <div class="text-sm text-gray-500 dark:text-gray-400">
          {{ tf('shared.behaviorEvidence.subtitle', '基于行为事件的事实摘要（不参与任何加权/平均）') }}
        </div>
      </div>
      <Button variant="outline" size="sm" :disabled="loading" @click="reload">
        {{
          loading
            ? tf('shared.behaviorEvidence.loading', '加载中...')
            : tf('shared.behaviorEvidence.refresh', '刷新')
        }}
      </Button>
    </div>

    <div v-if="error" class="text-sm text-red-600 mb-3">
      {{ error }}
    </div>

    <div v-if="loading" class="text-sm text-gray-500">
      {{ t('shared.behaviorEvidence.loading') }}
    </div>

    <div v-else class="space-y-4">
      <!-- 事实统计（不评价、不算分） -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3">
        <div class="border border-white/10 rounded p-3">
          <div class="text-xs text-gray-500">{{ tf('shared.behaviorEvidence.stats.ai', 'AI 提问') }}</div>
          <div class="mt-1 text-sm">
            <span class="font-medium">{{ n(summary?.activityStats?.ai?.questionCount) }}</span>
            <span class="text-gray-500"> / </span>
            <span class="text-gray-500">{{ tf('shared.behaviorEvidence.stats.aiQuestions', '提问次数') }}</span>
            <span class="ml-2 text-gray-500">·</span>
            <span class="ml-2">{{ tf('shared.behaviorEvidence.stats.aiFollowUps', '追问') }} {{ n(summary?.activityStats?.ai?.followUpCount) }}</span>
          </div>
        </div>
        <div class="border border-white/10 rounded p-3">
          <div class="text-xs text-gray-500">{{ tf('shared.behaviorEvidence.stats.assignment', '作业行为') }}</div>
          <div class="mt-1 text-sm">
            <span>{{ tf('shared.behaviorEvidence.stats.submits', '提交') }} </span><span class="font-medium">{{ n(summary?.activityStats?.assignment?.submitCount) }}</span>
            <span class="ml-2 text-gray-500">·</span>
            <span class="ml-2">{{ tf('shared.behaviorEvidence.stats.resubmits', '修改/重交') }} <span class="font-medium">{{ n(summary?.activityStats?.assignment?.resubmitCount) }}</span></span>
          </div>
        </div>
        <div class="border border-white/10 rounded p-3">
          <div class="text-xs text-gray-500">{{ tf('shared.behaviorEvidence.stats.community', '社区互动') }}</div>
          <div class="mt-1 text-sm">
            <span>{{ tf('shared.behaviorEvidence.stats.asks', '发问') }} </span><span class="font-medium">{{ n(summary?.activityStats?.community?.askCount) }}</span>
            <span class="ml-2 text-gray-500">·</span>
            <span class="ml-2">{{ tf('shared.behaviorEvidence.stats.answers', '回答') }} <span class="font-medium">{{ n(summary?.activityStats?.community?.answerCount) }}</span></span>
          </div>
        </div>
        <div class="border border-white/10 rounded p-3">
          <div class="text-xs text-gray-500">{{ tf('shared.behaviorEvidence.stats.feedback', '查看反馈') }}</div>
          <div class="mt-1 text-sm">
            <span>{{ tf('shared.behaviorEvidence.stats.views', '次数') }} </span><span class="font-medium">{{ n(summary?.activityStats?.feedback?.viewCount) }}</span>
          </div>
        </div>
      </div>

      <!-- 证据条目 -->
      <div>
        <div class="flex items-center justify-between mb-2">
          <div class="text-sm font-semibold">{{ tf('shared.behaviorEvidence.evidence.title', '证据条目') }}</div>
          <Badge v-if="summary?.evidenceItems?.length" size="sm" variant="info">
            {{ summary.evidenceItems.length }}
          </Badge>
        </div>
        <div v-if="!summary?.evidenceItems || summary.evidenceItems.length === 0" class="text-sm text-gray-500">
          {{ tf('shared.behaviorEvidence.evidence.empty', '暂无可展示的证据条目') }}
        </div>
        <div v-else class="space-y-2">
          <div v-for="it in summary.evidenceItems" :key="it.evidenceId || it.title" class="border border-white/10 rounded p-3">
            <div class="flex items-center justify-between gap-3">
              <div class="font-medium truncate">{{ it.title || it.evidenceType || '-' }}</div>
              <Badge v-if="it.evidenceId" size="sm" variant="secondary">{{ it.evidenceId }}</Badge>
            </div>
            <div class="text-sm text-gray-500 mt-1 whitespace-pre-wrap">{{ it.description || '-' }}</div>
            <div v-if="it.eventRefs?.length" class="text-xs text-gray-400 mt-2">
              <div>{{ tf('shared.behaviorEvidence.evidence.eventRefs', `关联事件 ${it.eventRefs.length} 条`) }}</div>
              <div class="mt-1 break-all">
                IDs: {{ it.eventRefs.join(', ') }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 只记录不评价：资源访问 -->
      <div v-if="summary?.nonEvaluative?.items?.length">
        <div class="text-sm font-semibold mb-2">{{ tf('shared.behaviorEvidence.nonEvaluative.title', '只记录不评价') }}</div>
        <div class="space-y-2">
          <div v-for="it in summary.nonEvaluative.items" :key="it.eventType" class="border border-white/10 rounded p-3">
            <div class="flex items-center justify-between">
              <div class="text-sm font-medium">{{ it.eventType || '-' }}</div>
              <Badge size="sm" variant="secondary">{{ n(it.count) }}</Badge>
            </div>
            <div v-if="it.meta?.byCategory" class="text-xs text-gray-500 mt-2">
              {{ tf('shared.behaviorEvidence.nonEvaluative.byCategory', '按类别') }}: {{ formatMap(it.meta.byCategory) }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </Card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import { behaviorEvidenceApi, type BehaviorSummaryResponse } from '@/api/behaviorEvidence.api'

const props = defineProps<{
  studentId?: string | number
  courseId?: string | number
  range?: string
}>()

const { t } = useI18n()

function tf(key: string, fallback: string): string {
  const v = t(key) as any
  return v === key ? fallback : String(v)
}

const loading = ref(false)
const error = ref<string | null>(null)
const data = ref<BehaviorSummaryResponse | null>(null)

const summary = computed(() => data.value)

function n(v: any): string {
  const num = Number(v ?? 0)
  return Number.isFinite(num) ? String(num) : '0'
}

function formatMap(m: Record<string, any>): string {
  try {
    return Object.entries(m)
      .map(([k, v]) => `${k}:${v}`)
      .join(', ')
  } catch {
    return '-'
  }
}

async function reload() {
  loading.value = true
  error.value = null
  try {
    data.value = await behaviorEvidenceApi.getSummary({
      studentId: props.studentId,
      courseId: props.courseId,
      range: props.range || '7d'
    })
  } catch (e: any) {
    error.value = e?.message || String(e || '')
  } finally {
    loading.value = false
  }
}

watch(
  () => [props.studentId, props.courseId, props.range],
  () => reload(),
  { deep: true }
)

onMounted(() => reload())
</script>

