import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { assignmentApi } from '@/api/assignment.api';
import type { Assignment, AssignmentCreationRequest, AssignmentUpdateRequest } from '@/types/assignment';
import { useUIStore } from './ui';
import { handleApiCall } from '@/utils/api-handler';

export const useAssignmentStore = defineStore('assignment', () => {
  const uiStore = useUIStore();

  // State
  const assignments = ref<Assignment[]>([]);
  const selectedAssignment = ref<Assignment | null>(null);
  const totalAssignments = ref(0);
  const loading = computed(() => uiStore.loading);

  // Actions
  const fetchAssignments = async (params: { courseId?: number; page?: number; size?: number; sort?: string } = {}) => {
    const apiCall = params.courseId 
      ? () => assignmentApi.getAssignmentsByCourse(params.courseId!, params)
      : () => assignmentApi.getAssignments(params);

    const response = await handleApiCall(
      apiCall,
      uiStore,
      '获取作业列表失败'
    );
    if (response) {
      assignments.value = response.data.content;
      totalAssignments.value = response.data.totalElements;
    }
  };
  
  const fetchAssignmentById = async (id: number) => {
    const response = await handleApiCall(
      () => assignmentApi.getAssignmentById(id),
      uiStore,
      '获取作业详情失败'
    );
    if (response) {
      selectedAssignment.value = response.data;
    }
  };

  const createAssignment = async (data: AssignmentCreationRequest) => {
    const response = await handleApiCall(
      () => assignmentApi.createAssignment(data),
      uiStore,
      '创建作业失败'
    );
    if (response) {
      uiStore.showNotification({ type: 'success', title: '作业已创建' });
      await fetchAssignments({ courseId: data.courseId });
      return response.data;
    }
    return null;
  };
  
  const updateAssignment = async (id: number, data: AssignmentUpdateRequest) => {
    const response = await handleApiCall(
      () => assignmentApi.updateAssignment(id, data),
      uiStore,
      '更新作业失败'
    );
    if (response) {
      uiStore.showNotification({ type: 'success', title: '作业已更新' });
      if (selectedAssignment.value && selectedAssignment.value.id === id) {
        selectedAssignment.value = { ...selectedAssignment.value, ...response.data };
      }
      const index = assignments.value.findIndex(a => a.id === id);
      if (index !== -1) {
        assignments.value[index] = { ...assignments.value[index], ...response.data };
      }
      return response.data;
    }
    return null;
  };

  const deleteAssignment = async (id: number, courseId: number) => {
    const response = await handleApiCall(
      () => assignmentApi.deleteAssignment(id),
      uiStore,
      '删除作业失败'
    );
    if (response) {
      uiStore.showNotification({ type: 'success', title: '作业已删除' });
      await fetchAssignments({ courseId });
    }
  };

  const publishAssignment = async (id: number) => {
      const response = await handleApiCall(() => assignmentApi.publishAssignment(id), uiStore, '发布作业失败');
      if (response) {
          uiStore.showNotification({ type: 'success', title: '作业已发布' });
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
