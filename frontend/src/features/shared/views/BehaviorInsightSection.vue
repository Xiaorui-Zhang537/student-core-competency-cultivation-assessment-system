<template>
  <card padding="md" tint="secondary">
    <div class="flex items-center justify-between gap-3 mb-3">
      <div class="min-w-0">
        <h3 class="text-lg font-semibold truncate">{{ t('shared.behaviorInsight.title') }}</h3>
        <div class="text-sm text-gray-500 dark:text-gray-400">
          {{ t('shared.behaviorInsight.subtitle') }}
        </div>
      </div>

      <div class="flex items-center gap-2">
        <badge v-if="insight?.meta?.status" size="sm" :variant="statusVariant">
          {{ t(`shared.behaviorInsight.status.${insight.meta.status}`) }}
        </badge>
        <Button
          v-if="props.adminMode"
          variant="secondary"
          size="sm"
          :disabled="historyLoading"
          @click="openHistory"
        >
          {{ t('shared.behaviorInsight.history.title') }}
        </Button>
        <Button
          v-if="canOperate"
          :variant="actionVariant"
          size="sm"
          :disabled="loading || (cooldownActive && !isTeacher)"
          @click="onClickAction"
        >
          {{ actionLabel }}
        </Button>
      </div>
    </div>

    <div v-if="cooldownActive" class="text-xs text-gray-500 mb-3">
      <div>{{ t('shared.behaviorInsight.cooldown') }}</div>
      <div v-if="nextAvailableAt">{{ t('shared.behaviorInsight.cooldownUntil', { time: formatDateTime(nextAvailableAt) }) }}</div>
    </div>

    <div v-if="error" class="text-sm text-red-600 mb-3">{{ error }}</div>
    <div v-if="loading" class="text-sm text-gray-500">{{ t('shared.behaviorInsight.loading') }}</div>

    <div v-else class="space-y-4">
      <div v-if="!insight" class="text-sm text-gray-500">
        {{ t('shared.behaviorInsight.empty') }}
      </div>

      <template v-else>
        <div class="rounded-2xl p-4 glass-ultraThin border border-white/20 dark:border-white/10">
          <div class="text-sm font-semibold mb-2">{{ t('shared.behaviorInsight.reportInfo.title') || '报告信息' }}</div>
          <div class="text-xs text-gray-500 mb-2">
            {{ t('shared.behaviorInsight.reportInfo.generatedAt') || '生成时间' }}：{{ formatDateTime(insight?.meta?.generatedAt) }}
          </div>
          <div class="grid grid-cols-2 md:grid-cols-4 gap-2 text-xs text-gray-600 dark:text-gray-300">
            <div>{{ t('shared.behaviorInsight.reportInfo.aiQ') || 'AI提问' }}：{{ reportStats.aiQuestionCount }}</div>
            <div>{{ t('shared.behaviorInsight.reportInfo.aiFollow') || 'AI追问' }}：{{ reportStats.aiFollowUpCount }}</div>
            <div>{{ t('shared.behaviorInsight.reportInfo.submit') || '作业提交' }}：{{ reportStats.assignmentSubmitCount }}</div>
            <div>{{ t('shared.behaviorInsight.reportInfo.resubmit') || '作业重交' }}：{{ reportStats.assignmentResubmitCount }}</div>
            <div>{{ t('shared.behaviorInsight.reportInfo.ask') || '社区提问' }}：{{ reportStats.communityAskCount }}</div>
            <div>{{ t('shared.behaviorInsight.reportInfo.answer') || '社区回答' }}：{{ reportStats.communityAnswerCount }}</div>
            <div>{{ t('shared.behaviorInsight.reportInfo.feedback') || '反馈查看' }}：{{ reportStats.feedbackViewCount }}</div>
            <div>{{ t('shared.behaviorInsight.reportInfo.events') || '输入事件数' }}：{{ reportContext?.inputEventCount ?? 0 }}</div>
          </div>
        </div>

        <!-- Explain -->
        <div>
          <div class="flex items-center gap-2 text-sm font-semibold mb-2">
            <document-text-icon class="w-4 h-4 text-sky-600 dark:text-sky-400" />
            <span>{{ t('shared.behaviorInsight.sections.explain') }}</span>
          </div>
          <div
            class="rounded-2xl p-4 glass-ultraThin glass-tint-info border border-white/20 dark:border-white/10"
            v-glass="{ strength: 'ultraThin', interactive: false }"
          >
            <div class="text-sm text-gray-700 dark:text-gray-200 whitespace-pre-wrap">
              {{ insight.explainScore?.text || '-' }}
            </div>
            <div v-if="insight.explainScore?.evidenceIds?.length" class="text-xs text-gray-400 mt-2">
              {{ t('shared.behaviorInsight.evidenceLabel') }}: {{ insight.explainScore.evidenceIds.join(', ') }}
            </div>
          </div>
        </div>

        <!-- Judgements -->
        <div v-if="insight.stageJudgements?.length">
          <div class="flex items-center gap-2 text-sm font-semibold mb-2">
            <chart-pie-icon class="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
            <span>{{ t('shared.behaviorInsight.sections.judgement') }}</span>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div
              v-for="it in insight.stageJudgements"
              :key="String(it.dimensionCode || '')"
              class="rounded-2xl p-4 glass-ultraThin border border-white/20 dark:border-white/10 border-l-4"
              :class="dimensionTintClass(it.dimensionCode)"
              v-glass="{ strength: 'ultraThin', interactive: false }"
            >
              <div class="flex items-center justify-between gap-2">
                <div class="min-w-0 flex items-center gap-2">
                  <span class="w-2.5 h-2.5 rounded-full flex-shrink-0" :class="dimensionDotClass(it.dimensionCode)"></span>
                  <div class="font-medium truncate">{{ dimensionLabel(it.dimensionCode) }}</div>
                </div>
                <badge size="sm" variant="info">{{ levelLabel(it.level) }}</badge>
              </div>
              <div class="text-sm text-gray-500 mt-1 whitespace-pre-wrap">{{ it.rationale || '-' }}</div>
              <div v-if="it.evidenceIds?.length" class="text-xs text-gray-400 mt-2">
                {{ t('shared.behaviorInsight.evidenceLabel') }}: {{ it.evidenceIds.join(', ') }}
              </div>
            </div>
          </div>
        </div>

        <!-- Risk Alerts (v2) -->
        <div v-if="insight.riskAlerts?.length">
          <div class="flex items-center gap-2 text-sm font-semibold mb-2">
            <exclamation-triangle-icon class="w-4 h-4 text-amber-600 dark:text-amber-400" />
            <span>{{ t('shared.behaviorInsight.sections.riskAlerts') }}</span>
          </div>
          <div class="space-y-2">
            <div
              v-for="(a, idx) in insight.riskAlerts"
              :key="String(a.title || idx)"
              class="rounded-2xl p-4 glass-ultraThin border border-white/20 dark:border-white/10"
              :class="riskTintClass(a.severity)"
              v-glass="{ strength: 'ultraThin', interactive: false }"
            >
              <div class="flex items-center justify-between gap-2">
                <div class="font-medium truncate">{{ a.title || '-' }}</div>
                <badge size="sm" :variant="severityVariant(a.severity)">
                  {{ severityLabel(a.severity) }}
                </badge>
              </div>
              <div class="text-sm text-gray-500 mt-1 whitespace-pre-wrap">{{ a.message || '-' }}</div>
              <div v-if="a.evidenceIds?.length" class="text-xs text-gray-400 mt-2">
                {{ t('shared.behaviorInsight.evidenceLabel') }}: {{ a.evidenceIds.join(', ') }}
              </div>
            </div>
          </div>
        </div>

        <!-- Suggestions -->
        <div v-if="insight.formativeSuggestions?.length">
          <div class="flex items-center gap-2 text-sm font-semibold mb-2">
            <light-bulb-icon class="w-4 h-4 text-indigo-600 dark:text-indigo-400" />
            <span>{{ t('shared.behaviorInsight.sections.suggestions') }}</span>
          </div>
          <div class="space-y-2">
            <div
              v-for="(s, idx) in insight.formativeSuggestions"
              :key="String(s.title || idx)"
              class="rounded-2xl p-4 glass-ultraThin glass-tint-secondary border border-white/20 dark:border-white/10"
              v-glass="{ strength: 'ultraThin', interactive: false }"
            >
              <div class="font-medium">{{ s.title || '-' }}</div>
              <div class="text-sm text-gray-500 mt-1 whitespace-pre-wrap">{{ s.description || '-' }}</div>
              <ul v-if="s.nextActions?.length" class="mt-2 text-sm text-gray-700 dark:text-gray-200 list-disc pl-5">
                <li v-for="(a, i) in s.nextActions" :key="i">{{ a }}</li>
              </ul>
              <div v-if="s.evidenceIds?.length" class="text-xs text-gray-400 mt-2">
                {{ t('shared.behaviorInsight.evidenceLabel') }}: {{ s.evidenceIds.join(', ') }}
              </div>
            </div>
          </div>
        </div>

        <!-- Action Recommendations (v2) -->
        <div v-if="insight.actionRecommendations?.length">
          <div class="flex items-center gap-2 text-sm font-semibold mb-2">
            <sparkles-icon class="w-4 h-4 text-fuchsia-600 dark:text-fuchsia-400" />
            <span>{{ t('shared.behaviorInsight.sections.actionRecommendations') }}</span>
          </div>
          <div class="space-y-2">
            <div
              v-for="(s, idx) in insight.actionRecommendations"
              :key="String(s.title || idx)"
              class="rounded-2xl p-4 glass-ultraThin glass-tint-primary border border-white/20 dark:border-white/10"
              v-glass="{ strength: 'ultraThin', interactive: false }"
            >
              <div class="font-medium">{{ s.title || '-' }}</div>
              <div class="text-sm text-gray-500 mt-1 whitespace-pre-wrap">{{ s.description || '-' }}</div>
              <ul v-if="s.nextActions?.length" class="mt-2 text-sm text-gray-700 dark:text-gray-200 list-disc pl-5">
                <li v-for="(a, i) in s.nextActions" :key="i">{{ a }}</li>
              </ul>
              <div v-if="s.evidenceIds?.length" class="text-xs text-gray-400 mt-2">
                {{ t('shared.behaviorInsight.evidenceLabel') }}: {{ s.evidenceIds.join(', ') }}
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </card>

  <glass-modal
    v-if="historyVisible"
    :title="String(t('shared.behaviorInsight.history.title'))"
    max-width="max-w-3xl"
    @close="closeHistory"
  >
    <div class="space-y-3">
      <div class="text-sm text-gray-500">{{ t('shared.behaviorInsight.history.subtitle') }}</div>
      <div v-if="historyError" class="text-sm text-red-600">{{ historyError }}</div>
      <div v-if="historyLoading" class="text-sm text-gray-500">{{ t('shared.behaviorInsight.loading') }}</div>
      <div v-else-if="historyItems.length === 0" class="text-sm text-gray-500">{{ t('shared.behaviorInsight.history.empty') }}</div>
      <div v-else class="space-y-2">
        <div
          v-for="row in historyItems"
          :key="String(row.id)"
          class="rounded-xl border border-white/20 dark:border-white/10 p-3 glass-ultraThin"
          v-glass="{ strength: 'ultraThin', interactive: false }"
        >
          <div class="flex items-center justify-between gap-2">
            <div class="min-w-0">
              <div class="text-sm font-medium truncate">{{ formatDateTime(row.generatedAt) }}</div>
              <div class="text-xs text-gray-500 truncate">
                {{ row.model || '-' }} · {{ t(`shared.behaviorInsight.status.${String(row.status || 'success')}`) }}
              </div>
            </div>
            <Button size="sm" variant="outline" :disabled="detailLoading" @click="openHistoryDetail(row.id)">
              {{ t('common.viewDetail') || '查看详情' }}
            </Button>
          </div>
        </div>
      </div>
    </div>
  </glass-modal>

  <glass-modal
    v-if="historyDetailVisible"
    :title="String(t('shared.behaviorInsight.history.detailTitle'))"
    max-width="max-w-4xl"
    @close="closeHistoryDetail"
  >
    <div v-if="detailLoading" class="text-sm text-gray-500">{{ t('shared.behaviorInsight.loading') }}</div>
    <div v-else-if="historyDetailError" class="text-sm text-red-600">{{ historyDetailError }}</div>
    <div v-else-if="!historyDetail" class="text-sm text-gray-500">{{ t('shared.behaviorInsight.empty') }}</div>
    <div v-else class="space-y-4">
      <div class="rounded-2xl p-4 glass-ultraThin border border-white/20 dark:border-white/10">
        <div class="text-sm font-semibold mb-2">{{ t('shared.behaviorInsight.reportInfo.title') || '报告信息' }}</div>
        <div class="text-xs text-gray-500 mb-2">
          {{ t('shared.behaviorInsight.reportInfo.generatedAt') || '生成时间' }}：{{ formatDateTime(historyDetail?.meta?.generatedAt) }}
          · {{ historyDetail?.meta?.model || '-' }}
        </div>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-2 text-xs text-gray-600 dark:text-gray-300">
          <div>{{ t('shared.behaviorInsight.reportInfo.aiQ') || 'AI提问' }}：{{ historyReportStats.aiQuestionCount }}</div>
          <div>{{ t('shared.behaviorInsight.reportInfo.aiFollow') || 'AI追问' }}：{{ historyReportStats.aiFollowUpCount }}</div>
          <div>{{ t('shared.behaviorInsight.reportInfo.submit') || '作业提交' }}：{{ historyReportStats.assignmentSubmitCount }}</div>
          <div>{{ t('shared.behaviorInsight.reportInfo.resubmit') || '作业重交' }}：{{ historyReportStats.assignmentResubmitCount }}</div>
          <div>{{ t('shared.behaviorInsight.reportInfo.ask') || '社区提问' }}：{{ historyReportStats.communityAskCount }}</div>
          <div>{{ t('shared.behaviorInsight.reportInfo.answer') || '社区回答' }}：{{ historyReportStats.communityAnswerCount }}</div>
          <div>{{ t('shared.behaviorInsight.reportInfo.feedback') || '反馈查看' }}：{{ historyReportStats.feedbackViewCount }}</div>
          <div>{{ t('shared.behaviorInsight.reportInfo.events') || '输入事件数' }}：{{ historyReportContext?.inputEventCount ?? 0 }}</div>
        </div>
      </div>

      <div>
        <div class="flex items-center gap-2 text-sm font-semibold mb-2">
          <document-text-icon class="w-4 h-4 text-sky-600 dark:text-sky-400" />
          <span>{{ t('shared.behaviorInsight.sections.explain') }}</span>
        </div>
        <div class="rounded-2xl p-4 glass-ultraThin glass-tint-info border border-white/20 dark:border-white/10">
          <div class="text-sm text-gray-700 dark:text-gray-200 whitespace-pre-wrap">
            {{ historyDetail.explainScore?.text || '-' }}
          </div>
          <div v-if="historyDetail.explainScore?.evidenceIds?.length" class="text-xs text-gray-400 mt-2">
            {{ t('shared.behaviorInsight.evidenceLabel') }}: {{ historyDetail.explainScore.evidenceIds.join(', ') }}
          </div>
        </div>
      </div>

      <div v-if="historyDetail.stageJudgements?.length">
        <div class="flex items-center gap-2 text-sm font-semibold mb-2">
          <chart-pie-icon class="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
          <span>{{ t('shared.behaviorInsight.sections.judgement') }}</span>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div
            v-for="it in historyDetail.stageJudgements"
            :key="String(it.dimensionCode || '') + '-history'"
            class="rounded-2xl p-4 glass-ultraThin border border-white/20 dark:border-white/10 border-l-4"
            :class="dimensionTintClass(it.dimensionCode)"
          >
            <div class="flex items-center justify-between gap-2">
              <div class="min-w-0 flex items-center gap-2">
                <span class="w-2.5 h-2.5 rounded-full flex-shrink-0" :class="dimensionDotClass(it.dimensionCode)"></span>
                <div class="font-medium truncate">{{ dimensionLabel(it.dimensionCode) }}</div>
              </div>
              <badge size="sm" variant="info">{{ levelLabel(it.level) }}</badge>
            </div>
            <div class="text-sm text-gray-500 mt-1 whitespace-pre-wrap">{{ it.rationale || '-' }}</div>
            <div v-if="it.evidenceIds?.length" class="text-xs text-gray-400 mt-2">
              {{ t('shared.behaviorInsight.evidenceLabel') }}: {{ it.evidenceIds.join(', ') }}
            </div>
          </div>
        </div>
      </div>

      <div v-if="historyDetail.riskAlerts?.length">
        <div class="flex items-center gap-2 text-sm font-semibold mb-2">
          <exclamation-triangle-icon class="w-4 h-4 text-amber-600 dark:text-amber-400" />
          <span>{{ t('shared.behaviorInsight.sections.riskAlerts') }}</span>
        </div>
        <div class="space-y-2">
          <div
            v-for="(a, idx) in historyDetail.riskAlerts"
            :key="String(a.title || idx) + '-history'"
            class="rounded-2xl p-4 glass-ultraThin border border-white/20 dark:border-white/10"
            :class="riskTintClass(a.severity)"
          >
            <div class="flex items-center justify-between gap-2">
              <div class="font-medium truncate">{{ a.title || '-' }}</div>
              <badge size="sm" :variant="severityVariant(a.severity)">
                {{ severityLabel(a.severity) }}
              </badge>
            </div>
            <div class="text-sm text-gray-500 mt-1 whitespace-pre-wrap">{{ a.message || '-' }}</div>
            <div v-if="a.evidenceIds?.length" class="text-xs text-gray-400 mt-2">
              {{ t('shared.behaviorInsight.evidenceLabel') }}: {{ a.evidenceIds.join(', ') }}
            </div>
          </div>
        </div>
      </div>

      <div v-if="historyDetail.formativeSuggestions?.length">
        <div class="flex items-center gap-2 text-sm font-semibold mb-2">
          <light-bulb-icon class="w-4 h-4 text-indigo-600 dark:text-indigo-400" />
          <span>{{ t('shared.behaviorInsight.sections.suggestions') }}</span>
        </div>
        <div class="space-y-2">
          <div
            v-for="(s, idx) in historyDetail.formativeSuggestions"
            :key="String(s.title || idx) + '-history'"
            class="rounded-2xl p-4 glass-ultraThin glass-tint-secondary border border-white/20 dark:border-white/10"
          >
            <div class="font-medium">{{ s.title || '-' }}</div>
            <div class="text-sm text-gray-500 mt-1 whitespace-pre-wrap">{{ s.description || '-' }}</div>
            <ul v-if="s.nextActions?.length" class="mt-2 text-sm text-gray-700 dark:text-gray-200 list-disc pl-5">
              <li v-for="(a, i) in s.nextActions" :key="i">{{ a }}</li>
            </ul>
            <div v-if="s.evidenceIds?.length" class="text-xs text-gray-400 mt-2">
              {{ t('shared.behaviorInsight.evidenceLabel') }}: {{ s.evidenceIds.join(', ') }}
            </div>
          </div>
        </div>
      </div>

      <div v-if="historyDetail.actionRecommendations?.length">
        <div class="flex items-center gap-2 text-sm font-semibold mb-2">
          <sparkles-icon class="w-4 h-4 text-fuchsia-600 dark:text-fuchsia-400" />
          <span>{{ t('shared.behaviorInsight.sections.actionRecommendations') }}</span>
        </div>
        <div class="space-y-2">
          <div
            v-for="(s, idx) in historyDetail.actionRecommendations"
            :key="String(s.title || idx) + '-history'"
            class="rounded-2xl p-4 glass-ultraThin glass-tint-primary border border-white/20 dark:border-white/10"
          >
            <div class="font-medium">{{ s.title || '-' }}</div>
            <div class="text-sm text-gray-500 mt-1 whitespace-pre-wrap">{{ s.description || '-' }}</div>
            <ul v-if="s.nextActions?.length" class="mt-2 text-sm text-gray-700 dark:text-gray-200 list-disc pl-5">
              <li v-for="(a, i) in s.nextActions" :key="i">{{ a }}</li>
            </ul>
            <div v-if="s.evidenceIds?.length" class="text-xs text-gray-400 mt-2">
              {{ t('shared.behaviorInsight.evidenceLabel') }}: {{ s.evidenceIds.join(', ') }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </glass-modal>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import { behaviorInsightApi, type BehaviorInsightResponse } from '@/api/behaviorInsight.api'
import { adminApi } from '@/api/admin.api'
import { ExclamationTriangleIcon, ChartPieIcon, SparklesIcon, LightBulbIcon, DocumentTextIcon } from '@heroicons/vue/24/outline'

const props = defineProps<{
  studentId: string | number
  courseId?: string | number
  range?: string
  adminMode?: boolean
  // UI policy
  allowStudentGenerate?: boolean
  allowTeacherGenerate?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:insight', v: BehaviorInsightResponse | null): void
}>()

const { t, locale } = useI18n()
const loading = ref(false)
const error = ref<string | null>(null)
const insight = ref<BehaviorInsightResponse | null>(null)
const historyVisible = ref(false)
const historyLoading = ref(false)
const historyError = ref<string | null>(null)
const historyItems = ref<Array<{ id: string | number; generatedAt?: string; status?: string; model?: string }>>([])
const historyDetailVisible = ref(false)
const detailLoading = ref(false)
const historyDetailError = ref<string | null>(null)
const historyDetail = ref<BehaviorInsightResponse | null>(null)
const historyReportContext = computed<any>(() => historyDetail.value?.extra?.reportContext || null)
const historyReportStats = computed(() => ({
  aiQuestionCount: Number(historyReportContext.value?.activityStats?.aiQuestionCount ?? 0),
  aiFollowUpCount: Number(historyReportContext.value?.activityStats?.aiFollowUpCount ?? 0),
  assignmentSubmitCount: Number(historyReportContext.value?.activityStats?.assignmentSubmitCount ?? 0),
  assignmentResubmitCount: Number(historyReportContext.value?.activityStats?.assignmentResubmitCount ?? 0),
  communityAskCount: Number(historyReportContext.value?.activityStats?.communityAskCount ?? 0),
  communityAnswerCount: Number(historyReportContext.value?.activityStats?.communityAnswerCount ?? 0),
  feedbackViewCount: Number(historyReportContext.value?.activityStats?.feedbackViewCount ?? 0),
}))

const cooldownActive = computed(() => Boolean(insight.value?.extra?.cooldown?.active))
const nextAvailableAt = computed(() => insight.value?.extra?.cooldown?.nextAvailableAt as string | undefined)

const statusVariant = computed(() => {
  const st = insight.value?.meta?.status
  if (st === 'success') return 'success'
  if (st === 'partial') return 'warning'
  if (st === 'failed') return 'danger'
  return 'info'
})

const canOperate = computed(() => Boolean(props.allowStudentGenerate || props.allowTeacherGenerate))
const isTeacher = computed(() => Boolean(props.allowTeacherGenerate))
const hasInsight = computed(() => Boolean(insight.value))
const reportContext = computed<any>(() => insight.value?.extra?.reportContext || null)
const reportStats = computed(() => ({
  aiQuestionCount: Number(reportContext.value?.activityStats?.aiQuestionCount ?? 0),
  aiFollowUpCount: Number(reportContext.value?.activityStats?.aiFollowUpCount ?? 0),
  assignmentSubmitCount: Number(reportContext.value?.activityStats?.assignmentSubmitCount ?? 0),
  assignmentResubmitCount: Number(reportContext.value?.activityStats?.assignmentResubmitCount ?? 0),
  communityAskCount: Number(reportContext.value?.activityStats?.communityAskCount ?? 0),
  communityAnswerCount: Number(reportContext.value?.activityStats?.communityAnswerCount ?? 0),
  feedbackViewCount: Number(reportContext.value?.activityStats?.feedbackViewCount ?? 0),
}))

const actionLabel = computed(() => {
  // 教师：已有结果 => 重新生成；否则生成
  if (isTeacher.value && hasInsight.value) return t('shared.behaviorInsight.regenerate')
  return t('shared.behaviorInsight.generate')
})

const actionVariant = computed(() => {
  if (isTeacher.value && hasInsight.value) return 'outline'
  return 'primary'
})

function dimensionLabel(code: any): string {
  if (!code) return '-'
  return (t(`shared.behaviorInsight.dimensions.${String(code)}`) as any) || String(code)
}

function levelLabel(level: any): string {
  if (!level) return '-'
  return (t(`shared.behaviorInsight.levels.${String(level)}`) as any) || String(level)
}

function severityVariant(sev: any) {
  const s = String(sev || '').toLowerCase()
  if (s === 'critical') return 'danger'
  if (s === 'warn') return 'warning'
  return 'info'
}

function severityLabel(sev: any): string {
  const s = String(sev || '').toLowerCase()
  return (t(`shared.behaviorInsight.severity.${s}`) as any) || (sev ? String(sev) : '-')
}

function riskTintClass(sev: any): string {
  const s = String(sev || '').toLowerCase()
  if (s === 'critical') return 'glass-tint-danger'
  if (s === 'warn') return 'glass-tint-warning'
  return 'glass-tint-info'
}

function dimensionTintClass(code: any): string {
  const c = String(code || '').toUpperCase()
  switch (c) {
    case 'MORAL_COGNITION': return 'glass-tint-accent border-l-fuchsia-500/70'
    case 'LEARNING_ATTITUDE': return 'glass-tint-success border-l-emerald-500/70'
    case 'LEARNING_ABILITY': return 'glass-tint-info border-l-sky-500/70'
    case 'LEARNING_METHOD': return 'glass-tint-warning border-l-amber-500/70'
    default: return 'glass-tint-secondary'
  }
}

function dimensionDotClass(code: any): string {
  const c = String(code || '').toUpperCase()
  switch (c) {
    case 'MORAL_COGNITION': return 'bg-fuchsia-500/80'
    case 'LEARNING_ATTITUDE': return 'bg-emerald-500/80'
    case 'LEARNING_ABILITY': return 'bg-sky-500/80'
    case 'LEARNING_METHOD': return 'bg-amber-500/80'
    default: return 'bg-gray-400/70'
  }
}

function formatDateTime(input: any): string {
  if (!input) return '-'
  const date = new Date(String(input))
  if (isNaN(date.getTime())) return '-'
  const isEn = String(locale?.value || 'zh-CN').toLowerCase().startsWith('en')
  if (!isEn) {
    const pad2 = (v: number) => (v < 10 ? '0' + v : String(v))
    const m = pad2(date.getMonth() + 1)
    const d = pad2(date.getDate())
    const hh = pad2(date.getHours())
    const mm = pad2(date.getMinutes())
    return `${m}月${d}日 ${hh}:${mm}`
  }
  const opts: Intl.DateTimeFormatOptions = { month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false }
  return new Intl.DateTimeFormat('en-US', opts).format(date).replace(',', '')
}

async function loadLatest() {
  loading.value = true
  error.value = null
  try {
    if (props.adminMode) {
      insight.value = await adminApi.getBehaviorInsightLatest({
        studentId: props.studentId,
        courseId: props.courseId,
        range: props.range || '7d'
      }) as any
    } else {
      insight.value = await behaviorInsightApi.getLatest({
        studentId: props.studentId,
        courseId: props.courseId,
        range: props.range || '7d'
      })
    }
    emit('update:insight', insight.value)
  } catch (e: any) {
    // 未生成时后端可能返回 null => axios 层会直接返回 null；这里兜底
    insight.value = null
    error.value = e?.message ? String(e.message) : null
    emit('update:insight', null)
  } finally {
    loading.value = false
  }
}

async function generate(force: boolean) {
  loading.value = true
  error.value = null
  try {
    if (props.adminMode) {
      insight.value = await adminApi.generateBehaviorInsight({
        studentId: props.studentId,
        courseId: props.courseId,
        range: props.range || '7d',
        force
      }) as any
    } else {
      insight.value = await behaviorInsightApi.generate({
        studentId: props.studentId,
        courseId: props.courseId,
        range: props.range || '7d',
        force
      })
    }
    emit('update:insight', insight.value)
  } catch (e: any) {
    error.value = e?.message ? String(e.message) : String(e || '')
    // 保留原有 insight，不覆盖；但仍同步一次，保证父组件状态一致
    emit('update:insight', insight.value)
  } finally {
    loading.value = false
  }
}

async function onClickAction() {
  const force = Boolean(isTeacher.value && hasInsight.value)
  if (force) {
    const ok = window.confirm(
      (t('shared.common.confirm') as string) || '确认操作？'
    )
    if (!ok) return
  }
  await generate(force)
}

watch(
  () => [props.studentId, props.courseId, props.range],
  () => loadLatest()
)

onMounted(() => loadLatest())

async function openHistory() {
  historyVisible.value = true
  historyLoading.value = true
  historyError.value = null
  historyItems.value = []
  try {
    const res: any = await adminApi.getBehaviorInsightHistory({
      studentId: props.studentId,
      courseId: props.courseId,
      page: 1,
      size: 20
    })
    const items = res?.items || res?.data?.items || []
    historyItems.value = Array.isArray(items) ? items : []
  } catch (e: any) {
    historyError.value = e?.message ? String(e.message) : String(e || '')
  } finally {
    historyLoading.value = false
  }
}

function closeHistory() {
  historyVisible.value = false
}

async function openHistoryDetail(id: string | number) {
  historyDetailVisible.value = true
  detailLoading.value = true
  historyDetailError.value = null
  historyDetail.value = null
  try {
    const res: any = await adminApi.getBehaviorInsightHistoryDetail(id)
    historyDetail.value = (res?.data || res || null) as BehaviorInsightResponse | null
  } catch (e: any) {
    historyDetailError.value = e?.message ? String(e.message) : String(e || '')
  } finally {
    detailLoading.value = false
  }
}

function closeHistoryDetail() {
  historyDetailVisible.value = false
}
</script>

