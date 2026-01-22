<template>
  <Card padding="md" tint="secondary">
    <div class="flex items-center justify-between gap-3 mb-3">
      <div class="min-w-0">
        <h3 class="text-lg font-semibold truncate">{{ t('shared.behaviorInsight.title') }}</h3>
        <div class="text-sm text-gray-500 dark:text-gray-400">
          {{ t('shared.behaviorInsight.subtitle') }}
        </div>
      </div>

      <div class="flex items-center gap-2">
        <Badge v-if="insight?.meta?.status" size="sm" :variant="statusVariant">
          {{ t(`shared.behaviorInsight.status.${insight.meta.status}`) }}
        </Badge>
        <Button
          v-if="showGenerateButton"
          variant="primary"
          size="sm"
          :disabled="loading || cooldownActive"
          @click="generate(false)"
        >
          {{ t('shared.behaviorInsight.generate') }}
        </Button>
        <Button
          v-if="showRegenerateButton"
          variant="outline"
          size="sm"
          :disabled="loading"
          @click="generate(true)"
        >
          {{ t('shared.behaviorInsight.regenerate') }}
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
          <div class="text-sm font-semibold mb-2">{{ t('shared.behaviorInsight.sections.explain') }}</div>
          <div class="text-sm text-gray-700 dark:text-gray-200 whitespace-pre-wrap">
            {{ insight.explainScore?.text || '-' }}
          </div>
        </div>

        <!-- Judgements -->
        <div v-if="insight.stageJudgements?.length">
          <div class="text-sm font-semibold mb-2">{{ t('shared.behaviorInsight.sections.judgement') }}</div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div
              v-for="it in insight.stageJudgements"
              :key="String(it.dimensionCode || '')"
              class="border border-white/10 rounded p-3"
            >
              <div class="flex items-center justify-between gap-2">
                <div class="font-medium truncate">{{ dimensionLabel(it.dimensionCode) }}</div>
                <Badge size="sm" variant="info">{{ levelLabel(it.level) }}</Badge>
              </div>
              <div class="text-sm text-gray-500 mt-1 whitespace-pre-wrap">{{ it.rationale || '-' }}</div>
              <div v-if="it.evidenceIds?.length" class="text-xs text-gray-400 mt-2">
                {{ t('shared.behaviorInsight.evidenceLabel') }}: {{ it.evidenceIds.join(', ') }}
              </div>
            </div>
          </div>
        </div>

        <!-- Suggestions -->
        <div v-if="insight.formativeSuggestions?.length">
          <div class="text-sm font-semibold mb-2">{{ t('shared.behaviorInsight.sections.suggestions') }}</div>
          <div class="space-y-2">
            <div
              v-for="(s, idx) in insight.formativeSuggestions"
              :key="String(s.title || idx)"
              class="border border-white/10 rounded p-3"
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
  </Card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import { behaviorInsightApi, type BehaviorInsightResponse } from '@/api/behaviorInsight.api'

const props = defineProps<{
  studentId: string | number
  courseId?: string | number
  range?: string
  // UI policy
  allowStudentGenerate?: boolean
  allowTeacherGenerate?: boolean
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

const showGenerateButton = computed(() => Boolean(props.allowStudentGenerate || props.allowTeacherGenerate))
const showRegenerateButton = computed(() => Boolean(props.allowTeacherGenerate))

function dimensionLabel(code: any): string {
  if (!code) return '-'
  return (t(`shared.behaviorInsight.dimensions.${String(code)}`) as any) || String(code)
}

function levelLabel(level: any): string {
  if (!level) return '-'
  return (t(`shared.behaviorInsight.levels.${String(level)}`) as any) || String(level)
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
  } catch (e: any) {
    // 未生成时后端可能返回 null => axios 层会直接返回 null；这里兜底
    insight.value = null
    error.value = e?.message ? String(e.message) : null
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
  } catch (e: any) {
    error.value = e?.message ? String(e.message) : String(e || '')
  } finally {
    loading.value = false
  }
}

watch(
  () => [props.studentId, props.courseId, props.range],
  () => loadLatest()
)

onMounted(() => loadLatest())
</script>

