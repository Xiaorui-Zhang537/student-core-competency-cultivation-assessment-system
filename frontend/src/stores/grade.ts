import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { gradeApi } from '@/api/grade.api';
import type { Grade, GradeRequest } from '@/types/grade';
import { useUIStore } from './ui';
import { notificationAPI } from '@/api/notification.api'
import { i18n } from '@/i18n'
import { handleApiCall } from '@/utils/api-handler';

export const useGradeStore = defineStore('grade', () => {
  const uiStore = useUIStore();

  // State
  const grades = ref<Grade[]>([]);
  const totalGrades = ref(0);
  const loading = computed(() => uiStore.loading);

  // Actions
  const fetchGradesByStudent = async (studentId: string, params?: { page?: number; size?: number; courseId?: string | number }) => {
    const response = await handleApiCall(
      () => gradeApi.getGradesByStudent(studentId, params),
      uiStore,
      '获取学生成绩失败'
    );
    if (response) {
      // Support multiple backend pagination shapes
      // Prefer PageResult { items, total } else fallback to Spring Page { content, totalElements }
      // @ts-expect-error runtime shape
      grades.value = (response.items as Grade[]) || (response.content as Grade[]) || [];
      // @ts-expect-error runtime shape
      totalGrades.value = (response.total as number) ?? (response.totalElements as number) ?? 0;
    }
  };

  const fetchGradesByAssignment = async (assignmentId: string, params?: { page?: number; size?: number }) => {
    const response = await handleApiCall(
      () => gradeApi.getGradesByAssignment(assignmentId, params),
      uiStore,
      '获取作业成绩失败'
    );
    if (response) {
      // @ts-expect-error runtime shape
      grades.value = (response.items as Grade[]) || (response.content as Grade[]) || [];
      // @ts-expect-error runtime shape
      totalGrades.value = (response.total as number) ?? (response.totalElements as number) ?? 0;
    }
  };

  const gradeSubmission = async (data: GradeRequest) => {
    const response = await handleApiCall(
      () => gradeApi.gradeSubmission(data),
      uiStore,
      '评分失败'
    );
    if (response) {
      uiStore.showNotification({ type: 'success', title: i18n.global.t('teacher.grading.notify.gradeSuccess') as string });
      // 自动通知学生：作业已批改
      // 通知交由后端在发布阶段自动触发；此处不再重复发送
      // Optionally refresh the list or update the specific grade
      return response;
    }
    return null;
  };

  const publishGrade = async (gradeId: string) => {
    const response = await handleApiCall(
      () => gradeApi.publishGrade(gradeId),
      uiStore,
      '发布成绩失败'
    );
    if (response) {
      uiStore.showNotification({ type: 'success', title: i18n.global.t('teacher.grading.notify.publishSuccess') as string });
      const grade = grades.value.find(g => g.id === gradeId);
      if (grade) {
        grade.isPublished = true;
      }
    }
  };

  return {
    grades,
    totalGrades,
    loading,
    fetchGradesByStudent,
    fetchGradesByAssignment,
    gradeSubmission,
    publishGrade,
  };
});
