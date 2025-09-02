<template>
  <div class="p-6">
    <div v-if="assignment" class="max-w-4xl mx-auto">
      <!-- Header -->
    <div class="mb-8">
        <h1 class="text-3xl font-bold">{{ assignment.title }}</h1>
        <p class="text-gray-500 mt-1">{{ assignment.description }}</p>
        <div class="mt-4 text-sm text-gray-600">
          <strong>{{ t('student.assignments.due') }}</strong> {{ new Date(assignment.dueDate).toLocaleString() }}
          <span class="ml-4 badge" :class="statusClass(submission?.status || 'NOT_SUBMITTED')">
            {{ t('student.assignments.submit.status') }} {{ submission ? submission.status : 'NOT_SUBMITTED' }}
          </span>
        </div>
      </div>
      
      <!-- Submission Form -->
      <div class="space-y-6">
        <div class="card p-6 glass-regular glass-interactive" v-glass="{ strength: 'regular', interactive: true }">
          <h2 class="text-xl font-semibold mb-4">{{ t('student.assignments.submit.contentTitle') }}</h2>
          <textarea v-model="form.content" rows="10" class="input w-full" :placeholder="t('student.assignments.submit.contentPlaceholder') as string"></textarea>
        </div>
        
        <div class="card p-6 glass-regular glass-interactive" v-glass="{ strength: 'regular', interactive: true }">
          <h2 class="text-xl font-semibold mb-4">{{ t('student.assignments.submit.uploadTitle') }}</h2>
          <FileUpload
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
        <div class="flex justify-end space-x-4">
          <button @click="handleSaveDraft" :disabled="submissionStore.loading" class="btn btn-outline">{{ t('student.assignments.submit.saveDraft') }}</button>
          <button @click="handleSubmit" :disabled="submissionStore.loading" class="btn btn-primary">{{ t('student.assignments.submit.submit') }}</button>
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
import { fileApi } from '@/api/file.api';
import type { FileInfo } from '@/types/file';
import { useUIStore } from '@/stores/ui';
import FileUpload from '@/components/forms/FileUpload.vue'
// @ts-ignore
import { useI18n } from 'vue-i18n'

const route = useRoute();
const assignmentStore = useAssignmentStore();
const submissionStore = useSubmissionStore();
const uiStore = useUIStore();
const { t } = useI18n();

const form = reactive({
  content: '',
  fileIds: [] as string[],
});

const uploadedFiles = ref<FileInfo[]>([]);
const isUploading = ref(false);

const assignment = computed(() => assignmentStore.selectedAssignment);
const submission = computed(() => {
    const assignmentId = route.params.id as string;
    return submissionStore.submissions.get(assignmentId);
});

const statusClass = (status: string | undefined) => {
  switch (status) {
    case 'SUBMITTED': return 'bg-green-100 text-green-800';
    case 'LATE': return 'bg-red-100 text-red-800';
    case 'GRADED': return 'bg-blue-100 text-blue-800';
    default: return 'bg-gray-100 text-gray-800';
  }
};

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
});
</script>
