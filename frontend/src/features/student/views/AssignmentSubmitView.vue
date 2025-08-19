<template>
  <div class="p-6">
    <div v-if="assignment" class="max-w-4xl mx-auto">
      <!-- Header -->
    <div class="mb-8">
        <h1 class="text-3xl font-bold">{{ assignment.title }}</h1>
        <p class="text-gray-500 mt-1">{{ assignment.description }}</p>
        <div class="mt-4 text-sm text-gray-600">
          <strong>截止日期:</strong> {{ new Date(assignment.dueDate).toLocaleString() }}
          <span class="ml-4 badge" :class="statusClass(submission?.status || 'NOT_SUBMITTED')">
            状态: {{ submission ? submission.status : '未提交' }}
          </span>
        </div>
      </div>
      
      <!-- Submission Form -->
      <div class="space-y-6">
        <div class="card p-6">
          <h2 class="text-xl font-semibold mb-4">提交内容</h2>
          <textarea v-model="form.content" rows="10" class="input w-full" placeholder="在此输入您的作业内容..."></textarea>
        </div>
        
        <div class="card p-6">
          <h2 class="text-xl font-semibold mb-4">上传附件</h2>
          <input type="file" @change="handleFileUpload" :disabled="isUploading" class="input" />
          <div v-if="isUploading" class="mt-2 text-sm text-gray-500">正在上传...</div>
          
          <div v-if="uploadedFiles.length > 0" class="mt-4 space-y-2">
            <h3 class="text-sm font-medium">已上传文件:</h3>
            <div v-for="file in uploadedFiles" :key="file.id" class="flex justify-between items-center p-2 bg-gray-100 rounded">
              <span>{{ file.fileName }}</span>
              <button @click="removeFile(file.id)" class="btn btn-sm btn-danger-outline">删除</button>
            </div>
          </div>
        </div>
        
        <!-- Actions -->
        <div class="flex justify-end space-x-4">
          <button @click="handleSaveDraft" :disabled="submissionStore.loading" class="btn btn-outline">保存草稿</button>
          <button @click="handleSubmit" :disabled="submissionStore.loading" class="btn btn-primary">提交作业</button>
        </div>
      </div>

    </div>
    <div v-else class="text-center py-12">
      <p>正在加载作业详情...</p>
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

const route = useRoute();
const assignmentStore = useAssignmentStore();
const submissionStore = useSubmissionStore();
const uiStore = useUIStore();

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

const handleFileUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files[0]) {
    isUploading.value = true;
    try {
      const response = await fileApi.uploadFile(target.files[0]);
      if (response && (response as any).data) {
        const fileInfo = (response as any).data as FileInfo
        uploadedFiles.value.push(fileInfo);
        form.fileIds.push(fileInfo.id);
      }
    } catch (error) {
      uiStore.showNotification({ type: 'error', title: '上传失败', message: '文件上传失败，请重试。'});
    } finally {
      isUploading.value = false;
    }
  }
};

const removeFile = async (fileId: string) => {
  try {
    await fileApi.deleteFile(fileId);
    uploadedFiles.value = uploadedFiles.value.filter(f => f.id !== fileId);
    form.fileIds = form.fileIds.filter(id => id !== fileId);
  } catch (error) {
     uiStore.showNotification({ type: 'error', title: '删除失败', message: '文件删除失败，请重试。'});
  }
};

const handleSaveDraft = () => {
  const assignmentId = route.params.id as string;
  submissionStore.saveDraft(assignmentId, form);
};

const handleSubmit = () => {
  if (!form.content) {
    uiStore.showNotification({ type: 'error', title: '内容为空', message: '请输入作业内容后再提交。'});
    return;
  }
  const assignmentId = route.params.id as string;
  submissionStore.submitAssignment(assignmentId, form);
};

onMounted(async () => {
  const assignmentId = route.params.id as string;
  await assignmentStore.fetchAssignmentById(assignmentId);
  await submissionStore.fetchSubmissionForAssignment(assignmentId);

  if (submission.value) {
    form.content = submission.value.content;
    form.fileIds = submission.value.fileIds;
    // If files exist, fetch their info to display
    if(form.fileIds.length > 0) {
        const fileInfos = await Promise.all(form.fileIds.map(id => fileApi.getFileInfo(id)));
        uploadedFiles.value = fileInfos.map(res => (res as any).data as FileInfo).filter(Boolean);
    }
  }
});
</script>
