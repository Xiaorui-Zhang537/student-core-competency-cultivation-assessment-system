import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { lessonApi } from '@/api/lesson.api';
import type { Lesson, StudentLesson } from '@/types/lesson';
import { handleApiCall } from '@/utils/api-handler';

export const useLessonStore = defineStore('lesson', () => {
  const lessons = ref<Lesson[]>([]); // Changed to simple Lesson type
  const currentLesson = ref<Lesson | null>(null);
  const loading = ref(false);

  // Computed
  const lessonsByCourse = computed(() => (courseId: string) => {
    return lessons.value.filter(l => l.courseId === courseId);
  });

  // Actions
  async function fetchLessonsForCourse(courseId: string) {
    const response = await handleApiCall(
      () => lessonApi.getLessonsByCourse(courseId),
      { loadingRef: loading, errorMessage: '获取课时列表失败' }
    );
    if (response?.data) {
      lessons.value = response;
    }
  }

  async function fetchLesson(lessonId: string) {
    const response = await handleApiCall(
      () => lessonApi.getLesson(lessonId),
      { loadingRef: loading, errorMessage: '获取课时详情失败' }
    );
    if (response?.data) {
      currentLesson.value = response;
    }
  }
  
  // Student action
  async function completeLesson(lessonId: string) {
    const response = await handleApiCall(
      () => lessonApi.completeLesson(lessonId),
      { successMessage: '课时已标记为完成' }
    );
    if (response) {
      const lesson = lessons.value.find(l => l.id === lessonId) as StudentLesson | undefined;
      if (lesson) {
        lesson.isCompleted = true;
        lesson.progress = 100;
      }
    }
    return !!response;
  }
  
  // Teacher actions
  async function createLesson(lessonData: Partial<Lesson>) {
    const response = await handleApiCall(
      () => lessonApi.createLesson(lessonData),
      { loadingRef: loading, successMessage: '课时创建成功', errorMessage: '创建课时失败' }
    );
    if (response?.data) {
      lessons.value.push(response);
      return response;
    }
    return null;
  }

  async function updateLesson(lessonId: string, lessonData: Partial<Lesson>) {
    const response = await handleApiCall(
      () => lessonApi.updateLesson(lessonId, lessonData),
      { loadingRef: loading, successMessage: '课时更新成功', errorMessage: '更新课时失败' }
    );
    if (response?.data) {
      const index = lessons.value.findIndex(l => l.id === lessonId);
      if (index !== -1) {
        lessons.value[index] = { ...lessons.value[index], ...response };
      }
      return response;
    }
    return null;
  }

  async function deleteLesson(lessonId: string) {
    const response = await handleApiCall(
      () => lessonApi.deleteLesson(lessonId),
      { loadingRef: loading, successMessage: '课时删除成功', errorMessage: '删除课时失败' }
    );
    if (response !== null) { // Deletion returns void, so check for not-null response
      lessons.value = lessons.value.filter(l => l.id !== lessonId);
      return true;
    }
    return false;
  }


  return {
    lessons,
    currentLesson,
    loading,
    lessonsByCourse,
    fetchLessonsForCourse,
    fetchLesson,
    completeLesson,
    createLesson,
    updateLesson,
    deleteLesson
  };
});
