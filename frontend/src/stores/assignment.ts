import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { assignmentApi } from '@/api/assignment.api';
import type { Assignment, AssignmentCreationRequest, AssignmentUpdateRequest } from '@/types/assignment';
import { useUIStore } from './ui';
import { i18n } from '@/i18n'
import { handleApiCall } from '@/utils/api-handler';

export const useAssignmentStore = defineStore('assignment', () => {
  const uiStore = useUIStore();

  // State
  const assignments = ref<Assignment[]>([]);
  const selectedAssignment = ref<Assignment | null>(null);
  const totalAssignments = ref(0);
  const loading = computed(() => uiStore.loading);

  // Actions
  const fetchAssignments = async (params: { courseId?: string; page?: number; size?: number; sort?: string } = {}) => {
    const apiCall = params.courseId 
      ? () => assignmentApi.getAssignmentsByCourse(params.courseId!, params)
      : () => assignmentApi.getAssignments(params);

    const response = await handleApiCall(
      apiCall,
      uiStore,
      '获取作业列表失败'
    );
    if (response) {
      assignments.value = response.items;
      totalAssignments.value = response.total;
    }
  };
  
  const fetchAssignmentById = async (id: string) => {
    const response = await handleApiCall(
      () => assignmentApi.getAssignmentById(id),
      uiStore,
      '获取作业详情失败'
    );
    if (response) {
      selectedAssignment.value = response;
    }
  };

  const createAssignment = async (data: AssignmentCreationRequest, opts?: { suppressNotify?: boolean }) => {
    const response = await handleApiCall(
      () => assignmentApi.createAssignment(data),
      uiStore,
      '创建作业失败'
    );
    if (response) {
      if (!opts?.suppressNotify) {
        uiStore.showNotification({ type: 'success', title: i18n.global.t('teacher.assignments.notify.createdTitle') as string, message: i18n.global.t('teacher.assignments.notify.createdMsg') as string });
      }
      await fetchAssignments({ courseId: data.courseId });
      return response;
    }
    return null;
  };
  
  const updateAssignment = async (id: string, data: AssignmentUpdateRequest, opts?: { suppressNotify?: boolean }) => {
    const response = await handleApiCall(
      () => assignmentApi.updateAssignment(id, data),
      uiStore,
      '更新作业失败'
    );
    if (response) {
      if (!opts?.suppressNotify) {
        uiStore.showNotification({ type: 'success', title: i18n.global.t('teacher.assignments.notify.updatedTitle') as string, message: i18n.global.t('teacher.assignments.notify.updatedMsg') as string });
      }
      if (selectedAssignment.value && selectedAssignment.value.id === id) {
        selectedAssignment.value = { ...selectedAssignment.value, ...response };
      }
      const index = assignments.value.findIndex(a => a.id === id);
      if (index !== -1) {
        assignments.value[index] = { ...assignments.value[index], ...response };
      }
      return response;
    }
    return null;
  };

  const deleteAssignment = async (id: string, courseId: string) => {
    const response = await handleApiCall(
      () => assignmentApi.deleteAssignment(id),
      uiStore,
      '删除作业失败'
    );
    if (response) {
      uiStore.showNotification({ type: 'success', title: i18n.global.t('teacher.assignments.notify.deletedTitle') as string, message: i18n.global.t('teacher.assignments.notify.deletedMsg') as string });
      await fetchAssignments({ courseId });
    }
  };

  const publishAssignment = async (id: string) => {
      const response = await handleApiCall(() => assignmentApi.publishAssignment(id), uiStore, '发布作业失败');
      if (response) {
          uiStore.showNotification({ type: 'success', title: i18n.global.t('teacher.assignments.notify.publishedTitle') as string, message: i18n.global.t('teacher.assignments.notify.publishedMsg') as string });
          const assignment = assignments.value.find(a => a.id === id);
          if (assignment) assignment.status = 'PUBLISHED';
      }
  };

  return {
    assignments,
    selectedAssignment,
    totalAssignments,
    loading,
    fetchAssignments,
    fetchAssignmentById,
    createAssignment,
    updateAssignment,
    deleteAssignment,
    publishAssignment,
  };
});
