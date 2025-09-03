<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- 页面头部 -->
    <div class="bg-white dark:bg-gray-800 shadow-sm">
      <div class="max-w-7xl mx-auto px-4 py-6">
        <div class="flex flex-col md:flex-row md:items-center md:justify-between">
          <div class="w-full">
            <PageHeader :title="t('shared.discovery.title') || '课程发现'" :subtitle="t('shared.discovery.subtitle') || '发现优质课程，开启学习之旅'" />
          </div>
          
          <!-- 搜索框 -->
          <div class="mt-4 md:mt-0 w-full md:w-96">
            <div class="relative">
              <magnifying-glass-icon class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                v-model="searchQuery"
                @keyup.enter="handleSearch"
                type="text"
                placeholder="搜索课程..."
                class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white dark:placeholder-gray-400"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 py-8">
      <!-- 分类导航 -->
      <div class="mb-8">
        <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-4">课程分类</h2>
        <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-8 gap-4">
          <div
            v-for="category in categories"
            :key="category.id"
            @click="handleSelectCategory(category)"
            class="category-card"
            :class="{ 'category-active': selectedCategory?.id === category.id }"
          >
            <div class="category-icon">
              <component :is="getCategoryIcon(category.id)" class="w-6 h-6" />
            </div>
            <h3 class="category-name">{{ category.name }}</h3>
            <p class="category-count">{{ category.courseCount }}门课程</p>
          </div>
        </div>
      </div>

      <!-- 热门课程 -->
      <div class="mb-8">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-semibold text-gray-900 dark:text-white">热门课程</h2>
          <button
            @click="viewAllPopular"
            class="text-blue-600 hover:text-blue-700 font-medium flex items-center"
          >
            查看全部
            <chevron-right-icon class="ml-1 w-4 h-4" />
          </button>
        </div>

        <div v-if="discoveryLoading.popular" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div v-for="i in 4" :key="i" class="course-card-skeleton">
            <div class="animate-pulse">
              <div class="h-48 bg-gray-300 rounded-t-lg"></div>
              <div class="p-4 space-y-3">
                <div class="h-4 bg-gray-300 rounded w-3/4"></div>
                <div class="h-3 bg-gray-300 rounded w-1/2"></div>
                <div class="h-3 bg-gray-300 rounded w-2/3"></div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div
            v-for="course in popularCourses"
            :key="course.id"
            @click="viewCourse(course)"
            class="course-card"
          >
            <div class="course-thumbnail">
              <img
                :src="course.thumbnail || '/placeholder-course.jpg'"
                :alt="course.title"
                class="w-full h-48 object-cover"
              />
              <div class="course-badge">
                <fire-icon class="w-4 h-4 text-orange-500" />
                <span class="text-orange-600 font-medium">热门</span>
              </div>
            </div>
            
            <div class="course-content">
              <h3 class="course-title">{{ course.title }}</h3>
              <p class="course-instructor">{{ course.teacherName }}</p>
              
              <div class="course-stats">
                <div class="flex items-center">
                  <star-icon class="w-4 h-4 text-yellow-400 fill-current" />
                  <span class="ml-1 text-sm text-gray-600">{{ course.rating }}</span>
                  <span class="ml-1 text-sm text-gray-500">({{ course.reviewCount }})</span>
                </div>
                <div class="flex items-center text-sm text-gray-500">
                  <users-icon class="w-4 h-4 mr-1" />
                  {{ formatStudentCount(course.enrollmentCount) }}
                </div>
              </div>
              
              <div class="course-footer">
                <div class="course-price">
                  <span v-if="course.price === 0" class="text-green-600 font-semibold">免费</span>
                  <span v-else class="text-gray-900 font-semibold">¥{{ course.price }}</span>
                </div>
                <div class="course-level">
                  <span class="level-badge" :class="getLevelClass(course.level)">
                    {{ getLevelText(course.level) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 推荐课程 -->
      <div class="mb-8">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-semibold text-gray-900 dark:text-white">为您推荐</h2>
          <button
            @click="refreshRecommended"
            class="text-blue-600 hover:text-blue-700 font-medium flex items-center"
          >
            换一批
            <arrow-path-icon class="ml-1 w-4 h-4" />
          </button>
        </div>

        <div v-if="discoveryLoading.recommended" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div v-for="i in 3" :key="i" class="course-card-skeleton">
            <div class="animate-pulse">
              <div class="h-48 bg-gray-300 rounded-t-lg"></div>
              <div class="p-4 space-y-3">
                <div class="h-4 bg-gray-300 rounded w-3/4"></div>
                <div class="h-3 bg-gray-300 rounded w-1/2"></div>
                <div class="h-3 bg-gray-300 rounded w-2/3"></div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
            v-for="course in recommendedCourses"
            :key="course.id"
            @click="viewCourse(course)"
            class="course-card"
          >
            <div class="course-thumbnail">
              <img
                :src="course.thumbnail || '/placeholder-course.jpg'"
                :alt="course.title"
                class="w-full h-48 object-cover"
              />
              <div class="course-badge bg-blue-500 text-white">
                <sparkles-icon class="w-4 h-4" />
                <span class="font-medium">推荐</span>
              </div>
            </div>
            
            <div class="course-content">
              <h3 class="course-title">{{ course.title }}</h3>
              <p class="course-instructor">{{ course.teacherName }}</p>
              <p class="course-description">{{ course.description }}</p>
              
              <div class="course-tags">
                <span
                  v-for="tag in course.tags?.slice(0, 3)"
                  :key="tag"
                  class="tag"
                >
                  {{ tag }}
                </span>
              </div>
              
              <div class="course-footer">
                <div class="course-price">
                  <span v-if="course.price === 0" class="text-green-600 font-semibold">免费</span>
                  <span v-else class="text-gray-900 font-semibold">¥{{ course.price }}</span>
                </div>
                <div class="course-duration">
                  <clock-icon class="w-4 h-4 text-gray-500" />
                  <span class="text-sm text-gray-500 ml-1">{{ formatDuration(course.duration) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分类课程列表 -->
      <div v-if="selectedCategory" class="mb-8">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-semibold text-gray-900 dark:text-white">
            {{ selectedCategory.name }}课程
          </h2>
          <button
            @click="clearCategoryFilter"
            class="text-gray-600 hover:text-gray-800 flex items-center"
          >
            <x-mark-icon class="w-4 h-4 mr-1" />
            清除筛选
          </button>
        </div>

        <div v-if="discoveryLoading.categoryCourses" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div v-for="i in 6" :key="i" class="course-card-skeleton">
            <div class="animate-pulse">
              <div class="h-48 bg-gray-300 rounded-t-lg"></div>
              <div class="p-4 space-y-3">
                <div class="h-4 bg-gray-300 rounded w-3/4"></div>
                <div class="h-3 bg-gray-300 rounded w-1/2"></div>
                <div class="h-3 bg-gray-300 rounded w-2/3"></div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="categoryCourses.length === 0" class="text-center py-12">
          <academic-cap-icon class="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <p class="text-gray-500">该分类下暂无课程</p>
        </div>

        <div v-else>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div
              v-for="course in categoryCourses"
              :key="course.id"
              @click="viewCourse(course)"
              class="course-card"
            >
              <div class="course-thumbnail">
                <img
                  :src="course.thumbnail || '/placeholder-course.jpg'"
                  :alt="course.title"
                  class="w-full h-48 object-cover"
                />
              </div>
              
              <div class="course-content">
                <h3 class="course-title">{{ course.title }}</h3>
                <p class="course-instructor">{{ course.teacherName }}</p>
                <p class="course-description">{{ course.description }}</p>
                
                <div class="course-footer">
                  <div class="course-price">
                    <span v-if="course.price === 0" class="text-green-600 font-semibold">免费</span>
                    <span v-else class="text-gray-900 font-semibold">¥{{ course.price }}</span>
                  </div>
                  <div class="course-level">
                    <span class="level-badge" :class="getLevelClass(course.level)">
                      {{ getLevelText(course.level) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 分页 -->
          <div v-if="categoryPagination.totalPages > 1" class="flex justify-center mt-8">
            <div class="flex items-center space-x-2">
              <button
                @click="handleCategoryPageChange(categoryPagination.page - 1)"
                :disabled="categoryPagination.page <= 1"
                class="pagination-btn"
              >
                上一页
              </button>
              <span class="text-sm text-gray-600">
                第 {{ categoryPagination.page }} 页，共 {{ categoryPagination.totalPages }} 页
              </span>
              <button
                @click="handleCategoryPageChange(categoryPagination.page + 1)"
                :disabled="categoryPagination.page >= categoryPagination.totalPages"
                class="pagination-btn"
              >
                下一页
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia';
import { useCourseStore } from '@/stores/course';
import type { Course, CourseCategory } from '@/types/course'
import PageHeader from '@/components/ui/PageHeader.vue'
import { useI18n } from 'vue-i18n'
import {
  MagnifyingGlassIcon,
  ChevronRightIcon,
  FireIcon,
  StarIcon,
  UsersIcon,
  SparklesIcon,
  ClockIcon,
  XMarkIcon,
  ArrowPathIcon,
  AcademicCapIcon,
  CodeBracketIcon,
  PaintBrushIcon,
  BriefcaseIcon,
  SpeakerWaveIcon,
  GlobeAltIcon,
  BeakerIcon,
  MusicalNoteIcon,
  EllipsisHorizontalIcon
} from '@heroicons/vue/24/outline'

// Store setup
const router = useRouter()
const courseStore = useCourseStore()
const {
  popularCourses,
  recommendedCourses,
  categories,
  categoryCourses,
  categoryPagination,
  discoveryLoading,
} = storeToRefs(courseStore);

// Local component state
const searchQuery = ref('')
const selectedCategory = ref<CourseCategory | null>(null)

// Methods delegating to store or router
const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push(`/courses/search?q=${encodeURIComponent(searchQuery.value)}`)
  }
}

const handleSelectCategory = async (category: CourseCategory) => {
  selectedCategory.value = category
  courseStore.categoryPagination.page = 1 // Reset page for new category
  await courseStore.fetchCoursesByCategory(category.id)
}

const clearCategoryFilter = () => {
  selectedCategory.value = null
}

const handleCategoryPageChange = async (page: number) => {
  if (selectedCategory.value) {
    await courseStore.changeCategoryPage(selectedCategory.value.id, page)
  }
}

const viewCourse = (course: Course) => {
  router.push(`/courses/${course.id}`)
}

const viewAllPopular = () => {
  router.push('/courses?sort=popular')
}

const refreshRecommended = async () => {
  await courseStore.fetchRecommendedCourses()
}

// UI Helper functions
const getCategoryIcon = (categoryId: string) => {
  const icons: Record<string, any> = {
    programming: CodeBracketIcon,
    design: PaintBrushIcon,
    business: BriefcaseIcon,
    marketing: SpeakerWaveIcon,
    language: GlobeAltIcon,
    science: BeakerIcon,
    art: MusicalNoteIcon,
    other: EllipsisHorizontalIcon
  }
  return icons[categoryId] || EllipsisHorizontalIcon
}

const formatStudentCount = (count: number) => {
  if (count >= 10000) {
    return `${Math.floor(count / 10000)}万+`
  } else if (count >= 1000) {
    return `${Math.floor(count / 100) / 10}k+`
  }
  return count.toString()
}

const formatDuration = (minutes: number) => {
  if (!minutes) return 'N/A';
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours === 0) {
    return `${mins}分钟`
  }
  return `${hours}小时${mins > 0 ? mins + '分钟' : ''}`
}

const getLevelClass = (level: string) => {
  const classes: Record<string, string> = {
    beginner: 'bg-green-100 text-green-800',
    intermediate: 'bg-yellow-100 text-yellow-800',
    advanced: 'bg-red-100 text-red-800'
  }
  return classes[level] || 'bg-gray-100 text-gray-800'
}

const getLevelText = (level: string) => {
  const texts: Record<string, string> = {
    beginner: '初级',
    intermediate: '中级',
    advanced: '高级'
  }
  return texts[level] || level
}

// Initial data loading
onMounted(() => {
  courseStore.fetchCategories()
  courseStore.fetchPopularCourses()
  courseStore.fetchRecommendedCourses()
})
</script>

<style scoped lang="postcss">
.category-card {
  @apply p-4 bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 cursor-pointer transition-all duration-200 hover:shadow-md hover:border-blue-300;
}

.category-active {
  @apply border-blue-500 bg-blue-50 dark:bg-blue-900/20;
}

.category-icon {
  @apply w-12 h-12 mx-auto mb-3 p-2 bg-gray-100 dark:bg-gray-700 rounded-lg flex items-center justify-center;
}

.category-name {
  @apply text-sm font-medium text-gray-900 dark:text-white text-center mb-1;
}

.category-count {
  @apply text-xs text-gray-500 dark:text-gray-400 text-center;
}

.course-card {
  @apply bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden cursor-pointer transition-all duration-200 hover:shadow-lg hover:-translate-y-1;
}

.course-card-skeleton {
  @apply bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden;
}

.course-thumbnail {
  @apply relative;
}

.course-badge {
  @apply absolute top-3 left-3 px-2 py-1 rounded-full text-xs font-medium flex items-center space-x-1;
}

.course-content {
  @apply p-4;
}

.course-title {
  @apply font-semibold text-gray-900 dark:text-white mb-2 line-clamp-2 leading-tight;
}

.course-instructor {
  @apply text-sm text-gray-600 dark:text-gray-400 mb-2;
}

.course-description {
  @apply text-sm text-gray-600 dark:text-gray-400 mb-3 line-clamp-2;
}

.course-stats {
  @apply flex items-center justify-between mb-3;
}

.course-tags {
  @apply flex flex-wrap gap-1 mb-3;
}

.tag {
  @apply px-2 py-1 bg-gray-100 dark:bg-gray-700 text-xs text-gray-600 dark:text-gray-400 rounded;
}

.course-footer {
  @apply flex items-center justify-between;
}

.course-price {
  @apply text-lg font-bold;
}

.course-duration {
  @apply flex items-center;
}

.level-badge {
  @apply px-2 py-1 text-xs font-medium rounded;
}

.pagination-btn {
  @apply px-3 py-2 bg-white border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed;
}
</style> 
