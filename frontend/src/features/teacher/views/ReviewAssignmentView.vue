<template>
  <div class="p-6">
    <!-- Header -->
    <div class="mb-8 flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold">作业管理</h1>
        <p class="text-gray-500">创建、查看和管理课程作业</p>
      </div>
      <button @click="openCreateModal" class="btn btn-primary" :disabled="!courseStore.courses.length">
        创建作业
      </button>
    </div>

    <!-- Filters -->
    <div class="mb-6 card p-4">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label for="course-filter" class="block text-sm font-medium mb-1">按课程筛选</label>
          <select id="course-filter" v-model="selectedCourseId" @change="handleCourseFilterChange" class="input">
            <option :value="null">所有课程</option>
            <option v-for="course in courseStore.courses" :key="course.id" :value="course.id">
              {{ course.title }}
            </option>
          </select>
        </div>
      </div>
    </div>

    <!-- Assignment List -->
    <div v-if="assignmentStore.loading" class="text-center py-12">
      <p>正在加载作业...</p>
    </div>
    <div v-else class="space-y-4">
      <div v-for="assignment in assignmentStore.assignments" :key="assignment.id" class="card p-4 flex justify-between items-center">
        <div>
          <h3 class="font-bold text-lg">{{ assignment.title }}</h3>
          <p class="text-sm text-gray-600">{{ assignment.description }}</p>
          <div class="text-sm text-gray-500 mt-2">
            截止日期: {{ new Date(assignment.dueDate).toLocaleDateString() }}
            <span class="ml-4 badge" :class="statusClass(assignment.status)">{{ assignment.status }}</span>
          </div>
        </div>
        <div>
          <button @click="openEditModal(assignment)" class="btn btn-sm btn-outline mr-2">编辑</button>
          <button @click="handleDeleteAssignment(assignment)" class="btn btn-sm btn-danger-outline">删除</button>
        </div>
      </div>
       <div v-if="!assignmentStore.loading && assignmentStore.assignments.length === 0" class="text-center py-12 card">
        <h3 class="text-lg font-medium">暂无作业</h3>
        <p class="text-gray-500">{{ selectedCourseId ? '该课程下暂无作业。' : '请先选择一个课程以查看作业。' }}</p>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 w-full max-w-lg">
        <h2 class="text-xl font-bold mb-4">{{ isEditing ? '编辑作业' : '创建新作业' }}</h2>
        <form @submit.prevent="handleSubmit" class="space-y-4">
           <div>
            <label for="courseId" class="block text-sm font-medium mb-1">课程</label>
            <select id="courseId" v-model="form.courseId" required class="input" :disabled="isEditing">
              <option v-for="course in courseStore.courses" :key="course.id" :value="course.id">
                {{ course.title }}
              </option>
            </select>
          </div>
          <div>
            <label for="title" class="block text-sm font-medium mb-1">标题</label>
            <input id="title" v-model="form.title" type="text" required class="input" />
          </div>
          <div>
            <label for="description" class="block text-sm font-medium mb-1">描述</label>
            <textarea id="description" v-model="form.description" rows="3" class="input"></textarea>
          </div>
          <div>
            <label for="dueDate" class="block text-sm font-medium mb-1">截止日期</label>
            <input id="dueDate" v-model="form.dueDate" type="date" required class="input" />
          </div>
          <div class="flex justify-end space-x-3 mt-6">
            <button type="button" @click="closeModal" class="btn btn-outline">取消</button>
            <button type="submit" :disabled="assignmentStore.loading" class="btn btn-primary">
              {{ isEditing ? '保存更改' : '创建作业' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useAssignmentStore } from '@/stores/assignment';
import { useCourseStore } from '@/stores/course';
import type { Assignment, AssignmentCreationRequest, AssignmentUpdateRequest } from '@/types/assignment';

const assignmentStore = useAssignmentStore();
const courseStore = useCourseStore();

const showModal = ref(false);
const isEditing = ref(false);
const editingAssignmentId = ref<string | null>(null);
const selectedCourseId = ref<string | null>(null);

const form = reactive<AssignmentCreationRequest & { id?: string }>({
  courseId: '',
  title: '',
  description: '',
  dueDate: '',
});

const statusClass = (status: string) => ({
  'bg-green-100 text-green-800': status === 'PUBLISHED',
  'bg-yellow-100 text-yellow-800': status === 'DRAFT',
  'bg-gray-100 text-gray-800': status === 'CLOSED',
});

const resetForm = () => {
    Object.assign(form, { courseId: selectedCourseId.value || '', title: '', description: '', dueDate: '' });
};

const openCreateModal = () => {
  if (!courseStore.courses.length) {
      alert('请先创建一门课程。');
      return;
  }
  isEditing.value = false;
  editingAssignmentId.value = null;
  resetForm();
  if (!form.courseId && courseStore.courses.length > 0) {
      form.courseId = String(courseStore.courses[0].id);
  }
  showModal.value = true;
};

const openEditModal = (assignment: Assignment) => {
  isEditing.value = true;
  editingAssignmentId.value = assignment.id;
  const dueDate = new Date(assignment.dueDate).toISOString().split('T')[0];
  Object.assign(form, { ...assignment, courseId: String(assignment.courseId), dueDate });
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
};

const handleSubmit = async () => {
  if (isEditing.value && editingAssignmentId.value) {
    const { title, description, dueDate } = form;
    const updateData: AssignmentUpdateRequest = { title, description, dueDate };
    await assignmentStore.updateAssignment(editingAssignmentId.value, updateData);
  } else {
    const createData: AssignmentCreationRequest = { ...form };
    await assignmentStore.createAssignment(createData);
  }
  
  if (!assignmentStore.loading) {
    closeModal();
  }
};

const handleDeleteAssignment = async (assignment: Assignment) => {
  if (confirm('您确定要删除这个作业吗？此操作无法撤销。')) {
    await assignmentStore.deleteAssignment(assignment.id, String(assignment.courseId));
  }
};

const handleCourseFilterChange = () => {
    if(selectedCourseId.value) {
        assignmentStore.fetchAssignments({ courseId: selectedCourseId.value });
    } else {
        assignmentStore.setAssignments([]);
    }
};

onMounted(async () => {
  await courseStore.fetchCourses({ page: 1, size: 100 });
  if(courseStore.courses.length > 0) {
      selectedCourseId.value = String(courseStore.courses[0].id);
      handleCourseFilterChange();
  }
});
</script>
