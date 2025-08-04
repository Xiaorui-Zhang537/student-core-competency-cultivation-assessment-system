<template>
  <div class="p-6">
    <div v-if="courseStore.loading || lessonStore.loading" class="text-center py-12">
      <p>正在加载课程详情...</p>
    </div>

    <div v-else-if="course" class="max-w-7xl mx-auto">
      <!-- Breadcrumbs -->
      <nav class="mb-6">
        <ol class="flex items-center space-x-2 text-sm">
          <li><router-link to="/student/courses" class="text-gray-500 hover:text-blue-600">我的课程</router-link></li>
          <li><span class="text-gray-400">/</span></li>
          <li class="font-medium text-gray-700">{{ course.title }}</li>
        </ol>
      </nav>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Main Content -->
        <div class="lg:col-span-2">
          <!-- Course Header -->
          <div class="mb-8">
            <h1 class="text-3xl font-bold mb-2">{{ course.title }}</h1>
            <p class="text-lg text-gray-600">{{ course.description }}</p>
          </div>
          
          <!-- Lesson List -->
          <div class="card">
            <div class="card-header">
                <h2 class="text-xl font-semibold">课程内容</h2>
                <span class="text-sm text-gray-500">{{ completedLessonsCount }} / {{ lessons.length }} 已完成</span>
            </div>
            <div class="space-y-3 p-4">
              <div
                v-for="(lesson, index) in lessons"
                :key="lesson.id"
                class="flex items-center p-4 border rounded-lg transition-colors"
                :class="{ 'bg-green-50 border-green-200': lesson.isCompleted }"
              >
                <div class="flex-shrink-0 mr-4">
                  <div v-if="lesson.isCompleted" class="w-8 h-8 bg-green-500 rounded-full flex items-center justify-center text-white">✓</div>
                  <div v-else class="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center font-medium">{{ index + 1 }}</div>
                </div>
                <div class="flex-1">
                  <h3 class="font-medium">{{ lesson.title }}</h3>
                  <p class="text-sm text-gray-600">{{ lesson.description }}</p>
                </div>
                <button
                  v-if="!lesson.isCompleted"
                  class="btn btn-sm btn-outline-primary"
                  @click="handleCompleteLesson(lesson.id)"
                  :disabled="completingId === lesson.id"
                >
                  {{ completingId === lesson.id ? '处理中...' : '标记完成' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Sidebar -->
        <div class="lg:col-span-1">
          <div class="card">
             <div class="card-header">
                <h3 class="text-lg font-semibold">讲师信息</h3>
             </div>
             <div class="p-4 text-center">
                <div class="w-16 h-16 bg-gray-200 rounded-full mx-auto mb-4 flex items-center justify-center">
                  <!-- Avatar placeholder -->
                  <span class="text-xl font-medium text-gray-600">{{ course.instructorName.charAt(0) }}</span>
                </div>
                <h4 class="font-medium">{{ course.instructorName }}</h4>
             </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="text-center py-12 card">
      <h3 class="text-lg font-medium">课程不存在</h3>
      <p class="text-gray-500 mt-2">抱歉，您访问的课程不存在或已被删除。</p>
      <router-link to="/student/courses" class="btn btn-primary mt-4">返回课程列表</router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useCourseStore } from '@/stores/course';
import { useLessonStore } from '@/stores/lesson';

const route = useRoute();
const courseStore = useCourseStore();
const lessonStore = useLessonStore();

// State
const completingId = ref<string | null>(null);

// Computed Properties
const course = computed(() => courseStore.currentCourse);
const lessons = computed(() => lessonStore.lessons);
const completedLessonsCount = computed(() => lessons.value.filter(l => l.isCompleted).length);

// Methods
const handleCompleteLesson = async (lessonId: string) => {
  completingId.value = lessonId;
  const success = await lessonStore.completeLesson(lessonId);
  if (success && course.value) {
    // Optionally, re-fetch course to update progress, or calculate locally
    await courseStore.fetchCourse(course.value.id);
  }
  completingId.value = null;
};

// Lifecycle Hooks
onMounted(async () => {
  const courseId = route.params.id as string;
  if (courseId) {
    await Promise.all([
      courseStore.fetchCourse(courseId),
      lessonStore.fetchLessonsForCourse(courseId)
    ]);
  }
});
</script>
