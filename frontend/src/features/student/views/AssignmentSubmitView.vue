<template>
  <div class="p-6">
    <div v-if="assignment" class="max-w-4xl mx-auto">
      <nav class="relative z-30 flex items-center space-x-2 text-sm !text-gray-900 dark:!text-gray-100 mb-2">
        <span class="hover:!text-gray-900 dark:hover:!text-gray-100 cursor-pointer font-medium" @click="router.push({ name: 'StudentAssignments' })">
          {{ i18nText('student.assignments.title', 'Assignments') }}
        </span>
        <chevron-right-icon class="w-4 h-4 opacity-70 !text-gray-900 dark:!text-gray-100" />
        <span class="truncate flex-1 font-medium !text-gray-900 dark:!text-gray-100">{{ assignment.title }}</span>
      </nav>
      <page-header :title="assignment.title" :subtitle="assignment.description" />
      <!-- 顶部：信息卡 + 附件卡 并排 -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-2">
        <assignment-info-card :assignment="assignment" :effectiveDue="effectiveDue" :status="displayStatus" />
        <attachment-list :files="teacherAttachments" :title="i18nText('student.assignments.detail.attachmentsTitle', '附件')" />
      </div>
      
      <!-- 统一垂直间距栈 -->
      <div class="space-y-6">
        <!-- Readonly Banner -->
        <div v-if="readOnly" class="p-3 rounded bg-yellow-50 dark:bg-yellow-900/20 text-yellow-800 dark:text-yellow-200 text-sm">
          {{ pastDue ? (t('student.assignments.submit.readOnlyBannerDue') as string) : (t('student.assignments.submit.readOnlyBannerSubmitted') as string) }}
        </div>

        <!-- 中部：提交内容 + 上传附件 并排 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <card>
            <template #header>
              <h2 class="text-xl font-semibold">{{ t('student.assignments.submit.contentTitle') }}</h2>
            </template>
            <glass-textarea v-model="form.content" :rows="10" class="w-full" :disabled="readOnly" :placeholder="t('student.assignments.submit.contentPlaceholder') as string" />
          </card>
          <card>
            <template #header>
              <h2 class="text-xl font-semibold">{{ t('student.assignments.submit.uploadTitle') }}</h2>
            </template>
            <file-upload v-if="!readOnly"
              :accept="'*'"
              :multiple="true"
              :showPreview="true"
              :compact="false"
              :dense="false"
              @files-selected="onFilesSelected"
              @file-removed="onFileRemoved"
            />
            <div v-if="isUploading" class="mt-2 text-sm text-gray-500">{{ t('student.assignments.submit.uploading') }}</div>
            <div v-if="uploadedFiles.length > 0" class="mt-4 space-y-2">
              <h3 class="text-sm font-medium">{{ t('student.assignments.submit.uploadedList') }}</h3>
              <div v-for="file in uploadedFiles" :key="file.id" class="flex justify-between items-center p-2 rounded glass-ultraThin" v-glass="{ strength: 'ultraThin', interactive: false }">
                <span>{{ (file as any).originalName || file.fileName }}</span>
                <div class="flex items-center gap-2">
                  <Button v-if="!readOnly" size="sm" variant="danger" @click="removeFile(file.id)">
                    <template #icon>
                      <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/></svg>
                    </template>
                    {{ t('student.assignments.submit.delete') }}
                  </Button>
                  <Button size="sm" variant="success" @click="downloadSubmissionFile(file)">
                    <template #icon>
                      <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                    </template>
                    {{ i18nText('student.assignments.detail.download', '下载') }}
                  </Button>
                </div>
              </div>
            </div>
          </card>
        </div>

        <!-- Actions (moved up, unique) -->
        <div v-if="!readOnly" class="flex justify-end space-x-4">
          <Button variant="outline" @click="handleSaveDraft" :disabled="disableActions || pastDue">
            <template #icon>
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 3H3v14h14V3zM5 5h10v10H5V5zm2 2h6v2H7V7z" /></svg>
            </template>
            {{ t('student.assignments.submit.saveDraft') }}
          </Button>
          <Button variant="primary" @click="handleSubmit" :disabled="disableActions || pastDue || !form.content">
            <template #icon>
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 13l4 4L19 7" /></svg>
            </template>
            {{ t('student.assignments.submit.submit') }}
          </Button>
        </div>

        <!-- Grade Block (only when graded) -->
        <!-- 底部：成绩与 AI 报告 -->
        <!-- 提示：已提交但未评分 -->
        <div v-if="displayStatus==='SUBMITTED' && !grade" class="p-3 rounded-xl glass-ultraThin" v-glass="{ strength: 'ultraThin', interactive: false }">
          <span class="text-sm text-gray-700 dark:text-gray-300">{{ i18nText('student.assignments.detail.ungradedHint', '作业已提交，等待老师评分。') }}</span>
        </div>

        <card v-if="displayStatus==='GRADED'">
          <h2 class="text-xl font-semibold mb-4">{{ i18nText('student.grades.title', '成绩') }}</h2>

          <!-- Animated score strip -->
          <div class="rounded-xl p-4 glass-ultraThin mb-4" v-glass="{ strength: 'ultraThin', interactive: true }">
            <div class="flex items-center justify-between">
              <div class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ animatedScore.toFixed(1) }}</div>
              <div class="text-sm text-gray-500 dark:text-gray-400">/ {{ totalMaxScore }}{{ i18nText('teacher.grading.history.scoreSuffix', '分') }}</div>
            </div>
            <div class="mt-2 h-2 rounded-full bg-gray-200/60 dark:bg-gray-700/60 overflow-hidden">
              <div class="h-full rounded-full bg-primary-500/70 backdrop-blur-sm transition-all duration-300" :style="{ width: scorePercent + '%' }"></div>
            </div>
            <div class="mt-3 flex items-center gap-2 text-sm">
              <span class="text-gray-600 dark:text-gray-300">{{ i18nText('student.grades.level', '等级') }}</span>
              <span class="px-2 py-0.5 rounded-full glass-ultraThin" v-glass="{ strength: 'ultraThin', interactive: false }">{{ displayLevel }}</span>
              <span class="ml-auto text-gray-500 dark:text-gray-400">{{ i18nText('student.grades.gradedAt', '评分时间') }}：{{ displayGradedAt }}</span>
            </div>
          </div>

          <!-- Teacher feedback -->
          <div class="text-sm text-gray-700 dark:text-gray-300 space-y-3">
            <div v-if="hasFeedback">
              <div class="text-sm font-medium mb-1">{{ i18nText('student.grades.feedback', '教师评语') }}</div>
              <p class="whitespace-pre-line">{{ normalizeText((grade as any)?.feedback) }}</p>
            </div>

            <div v-if="hasStrengths">
              <div class="text-sm font-medium mb-1">{{ i18nText('student.grades.strengths', '优点') }}</div>
              <p class="whitespace-pre-line">{{ normalizeText((grade as any)?.strengths) }}</p>
            </div>
            <div v-if="hasImprovements">
              <div class="text-sm font-medium mb-1">{{ i18nText('student.grades.improvements', '可改进之处') }}</div>
              <p class="whitespace-pre-line">{{ normalizeText((grade as any)?.improvements) }}</p>
            </div>
          </div>
        </card>

        <!-- AI Report -->
        <card v-if="displayStatus==='GRADED' && latestReport">
          <div class="flex items-center mb-4">
            <h2 class="text-xl font-semibold flex-1">{{ i18nText('student.ability.latestReport', 'AI 能力报告') }}</h2>
            <Button size="sm" variant="indigo" @click="openAiDetail" :disabled="!parsedAi"><template #icon><svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="12" cy="12" r="3"></circle></svg></template>{{ i18nText('teacher.aiGrading.viewDetail', '查看详情') }}</Button>
          </div>
          <div class="space-y-3 text-sm">
            <div class="flex items-center gap-3">
              <span class="text-gray-600 dark:text-gray-300">{{ i18nText('student.ability.overall', '综合评分') }}</span>
              <span class="px-2 py-0.5 rounded-full glass-ultraThin" v-glass="{ strength: 'ultraThin', interactive: false }">{{ (latestReport.overallScore ?? 0).toFixed ? latestReport.overallScore.toFixed(1) : latestReport.overallScore }}</span>
              <span class="ml-auto text-gray-500 dark:text-gray-400">{{ new Date(latestReport.createdAt || Date.now()).toLocaleString() }}</span>
            </div>
            <div v-if="dimensionRows.length" class="space-y-2">
              <div v-for="row in dimensionRows" :key="row.code" class="flex items-center gap-3">
                <div class="w-40 text-xs text-gray-700 dark:text-gray-300">{{ row.label }}</div>
                <div class="h-2 flex-1 rounded-md overflow-hidden border border-gray-300/70 dark:border-white/10 bg-gray-200/60 dark:bg-white/10 shadow-inner">
                  <div class="h-full" data-gradient="dimension" :style="{ width: row.percent + '%' }"></div>
                </div>
                <div class="w-10 text-right text-xs">{{ row.value }}</div>
              </div>
            </div>
            <div v-if="latestReport.recommendations" class="mt-2 text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap">
              {{ latestReport.recommendations }}
            </div>
          </div>
        </card>

        <!-- AI Report Detail Modal (full report, reuse teacher history rendering/export) -->
        <glass-modal v-if="aiDetailOpen" :title="i18nText('student.ability.latestReport', 'AI 能力报告（最近一次）')" size="xl" :hideScrollbar="true" heightVariant="max" solidBody @close="aiDetailOpen=false">
          <div v-if="parsedAi" ref="aiDetailRef" data-export-root="1" class="space-y-4">
            <Card padding="sm" tint="secondary">
              <h4 class="font-semibold mb-2">{{ i18nText('teacher.aiGrading.render.overall', '总体') }}</h4>
              <div>
                <div class="text-sm mb-2 flex items-center gap-3" v-if="getOverall(parsedAi)?.final_score != null">
                  <span>{{ i18nText('teacher.aiGrading.render.final_score', '最终分') }}: {{ overallScore(parsedAi) }}</span>
                  <div class="h-2 w-64 rounded-md overflow-hidden border border-gray-300/70 dark:border-white/10 bg-gray-200/60 dark:bg-white/10 shadow-inner">
                    <div class="h-full" data-gradient="overall" :style="{ width: ((Number(overallScore(parsedAi))*20) || 0) + '%' }"></div>
                  </div>
                </div>
                <div class="space-y-2 mb-2" v-if="dimensionBars(parsedAi)">
                  <div class="text-sm font-medium">{{ i18nText('teacher.aiGrading.render.dimension_averages', '维度均分') }}</div>
                  <div v-for="row in dimensionBars(parsedAi)" :key="row.key" class="flex items-center gap-3">
                    <div class="w-40 text-xs text-gray-700 dark:text-gray-300">{{ row.label }}: {{ row.value }}</div>
                    <div class="h-2 flex-1 rounded-md overflow-hidden border border-gray-300/70 dark:border-white/10 bg-gray-200/60 dark:bg-white/10 shadow-inner">
                      <div class="h-full" :data-gradient="dimGradient(row.key)" :style="{ width: ((row.value*20) || 0) + '%' }"></div>
                    </div>
                  </div>
                </div>
                <div class="text-sm whitespace-pre-wrap">{{ i18nText('teacher.aiGrading.render.holistic_feedback', '整体反馈') }}: {{ overallFeedback(parsedAi) || (i18nText('common.empty', '无内容')) }}</div>
              </div>
            </Card>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Card padding="sm" tint="warning" v-if="parsedAi?.moral_reasoning">
              <h4 class="font-semibold mb-2">{{ i18nText('teacher.aiGrading.render.moral_reasoning', '道德推理') }}</h4>
              <div v-html="renderCriterion(parsedAi.moral_reasoning, 'dimension_moral')"></div>
            </Card>
            <Card padding="sm" tint="accent" v-if="parsedAi?.attitude_development">
              <h4 class="font-semibold mb-2">{{ i18nText('teacher.aiGrading.render.attitude_development', '学习态度') }}</h4>
              <div v-html="renderCriterion(parsedAi.attitude_development, 'dimension_attitude')"></div>
            </Card>
            <Card padding="sm" tint="info" v-if="parsedAi?.ability_growth">
              <h4 class="font-semibold mb-2">{{ i18nText('teacher.aiGrading.render.ability_growth', '学习能力') }}</h4>
              <div v-html="renderCriterion(parsedAi.ability_growth, 'dimension_ability')"></div>
            </Card>
            <Card padding="sm" tint="success" v-if="parsedAi?.strategy_optimization">
              <h4 class="font-semibold mb-2">{{ i18nText('teacher.aiGrading.render.strategy_optimization', '学习策略') }}</h4>
              <div v-html="renderCriterion(parsedAi.strategy_optimization, 'dimension_strategy')"></div>
            </Card>
            </div>
          </div>
          <pre v-else class="bg-black/70 text-green-100 p-3 rounded overflow-auto text-xs max-h-[60vh]">{{ pretty(aiRawJson) }}</pre>
          <template #footer>
            <Button size="sm" variant="primary" @click="exportAiDetailAsText" :disabled="!parsedAi"><template #icon><svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21H5a2 2 0 0 1-2-2V7"></path><path d="M9 7V3h6v4"></path><path d="M9 15h6"></path></svg></template>{{ i18nText('teacher.aiGrading.exportText', '导出文本') }}</Button>
            <Button size="sm" variant="success" @click="exportAiDetailAsPng" :disabled="!parsedAi"><template #icon><svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="14" rx="2" ry="2"></rect><path d="M8 21h8"></path><path d="M12 17v4"></path></svg></template>{{ i18nText('teacher.aiGrading.exportPng', '导出 PNG') }}</Button>
            <Button size="sm" variant="purple" @click="exportAiDetailAsPdf" :disabled="!parsedAi"><template #icon><svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2h9l5 5v13a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z"></path><path d="M14 2v6h6"></path></svg></template>{{ i18nText('teacher.aiGrading.exportPdf', '导出 PDF') }}</Button>
            <Button size="sm" variant="secondary" @click="aiDetailOpen=false"><template #icon><svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg></template>{{ i18nText('teacher.aiGrading.picker.close', '关闭') }}</Button>
          </template>
        </glass-modal>

        
      </div>

    </div>
    <div v-else class="text-center py-12">
      <p>{{ t('student.assignments.submit.loading') }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAssignmentStore } from '@/stores/assignment';
import { useSubmissionStore } from '@/stores/submission';
import { useAuthStore } from '@/stores/auth';
import { fileApi } from '@/api/file.api';
import { gradeApi } from '@/api/grade.api';
import { submissionApi } from '@/api/submission.api';
import type { FileInfo } from '@/types/file';
import { useUIStore } from '@/stores/ui';
import FileUpload from '@/components/forms/FileUpload.vue'
// @ts-ignore
import { useI18n } from 'vue-i18n'
import PageHeader from '@/components/ui/PageHeader.vue'
import { baseURL } from '@/api/config'
import { ChevronRightIcon, ArrowDownTrayIcon } from '@heroicons/vue/24/outline'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
import Button from '@/components/ui/Button.vue'
import { abilityApi } from '@/api/ability.api'
import GlassModal from '@/components/ui/GlassModal.vue'
import { exportNodeAsPng, exportNodeAsPdf, applyExportGradientsInline } from '@/shared/utils/exporters'
import Card from '@/components/ui/Card.vue'
import AssignmentInfoCard from '@/features/shared/AssignmentInfoCard.vue'
import AttachmentList from '@/features/shared/AttachmentList.vue'

const route = useRoute();
const router = useRouter();
const assignmentStore = useAssignmentStore();
const submissionStore = useSubmissionStore();
const authStore = useAuthStore();
const uiStore = useUIStore();
const { t } = useI18n();

const form = reactive({
  content: '',
  fileIds: [] as string[],
});

const uploadedFiles = ref<FileInfo[]>([]);
const isUploading = ref(false);
const grade = ref<any | null>(null);
const teacherAttachments = ref<any[]>([])
const latestReport = ref<any|null>(null)
const dimensionRows = ref<Array<{ code: string; label: string; value: number; percent: number }>>([])
const aiDetailOpen = ref(false)
const aiDetailRef = ref<HTMLElement | null>(null)
const aiRawJson = ref<any>(null)
function i18nText(key: string, fallback: string): string {
  try {
    const msg = (t(key) as any) as string
    return (msg && msg !== key) ? msg : fallback
  } catch {
    return fallback
  }
}
function formatSize(bytes?: number) {
  if (!bytes || bytes <= 0) return '-'
  const units = ['B','KB','MB','GB']
  let i = 0
  let n = bytes
  while (n >= 1024 && i < units.length - 1) { n /= 1024; i++ }
  return `${n.toFixed(1)} ${units[i]}`
}

async function downloadSubmissionFile(file: any) {
  try {
    const id = String((file as any)?.id || (file as any)?.fileId || '')
    if (!id) return
    const name = (file as any)?.originalName || (file as any)?.fileName || `file_${id}`
    await fileApi.downloadFile(id, name)
  } catch (e) {
    try {
      const id = String((file as any)?.id || (file as any)?.fileId || '')
      if (!id) return
      const a = document.createElement('a')
      a.href = `${baseURL}/files/${encodeURIComponent(id)}/download`
      a.download = (file as any)?.originalName || (file as any)?.fileName || `file_${id}`
      document.body.appendChild(a)
      a.click()
      a.remove()
    } catch {}
  }
}

const assignment = computed(() => assignmentStore.selectedAssignment);
const submission = computed(() => {
    const assignmentId = route.params.id as string;
    return submissionStore.submissions.get(assignmentId);
});

// 有“打回重做”则以个性化重交截止覆盖 dueDate
const effectiveDue = computed(() => {
  const g: any = grade.value
  if (g && String(g.status || '').toLowerCase() === 'returned' && g.resubmitUntil) return g.resubmitUntil
  return (assignment.value as any)?.dueDate
})
const dueLabel = computed(() => {
  const g: any = grade.value
  if (g && String(g.status || '').toLowerCase() === 'returned' && g.resubmitUntil) return (t('student.assignments.resubmitUntil') as any) || '重交截止'
  return t('student.assignments.due') as any
})
const pastDue = computed(() => {
  const ts = effectiveDue.value as any
  if (!ts) return false
  const d = new Date(ts)
  if (isNaN(d.getTime())) return false
  return Date.now() > d.getTime()
})

// Animated score like teacher view
const animatedScore = ref(0)
const totalMaxScore = computed(() => Number((assignment.value as any)?.maxScore || (assignment.value as any)?.totalScore || 100))
const scorePercent = computed(() => {
  const max = Number(totalMaxScore.value || 100)
  const val = Number(animatedScore.value || 0)
  return max > 0 ? Math.min(100, Math.max(0, (val / max) * 100)) : 0
})
function pickLevelByScore(score: number, max: number) {
  const pct = max > 0 ? (score / max) * 100 : 0
  if (pct >= 95) return 'A+'
  if (pct >= 90) return 'A'
  if (pct >= 85) return 'B+'
  if (pct >= 80) return 'B'
  if (pct >= 70) return 'C+'
  if (pct >= 60) return 'C'
  if (pct >= 50) return 'D'
  return 'F'
}
const displayLevel = computed(() => pickLevelByScore(Number(grade.value?.score || 0), Number(totalMaxScore.value || 100)))
function normalizeText(v: any) { return Array.isArray(v) ? v.join('\n') : String(v || '') }
const hasStrengths = computed(() => !!normalizeText((grade.value as any)?.strengths))
const hasImprovements = computed(() => !!normalizeText((grade.value as any)?.improvements))
const hasFeedback = computed(() => !!normalizeText((grade.value as any)?.feedback))

// 评分时间展示（多字段兜底）
const displayGradedAt = computed(() => {
  const g: any = grade.value || {}
  const s: any = submission.value || {}
  const ts = g.gradedAt || g.updatedAt || g.reviewedAt || g.createdAt || s.gradedAt || s.updatedAt || s.createdAt
  return ts ? new Date(ts).toLocaleString() : '-'
})

// 进入只读：过期或已提交或已评分（但若被打回且未过重交截止，则可编辑）
const readOnly = computed(() => {
  const s = ((submission.value as any)?.status || '').toUpperCase()
  const gstatus = String((grade.value as any)?.status || '').toUpperCase()
  // 被打回且未过重交截止：可编辑
  if (gstatus === 'RETURNED' && !pastDue.value) return false
  // 已过截止：只读
  if (pastDue.value) return true
  // 已评分：只读
  if (s === 'GRADED') return true
  // 已提交但被打回：可编辑（注意此时 s 可能仍是 SUBMITTED，但以 gstatus 为准）
  if (s === 'SUBMITTED' && gstatus === 'RETURNED' && !pastDue.value) return false
  // 其他已提交：只读
  if (s === 'SUBMITTED') return true
  return false
})

// 禁用条件：正在加载 或 已提交/已评分（但若被打回且未过重交截止，则可操作）
const disableActions = computed(() => {
  const s = (submission.value as any)?.status || ''
  const upper = String(s || '').toUpperCase()
  const gstatus = String((grade.value as any)?.status || '').toUpperCase()
  if (submissionStore.loading) return true
  if (gstatus === 'RETURNED' && !pastDue.value) return false
  if (upper === 'SUBMITTED' || upper === 'GRADED') return true
  return false
})

// 归一化状态到前端显示语义（含被打回）
const normalizeStatus = (raw?: string) => {
  const s = (raw || '').toUpperCase()
  if (!s || s === 'NOT_SUBMITTED') return 'PENDING'
  if (s === 'PUBLISHED') return 'PENDING'
  if (s === 'SUBMITTED') return 'SUBMITTED'
  if (s === 'GRADED') return 'GRADED'
  if (s === 'LATE') return 'LATE'
  if (s === 'RETURNED') return 'RETURNED'
  return 'UNKNOWN'
}

// 用于模板显示的状态
const displayStatus = computed(() => normalizeStatus((submission.value as any)?.status))

const statusClass = (status: string | undefined) => {
  switch (status) {
    case 'PENDING': return 'bg-yellow-100 text-yellow-800';
    case 'SUBMITTED': return 'bg-blue-100 text-blue-800';
    case 'LATE': return 'bg-red-100 text-red-800';
    case 'GRADED': return 'bg-green-100 text-green-800';
    case 'RETURNED': return 'bg-amber-100 text-amber-800';
    default: return 'bg-gray-100 text-gray-800';
  }
};

// 本地化状态文案
const statusText = (status: string | undefined) => {
  const s = (status || 'UNKNOWN').toUpperCase()
  if (s === 'PENDING') return t('student.assignments.status.pending') as string
  if (s === 'SUBMITTED') return t('student.assignments.status.submitted') as string
  if (s === 'GRADED') return t('student.assignments.status.graded') as string
  if (s === 'RETURNED') return i18nText('student.assignments.status.returned', '已退回')
  return t('student.assignments.status.unknown') as string
}

const onFilesSelected = async (files: File[]) => {
  if (!files || files.length === 0) return;
  const assignmentId = route.params.id as string;
  isUploading.value = true;
  try {
    // 顺序上传，收集返回的 fileId
    for (const f of files) {
      const info = await fileApi.uploadFile(f, { purpose: 'submission', relatedId: assignmentId });
      if (info) {
        uploadedFiles.value.push(info as FileInfo);
        const newId = String((info as any).id ?? (info as any).fileId ?? '');
        if (newId) form.fileIds.push(newId);
      }
    }
  } catch (e) {
    uiStore.showNotification({ type: 'error', title: t('student.assignments.submit.errors.uploadFailedTitle') as string, message: t('student.assignments.submit.errors.uploadFailedMsg') as string });
  } finally {
    isUploading.value = false;
  }
};

const onFileRemoved = async (_file: File, _index: number) => {
  // 保持与下方 removeFile 一致的行为：如果已上传过，调用删除接口
  try {
    const id = String((_file as any)?.id || (_file as any)?.fileId || '');
    if (id) await fileApi.deleteFile(id);
  } catch {}
};

const removeFile = async (fileId: string) => {
  try {
    await fileApi.deleteFile(fileId);
    uploadedFiles.value = uploadedFiles.value.filter(f => f.id !== fileId);
    form.fileIds = form.fileIds.filter(id => id !== fileId);
  } catch (error) {
     uiStore.showNotification({ type: 'error', title: t('student.assignments.submit.errors.deleteFailedTitle') as string, message: t('student.assignments.submit.errors.deleteFailedMsg') as string });
  }
};

const handleSaveDraft = async () => {
  const assignmentId = route.params.id as string;
  await submissionStore.saveDraft(assignmentId, form);
  await submissionStore.fetchSubmissionForAssignment(assignmentId);
};

const handleSubmit = async () => {
  if (!form.content) {
    uiStore.showNotification({ type: 'error', title: t('student.assignments.submit.errors.contentEmptyTitle') as string, message: t('student.assignments.submit.errors.contentEmptyMsg') as string });
    return;
  }
  const assignmentId = route.params.id as string;
  const res = await submissionStore.submitAssignment(assignmentId, form);
  if (res) {
    await submissionStore.fetchSubmissionForAssignment(assignmentId);
  }
};

onMounted(async () => {
  const assignmentId = route.params.id as string;
  let resolvedId = assignmentId
  try {
    await assignmentStore.fetchAssignmentById(resolvedId);
  } catch (e) {
    // 兜底：若当作作业ID获取失败，尝试将路由参数当作“gradeId”，反查 assignmentId 后重定向
    try {
      const gr: any = await gradeApi.getGradeById(assignmentId)
      const aid = String((gr as any)?.assignmentId || (gr as any)?.assignment_id || '')
      if (aid) {
        resolvedId = aid
        await assignmentStore.fetchAssignmentById(resolvedId)
        if (resolvedId !== assignmentId) router.replace(`/student/assignments/${resolvedId}/submit`)
      }
    } catch {}
    // 若不是成绩ID，再尝试按提交ID反查作业ID
    if (!assignment.value) {
      try {
        const sres: any = await submissionApi.getSubmissionById(assignmentId)
        const sidData = (sres && sres.data !== undefined) ? sres.data : sres
        const aid2 = String((sidData as any)?.assignmentId || (sidData as any)?.assignment_id || '')
        if (aid2) {
          resolvedId = aid2
          await assignmentStore.fetchAssignmentById(resolvedId)
          if (resolvedId !== assignmentId) router.replace(`/student/assignments/${resolvedId}/submit`)
        }
      } catch {}
    }
  }
  if (!assignment.value) {
    uiStore.showNotification({ type: 'error', title: i18nText('student.assignments.detail.notFoundTitle', '作业不存在'), message: i18nText('student.assignments.detail.notFoundMsg', '该作业可能已被删除或不可访问。') })
    return router.push('/student/assignments')
  }
  await submissionStore.fetchSubmissionForAssignment(resolvedId);

  // 拉取教师附件
  try {
    const res: any = await fileApi.getRelatedFiles('assignment_attachment', resolvedId)
    teacherAttachments.value = res?.data || res || []
  } catch {}

  if (submission.value) {
    form.content = (submission.value as any)?.content || '';
    const ids = (submission.value as any)?.fileIds;
    form.fileIds = Array.isArray(ids) ? ids.map((id: any) => String(id)) : [];
    // If files exist, fetch their info to display
    if (Array.isArray(form.fileIds) && form.fileIds.length > 0) {
      try {
      const fileInfos = await Promise.all(form.fileIds.map(id => fileApi.getFileInfo(id)));
      uploadedFiles.value = (fileInfos as any[]).filter(Boolean) as any;
      } catch {}
    }
    // 无 fileIds 或补充：直接按 submission 关联读取（确保刷新后也能看到附件）
    try {
      const rel: any = await fileApi.getRelatedFiles('submission', String((submission.value as any)?.id))
      const list: any[] = (rel?.data || rel || []) as any[]
      if (Array.isArray(list) && list.length) {
        uploadedFiles.value = list
        form.fileIds = list.map((f: any) => String(f?.id || ''))
      }
    } catch {}
  }
  // 拉取成绩（用于判断是否被打回/重交截止，以及展示成绩）
  try {
      const sid = authStore.user?.id
      if (sid) {
        const res: any = await gradeApi.getGradeForStudentAssignment(String(sid), resolvedId)
        grade.value = (res && res.data !== undefined) ? res.data : res
      if (displayStatus.value === 'GRADED') {
        const target = Number((grade.value as any)?.score || 0)
        const start = Number(animatedScore.value || 0)
        const duration = 300
        const startTs = performance.now()
        const tick = (now: number) => {
          const p = Math.min(1, (now - startTs) / duration)
          animatedScore.value = start + (target - start) * p
          if (p < 1) requestAnimationFrame(tick)
        }
        requestAnimationFrame(tick)
      }
    }
  } catch {}

  // 拉取最新能力报告供展示（若存在）
  try {
    // 优先按上下文获取与本作业/提交关联的最新AI报告
    const ctxParams: any = {}
    const sub = submission.value as any
    if (sub?.id) ctxParams.submissionId = String(sub.id)
    if ((assignment.value as any)?.id) ctxParams.assignmentId = String((assignment.value as any).id)
    if ((assignment.value as any)?.courseId) ctxParams.courseId = String((assignment.value as any).courseId)
    let rep: any
    try {
      rep = await abilityApi.getStudentLatestReportByContext(ctxParams)
    } catch {
      rep = await abilityApi.getStudentLatestReport()
    }
    const data = (rep && rep.data !== undefined) ? rep.data : rep
    latestReport.value = data || null
    // 供“查看详情”使用：从能力报告读取完整规范化JSON
    if (latestReport.value) {
      aiRawJson.value = (latestReport.value as any).trendsAnalysis || (latestReport.value as any).trends_analysis || (latestReport.value as any).normalizedJson || (latestReport.value as any).rawJson || (latestReport.value as any).raw_json || null
      // 若整体评价缺失，用报告 recommendations 兜底（保证与教师端显示一致）
      try {
        if (aiRawJson.value) {
          const obj = safeJsonParseLoose(aiRawJson.value)
          const ov: any = (obj && typeof obj === 'object') ? (obj.overall || obj) : null
          const hasHolistic = !!(ov && typeof ov.holistic_feedback === 'string' && ov.holistic_feedback.trim() !== '')
          const rec = String((latestReport.value as any).recommendations || '').trim()
          if (!hasHolistic && rec) {
            if (obj && typeof obj === 'object') {
              if (obj.overall && typeof obj.overall === 'object') obj.overall.holistic_feedback = rec
              else (obj as any).holistic_feedback = rec
              aiRawJson.value = JSON.stringify(obj)
            }
          }
        }
      } catch {}
    }
    if (latestReport.value && latestReport.value.dimensionScores) {
      let scores: any = latestReport.value.dimensionScores
      if (typeof scores === 'string') {
        try { scores = JSON.parse(scores) } catch { scores = null }
      }
      if (scores && typeof scores === 'object') {
      const mapping: Array<{ code: string; key: string; label: string }> = [
          { code: 'MORAL_COGNITION', key: 'moral_reasoning', label: i18nText('teacher.analytics.weights.dimensions.MORAL_COGNITION', '道德认知') },
          { code: 'LEARNING_ATTITUDE', key: 'attitude', label: i18nText('teacher.analytics.weights.dimensions.LEARNING_ATTITUDE', '学习态度') },
          { code: 'LEARNING_ABILITY', key: 'ability', label: i18nText('teacher.analytics.weights.dimensions.LEARNING_ABILITY', '学习能力') },
          { code: 'LEARNING_METHOD', key: 'strategy', label: i18nText('teacher.analytics.weights.dimensions.LEARNING_METHOD', '学习方法') },
          // 学习成绩不在学生端 AI 报告区块展示，由教师评分单独体现
        ]
        dimensionRows.value = mapping
          .map(m => {
            const raw = Number(scores[m.key] ?? scores[m.code] ?? 0)
            const value = Number.isFinite(raw) ? raw : 0
            const percent = Math.max(0, Math.min(100, Math.round((value / 5) * 100)))
            return { code: m.code, label: m.label, value, percent }
          })
      }
    }
  } catch {}
});

// 打开完整AI报告弹窗
function openAiDetail() {
  if (!aiRawJson.value && latestReport.value) {
    aiRawJson.value = (latestReport.value as any).trendsAnalysis || (latestReport.value as any).trends_analysis || (latestReport.value as any).normalizedJson || (latestReport.value as any).rawJson || (latestReport.value as any).raw_json || null
  }
  aiDetailOpen.value = true
}

// 解析/归一化/渲染函数（复用老师历史页精简版）
const parsedAi = computed(() => {
  try {
    if (!aiRawJson.value) return null
    const obj = safeJsonParseLoose(aiRawJson.value)
    return normalizeAssessment(obj)
  } catch { return null }
})

function pretty(v: any) { try { return JSON.stringify(JSON.parse(String(v||'')), null, 2) } catch { return String(v||'') } }

function safeJsonParseLoose(raw: any): any {
  if (raw && typeof raw === 'object') return raw
  let s = String(raw || '')
  s = s.replace(/^\uFEFF/, '').trim()
  const fence = /```(?:json)?\s*([\s\S]*?)```/i
  const m = s.match(fence)
  if (m && m[1]) s = m[1].trim()
  const first = s.indexOf('{')
  const last = s.lastIndexOf('}')
  if (first >= 0 && last > first) s = s.substring(first, last + 1)
  s = s.replace(/,\s*}/g, '}').replace(/,\s*]/g, ']')
  return JSON.parse(s)
}

function getOverall(obj: any): any {
  if (!obj || typeof obj !== 'object') return null
  if ((obj as any).overall) return (obj as any).overall
  if ((obj as any).final_score || (obj as any).holistic_feedback || (obj as any).dimension_averages) return obj
  return null
}
function overallScore(obj: any): number {
  const ov = getOverall(obj)
  const n = Number(ov?.final_score)
  return Number.isFinite(n) ? n : 0
}
function overallFeedback(obj: any): string {
  const ov = getOverall(obj)
  return String(ov?.holistic_feedback || '')
}
function dimensionBars(obj: any): Array<{ key: string; label: string; value: number }> | null {
  try {
    const ov = getOverall(obj)
    const dm = ov?.dimension_averages
    if (!dm) return null
    return [
      { key: 'moral_reasoning', label: i18nText('teacher.aiGrading.render.moral_reasoning', '道德推理'), value: Number(dm.moral_reasoning ?? 0) },
      { key: 'attitude', label: i18nText('teacher.aiGrading.render.attitude_development', '学习态度'), value: Number(dm.attitude ?? 0) },
      { key: 'ability', label: i18nText('teacher.aiGrading.render.ability_growth', '学习能力'), value: Number(dm.ability ?? 0) },
      { key: 'strategy', label: i18nText('teacher.aiGrading.render.strategy_optimization', '学习策略'), value: Number(dm.strategy ?? 0) }
    ]
  } catch { return null }
}
function renderCriterion(block: any, barKind?: 'dimension' | 'dimension_moral' | 'dimension_attitude' | 'dimension_ability' | 'dimension_strategy') {
  try {
    const sections: string[] = []
    for (const [k, v] of Object.entries(block || {})) {
      const sec = v as any
      const score = sec?.score
      const ev = Array.isArray(sec?.evidence) ? sec.evidence : []
      const sug = Array.isArray(sec?.suggestions) ? sec.suggestions : []
      const which = barKind || 'dimension'
      const bar = typeof score === 'number' ? `<div class=\"h-2 w-40 rounded-md overflow-hidden border border-gray-300/70 dark:border-white/10 bg-gray-200/60 dark:bg-white/10 shadow-inner\"><div class=\"h-full\" data-gradient=\"${which}\" style=\"width:${score*20}%\"></div></div>` : ''
      const firstReasoning = (ev.find((e: any) => e && String(e.reasoning || '').trim()) || {}).reasoning || ''
      const firstConclusion = (ev.find((e: any) => e && String(e.conclusion || '').trim()) || {}).conclusion || ''
      const evid = ev
        .filter((e: any) => (e && (e.quote || e.explanation)))
        .map((e: any) => `<li class="mb-1">
            <div class="text-xs text-gray-600 dark:text-gray-300">${e.quote ? '“'+escapeHtml(e.quote)+'”' : ''}</div>
            ${e.explanation?`<div class="text-[11px] text-gray-500">${escapeHtml(e.explanation)}</div>`:''}</li>`) 
        .join('')
      const reasoningBlock = firstReasoning ? `<div class="text-xs"><span class="font-semibold">Reasoning:</span> ${escapeHtml(firstReasoning)}</div>` : ''
      const conclusionBlock = firstConclusion ? `<div class="text-xs italic text-gray-600 dark:text-gray-300"><span class="not-italic font-semibold">Conclusion:</span> ${escapeHtml(firstConclusion)}</div>` : ''
      const sugg = sug.map((s: any) => `<li class="mb-1 text-xs">${escapeHtml(String(s))}</li>`).join('')
      sections.push(`<div class="space-y-2"><div class="text-sm font-medium">${escapeHtml(String(k))} ${score!=null?`(${score}/5)`:''}</div>${bar}<div><div class="text-xs font-semibold mt-2">Evidence</div><ul>${evid}</ul>${reasoningBlock}${conclusionBlock}</div><div><div class="text-xs font-semibold mt-2">Suggestions</div><ul>${sugg}</ul></div></div>`)
    }
    return sections.join('')
  } catch { return `<pre class="text-xs">${escapeHtml(pretty(block))}</pre>` }
}
function dimGradient(key: string): 'dimension' | 'overall' | 'dimension_moral' | 'dimension_attitude' | 'dimension_ability' | 'dimension_strategy' {
  const k = String(key || '').toLowerCase()
  if (k.includes('moral') || k.includes('stage') || k.includes('argument') || k.includes('foundation')) return 'dimension_moral'
  if (k.includes('attitude') || k.includes('emotion') || k.includes('resilience') || k.includes('flow')) return 'dimension_attitude'
  if (k.includes('ability') || k.includes('bloom') || k.includes('metacognition') || k.includes('transfer')) return 'dimension_ability'
  if (k.includes('strategy') || k.includes('diversity') || k.includes('depth') || k.includes('regulation')) return 'dimension_strategy'
  return 'dimension'
}
function escapeHtml(s: string) {
  return String(s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function normalizeAssessment(obj: any) {
  try {
    if (obj == null) return obj
    if (typeof obj === 'string') return obj
    if (Array.isArray((obj as any).evaluation_result)) {
      return normalizeFromEvaluationArray((obj as any).evaluation_result)
    }
    if ((obj as any).evaluation && typeof (obj as any).evaluation === 'object') {
      const ev = (obj as any).evaluation
      if (ev?.dimensions) return normalizeFromEvaluationDimensions(ev)
      return normalizeFromEvaluationObject(ev)
    }
    if ((obj as any).evaluation_results && typeof (obj as any).evaluation_results === 'object') {
      return normalizeFromEvaluationResults((obj as any).evaluation_results)
    }
    const hasCore = (o: any) => !!(o && (o.moral_reasoning || o.attitude_development || o.ability_growth || o.strategy_optimization || o.overall))
    if (hasCore(obj)) return obj
    return obj
  } catch { return obj }
}

function normalizeFromEvaluationArray(arr: any[]) {
  const out: any = { moral_reasoning: {}, attitude_development: {}, ability_growth: {}, strategy_optimization: {} }
  const getSec = (raw: any) => {
    const score = Number(raw?.score ?? 0)
    const ev = raw?.evidence
    const evidence = Array.isArray(ev?.quotes)
      ? ev.quotes.map((q: any) => ({ quote: String(q), reasoning: String(ev?.reasoning || ''), conclusion: String(ev?.conclusion || '') }))
      : [{ quote: '', reasoning: String(typeof ev === 'string' ? ev : ''), conclusion: '' }]
    const sraw = raw?.suggestions
    const suggestions = Array.isArray(sraw) ? sraw : (sraw ? [String(sraw)] : [])
    return { score, evidence, suggestions }
  }
  const mapDim = (name: string) => {
    const n = (name || '').toLowerCase()
    if (n.includes('moral')) return 'moral_reasoning'
    if (n.includes('attitude')) return 'attitude_development'
    if (n.includes('ability')) return 'ability_growth'
    if (n.includes('strategy')) return 'strategy_optimization'
    return ''
  }
  const mapId = (dimKey: string, id: string) => {
    const i = (id || '').toUpperCase()
    const tbl: Record<string, Record<string, string>> = {
      moral_reasoning: { '1A': 'stage_level', '1B': 'foundations_balance', '1C': 'argument_chain' },
      attitude_development: { '2A': 'emotional_engagement', '2B': 'resilience', '2C': 'focus_flow' },
      ability_growth: { '3A': 'blooms_level', '3B': 'metacognition', '3C': 'transfer' },
      strategy_optimization: { '4A': 'diversity', '4B': 'depth', '4C': 'self_regulation' }
    }
    return tbl[dimKey]?.[i] || ''
  }
  for (const g of (arr || [])) {
    const dimKey = mapDim(g?.dimension || '')
    if (!dimKey) continue
    const bucket: any = out[dimKey]
    const items = Array.isArray(g?.sub_criteria) ? g.sub_criteria : []
    for (const it of items) {
      const secKey = mapId(dimKey, String(it?.id || ''))
      if (!secKey) continue
      bucket[secKey] = getSec(it)
    }
  }
  const avg = (nums: number[]) => { const arrv = nums.filter(n => Number.isFinite(n)); if (!arrv.length) return 0; return Math.round((arrv.reduce((a, b) => a + b, 0) / arrv.length) * 10) / 10 }
  const mrAvg = avg([ Number(out.moral_reasoning?.stage_level?.score), Number(out.moral_reasoning?.foundations_balance?.score), Number(out.moral_reasoning?.argument_chain?.score) ])
  const adAvg = avg([ Number(out.attitude_development?.emotional_engagement?.score), Number(out.attitude_development?.resilience?.score), Number(out.attitude_development?.focus_flow?.score) ])
  const agAvg = avg([ Number(out.ability_growth?.blooms_level?.score), Number(out.ability_growth?.metacognition?.score), Number(out.ability_growth?.transfer?.score) ])
  const soAvg = avg([ Number(out.strategy_optimization?.diversity?.score), Number(out.strategy_optimization?.depth?.score), Number(out.strategy_optimization?.self_regulation?.score) ])
  const finalScore = avg([mrAvg, adAvg, agAvg, soAvg])
  out.overall = { dimension_averages: { moral_reasoning: mrAvg, attitude: adAvg, ability: agAvg, strategy: soAvg }, final_score: finalScore, holistic_feedback: '' }
  return out
}

function normalizeFromEvaluationObject(evaluation: any) {
  const toSec = (raw: any) => {
    const score = Number(raw?.score ?? 0)
    const ev = raw?.evidence
    const evidence = Array.isArray(ev?.quotes)
      ? ev.quotes.map((q: any) => ({ quote: String(q), reasoning: String(ev?.reasoning || ''), conclusion: String(ev?.conclusion || '') }))
      : [{ quote: '', reasoning: String(typeof ev === 'string' ? ev : ''), conclusion: '' }]
    const sraw = raw?.suggestions
    const suggestions = Array.isArray(sraw) ? sraw : (sraw ? [String(sraw)] : [])
    return { score, evidence, suggestions }
  }
  const findGroup = (obj: any, hint: string) => { const k = Object.keys(obj || {}).find(k => k.toLowerCase().includes(hint)); return k ? obj[k] : undefined }
  const findSubKey = (obj: any, hints: string[]) => {
    const keys = Object.keys(obj || {})
    const lowerHints = hints.map(h => String(h).toLowerCase())
    const hit = keys.find(k => lowerHints.every(h => k.toLowerCase().includes(h)))
    return hit ? obj[hit] : undefined
  }
  const moral = evaluation["1) Moral Reasoning Maturity"] || findGroup(evaluation, 'moral reasoning') || {}
  const attitude = evaluation["2) Learning Attitude Development"] || findGroup(evaluation, 'attitude') || {}
  const ability = evaluation["3) Learning Ability Growth"] || findGroup(evaluation, 'ability') || {}
  const strategy = evaluation["4) Learning Strategy Optimization"] || findGroup(evaluation, 'strategy') || {}
  const out: any = {}
  out.moral_reasoning = { stage_level: toSec(moral['1A. Stage Level Identification'] || moral['1A']), foundations_balance: toSec(moral['1B. Breadth of Moral Foundations'] || moral['1B']), argument_chain: toSec(moral['1C. Argument Chains and Counter-arguments'] || moral['1C']) }
  out.attitude_development = { emotional_engagement: toSec(attitude['2A. Emotional Engagement'] || attitude['2A']), resilience: toSec(attitude['2B. Persistence/Resilience'] || attitude['2B']), focus_flow: toSec(attitude['2C. Task Focus / Flow'] || attitude['2C']) }
  out.ability_growth = {
    blooms_level: toSec(
      ability['3A. Bloom\'s Taxonomy Progression'] ||
      ability['3A. Bloom’s Taxonomy Progression'] ||
      findSubKey(ability, ['3a','bloom']) ||
      ability['3A']
    ),
    metacognition: toSec(ability['3B. Metacognition (Plan–Monitor–Revise)'] || ability['3B']),
    transfer: toSec(ability['3C. Knowledge Transfer'] || ability['3C'])
  }
  out.strategy_optimization = { diversity: toSec(strategy['4A. Strategy Diversity'] || strategy['4A']), depth: toSec(strategy['4B. Depth of Processing'] || strategy['4B']), self_regulation: toSec(strategy['4C. Self-Regulation'] || strategy['4C']) }
  const avg = (nums: number[]) => { const arr = nums.filter(n => Number.isFinite(n)); if (!arr.length) return 0; return Math.round((arr.reduce((a, b) => a + b, 0) / arr.length) * 10) / 10 }
  const mrAvg = avg([ Number(out.moral_reasoning?.stage_level?.score), Number(out.moral_reasoning?.foundations_balance?.score), Number(out.moral_reasoning?.argument_chain?.score) ])
  const adAvg = avg([ Number(out.attitude_development?.emotional_engagement?.score), Number(out.attitude_development?.resilience?.score), Number(out.attitude_development?.focus_flow?.score) ])
  const agAvg = avg([ Number(out.ability_growth?.blooms_level?.score), Number(out.ability_growth?.metacognition?.score), Number(out.ability_growth?.transfer?.score) ])
  const soAvg = avg([ Number(out.strategy_optimization?.diversity?.score), Number(out.strategy_optimization?.depth?.score), Number(out.strategy_optimization?.self_regulation?.score) ])
  const finalScore = avg([mrAvg, adAvg, agAvg, soAvg])
  out.overall = { dimension_averages: { moral_reasoning: mrAvg, attitude: adAvg, ability: agAvg, strategy: soAvg }, final_score: finalScore, holistic_feedback: '' }
  return out
}

function normalizeFromEvaluationResults(er: any) {
  const lcKeys = Object.keys(er || {})
  const findKey = (includes: string[]) => { const inc = includes.map(s => s.toLowerCase()); return lcKeys.find(k => inc.every(t => k.toLowerCase().includes(t))) }
  const getSec = (group: any, hints: string[]) => {
    if (!group || typeof group !== 'object') return undefined
    const keys = Object.keys(group)
    const target = keys.find(k => hints.every(h => k.toLowerCase().includes(h)))
    const sec = target ? group[target] : undefined
    if (!sec || typeof sec !== 'object') return undefined
    const score = Number((sec as any).score ?? 0)
    const rawEv = (sec as any).evidence
    const evArr = Array.isArray(rawEv) ? rawEv : (rawEv ? [rawEv] : [])
    const evidence = evArr.map((e: any) => { if (e && typeof e === 'object') return e; return { quote: '', reasoning: String(e ?? ''), conclusion: '' } })
    const rawSug = (sec as any).suggestions
    const suggestions = Array.isArray(rawSug) ? rawSug : (rawSug ? [rawSug] : [])
    return { score, evidence, suggestions }
  }
  const pickGroup = (tokens: string[]) => { const k = findKey(tokens); return k ? er[k] : undefined }
  const moral = pickGroup(['moral', 'reasoning'])
  const attitude = pickGroup(['attitude']) || pickGroup(['learning', 'attitude']) || pickGroup(['attitude', 'development'])
  const ability = pickGroup(['ability', 'growth']) || pickGroup(['learning', 'ability'])
  const strategy = pickGroup(['strategy']) || pickGroup(['strategy', 'optimization'])
  const out: any = {}
  out.moral_reasoning = { stage_level: getSec(moral, ['stage', 'level']) || { score: 0, evidence: [], suggestions: [] }, foundations_balance: getSec(moral, ['foundation']) || { score: 0, evidence: [], suggestions: [] }, argument_chain: getSec(moral, ['argument']) || getSec(moral, ['counter']) || { score: 0, evidence: [], suggestions: [] } }
  out.attitude_development = { emotional_engagement: getSec(attitude, ['emotional']) || { score: 0, evidence: [], suggestions: [] }, resilience: getSec(attitude, ['resilience']) || getSec(attitude, ['persistence']) || { score: 0, evidence: [], suggestions: [] }, focus_flow: getSec(attitude, ['flow']) || getSec(attitude, ['focus']) || getSec(attitude, ['task']) || { score: 0, evidence: [], suggestions: [] } }
  out.ability_growth = { blooms_level: getSec(ability, ['bloom']) || getSec(ability, ['taxonomy']) || { score: 0, evidence: [], suggestions: [] }, metacognition: getSec(ability, ['metacognition']) || { score: 0, evidence: [], suggestions: [] }, transfer: getSec(ability, ['transfer']) || { score: 0, evidence: [], suggestions: [] } }
  out.strategy_optimization = { diversity: getSec(strategy, ['diversity']) || { score: 0, evidence: [], suggestions: [] }, depth: getSec(strategy, ['depth']) || { score: 0, evidence: [], suggestions: [] }, self_regulation: getSec(strategy, ['self', 'regulation']) || { score: 0, evidence: [], suggestions: [] } }
  const avg = (nums: number[]) => { const arr = nums.filter(n => Number.isFinite(n)); if (!arr.length) return 0; return Math.round((arr.reduce((a, b) => a + b, 0) / arr.length) * 10) / 10 }
  const mrAvg = avg([ Number(out.moral_reasoning.stage_level.score), Number(out.moral_reasoning.foundations_balance.score), Number(out.moral_reasoning.argument_chain.score) ])
  const adAvg = avg([ Number(out.attitude_development.emotional_engagement.score), Number(out.attitude_development.resilience.score), Number(out.attitude_development.focus_flow.score) ])
  const agAvg = avg([ Number(out.ability_growth.blooms_level.score), Number(out.ability_growth.metacognition.score), Number(out.ability_growth.transfer.score) ])
  const soAvg = avg([ Number(out.strategy_optimization.diversity.score), Number(out.strategy_optimization.depth.score), Number(out.strategy_optimization.self_regulation.score) ])
  const finalScore = avg([mrAvg, adAvg, agAvg, soAvg])
  out.overall = { dimension_averages: { moral_reasoning: mrAvg, attitude: adAvg, ability: agAvg, strategy: soAvg }, final_score: finalScore, holistic_feedback: '' }
  return out
}

function normalizeFromEvaluationDimensions(evaluation: any) {
  const dims = evaluation?.dimensions || {}
  const findDim = (names: string[]) => { const keys = Object.keys(dims); for (const k of keys) { const kl = k.toLowerCase(); for (const n of names) { if (kl.includes(n.toLowerCase())) return dims[k] } } return undefined }
  const toSec = (raw: any) => {
    const score = Number(raw?.score ?? 0)
    const quotes = Array.isArray(raw?.evidence?.quotes) ? raw.evidence.quotes : []
    const reasoning = String(raw?.reasoning || '')
    const conclusion = String(raw?.conclusion || '')
    const evidence = quotes.length ? quotes.map((q: any) => ({ quote: String(q), reasoning, conclusion })) : [{ quote: '', reasoning, conclusion }]
    const sraw = (raw?.suggestions)
    const suggestions = Array.isArray(sraw) ? sraw : (sraw ? [sraw] : [])
    return { score, evidence, suggestions }
  }
  const moral = findDim(['moral reasoning'])
  const attitude = findDim(['learning attitude'])
  const ability = findDim(['learning ability'])
  const strategy = findDim(['strategy'])
  const out: any = {}
  out.moral_reasoning = { stage_level: toSec(moral?.['1A']), foundations_balance: toSec(moral?.['1B']), argument_chain: toSec(moral?.['1C']) }
  out.attitude_development = { emotional_engagement: toSec(attitude?.['2A']), resilience: toSec(attitude?.['2B']), focus_flow: toSec(attitude?.['2C']) }
  out.ability_growth = { blooms_level: toSec(ability?.['3A']), metacognition: toSec(ability?.['3B']), transfer: toSec(ability?.['3C']) }
  out.strategy_optimization = { diversity: toSec(strategy?.['4A']), depth: toSec(strategy?.['4B']), self_regulation: toSec(strategy?.['4C']) }
  const avg = (nums: number[]) => { const arr = nums.filter(n => Number.isFinite(n)); if (!arr.length) return 0; return Math.round((arr.reduce((a, b) => a + b, 0) / arr.length) * 10) / 10 }
  const mrAvg = avg([ Number(out.moral_reasoning.stage_level?.score), Number(out.moral_reasoning.foundations_balance?.score), Number(out.moral_reasoning.argument_chain?.score) ])
  const adAvg = avg([ Number(out.attitude_development.emotional_engagement?.score), Number(out.attitude_development.resilience?.score), Number(out.attitude_development.focus_flow?.score) ])
  const agAvg = avg([ Number(out.ability_growth.blooms_level?.score), Number(out.ability_growth.metacognition?.score), Number(out.ability_growth.transfer?.score) ])
  const soAvg = avg([ Number(out.strategy_optimization.diversity?.score), Number(out.strategy_optimization.depth?.score), Number(out.strategy_optimization.self_regulation?.score) ])
  const finalScore = avg([mrAvg, adAvg, agAvg, soAvg])
  out.overall = { dimension_averages: { moral_reasoning: mrAvg, attitude: adAvg, ability: agAvg, strategy: soAvg }, final_score: finalScore, holistic_feedback: '' }
  return out
}

// 导出功能
function exportAiDetailAsText() {
  if (!parsedAi.value) return
  const res: any = parsedAi.value
  const lines: string[] = []
  const pushSec = (title: string, sec: any) => {
    if (!sec) return
    for (const [k, v] of Object.entries(sec)) {
      const s = v as any
      lines.push(`${title} - ${String(k)}: ${Number(s?.score ?? 0)}/5`)
      const ev = Array.isArray(s?.evidence) ? s.evidence : []
      if (ev.length) {
        const quotes = ev.map((e: any) => String(e?.quote || '')).filter((q: string) => q.trim())
        if (quotes.length) {
          lines.push('证据:')
          quotes.forEach((q: string) => lines.push(`- ${q}`))
        }
        const reasoning = (ev.find((e: any) => e && String(e.reasoning || '').trim()) || {}).reasoning
        const conclusion = (ev.find((e: any) => e && String(e.conclusion || '').trim()) || {}).conclusion
        const explanation = (ev.find((e: any) => e && String(e.explanation || '').trim()) || {}).explanation
        if (reasoning) lines.push(`推理: ${String(reasoning)}`)
        if (conclusion) lines.push(`结论: ${String(conclusion)}`)
        if (explanation) lines.push(`解释: ${String(explanation)}`)
      }
      const sug = Array.isArray(s?.suggestions) ? s.suggestions : []
      if (sug.length) {
        lines.push('建议:')
        sug.forEach((x: any, idx: number) => lines.push(`${idx + 1}. ${String(x)}`))
      }
      lines.push('')
    }
  }
  const ov = getOverall(res) as any
  if (ov) {
    lines.push(`总体评分: ${Number(ov?.final_score ?? 0)}/5`)
    if (ov?.holistic_feedback) lines.push(`总体评价: ${String(ov.holistic_feedback)}`)
    lines.push('')
  }
  pushSec('道德推理', res?.moral_reasoning)
  pushSec('学习态度', res?.attitude_development)
  pushSec('学习能力', res?.ability_growth)
  pushSec('学习策略', res?.strategy_optimization)

  const blob = new Blob([lines.join('\n')], { type: 'text/plain;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  const base = (assignment.value as any)?.title || 'grading'
  a.download = `${String(base).replace(/\s+/g,'_')}.txt`
  a.click()
  URL.revokeObjectURL(a.href)
}

async function exportAiDetailAsPng() {
  if (!aiDetailRef.value) return
  applyExportGradientsInline(aiDetailRef.value)
  const fileBase = (String((assignment.value as any)?.title || 'grading')).replace(/\s+/g, '_')
  await exportNodeAsPng(aiDetailRef.value, fileBase)
}

async function exportAiDetailAsPdf() {
  if (!aiDetailRef.value) return
  const node = aiDetailRef.value
  const cloned = node.cloneNode(true) as HTMLElement
  const wrapper = document.createElement('div')
  wrapper.style.position = 'fixed'
  wrapper.style.left = '-99999px'
  wrapper.style.top = '0'
  wrapper.style.width = `${node.clientWidth}px`
  wrapper.style.background = '#ffffff'
  cloned.style.maxHeight = 'none'
  cloned.style.overflow = 'visible'
  applyExportGradientsInline(cloned)
  wrapper.appendChild(cloned)
  document.body.appendChild(wrapper)
  const fileBase = (String((assignment.value as any)?.title || 'grading')).replace(/\s+/g, '_')
  await exportNodeAsPdf(cloned, fileBase)
  document.body.removeChild(wrapper)
}
</script>

