<template>
  <div class="p-6 bg-gray-50">
    <!-- Header -->
    <div class="mb-8">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold">我的课程</h1>
          <p class="text-gray-500">浏览和管理您的学习课程</p>
        </div>
        <button class="btn btn-primary" @click="showCourseStore = true">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" /></svg>
          浏览课程
        </button>
      </div>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <div class="card p-4 text-center">
        <div class="text-2xl font-bold text-blue-600">{{ activeCoursesCount }}</div>
        <p class="text-sm text-gray-500">进行中</p>
      </div>
      <div class="card p-4 text-center">
        <div class="text-2xl font-bold text-green-600">{{ completedCoursesCount }}</div>
        <p class="text-sm text-gray-500">已完成</p>
      </div>
      <div class="card p-4 text-center">
        <div class="text-2xl font-bold text-purple-600">{{ averageProgress.toFixed(1) }}%</div>
        <p class="text-sm text-gray-500">平均进度</p>
      </div>
    </div>

    <!-- Search and Filter -->
    <div class="card p-4 mb-8">
      <div class="flex flex-col md:flex-row gap-4">
        <div class="flex-grow">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索我的课程..."
            class="input w-full"
          />
        </div>
        <select v-model="selectedCategory" class="input md:w-48">
          <option value="">全部分类</option>
          <option v-for="category in categories" :key="category" :value="category">
            {{ category }}
          </option>
        </select>
      </div>
    </div>
    
    <!-- Loading or Empty State -->
    <div v-if="studentStore.loading" class="text-center py-12">
      <p>正在加载我的课程...</p>
    </div>
    <div v-else-if="filteredCourses.length === 0" class="text-center py-12 card">
      <h3 class="text-lg font-medium">没有找到课程</h3>
      <p class="text-gray-500 mt-2">您还没有报名任何课程，或者没有匹配筛选条件的课程。</p>
      <button class="btn btn-primary mt-4" @click="showCourseStore = true">去课程商店看看</button>
    </div>

    <!-- Courses Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="course in filteredCourses"
        :key="course.id"
        class="card overflow-hidden cursor-pointer group"
        @click="enterCourse(course)"
      >
        <div class="relative h-48">
          <img v-if="course.coverImageUrl" :src="course.coverImageUrl" :alt="course.title" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"/>
          <div v-else class="w-full h-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center">
            <span class="text-4xl font-bold text-white">{{ course.title.charAt(0) }}</span>
          </div>
          <div class="absolute bottom-0 left-0 right-0 bg-black bg-opacity-50 p-4">
            <div class="flex items-center justify-between text-white text-sm mb-2">
              <span>学习进度</span>
              <span>{{ course.progress.toFixed(0) }}%</span>
            </div>
            <div class="w-full bg-gray-200 rounded-full h-1.5"><div class="bg-blue-600 h-1.5 rounded-full" :style="{ width: `${course.progress}%` }"></div></div>
          </div>
        </div>
        <div class="p-6">
          <h3 class="text-lg font-semibold line-clamp-2 mb-2">{{ course.title }}</h3>
          <p class="text-gray-600 text-sm mb-4 line-clamp-2">{{ course.description }}</p>
          <div class="flex items-center justify-between text-sm">
            <span class="text-gray-500">讲师: {{ course.instructorName }}</span>
            <span class="font-medium" :class="course.progress === 100 ? 'text-green-600' : 'text-blue-600'">
              {{ course.progress === 100 ? '已完成' : '进行中' }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Course Store Modal -->
    <div v-if="showCourseStore" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" @click.self="showCourseStore = false">
      <div class="bg-white rounded-lg w-full max-w-4xl max-h-[90vh] flex flex-col">
        <div class="p-6 border-b flex justify-between items-center">
          <h3 class="text-lg font-semibold">课程商店</h3>
          <button @click="showCourseStore = false">&times;</button>
        </div>
        <div class="p-6 overflow-y-auto">
           <div v-if="courseStore.loading" class="text-center"><p>正在加载课程...</p></div>
           <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div v-for="course in courseStore.courses" :key="course.id" class="border rounded-lg p-4">
              <h4 class="font-medium mb-2">{{ course.title }}</h4>
              <p class="text-sm text-gray-600 mb-3">{{ course.description }}</p>
              <div class="flex justify-between items-center">
                <span class="text-sm text-gray-500">讲师: {{ course.teacherName }}</span>
                <button
                  class="btn btn-sm btn-primary"
                  :disabled="isEnrolled(String(course.id)) || enrollingId === String(course.id)"
                  @click="handleEnroll(String(course.id))"
                >
                  <span v-if="enrollingId === String(course.id)">处理中...</span>
                  <span v-else-if="isEnrolled(String(course.id))">已报名</span>
                  <span v-else>加入课程</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useStudentStore } from '@/stores/student';
import { useCourseStore } from '@/stores/course';
import type { StudentCourse } from '@/types/student';

const router = useRouter();
const studentStore = useStudentStore();
const courseStore = useCourseStore();

// State
const showCourseStore = ref(false);
const enrollingId = ref<string | null>(null);
const searchQuery = ref('');
const selectedCategory = ref('');

// Computed Properties
const myCourses = computed(() => studentStore.myCourses);

const activeCoursesCount = computed(() => myCourses.value.filter(c => c.progress < 100).length);
const completedCoursesCount = computed(() => myCourses.value.filter(c => c.progress === 100).length);
const averageProgress = computed(() => {
  if (myCourses.value.length === 0) return 0;
  const total = myCourses.value.reduce((sum, course) => sum + course.progress, 0);
  return total / myCourses.value.length;
});

const categories = computed(() => {
  const cats = new Set(myCourses.value.map(c => c.category));
  return Array.from(cats).sort();
});

const filteredCourses = computed(() => {
  return myCourses.value.filter(course => {
    const searchMatch = searchQuery.value.toLowerCase() === '' || course.title.toLowerCase().includes(searchQuery.value.toLowerCase());
    const categoryMatch = selectedCategory.value === '' || course.category === selectedCategory.value;
    return searchMatch && categoryMatch;
  });
});

// Methods
const enterCourse = (course: StudentCourse) => {
  router.push(`/student/courses/${course.id}`);
};

const handleEnroll = async (courseId: string) => {
  enrollingId.value = courseId;
  const success = await courseStore.enrollInCourse(courseId);
  if (success) {
    await studentStore.fetchMyCourses(); // Refresh student's course list
  }
  enrollingId.value = null;
};

const isEnrolled = (courseId: string) => {
  return myCourses.value.some(c => c.id === courseId);
};

// Lifecycle Hooks
onMounted(() => {
  studentStore.fetchMyCourses();
  courseStore.fetchCourses(); // For the course store modal
});
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
