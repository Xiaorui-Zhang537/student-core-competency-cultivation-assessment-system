<template>
  <div class="p-6">
    <div v-if="courseStore.loading" class="text-center py-12">
      <p>{{ t('teacher.courseDetail.loading') }}</p>
    </div>
    <div v-else-if="course" class="space-y-6">
      <!-- 面包屑导航（与学生管理界面风格一致） -->
      <nav class="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400 mb-2">
        <span class="hover:text-gray-700 dark:hover:text-gray-200 cursor-pointer" @click="router.push('/teacher/courses')">
          {{ t('teacher.courseDetail.breadcrumb.courses') }}
        </span>
        <ChevronRightIcon class="w-4 h-4" />
        <span>{{ course.title }}</span>
      </nav>

      <div class="flex items-start justify-between">
        <div>
          <h1 class="text-3xl font-bold">{{ course.title }}</h1>
          <p class="text-gray-500">{{ course.category }}</p>
        </div>
        <div class="flex items-center gap-2">
          <Button variant="teal" @click="router.push(`/teacher/courses/${course.id}/students`)">
            <UserGroupIcon class="w-4 h-4 mr-2" />
            {{ t('teacher.courseDetail.buttons.students') }}
          </Button>
          <Button variant="purple" @click="router.push({ name: 'TeacherAssignments' })">
            <ClipboardDocumentListIcon class="w-4 h-4 mr-2" />
            {{ t('teacher.courseDetail.buttons.assignments') }}
          </Button>
          <Button variant="primary" @click="router.push(`/teacher/analytics?courseId=${course.id}`)">
            <PresentationChartBarIcon class="w-4 h-4 mr-2" />
            {{ t('teacher.courseDetail.buttons.analytics') }}
          </Button>
        </div>
      </div>
      <div class="w-full h-56 bg-gray-200 rounded overflow-hidden" v-if="course.coverImage">
        <img v-if="coverSrc" :src="coverSrc" :alt="t('teacher.courses.card.coverAlt')" class="w-full h-full object-cover" @error="coverSrc='';" />
      </div>
      <div class="card p-6">
        <h2 class="text-xl font-semibold mb-4">{{ t('teacher.courseDetail.sections.description') }}</h2>
        <p>{{ course.description }}</p>
      </div>
      <div class="card p-6">
        <h2 class="text-xl font-semibold mb-4">{{ t('teacher.courseDetail.sections.content') }}</h2>
        <div v-html="course.content"></div>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6 items-stretch">
        <div class="card p-6 order-2 sm:order-1 h-full">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-xl font-semibold leading-tight break-all">{{ t('teacher.courseDetail.sections.materials') }}</h2>
            <FileUpload
              ref="materialUploader"
              :multiple="true"
              :autoUpload="true"
              :accept="'.pdf,.doc,.docx,.ppt,.pptx,.zip'"
              :upload-url="`${baseURL}/files/upload`"
              :upload-headers="uploadHeaders"
              :upload-data="{ purpose: 'course_material', relatedId: course.id }"
              @upload-success="refreshMaterials"
              :compact="true"
              :dense="true"
            />
          </div>
          <div class="mb-3">
            <input v-model="materialQuery" :placeholder="t('teacher.courseDetail.sections.searchMaterial')" class="input input-sm w-full" />
          </div>
          <ul class="divide-y divide-gray-200">
            <li v-for="f in paginatedMaterials" :key="f.id" class="py-2 flex items-center justify-between">
              <div class="flex items-center min-w-0 mr-3 gap-2">
                <component :is="fileIcon(f)" class="w-5 h-5 text-gray-500" />
                <div class="min-w-0">
                  <div class="text-sm truncate">{{ f.originalName || f.fileName || t('teacher.courseDetail.sections.file', { id: f.id }) }}</div>
                  <div class="text-xs text-gray-500">{{ t('teacher.courseDetail.sections.size', { size: formatSize(f.fileSize) }) }}</div>
                </div>
              </div>
              <div class="flex items-center gap-2">
                <Button size="sm" variant="outline" as="a" :href="`${baseURL}/files/${f.id}/download`">{{ t('teacher.courseDetail.sections.download') }}</Button>
                <Button size="sm" variant="danger" @click="confirmDelete(f.id, 'material')">{{ t('teacher.courseDetail.sections.delete') }}</Button>
              </div>
            </li>
            <li v-if="!materials.length" class="py-6 text-center text-sm text-gray-500">{{ t('teacher.courseDetail.sections.noMaterials') }}</li>
          </ul>
          <div v-if="materials.length > pageSize" class="mt-3 flex items-center justify-end gap-2">
            <Button size="sm" variant="outline" :disabled="materialsPage===1" @click="materialsPage--">{{ t('teacher.courseDetail.sections.prev') }}</Button>
            <span class="text-xs text-gray-500">{{ materialsPage }} / {{ materialsTotalPages }}</span>
            <Button size="sm" variant="outline" :disabled="materialsPage===materialsTotalPages" @click="materialsPage++">{{ t('teacher.courseDetail.sections.next') }}</Button>
          </div>
        </div>
        <div class="card p-6 order-1 sm:order-2 h-full">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-xl font-semibold leading-tight break-all">{{ t('teacher.courseDetail.sections.videos') }}</h2>
            <FileUpload
              ref="videoUploader"
              :multiple="true"
              :autoUpload="true"
              :accept="'video/*'"
              :upload-url="`${baseURL}/files/upload`"
              :upload-headers="uploadHeaders"
              :upload-data="{ purpose: 'course_video', relatedId: course.id }"
              @upload-success="refreshVideos"
              :compact="true"
              :dense="true"
            />
          </div>
          <div class="mb-3">
            <input v-model="videoQuery" :placeholder="t('teacher.courseDetail.sections.searchVideo')" class="input input-sm w-full" />
          </div>
          <ul class="divide-y divide-gray-200">
            <li v-for="f in paginatedVideos" :key="f.id" class="py-2 flex items-center justify-between">
              <div class="flex items-center min-w-0 mr-3 gap-2">
                <component :is="fileIcon(f)" class="w-5 h-5 text-gray-500" />
                <div class="min-w-0">
                  <div class="text-sm truncate">{{ f.originalName || f.fileName || t('teacher.courseDetail.sections.file', { id: f.id }) }}</div>
                  <div class="text-xs text-gray-500">{{ t('teacher.courseDetail.sections.size', { size: formatSize(f.fileSize) }) }}</div>
                </div>
              </div>
              <div class="flex items-center gap-2">
                <Button size="sm" variant="outline" as="a" :href="`${baseURL}/files/${f.id}/download`">{{ t('teacher.courseDetail.sections.download') }}</Button>
                <Button size="sm" variant="danger" @click="confirmDelete(f.id, 'video')">{{ t('teacher.courseDetail.sections.delete') }}</Button>
              </div>
            </li>
            <li v-if="!videos.length" class="py-6 text-center text-sm text-gray-500">{{ t('teacher.courseDetail.sections.noVideos') }}</li>
          </ul>
          <div v-if="videos.length > pageSize" class="mt-3 flex items-center justify-end gap-2">
            <Button size="sm" variant="outline" :disabled="videosPage===1" @click="videosPage--">{{ t('teacher.courseDetail.sections.prev') }}</Button>
            <span class="text-xs text-gray-500">{{ videosPage }} / {{ videosTotalPages }}</span>
            <Button size="sm" variant="outline" :disabled="videosPage===videosTotalPages" @click="videosPage++">{{ t('teacher.courseDetail.sections.next') }}</Button>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="text-center py-12">
      <h2 class="text-2xl font-bold">{{ t('teacher.courseDetail.notFound.title') }}</h2>
      <p class="text-gray-500">{{ t('teacher.courseDetail.notFound.desc') }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed, ref, watch } from 'vue';
import { useCourseStore } from '@/stores/course';
import { useRoute, useRouter } from 'vue-router';
import apiClient, { baseURL } from '@/api/config';
import FileUpload from '@/components/forms/FileUpload.vue';
import { fileApi } from '@/api/file.api';
import Button from '@/components/ui/Button.vue'
import { DocumentIcon, PhotoIcon, FilmIcon, ArchiveBoxIcon, ChevronRightIcon, UserGroupIcon, ClipboardDocumentListIcon, PresentationChartBarIcon } from '@heroicons/vue/24/outline'
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'

const courseStore = useCourseStore();
const route = useRoute();
const router = useRouter();
const { t } = useI18n()

const course = computed(() => courseStore.currentCourse);
const materials = ref<any[]>([]);
const videos = ref<any[]>([]);
const materialQuery = ref('');
const videoQuery = ref('');
const pageSize = ref(10);
const materialsPage = ref(1);
const videosPage = ref(1);
const materialsTotalPages = computed(() => Math.max(1, Math.ceil(materials.value.length / pageSize.value)));
const videosTotalPages = computed(() => Math.max(1, Math.ceil(videos.value.length / pageSize.value)));
const filteredMaterials = computed(() => {
  if (!materialQuery.value) return materials.value;
  const q = materialQuery.value.toLowerCase();
  return materials.value.filter((f: any) => (f.originalName || f.fileName || '').toLowerCase().includes(q));
});
const filteredVideos = computed(() => {
  if (!videoQuery.value) return videos.value;
  const q = videoQuery.value.toLowerCase();
  return videos.value.filter((f: any) => (f.originalName || f.fileName || '').toLowerCase().includes(q));
});
const paginatedMaterials = computed(() => {
  const start = (materialsPage.value - 1) * pageSize.value;
  return filteredMaterials.value.slice(start, start + pageSize.value);
});
const paginatedVideos = computed(() => {
  const start = (videosPage.value - 1) * pageSize.value;
  return filteredVideos.value.slice(start, start + pageSize.value);
});

const fileIcon = (f: any) => {
  const name = (f.originalName || f.fileName || '').toLowerCase();
  const ext = (f.extension || name.split('.').pop() || '').toLowerCase();
  const mime = (f.mimeType || f.contentType || '').toLowerCase();
  if (mime.startsWith('image/') || ['png','jpg','jpeg','webp','gif'].includes(ext)) return PhotoIcon
  if (mime.startsWith('video/') || ['mp4','mov','avi','mkv'].includes(ext)) return FilmIcon
  if (['zip','rar','7z'].includes(ext)) return ArchiveBoxIcon
  return DocumentIcon
}
const uploadHeaders = {
  Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : ''
};
const coverSrc = ref('');
const isHttpUrl = (v?: string) => !!v && /^(http|https):\/\//i.test(v);
const loadCover = async () => {
  const v: any = course.value?.coverImage;
  if (!v) { coverSrc.value = ''; return; }
  // 外链在本地网络环境可能被拦截，直接不加载，由占位显示
  if (isHttpUrl(v)) { coverSrc.value = ''; return; }
  try {
    const blob: any = await apiClient.get(`/files/${encodeURIComponent(String(v))}/preview`, { responseType: 'blob' });
    coverSrc.value = URL.createObjectURL(blob);
  } catch {
    coverSrc.value = '';
  }
};
watch(course, loadCover, { immediate: true, deep: true });

const formatSize = (bytes?: number) => {
  if (!bytes || bytes <= 0) return t('teacher.analytics.charts.unknown') as string;
  const units = ['B','KB','MB','GB'];
  let i = 0; let n = bytes;
  while (n >= 1024 && i < units.length - 1) { n /= 1024; i++; }
  return `${n.toFixed(1)} ${units[i]}`;
};

const refreshMaterials = async () => {
  if (!course.value?.id) return;
  const res: any = await fileApi.getRelatedFiles('course_material', course.value.id);
  materials.value = res?.data || res || [];
};
const refreshVideos = async () => {
  if (!course.value?.id) return;
  const res: any = await fileApi.getRelatedFiles('course_video', course.value.id);
  videos.value = res?.data || res || [];
};

const confirmDelete = async (fileId: string | number, kind: 'material'|'video') => {
  if (!confirm(t('teacher.courseDetail.confirm.deleteFile'))) return;
  await fileApi.deleteFile(String(fileId));
  if (kind === 'material') await refreshMaterials(); else await refreshVideos();
};

onMounted(() => {
  const courseId = route.params.id as string;
  if (courseId) {
    courseStore.fetchCourseById(courseId);
  }
  // 延迟到课程加载后刷新附件
  setTimeout(() => { refreshMaterials(); refreshVideos(); }, 300);
});
</script>
