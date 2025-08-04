<template>
  <div class="p-6">
    <div v-if="courseStore.loading" class="text-center py-12">
      <p>正在加载课程数据...</p>
    </div>

    <div v-else-if="course" class="max-w-7xl mx-auto space-y-8">
      <!-- Header -->
      <div>
        <div class="flex items-start justify-between">
                <div>
            <h1 class="text-3xl font-bold">{{ course.title }}</h1>
            <p class="text-gray-500 mt-1">{{ course.description }}</p>
          </div>
          <div class="flex gap-2">
            <router-link :to="`/teacher/edit-course/${course.id}`" class="btn btn-secondary">编辑</router-link>
            <button @click="togglePublish" class="btn" :class="course.isPublished ? 'btn-warning' : 'btn-success'">
              {{ course.isPublished ? '取消发布' : '发布课程' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Stats -->
      <div v-if="teacherStore.courseAnalytics" class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div class="card p-4 text-center">
            <h3 class="text-2xl font-bold">{{ teacherStore.courseAnalytics.enrollmentCount }}</h3>
            <p class="text-sm text-gray-500">学生人数</p>
          </div>
        <div class="card p-4 text-center">
            <h3 class="text-2xl font-bold">{{ teacherStore.courseAnalytics.averageCompletionRate.toFixed(1) }}%</h3>
            <p class="text-sm text-gray-500">平均完成率</p>
          </div>
        <div class="card p-4 text-center">
            <h3 class="text-2xl font-bold">{{ teacherStore.courseAnalytics.averageScore.toFixed(1) }}</h3>
            <p class="text-sm text-gray-500">平均分</p>
          </div>
         <div class="card p-4 text-center">
            <h3 class="text-2xl font-bold">{{ teacherStore.courseAnalytics.assignmentCount }}</h3>
            <p class="text-sm text-gray-500">作业数</p>
          </div>
      </div>

      <!-- Lesson Management -->
      <div class="card">
        <div class="card-header">
            <h2 class="text-xl font-semibold">课程内容管理</h2>
            <button @click="openLessonModal()" class="btn btn-primary">添加课时</button>
              </div>
        <div v-if="lessonStore.loading" class="p-6 text-center">正在加载课时...</div>
        <div v-else-if="lessons.length === 0" class="p-6 text-center">
            <p>该课程还没有任何课时。</p>
                </div>
        <div v-else class="p-4 space-y-3">
          <div v-for="lesson in lessons" :key="lesson.id" class="flex items-center p-3 border rounded-lg">
                    <div class="flex-1">
              <p class="font-medium">{{ lesson.title }}</p>
              <p class="text-sm text-gray-600">{{ lesson.description }}</p>
            </div>
            <div class="flex gap-2">
              <button @click="openLessonModal(lesson)" class="btn btn-sm btn-secondary">编辑</button>
              <button @click="handleDeleteLesson(lesson.id)" class="btn btn-sm btn-danger">删除</button>
            </div>
                </div>
              </div>
            </div>
            </div>
    
    <!-- Lesson Form Modal -->
    <div v-if="isModalOpen" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white rounded-lg p-8 w-full max-w-lg space-y-6">
            <h2 class="text-2xl font-bold">{{ editingLesson ? '编辑课时' : '添加新课时' }}</h2>
            <form @submit.prevent="handleFormSubmit">
                <div>
                    <label class="label">标题</label>
                    <input v-model="lessonForm.title" type="text" class="input" required>
                </div>
                <div>
                    <label class="label">描述</label>
                    <textarea v-model="lessonForm.description" rows="3" class="input"></textarea>
                </div>
                <div>
                    <label class="label">内容 (可选)</label>
                    <textarea v-model="lessonForm.content" rows="5" class="input"></textarea>
                </div>
                <div class="flex justify-end gap-4 pt-4">
                    <button type="button" @click="isModalOpen = false" class="btn btn-secondary">取消</button>
                    <button type="submit" class="btn btn-primary" :disabled="lessonStore.loading">
                        {{ lessonStore.loading ? '保存中...' : '保存' }}
              </button>
            </div>
            </form>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue';
import { useRoute } from 'vue-router';
import { useCourseStore } from '@/stores/course';
import { useLessonStore } from '@/stores/lesson';
import { useTeacherStore } from '@/stores/teacher';
import type { Lesson } from '@/types/lesson';

const route = useRoute();
const courseStore = useCourseStore();
const lessonStore = useLessonStore();
const teacherStore = useTeacherStore();

const isModalOpen = ref(false);
const editingLesson = ref<Lesson | null>(null);

const lessonForm = reactive({
    title: '',
    description: '',
    content: ''
});

const course = computed(() => courseStore.currentCourse);
const lessons = computed(() => lessonStore.lessons);

const openLessonModal = (lesson: Lesson | null = null) => {
    editingLesson.value = lesson;
    if (lesson) {
        lessonForm.title = lesson.title;
        lessonForm.description = lesson.description;
        lessonForm.content = lesson.content || '';
    } else {
        lessonForm.title = '';
        lessonForm.description = '';
        lessonForm.content = '';
    }
    isModalOpen.value = true;
};

const handleFormSubmit = async () => {
    if (!course.value) return;

    const lessonData = {
        title: lessonForm.title,
        description: lessonForm.description,
        content: lessonForm.content,
        courseId: String(course.value.id),
    };
    
    let success = false;
    if (editingLesson.value) {
        success = !!await lessonStore.updateLesson(editingLesson.value.id, lessonData);
    } else {
        success = !!await lessonStore.createLesson(lessonData);
    }

    if (success) {
        isModalOpen.value = false;
    }
};

const handleDeleteLesson = async (lessonId: string) => {
    if (confirm('确定要删除这个课时吗？')) {
        await lessonStore.deleteLesson(lessonId);
    }
};

const togglePublish = async () => {
    if (!course.value) return;
    if (course.value.isPublished) {
        // TODO: Implement unpublish logic if available in API
        console.warn("Unpublish action not implemented yet.");
        // await courseStore.unpublishCourse(String(course.value.id));
    } else {
        await courseStore.publishCourse(String(course.value.id));
    }
};


onMounted(async () => {
  const courseId = route.params.id as string;
  if (courseId) {
    await Promise.all([
        courseStore.fetchCourseById(courseId),
        lessonStore.fetchLessonsForCourse(courseId),
        teacherStore.fetchCourseAnalytics(courseId)
    ]);
  }
});
</script> 
