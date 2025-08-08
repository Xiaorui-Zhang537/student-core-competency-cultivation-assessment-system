import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { teacherApi } from '@/api/teacher.api';
import type { StudentProgressData, CourseAnalyticsData, AssignmentAnalyticsData, ClassPerformanceData } from '@/types/teacher';
import { useUIStore } from './ui';
import { handleApiCall } from '@/utils/api-handler';
import type { PaginatedResponse } from '@/types/api';

// --- default placeholders to avoid undefined errors on first render ---
const defaultCourseAnalytics: CourseAnalyticsData = {
  courseId: 0,
  courseTitle: '',
  enrollmentCount: 0,
  averageCompletionRate: 0,
  averageScore: 0,
  assignmentCount: 0,
};

const defaultAssignmentAnalytics: AssignmentAnalyticsData = {
  assignmentId: 0,
  assignmentTitle: '',
  submissionCount: 0,
  averageScore: 0,
  submissionRate: 0,
};

const defaultClassPerformance: ClassPerformanceData = {
  courseId: 0,
  courseTitle: '',
  studentPerformance: [],
  scoreDistribution: [],
};
// ----------------------------------------------------------------------

// ◇ Utility to handle possible AxiosResponse wrappers
function unwrap<T>(res: any): T {
  return res && res.data !== undefined ? res.data : res;
}

export const useTeacherStore = defineStore('teacher', () => {
  const uiStore = useUIStore();

  // State
  const studentProgress = ref<StudentProgressData[]>([]);
  const courseAnalytics = ref<CourseAnalyticsData>({ ...defaultCourseAnalytics });
  const assignmentAnalytics = ref<AssignmentAnalyticsData>({ ...defaultAssignmentAnalytics });
  const classPerformance = ref<ClassPerformanceData>({ ...defaultClassPerformance });
  const loading = computed(() => uiStore.loading);

  // Actions
  const fetchStudentProgress = async (params?: { page?: number; size?: number; courseId?: string }) => {
    const response = await handleApiCall(
      () => teacherApi.getStudentProgress(params),
      uiStore,
      '获取学生进度失败'
    );
    if (response) {
      const data = unwrap<PaginatedResponse<StudentProgressData>>(response);
      studentProgress.value = data.items;
    }
  };

  const fetchCourseAnalytics = async (courseId: string) => {
    const response = await handleApiCall(
      () => teacherApi.getCourseAnalytics(courseId),
      uiStore,
      '获取课程分析数据失败'
    );
    if (response) {
        const data = unwrap<CourseAnalyticsData>(response);
      courseAnalytics.value = { ...defaultCourseAnalytics, ...data };
    }
  };

  const fetchAssignmentAnalytics = async (assignmentId: string) => {
    const response = await handleApiCall(
      () => teacherApi.getAssignmentAnalytics(assignmentId),
      uiStore,
      '获取作业分析数据失败'
    );
    if (response) {
        const data = unwrap<AssignmentAnalyticsData>(response);
      assignmentAnalytics.value = { ...defaultAssignmentAnalytics, ...data };
    }
  };

  const fetchClassPerformance = async (courseId: string) => {
    const response = await handleApiCall(
      () => teacherApi.getClassPerformance(courseId),
      uiStore,
      '获取班级表现数据失败'
    );
    if (response) {
        const data = unwrap<ClassPerformanceData>(response);
      classPerformance.value = { ...defaultClassPerformance, ...data };
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
