import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { courseApi } from '@/api/course.api';
import type { Course, CourseDetailed, CourseCreationRequest, CourseUpdateRequest } from '@/types/course';
import { handleApiCall } from '@/utils/api-handler';

export const useCourseStore = defineStore('course', () => {
  const courses = ref<Course[]>([]);
  const currentCourse = ref<CourseDetailed | null>(null);
  const loading = ref(false);
  const totalCourses = ref(0);

  async function fetchCourses(params?: any) {
    const response = await handleApiCall(
      () => courseApi.getCourses(params),
      { loadingRef: loading, errorMessage: '获取课程列表失败' }
    );
    if (response?.data) {
      courses.value = response.data.content;
      totalCourses.value = response.data.totalElements;
    }
  }

  async function fetchCourse(id: string) {
    const response = await handleApiCall(
      () => courseApi.getCourseById(id),
      { loadingRef: loading, errorMessage: '获取课程详情失败' }
    );
    if (response?.data) {
      currentCourse.value = response.data;
    }
  }

  async function createCourse(data: CourseCreationRequest) {
    const response = await handleApiCall(
      () => courseApi.createCourse(data),
      { loadingRef: loading, successMessage: '课程创建成功' }
    );
    if (response?.data) {
      await fetchCourses();
      return response.data;
    }
    return null;
  }

  async function updateCourse(id: string, data: CourseUpdateRequest) {
    const response = await handleApiCall(
      () => courseApi.updateCourse(id, data),
      { loadingRef: loading, successMessage: '课程更新成功' }
    );
    if (response?.data) {
      const index = courses.value.findIndex(c => c.id === Number(id));
      if (index !== -1) {
        courses.value[index] = { ...courses.value[index], ...response.data };
      }
      if (currentCourse.value?.id === Number(id)) {
        currentCourse.value = { ...currentCourse.value, ...response.data };
      }
      return true;
    }
    return false;
  }

  async function deleteCourse(id: string) {
    const response = await handleApiCall(
      () => courseApi.deleteCourse(id),
      { loadingRef: loading, successMessage: '课程删除成功' }
    );
    if (response !== null) {
      await fetchCourses();
      return true;
    }
    return false;
  }
  
  async function enrollInCourse(courseId: string) {
      const response = await handleApiCall(
          () => courseApi.enrollInCourse(courseId),
          { loadingRef: loading, successMessage: '报名成功！' }
      );
      return response !== null;
  }
  
  async function publishCourse(courseId: string) {
      const response = await handleApiCall(
          () => courseApi.publishCourse(courseId),
          { loadingRef: loading, successMessage: '课程已发布' }
      );
      if (response?.data && currentCourse.value?.id === Number(courseId)) {
          currentCourse.value.isPublished = true;
      }
  }

  async function unpublishCourse(courseId: string) {
      const response = await handleApiCall(
          () => courseApi.unpublishCourse(courseId),
          { loadingRef: loading, successMessage: '课程已取消发布' }
      );
      if (response?.data && currentCourse.value?.id === Number(courseId)) {
          currentCourse.value.isPublished = false;
      }
  }

  return {
    courses,
    currentCourse,
    loading,
    totalCourses,
    fetchCourses,
    fetchCourse,
    createCourse,
    updateCourse,
    deleteCourse,
    enrollInCourse,
    publishCourse,
    unpublishCourse
  };
});
