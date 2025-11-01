<template>
  <div class="min-h-screen p-6">
    <div class="max-w-7xl mx-auto">
      <!-- Header -->
      <PageHeader :title="t('teacher.courses.title')" :subtitle="t('teacher.courses.subtitle')">
        <template #actions>
          <Button variant="primary" @click="openCreateModal">
            <PlusIcon class="w-4 h-4 mr-2" />
            {{ t('teacher.courses.actions.create') }}
          </Button>
        </template>
      </PageHeader>

      <!-- Filters -->
      <FilterBar tint="info" align="center" :dense="false" class="mb-6 h-19">
        <template #left>
          <div class="flex items-center gap-3 flex-wrap">
            <div class="w-auto flex items-center gap-2">
              <span class="text-xs font-medium leading-tight text-gray-700 dark:text-gray-300">{{ t('teacher.courses.filters.sortDifficultyLabel') || '难度排序' }}</span>
              <div class="w-48">
                <GlassPopoverSelect
                  v-model="difficultyOrder"
                  :options="difficultyOrderOptions"
                  size="sm"
                />
              </div>
            </div>
            <SegmentedPills
              :model-value="filters.status"
              :options="statusOptions"
              size="sm"
              @update:modelValue="(v:any) => setStatus(v)"
            />
          </div>
        </template>
        <template #right>
          <div class="relative w-56">
            <GlassSearchInput
              v-model="filters.query"
              :placeholder="t('teacher.courses.filters.searchPlaceholder') as string"
              size="sm"
              @update:modelValue="applyFilters()"
            />
          </div>
        </template>
      </FilterBar>

      <!-- Course List -->
      <div v-if="courseStore.loading" class="text-center py-12">
        <p>{{ t('teacher.courses.loading') }}</p>
      </div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mt-4">
        <Card
          v-for="course in displayedCourses"
          :key="course.id"
          class="relative overflow-hidden cursor-pointer transition-all duration-300 hover:shadow-md hover:-translate-y-0.5"
          tint="primary"
          padding="none"
          @click="navigateToCourse(course)"
        >
          <div class="h-40 bg-gray-100 dark:bg-gray-700 overflow-hidden relative rounded-2xl">
            <img
              v-if="course.coverImage && getCoverSrc(course)"
              :src="getCoverSrc(course)"
              :alt="t('teacher.courses.card.coverAlt')"
              class="w-full h-full object-cover rounded-2xl"
              @error="onCardCoverError(course)"
            />
            <div v-else class="w-full h-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center">
              <span class="text-4xl font-bold text-white">{{ (course.title || '').charAt(0) }}</span>
            </div>
            <!-- 状态徽标浮层（玻璃 Badge） -->
            <Badge
              :variant="statusVariant(course.status)"
              size="sm"
              class="absolute top-3 left-3"
            >
              {{ statusText(course.status) }}
            </Badge>
          </div>
          <div class="p-4">
            <h3 class="font-bold text-lg line-clamp-1">{{ course.title }}</h3>
            <p class="text-xs text-gray-500 mb-2">{{ localizeCategory(course.category) }}</p>
            <p class="text-sm text-gray-600 dark:text-gray-300 line-clamp-2 min-h-[2.5rem] whitespace-pre-line">{{ course.description }}</p>
            <div class="mt-4 flex justify-between items-center">
              <div class="flex flex-wrap gap-1.5 items-center min-h-[1.5rem]">
                <Badge v-for="tag in toTagList(course)" :key="String(tag)" size="sm" :variant="tagVariant(String(tag))">
                  {{ localizeTag(String(tag)) }}
                </Badge>
                <span v-if="toTagList(course).length===0" class="text-xs text-gray-400">{{ t('teacher.courses.card.none') }}</span>
              </div>
              <div class="flex items-center">
                <Button size="sm" variant="secondary" class="mr-2" icon="edit" @click.stop="openEditModal(course)">{{ t('teacher.courses.actions.edit') }}</Button>
                <Button size="sm" variant="danger" icon="delete" @click.stop="handleDeleteCourse(String(course.id))">{{ t('teacher.courses.actions.delete') }}</Button>
              </div>
            </div>
          </div>
        </Card>
      </div>
      <Card v-if="!courseStore.loading && courseStore.courses.length === 0" class="text-center py-12" tint="info">
        <h3 class="text-lg font-medium">{{ t('teacher.courses.empty.title') }}</h3>
        <p class="text-gray-500 mb-4">{{ t('teacher.courses.empty.description') }}</p>
        <Button variant="primary" @click="openCreateModal">
          <PlusIcon class="w-4 h-4 mr-2" />
          {{ t('teacher.courses.actions.create') }}
        </Button>
      </Card>

      <!-- Create/Edit Modal (GlassModal) -->
      <GlassModal v-if="showModal" :title="(isEditing ? t('teacher.courses.modal.editTitle') : t('teacher.courses.modal.createTitle')) as string" size="md" heightVariant="tall" solidBody @close="closeModal">
        <form id="courseForm" @submit.prevent="handleSubmit" class="space-y-4">
          <div>
            <label for="title" class="block text-sm font-medium mb-1">{{ t('teacher.courses.modal.title') }}</label>
            <GlassInput id="title" v-model="form.title" type="text" />
          </div>
          <div>
            <label for="description" class="block text-sm font-medium mb-1">{{ t('teacher.courses.modal.description') }}</label>
            <GlassTextarea id="description" v-model="form.description" :rows="3" />
          </div>
          <div>
            <label for="content" class="block text-sm font-medium mb-1">{{ t('teacher.courses.modal.content') }}</label>
            <GlassTextarea id="content" v-model="form.content" :rows="6" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">{{ t('teacher.courses.modal.status') || '状态' }}</label>
            <div class="w-full md:w-64">
              <SegmentedPills
                :model-value="form.status"
                :options="statusPillOptions"
                size="sm"
                variant="primary"
                @update:modelValue="(v:any) => form.status = v as any"
              />
            </div>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label for="category" class="block text-sm font-medium mb-1">{{ t('teacher.courses.modal.category') }}</label>
              <div class="w-full md:w-64">
                <GlassPopoverSelect :options="categoryOptions" :model-value="form.category" size="sm" @update:modelValue="(v:any)=> form.category = String(v)" />
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">{{ t('teacher.courses.difficulty') || '难度' }}</label>
              <div class="w-full md:w-64">
              <SegmentedPills :model-value="(form as any).difficulty || 'beginner'" :options="difficultyOptions" size="sm" variant="accent" @update:modelValue="(v:any)=> (form as any).difficulty = String(v)" />
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">{{ t('teacher.courses.duration') || '预计时长(小时)' }}</label>
              <div class="w-full md:w-64">
                <GlassInput v-model="durationInput" type="number" />
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">{{ t('teacher.courses.maxStudents') || '最大人数' }}</label>
              <div class="w-full md:w-64">
                <GlassInput v-model="maxStudentsInput" type="number" />
              </div>
            </div>
            <div>
              <GlassDateTimePicker v-model="startDate" :label="(t('teacher.courses.startDate') as string) || '开课时间'" date-only />
            </div>
            <div>
              <GlassDateTimePicker v-model="endDate" :label="(t('teacher.courses.endDate') as string) || '结课时间'" date-only />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">{{ t('teacher.courses.modal.tags') || '标签' }}</label>
            <GlassTagsInput v-model="selectedTagsStr" :placeholder="(t('teacher.courses.tagsPlaceholder') as string) || '输入后回车添加标签'" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">{{ t('teacher.courses.modal.cover') }}</label>
            <file-upload
              ref="coverUploader"
              :accept="'image/*'"
              :multiple="false"
              :autoUpload="true"
              :upload-url="`${baseURL}/files/upload`"
              :upload-headers="uploadHeaders"
              :upload-data="{ purpose: 'course_cover' }"
              @upload-success="onCoverUploaded"
              @upload-error="onCoverUploadError"
            />
            <p v-if="form.coverImage" class="text-xs text-gray-500 mt-2">{{ t('teacher.courses.modal.coverPicked', { id: form.coverImage }) }}</p>
          </div>
        </form>
        <template #footer>
          <Button type="button" variant="secondary" @click="closeModal">{{ t('teacher.courses.modal.cancel') }}</Button>
          <Button type="submit" form="courseForm" :disabled="courseStore.loading" variant="primary">
            {{ isEditing ? t('teacher.courses.actions.save') : t('teacher.courses.actions.create') }}
          </Button>
        </template>
      </GlassModal>
    
    <!-- root wrapper close -->
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { useCourseStore } from '@/stores/course';
import { useAuthStore } from '@/stores/auth';
import { useRouter, useRoute } from 'vue-router';
import type { Course, CourseCreationRequest, CourseUpdateRequest } from '@/types/course';
// 轻量去依赖：局部实现 debounce，避免引入 lodash-es
const debounce = (fn: (...args: any[]) => void, delay = 300) => {
  let timer: any = null
  return (...args: any[]) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn(...args), delay)
  }
}
import FileUpload from '@/components/forms/FileUpload.vue';
import apiClient, { baseURL } from '@/api/config';
import Button from '@/components/ui/Button.vue'
import { PlusIcon, XMarkIcon } from '@heroicons/vue/24/outline'
// @ts-ignore - vue-i18n runtime is available at build time; type may be resolved via shim
import { useI18n } from 'vue-i18n'
import PageHeader from '@/components/ui/PageHeader.vue'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import GlassMultiSelect from '@/components/ui/filters/GlassMultiSelect.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import FilterBar from '@/components/ui/filters/FilterBar.vue'
import Badge from '@/components/ui/Badge.vue'
import SegmentedPills from '@/components/ui/SegmentedPills.vue'
import GlassTagsInput from '@/components/ui/inputs/GlassTagsInput.vue'
import GlassDateTimePicker from '@/components/ui/inputs/GlassDateTimePicker.vue'
import Card from '@/components/ui/Card.vue'

const courseStore = useCourseStore();
const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();
const { t } = useI18n()

const showModal = ref(false);
const isEditing = ref(false);
const editingCourseId = ref<string | null>(null);

const form = reactive<Omit<CourseCreationRequest, 'tags'> & { id?: string; tags: string[]; coverImage?: string; content?: string; status?: 'draft'|'published'|'archived' }>({
  title: '',
  description: '',
  content: '',
  category: '',
  tags: [],
  coverImage: '',
  status: 'draft'
});
const selectedTagsStr = ref<string[]>([])
const startDate = ref<string>('')
const endDate = ref<string>('')
const durationInput = ref<string>('')
const maxStudentsInput = ref<string>('')
const difficultyOptions = computed(() => [
  { label: t('teacher.courses.tagOptions.beginner') as string || '入门', value: 'beginner' },
  { label: t('teacher.courses.tagOptions.intermediate') as string || '进阶', value: 'intermediate' },
  { label: t('teacher.courses.tagOptions.advanced') as string || '高级', value: 'advanced' }
])
const categoryOptions = computed(() => [
  { label: t('teacher.courseEdit.form.categoryOptions.programming') as string || '编程开发', value: 'programming' },
  { label: t('teacher.courseEdit.form.categoryOptions.design') as string || '设计创意', value: 'design' },
  { label: t('teacher.courseEdit.form.categoryOptions.business') as string || '商业管理', value: 'business' },
  { label: t('teacher.courseEdit.form.categoryOptions.marketing') as string || '市场营销', value: 'marketing' },
  { label: t('teacher.courseEdit.form.categoryOptions.language') as string || '语言学习', value: 'language' },
  { label: t('teacher.courseEdit.form.categoryOptions.science') as string || '科学技术', value: 'science' },
  { label: t('teacher.courseEdit.form.categoryOptions.art') as string || '艺术人文', value: 'art' },
  { label: t('teacher.courses.category.liberalArts') as string || '文科', value: 'liberal-arts' },
  { label: t('teacher.courses.category.engineering') as string || '工科', value: 'engineering' },
  { label: t('teacher.courses.category.science') as string || '理科', value: 'science-college' },
  { label: t('teacher.courses.category.other') as string || '其他', value: 'other' },
])
const coverUploader = ref();
const uploadHeaders = {
  Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : ''
};

// 封面预览：支持外链URL与受保护预览（以blob方式加载避免<img>不带Authorization）
const coverSrcMap = reactive<Record<string, string>>({});
const isHttpUrl = (v?: string) => !!v && /^(http|https):\/\//i.test(v);
const getCoverSrc = (course: Course) => {
  const v: any = (course as any).coverImage;
  if (!v) return '';
  // 避免加载外网被拦截导致报错：对外链封面直接不加载，使用灰底占位
  if (isHttpUrl(v)) return '';
  const key = String(v);
  if (coverSrcMap[key]) return coverSrcMap[key];
  // 延迟加载blob
  apiClient
    .get(`/files/${encodeURIComponent(key)}/preview`, { responseType: 'blob' })
    .then((res) => {
      const url = URL.createObjectURL(res as any);
      coverSrcMap[key] = url;
    })
    .catch(() => {
      // ignore
    });
  return '';
};

const onCardCoverError = (_course: Course) => {
  // 忽略错误，显示灰底占位
};

const filters = reactive({
    query: '',
    status: '',
});
const difficultyOrder = ref<'none'|'asc'|'desc'>('none')
const difficultyOrderOptions = computed(() => ([
  { label: (t('student.courses.sortNone') as string) || '默认', value: 'none' },
  { label: (t('student.courses.sortDifficultyAsc') as string) || '难度从低到高', value: 'asc' },
  { label: (t('student.courses.sortDifficultyDesc') as string) || '难度从高到低', value: 'desc' }
]))
const statusOptions = computed(() => [
  { label: t('teacher.courses.filters.status.all') as string, value: '' },
  { label: t('teacher.courses.filters.status.draft') as string, value: 'DRAFT' },
  { label: t('teacher.courses.filters.status.published') as string, value: 'PUBLISHED' },
  { label: t('teacher.courses.filters.status.archived') as string, value: 'ARCHIVED' },
])

const statusClass = (status: string) => {
    return {
        'bg-green-100 text-green-800': status === 'PUBLISHED',
        'bg-yellow-100 text-yellow-800': status === 'DRAFT',
        'bg-gray-100 text-gray-800': status === 'ARCHIVED',
    }
}

const statusText = (status: string) => {
  const normalized = (status || '').toUpperCase()
  const map: Record<string, string> = { DRAFT: t('teacher.courses.status.draft'), PUBLISHED: t('teacher.courses.status.published'), ARCHIVED: t('teacher.courses.status.archived') }
  return map[normalized] || status || t('teacher.courses.status.unknown')
}

const statusBadgeClass = (status: string) => {
  const map: Record<string, string> = {
    DRAFT: 'text-yellow-700 dark:text-yellow-300',
    PUBLISHED: 'text-green-700 dark:text-green-300',
    ARCHIVED: 'text-gray-600 dark:text-gray-300'
  }
  return map[status] || 'text-gray-600'
}

const setStatus = (val: string) => {
  filters.status = val
  applyFilters()
}

function segGlass(val: string) {
  const isActive = filters.status === val
  return { strength: isActive ? 'regular' : 'ultraThin', interactive: false }
}

const segClass = (val: string) => {
  const isActive = filters.status === val
  return [
    'px-3 py-2 text-sm transition-colors focus:outline-none',
    isActive
      ? 'text-white bg-primary-500/30'
      : 'text-gray-800 dark:text-gray-200 hover:bg-white/10 dark:hover:bg-white/10'
  ]
}

const getTagsText = (course: any): string => {
  const v = (course as any)?.tags
  if (Array.isArray(v) && v.length) return v.join(', ')
  if (typeof v === 'string' && v.trim().length) return v
  return ''
}

function toTagList(course: any): string[] {
  const v = course?.tags
  if (Array.isArray(v)) return v
  if (typeof v === 'string' && v.trim().length) return v.split(',').map((s: string) => s.trim()).filter(Boolean)
  return []
}

function localizeTag(tag: string): string {
  const key = tag.toLowerCase()
  const dict: Record<string, string> = {
    beginner: t('teacher.courses.tagOptions.beginner') as string,
    intermediate: t('teacher.courses.tagOptions.intermediate') as string,
    advanced: t('teacher.courses.tagOptions.advanced') as string,
    exam: t('teacher.courses.tagOptions.examPrep') as string,
    examprep: t('teacher.courses.tagOptions.examPrep') as string,
    practice: t('teacher.courses.tagOptions.practice') as string
  }
  return dict[key] || tag
}

function localizeCategory(cat?: string): string {
  const key = String(cat || '').toLowerCase()
  const map: Record<string, string> = {
    programming: t('teacher.courseEdit.form.categoryOptions.programming') as string,
    design: t('teacher.courseEdit.form.categoryOptions.design') as string,
    business: t('teacher.courseEdit.form.categoryOptions.business') as string,
    marketing: t('teacher.courseEdit.form.categoryOptions.marketing') as string,
    language: t('teacher.courseEdit.form.categoryOptions.language') as string,
    science: t('teacher.courseEdit.form.categoryOptions.science') as string,
    art: t('teacher.courseEdit.form.categoryOptions.art') as string,
    'liberal-arts': t('teacher.courses.category.liberalArts') as string,
    engineering: t('teacher.courses.category.engineering') as string,
    'science-college': t('teacher.courses.category.science') as string,
    other: t('teacher.courses.category.other') as string
  }
  return map[key] || cat || ''
}

function statusVariant(status: string): 'success' | 'warning' | 'secondary' {
  const s = (status || '').toUpperCase()
  if (s === 'PUBLISHED') return 'success'
  if (s === 'DRAFT') return 'warning'
  return 'secondary'
}

function tagVariant(tag: string): 'info' | 'primary' | 'danger' | 'warning' | 'success' | 'secondary' {
  const k = tag.toLowerCase()
  // Known mappings keep fixed semantic colors
  if (k === 'beginner') return 'info'
  if (k === 'intermediate') return 'primary'
  if (k === 'advanced') return 'danger'
  if (k === 'exam' || k === 'examprep') return 'warning'
  if (k === 'practice') return 'success'
  // Custom tags: stable pseudo-random color from allowed variants (exclude secondary to ensure colored)
  const palette: Array<'info'|'primary'|'danger'|'warning'|'success'> = ['info','primary','danger','warning','success']
  let hash = 0
  for (let i = 0; i < k.length; i++) {
    hash = ((hash << 5) - hash) + k.charCodeAt(i)
    hash |= 0
  }
  const idx = Math.abs(hash) % palette.length
  return palette[idx]
}

function normalizeStatus(s: any): 'draft'|'published'|'archived' {
  const v = String(s || '').toLowerCase()
  if (v === 'published') return 'published'
  if (v === 'archived') return 'archived'
  return 'draft'
}

const statusPillOptions = computed(() => [
  { label: t('teacher.courses.status.draft') as string, value: 'draft' },
  { label: t('teacher.courses.status.published') as string, value: 'published' },
  { label: t('teacher.courses.status.archived') as string, value: 'archived' }
])

const navigateToCourse = (course: Course) => {
    if (course && course.id) {
        router.push({ name: 'TeacherCourseDetail', params: { id: course.id } });
    }
};

const openCreateModal = () => {
  isEditing.value = false;
  editingCourseId.value = null;
  Object.assign(form, { title: '', description: '', content: '', category: '', tags: [], coverImage: '', status: 'draft', difficulty: 'beginner', duration: undefined as any, maxStudents: undefined as any, startDate: '', endDate: '' });
  selectedTagsStr.value = [];
  startDate.value = ''
  endDate.value = ''
  durationInput.value = ''
  maxStudentsInput.value = ''
  showModal.value = true;
};

const openEditModal = (course: Course) => {
  isEditing.value = true;
  editingCourseId.value = String(course.id);
  Object.assign(form, { ...course, status: normalizeStatus((course as any).status) });
  selectedTagsStr.value = Array.isArray(course.tags)
    ? (course.tags as string[])
    : (typeof (course as any).tags === 'string' && (course as any).tags.trim().length
        ? String((course as any).tags).split(',').map((s: string) => s.trim()).filter(Boolean)
        : []);
  startDate.value = (course as any).startDate || ''
  endDate.value = (course as any).endDate || ''
  durationInput.value = String((course as any).duration ?? '')
  maxStudentsInput.value = String((course as any).maxStudents ?? '')
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
};

const handleSubmit = async () => {
  const teacherId = authStore.user?.id;
  if (!teacherId) {
      console.error("User not authenticated");
      return;
  }
  
  // Prepare payload for the API
  const payload = {
      ...form,
      // 后端 Course.tags 为 String，按逗号拼接提交
      tags: (selectedTagsStr.value || []).join(','),
      startDate: startDate.value || undefined,
      endDate: endDate.value || undefined,
      duration: durationInput.value ? Number(durationInput.value) : undefined,
      maxStudents: maxStudentsInput.value ? Number(maxStudentsInput.value) : undefined,
      teacherId: String(teacherId)
  };

  if (isEditing.value && editingCourseId.value) {
    const { id, ...updateData } = payload;
    await courseStore.updateCourse(editingCourseId.value, updateData as CourseUpdateRequest);
    // 若目标状态为已发布，调用发布接口确保后端切换
    if (String(form.status).toLowerCase() === 'published') {
      try { await courseStore.publishCourse(String(editingCourseId.value) as any) } catch {}
    }
    {
      const { useUIStore } = await import('@/stores/ui')
      const ui = useUIStore()
      ui.showNotification({ type: 'success', title: t('teacher.courses.notify.updatedTitle') as string, message: t('teacher.courses.notify.updatedMsg') as string })
    }
  } else {
    const { id, ...createData } = payload;
    const created: any = await courseStore.createCourse(createData as CourseCreationRequest);
    // 立即发布：创建成功后调用发布接口
    try {
      const newId = (created?.id || created?.data?.id || (created && created.data && created.data.id)) as any
      if (newId && String(form.status).toLowerCase() === 'published') {
        await courseStore.publishCourse(String(newId) as any)
      }
    } catch {}
    {
      const { useUIStore } = await import('@/stores/ui')
      const ui = useUIStore()
      ui.showNotification({ type: 'success', title: t('teacher.courses.notify.createdTitle') as string, message: t('teacher.courses.notify.createdMsg') as string })
    }
  }
  
  if (!courseStore.loading) {
    closeModal();
    fetchTeacherCourses();
  }
};

const onCoverUploaded = (response: any) => {
  // axios 拦截器可能已解包，兼容 { id } 或 { data: { id } }
  const data = response?.data ?? response;
  if (data && (data.id || data.fileId)) {
    form.coverImage = String(data.id ?? data.fileId);
  }
};

const onCoverUploadError = async (message: string) => {
  // 简单打印错误，表单仍可继续提交
  console.error('封面上传失败:', message);
  const { useUIStore } = await import('@/stores/ui')
  const ui = useUIStore()
  ui.showNotification({ type: 'error', title: t('app.notifications.error.title') as string, message: message || '' })
};

const handleDeleteCourse = async (id: string) => {
  if (confirm(t('teacher.courses.confirm.deleteCourse'))) {
    await courseStore.deleteCourse(id);
    {
      const { useUIStore } = await import('@/stores/ui')
      const ui = useUIStore()
      ui.showNotification({ type: 'success', title: t('teacher.courses.notify.deletedTitle') as string, message: t('teacher.courses.notify.deletedMsg') as string })
    }
    fetchTeacherCourses();
  }
};

const fetchTeacherCourses = () => {
    const teacherId = authStore.user?.id;
    if (!teacherId) return;
    courseStore.fetchCourses({
      page: 1,
      size: 10,
      query: filters.query,
      status: filters.status,
      teacherId: String(teacherId),
      // 前端难度排序仅用于展示顺序（本地排序），不传给后端
    });
};

const applyFilters = debounce(fetchTeacherCourses, 300);

// 展示用排序（按难度）
const displayedCourses = computed(() => {
  const list = Array.isArray(courseStore.courses) ? [...courseStore.courses] : []
  if (difficultyOrder.value === 'none') return list
  const rank = (d?: string) => {
    const v = String(d || '').toLowerCase()
    if (v.startsWith('beginner') || v.includes('入门') || v.includes('初级')) return 1
    if (v.startsWith('intermediate') || v.includes('进阶') || v.includes('中级')) return 2
    if (v.startsWith('advanced') || v.includes('高级')) return 3
    return 0
  }
  list.sort((a: any, b: any) => {
    const ra = rank((a.difficulty || (a as any).level))
    const rb = rank((b.difficulty || (b as any).level))
    return difficultyOrder.value === 'asc' ? (ra - rb) : (rb - ra)
  })
  return list
})

const clearFilters = () => {
    filters.query = '';
    filters.status = '';
    applyFilters();
}

onMounted(async () => {
    if (!authStore.user) {
        await authStore.fetchUser();
    }
    fetchTeacherCourses();
    // 支持通过路由参数触发创建弹窗：?create=1 / ?action=create
    try {
      const q: any = route.query || {}
      const flag = String(q.create || q.action || '').toLowerCase()
      if (flag === '1' || flag === 'true' || flag === 'create' || flag === 'createcourse') {
        openCreateModal()
        const newQuery = { ...q }
        delete newQuery.create
        delete newQuery.action
        router.replace({ query: newQuery })
      }
    } catch {}
});
</script>