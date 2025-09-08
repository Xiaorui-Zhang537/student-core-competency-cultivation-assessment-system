<template>
  <div class="p-6">
    <div v-if="assignment" class="max-w-4xl mx-auto">
      <nav class="relative z-30 flex items-center space-x-2 text-sm !text-gray-900 dark:!text-gray-100 mb-2">
        <span class="hover:!text-gray-900 dark:hover:!text-gray-100 cursor-pointer font-medium" @click="router.push({ name: 'StudentAssignments' })">
          {{ i18nText('student.assignments.title', 'Assignments') }}
        </span>
        <ChevronRightIcon class="w-4 h-4 opacity-70 !text-gray-900 dark:!text-gray-100" />
        <span class="truncate flex-1 font-medium !text-gray-900 dark:!text-gray-100">{{ assignment.title }}</span>
      </nav>
      <PageHeader :title="assignment.title" :subtitle="assignment.description" />
        <div class="mt-2 text-sm text-gray-600 dark:text-gray-300">
          <strong>{{ t('student.assignments.due') }}</strong> {{ new Date(assignment.dueDate).toLocaleString() }}
          <span class="ml-4 badge" :class="statusClass(displayStatus)">
            {{ t('student.assignments.submit.status') }} {{ statusText(displayStatus) }}
          </span>
        </div>
      
      <!-- 统一垂直间距栈 -->
      <div class="space-y-6">
        <!-- Readonly Banner -->
        <div v-if="readOnly" class="p-3 rounded bg-yellow-50 dark:bg-yellow-900/20 text-yellow-800 dark:text-yellow-200 text-sm">
          {{ pastDue ? (t('student.assignments.submit.readOnlyBannerDue') as string) : (t('student.assignments.submit.readOnlyBannerSubmitted') as string) }}
        </div>

        <!-- Teacher Attachments -->
        <div class="card p-6 glass-ultraThin" v-glass="{ strength: 'ultraThin', interactive: true }">
          <h2 class="text-lg font-semibold mb-3">{{ i18nText('student.assignments.detail.attachmentsTitle', '附件') }}</h2>
          <div v-if="teacherAttachments.length === 0" class="text-sm text-gray-500">{{ i18nText('student.assignments.detail.noAttachments', '暂无附件') }}</div>
          <ul v-else class="divide-y divide-gray-200/60 dark:divide-gray-700/60">
            <li v-for="f in teacherAttachments" :key="String((f as any).id)" class="py-2 flex items-center justify-between">
              <div class="min-w-0 mr-3">
                <div class="text-sm truncate">{{ (f as any).originalName || (f as any).fileName || ('附件#' + (f as any).id) }}</div>
                <div class="text-xs text-gray-500">{{ formatSize((f as any).fileSize) }}</div>
              </div>
              <a class="btn btn-sm btn-outline" :href="`${baseURL}/files/${(f as any).id}/download`">{{ i18nText('student.assignments.detail.download', '下载') }}</a>
            </li>
          </ul>
        </div>

        <!-- Ungraded Hint -->
        <div v-if="displayStatus==='SUBMITTED' && !grade" class="p-3 rounded-xl glass-ultraThin" v-glass="{ strength: 'ultraThin', interactive: false }">
          <span class="text-sm text-gray-700 dark:text-gray-300">{{ i18nText('student.assignments.detail.ungradedHint', '作业已提交，等待老师评分。') }}</span>
        </div>

        <!-- Grade Block (only when graded) -->
        <div v-if="displayStatus==='GRADED'" class="card p-6 glass-ultraThin" v-glass="{ strength: 'ultraThin', interactive: true }">
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
              <span class="ml-auto text-gray-500 dark:text-gray-400">{{ i18nText('student.grades.gradedAt', '评分时间') }}：{{ grade?.gradedAt ? new Date(grade.gradedAt).toLocaleString() : '-' }}</span>
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
        </div>

        <div class="card p-6 glass-ultraThin" v-glass="{ strength: 'ultraThin', interactive: true }">
          <h2 class="text-xl font-semibold mb-4">{{ t('student.assignments.submit.contentTitle') }}</h2>
          <GlassTextarea v-model="form.content" :rows="10" class="w-full" :disabled="readOnly" :placeholder="t('student.assignments.submit.contentPlaceholder') as string" />
        </div>
        
        <div class="card p-6 glass-ultraThin" v-glass="{ strength: 'ultraThin', interactive: true }">
          <h2 class="text-xl font-semibold mb-4">{{ t('student.assignments.submit.uploadTitle') }}</h2>
          <FileUpload v-if="!readOnly"
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
              <button @click="removeFile(file.id)" class="btn btn-sm btn-danger-outline">{{ t('student.assignments.submit.delete') }}</button>
            </div>
          </div>
        </div>
        
        <!-- Actions -->
        <div v-if="!readOnly" class="flex justify-end space-x-4">
          <button @click="handleSaveDraft" :disabled="disableActions || pastDue" class="btn btn-outline">{{ t('student.assignments.submit.saveDraft') }}</button>
          <button @click="handleSubmit" :disabled="disableActions || pastDue || !form.content" class="btn btn-primary">{{ t('student.assignments.submit.submit') }}</button>
        </div>
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
import { ChevronRightIcon } from '@heroicons/vue/24/outline'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'

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

const assignment = computed(() => assignmentStore.selectedAssignment);
const submission = computed(() => {
    const assignmentId = route.params.id as string;
    return submissionStore.submissions.get(assignmentId);
});

const pastDue = computed(() => {
  const ts = (assignment.value as any)?.dueDate
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

// 进入只读：过期或已提交或已评分
const readOnly = computed(() => {
  const s = ((submission.value as any)?.status || '').toUpperCase()
  if (pastDue.value) return true
  if (s === 'SUBMITTED' || s === 'GRADED') return true
  return false
})

// 禁用条件：正在加载 或 已提交/已评分
const disableActions = computed(() => {
  const s = (submission.value as any)?.status || ''
  const upper = String(s || '').toUpperCase()
  if (submissionStore.loading) return true
  if (upper === 'SUBMITTED' || upper === 'GRADED') return true
  return false
})

// 归一化状态到前端显示语义
const normalizeStatus = (raw?: string) => {
  const s = (raw || '').toUpperCase()
  if (!s || s === 'NOT_SUBMITTED') return 'PENDING'
  if (s === 'PUBLISHED') return 'PENDING'
  if (s === 'SUBMITTED') return 'SUBMITTED'
  if (s === 'GRADED') return 'GRADED'
  if (s === 'LATE') return 'LATE'
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
    default: return 'bg-gray-100 text-gray-800';
  }
};

// 本地化状态文案
const statusText = (status: string | undefined) => {
  const s = (status || 'UNKNOWN').toUpperCase()
  if (s === 'PENDING') return t('student.assignments.status.pending') as string
  if (s === 'SUBMITTED') return t('student.assignments.status.submitted') as string
  if (s === 'GRADED') return t('student.assignments.status.graded') as string
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
      const fileInfos = await Promise.all(form.fileIds.map(id => fileApi.getFileInfo(id)));
      // api 拦截器会解包返回 data
      uploadedFiles.value = (fileInfos as any[]).filter(Boolean) as any;
    }
  }
  // 若已评分，按需拉取成绩
  try {
    if (displayStatus.value === 'GRADED') {
      const sid = authStore.user?.id
      if (sid) {
        const res: any = await gradeApi.getGradeForStudentAssignment(String(sid), resolvedId)
        grade.value = (res && res.data !== undefined) ? res.data : res
        // animate score
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
});
</script>
