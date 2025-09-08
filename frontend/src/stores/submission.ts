import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { submissionApi } from '@/api/submission.api';
import type { Submission, SubmissionRequest, DraftRequest } from '@/types/submission';
import { useUIStore } from './ui';
import { handleApiCall } from '@/utils/api-handler';

export const useSubmissionStore = defineStore('submission', () => {
  const uiStore = useUIStore();

  // State
  const submissions = ref<Map<string, Submission>>(new Map()); // Key is assignmentId
  const loading = computed(() => uiStore.loading);

  // Actions
  const fetchSubmissionForAssignment = async (assignmentId: string) => {
    const response = await handleApiCall(
      () => submissionApi.getSubmissionForAssignment(assignmentId),
      uiStore,
      '获取提交记录失败'
    );
    if (response) {
      submissions.value.set(assignmentId, response);
      // 触发依赖组件的响应式更新（Map 原地修改不触发）
      submissions.value = new Map(submissions.value);
    }
  };

  const submitAssignment = async (assignmentId: string, data: SubmissionRequest) => {
    const response = await handleApiCall(
      () => submissionApi.submitAssignment(assignmentId, data),
      uiStore,
      '提交作业失败',
      { successMessage: (window as any).$i18n?.t?.('student.assignments.submit.success') || '作业已成功提交' }
    );
    if (response) {
      submissions.value.set(assignmentId, response);
      // 触发依赖组件的响应式更新
      submissions.value = new Map(submissions.value);
      return response;
    }
    return null;
  };

  const saveDraft = async (assignmentId: string, data: DraftRequest) => {
    await handleApiCall(
      () => submissionApi.saveDraft(assignmentId, data),
      uiStore,
      '保存草稿失败',
      { successMessage: '草稿已保存' }
    );
  };
  
  const getSubmissionByAssignmentId = (assignmentId: string) => {
      return computed(() => submissions.value.get(assignmentId));
  }


  return {
    submissions,
    loading,
    fetchSubmissionForAssignment,
    submitAssignment,
    saveDraft,
    getSubmissionByAssignmentId,
  };
});
