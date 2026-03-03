<template>
  <card padding="md" tint="info">
    <div class="flex items-center justify-between gap-3 mb-3">
      <div class="min-w-0">
        <h3 class="text-lg font-semibold truncate">{{ tf('shared.behaviorEvidence.title', '行为证据') }}</h3>
        <div class="text-sm text-gray-500 dark:text-gray-400">
          {{ tf('shared.behaviorEvidence.subtitle', '基于行为事件的事实摘要') }}
        </div>
      </div>
      <Button variant="outline" size="sm" :disabled="loading" @click="reload(true)">
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
      <!-- 事实统计 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-3">
        <div class="rounded-2xl p-4 glass-ultraThin glass-tint-primary border border-white/20 dark:border-white/10" v-glass="{ strength: 'ultraThin', interactive: false }">
          <div class="flex items-center gap-2 text-xs text-gray-600 dark:text-gray-300">
            <sparkles-icon class="w-4 h-4 text-fuchsia-600 dark:text-fuchsia-400" />
            <span>{{ tf('shared.behaviorEvidence.stats.ai', 'AI 提问') }}</span>
          </div>
          <div class="mt-1 text-sm">
            <span class="text-gray-700 dark:text-gray-200">
              {{ tf('shared.behaviorEvidence.stats.aiQuestions', '提问') }}：<span class="font-medium">{{ n(summary?.activityStats?.ai?.questionCount) }}</span>
            </span>
            <span class="mx-2 text-gray-500">·</span>
            <span class="text-gray-700 dark:text-gray-200">
              {{ tf('shared.behaviorEvidence.stats.aiFollowUps', '追问') }}：<span class="font-medium">{{ n(summary?.activityStats?.ai?.followUpCount) }}</span>
            </span>
          </div>
        </div>
        <div class="rounded-2xl p-4 glass-ultraThin glass-tint-info border border-white/20 dark:border-white/10" v-glass="{ strength: 'ultraThin', interactive: false }">
          <div class="flex items-center gap-2 text-xs text-gray-600 dark:text-gray-300">
            <academic-cap-icon class="w-4 h-4 text-sky-600 dark:text-sky-400" />
            <span>{{ tf('shared.behaviorEvidence.stats.assignment', '作业行为') }}</span>
          </div>
          <div class="mt-1 text-sm">
            <span class="text-gray-700 dark:text-gray-200">
              {{ tf('shared.behaviorEvidence.stats.submits', '提交') }}：<span class="font-medium">{{ n(summary?.activityStats?.assignment?.submitCount) }}</span>
            </span>
            <span class="ml-2 text-gray-500">·</span>
            <span class="ml-2 text-gray-700 dark:text-gray-200">
              {{ tf('shared.behaviorEvidence.stats.resubmits', '修改/重交') }}：<span class="font-medium">{{ n(summary?.activityStats?.assignment?.resubmitCount) }}</span>
            </span>
          </div>
        </div>
        <div class="rounded-2xl p-4 glass-ultraThin glass-tint-success border border-white/20 dark:border-white/10" v-glass="{ strength: 'ultraThin', interactive: false }">
          <div class="flex items-center gap-2 text-xs text-gray-600 dark:text-gray-300">
            <chat-bubble-left-right-icon class="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
            <span>{{ tf('shared.behaviorEvidence.stats.community', '社区互动') }}</span>
          </div>
          <div class="mt-1 text-sm">
            <span class="text-gray-700 dark:text-gray-200">
              {{ tf('shared.behaviorEvidence.stats.asks', '发问') }}：<span class="font-medium">{{ n(summary?.activityStats?.community?.askCount) }}</span>
            </span>
            <span class="ml-2 text-gray-500">·</span>
            <span class="ml-2 text-gray-700 dark:text-gray-200">
              {{ tf('shared.behaviorEvidence.stats.answers', '回答') }}：<span class="font-medium">{{ n(summary?.activityStats?.community?.answerCount) }}</span>
            </span>
          </div>
        </div>
        <div class="rounded-2xl p-4 glass-ultraThin glass-tint-warning border border-white/20 dark:border-white/10" v-glass="{ strength: 'ultraThin', interactive: false }">
          <div class="flex items-center gap-2 text-xs text-gray-600 dark:text-gray-300">
            <document-text-icon class="w-4 h-4 text-amber-600 dark:text-amber-400" />
            <span>{{ tf('shared.behaviorEvidence.stats.feedback', '查看反馈') }}</span>
          </div>
          <div class="mt-1 text-sm">
            <span class="text-gray-700 dark:text-gray-200">
              {{ tf('shared.behaviorEvidence.stats.views', '次数') }}：<span class="font-medium">{{ n(summary?.activityStats?.feedback?.viewCount) }}</span>
            </span>
          </div>
        </div>
        <div class="rounded-2xl p-4 glass-ultraThin glass-tint-accent border border-white/20 dark:border-white/10" v-glass="{ strength: 'ultraThin', interactive: false }">
          <div class="flex items-center gap-2 text-xs text-gray-600 dark:text-gray-300">
            <microphone-icon class="w-4 h-4 text-violet-600 dark:text-violet-400" />
            <span>{{ tf('shared.behaviorEvidence.stats.voice', '口语练习') }}</span>
          </div>
          <div class="mt-1 text-sm">
            <span class="text-gray-700 dark:text-gray-200">
              {{ tf('shared.behaviorEvidence.stats.voiceTurns', '回合') }}：<span class="font-medium">{{ n(summary?.signals?.voiceTurnCount) }}</span>
            </span>
          </div>
        </div>
      </div>

      <!-- 能力目标证据 -->
      <div v-if="hasGoalSection">
        <div class="flex items-center justify-between mb-2">
          <div class="flex items-center gap-2 text-sm font-semibold">
            <sparkles-icon class="w-4 h-4 text-rose-600 dark:text-rose-400" />
            <span>{{ tf('shared.behaviorEvidence.goal.title', '能力目标') }}</span>
          </div>
          <badge v-if="Number(goalSignals.totalGoalCount) > 0" size="sm" variant="info">
            {{ n(goalSignals.totalGoalCount) }}
          </badge>
        </div>
        <div class="rounded-2xl p-4 glass-ultraThin border border-white/20 dark:border-white/10 bg-gradient-to-r from-rose-500/10 via-orange-500/6 to-transparent">
          <div class="grid grid-cols-2 lg:grid-cols-4 gap-3 text-sm">
            <div>
              <div class="text-xs text-gray-500">{{ tf('shared.behaviorEvidence.goal.active', '进行中') }}</div>
              <div class="mt-1 font-semibold text-gray-700 dark:text-gray-200">{{ n(goalSignals.activeCount) }}</div>
            </div>
            <div>
              <div class="text-xs text-gray-500">{{ tf('shared.behaviorEvidence.goal.overdue', '已逾期') }}</div>
              <div class="mt-1 font-semibold text-gray-700 dark:text-gray-200">{{ n(goalSignals.overdueCount) }}</div>
            </div>
            <div>
              <div class="text-xs text-gray-500">{{ tf('shared.behaviorEvidence.goal.completionRate', '达成率') }}</div>
              <div class="mt-1 font-semibold text-gray-700 dark:text-gray-200">{{ percent(goalSignals.completionRate) }}</div>
            </div>
            <div>
              <div class="text-xs text-gray-500">{{ tf('shared.behaviorEvidence.goal.linkedDelta', '目标关联分数变化') }}</div>
              <div class="mt-1 font-semibold text-gray-700 dark:text-gray-200">{{ signed(goalSignals.linkedScoreDelta) }}</div>
            </div>
          </div>
          <div class="mt-3 grid grid-cols-1 lg:grid-cols-3 gap-3 text-sm">
            <div>
              <div class="text-xs text-gray-500">{{ tf('shared.behaviorEvidence.goal.focus', '当前聚焦') }}</div>
              <div class="mt-1 text-gray-700 dark:text-gray-200">{{ goalSignals.focusSummary || tf('shared.behaviorEvidence.goal.none', '暂无') }}</div>
            </div>
            <div>
              <div class="text-xs text-gray-500">{{ tf('shared.behaviorEvidence.goal.nextDue', '最近截止') }}</div>
              <div class="mt-1 text-gray-700 dark:text-gray-200">
                {{ formatGoalDate(goalSignals.nextTargetDate) }}
                <span v-if="goalSignals.nextTargetDate" class="text-xs text-gray-500 ml-2">
                  ({{ dueLabel(goalSignals.nearestDueInDays) }})
                </span>
              </div>
            </div>
            <div>
              <div class="text-xs text-gray-500">{{ tf('shared.behaviorEvidence.goal.recovery', '逾期后达成') }}</div>
              <div class="mt-1 text-gray-700 dark:text-gray-200">
                {{ n(goalSignals.overdueRecoveryCount) }}
                <span class="text-xs text-gray-500 ml-2">
                  {{ tf('shared.behaviorEvidence.goal.latestAction', '最近动作') }}: {{ formatGoalDateTime(goalSignals.latestActionAt) }}
                </span>
              </div>
            </div>
          </div>
          <div v-if="goalSignals.activeGoals?.length" class="mt-3">
            <div class="text-xs text-gray-500 mb-2">{{ tf('shared.behaviorEvidence.goal.activeList', '进行中目标') }}</div>
            <div class="flex flex-wrap gap-2">
              <span
                v-for="it in goalSignals.activeGoals"
                :key="`active-${it.goalId || it.title}`"
                class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full text-xs bg-white/60 dark:bg-white/8 text-gray-700 dark:text-gray-200 border border-white/40 dark:border-white/10"
              >
                <span>{{ it.dimensionName || it.title || '-' }}</span>
                <span class="text-gray-500">{{ formatGoalScore(it.currentScore) }}/{{ formatGoalScore(it.targetScore) }}</span>
              </span>
            </div>
          </div>
          <div v-if="goalSignals.overdueGoals?.length" class="mt-3">
            <div class="text-xs text-gray-500 mb-2">{{ tf('shared.behaviorEvidence.goal.overdueList', '逾期目标') }}</div>
            <div class="flex flex-wrap gap-2">
              <span
                v-for="it in goalSignals.overdueGoals"
                :key="`overdue-${it.goalId || it.title}`"
                class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full text-xs bg-rose-500/10 text-rose-700 dark:text-rose-300 border border-rose-400/20"
              >
                <span>{{ it.dimensionName || it.title || '-' }}</span>
                <span>{{ formatGoalDate(it.targetDate) }}</span>
              </span>
            </div>
          </div>
        </div>

        <div v-if="goalEvidenceItems.length" class="space-y-2 mt-3">
          <div
            v-for="(it, idx) in goalEvidenceItems"
            :key="it.evidenceId || `goal-${idx}`"
            class="rounded-2xl p-4 glass-ultraThin border border-white/20 dark:border-white/10 border-l-4"
            :class="goalEvidenceTintClass(it.evidenceType)"
            v-glass="{ strength: 'ultraThin', interactive: false }"
          >
            <div class="flex items-center justify-between gap-3">
              <div class="font-medium truncate">{{ evidenceTitle(it) }}</div>
              <badge v-if="it.evidenceId" size="sm" variant="secondary">{{ it.evidenceId }}</badge>
            </div>
            <div class="text-sm text-gray-500 mt-1 whitespace-pre-wrap">{{ evidenceDescription(it) }}</div>
            <div v-if="it.eventRefs?.length" class="text-xs text-gray-400 mt-2">
              <div>{{ eventRefsLabel(it.eventRefs.length) }}</div>
              <div class="mt-1 break-all">
                {{ tf('shared.behaviorEvidence.evidence.idsLabel', 'IDs') }}: {{ it.eventRefs.join(', ') }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 证据条目 -->
      <div>
        <div class="flex items-center justify-between mb-2">
          <div class="flex items-center gap-2 text-sm font-semibold">
            <clipboard-document-list-icon class="w-4 h-4 text-indigo-600 dark:text-indigo-400" />
            <span>{{ tf('shared.behaviorEvidence.evidence.title', '证据条目') }}</span>
          </div>
          <badge v-if="generalEvidenceItems.length" size="sm" variant="info">
            {{ generalEvidenceItems.length }}
          </badge>
        </div>
        <div v-if="generalEvidenceItems.length === 0" class="text-sm text-gray-500">
          {{ tf('shared.behaviorEvidence.evidence.empty', '暂无可展示的证据条目') }}
        </div>
        <div v-else class="space-y-2">
          <div
            v-for="(it, idx) in generalEvidenceItems"
            :key="it.evidenceId || it.title"
            class="rounded-2xl p-4 glass-ultraThin border border-white/20 dark:border-white/10 border-l-4"
            :class="evidenceTintClass(idx)"
            v-glass="{ strength: 'ultraThin', interactive: false }"
          >
            <div class="flex items-center justify-between gap-3">
              <div class="font-medium truncate">{{ evidenceTitle(it) }}</div>                                                                  
              <badge v-if="it.evidenceId" size="sm" variant="secondary">{{ it.evidenceId }}</badge>
            </div>
            <div class="text-sm text-gray-500 mt-1 whitespace-pre-wrap">{{ evidenceDescription(it) }}</div>                                                       
            <div v-if="it.eventRefs?.length" class="text-xs text-gray-400 mt-2">
              <div>{{ eventRefsLabel(it.eventRefs.length) }}</div>                                           
              <div class="mt-1 break-all">
                {{ tf('shared.behaviorEvidence.evidence.idsLabel', 'IDs') }}: {{ it.eventRefs.join(', ') }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 资源访问（仅记录） -->
      <div v-if="summary?.nonEvaluative?.items?.length">
        <div class="flex items-center gap-2 text-sm font-semibold mb-2">
          <information-circle-icon class="w-4 h-4 text-gray-500" />
          <span>{{ tf('shared.behaviorEvidence.nonEvaluative.title', '资源访问') }}</span>
        </div>
        <div class="space-y-2">
          <div
            v-for="it in summary.nonEvaluative.items"
            :key="it.eventType"
            class="rounded-2xl p-4 glass-ultraThin glass-tint-secondary border border-white/20 dark:border-white/10"
            v-glass="{ strength: 'ultraThin', interactive: false }"
          >
            <div class="flex items-center justify-between">
              <div class="text-sm font-medium">{{ it.eventType || '-' }}</div>
              <badge size="sm" variant="secondary">{{ n(it.count) }}</badge>
            </div>
            <div v-if="it.meta?.byCategory" class="text-xs text-gray-500 mt-2">
              {{ tf('shared.behaviorEvidence.nonEvaluative.byCategory', '按类别') }}: {{ formatMap(it.meta.byCategory) }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </card>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import { behaviorEvidenceApi, type BehaviorSummaryResponse } from '@/api/behaviorEvidence.api'
import {
  SparklesIcon,
  AcademicCapIcon,
  ChatBubbleLeftRightIcon,
  DocumentTextIcon,
  ClipboardDocumentListIcon,
  InformationCircleIcon,
  MicrophoneIcon
} from '@heroicons/vue/24/outline'

const props = defineProps<{
  studentId?: string | number
  courseId?: string | number
  range?: string
  adminMode?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:summary', v: BehaviorSummaryResponse | null): void
}>()

const { t, locale } = useI18n()

function tf(key: string, fallback: string): string {
  const v = t(key) as any
  return v === key ? fallback : String(v)
}

const loading = ref(false)
const error = ref<string | null>(null)
const data = ref<BehaviorSummaryResponse | null>(null)
const lastReloadKey = ref('')

const summary = computed(() => data.value)
const isEnLocale = computed(() => String(locale?.value || 'zh-CN').toLowerCase().startsWith('en'))
const goalSignals = computed<Record<string, any>>(() => {
  const raw = summary.value?.signals?.goalSignals
  return raw && typeof raw === 'object' ? raw as Record<string, any> : {}
})
const goalEvidenceItems = computed(() =>
  (summary.value?.evidenceItems || []).filter((it) => String(it?.evidenceType || '').startsWith('goal_'))
)
const generalEvidenceItems = computed(() =>
  (summary.value?.evidenceItems || []).filter((it) => !String(it?.evidenceType || '').startsWith('goal_'))
)
const hasGoalSection = computed(() =>
  goalEvidenceItems.value.length > 0
  || Number(goalSignals.value.totalGoalCount || 0) > 0
  || Number(goalSignals.value.activeCount || 0) > 0
)

function toNumber(v: any): number {
  const num = Number(v ?? 0)
  return Number.isFinite(num) ? num : 0
}

function n(v: any): string {
  return String(toNumber(v))
}

function percent(v: any): string {
  const raw = Number(v)
  if (!Number.isFinite(raw)) return '0%'
  const normalized = raw > 1 ? raw : raw * 100
  return `${Math.round(normalized)}%`
}

function signed(v: any): string {
  const raw = Number(v)
  if (!Number.isFinite(raw)) return '-'
  if (raw > 0) return `+${raw.toFixed(2)}`
  return raw.toFixed(2)
}

function formatGoalScore(v: any): string {
  const raw = Number(v)
  if (!Number.isFinite(raw)) return '-'
  return raw.toFixed(2)
}

function formatGoalDate(input: any): string {
  if (!input) return '-'
  const date = new Date(String(input))
  if (isNaN(date.getTime())) return '-'
  if (isEnLocale.value) {
    return new Intl.DateTimeFormat('en-US', { month: 'short', day: '2-digit' }).format(date)
  }
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${month}月${day}日`
}

function formatGoalDateTime(input: any): string {
  if (!input) return '-'
  const date = new Date(String(input))
  if (isNaN(date.getTime())) return '-'
  if (isEnLocale.value) {
    return new Intl.DateTimeFormat('en-US', {
      month: 'short',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    }).format(date).replace(',', '')
  }
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  const hour = `${date.getHours()}`.padStart(2, '0')
  const minute = `${date.getMinutes()}`.padStart(2, '0')
  return `${month}月${day}日 ${hour}:${minute}`
}

function dueLabel(v: any): string {
  const days = Number(v)
  if (!Number.isFinite(days)) return '-'
  if (days === 0) return tf('shared.behaviorEvidence.goal.dueToday', '今天到期')
  if (days > 0) {
    return tf('shared.behaviorEvidence.goal.dueInDays', '剩余 {days} 天').replace('{days}', String(days))
  }
  return tf('shared.behaviorEvidence.goal.overdueByDays', '已逾期 {days} 天').replace('{days}', String(Math.abs(days)))
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

function eventRefsLabel(count: number): string {
  // i18n 优先；否则用中文兜底
  try {
    const v = t('shared.behaviorEvidence.evidence.eventRefs', { count }) as any
    if (v && v !== 'shared.behaviorEvidence.evidence.eventRefs') return String(v)
  } catch {}
  return `关联事件 ${count} 条`
}

function evidenceTitle(it: any): string {
  if (!it) return '-'
  const type = String(it.evidenceType || '')
  // 英文环境：后端当前返回的是中文 title/description，这里用结构化信息生成英文标题
  if (isEnLocale.value) {
    switch (type) {
      case 'ai_activity':
        return tf('shared.behaviorEvidence.records.ai.title', 'AI Interaction')
      case 'voice_practice_activity':
        return tf('shared.behaviorEvidence.records.voice.title', 'Speaking Practice')
      case 'voice_practice_review':
        return tf('shared.behaviorEvidence.records.voiceReview.title', 'Speaking Replay')
      case 'community_activity':
        return tf('shared.behaviorEvidence.records.community.title', 'Community Activity')
      case 'assignment_activity':
        return tf('shared.behaviorEvidence.records.assignment.title', 'Assignment Submissions')
      case 'feedback_view':
        return tf('shared.behaviorEvidence.records.feedback.title', 'Feedback Views')
      case 'feedback_iteration':
        return tf('shared.behaviorEvidence.records.feedbackIteration.title', 'Post-feedback revision')
      case 'goal_focus':
        return tf('shared.behaviorEvidence.records.goalFocus.title', 'Goal Focus')
      case 'goal_achievement':
        return tf('shared.behaviorEvidence.records.goalAchievement.title', 'Goal Achieved')
      case 'goal_overdue':
        return tf('shared.behaviorEvidence.records.goalOverdue.title', 'Goal Overdue')
      default:
        return it.title || it.evidenceType || '-'
    }
  }
  return it.title || it.evidenceType || '-'
}

function evidenceDescription(it: any): string {
  if (!it) return '-'
  const type = String(it.evidenceType || '')
  if (isEnLocale.value) {
    const q = Number(summary.value?.activityStats?.ai?.questionCount ?? 0)
    const f = Number(summary.value?.activityStats?.ai?.followUpCount ?? 0)
    const voice = Number(summary.value?.signals?.voiceTurnCount ?? 0)
    const ask = Number(summary.value?.activityStats?.community?.askCount ?? 0)
    const ans = Number(summary.value?.activityStats?.community?.answerCount ?? 0)
    const submit = Number(summary.value?.activityStats?.assignment?.submitCount ?? 0)
    const resub = Number(summary.value?.activityStats?.assignment?.resubmitCount ?? 0)
    const fb = Number(summary.value?.activityStats?.feedback?.viewCount ?? 0)
    switch (type) {
      case 'ai_activity':
        return tf('shared.behaviorEvidence.records.ai.desc', 'In this stage: {q} questions, {f} follow-ups.')
          .replace('{q}', String(q))
          .replace('{f}', String(f))
      case 'voice_practice_activity':
        return tf('shared.behaviorEvidence.records.voice.desc', 'In this stage: {n} speaking practice turns.')
          .replace('{n}', String(voice))
      case 'community_activity':
        return tf('shared.behaviorEvidence.records.community.desc', 'In this stage: {ask} asks, {ans} replies.')
          .replace('{ask}', String(ask))
          .replace('{ans}', String(ans))
      case 'voice_practice_review':
        return tf('shared.behaviorEvidence.records.voiceReview.desc', 'Replay activity was recorded in this stage.')
      case 'assignment_activity':
        return tf('shared.behaviorEvidence.records.assignment.desc', 'In this stage: {submit} submissions, {resub} resubmits.')
          .replace('{submit}', String(submit))
          .replace('{resub}', String(resub))
      case 'feedback_view':
        return tf('shared.behaviorEvidence.records.feedback.desc', 'In this stage: {fb} feedback views.')
          .replace('{fb}', String(fb))
      case 'feedback_iteration':
        // 该条证据是后端通过事件引用生成的最小证据，这里给个稳定英文兜底
        return tf('shared.behaviorEvidence.records.feedbackIteration.desc', 'Revised an assignment after viewing feedback.')
      case 'goal_focus':
        return tf('shared.behaviorEvidence.records.goalFocus.desc', 'Created, updated, or kept active learning goals in this stage.')
      case 'goal_achievement':
        return tf('shared.behaviorEvidence.records.goalAchievement.desc', 'Completed at least one learning goal in this stage.')
      case 'goal_overdue':
        return tf('shared.behaviorEvidence.records.goalOverdue.desc', 'There are active goals that have passed the target date and still need follow-through.')
      default:
        return it.description || '-'
    }
  }
  return it.description || '-'
}

function goalEvidenceTintClass(type: any): string {
  switch (String(type || '')) {
    case 'goal_focus':
      return 'border-l-rose-500 bg-gradient-to-r from-rose-500/18 via-rose-500/8 to-transparent dark:from-rose-400/22 dark:via-rose-400/10'
    case 'goal_achievement':
      return 'border-l-emerald-500 bg-gradient-to-r from-emerald-500/18 via-emerald-500/8 to-transparent dark:from-emerald-400/22 dark:via-emerald-400/10'
    case 'goal_overdue':
      return 'border-l-amber-500 bg-gradient-to-r from-amber-500/18 via-amber-500/8 to-transparent dark:from-amber-400/22 dark:via-amber-400/10'
    default:
      return 'glass-tint-secondary'
  }
}

function evidenceTintClass(idx: number): string {
  // 证据条目：循环 4 种配色（更像报告卡片）
  const i = Math.abs(Number(idx || 0)) % 4
  switch (i) {
    // 用“亮色渐变底 + 亮色左侧色条”，避免 glass-tint 叠加导致发灰/脏
    case 0: return 'border-l-fuchsia-500 bg-gradient-to-r from-fuchsia-500/18 via-fuchsia-500/8 to-transparent dark:from-fuchsia-400/22 dark:via-fuchsia-400/10'
    case 1: return 'border-l-sky-500 bg-gradient-to-r from-sky-500/18 via-sky-500/8 to-transparent dark:from-sky-400/22 dark:via-sky-400/10'
    case 2: return 'border-l-emerald-500 bg-gradient-to-r from-emerald-500/18 via-emerald-500/8 to-transparent dark:from-emerald-400/22 dark:via-emerald-400/10'
    case 3: return 'border-l-amber-500 bg-gradient-to-r from-amber-500/18 via-amber-500/8 to-transparent dark:from-amber-400/22 dark:via-amber-400/10'
    default: return 'glass-tint-secondary border-l-gray-400/70'
  }
}

function buildReloadKey(): string {
  return [props.studentId ?? '', props.courseId ?? '', props.range || '7d', props.adminMode ? 'admin' : 'user'].join('|')
}

async function reload(force = false) {
  // 参数未就绪时不请求，避免首帧出现“studentId 必填”
  if (props.studentId === undefined || props.studentId === null || String(props.studentId).trim() === '') {
    data.value = null
    error.value = null
    lastReloadKey.value = ''
    emit('update:summary', null)
    return
  }
  const requestKey = buildReloadKey()
  if (!force && requestKey === lastReloadKey.value) {
    return
  }
  loading.value = true
  error.value = null
  try {
    // 说明：当前后端 /admin/behavior/summary 在部分环境会 500，
    // 这里统一走稳定的通用接口，确保管理员详情页可用性。
    data.value = await behaviorEvidenceApi.getSummary({
      studentId: props.studentId,
      courseId: props.courseId,
      range: props.range || '7d'
    })
    lastReloadKey.value = requestKey
    emit('update:summary', data.value)
  } catch (e: any) {
    error.value = e?.message || String(e || '')
    emit('update:summary', data.value)
  } finally {
    loading.value = false
  }
}

watch(
  [() => props.studentId, () => props.courseId, () => props.range, () => props.adminMode],
  () => reload(),
  { immediate: true }
)
</script>
