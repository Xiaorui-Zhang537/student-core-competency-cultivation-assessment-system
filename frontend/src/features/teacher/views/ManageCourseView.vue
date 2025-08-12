<template>
  <div class="min-h-screen p-6">
    <div class="max-w-7xl mx-auto">
      <!-- Header -->
      <div class="mb-8 flex items-center justify-between">
        <div class="flex-1">
          <nav class="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400 mb-2">
            <span>课程管理</span>
          </nav>
          <h1 class="text-3xl font-bold text-gray-900 dark:text-white">课程管理</h1>
          <p class="text-gray-600 dark:text-gray-400">管理您的所有课程</p>
        </div>
        <Button variant="indigo" @click="openCreateModal">
          <PlusIcon class="w-4 h-4 mr-2" />
          创建课程
        </Button>
      </div>

      <!-- Filters -->
      <div class="mb-6 card p-4">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 items-center">
        <!-- 搜索框 with 图标 -->
        <div class="relative">
          <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <magnifying-glass-icon class="h-5 w-5 text-gray-400" />
          </div>
          <input
            v-model="filters.query"
            type="text"
            placeholder="搜索课程标题/描述..."
            @input="applyFilters"
            class="input pl-10"
          />
        </div>
        <!-- 状态分段按钮组 -->
        <div class="flex rounded-lg overflow-hidden border border-gray-300 dark:border-gray-600">
          <button type="button" @click="setStatus('')" :class="segClass('')">全部</button>
          <button type="button" @click="setStatus('DRAFT')" :class="segClass('DRAFT')">草稿</button>
          <button type="button" @click="setStatus('PUBLISHED')" :class="segClass('PUBLISHED')">已发布</button>
          <button type="button" @click="setStatus('ARCHIVED')" :class="segClass('ARCHIVED')">已归档</button>
        </div>
        <div class="flex justify-start md:justify-end">
          <Button variant="outline" @click="clearFilters">
            <XMarkIcon class="w-4 h-4 mr-2" />
            清除筛选
          </Button>
        </div>
      </div>
      </div>

      <!-- Course List -->
      <div v-if="courseStore.loading" class="text-center py-12">
        <p>正在加载课程...</p>
      </div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="course in courseStore.courses"
          :key="course.id"
          class="relative card overflow-hidden cursor-pointer transition-all duration-300 hover:shadow-md hover:-translate-y-0.5"
          @click="navigateToCourse(course)"
        >
          <div class="h-40 bg-gray-100 dark:bg-gray-700 overflow-hidden relative">
            <img
              v-if="course.coverImage && getCoverSrc(course)"
              :src="getCoverSrc(course)"
              alt="课程封面"
              class="w-full h-full object-cover"
              @error="onCardCoverError(course)"
            />
            <!-- 状态徽标浮层 -->
            <span
              class="absolute top-3 left-3 text-xs px-2 py-0.5 rounded bg-white/90 dark:bg-gray-800/90 border border-gray-200 dark:border-gray-700"
              :class="statusBadgeClass(course.status)"
            >
              {{ statusText(course.status) }}
            </span>
          </div>
          <div class="p-4">
            <h3 class="font-bold text-lg line-clamp-1">{{ course.title }}</h3>
            <p class="text-xs text-gray-500 mb-2">{{ course.category }}</p>
            <p class="text-sm text-gray-600 dark:text-gray-300 line-clamp-2 min-h-[2.5rem]">{{ course.description }}</p>
            <div class="mt-4 flex justify-between items-center">
              <div class="text-xs text-gray-500">
                <span>标签：</span>
                <span v-if="getTagsText(course)" class="text-gray-700 dark:text-gray-200">{{ getTagsText(course) }}</span>
                <span v-else class="text-gray-400">无</span>
              </div>
              <div class="flex items-center">
                <Button size="sm" variant="outline" class="mr-2" @click.stop="openEditModal(course)">
                  <PencilSquareIcon class="w-4 h-4 mr-1" />
                  编辑
                </Button>
                <Button size="sm" variant="outline" @click.stop="handleDeleteCourse(String(course.id))">
                  <TrashIcon class="w-4 h-4 mr-1" />
                  删除
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-if="!courseStore.loading && courseStore.courses.length === 0" class="text-center py-12 card">
        <h3 class="text-lg font-medium">暂无课程</h3>
        <p class="text-gray-500 mb-4">开始创建您的第一个课程吧！</p>
        <Button variant="indigo" @click="openCreateModal">
          <PlusIcon class="w-4 h-4 mr-2" />
          创建课程
        </Button>
      </div>

      <!-- Create/Edit Modal -->
      <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-xl w-full max-w-2xl max-h-[85vh] overflow-y-auto">
        <div class="p-6">
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
            <label for="content" class="block text-sm font-medium mb-1">课程内容</label>
            <textarea id="content" v-model="form.content" rows="6" class="input" placeholder="可填写课程大纲、教学安排等"></textarea>
          </div>
          <div>
            <label for="category" class="block text-sm font-medium mb-1">分类</label>
            <input id="category" v-model="form.category" type="text" required class="input" />
          </div>
          <div>
            <label for="tags" class="block text-sm font-medium mb-1">标签 (逗号分隔)</label>
            <input id="tags" v-model="tagsInput" type="text" class="input" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">课程封面</label>
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
            <p v-if="form.coverImage" class="text-xs text-gray-500 mt-2">已选择封面（文件ID：{{ form.coverImage }}）</p>
          </div>
          <div class="flex justify-end space-x-3 mt-6">
            <Button type="button" variant="outline" @click="closeModal">取消</Button>
            <Button type="submit" :disabled="courseStore.loading" variant="indigo">
              {{ isEditing ? '保存更改' : '创建课程' }}
            </Button>
          </div>
        </form>
        </div>
      </div>
    </div>
    
    <!-- root wrapper close -->
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useCourseStore } from '@/stores/course';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';
import type { Course, CourseCreationRequest, CourseUpdateRequest } from '@/types/course';
import { debounce } from 'lodash-es';
import FileUpload from '@/components/forms/FileUpload.vue';
import apiClient, { baseURL } from '@/api/config';
import Button from '@/components/ui/Button.vue'
import { MagnifyingGlassIcon, PlusIcon, XMarkIcon, PencilSquareIcon, TrashIcon } from '@heroicons/vue/24/outline'

const courseStore = useCourseStore();
const authStore = useAuthStore();
const router = useRouter();

const showModal = ref(false);
const isEditing = ref(false);
const editingCourseId = ref<string | null>(null);

const form = reactive<Omit<CourseCreationRequest, 'tags'> & { id?: string; tags: string[]; coverImage?: string; content?: string }>({
  title: '',
  description: '',
  content: '',
  category: '',
  tags: [],
  coverImage: ''
});
const tagsInput = ref('');
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

const statusClass = (status: string) => {
    return {
        'bg-green-100 text-green-800': status === 'PUBLISHED',
        'bg-yellow-100 text-yellow-800': status === 'DRAFT',
        'bg-gray-100 text-gray-800': status === 'ARCHIVED',
    }
}

const statusText = (status: string) => {
  const map: Record<string, string> = { DRAFT: '草稿', PUBLISHED: '已发布', ARCHIVED: '已归档' }
  return map[status] || status || '未知'
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

const segClass = (val: string) => {
  const isActive = filters.status === val
  return [
    'px-3 py-2 text-sm transition-colors',
    isActive
      ? 'bg-primary-600 text-white'
      : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700'
  ]
}

const getTagsText = (course: any): string => {
  const t = (course as any)?.tags
  if (Array.isArray(t)) return t.join(', ')
  if (typeof t === 'string') return t
  return ''
}

const navigateToCourse = (course: Course) => {
    if (course && course.id) {
        router.push({ name: 'TeacherCourseDetail', params: { id: course.id } });
    }
};

const openCreateModal = () => {
  isEditing.value = false;
  editingCourseId.value = null;
  Object.assign(form, { title: '', description: '', content: '', category: '', tags: [], coverImage: '' });
  tagsInput.value = '';
  showModal.value = true;
};

const openEditModal = (course: Course) => {
  isEditing.value = true;
  editingCourseId.value = String(course.id);
  Object.assign(form, { ...course });
  tagsInput.value = (Array.isArray(course.tags) ? course.tags : []).join(', ');
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
      tags: tagsInput.value, // Send tags as a string
      teacherId: String(teacherId)
  };

  if (isEditing.value && editingCourseId.value) {
    const { id, ...updateData } = payload;
    await courseStore.updateCourse(editingCourseId.value, updateData as CourseUpdateRequest);
  } else {
    const { id, ...createData } = payload;
    await courseStore.createCourse(createData as CourseCreationRequest);
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

const onCoverUploadError = (message: string) => {
  // 简单打印错误，表单仍可继续提交
  console.error('封面上传失败:', message);
};

const handleDeleteCourse = async (id: string) => {
  if (confirm('您确定要删除这门课程吗？此操作无法撤销。')) {
    await courseStore.deleteCourse(id);
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
    });
};

const applyFilters = debounce(fetchTeacherCourses, 300);

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
});
</script>