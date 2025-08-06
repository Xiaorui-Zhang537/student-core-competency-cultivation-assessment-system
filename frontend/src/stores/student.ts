import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { studentApi } from '@/api/student.api';
import type { StudentDashboardData, StudentCourse, StudentLesson, StudentSubmission } from '@/types/student';
import { useUIStore } from './ui';
import { handleApiCall } from '@/utils/api-handler';

export const useStudentStore = defineStore('student', () => {
  const uiStore = useUIStore();

  // 默认仪表盘结构，避免模板首次渲染时报 undefined
  const defaultDashboardData: StudentDashboardData = {
    upcomingAssignments: [],
    activeCourses: [],
    recentGrades: [],
    overallProgress: 0,
  };

  const dashboardData = ref<StudentDashboardData>(defaultDashboardData);
  const myCourses = ref<StudentCourse[]>([]);
  const currentCourseProgress = ref<StudentCourse | null>(null);
  const currentLesson = ref<StudentLesson | null>(null);
  const mySubmissions = ref<StudentSubmission[]>([]);
  const loading = computed(() => uiStore.loading);

  // Actions
  const fetchDashboardData = async () => {
    const response = await handleApiCall(
      () => studentApi.getDashboardData(),
      uiStore,
      '获取仪表盘数据失败'
    );
    if (response) {
      // 用后端返回的数据覆盖默认占位，缺失字段仍保持安全默认值
      dashboardData.value = { ...defaultDashboardData, ...response };
    }
  };

  const fetchMyCourses = async () => {
    const response = await handleApiCall(
      () => studentApi.getMyCourses(),
      uiStore,
      '获取我的课程失败'
    );
    if (response) {
      myCourses.value = response.items;
    }
  };

  const fetchCourseProgress = async (courseId: string) => {
    const response = await handleApiCall(
      () => studentApi.getCourseProgress(courseId),
      uiStore,
      '获取课程进度失败'
    );
    if (response) {
      currentCourseProgress.value = response;
    }
  };

  const fetchLessonDetails = async (lessonId: string) => {
    const response = await handleApiCall(
      () => studentApi.getLessonDetails(lessonId),
      uiStore,
      '获取课程详情失败'
    );
    if (response) {
      currentLesson.value = response;
    }
  };
  
  const toggleLessonCompleted = async (lessonId: string, completed: boolean) => {
      const apiCall = completed 
          ? () => studentApi.markLessonAsCompleted(lessonId)
          : () => studentApi.markLessonAsIncomplete(lessonId);
          
      await handleApiCall(apiCall, uiStore, '更新课程状态失败');
      
      // Refresh course progress after update
      if(currentCourseProgress.value) {
          await fetchCourseProgress(String(currentCourseProgress.value.id));
      }
  }


  return {
    dashboardData,
    myCourses,
    currentCourseProgress,
    currentLesson,
    mySubmissions,
    loading,
    fetchDashboardData,
    fetchMyCourses,
    fetchCourseProgress,
    fetchLessonDetails,
    toggleLessonCompleted,
  };
});
