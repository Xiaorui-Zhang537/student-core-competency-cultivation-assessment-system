<template>
  <div class="min-h-screen p-6">
    <div class="max-w-7xl mx-auto">
    <!-- Header -->
      <div class="mb-3">
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
        <PageHeader :title="t('teacher.assignments.title')" :subtitle="t('teacher.assignments.subtitle')">
          <template #actions>
            <Button variant="primary" @click="openCreateModal" :disabled="!courseStore.courses.length">
              <PlusIcon class="w-4 h-4 mr-2" />
              {{ t('teacher.assignments.actions.create') }}
            </Button>
            <Button variant="success" class="ml-2" @click="goAiGrading()">
              <InboxArrowDownIcon class="w-4 h-4 mr-1" />
              {{ t('teacher.aiGrading.entry') }}
            </Button>
          </template>
        </PageHeader>
      </div>
    </div>

    <!-- Filters: 标题-选择器一排 + 状态选择器 + 右侧搜索器 -->
    <div class="mb-3 card p-4">
      <div class="flex items-center gap-1 flex-wrap">
        <div class="w-auto flex items-center gap-2">
          <span class="text-sm font-semibold leading-tight text-gray-700 dark:text-gray-300">{{ t('teacher.assignments.filters.byCourse') }}</span>
          <div class="w-56">
          <GlassPopoverSelect
            v-model="selectedCourseId"
            :options="teacherCourses.map((c: any) => ({ label: c.title, value: String(c.id) }))"
              :placeholder="(t('teacher.assignments.filters.selectCourse') as string)"
              size="sm"
              :fullWidth="false"
              @change="handleCourseFilterChange"
            />
          </div>
        </div>

        <div class="w-auto flex items-center gap-2 -ml-16">
          <span class="text-sm font-semibold leading-tight text-gray-700 dark:text-gray-300">{{ t('teacher.assignments.filters.statusLabel') || '状态' }}</span>
          <div class="w-64">
            <GlassPopoverSelect
              v-model="statusFilter"
              :options="statusOptions"
              :placeholder="(t('teacher.assignments.filters.statusPlaceholder') as string) || '请选择状态'"
              size="sm"
              :fullWidth="false"
            @change="handleCourseFilterChange"
          />
          </div>
        </div>

        <div class="ml-auto w-56">
          <GlassSearchInput v-model="searchText" :placeholder="(t('teacher.assignments.searchPlaceholder') as string) || '搜索作业'" size="sm" />
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
            {{ t('teacher.assignments.list.dueDate') }}: {{ formatMinute(assignment.dueDate) }}
            <Badge class="ml-2" size="sm" :variant="statusVariantByDisplay(displayStatusKey(assignment))">{{ renderStatus(assignment) }}</Badge>
          </div>
          <div v-if="String(assignment.status).toLowerCase()==='scheduled' && assignment.publishAt" class="text-xs text-gray-500 mt-1">
            {{ (t('teacher.assignments.modal.publishAt') as string) }}: {{ formatMinute(assignment.publishAt) }}
          </div>
        </div>
        <div>
          <Button size="sm" variant="success" class="mr-2" @click="() => viewSubmissions(assignment)">
            <InboxArrowDownIcon class="w-4 h-4 mr-1" />
            {{ t('teacher.assignments.actions.viewSubmissions') }}
          </Button>
          <Button size="sm" variant="outline" class="mr-2" @click="openEditModal(assignment)">
            <PencilSquareIcon class="w-4 h-4 mr-1" />
            {{ t('teacher.assignments.actions.edit') }}
          </Button>
          <Button size="sm" variant="danger" @click="handleDeleteAssignment(assignment)">
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
          <div class="w-24">
            <GlassPopoverSelect
              :model-value="pageSize"
              :options="[{label:'10', value:10}, {label:'20', value:20}, {label:'50', value:50}]"
              size="sm"
              @update:modelValue="(v:any)=>{ pageSize = Number(v||10); handleCourseFilterChange() }"
            />
          </div>
          <span class="text-sm text-gray-700">{{ t('teacher.assignments.pagination.perPageSuffix') }}</span>
        </div>

        <div class="flex items-center space-x-2">
          <Button variant="outline" size="sm" @click="prevPage" :disabled="currentPage === 1">{{ t('teacher.assignments.pagination.prev') }}</Button>
          <span class="text-sm">{{ t('teacher.assignments.pagination.page', { page: currentPage }) }}</span>
          <Button variant="outline" size="sm" @click="nextPage" :disabled="currentPage >= totalPages">{{ t('teacher.assignments.pagination.next') }}</Button>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal (GlassModal) -->
    <GlassModal v-if="showModal" :title="(isEditing ? t('teacher.assignments.modal.editTitle') : t('teacher.assignments.modal.createTitle')) as string" maxWidth="max-w-lg" heightVariant="normal" @close="closeModal">
      <div class="mb-3">
        <label class="block text-sm font-medium mb-1">{{ t('teacher.assignments.modal.visibility') || '可见性' }}</label>
        <div class="flex items-center gap-4 text-sm">
          <SegmentedPills :model-value="publishMode" :options="publishOptions" size="sm" @update:modelValue="(v:any)=> publishMode = v" />
        </div>
        <p class="mt-1 text-xs text-gray-500">{{ t('teacher.assignments.modal.visibilityHint') || '草稿不会对学生可见；仅发布后学生才能看到并提交。' }}</p>
      </div>
      <form @submit.prevent="handleSubmit" class="space-y-4">
         <div>
          <label for="courseId" class="block text-sm font-medium mb-1">{{ t('teacher.assignments.modal.course') }}</label>
          <select id="courseId" v-model="form.courseId" required class="ui-pill--select ui-pill--pl ui-pill--md ui-pill--pr-select" :disabled="isEditing">
            <option v-for="course in courseStore.courses" :key="course.id" :value="course.id">
              {{ course.title }}
            </option>
          </select>
        </div>
        <div>
          <label for="title" class="block text-sm font-medium mb-1">{{ t('teacher.assignments.modal.title') }}</label>
          <GlassInput id="title" v-model="form.title" type="text" required />
        </div>
        <div>
          <label for="description" class="block text-sm font-medium mb-1">{{ t('teacher.assignments.modal.description') }}</label>
          <GlassTextarea id="description" v-model="form.description" :rows="3" />
        </div>
        <div class="grid grid-cols-1 gap-3">
          <div v-if="publishMode==='scheduled'" class="glass-thin rounded-lg p-3" v-glass="{ strength: 'thin', interactive: true }">
            <GlassDateTimePicker :label="(t('teacher.assignments.modal.publishAt') as string) || '发布时间'" v-model="form.publishAt" />
          </div>
          <div class="glass-thin rounded-lg p-3" v-glass="{ strength: 'thin', interactive: true }">
            <GlassDateTimePicker :label="(t('teacher.assignments.modal.dueAt') as string) || (t('teacher.assignments.modal.dueDate') as string)" v-model="form.dueDate" />
            <div class="mt-2 flex flex-wrap items-center gap-2 text-xs text-gray-600 dark:text-gray-300">
              <span>{{ t('teacher.assignments.modal.quickHint') || '快捷设置：' }}</span>
              <Button size="xs" variant="outline" @click="quickSetDue(1)">{{ t('teacher.assignments.modal.quickRanges.1d') || '+1 天' }}</Button>
              <Button size="xs" variant="outline" @click="quickSetDue(7)">{{ t('teacher.assignments.modal.quickRanges.7d') || '+7 天' }}</Button>
              <Button size="xs" variant="outline" @click="quickSetDue(14)">{{ t('teacher.assignments.modal.quickRanges.14d') || '+14 天' }}</Button>
              <Button size="xs" variant="outline" @click="quickSetDue(30)">{{ t('teacher.assignments.modal.quickRanges.30d') || '+30 天' }}</Button>
            </div>
          </div>
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
                  <Button as="a" :href="`${baseURL}/files/${f.id}/download`" size="sm" variant="outline">{{ t('teacher.assignments.modal.download') }}</Button>
                  <Button size="sm" variant="danger" @click="confirmDeleteAttachment(f.id)">{{ t('teacher.assignments.modal.delete') }}</Button>
                </div>
              </li>
            </ul>
          </div>
        </div>
        <div class="flex justify-end space-x-3 mt-6">
          <Button variant="secondary" type="button" @click="closeModal">
            {{ t('teacher.assignments.modal.cancel') }}
          </Button>
          <Button variant="primary" type="submit" :disabled="assignmentStore.loading">
            {{ isEditing ? t('teacher.assignments.actions.save') : t('teacher.assignments.modal.create') }}
          </Button>
        </div>
      </form>
    </GlassModal>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue';
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
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import GlassDateTimePicker from '@/components/ui/inputs/GlassDateTimePicker.vue'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import Badge from '@/components/ui/Badge.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import SegmentedPills from '@/components/ui/SegmentedPills.vue'
import { assignmentApi } from '@/api/assignment.api'

const assignmentStore = useAssignmentStore();
const courseStore = useCourseStore();
const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();
const { t } = useI18n()

const showModal = ref(false);
const isEditing = ref(false);
const editingAssignmentId = ref<string | null>(null);
const publishMode = ref<'draft' | 'publish' | 'scheduled'>('draft')
const publishOptions = computed(() => ([
  { label: (t('teacher.assignments.modal.draft') as string) || '保存为草稿', value: 'draft' },
  { label: (t('teacher.assignments.modal.publishNow') as string) || '立即发布', value: 'publish' },
  { label: (t('teacher.assignments.modal.schedule') as string) || '定时发布', value: 'scheduled' },
]))
const originalStatus = ref<string>('draft')
const selectedCourseId = ref<string | null>(null);
const searchText = ref('')
const statusFilter = ref('')
const statusOptions = computed(() => ([
  { label: (t('teacher.courses.status.draft') as string) || '草稿', value: 'draft' },
  { label: (t('teacher.courses.status.published') as string) || '已发布', value: 'published' },
  { label: (t('teacher.assignments.status.pendingReview') as string) || '待批改', value: 'pending_review' },
  { label: (t('teacher.assignments.status.ended') as string) || '已结束', value: 'ended' },
  { label: (t('teacher.courses.status.archived') as string) || '已关闭', value: 'closed' },
  { label: (t('teacher.assignments.modal.schedule') as string) || '定时发布', value: 'scheduled' },
]))
const currentPage = ref(1);
const pageSize = ref(10);
const totalPages = computed(() => {
  const total = Number(assignmentStore.totalAssignments || 0)
  const size = Number(pageSize.value || 10)
  return Math.max(1, Math.ceil(total / (size || 1)))
})

const form = reactive<AssignmentCreationRequest & { id?: string; publishAt?: string }>({
  courseId: '',
  title: '',
  description: '',
  dueDate: '',
  publishAt: '',
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


const statusClass = (status: string) => {
  const base = 'glass-ultraThin rounded-full'
  if (status === 'success') return `${base} bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300`
  if (status === 'warning') return `${base} bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300`
  if (status === 'secondary') return `${base} bg-gray-100 text-gray-800 dark:bg-gray-800/60 dark:text-gray-200`
  if (status === 'info') return `${base} bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300`
  return `${base} bg-gray-100 text-gray-800 dark:bg-gray-800/60 dark:text-gray-200`
}

const statusVariantByDisplay = (key: string) => {
  const k = String(key || '').toLowerCase()
  if (k === 'published') return 'success'
  if (k === 'draft' || k === 'scheduled' || k === 'pending_review') return 'warning'
  if (k === 'ended' || k === 'closed') return 'secondary'
  return 'secondary'
}

function renderStatus(a: any) {
  const k = displayStatusKey(a)
  if (k === 'scheduled') return (t('teacher.assignments.modal.schedule') as string) || '定时发布'
  if (k === 'draft') return (t('teacher.courses.status.draft') as string) || '草稿'
  if (k === 'published') return (t('teacher.courses.status.published') as string) || '已发布'
  if (k === 'pending_review') return (t('teacher.assignments.status.pendingReview') as string) || '待批改'
  if (k === 'ended' || k === 'closed') return (t('teacher.assignments.status.ended') as string) || '已结束'
  return k
}

const statsCache = reactive<Record<string, { gradedCount?: number; ungradedCount?: number; submittedCount?: number }>>({})

function isPastDue(a: any): boolean {
  const dv = a?.dueDate || a?.dueAt
  if (!dv) return false
  const d = new Date(dv)
  if (isNaN(d.getTime())) return false
  return Date.now() > d.getTime()
}

async function ensureStats(a: any) {
  const id = String(a?.id || '')
  if (!id || statsCache[id]) return
  try {
    const res: any = await assignmentApi.getAssignmentSubmissionStats(id)
    const d: any = res?.data || res || {}
    statsCache[id] = {
      gradedCount: Number(d?.gradedCount || 0),
      ungradedCount: Number(d?.ungradedCount || Math.max(0, Number(d?.submittedCount || 0) - Number(d?.gradedCount || 0))),
      submittedCount: Number(d?.submittedCount || 0),
    }
  } catch { /* ignore */ }
}

function displayStatusKey(a: any): 'draft'|'published'|'scheduled'|'closed'|'pending_review'|'ended' {
  const raw = String(a?.status || '').toLowerCase()
  if (raw === 'draft' || raw === 'scheduled' || raw === 'closed') return raw as any
  if (raw === 'published') {
    if (isPastDue(a)) {
      const id = String(a?.id || '')
      const st = statsCache[id]
      if (!st) {
        // 异步加载，先返回待批改占位
        ensureStats(a)
        return 'pending_review'
      }
      return (Number(st?.ungradedCount || 0) > 0) ? 'pending_review' : 'ended'
    }
    return 'published'
  }
  return 'draft'
}

const displayAssignments = computed(() => {
  const list = assignmentStore.assignments || []
  const filter = String(statusFilter.value || '')
  if (!filter) return list
  if (filter === 'pending_review' || filter === 'ended') {
    return list.filter((a: any) => displayStatusKey(a) === filter)
  }
  return list.filter((a: any) => String(a?.status || '').toLowerCase() === filter)
})

function formatMinute(v: any) {
  try { const d = new Date(v); if (isNaN(d.getTime())) return ''; return d.toLocaleString() } catch { return '' }
}

const resetForm = () => {
    Object.assign(form, { courseId: selectedCourseId.value || '', title: '', description: '', dueDate: '', publishAt: '' });
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
  publishMode.value = 'draft'
  showModal.value = true;
};

const openEditModal = (assignment: Assignment) => {
  isEditing.value = true;
  editingAssignmentId.value = assignment.id;
  const toLocalInput = (d: any) => {
    if (!d) return ''
    try {
      const s = String(d)
      // 后端通常返回 "YYYY-MM-DD HH:mm:ss" -> 直接转为 datetime-local
      if (s.includes(' ')) {
        const [date, time] = s.split(' ')
        const [hh='00', mm='00'] = (time || '').split(':')
        return `${date}T${hh}:${mm}`
      }
      // 兜底：Date 可解析时，用本地时间格式化
      const dt = new Date(s)
      if (isNaN(dt.getTime())) return ''
      const pad = (n:number)=>String(n).padStart(2,'0')
      return `${dt.getFullYear()}-${pad(dt.getMonth()+1)}-${pad(dt.getDate())}T${pad(dt.getHours())}:${pad(dt.getMinutes())}`
    } catch { return '' }
  }
  Object.assign(form, {
    courseId: String(assignment.courseId),
    title: assignment.title,
    description: (assignment as any).description || '',
    dueDate: toLocalInput((assignment as any).dueDate),
    publishAt: toLocalInput((assignment as any).publishAt)
  });
  originalStatus.value = String((assignment as any)?.status || 'draft').toLowerCase()
  showModal.value = true;
  // 加载已有附件
  refreshAttachments(assignment.id);
  const stLower = originalStatus.value
  if (stLower === 'published') publishMode.value = 'publish'
  else if (stLower === 'scheduled') publishMode.value = 'scheduled'
  else publishMode.value = 'draft'
};

const closeModal = () => {
  showModal.value = false;
};

const handleSubmit = async () => {
  const toServerTime = (v?: string) => {
    if (!v) return ''
    // datetime-local: YYYY-MM-DDTHH:mm → YYYY-MM-DD HH:mm:00
    return String(v).replace('T', ' ') + ':00'
  }
  // 校验：截止必须晚于发布时间/当前时间
  const baseTime = publishMode.value === 'scheduled' && form.publishAt ? new Date(form.publishAt).getTime() : Date.now()
  const due = form.dueDate ? new Date(form.dueDate).getTime() : 0
  if (!due || due <= baseTime) {
    alert(String(t('teacher.assignments.modal.validation.publishBeforeDue') || '截止时间必须晚于发布时间/当前时间'))
    return
  }
  if (isEditing.value && editingAssignmentId.value) {
    const { title, description, dueDate, publishAt } = form;
    const updateData: AssignmentUpdateRequest = { title, description, dueDate: toServerTime(dueDate) } as any;
    // 确保 courseId 传回后端，避免“课程ID不能为空”
    (updateData as any).courseId = form.courseId;
    // 提供默认满分，避免后端校验失败（若后端允许不更新可移除）
    (updateData as any).maxScore = (updateData as any).maxScore || 100;
    if (publishMode.value === 'scheduled') {
      (updateData as any).status = 'scheduled'
      ;(updateData as any).publishAt = toServerTime(publishAt)
    } else if (publishMode.value === 'draft') {
      (updateData as any).status = 'draft'
    }
    const updated = await assignmentStore.updateAssignment(editingAssignmentId.value, updateData);
    // 编辑时，如有新文件被选择，则上传
    if (updated && assignmentUploader.value) {
      assignmentUploadData.relatedId = editingAssignmentId.value;
      await assignmentUploader.value.uploadFiles?.();
      await refreshAttachments(editingAssignmentId.value);
    }
    // 若选择立即发布且之前不是已发布，则调用发布接口
    if (publishMode.value === 'publish' && originalStatus.value !== 'published') {
      try { await assignmentStore.publishAssignment(String(editingAssignmentId.value)) } catch {}
    }
    // 强制刷新列表以获取后端归一化后的时间
    try {
      if (selectedCourseId.value) {
        await assignmentStore.fetchAssignments({ courseId: selectedCourseId.value, page: currentPage.value, size: pageSize.value })
      } else {
        await assignmentStore.fetchAssignments({ page: currentPage.value, size: pageSize.value })
      }
    } catch {}
  } else {
    const createData: AssignmentCreationRequest = { ...form, dueDate: toServerTime(form.dueDate) } as any;
    // 默认满分 100 分
    (createData as any).maxScore = 100;
    if (publishMode.value === 'scheduled') {
      (createData as any).status = 'scheduled'
      ;(createData as any).publishAt = toServerTime(form.publishAt)
    } else {
      (createData as any).status = 'draft'
    }
    // 先创建作业，但不立即弹出成功提示，待附件上传完成后再关闭弹窗
    const created = await assignmentStore.createAssignment(createData, { suppressNotify: true });
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
      // 若选择立即发布，则在附件上传完成后发布
      if (assignmentId && publishMode.value === 'publish') {
        try { await assignmentStore.publishAssignment(String(assignmentId)) } catch {}
      }
    }
  }
  if (!assignmentStore.loading) {
    // 直接关闭，无弹窗
    closeModal();
  }
};

function quickSetDue(days: number) {
  const base = (publishMode.value === 'scheduled' && form.publishAt) ? new Date(form.publishAt) : new Date()
  const dt = new Date(base.getTime() + days * 24 * 60 * 60 * 1000)
  dt.setSeconds(0, 0)
  const iso = new Date(dt.getTime() - dt.getTimezoneOffset() * 60000).toISOString().slice(0,16)
  form.dueDate = iso
}

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
  const base: any = { page: currentPage.value, size: pageSize.value }
  if (selectedCourseId.value) base.courseId = selectedCourseId.value
  if (searchText.value) base.search = searchText.value
  if (statusFilter.value) base.status = statusFilter.value
  assignmentStore.fetchAssignments(base)
};

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
    handleCourseFilterChange();
  }
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
    handleCourseFilterChange();
  }
};

function goAiGrading() {
  const cid = effectiveCourseId.value
  if (cid) {
    router.push({ name: 'TeacherAIGrading', query: { courseId: String(cid) } })
  } else {
    router.push({ name: 'TeacherAIGrading' })
  }
}

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

// 监听筛选与搜索（含防抖）
watch([selectedCourseId, statusFilter, pageSize], () => {
  currentPage.value = 1
  handleCourseFilterChange()
})

let searchTimer: any = null
watch(searchText, () => {
  currentPage.value = 1
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => handleCourseFilterChange(), 300)
})
</script>
