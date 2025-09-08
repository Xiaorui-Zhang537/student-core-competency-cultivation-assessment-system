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

      <PageHeader :title="course.title" :subtitle="course.category">
        <template #actions>
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
        </template>
      </PageHeader>
      <div class="w-full h-56 bg-gray-200 rounded overflow-hidden" v-if="course.coverImage">
        <img v-if="coverSrc" :src="coverSrc" :alt="t('teacher.courses.card.coverAlt')" class="w-full h-full object-cover" @error="coverSrc='';" />
      </div>
      <Card :hoverable="true" padding="md" class="relative overflow-hidden">
        <h2 class="text-xl font-semibold mb-4">{{ t('teacher.courseDetail.sections.description') }}</h2>
        <p>{{ course.description }}</p>
      </Card>
      <Card :hoverable="true" padding="md" class="relative overflow-hidden">
        <h2 class="text-xl font-semibold mb-4">{{ t('teacher.courseDetail.sections.content') }}</h2>
        <div v-html="course.content"></div>
      </Card>

      <!-- Lessons Editor: inline per-lesson editing for weight/video/materials/assignment binding -->
      <Card :hoverable="true" padding="md" class="relative overflow-hidden">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-xl font-semibold">{{ t('teacher.courseDetail.sections.lessons') || '课程节次' }}</h2>
          <Button size="sm" variant="outline" @click="reloadLessons">{{ t('teacher.courseDetail.actions.reloadLessons') || '刷新' }}</Button>
        </div>
        <!-- Chapters Toolbar -->
        <div class="p-4 rounded border relative overflow-hidden" v-glass="{ strength: 'regular', interactive: true }" :class="['glass-regular glass-interactive','mb-4']">
          <div class="grid grid-cols-1 md:grid-cols-3 gap-3 items-end">
            <div>
              <label class="block text-sm mb-1">{{ t('teacher.courseDetail.sections.newChapterTitle') }}</label>
              <GlassInput class="input-sm w-full" v-model="newChapterTitle" :placeholder="t('teacher.courseDetail.sections.newChapterTitlePh') as string" />
            </div>
            <div class="md:col-span-2">
              <label class="block text-sm mb-1">{{ t('teacher.courseDetail.sections.chapterDesc') }}</label>
              <GlassInput class="input-sm w-full" v-model="newChapterDesc" :placeholder="t('teacher.courseDetail.sections.chapterDescPh') as string" />
            </div>
          </div>
          <div class="mt-3 flex items-center gap-2">
            <Button size="sm" variant="primary" @click="createChapter">{{ t('teacher.courseDetail.actions.addChapter') }}</Button>
            <Button size="sm" variant="outline" @click="reloadChapters">{{ t('teacher.courseDetail.actions.reloadChapters') }}</Button>
          </div>
          <div class="mt-4">
            <div class="text-sm mb-2">{{ t('teacher.courseDetail.sections.chapterList') }}</div>
            <ul class="rounded border overflow-hidden">
              <li v-for="c in chapters" :key="c.id"
                  class="p-3 flex items-center justify-between cursor-move relative"
                  draggable="true" @dragstart="onChapterDragStart(c)" @dragover.prevent @drop="onChapterDrop(c)">
                <div class="absolute inset-0" v-glass></div>
                <div class="min-w-0 relative">
                  <div class="text-sm font-medium truncate">{{ c.title }}</div>
                  <div class="text-xs text-gray-500 truncate">{{ c.description }}</div>
                </div>
                <span class="text-xs text-gray-400 relative">#{{ c.orderIndex }}</span>
              </li>
              <li v-if="!chapters.length" class="p-3 text-center text-xs text-gray-500">{{ t('teacher.courseDetail.sections.noChapters') }}</li>
            </ul>
          </div>
        </div>
        <div class="space-y-4">
          <!-- New lesson inline form -->
          <div class="p-4 rounded border relative overflow-hidden" v-glass="{ strength: 'regular', interactive: true }">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-3 items-end">
              <div>
                <label class="block text-sm mb-1">{{ t('teacher.courseDetail.sections.newLessonTitle') }}</label>
                <GlassInput class="input-sm w-full" v-model="newLessonTitle" :placeholder="t('teacher.courseDetail.sections.newLessonTitlePh') as string" />
              </div>
              <div class="md:col-span-2">
                <label class="block text-sm mb-1">{{ t('teacher.courseDetail.sections.lessonDesc') }}</label>
                <GlassInput class="input-sm w-full" v-model="newLessonDesc" :placeholder="t('teacher.courseDetail.sections.lessonDescPh') as string" />
              </div>
            </div>
            <div class="mt-3">
              <Button size="sm" variant="primary" @click="createLesson">{{ t('teacher.courseDetail.actions.addLesson') }}</Button>
            </div>
          </div>
          <div v-for="l in lessons" :key="l.id" class="p-4 rounded border border-gray-300 dark:border-gray-600 relative overflow-hidden group" draggable="true" @dragstart="onDragStart(l)" @dragover.prevent @drop="onDrop(l)">
            <div class="flex items-center gap-3">
              <div class="font-medium flex-1 truncate">{{ l.title }}</div>
              <div class="w-full md:w-1/2 flex items-center justify-end gap-3">
                <div class="flex items-center gap-2 whitespace-nowrap">
                  <label class="text-sm whitespace-nowrap">{{ t('teacher.courseDetail.sections.chapterSelect') }}</label>
                  <div class="w-56">
                    <GlassPopoverSelect
                      :options="chapterOptions"
                      size="sm"
                      :label="undefined"
                      v-model="l._chapterId"
                      @change="() => saveChapter(l)"
                    />
                  </div>
                </div>
                <div class="flex items-center gap-2 whitespace-nowrap">
                  <label class="text-sm whitespace-nowrap">{{ t('teacher.courseDetail.sections.weight') }}</label>
                  <GlassInput class="input-sm w-20" :fullWidth="false" type="number" step="0.1" v-model="(l._weight as any)" />
                </div>
                <Button size="sm" variant="primary" @click="saveWeight(l)">{{ t('teacher.courseDetail.actions.saveWeight') }}</Button>
              </div>
            </div>
            <div class="mt-3 grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <div class="flex items-center gap-2 whitespace-nowrap">
                  <label class="text-sm whitespace-nowrap">{{ t('teacher.courseDetail.sections.video') }}</label>
                  <GlassInput class="input-sm w-80 md:w-96 flex-1 min-w-0" :fullWidth="false" v-model="l._videoUrl" :placeholder="t('teacher.courseDetail.sections.videoUrlPh') as string" />
                  <Button size="sm" variant="outline" class="whitespace-nowrap" @click="openVideoPicker(l)">{{ t('teacher.courseDetail.actions.selectVideoShort') }}</Button>
                </div>
              </div>
              <div>
                <div class="flex items-center gap-2">
                  <label class="text-sm">{{ t('teacher.courseDetail.sections.bindAssignment') }}</label>
                  <div class="w-56">
                    <GlassPopoverSelect
                      :options="assignmentOptions"
                      size="sm"
                      :label="undefined"
                      v-model="l._assignmentId"
                    />
                  </div>
                  <Button size="sm" variant="primary" class="whitespace-nowrap" @click="bindAssignment(l)">{{ t('teacher.courseDetail.actions.saveBinding') }}</Button>
                </div>
              </div>
              <div class="md:col-span-2">
                <div class="mt-2">
                  <label class="block text-sm mb-1">{{ t('teacher.courseDetail.sections.lessonIntro') }}</label>
                  <GlassTextarea class="w-full" :rows="2" v-model="l._content" :placeholder="t('teacher.courseDetail.sections.lessonIntroPh') as string" />
                </div>
                <div class="mt-2 flex items-center gap-2">
                  <Button size="sm" variant="primary" @click="saveContent(l)">{{ t('teacher.courseDetail.actions.saveContent') }}</Button>
                </div>
              </div>
            </div>
          </div>
          <div v-if="!lessons.length" class="text-sm text-gray-500">{{ t('teacher.courseDetail.sections.noLessons') }}</div>
        </div>
      </Card>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6 items-stretch">
        <Card padding="md" class="order-2 sm:order-1 h-full relative overflow-hidden">
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
            <GlassSearchInput v-model="materialQuery" :placeholder="t('teacher.courseDetail.sections.searchMaterial') as string" size="sm" />
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
        </Card>
        <Card padding="md" class="order-1 sm:order-2 h-full relative overflow-hidden">
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
            <GlassSearchInput v-model="videoQuery" :placeholder="t('teacher.courseDetail.sections.searchVideo') as string" size="sm" />
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
        </Card>
      </div>
    </div>
    <div v-else class="text-center py-12">
      <h2 class="text-2xl font-bold">{{ t('teacher.courseDetail.notFound.title') }}</h2>
      <p class="text-gray-500">{{ t('teacher.courseDetail.notFound.desc') }}</p>
    </div>
  </div>

  <!-- Video Picker Modal -->
  <teleport to="body">
    <div v-if="videoPickerVisible" class="fixed inset-0 z-[1100] flex items-center justify-center">
      <div class="absolute inset-0 bg-black/40" @click="videoPickerVisible = false"></div>
      <div
        class="relative z-[1101] w-[44rem] max-w-[90vw] max-h-[80vh] rounded-xl overflow-hidden"
        v-glass="{ strength: 'regular', interactive: true }"
      >
        <div class="px-4 py-3 border-b border-white/20 flex items-center justify-between">
          <h3 class="text-lg font-semibold">{{ t('teacher.courseDetail.actions.selectVideoTitle') }}</h3>
          <button class="text-sm text-gray-500 hover:text-gray-700" @click="videoPickerVisible = false">×</button>
        </div>
        <div class="p-4">
          <GlassSearchInput v-model="videoQuery" :placeholder="t('teacher.courseDetail.sections.searchVideo') as string" size="sm" />
          <ul class="divide-y divide-gray-200 overflow-y-auto" style="max-height: 55vh;">
            <li v-for="f in paginatedVideos" :key="f.id" class="py-2 flex items-center justify-between">
              <div class="flex items-center min-w-0 mr-3 gap-2">
                <component :is="fileIcon(f)" class="w-5 h-5 text-gray-500" />
                <div class="min-w-0">
                  <div class="text-sm truncate">{{ f.originalName || f.fileName || t('teacher.courseDetail.sections.file', { id: f.id }) }}</div>
                  <div class="text-xs text-gray-500">{{ t('teacher.courseDetail.sections.size', { size: formatSize(f.fileSize) }) }}</div>
                </div>
              </div>
              <div class="flex items-center gap-2">
                <Button size="sm" variant="primary" class="whitespace-nowrap" @click="chooseVideo(f)">{{ t('teacher.courseDetail.actions.useThisVideo') }}</Button>
                <Button size="sm" variant="outline" as="a" :href="`${baseURL}/files/${f.id}/download`">{{ t('teacher.courseDetail.sections.download') }}</Button>
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
        <div class="px-4 py-3 border-t border-white/10 flex items-center justify-end">
          <Button size="sm" variant="outline" @click="videoPickerVisible = false">{{ t('teacher.courseDetail.actions.close') }}</Button>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { onMounted, computed, ref, watch } from 'vue';
import { useCourseStore } from '@/stores/course';
import { lessonApi } from '@/api/lesson.api';
import { assignmentApi } from '@/api/assignment.api';
import { useRoute, useRouter } from 'vue-router';
import apiClient, { baseURL } from '@/api/config';
import FileUpload from '@/components/forms/FileUpload.vue';
import { fileApi } from '@/api/file.api';
import Button from '@/components/ui/Button.vue'
import Card from '@/components/ui/Card.vue'
import { chapterApi } from '@/api/chapter.api'
import { DocumentIcon, PhotoIcon, FilmIcon, ArchiveBoxIcon, ChevronRightIcon, UserGroupIcon, ClipboardDocumentListIcon, PresentationChartBarIcon } from '@heroicons/vue/24/outline'
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'

const courseStore = useCourseStore();
const route = useRoute();
const router = useRouter();
const { t } = useI18n()

// 接收路由 props: id（用于避免 extraneous non-props attributes 警告）
const props = withDefaults(defineProps<{ id?: string | number }>(), {});

const course = computed(() => courseStore.currentCourse);
const lessons = ref<any[]>([]);
const newLessonTitle = ref('');
const newLessonDesc = ref('');
const courseAssignments = ref<any[]>([]);
const materials = ref<any[]>([]);
const videos = ref<any[]>([]);
const materialQuery = ref('');
const videoQuery = ref('');
const pageSize = ref(10);
const materialsPage = ref(1);
const videosPage = ref(1);
const materialsTotalPages = computed(() => Math.max(1, Math.ceil(materials.value.length / pageSize.value)));
const videosTotalPages = computed(() => Math.max(1, Math.ceil(videos.value.length / pageSize.value)));
const chapters = ref<any[]>([]);
const newChapterTitle = ref('');
const newChapterDesc = ref('');
const videoPickerVisible = ref(false);
const videoPickerFor = ref<any>(null);
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

// Glass select options
const chapterOptions = computed(() => [
  { label: t('teacher.courseDetail.sections.unset') as string, value: '' },
  ...chapters.value.map((c: any) => ({ label: String(c.title || c.id), value: String(c.id) }))
]);
const assignmentOptions = computed(() => [
  { label: t('teacher.courseDetail.sections.unbound') as string, value: '' },
  ...courseAssignments.value.map((a: any) => ({ label: String(a.title || a.id), value: String(a.id) }))
]);

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
  const courseId = getCourseId();
  if (courseId) {
    courseStore.fetchCourseById(courseId);
  }
  // 延迟到课程加载后刷新附件
  setTimeout(() => { refreshMaterials(); refreshVideos(); }, 300);
  // 加载课程节与作业
  setTimeout(() => { reloadLessons(); reloadAssignments(); }, 350);
  // 加载章节
  setTimeout(() => { reloadChapters(); }, 360);
});

const reloadLessons = async () => {
  const courseId = getCourseId();
  if (!courseId) return;
  const res: any = await lessonApi.getLessonsByCourse(courseId);
  const arr = (res?.data || res || []) as any[];
  lessons.value = arr.map((x: any) => ({
    ...x,
    _weight: x.weight ?? 1.0,
    _videoUrl: x.videoUrl || '',
    _assignmentId: '',
    _chapterId: x.chapterId ? String(x.chapterId) : '',
    _content: x.content || ''
  }));
};

const reloadAssignments = async () => {
  const courseId = getCourseId();
  if (!courseId) return;
  const res: any = await assignmentApi.getAssignmentsByCourse(courseId, { page: 1, size: 100 });
  const items = res?.items || res?.data?.items || [];
  courseAssignments.value = items;
};

const saveWeight = async (l: any) => {
  await lessonApi.updateLesson(String(l.id), { weight: Number(l._weight || 1.0) } as any);
};

const saveContent = async (l: any) => {
  await lessonApi.updateLessonContent(String(l.id), { videoUrl: l._videoUrl });
  if (typeof l._content === 'string') {
    await lessonApi.updateLesson(String(l.id), { content: l._content } as any);
  }
};

const bindAssignment = async (l: any) => {
  const aId = l._assignmentId;
  if (!aId) return;
  await assignmentApi.bindLesson(String(aId), String(l.id));
};

// Chapters CRUD & ordering
const reloadChapters = async () => {
  const courseId = getCourseId();
  if (!courseId) return;
  const res: any = await chapterApi.listByCourse(courseId);
  chapters.value = res?.data || res || [];
};

const createChapter = async () => {
  const courseId = getCourseId();
  if (!courseId || !newChapterTitle.value.trim()) return;
  await chapterApi.create({ courseId: Number(courseId), title: newChapterTitle.value.trim(), description: newChapterDesc.value.trim() });
  newChapterTitle.value = '';
  newChapterDesc.value = '';
  await reloadChapters();
};

const saveChapter = async (l: any) => {
  await lessonApi.updateLesson(String(l.id), { chapterId: l._chapterId ? Number(l._chapterId) : null } as any);
};

// Create lesson
const createLesson = async () => {
  const courseId = getCourseId();
  if (!courseId || !newLessonTitle.value) return;
  await lessonApi.createLesson({ courseId: Number(courseId), title: newLessonTitle.value, description: newLessonDesc.value, weight: 1.0 } as any);
  newLessonTitle.value = '';
  newLessonDesc.value = '';
  await reloadLessons();
};

// Drag sort
let dragItem: any = null;
const onDragStart = (l: any) => { dragItem = l; };
const onDrop = async (target: any) => {
  if (!dragItem || dragItem.id === target.id) return;
  // 简单前端重排：将 dragItem 放到 target 前面
  const arr = [...lessons.value];
  const from = arr.findIndex(x => x.id === dragItem.id);
  const to = arr.findIndex(x => x.id === target.id);
  if (from < 0 || to < 0) return;
  arr.splice(to, 0, arr.splice(from, 1)[0]);
  lessons.value = arr;
  // 依次更新 order_index（从1开始）
  for (let i = 0; i < lessons.value.length; i++) {
    const it = lessons.value[i];
    await lessonApi.updateLessonOrder(String(it.id), i + 1);
  }
  dragItem = null;
};

// Drag & save order for chapters
let chapterDragItem: any = null;
const onChapterDragStart = (c: any) => { chapterDragItem = c; };
const onChapterDrop = async (target: any) => {
  if (!chapterDragItem || chapterDragItem.id === target.id) return;
  const arr = [...chapters.value];
  const from = arr.findIndex(x => x.id === chapterDragItem.id);
  const to = arr.findIndex(x => x.id === target.id);
  if (from < 0 || to < 0) return;
  arr.splice(to, 0, arr.splice(from, 1)[0]);
  chapters.value = arr;
  for (let i = 0; i < chapters.value.length; i++) {
    const it = chapters.value[i];
    await chapterApi.update(it.id, { orderIndex: i + 1 } as any);
  }
  chapterDragItem = null;
};

// Video picker helpers
const openVideoPicker = (l: any) => { videoPickerFor.value = l; videoPickerVisible.value = true; };
const chooseVideo = (file: any) => {
  if (!videoPickerFor.value) return;
  const downloadUrl = `${baseURL}/files/${file.id}/download`;
  videoPickerFor.value._videoUrl = downloadUrl;
  videoPickerVisible.value = false;
};

// helpers
function getCourseId(): string {
  const fromRoute = route.params.id as string | undefined;
  const fromProp = props.id != null ? String(props.id) : '';
  return String(fromRoute || fromProp || '');
}
</script>
