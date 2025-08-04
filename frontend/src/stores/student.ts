import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { studentApi } from '@/api/student.api';
import type { StudentDashboardData, StudentCourse, StudentLesson, StudentSubmission } from '@/types/student';
import { useUIStore } from './ui';
import { handleApiCall } from '@/utils/api-handler';

export const useStudentStore = defineStore('student', () => {
  const uiStore = useUIStore();

  // State
  const dashboardData = ref<StudentDashboardData | null>(null);
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
      dashboardData.value = response.data;
    }
  };

  const fetchMyCourses = async () => {
    const response = await handleApiCall(
      () => studentApi.getMyCourses(),
      uiStore,
      '获取我的课程失败'
    );
    if (response) {
      myCourses.value = response.data.items;
    }
  };

  const fetchCourseProgress = async (courseId: string) => {
    const response = await handleApiCall(
      () => studentApi.getCourseProgress(courseId),
      uiStore,
      '获取课程进度失败'
    );
    if (response) {
      currentCourseProgress.value = response.data;
    }
  };

  const fetchLessonDetails = async (lessonId: string) => {
    const response = await handleApiCall(
      () => studentApi.getLessonDetails(lessonId),
      uiStore,
      '获取课程详情失败'
    );
    if (response) {
      currentLesson.value = response.data;
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
