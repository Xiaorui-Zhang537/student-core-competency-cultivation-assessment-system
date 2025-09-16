<template>
  <div class="p-6 bg-gray-50">
    <PageHeader :title="t('student.courses.title')" :subtitle="t('student.courses.subtitle')">
      <template #actions>
        <Button variant="glass" @click="showCourseStore = true">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" /></svg>
          {{ t('student.courses.browse') }}
        </Button>
      </template>
    </PageHeader>

    <!-- Stats (StartCard) -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <StartCard :value="activeCoursesCount" :label="t('student.courses.active') as string" color="blue" />
      <StartCard :value="completedCoursesCount" :label="t('student.courses.completed') as string" color="green" />
      <StartCard :value="`${averageProgress.toFixed(1)}%`" :label="t('student.courses.avgProgress') as string" color="purple" />
    </div>

    <!-- Search and Filter (aligned with Assignments FilterBar) -->
    <div class="mb-8">
      <FilterBar class="glass-thin rounded-full">
        <template #left>
          <div class="relative w-80">
            <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M9 3.5a5.5 5.5 0 100 11 5.5 5.5 0 000-11zM2 9a7 7 0 1112.452 4.391l3.328 3.329a.75.75 0 11-1.06 1.06l-3.329-3.328A7 7 0 012 9z" clip-rule="evenodd"/></svg>
            <input v-model="searchQuery" type="text" :placeholder="t('student.courses.searchPlaceholder') as string" class="input input--glass pl-9 w-full h-10" @keyup.enter="applyImmediateSearch()" />
          </div>
          <div class="w-56 ml-2">
            <GlassPopoverSelect
              v-model="selectedCategory"
              :options="[{ label: t('student.courses.allCategories') as string, value: '' }, ...categoryOptions]"
              size="sm"
              :placeholder="t('student.courses.allCategories') as string"
            />
          </div>
        </template>
      </FilterBar>
    </div>
    
    <!-- Loading or Empty State -->
    <div v-if="!pageLoaded" class="text-center py-12">
      <p>{{ t('student.courses.loading') }}</p>
    </div>
    <Card v-else-if="filteredCourses.length === 0" class="text-center py-12">
      <h3 class="text-lg font-medium">{{ t('student.courses.emptyTitle') }}</h3>
      <p class="text-gray-500 mt-2">{{ t('student.courses.emptyDesc') }}</p>
      <Button class="mt-4" variant="glass" @click="showCourseStore = true">{{ t('student.courses.goStore') }}</Button>
    </Card>
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <Card
        v-for="course in filteredCourses"
        :key="course.id"
        class="overflow-hidden cursor-pointer group"
        padding="none"
        :hoverable="true"
        @click="enterCourse(course)"
      >
        <div class="relative h-48">
          <img v-if="course.coverImageUrl && !coverErrorMap[String(course.id)] && isAllowedImage(course.coverImageUrl)"
               :src="course.coverImageUrl"
               :alt="course.title || ''"
               class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
               @error="coverErrorMap[String(course.id)] = true"
          />
          <div v-else class="w-full h-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center">
            <span class="text-4xl font-bold text-white">{{ (course.title || '').charAt(0) }}</span>
          </div>
          <div class="absolute bottom-0 left-0 right-0 bg-black bg-opacity-50 p-4">
            <div class="flex items-center justify-between text-white text-sm mb-2">
              <span>{{ t('student.courses.progressLabel') }}</span>
              <span>{{ (Number(course.progress) || 0).toFixed(0) }}%</span>
            </div>
            <div class="w-full bg-gray-200 rounded-full h-1.5"><div class="bg-blue-600 h-1.5 rounded-full" :style="{ width: `${Number(course.progress || 0)}%` }"></div></div>
          </div>
        </div>
        <div class="p-6">
          <h3 class="text-lg font-semibold line-clamp-2 mb-2">{{ course.title }}</h3>
          <p class="text-gray-600 text-sm mb-4 line-clamp-2">{{ course.description }}</p>
          <div class="flex items-center justify-between text-sm">
            <span class="text-gray-500">{{ t('student.courses.instructor') }}: {{ course.teacherName }}</span>
            <span class="font-medium" :class="course.progress === 100 ? 'text-green-600' : 'text-blue-600'">
              {{ course.progress === 100 ? t('student.courses.statusCompleted') : t('student.courses.statusOngoing') }}
            </span>
          </div>
        </div>
      </Card>
    </div>

    <!-- Course Store Modal -->
    <div v-if="showCourseStore" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" @click.self="showCourseStore = false">
      <Card class="w-full max-w-4xl max-h-[90vh] flex flex-col">
        <div class="p-6 border-b flex justify-between items-center">
          <h3 class="text-lg font-semibold">{{ t('student.courses.storeTitle') }}</h3>
          <Button variant="glass-ghost" @click="showCourseStore = false">&times;</Button>
        </div>
        <div class="p-6 overflow-y-auto">
          <div v-if="courseStore.loading" class="text-center"><p>{{ t('student.courses.loading') }}</p></div>
          <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Card v-for="course in courseStore.courses" :key="course.id">
              <h4 class="font-medium mb-2">{{ course.title }}</h4>
              <p class="text-sm text-gray-600 mb-3">{{ course.description }}</p>
              <div class="flex justify-between items-center">
                <span class="text-sm text-gray-500">{{ t('student.courses.instructor') }}: {{ course.teacherName }}</span>
                <Button
                  size="sm"
                  variant="primary"
                  :loading="enrollingId === String(course.id)"
                  :disabled="isEnrolled(String(course.id)) || enrollingId === String(course.id)"
                  @click="handleEnroll(String(course.id))"
                >
                  <span v-if="isEnrolled(String(course.id))">{{ t('student.courses.enrolled') }}</span>
                  <span v-else>{{ t('student.courses.enroll') }}</span>
                </Button>
              </div>
            </Card>
          </div>
        </div>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router';
import { useStudentStore } from '@/stores/student';
import { useCourseStore } from '@/stores/course';
import type { StudentCourse } from '@/types/student';
import { t } from '@/i18n';
import Button from '@/components/ui/Button.vue'
import Card from '@/components/ui/Card.vue'
import StartCard from '@/components/ui/StartCard.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import FilterBar from '@/components/ui/filters/FilterBar.vue'

const router = useRouter();
const studentStore = useStudentStore();
const courseStore = useCourseStore();

// State
const showCourseStore = ref(false);
const enrollingId = ref<string | null>(null);
const searchQuery = ref('');
const debouncedQuery = ref('');
const pageLoaded = ref(false);
const coverErrorMap = ref<Record<string, boolean>>({});
const debounced = (fn: Function, wait = 300) => {
  let timer: number | undefined
  return (...args: any[]) => {
    if (timer) window.clearTimeout(timer)
    timer = window.setTimeout(() => fn(...args), wait)
  }
}

const applyImmediateSearch = () => {
  // 这里实际只影响本地 computed 过滤，无需额外动作；预留 hooks 便于后续接入后端搜索
}

// 当输入变化时，延迟更新用于过滤的关键字，提升性能与体验
const updateDebounced = debounced((val: string) => { debouncedQuery.value = val }, 250)
watch(searchQuery, (val) => updateDebounced(String(val || '')))
// 初始化
debouncedQuery.value = ''

const selectedCategory = ref('');

// Computed Properties
// 使用 storeToRefs 保证解构后仍保留响应性
const { myCourses } = storeToRefs(studentStore);
// 兜底：确保所有计算均基于数组
const coursesSafe = computed(() => Array.isArray(myCourses.value) ? myCourses.value : []);

const activeCoursesCount = computed(() => coursesSafe.value.filter((c: StudentCourse) => Number(c.progress || 0) < 100).length);
const completedCoursesCount = computed(() => coursesSafe.value.filter((c: StudentCourse) => Number(c.progress || 0) === 100).length);
const averageProgress = computed(() => {
  if (coursesSafe.value.length === 0) return 0;
  const total = coursesSafe.value.reduce((sum: number, course: StudentCourse) => sum + Number(course.progress || 0), 0);
  return total / coursesSafe.value.length;
});

const categories = computed(() => {
  const cats = new Set(coursesSafe.value.map((c: StudentCourse) => c.category).filter(Boolean));
  return Array.from(cats).sort();
});

const categoryOptions = computed(() => (categories.value as string[]).map((name) => ({ label: name, value: name })))

const filteredCourses = computed(() => {
  const list = Array.isArray(coursesSafe.value) ? coursesSafe.value : []
  const q = (debouncedQuery.value || '').toLowerCase();
  const cat = selectedCategory.value || '';
  return list.filter((course: StudentCourse) => {
    const title = (course.title || '').toLowerCase();
    const searchMatch = q === '' || title.includes(q);
    const categoryMatch = cat === '' || course.category === cat;
    return searchMatch && categoryMatch;
  });
});

// Methods
const enterCourse = (course: StudentCourse) => {
  router.push(`/student/courses/${course.id}`);
};


const handleEnroll = async (courseId: string) => {
  enrollingId.value = courseId;
  await courseStore.enrollInCourse(courseId);
  // After enrollment, refresh the student's course list to reflect the change.
  await studentStore.fetchMyCourses();
  enrollingId.value = null;
};

const isEnrolled = (courseId: string) => {
  // Assuming myCourses contains course objects with string IDs
  return coursesSafe.value.some((c: StudentCourse) => String(c.id) === courseId);
};

function isAllowedImage(url: string): boolean {
  try {
    const u = new URL(url)
    // 仅允许与站点同源的图片，或 http(s) 且非已知失败域
    const sameOrigin = u.origin === window.location.origin
    const blockedHosts = ['via.placeholder.com']
    return sameOrigin || (!blockedHosts.includes(u.hostname) && /^https?:$/.test(u.protocol))
  } catch {
    return false
  }
}

// Lifecycle Hooks
onMounted(async () => {
  await studentStore.fetchMyCourses({ page: 1, size: 12, q: '' });
  pageLoaded.value = true;
  // 课程商店数据异步加载，不影响我的课程首屏
  void courseStore.fetchCourses({ page: 1, size: 20 });
});
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
