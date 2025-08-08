<template>
  <div class="p-6">
    <div v-if="courseStore.loading" class="text-center py-12">
      <p>正在加载课程详情...</p>
    </div>
    <div v-else-if="course" class="space-y-6">
      <div>
        <h1 class="text-3xl font-bold">{{ course.title }}</h1>
        <p class="text-gray-500">{{ course.category }}</p>
      </div>
      <div class="card p-6">
        <h2 class="text-xl font-semibold mb-4">课程描述</h2>
        <p>{{ course.description }}</p>
      </div>
      <div class="card p-6">
        <h2 class="text-xl font-semibold mb-4">课程内容</h2>
        <div v-html="course.content"></div>
      </div>
    </div>
    <div v-else class="text-center py-12">
      <h2 class="text-2xl font-bold">未找到课程</h2>
      <p class="text-gray-500">无法加载所请求的课程详情。</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue';
import { useCourseStore } from '@/stores/course';
import { useRoute } from 'vue-router';

const courseStore = useCourseStore();
const route = useRoute();

const course = computed(() => courseStore.currentCourse);

onMounted(() => {
  const courseId = route.params.id as string;
  if (courseId) {
    courseStore.fetchCourseById(courseId);
  }
});
</script>
