<template>
  <div class="p-6">
    <div v-if="courseStore.loading || lessonStore.loading" class="text-center py-12">
      <p>正在加载课程详情...</p>
    </div>

    <div v-else-if="course" class="max-w-7xl mx-auto">
      <!-- Breadcrumbs -->
      <nav class="mb-6">
        <ol class="flex items-center space-x-2 text-sm">
          <li><router-link to="/student/courses" class="text-gray-500 hover:text-blue-600">我的课程</router-link></li>
          <li><span class="text-gray-400">/</span></li>
          <li class="font-medium text-gray-700">{{ course.title }}</li>
        </ol>
      </nav>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Main Content -->
        <div class="lg:col-span-2">
          <!-- Course Header -->
          <div class="mb-8">
            <h1 class="text-3xl font-bold mb-2">{{ course.title }}</h1>
            <p class="text-lg text-gray-600">{{ course.description }}</p>
            <div class="mt-4 p-4 rounded border inline-flex items-center gap-3">
              <span class="text-sm text-gray-600">课程总体进度</span>
              <div class="font-semibold">{{ courseProgress }}%</div>
            </div>
          </div>
          
          <!-- Lesson List -->
          <div class="card">
            <div class="card-header">
                <h2 class="text-xl font-semibold">课程内容</h2>
                <span class="text-sm text-gray-500">{{ completedLessonsCount }} / {{ lessons.length }} 已完成</span>
            </div>
            <div class="space-y-3 p-4">
              <div
                v-for="(lesson, index) in lessons"
                :key="lesson.id"
                class="p-4 border rounded-lg transition-colors"
                :class="{ 'bg-green-50 border-green-200': lesson.isCompleted }"
              >
                <div class="flex items-start gap-4">
                  <div class="flex-shrink-0 mr-1">
                    <div v-if="lesson.isCompleted" class="w-8 h-8 bg-green-500 rounded-full flex items-center justify-center text-white">✓</div>
                    <div v-else class="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center font-medium">{{ index + 1 }}</div>
                  </div>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center justify-between gap-3">
                      <div class="min-w-0">
                        <h3 class="font-medium truncate">{{ lesson.title }}</h3>
                        <p class="text-sm text-gray-600">{{ lesson.description }}</p>
                      </div>
                      <div class="flex items-center gap-2">
                        <button
                          v-if="!lesson.isCompleted"
                          class="btn btn-sm btn-outline-primary"
                          @click="handleCompleteLesson(lesson.id)"
                          :disabled="completingId === lesson.id"
                        >
                          {{ completingId === lesson.id ? '处理中...' : '标记完成' }}
                        </button>
                        <button class="btn btn-sm" @click="toggleLesson(lesson)">{{ expanded.has(lesson.id) ? '收起' : '展开' }}</button>
                      </div>
                    </div>

                    <div v-if="expanded.has(lesson.id)" class="mt-4 space-y-4">
                      <div v-if="lesson.videoUrl" class="w-full">
                        <video controls class="w-full max-h-72 rounded">
                          <source :src="resolveVideoSrc(lesson.videoUrl)" type="video/mp4" />
                        </video>
                      </div>

                      <div>
                        <h4 class="font-medium mb-2">资料</h4>
                        <ul class="list-disc pl-5 space-y-1">
                          <li v-for="f in (lessonMaterials[lesson.id]||[])" :key="f.id">
                            <a class="text-blue-600 hover:underline" :href="`${baseURL}/files/${f.id}/download`">{{ f.originalName || f.fileName || `文件#${f.id}` }}</a>
                          </li>
                          <li v-if="!(lessonMaterials[lesson.id]||[]).length" class="text-sm text-gray-500">暂无资料</li>
                        </ul>
                      </div>

                      <div>
                        <h4 class="font-medium mb-2">本节作业</h4>
                        <ul class="list-disc pl-5 space-y-1">
                          <li v-for="a in lessonAssignments(lesson.id)" :key="a.id">
                            <router-link class="text-blue-600 hover:underline" :to="`/student/assignments/${a.id}/submit`">{{ a.title }}</router-link>
                            <span class="text-xs text-gray-500 ml-2" v-if="a.dueDate">截止：{{ formatDate(a.dueDate) }}</span>
                          </li>
                          <li v-if="lessonAssignments(lesson.id).length === 0" class="text-sm text-gray-500">本节暂无作业</li>
                        </ul>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Sidebar -->
        <div class="lg:col-span-1">
          <div class="card">
            <div class="card-header">
              <h3 class="text-lg font-semibold">讲师信息</h3>
            </div>
            <div class="p-4 text-center">
              <div class="w-16 h-16 bg-gray-200 rounded-full mx-auto mb-4 flex items-center justify-center">
                <span class="text-xl font-medium text-gray-600">{{ course.teacherName.charAt(0) }}</span>
              </div>
              <h4 class="font-medium">{{ course.teacherName }}</h4>
              <div class="mt-4 flex justify-center">
                <button class="btn btn-sm btn-primary" @click="contactTeacher">联系老师</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="text-center py-12 card">
      <h3 class="text-lg font-medium">课程不存在</h3>
      <p class="text-gray-500 mt-2">抱歉，您访问的课程不存在或已被删除。</p>
      <router-link to="/student/courses" class="btn btn-primary mt-4">返回课程列表</router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useCourseStore } from '@/stores/course';
import { useLessonStore } from '@/stores/lesson';
import type { StudentLesson } from '@/types/lesson';
import { useChatStore } from '@/stores/chat'
import { lessonApi } from '@/api/lesson.api'
import { assignmentApi } from '@/api/assignment.api'
import { baseURL } from '@/api/config'
import { fileApi } from '@/api/file.api'

const route = useRoute();
const courseStore = useCourseStore();
const lessonStore = useLessonStore();
const chat = useChatStore();

// State
const completingId = ref<string | null>(null);
const courseProgress = ref<number>(0);
const expanded = ref<Set<string>>(new Set());
const lessonMaterials = ref<Record<string, any[]>>({});
const assignments = ref<any[]>([]);

// Computed Properties
const course = computed(() => courseStore.currentCourse);
const lessons = computed(() => lessonStore.lessons as StudentLesson[]);
const completedLessonsCount = computed(() => lessons.value.filter(l => l.isCompleted).length);

// Methods
const handleCompleteLesson = async (lessonId: string) => {
  completingId.value = lessonId;
  const success = await lessonStore.completeLesson(lessonId);
  if (success && course.value) {
    await fetchCourseProgress(String(course.value.id));
  }
  completingId.value = null;
};

function contactTeacher() {
  const c = course.value
  if (!c) return
  // 假设后端返回 teacherId/teacherName 字段；若不同请在 store 侧做映射
  const teacherId = (c as any).teacherId || (c as any).teacher?.id
  const teacherName = (c as any).teacherName || (c as any).teacher?.name || (c as any).teacher?.username
  if (teacherId) {
    chat.openChat(String(teacherId), teacherName || null, String(c.id))
  }
}

// Lifecycle Hooks
onMounted(async () => {
  const courseId = route.params.id as string;
  if (courseId) {
    await Promise.all([
      courseStore.fetchCourseById(courseId),
      lessonStore.fetchLessonsForCourse(courseId)
    ]);
    await Promise.all([
      fetchCourseProgress(courseId),
      loadAssignments(courseId)
    ]);
  }
});

const fetchCourseProgress = async (courseId: string) => {
  try {
    const res: any = await lessonApi.getCourseProgress(courseId as any);
    const v = res?.progress ?? res?.data ?? res;
    courseProgress.value = Number(v || 0).toFixed ? Number(Number(v).toFixed(0)) : Number(v || 0);
  } catch {
    courseProgress.value = 0;
  }
};

const loadAssignments = async (courseId: string) => {
  const res: any = await assignmentApi.getAssignmentsByCourse(courseId, { page: 1, size: 100 });
  assignments.value = res?.items || res?.data?.items || [];
};

const lessonAssignments = (lessonId: string) => {
  return (assignments.value || []).filter((x: any) => String(x.lessonId || '') === String(lessonId));
};

const toggleLesson = async (lesson: any) => {
  const id = String(lesson.id);
  if (expanded.value.has(id)) { expanded.value.delete(id); return; }
  expanded.value.add(id);
  // 懒加载资料
  if (!lessonMaterials.value[id]) {
    try {
      const list: any = await fileApi.getRelatedFiles('lesson_material', id);
      lessonMaterials.value[id] = list?.data || list || [];
    } catch {
      lessonMaterials.value[id] = [];
    }
  }
};

const resolveVideoSrc = (videoUrl: string) => {
  // 若为纯数字（文件ID），构造下载地址，否则直接返回外链
  if (/^\d+$/.test(String(videoUrl))) return `${baseURL}/files/${videoUrl}/download`;
  return videoUrl;
};

const formatDate = (v: any) => {
  try { return new Date(v).toLocaleString(); } catch { return v; }
};
</script>
