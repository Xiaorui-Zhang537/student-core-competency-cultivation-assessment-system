import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { lessonApi } from '@/api/lesson.api';
import type { Lesson, StudentLesson } from '@/types/lesson';
import { handleApiCall } from '@/utils/api-handler';
import { useUIStore } from './ui';

export const useLessonStore = defineStore('lesson', () => {
  const lessons = ref<Lesson[]>([]); // Changed to simple Lesson type
  const currentLesson = ref<Lesson | null>(null);
  const loading = ref(false);
  const uiStore = useUIStore();

  // Computed
  const lessonsByCourse = computed(() => (courseId: string) => {
    return lessons.value.filter(l => l.courseId === courseId);
  });

  // Actions
  async function fetchLessonsForCourse(courseId: string) {
    const response = await handleApiCall(
      () => lessonApi.getLessonsByCourse(courseId),
      uiStore,
      '获取课时列表失败',
      { loadingRef: loading }
    ) as any;
    if (response) {
      lessons.value = Array.isArray(response) ? response : (response.items || []);
    }
  }

  async function fetchLesson(lessonId: string) {
    const response = await handleApiCall(
      () => lessonApi.getLesson(lessonId),
      uiStore,
      '获取课时详情失败',
      { loadingRef: loading }
    ) as any;
    if (response) {
      currentLesson.value = response;
    }
  }
  
  // Student action
  async function completeLesson(lessonId: string) {
    const response = await handleApiCall(
      () => lessonApi.completeLesson(lessonId),
      uiStore,
      '标记课时完成失败',
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
      uiStore,
      '创建课时失败',
      { loadingRef: loading, successMessage: '课时创建成功' }
    ) as any;
    if (response) {
      lessons.value.push(response);
      return response;
    }
    return null;
  }

  async function updateLesson(lessonId: string, lessonData: Partial<Lesson>) {
    const response = await handleApiCall(
      () => lessonApi.updateLesson(lessonId, lessonData),
      uiStore,
      '更新课时失败',
      { loadingRef: loading, successMessage: '课时更新成功' }
    ) as any;
    if (response) {
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
      uiStore,
      '删除课时失败',
      { loadingRef: loading, successMessage: '课时删除成功' }
    );
    if (response !== null) {
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
