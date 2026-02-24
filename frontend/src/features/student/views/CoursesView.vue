<template>
  <div class="p-6">
    <page-header :title="t('student.courses.title')" :subtitle="t('student.courses.subtitle')">
      <template #actions>
        <button variant="primary" @click="showCourseStore = true">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" /></svg>
          {{ t('student.courses.browse') }}
        </button>
      </template>
    </page-header>

    <!-- Stats (StatCard, 复用工作台四卡样式) -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <start-card :label="t('student.courses.active') as string" :value="activeCoursesCount" tone="blue" :icon="AcademicCapIcon" />
      <start-card :label="t('student.courses.completed') as string" :value="completedCoursesCount" tone="emerald" :icon="CheckCircleIcon" />
      <start-card :label="t('student.courses.avgProgress') as string" :value="`${averageProgress.toFixed(1)}%`" tone="violet" :icon="ChartBarIcon" />
    </div>

    <!-- Search and Filter (aligned with Assignments FilterBar) -->
    <div class="mb-8">
      <filter-bar tint="secondary" class="rounded-full">
        <template #left>
          <div class="flex items-center gap-4">
            <div class="w-auto flex items-center gap-2">
              <span class="text-xs font-medium leading-tight text-gray-700 dark:text-gray-300">{{ t('student.courses.categoryLabel') || '分类' }}</span>
              <div class="w-56">
                <glass-popover-select
                  v-model="selectedCategory"
                  :options="[{ label: t('student.courses.allCategories') as string, value: '' }, ...categoryOptions]"
                  size="sm"
                  :placeholder="t('student.courses.allCategories') as string"
                  tint="primary"
                />
              </div>
            </div>
            <div class="w-auto flex items-center gap-2">
              <span class="text-xs font-medium leading-tight text-gray-700 dark:text-gray-300">{{ t('student.courses.statusFilterLabel') || '状态' }}</span>
              <div class="w-44">
                <glass-popover-select
                  v-model="selectedStatus"
                  :options="statusFilterOptions"
                  size="sm"
                  tint="accent"
                />
              </div>
            </div>
            <div class="w-auto flex items-center gap-2">
              <span class="text-xs font-medium leading-tight text-gray-700 dark:text-gray-300">{{ t('student.courses.sortDifficultyLabel') || '难度排序' }}</span>
              <div class="w-48">
                <glass-popover-select
                  v-model="difficultyOrder"
                  :options="difficultyOrderOptions"
                  size="sm"
                  tint="secondary"
                />
              </div>
            </div>
          </div>
        </template>
        <template #right>
          <div class="relative w-56 ml-auto">
            <glass-search-input v-model="searchQuery" :placeholder="t('student.courses.searchPlaceholder') as string" size="sm" tint="info" @keyup.enter="applyImmediateSearch()" />
          </div>
        </template>
      </filter-bar>
    </div>
    
    <!-- Loading or Empty State -->
    <div v-if="!pageLoaded" class="text-center py-12">
      <p>{{ t('student.courses.loading') }}</p>
    </div>
    <card v-else-if="filteredCourses.length === 0" class="text-center py-12" tint="info">
      <h3 class="text-lg font-medium">{{ t('student.courses.emptyTitle') }}</h3>
      <p class="text-gray-500 mt-2">{{ t('student.courses.emptyDesc') }}</p>
      <button class="mt-4" variant="primary" @click="showCourseStore = true">{{ t('student.courses.goStore') }}</button>
    </card>
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <card
        v-for="course in filteredCourses"
        :key="course.id"
        class="overflow-hidden cursor-pointer group rounded-2xl"
        padding="none"
        :hoverable="true"
        tint="primary"
        @click="enterCourse(course)"
      >
        <div class="relative h-48 rounded-2xl overflow-hidden">
          <img v-if="getCoverSrc(course) && !coverErrorMap[String(course.id)]"
               :src="getCoverSrc(course)"
               :alt="course.title || ''"
               class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300 rounded-2xl"
               @error="coverErrorMap[String(course.id)] = true"
          />
          <div v-else class="w-full h-full bg-gradient-to-br from-[var(--color-primary)] to-[var(--color-accent)] flex items-center justify-center">
            <span class="text-4xl font-bold text-white">{{ (course.title || '').charAt(0) }}</span>
          </div>
          <div class="absolute bottom-0 left-0 right-0 bg-black bg-opacity-50 p-4">
            <div class="flex items-center justify-between text-white text-sm mb-2">
              <span>{{ t('student.courses.progressLabel') }}</span>
              <span>{{ (Number(course.progress) || 0).toFixed(0) }}%</span>
            </div>
            <progress
              :value="Number(course.progress || 0)"
              size="sm"
              :color="Number(course.progress || 0) >= 100 ? 'success' : 'primary'"
            />
          </div>
        </div>
        <div class="p-6">
          <h3 class="text-lg font-semibold line-clamp-2 mb-2">{{ course.title }}</h3>
          <p class="text-muted text-sm mb-4 line-clamp-2 whitespace-pre-line">{{ course.description }}</p>
          <div class="flex items-center justify-between text-sm">
            <span class="text-muted">{{ t('student.courses.instructor') }}: {{ course.teacherName }}</span>
            <span class="font-medium" :class="course.progress === 100 ? 'text-[var(--color-success)]' : 'text-[var(--color-primary)]'">
              {{ course.progress === 100 ? t('student.courses.statusCompleted') : t('student.courses.statusOngoing') }}
            </span>
          </div>
        </div>
      </card>
    </div>

    <!-- Course Store Modal (GlassModal) -->
    <glass-modal v-if="showCourseStore" :title="t('student.courses.storeTitle') as string" size="md" heightVariant="tall" @close="showCourseStore=false">
      <div class="overflow-y-auto" style="max-height:70vh;">
        <div v-if="courseStore.loading" class="text-center"><p>{{ t('student.courses.loading') }}</p></div>
        <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <card v-for="course in courseStore.courses" :key="course.id">
            <h4 class="font-medium mb-2">{{ course.title }}</h4>
            <p class="text-sm text-gray-600 mb-3 whitespace-pre-line">{{ course.description }}</p>
            <div class="flex flex-wrap items-center gap-2 mb-2 text-sm">
              <badge v-if="course.startDate" size="sm" variant="info">
                <span class="inline-flex items-center gap-1">📅 {{ t('student.courses.detail.startDate') }}: {{ formatDateOnly(course.startDate) }}</span>
              </badge>
              <badge v-if="course.endDate" size="sm" variant="info">
                <span class="inline-flex items-center gap-1">⏳ {{ t('student.courses.detail.endDate') }}: {{ formatDateOnly(course.endDate) }}</span>
              </badge>
            </div>
            <div class="flex flex-wrap items-center gap-2 mb-3 text-sm">
              <badge v-if="course.difficulty" size="sm" :variant="getDifficultyVariant(course.difficulty)">
                {{ t('student.courses.detail.difficulty') }}: {{ localizeDifficulty(course.difficulty, t) }}
              </badge>
              <badge v-if="course.category" size="sm" :variant="getCategoryVariant(course.category)">
                {{ t('student.courses.detail.category') }}: {{ localizeCategory(course.category, t) }}
              </badge>
              <template v-for="tag in toTagArray(course.tags)" :key="`${course.id}-${tag}`">
                <badge size="sm" :variant="getTagVariant(tag)">#{{ tag }}</badge>
              </template>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-sm text-gray-500">{{ t('student.courses.instructor') }}: {{ course.teacherName }}</span>
              <div class="flex items-center gap-2">
                <badge v-if="isEnrollClosed(course)" size="sm" variant="warning">{{ t('student.courses.enrollClosed') || '报名已截止' }}</badge>
                <button
                  v-if="isEnrolled(String(course.id))"
                  size="sm"
                  variant="danger"
                  :loading="enrollingId === `un-${String(course.id)}`"
                  @click.stop="handleUnenroll(String(course.id))"
                >
                  {{ t('student.courses.unenroll') || '退课' }}
                </button>
                <button
                  v-else
                  size="sm"
                  variant="primary"
                  :loading="enrollingId === String(course.id)"
                  :disabled="isEnrollClosed(course) || enrollingId === String(course.id)"
                  @click.stop="startEnroll(course)"
                >
                  {{ isEnrollClosed(course) ? (t('student.courses.enrollClosed') || '报名已截止') : (t('student.courses.enroll') ) }}
                </button>
              </div>
            </div>
          </card>
        </div>
      </div>
      <template #footer>
        <button variant="secondary" @click="showCourseStore=false">{{ t('common.close') || '关闭' }}</button>
      </template>
    </glass-modal>

    <!-- 入课密钥输入弹窗 -->
    <glass-modal
      v-if="enrollKeyModalVisible"
      :title="(t('student.courses.enterEnrollKey') as string) || '输入入课密钥'"
      size="sm"
      heightVariant="compact"
      @close="closeEnrollKeyModal"
    >
      <div class="space-y-4">
        <div class="text-sm text-muted">{{ t('teacher.students.enrollKey.tip') }}</div>
        <label class="block text-sm mb-1">{{ t('teacher.students.enrollKey.keyLabel') }}</label>
        <glass-input
          v-model="enrollKeyInput"
          type="password"
          :placeholder="t('teacher.students.enrollKey.placeholder') as string"
        />
      </div>
      <template #footer>
        <button variant="secondary" @click="closeEnrollKeyModal">{{ t('common.cancel') || '取消' }}</button>
        <button
          variant="primary"
          :loading="enrollingId === String(enrollKeyCourseId)"
          :disabled="!enrollKeyInput"
          @click="confirmEnrollWithKey"
        >
          {{ t('student.courses.enroll') }}
        </button>
      </template>
    </glass-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router';
import { useStudentStore } from '@/stores/student';
import { useCourseStore } from '@/stores/course';
import type { StudentCourse } from '@/types/student';
import { useI18n } from 'vue-i18n'
import Button from '@/components/ui/Button.vue'
import Card from '@/components/ui/Card.vue'
import StartCard from '@/components/ui/StartCard.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import FilterBar from '@/components/ui/filters/FilterBar.vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import Progress from '@/components/ui/Progress.vue'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import Badge from '@/components/ui/Badge.vue'
import { getDifficultyVariant, getCategoryVariant, getTagVariant } from '@/shared/utils/badgeColor'
import { localizeDifficulty, localizeCategory } from '@/shared/utils/localize'
import apiClient from '@/api/config'
import { AcademicCapIcon, CheckCircleIcon, ChartBarIcon } from '@heroicons/vue/24/outline'

const router = useRouter();
const { t, locale } = useI18n()
const studentStore = useStudentStore();
const courseStore = useCourseStore();

// State
const showCourseStore = ref(false);
const enrollingId = ref<string | null>(null);
const enrollKeyModalVisible = ref(false)
const enrollKeyCourseId = ref<string | null>(null)
const enrollKeyInput = ref('')
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
const selectedStatus = ref<'all'|'active'|'completed'>('all')
const statusFilterOptions = computed(() => ([
  { label: (t('student.courses.statusAll') as string) || '全部', value: 'all' },
  { label: (t('student.courses.statusOngoing') as string) || '进行中', value: 'active' },
  { label: (t('student.courses.statusCompleted') as string) || '已完成', value: 'completed' }
]))
const difficultyOrder = ref<'none'|'asc'|'desc'>('none')
const difficultyOrderOptions = computed(() => ([
  { label: (t('student.courses.sortNone') as string) || '默认', value: 'none' },
  { label: (t('student.courses.sortDifficultyAsc') as string) || '难度从低到高', value: 'asc' },
  { label: (t('student.courses.sortDifficultyDesc') as string) || '难度从高到低', value: 'desc' }
]))

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
  const filtered = list.filter((course: StudentCourse) => {
    const title = (course.title || '').toLowerCase();
    const searchMatch = q === '' || title.includes(q);
    const categoryMatch = cat === '' || course.category === cat;
    const statusMatch = selectedStatus.value === 'all'
      ? true
      : selectedStatus.value === 'completed'
        ? Number(course.progress || 0) >= 100
        : Number(course.progress || 0) < 100
    return searchMatch && categoryMatch && statusMatch;
  });
  // sort by difficulty if needed
  if (difficultyOrder.value !== 'none') {
    const order = difficultyOrder.value
    const rank = (d?: string) => {
      const v = String(d || '').toLowerCase()
      if (v.startsWith('beginner') || v.includes('入门') || v.includes('初级')) return 1
      if (v.startsWith('intermediate') || v.includes('中级')) return 2
      if (v.startsWith('advanced') || v.includes('高级')) return 3
      return 0
    }
    filtered.sort((a: any, b: any) => {
      const ra = rank((a.difficulty || a.level))
      const rb = rank((b.difficulty || b.level))
      return order === 'asc' ? (ra - rb) : (rb - ra)
    })
  }
  return filtered
});

// Methods
const enterCourse = (course: StudentCourse) => {
  router.push(`/student/courses/${course.id}`);
};


function startEnroll(course: any) {
  const id = String(course?.id || '')
  if (!id) return
  if (course && course.requireEnrollKey) {
    enrollKeyCourseId.value = id
    enrollKeyInput.value = ''
    enrollKeyModalVisible.value = true
  } else {
    handleEnroll(id)
  }
}

const handleEnroll = async (courseId: string) => {
  enrollingId.value = courseId;
  await courseStore.enrollInCourse(courseId);
  await studentStore.fetchMyCourses();
  enrollingId.value = null;
};

function closeEnrollKeyModal() {
  enrollKeyModalVisible.value = false
  enrollKeyInput.value = ''
  enrollKeyCourseId.value = null
}

const confirmEnrollWithKey = async () => {
  const id = String(enrollKeyCourseId.value || '')
  if (!id || !enrollKeyInput.value) return
  enrollingId.value = id
  try {
    await courseStore.enrollInCourse(id, enrollKeyInput.value)
    await studentStore.fetchMyCourses()
    enrollKeyModalVisible.value = false
  } finally {
    enrollingId.value = null
    enrollKeyInput.value = ''
    enrollKeyCourseId.value = null
  }
}

const isEnrolled = (courseId: string) => {
  // Assuming myCourses contains course objects with string IDs
  return coursesSafe.value.some((c: StudentCourse) => String(c.id) === courseId);
};

function isEnrollClosed(course: any): boolean {
  try {
    const end = course?.endDate
    if (!end) return false
    const d = new Date(end)
    if (Number.isNaN(d.getTime())) return false
    const today = new Date()
    // 比较日期部分
    const endDateOnly = new Date(d.getFullYear(), d.getMonth(), d.getDate())
    const todayOnly = new Date(today.getFullYear(), today.getMonth(), today.getDate())
    return todayOnly.getTime() > endDateOnly.getTime()
  } catch { return false }
}

const handleUnenroll = async (courseId: string) => {
  const ok = window.confirm(String(t('student.courses.confirmUnenroll') || '确定要退课吗？'))
  if (!ok) return
  enrollingId.value = `un-${courseId}`
  await courseStore.unenrollFromCourse(courseId)
  await studentStore.fetchMyCourses()
  enrollingId.value = null
}

const coverBlobMap = ref<Record<string, string>>({})
function getCoverSrc(course: any): string {
  const raw = String(course?.coverImageUrl || '')
  if (!raw || coverErrorMap.value[String(course.id)]) return ''
  // http(s) 直链
  if (/^https?:\/\//i.test(raw)) return raw
  // 文件ID → 预览blob
  const key = `${course.id}|${raw}`
  if (coverBlobMap.value[key]) return coverBlobMap.value[key]
  // 异步拉取blob并缓存
  apiClient
    .get(`/files/${encodeURIComponent(raw)}/preview`, { responseType: 'blob' })
    .then((res: any) => {
      const url = URL.createObjectURL(res)
      coverBlobMap.value[key] = url
    })
    .catch(() => {
      // 预览失败（可能非 image/pdf），回退到 download 接口
      apiClient
        .get(`/files/${encodeURIComponent(raw)}/download`, { responseType: 'blob' })
        .then((res: any) => {
          const url = URL.createObjectURL(res)
          coverBlobMap.value[key] = url
        })
        .catch(() => { coverErrorMap.value[String(course.id)] = true })
    })
  return ''
}

function formatDateOnly(v: any): string {
  try {
    const d = new Date(v)
    if (Number.isNaN(d.getTime())) return String(v)
    const lang = String(locale.value || 'zh-CN').toLowerCase()
    const isZh = lang.startsWith('zh')
    if (isZh) {
      const y = d.getFullYear(); const m = d.getMonth() + 1; const day = d.getDate()
      return `${y}年${m}月${day}日`
    }
    return d.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })
  } catch { return String(v) }
}

function toTagArray(raw: any): string[] {
  if (!raw) return []
  if (Array.isArray(raw)) return raw.filter(Boolean).map((x: any) => String(x))
  if (typeof raw === 'string') return raw.split(',').map(s => s.trim()).filter(Boolean)
  return []
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
