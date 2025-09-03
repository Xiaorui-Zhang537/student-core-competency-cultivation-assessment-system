<template>
  <div class="p-6">
    <div v-if="assignment" class="max-w-4xl mx-auto">
      <PageHeader :title="assignment.title" :subtitle="assignment.description" />
        <div class="mt-2 text-sm text-gray-600 dark:text-gray-300">
          <strong>{{ t('student.assignments.due') }}</strong> {{ new Date(assignment.dueDate).toLocaleString() }}
          <span class="ml-4 badge" :class="statusClass(displayStatus)">
            {{ t('student.assignments.submit.status') }} {{ statusText(displayStatus) }}
          </span>
        </div>
      
      <!-- Readonly Banner -->
      <div v-if="readOnly" class="mb-4 p-3 rounded bg-yellow-50 dark:bg-yellow-900/20 text-yellow-800 dark:text-yellow-200 text-sm">
        {{ pastDue ? (t('student.assignments.submit.readOnlyBannerDue') as string) : (t('student.assignments.submit.readOnlyBannerSubmitted') as string) }}
      </div>

      <!-- Submission Form -->
      <div class="space-y-6">
        <!-- Grade Block (only when graded) -->
        <div v-if="displayStatus==='GRADED'" class="card p-6 glass-regular glass-interactive" v-glass="{ strength: 'regular', interactive: true }">
          <h2 class="text-xl font-semibold mb-4">{{ t('student.grades.title') }}</h2>
          <div class="text-sm text-gray-700 dark:text-gray-300">
            <div class="mb-1">
              <span class="font-medium">{{ t('student.grades.gradedAt') }}</span>: {{ grade?.gradedAt ? new Date(grade.gradedAt).toLocaleString() : '-' }}
            </div>
            <div class="mb-1">
              <span class="font-medium">{{ t('student.grades.score') }}</span>
              <span class="ml-2">{{ grade?.score ?? '-' }}</span>
            </div>
            <div class="mt-2">
              <span class="font-medium">{{ t('student.grades.feedback') }}</span>：{{ grade?.feedback || '-' }}
            </div>
          </div>
        </div>

        <div class="card p-6 glass-regular glass-interactive" v-glass="{ strength: 'regular', interactive: true }">
          <h2 class="text-xl font-semibold mb-4">{{ t('student.assignments.submit.contentTitle') }}</h2>
          <textarea v-model="form.content" rows="10" class="input w-full" :disabled="readOnly" :placeholder="t('student.assignments.submit.contentPlaceholder') as string"></textarea>
        </div>
        
        <div class="card p-6 glass-regular glass-interactive" v-glass="{ strength: 'regular', interactive: true }">
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
import { useRoute } from 'vue-router';
import { useAssignmentStore } from '@/stores/assignment';
import { useSubmissionStore } from '@/stores/submission';
import { useAuthStore } from '@/stores/auth';
import { fileApi } from '@/api/file.api';
import { gradeApi } from '@/api/grade.api';
import type { FileInfo } from '@/types/file';
import { useUIStore } from '@/stores/ui';
import FileUpload from '@/components/forms/FileUpload.vue'
// @ts-ignore
import { useI18n } from 'vue-i18n'
import PageHeader from '@/components/ui/PageHeader.vue'

const route = useRoute();
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
  await assignmentStore.fetchAssignmentById(assignmentId);
  await submissionStore.fetchSubmissionForAssignment(assignmentId);

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
        const res: any = await gradeApi.getGradeForStudentAssignment(String(sid), assignmentId)
        grade.value = (res && res.data !== undefined) ? res.data : res
      }
    }
  } catch {}
});
</script>
