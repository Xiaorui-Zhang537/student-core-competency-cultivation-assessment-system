<template>
  <div class="p-6">
    <div v-if="courseStore.loading" class="text-center py-12">
      <p>正在加载课程详情...</p>
    </div>
    <div v-else-if="course" class="space-y-6">
      <div class="flex items-start justify-between">
        <div>
          <h1 class="text-3xl font-bold">{{ course.title }}</h1>
          <p class="text-gray-500">{{ course.category }}</p>
        </div>
        <div class="flex items-center gap-2">
          <router-link :to="`/teacher/courses/${course.id}/students`" class="btn btn-outline">学生管理</router-link>
          <router-link :to="{ name: 'TeacherAssignments' }" class="btn btn-outline">作业管理</router-link>
          <router-link :to="`/teacher/analytics?courseId=${course.id}`" class="btn btn-primary">课程分析</router-link>
        </div>
      </div>
      <div class="w-full h-56 bg-gray-200 rounded overflow-hidden" v-if="course.coverImage">
        <img v-if="coverSrc" :src="coverSrc" alt="课程封面" class="w-full h-full object-cover" @error="coverSrc='';" />
      </div>
      <div class="card p-6">
        <h2 class="text-xl font-semibold mb-4">课程描述</h2>
        <p>{{ course.description }}</p>
      </div>
      <div class="card p-6">
        <h2 class="text-xl font-semibold mb-4">课程内容</h2>
        <div v-html="course.content"></div>
      </div>
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div class="card p-6">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-xl font-semibold">课程资料</h2>
            <FileUpload
              ref="materialUploader"
              :multiple="true"
              :autoUpload="true"
              :accept="'.pdf,.doc,.docx,.ppt,.pptx,.zip'"
              :upload-url="`${baseURL}/files/upload`"
              :upload-headers="uploadHeaders"
              :upload-data="{ purpose: 'course_material', relatedId: course.id }"
              @upload-success="refreshMaterials"
            />
          </div>
          <div class="mb-3">
            <input v-model="materialQuery" placeholder="搜索资料文件名..." class="input input-sm w-full" />
          </div>
          <ul class="divide-y divide-gray-200">
            <li v-for="f in paginatedMaterials" :key="f.id" class="py-2 flex items-center justify-between">
              <div class="flex items-center min-w-0 mr-3 gap-2">
                <component :is="fileIcon(f)" class="w-5 h-5 text-gray-500" />
                <div class="min-w-0">
                  <div class="text-sm truncate">{{ f.originalName || f.fileName || ('文件#' + f.id) }}</div>
                  <div class="text-xs text-gray-500">大小：{{ formatSize(f.fileSize) }}</div>
                </div>
              </div>
              <div class="flex items-center gap-2">
                <a class="btn btn-sm btn-outline" :href="`${baseURL}/files/${f.id}/download`">下载</a>
                <button class="btn btn-sm btn-danger-outline" @click="confirmDelete(f.id, 'material')">删除</button>
              </div>
            </li>
            <li v-if="!materials.length" class="py-6 text-center text-sm text-gray-500">暂无资料</li>
          </ul>
          <div v-if="materials.length > pageSize" class="mt-3 flex items-center justify-end gap-2">
            <button class="btn btn-sm btn-outline" :disabled="materialsPage===1" @click="materialsPage--">上一页</button>
            <span class="text-xs text-gray-500">{{ materialsPage }} / {{ materialsTotalPages }}</span>
            <button class="btn btn-sm btn-outline" :disabled="materialsPage===materialsTotalPages" @click="materialsPage++">下一页</button>
          </div>
        </div>
        <div class="card p-6">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-xl font-semibold">课程视频</h2>
            <FileUpload
              ref="videoUploader"
              :multiple="true"
              :autoUpload="true"
              :accept="'video/*'"
              :upload-url="`${baseURL}/files/upload`"
              :upload-headers="uploadHeaders"
              :upload-data="{ purpose: 'course_video', relatedId: course.id }"
              @upload-success="refreshVideos"
            />
          </div>
          <div class="mb-3">
            <input v-model="videoQuery" placeholder="搜索视频文件名..." class="input input-sm w-full" />
          </div>
          <ul class="divide-y divide-gray-200">
            <li v-for="f in paginatedVideos" :key="f.id" class="py-2 flex items-center justify-between">
              <div class="flex items-center min-w-0 mr-3 gap-2">
                <component :is="fileIcon(f)" class="w-5 h-5 text-gray-500" />
                <div class="min-w-0">
                  <div class="text-sm truncate">{{ f.originalName || f.fileName || ('视频#' + f.id) }}</div>
                  <div class="text-xs text-gray-500">大小：{{ formatSize(f.fileSize) }}</div>
                </div>
              </div>
              <div class="flex items-center gap-2">
                <a class="btn btn-sm btn-outline" :href="`${baseURL}/files/${f.id}/download`">下载</a>
                <button class="btn btn-sm btn-danger-outline" @click="confirmDelete(f.id, 'video')">删除</button>
              </div>
            </li>
            <li v-if="!videos.length" class="py-6 text-center text-sm text-gray-500">暂无视频</li>
          </ul>
          <div v-if="videos.length > pageSize" class="mt-3 flex items-center justify-end gap-2">
            <button class="btn btn-sm btn-outline" :disabled="videosPage===1" @click="videosPage--">上一页</button>
            <span class="text-xs text-gray-500">{{ videosPage }} / {{ videosTotalPages }}</span>
            <button class="btn btn-sm btn-outline" :disabled="videosPage===videosTotalPages" @click="videosPage++">下一页</button>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="text-center py-12">
      <h2 class="text-2xl font-bold">未找到课程</h2>
      <p class="text-gray-500">无法加载所请求的课程详情。</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed, ref, watch } from 'vue';
import { useCourseStore } from '@/stores/course';
import { useRoute } from 'vue-router';
import apiClient, { baseURL } from '@/api/config';
import FileUpload from '@/components/forms/FileUpload.vue';
import { fileApi } from '@/api/file.api';
import { DocumentIcon, PhotoIcon, FilmIcon, ArchiveBoxIcon } from '@heroicons/vue/24/outline'

const courseStore = useCourseStore();
const route = useRoute();

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
  if (!bytes || bytes <= 0) return '未知';
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
  if (!confirm('确认删除该文件？此操作不可撤销。')) return;
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
