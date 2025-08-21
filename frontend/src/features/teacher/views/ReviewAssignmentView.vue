<template>
  <div class="min-h-screen p-6">
    <div class="max-w-7xl mx-auto">
    <!-- Header -->
      <div class="mb-8 flex items-center justify-between">
      <div class="flex-1">
        <nav class="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400 mb-2">
          <span class="hover:text-gray-700 dark:hover:text-gray-200 cursor-pointer" @click="router.push('/teacher/courses')">
            {{ t('teacher.assignments.breadcrumb.courses') }}
          </span>
          <ChevronRightIcon class="w-4 h-4" />
          <template v-if="hasCourseContext">
            <span class="hover:text-gray-700 dark:hover:text-gray-200 cursor-pointer" @click="router.push({ name: 'TeacherCourseDetail', params: { id: effectiveCourseId } })">
              {{ currentCourseTitle }}
            </span>
            <ChevronRightIcon class="w-4 h-4" />
          </template>
          <span>{{ t('teacher.assignments.breadcrumb.self') }}</span>
        </nav>
        <h1 class="text-3xl font-bold">{{ t('teacher.assignments.title') }}</h1>
        <p class="text-gray-500">{{ t('teacher.assignments.subtitle') }}</p>
      </div>
        <Button variant="purple" @click="openCreateModal" :disabled="!courseStore.courses.length">
          <PlusIcon class="w-4 h-4 mr-2" />
          {{ t('teacher.assignments.actions.create') }}
        </Button>
    </div>

    <!-- Filters -->
    <div class="mb-6 card p-4">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label for="course-filter" class="block text-sm font-medium mb-1">{{ t('teacher.assignments.filters.byCourse') }}</label>
          <select id="course-filter" v-model="selectedCourseId" @change="handleCourseFilterChange" class="input">
            <option :value="null">{{ t('teacher.assignments.filters.selectCourse') }}</option>
            <option v-for="course in teacherCourses" :key="course.id" :value="String(course.id)">
              {{ course.title }}
            </option>
          </select>
        </div>
      </div>
    </div>

    <!-- Assignment List -->
    <div v-if="assignmentStore.loading" class="text-center py-12">
      <p>{{ t('teacher.assignments.loading') }}</p>
    </div>
    <div v-else class="space-y-4">
      <div v-for="assignment in assignmentStore.assignments" :key="assignment.id" class="card p-4 flex justify-between items-center">
        <div>
          <h3 class="font-bold text-lg">{{ assignment.title }}</h3>
          <p class="text-sm text-gray-600">{{ assignment.description }}</p>
          <div class="text-sm text-gray-500 mt-2">
            {{ t('teacher.assignments.list.dueDate') }}: {{ new Date(assignment.dueDate).toLocaleDateString() }}
            <span class="ml-4 badge" :class="statusClass(assignment.status)">{{ assignment.status }}</span>
          </div>
        </div>
        <div>
          <Button size="sm" variant="teal" class="mr-2" @click="() => viewSubmissions(assignment)">
            <InboxArrowDownIcon class="w-4 h-4 mr-1" />
            {{ t('teacher.assignments.actions.viewSubmissions') }}
          </Button>
          <Button size="sm" variant="outline" class="mr-2" @click="openEditModal(assignment)">
            <PencilSquareIcon class="w-4 h-4 mr-1" />
            {{ t('teacher.assignments.actions.edit') }}
          </Button>
          <Button size="sm" variant="outline" @click="handleDeleteAssignment(assignment)">
            <TrashIcon class="w-4 h-4 mr-1" />
            {{ t('teacher.assignments.actions.delete') }}
          </Button>
        </div>
      </div>
       <div v-if="!assignmentStore.loading && assignmentStore.assignments.length === 0" class="text-center py-12 card">
        <h3 class="text-lg font-medium">{{ t('teacher.assignments.list.emptyTitle') }}</h3>
        <p class="text-gray-500">{{ selectedCourseId ? t('teacher.assignments.list.emptyDescWithCourse') : t('teacher.assignments.list.emptyDescNoCourse') }}</p>
      </div>
      <!-- Pagination -->
      <div class="mt-6 flex items-center justify-between">
        <div class="flex items-center space-x-2">
          <span class="text-sm text-gray-700">{{ t('teacher.assignments.pagination.perPagePrefix') }}</span>
          <select class="input input-sm w-20" @change="handleCourseFilterChange" v-model.number="pageSize">
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>
          <span class="text-sm text-gray-700">{{ t('teacher.assignments.pagination.perPageSuffix') }}</span>
        </div>

        <div class="flex items-center space-x-2">
          <Button variant="outline" size="sm" @click="prevPage" :disabled="currentPage === 1">{{ t('teacher.assignments.pagination.prev') }}</Button>
          <span class="text-sm">{{ t('teacher.assignments.pagination.page', { page: currentPage }) }}</span>
          <Button variant="outline" size="sm" @click="nextPage">{{ t('teacher.assignments.pagination.next') }}</Button>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-lg max-h-[80vh] overflow-y-auto">
        <h2 class="text-xl font-bold mb-4">{{ isEditing ? t('teacher.assignments.modal.editTitle') : t('teacher.assignments.modal.createTitle') }}</h2>
        <form @submit.prevent="handleSubmit" class="space-y-4">
           <div>
            <label for="courseId" class="block text-sm font-medium mb-1">{{ t('teacher.assignments.modal.course') }}</label>
            <select id="courseId" v-model="form.courseId" required class="input" :disabled="isEditing">
              <option v-for="course in courseStore.courses" :key="course.id" :value="course.id">
                {{ course.title }}
              </option>
            </select>
          </div>
          <div>
            <label for="title" class="block text-sm font-medium mb-1">{{ t('teacher.assignments.modal.title') }}</label>
            <input id="title" v-model="form.title" type="text" required class="input" />
          </div>
          <div>
            <label for="description" class="block text-sm font-medium mb-1">{{ t('teacher.assignments.modal.description') }}</label>
            <textarea id="description" v-model="form.description" rows="3" class="input"></textarea>
          </div>
          <div>
            <label for="dueDate" class="block text-sm font-medium mb-1">{{ t('teacher.assignments.modal.dueDate') }}</label>
            <input id="dueDate" v-model="form.dueDate" type="date" required class="input" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">{{ t('teacher.assignments.modal.attachments') }}</label>
            <FileUpload
              ref="assignmentUploader"
              :multiple="true"
              :autoUpload="false"
              :accept="'.pdf,.doc,.docx,.ppt,.pptx,.zip,image/*'"
              :upload-url="`${baseURL}/files/upload`"
              :upload-headers="uploadHeaders"
              :upload-data="assignmentUploadData"
              @update:files="onFilesUpdate"
              @upload-error="onAssignmentUploadError"
            />
            <!-- 已有关联附件列表（编辑时显示） -->
            <div v-if="editingAssignmentId && attachments.length" class="mt-4">
              <h4 class="text-sm font-medium mb-2">{{ t('teacher.assignments.modal.existing') }}</h4>
              <ul class="divide-y divide-gray-200">
                <li v-for="f in attachments" :key="f.id" class="py-2 flex items-center justify-between">
                  <div class="min-w-0 mr-3">
                    <div class="text-sm truncate">{{ f.originalName || f.fileName || ('附件#' + f.id) }}</div>
                    <div class="text-xs text-gray-500">{{ t('teacher.assignments.modal.size') }}：{{ formatSize(f.fileSize) }}</div>
                  </div>
                  <div class="flex items-center gap-2">
                    <a class="btn btn-sm btn-outline" :href="`${baseURL}/files/${f.id}/download`">{{ t('teacher.assignments.modal.download') }}</a>
                    <button type="button" class="btn btn-sm btn-danger-outline" @click="confirmDeleteAttachment(f.id)">{{ t('teacher.assignments.modal.delete') }}</button>
                  </div>
                </li>
              </ul>
            </div>
          </div>
          <div class="flex justify-end space-x-3 mt-6">
            <button type="button" @click="closeModal" class="btn btn-secondary">{{ t('teacher.assignments.modal.cancel') }}</button>
            <button type="submit" :disabled="assignmentStore.loading" class="btn btn-primary">
              {{ isEditing ? t('teacher.assignments.actions.save') : t('teacher.assignments.modal.create') }}
            </button>
          </div>
        </form>
      </div>
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
import { useRoute, useRouter } from 'vue-router';
import Button from '@/components/ui/Button.vue'
import { ChevronRightIcon, PlusIcon, InboxArrowDownIcon, PencilSquareIcon, TrashIcon } from '@heroicons/vue/24/outline';
import FileUpload from '@/components/forms/FileUpload.vue';
import { baseURL } from '@/api/config';
import { fileApi } from '@/api/file.api';
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'

const assignmentStore = useAssignmentStore();
const courseStore = useCourseStore();
const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();
const { t } = useI18n()

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
  if (!confirm(t('teacher.assignments.confirm.deleteAttachment'))) return;
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
  const normalize = (v: string) => (/^\d{4}-\d{2}-\d{2}$/.test(v) ? `${v} 23:59:59` : v)
  if (isEditing.value && editingAssignmentId.value) {
    const { title, description, dueDate } = form;
    const updateData: AssignmentUpdateRequest = { title, description, dueDate: normalize(dueDate) } as any;
    // 确保 courseId 传回后端，避免“课程ID不能为空”
    (updateData as any).courseId = form.courseId;
    // 提供默认满分，避免后端校验失败（若后端允许不更新可移除）
    (updateData as any).maxScore = (updateData as any).maxScore || 100;
    const updated = await assignmentStore.updateAssignment(editingAssignmentId.value, updateData);
    // 编辑时，如有新文件被选择，则上传
    if (updated && assignmentUploader.value) {
      assignmentUploadData.relatedId = editingAssignmentId.value;
      await assignmentUploader.value.uploadFiles?.();
      await refreshAttachments(editingAssignmentId.value);
    }
  } else {
    const createData: AssignmentCreationRequest = { ...form, dueDate: normalize(form.dueDate) } as any;
    // 默认满分 100 分
    (createData as any).maxScore = 100;
    // 先创建作业，但不立即弹出成功提示，待附件上传完成后再关闭弹窗
    const created = await assignmentStore.createAssignment(createData);
    if (created) {
      const assignmentId = (created as any)?.id || (created as any)?.data?.id;
      if (assignmentId && assignmentUploader.value) {
        assignmentUploadData.relatedId = assignmentId;
        try {
          await assignmentUploader.value.uploadFiles?.();
        } catch (_) {
          // FileUpload 内部已处理错误提示，这里不打断流程
        }
      }
    }
  }
  if (!assignmentStore.loading) {
    // 直接关闭，无弹窗
    closeModal();
  }
};

const uploadFailed = ref(false);
const onAssignmentUploadError = (msg: string) => {
  uploadFailed.value = true;
  console.error('作业附件上传失败:', msg);
};

// 本地缓存当前选择的附件，用于“有附件才允许创建”的约束
const selectedFiles = ref<any[]>([]);
const onFilesUpdate = (files: any[]) => {
  selectedFiles.value = files || [];
};

const handleDeleteAssignment = async (assignment: Assignment) => {
  if (confirm(t('teacher.assignments.confirm.deleteAssignment'))) {
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
  // 读取查询参数：courseId 用于筛选，openCreate=1 用于自动弹出创建
  const qCourseId = (route.query.courseId as string | undefined) || undefined
  const qOpenCreate = String(route.query.openCreate || '')
  if (teacherCourses.value.length > 0) {
    // 如果 URL 中没有课程ID，但需要默认上下文，选取第一门课程
    if (!cid) {
      selectedCourseId.value = qCourseId || String(teacherCourses.value[0].id)
      // 填充 currentCourse 以便面包屑能立即显示课程名
      await courseStore.fetchCourseById(selectedCourseId.value)
    }
    handleCourseFilterChange();
  }
  // 若要求自动创建作业
  if (qOpenCreate === '1') {
    if (qCourseId) {
      form.courseId = qCourseId
    } else if (!form.courseId && courseStore.courses.length > 0) {
      form.courseId = String(courseStore.courses[0].id)
    }
    openCreateModal()
  }
});
</script>
