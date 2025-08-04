<template>
  <div class="p-6 bg-gray-50">
    <div class="max-w-4xl mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold">创建新课程</h1>
        <p class="text-gray-500">填写课程的基本信息</p>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleCreateCourse" class="card p-8 space-y-6">
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
          <p class="text-xs text-gray-500 mt-1">详细介绍课程内容、学习方法、适用对象等。</p>
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
            <router-link to="/teacher/manage-course" class="btn btn-secondary">
                取消
            </router-link>
            <button type="submit" class="btn btn-primary" :disabled="courseStore.loading">
                {{ courseStore.loading ? '创建中...' : '创建课程' }}
            </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useCourseStore } from '@/stores/course';
import type { CourseCreationRequest } from '@/types/course';

const router = useRouter();
const courseStore = useCourseStore();

const courseForm = reactive<CourseCreationRequest>({
  title: '',
  description: '',
  content: '',
  category: '',
  tags: [], // Added missing property
});

// Mock categories, ideally this should come from a dedicated API
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

const handleCreateCourse = async () => {
  const newCourse = await courseStore.createCourse(courseForm);
  if (newCourse) {
    // Navigate to the course management page after successful creation
    router.push('/teacher/manage-course');
  }
};
</script>

<style scoped lang="postcss">
.label {
    @apply block text-sm font-medium text-gray-700 mb-2;
}
</style>
