import { defineStore } from 'pinia';
import { ref } from 'vue';
import { lessonApi } from '@/api/lesson.api';
import type { Lesson } from '@/types/lesson';
import { handleApiCall } from '@/utils/api-handler';
import { useUIStore } from './ui';

export const useLessonStore = defineStore('lesson', () => {
  const lessons = ref<Lesson[]>([]); // Changed to simple Lesson type
  const loading = ref(false);
  const uiStore = useUIStore();

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

  return {
    lessons,
    loading,
    fetchLessonsForCourse,
  };
});
