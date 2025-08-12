import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { teacherApi } from '@/api/teacher.api';
import type { CourseAnalyticsData, AssignmentAnalyticsData, ClassPerformanceData } from '@/types/teacher';
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
  totalStudents: 0,
  activeStudents: 0,
  totalAssignments: 0,
  completionRate: 0,
  timeSeriesData: [],
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
  totalStudents: 0,
  gradeStats: null,
  activityStats: null,
  gradeDistribution: [],
};
// ----------------------------------------------------------------------

// ◇ Utility to handle possible AxiosResponse wrappers
function unwrap<T>(res: any): T {
  return res && res.data !== undefined ? res.data : res;
}

export const useTeacherStore = defineStore('teacher', () => {
  const uiStore = useUIStore();

  // State
  const courseAnalytics = ref<CourseAnalyticsData>({ ...defaultCourseAnalytics });
  const assignmentAnalytics = ref<AssignmentAnalyticsData>({ ...defaultAssignmentAnalytics });
  const classPerformance = ref<ClassPerformanceData>({ ...defaultClassPerformance });
  const loading = computed(() => uiStore.loading);

  // Actions
  // 移除学生进度分页逻辑，统一使用课程学生表现端点

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
    courseAnalytics,
    assignmentAnalytics,
    classPerformance,
    loading,
    fetchCourseAnalytics,
    fetchAssignmentAnalytics,
    fetchClassPerformance,
  };
});
