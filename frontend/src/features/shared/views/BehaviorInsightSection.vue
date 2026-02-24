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
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import { behaviorInsightApi, type BehaviorInsightResponse } from '@/api/behaviorInsight.api'
import { ExclamationTriangleIcon, ChartPieIcon, SparklesIcon, LightBulbIcon, DocumentTextIcon } from '@heroicons/vue/24/outline'

const props = defineProps<{
  studentId: string | number
  courseId?: string | number
  range?: string
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
    insight.value = await behaviorInsightApi.getLatest({
      studentId: props.studentId,
      courseId: props.courseId,
      range: props.range || '7d'
    })
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
    insight.value = await behaviorInsightApi.generate({
      studentId: props.studentId,
      courseId: props.courseId,
      range: props.range || '7d',
      force
    })
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
</script>

