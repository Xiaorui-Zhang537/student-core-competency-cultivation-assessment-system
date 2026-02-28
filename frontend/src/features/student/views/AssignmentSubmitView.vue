<template>
  <div class="p-6">
    <div v-if="assignment" class="max-w-4xl mx-auto">
      <breadcrumb
        class="mb-2"
        :items="breadcrumbItems"
      />
      <page-header :title="assignment.title" />
      <!-- 顶部：信息卡 + 附件卡 并排 -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-2 mb-6 md:mb-8">
        <assignment-info-card :assignment="assignment" :effectiveDue="effectiveDue" :status="displayStatus" />
        <card tint="accent"><attachment-list :files="teacherAttachments" :title="i18nText('student.assignments.detail.attachmentsTitle', '附件')" :noCard="true" :hideHeader="false" /></card>
      </div>
      
      <!-- 统一垂直间距栈 -->
      <div class="mt-6 md:mt-8 space-y-8">
        <!-- Readonly Banner (Glass + Tint) -->
        <div v-if="readOnly" class="p-3 rounded-xl glass-ultraThin glass-tint-warning text-sm mt-4" v-glass="{ strength: 'ultraThin', interactive: false }">
          {{ pastDue ? (t('student.assignments.submit.readOnlyBannerDue') as string) : (t('student.assignments.submit.readOnlyBannerSubmitted') as string) }}
        </div>

        <!-- 中部：提交内容 + 上传附件 并排 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <card tint="secondary">
            <template #header>
              <h2 class="text-xl font-semibold">{{ t('student.assignments.submit.contentTitle') }}</h2>
            </template>
            <glass-textarea v-model="form.content" :rows="10" class="w-full" :disabled="readOnly" :placeholder="t('student.assignments.submit.contentPlaceholder') as string" />
          </card>
          <card tint="info">
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
              <div v-for="file in uploadedFiles" :key="file.id" class="flex justify-between items-center p-2 rounded glass-ultraThin glass-tint-secondary" v-glass="{ strength: 'ultraThin', interactive: false }">
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
      <div v-if="!readOnly" class="flex justify-end space-x-4 pointer-events-auto relative z-10">
          <Button variant="outline" @click="handleSaveDraft" :disabled="disableActions || pastDue || !(form.content && form.content.trim())">
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
        <div v-if="displayStatus==='SUBMITTED' && !grade" class="p-3 rounded-xl glass-ultraThin glass-tint-info" v-glass="{ strength: 'ultraThin', interactive: false }">
          <span class="text-sm text-gray-700 dark:text-gray-300">{{ i18nText('student.assignments.detail.ungradedHint', '作业已提交，等待老师评分。') }}</span>
        </div>

        <card v-if="displayStatus==='GRADED'" tint="secondary">
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
        <card v-if="displayStatus==='GRADED' && latestReport" tint="info">
          <div class="flex items-center mb-4">
            <h2 class="text-xl font-semibold flex-1">{{ i18nText('student.ability.latestReport', 'AI 能力报告') }}</h2>
            <Button size="sm" variant="indigo" @click="openAiDetail" :disabled="!parsedDetailAi"><template #icon><svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="12" cy="12" r="3"></circle></svg></template>{{ i18nText('teacher.aiGrading.viewDetail', '查看详情') }}</Button>
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
        <glass-modal v-if="aiDetailOpen" :title="i18nText('student.ability.latestReport', 'AI 能力报告（最近一次）')" size="xl" :hideScrollbar="true" heightVariant="max" @close="aiDetailOpen=false">
          <div ref="aiDetailRef" data-export-root="1" class="space-y-4">
            <AbilityReportDetailContent :report="latestReport" />
          </div>
          <template #footer>
            <Button size="sm" variant="primary" @click="exportAiDetailAsText" :disabled="!parsedDetailAi"><template #icon><svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21H5a2 2 0 0 1-2-2V7"></path><path d="M9 7V3h6v4"></path><path d="M9 15h6"></path></svg></template>{{ i18nText('teacher.aiGrading.exportText', '导出文本') }}</Button>
            <Button size="sm" variant="success" @click="exportAiDetailAsPng" :disabled="!parsedDetailAi"><template #icon><svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="14" rx="2" ry="2"></rect><path d="M8 21h8"></path><path d="M12 17v4"></path></svg></template>{{ i18nText('teacher.aiGrading.exportPng', '导出 PNG') }}</Button>
            <Button size="sm" variant="purple" @click="exportAiDetailAsPdf" :disabled="!parsedDetailAi"><template #icon><svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2h9l5 5v13a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z"></path><path d="M14 2v6h6"></path></svg></template>{{ i18nText('teacher.aiGrading.exportPdf', '导出 PDF') }}</Button>
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
import { lessonApi } from '@/api/lesson.api'
import type { FileInfo } from '@/types/file';
import { useUIStore } from '@/stores/ui';
import FileUpload from '@/components/forms/FileUpload.vue'
// @ts-ignore
import { useI18n } from 'vue-i18n'
import PageHeader from '@/components/ui/PageHeader.vue'
import { baseURL } from '@/api/config'
import { ArrowDownTrayIcon } from '@heroicons/vue/24/outline'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
import Button from '@/components/ui/Button.vue'
import { abilityApi } from '@/api/ability.api'
import GlassModal from '@/components/ui/GlassModal.vue'
import { exportNodeAsPng, exportNodeAsPdf, applyExportGradientsInline } from '@/shared/utils/exporters'
import AbilityReportDetailContent from '@/shared/views/AbilityReportDetailContent.vue'
import { parseAbilityReport } from '@/shared/utils/abilityReportData'
import { buildAbilityReportText } from '@/shared/utils/abilityReportExport'
import Card from '@/components/ui/Card.vue'
import AssignmentInfoCard from '@/features/shared/AssignmentInfoCard.vue'
import AttachmentList from '@/features/shared/AttachmentList.vue'
import Breadcrumb from '@/components/ui/Breadcrumb.vue'

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
const parsedDetailAi = computed(() => parseAbilityReport(latestReport.value))
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

// 课程作业（course_bound）默认无截止时间
const isCourseBoundAssignment = computed(() => {
  const at = String((assignment.value as any)?.assignmentType || '').toLowerCase()
  return at === 'course_bound'
})
const lessonBreadcrumbTitle = ref('')
const courseBoundLessonId = computed(() => {
  const raw = (assignment.value as any)?.lessonId
    ?? (assignment.value as any)?.lesson_id
    ?? route.query.lessonId
  return raw == null ? '' : String(raw)
})

const breadcrumbItems = computed(() => {
  const currentTitle = String((assignment.value as any)?.title || '')
  if (isCourseBoundAssignment.value) {
    const cid = String((assignment.value as any)?.courseId || '')
    const ctitle = String((assignment.value as any)?.courseTitle || (assignment.value as any)?.courseName || (assignment.value as any)?.course?.title || i18nText('student.courses.detailTitle', '课程详情'))
    const lid = courseBoundLessonId.value
    const ltitle = String(
      lessonBreadcrumbTitle.value
      || (assignment.value as any)?.lessonTitle
      || (assignment.value as any)?.lesson?.title
      || i18nText('student.courses.detail.sectionIntro', '节次详情')
    )
    return [
      { label: i18nText('student.courses.title', 'My Courses'), to: '/student/courses' },
      ...(cid ? [{ label: ctitle, to: `/student/courses/${cid}` }] : []),
      ...(lid ? [{ label: ltitle, to: `/student/lessons/${lid}` }] : []),
      { label: currentTitle }
    ]
  }
  return [
    { label: i18nText('student.assignments.title', 'Assignments'), to: '/student/assignments' },
    { label: currentTitle }
  ]
})

// 有“打回重做”则以个性化重交截止覆盖 dueDate
const effectiveDue = computed(() => {
  const g: any = grade.value
  if (g && String(g.status || '').toLowerCase() === 'returned' && g.resubmitUntil) return g.resubmitUntil
  if (isCourseBoundAssignment.value) return null
  return (assignment.value as any)?.dueDate
})
const dueLabel = computed(() => {
  const g: any = grade.value
  if (g && String(g.status || '').toLowerCase() === 'returned' && g.resubmitUntil) return (t('student.assignments.resubmitUntil') as any) || '重交截止'
  return t('student.assignments.due') as any
})
const pastDue = computed(() => {
  // 课程作业默认无截止，不应触发“已过期”
  if (isCourseBoundAssignment.value) {
    const g: any = grade.value
    // 仅当被打回且明确设置了重交截止时，才按截止控制
    if (!(g && String(g.status || '').toLowerCase() === 'returned' && g.resubmitUntil)) {
      return false
    }
  }
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

// 进入只读：仅过期时不可编辑，其余时间允许反复提交覆盖
const readOnly = computed(() => pastDue.value)

// 禁用条件：加载或过期
const disableActions = computed(() => submissionStore.loading || pastDue.value)

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
  if (!form.content || !form.content.trim()) {
    uiStore.showNotification({ type: 'error', title: t('app.notifications.error.title') as string, message: (t('student.assignments.submit.errors.contentEmptyMsg') as string) || '请输入内容后再保存草稿' })
    return
  }
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
  await loadLessonBreadcrumbTitle()
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
      const sid = String((submission.value as any)?.id || '')
      if (sid) {
        const rel: any = await fileApi.getRelatedFiles('submission', sid)
        const list: any[] = (rel?.data || rel || []) as any[]
        if (Array.isArray(list) && list.length) {
          uploadedFiles.value = list
          form.fileIds = list.map((f: any) => String(f?.id || ''))
        }
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

async function loadLessonBreadcrumbTitle() {
  if (!isCourseBoundAssignment.value) return
  const lid = courseBoundLessonId.value
  if (!lid) return
  try {
    const res: any = await lessonApi.getLesson(lid)
    const row = (res?.data || res || {}) as any
    lessonBreadcrumbTitle.value = String(row?.title || '').trim()
  } catch {
    lessonBreadcrumbTitle.value = ''
  }
}

// 打开完整AI报告弹窗
function openAiDetail() {
  aiDetailOpen.value = true
}

// 导出功能
function exportAiDetailAsText() {
  if (!parsedDetailAi.value) return
  const text = buildAbilityReportText(parsedDetailAi.value)
  const blob = new Blob([text], { type: 'text/plain;charset=utf-8' })
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

