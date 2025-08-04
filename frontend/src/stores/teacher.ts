import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { teacherApi } from '@/api/teacher.api';
import type { StudentProgressData, CourseAnalyticsData, AssignmentAnalyticsData, ClassPerformanceData } from '@/types/teacher';
import { useUIStore } from './ui';
import { handleApiCall } from '@/utils/api-handler';

export const useTeacherStore = defineStore('teacher', () => {
  const uiStore = useUIStore();

  // State
  const studentProgress = ref<StudentProgressData[]>([]);
  const courseAnalytics = ref<CourseAnalyticsData | null>(null);
  const assignmentAnalytics = ref<AssignmentAnalyticsData | null>(null);
  const classPerformance = ref<ClassPerformanceData | null>(null);
  const loading = computed(() => uiStore.loading);

  // Actions
  const fetchStudentProgress = async (params?: { page?: number; size?: number; courseId?: number }) => {
    const response = await handleApiCall(
      () => teacherApi.getStudentProgress(params),
      uiStore,
      '获取学生进度失败'
    );
    if (response) {
      // Corrected from response.data.content to response.data.items
      studentProgress.value = response.data.items;
    }
  };

  const fetchCourseAnalytics = async (courseId: number) => {
    const response = await handleApiCall(
      () => teacherApi.getCourseAnalytics(courseId),
      uiStore,
      '获取课程分析数据失败'
    );
    if (response) {
      courseAnalytics.value = response.data;
    }
  };
  
  const fetchAssignmentAnalytics = async (assignmentId: number) => {
    const response = await handleApiCall(
      () => teacherApi.getAssignmentAnalytics(assignmentId),
      uiStore,
      '获取作业分析数据失败'
    );
    if (response) {
      assignmentAnalytics.value = response.data;
    }
  };

  const fetchClassPerformance = async (courseId: number) => {
    const response = await handleApiCall(
      () => teacherApi.getClassPerformance(courseId),
      uiStore,
      '获取班级表现数据失败'
    );
    if (response) {
      classPerformance.value = response.data;
    }
  };

  return {
    studentProgress,
    courseAnalytics,
    assignmentAnalytics,
    classPerformance,
    loading,
    fetchStudentProgress,
    fetchCourseAnalytics,
    fetchAssignmentAnalytics,
    fetchClassPerformance,
  };
});
