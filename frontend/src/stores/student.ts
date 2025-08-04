import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { studentApi } from '@/api/student.api';
import type { StudentDashboardData, StudentCourse } from '@/types/student';
import { handleApiCall } from '@/utils/api-handler';

export const useStudentStore = defineStore('student', () => {
  // State
  const dashboardData = ref<StudentDashboardData | null>(null);
  const myCourses = ref<StudentCourse[]>([]);
  const loading = ref(false);

  // Actions
  async function fetchDashboard() {
    const response = await handleApiCall(
      studentApi.getDashboard,
      { loadingRef: loading, errorMessage: '获取仪表盘数据失败' }
    );
    if (response?.data) {
      dashboardData.value = response.data;
    }
  }

  async function fetchMyCourses() {
    const response = await handleApiCall(
      () => studentApi.getMyCourses(), // Assuming it fetches all for now
      { loadingRef: loading, errorMessage: '获取我的课程列表失败' }
    );
    if (response?.data) {
      // Assuming the backend response for 'my/courses' matches StudentCourse[]
      // This might need adjustment based on the actual API response structure.
      myCourses.value = response.data.content as unknown as StudentCourse[];
    }
  }

  return {
    dashboardData,
    myCourses,
    loading,
    fetchDashboard,
    fetchMyCourses,
  };
});
