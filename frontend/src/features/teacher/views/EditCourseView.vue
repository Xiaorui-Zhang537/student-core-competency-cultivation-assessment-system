<template>
  <div class="p-6 bg-gray-50">
    <div class="max-w-4xl mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold">编辑课程</h1>
        <p class="text-gray-500">更新课程的基本信息</p>
      </div>

      <!-- Loading State -->
      <div v-if="courseStore.loading && !courseForm.id" class="text-center py-12 card">
        <p>正在加载课程数据...</p>
      </div>

      <!-- Form -->
      <form v-else-if="courseForm.id" @submit.prevent="handleUpdateCourse" class="card p-8 space-y-6">
                <div>
          <label for="title" class="label">课程标题 <span class="text-red-500">*</span></label>
          <input id="title" v-model="courseForm.title" type="text" class="input" required />
                </div>

                <div>
          <label for="description" class="label">课程简介 <span class="text-red-500">*</span></label>
          <textarea id="description" v-model="courseForm.description" rows="3" class="input" required></textarea>
                </div>

                <div>
          <label for="content" class="label">详细描述</label>
          <textarea id="content" v-model="courseForm.content" rows="6" class="input"></textarea>
                  </div>

                  <div>
          <label for="category" class="label">课程分类 <span class="text-red-500">*</span></label>
          <select id="category" v-model="courseForm.category" class="input" required>
            <option disabled value="">请选择一个分类</option>
            <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
                    </select>
                </div>

        <!-- Actions -->
        <div class="flex justify-end gap-4 pt-4">
            <router-link :to="'/teacher/courses/' + courseForm.id" class="btn btn-secondary">
                取消
            </router-link>
            <button type="submit" class="btn btn-primary" :disabled="courseStore.loading">
                {{ courseStore.loading ? '保存中...' : '保存更改' }}
              </button>
        </div>
      </form>
      
      <!-- Error State -->
      <div v-else class="text-center py-12 card">
          <h3 class="text-lg font-medium">加载失败</h3>
          <p class="text-gray-500 mt-2">无法加载课程数据，或课程不存在。</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useCourseStore } from '@/stores/course';
import type { CourseUpdateRequest } from '@/types/course';

const route = useRoute();
const router = useRouter();
const courseStore = useCourseStore();

const courseForm = reactive<CourseUpdateRequest & { id: string | null }>({
  id: null,
  title: '',
  description: '',
  content: '',
  category: '',
});

// Mock categories
const categories = [
  '编程开发',
  '设计创意',
  '商业管理',
  '市场营销',
  '语言学习',
  '科学技术',
  '艺术人文',
  '其他',
];

const handleUpdateCourse = async () => {
    if (!courseForm.id) return;
    
    const { id, ...updateData } = courseForm;

    const response = await courseStore.updateCourse(courseForm.id, updateData);
    if (response) {
        router.push(`/teacher/courses/${courseForm.id}`);
    }
};

// Watch for the course data to be loaded into the store
watch(() => courseStore.currentCourse, (newCourse) => {
    if (newCourse) {
        courseForm.id = String(newCourse.id);
        courseForm.title = newCourse.title;
        courseForm.description = newCourse.description;
        courseForm.content = newCourse.content || '';
        courseForm.category = newCourse.category;
    }
});

onMounted(async () => {
  const courseId = route.params.id as string;
  if (courseId) {
    await courseStore.fetchCourseById(courseId);
  } else {
      // Handle case where ID is missing
      router.push('/teacher/manage-course');
  }
});
</script>

<style scoped lang="postcss">
.label {
    @apply block text-sm font-medium text-gray-700 mb-2;
}
</style>
