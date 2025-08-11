<template>
  <div class="p-6">
    <!-- Header -->
    <div class="mb-8 flex items-center justify-between">
      <div class="flex-1">
        <nav class="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400 mb-2">
          <router-link to="/teacher/courses" class="hover:text-gray-700 dark:hover:text-gray-200">
            课程管理
          </router-link>
          <chevron-right-icon class="w-4 h-4" />
          <template v-if="hasCourseContext">
            <router-link :to="`/teacher/courses/${effectiveCourseId}`" class="hover:text-gray-700 dark:hover:text-gray-200">
              {{ currentCourseTitle }}
            </router-link>
            <chevron-right-icon class="w-4 h-4" />
          </template>
          <span>作业管理</span>
        </nav>
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
            <option :value="null">请选择课程</option>
            <option v-for="course in teacherCourses" :key="course.id" :value="String(course.id)">
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
          <button @click="() => viewSubmissions(assignment)" class="btn btn-sm btn-outline mr-2">查看提交</button>
          <button @click="openEditModal(assignment)" class="btn btn-sm btn-outline mr-2">编辑</button>
          <button @click="handleDeleteAssignment(assignment)" class="btn btn-sm btn-danger-outline">删除</button>
        </div>
      </div>
       <div v-if="!assignmentStore.loading && assignmentStore.assignments.length === 0" class="text-center py-12 card">
        <h3 class="text-lg font-medium">暂无作业</h3>
        <p class="text-gray-500">{{ selectedCourseId ? '该课程下暂无作业。' : '请先选择一个课程以查看作业。' }}</p>
      </div>
      <!-- Pagination -->
      <div class="mt-6 flex items-center justify-between">
        <div class="flex items-center space-x-2">
          <span class="text-sm text-gray-700">每页显示</span>
          <select class="input input-sm w-20" @change="handleCourseFilterChange" v-model.number="pageSize">
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>
          <span class="text-sm text-gray-700">条</span>
        </div>

        <div class="flex items-center space-x-2">
          <button variant="outline" size="sm" @click="prevPage" :disabled="currentPage === 1">上一页</button>
          <span class="text-sm">第 {{ currentPage }} 页</span>
          <button variant="outline" size="sm" @click="nextPage">下一页</button>
        </div>
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
          <div>
            <label class="block text-sm font-medium mb-1">附件上传（可选，多文件）</label>
            <file-upload
              ref="assignmentUploader"
              :multiple="true"
              :autoUpload="false"
              :accept="'.pdf,.doc,.docx,.ppt,.pptx,.zip,image/*'"
              :upload-url="`${baseURL}/files/upload`"
              :upload-headers="uploadHeaders"
              :upload-data="assignmentUploadData"
              @upload-error="onAssignmentUploadError"
            />
            <!-- 已有关联附件列表（编辑时显示） -->
            <div v-if="editingAssignmentId && attachments.length" class="mt-4">
              <h4 class="text-sm font-medium mb-2">已上传附件</h4>
              <ul class="divide-y divide-gray-200">
                <li v-for="f in attachments" :key="f.id" class="py-2 flex items-center justify-between">
                  <div class="min-w-0 mr-3">
                    <div class="text-sm truncate">{{ f.originalName || f.fileName || ('附件#' + f.id) }}</div>
                    <div class="text-xs text-gray-500">大小：{{ formatSize(f.fileSize) }}</div>
                  </div>
                  <div class="flex items-center gap-2">
                    <a class="btn btn-sm btn-outline" :href="`${baseURL}/files/${f.id}/download`">下载</a>
                    <button class="btn btn-sm btn-danger-outline" @click="confirmDeleteAttachment(f.id)">删除</button>
                  </div>
                </li>
              </ul>
            </div>
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
import { ref, reactive, onMounted, computed } from 'vue';
import { useAssignmentStore } from '@/stores/assignment';
import { useCourseStore } from '@/stores/course';
import { useAuthStore } from '@/stores/auth';
import type { Assignment, AssignmentCreationRequest, AssignmentUpdateRequest } from '@/types/assignment';
import { useRoute } from 'vue-router';
import { ChevronRightIcon } from '@heroicons/vue/24/outline';
import FileUpload from '@/components/forms/FileUpload.vue';
import { baseURL } from '@/api/config';
import { fileApi } from '@/api/file.api';

const assignmentStore = useAssignmentStore();
const courseStore = useCourseStore();
const authStore = useAuthStore();
const route = useRoute();

const showModal = ref(false);
const isEditing = ref(false);
const editingAssignmentId = ref<string | null>(null);
const selectedCourseId = ref<string | null>(null);
const currentPage = ref(1);
const pageSize = ref(10);

const form = reactive<AssignmentCreationRequest & { id?: string }>({
  courseId: '',
  title: '',
  description: '',
  dueDate: '',
});
const assignmentUploader = ref();
const assignmentUploadData = reactive<{ purpose: string; relatedId?: string | number }>({ purpose: 'assignment_attachment' });
const uploadHeaders = {
  Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : ''
};
const attachments = ref<any[]>([]);

const refreshAttachments = async (assignmentId: string | number) => {
  const res: any = await fileApi.getRelatedFiles('assignment_attachment', assignmentId);
  attachments.value = res?.data || res || [];
};

const confirmDeleteAttachment = async (fileId: string | number) => {
  if (!editingAssignmentId.value) return;
  if (!confirm('确认删除该附件？此操作不可撤销。')) return;
  await fileApi.deleteFile(String(fileId));
  await refreshAttachments(editingAssignmentId.value);
};

const formatSize = (bytes?: number) => {
  if (!bytes || bytes <= 0) return '未知';
  const units = ['B','KB','MB','GB'];
  let i = 0; let n = bytes;
  while (n >= 1024 && i < units.length - 1) { n /= 1024; i++; }
  return `${n.toFixed(1)} ${units[i]}`;
};
const teacherCourses = computed(() => {
  if (!authStore.user?.id) return []
  return courseStore.courses.filter(c => String(c.teacherId) === String(authStore.user?.id))
});

const currentCourseTitle = computed(() => {
  const idToUse = (route.params.id as string | undefined) || selectedCourseId.value || ''
  if (!idToUse) return ''
  const found = courseStore.courses.find(c => String(c.id) === String(idToUse)) || courseStore.currentCourse
  return found?.title || ''
})

const effectiveCourseId = computed(() => {
  const cid = route.params.id as string | undefined
  if (cid) return cid
  if (selectedCourseId.value) return selectedCourseId.value
  return ''
})

const hasCourseContext = computed(() => effectiveCourseId.value !== '')


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
  // 加载已有附件
  refreshAttachments(assignment.id);
};

const closeModal = () => {
  showModal.value = false;
};

const handleSubmit = async () => {
  if (isEditing.value && editingAssignmentId.value) {
    const { title, description, dueDate } = form;
    const updateData: AssignmentUpdateRequest = { title, description, dueDate };
    const updated = await assignmentStore.updateAssignment(editingAssignmentId.value, updateData);
    // 编辑时，如有新文件被选择，则上传
    if (updated && assignmentUploader.value) {
      assignmentUploadData.relatedId = editingAssignmentId.value;
      await assignmentUploader.value.uploadFiles?.();
      await refreshAttachments(editingAssignmentId.value);
    }
  } else {
    const createData: AssignmentCreationRequest = { ...form };
    const created = await assignmentStore.createAssignment(createData);
    if (created) {
      const assignmentId = (created as any)?.id || (created as any)?.data?.id;
      if (assignmentId && assignmentUploader.value) {
        assignmentUploadData.relatedId = assignmentId;
        await assignmentUploader.value.uploadFiles?.();
      }
    }
  }
  if (!assignmentStore.loading) {
    closeModal();
  }
};

const onAssignmentUploadError = (msg: string) => {
  console.error('作业附件上传失败:', msg);
};

const handleDeleteAssignment = async (assignment: Assignment) => {
  if (confirm('您确定要删除这个作业吗？此操作无法撤销。')) {
    await assignmentStore.deleteAssignment(assignment.id, String(assignment.courseId));
  }
};

  const viewSubmissions = (assignment: Assignment) => {
    // 跳转到提交列表页
    window.location.href = `/teacher/assignments/${assignment.id}/submissions`;
  };

const handleCourseFilterChange = () => {
  if (selectedCourseId.value) {
    assignmentStore.fetchAssignments({ courseId: selectedCourseId.value, page: currentPage.value, size: pageSize.value })
  } else {
    assignmentStore.fetchAssignments({ page: currentPage.value, size: pageSize.value })
  }
};

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
    handleCourseFilterChange();
  }
};

const nextPage = () => {
  currentPage.value++;
  handleCourseFilterChange();
};

onMounted(async () => {
  if (!authStore.user) {
    await authStore.fetchUser();
  }
  await courseStore.fetchCourses({ page: 1, size: 100 });
  // 若带课程ID则确保 currentCourse 可用于面包屑
  const cid = route.params.id as string | undefined
  if (cid) {
    await courseStore.fetchCourseById(cid)
  }
  if (teacherCourses.value.length > 0) {
    // 如果 URL 中没有课程ID，但需要默认上下文，选取第一门课程
    if (!cid) {
      selectedCourseId.value = String(teacherCourses.value[0].id)
      // 填充 currentCourse 以便面包屑能立即显示课程名
      await courseStore.fetchCourseById(selectedCourseId.value)
    }
    handleCourseFilterChange();
  }
});
</script>
