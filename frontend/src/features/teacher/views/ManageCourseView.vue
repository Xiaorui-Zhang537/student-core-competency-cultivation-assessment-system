<template>
  <div class="p-6">
    <!-- Header -->
    <div class="mb-8 flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold">课程管理</h1>
        <p class="text-gray-500">管理您的所有课程</p>
      </div>
      <button @click="openCreateModal" class="btn btn-primary">创建课程</button>
    </div>

    <!-- Filters -->
    <div class="mb-6 card p-4">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <input v-model="filters.query" type="text" placeholder="搜索课程..." @input="applyFilters" class="input" />
        <select v-model="filters.status" @change="applyFilters" class="input">
          <option value="">全部状态</option>
          <option value="DRAFT">草稿</option>
          <option value="PUBLISHED">已发布</option>
          <option value="ARCHIVED">已归档</option>
        </select>
        <button @click="clearFilters" class="btn btn-outline">清除筛选</button>
      </div>
    </div>

    <!-- Course List -->
    <div v-if="courseStore.loading" class="text-center py-12">
        <p>正在加载课程...</p>
    </div>
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="course in courseStore.courses" :key="course.id" class="card overflow-hidden">
        <div class="h-40 bg-gray-200"></div>
        <div class="p-4">
          <h3 class="font-bold text-lg">{{ course.title }}</h3>
          <p class="text-sm text-gray-500 mb-2">{{ course.category }}</p>
          <p class="text-sm text-gray-600 truncate">{{ course.description }}</p>
          <div class="mt-4 flex justify-between items-center">
            <span class="badge" :class="statusClass(course.status)">{{ course.status }}</span>
            <div>
              <button @click="openEditModal(course)" class="btn btn-sm btn-outline mr-2">编辑</button>
              <button @click="handleDeleteCourse(String(course.id))" class="btn btn-sm btn-danger-outline">删除</button>
            </div>
          </div>
        </div>
      </div>
    </div>
     <div v-if="!courseStore.loading && courseStore.courses.length === 0" class="text-center py-12 card">
        <h3 class="text-lg font-medium">暂无课程</h3>
        <p class="text-gray-500 mb-4">开始创建您的第一个课程吧！</p>
        <button @click="openCreateModal" class="btn btn-primary">创建课程</button>
      </div>

    <!-- Create/Edit Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 w-full max-w-lg">
        <h2 class="text-xl font-bold mb-4">{{ isEditing ? '编辑课程' : '创建新课程' }}</h2>
        <form @submit.prevent="handleSubmit" class="space-y-4">
          <div>
            <label for="title" class="block text-sm font-medium mb-1">标题</label>
            <input id="title" v-model="form.title" type="text" required class="input" />
          </div>
          <div>
            <label for="description" class="block text-sm font-medium mb-1">描述</label>
            <textarea id="description" v-model="form.description" rows="3" class="input"></textarea>
          </div>
          <div>
            <label for="category" class="block text-sm font-medium mb-1">分类</label>
            <input id="category" v-model="form.category" type="text" required class="input" />
          </div>
          <div>
            <label for="tags" class="block text-sm font-medium mb-1">标签 (逗号分隔)</label>
            <input id="tags" v-model="tagsInput" type="text" class="input" />
          </div>
          <div class="flex justify-end space-x-3 mt-6">
            <button type="button" @click="closeModal" class="btn btn-outline">取消</button>
            <button type="submit" :disabled="courseStore.loading" class="btn btn-primary">
              {{ isEditing ? '保存更改' : '创建课程' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useCourseStore } from '@/stores/course';
import type { Course, CourseCreationRequest, CourseUpdateRequest } from '@/types/course';
import { debounce } from 'lodash-es';

const courseStore = useCourseStore();

const showModal = ref(false);
const isEditing = ref(false);
const editingCourseId = ref<string | null>(null);

const form = reactive<Omit<CourseCreationRequest, 'tags'> & { id?: string; tags: string[] }>({
  title: '',
  description: '',
  category: '',
  tags: [],
});
const tagsInput = ref('');

const filters = reactive({
    query: '',
    status: '',
});

const statusClass = (status: string) => {
    return {
        'bg-green-100 text-green-800': status === 'PUBLISHED',
        'bg-yellow-100 text-yellow-800': status === 'DRAFT',
        'bg-gray-100 text-gray-800': status === 'ARCHIVED',
    }
}

const openCreateModal = () => {
  isEditing.value = false;
  editingCourseId.value = null;
  Object.assign(form, { title: '', description: '', category: '', tags: [] });
  tagsInput.value = '';
  showModal.value = true;
};

const openEditModal = (course: Course) => {
  isEditing.value = true;
  editingCourseId.value = String(course.id);
  Object.assign(form, { ...course });
  tagsInput.value = course.tags.join(', ');
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
};

const handleSubmit = async () => {
  form.tags = tagsInput.value.split(',').map(t => t.trim()).filter(Boolean);
  
  if (isEditing.value && editingCourseId.value) {
    const { id, ...updateData } = form;
    await courseStore.updateCourse(editingCourseId.value, updateData as CourseUpdateRequest);
  } else {
    const { id, ...createData } = form;
    await courseStore.createCourse(createData as CourseCreationRequest);
  }
  
  if (!courseStore.loading) {
    closeModal();
    // Refresh the list
    await courseStore.fetchCourses({ page: 1, size: 10 });
  }
};

const handleDeleteCourse = async (id: string) => {
  if (confirm('您确定要删除这门课程吗？此操作无法撤销。')) {
    await courseStore.deleteCourse(id);
  }
};

const applyFilters = debounce(() => {
    console.log('Applying filters:', filters);
    // In a real app, you would do:
    courseStore.fetchCourses({ page: 1, size: 10, query: filters.query, status: filters.status });
}, 300);

const clearFilters = () => {
    filters.query = '';
    filters.status = '';
    applyFilters();
}

onMounted(() => {
  courseStore.fetchCourses({ page: 1, size: 10 });
});
</script>