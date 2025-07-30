<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <!-- 页面标题 -->
    <div class="mb-8">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">我的课程</h1>
          <p class="text-gray-600 dark:text-gray-400">浏览和管理您的学习课程</p>
        </div>
        <div class="flex items-center space-x-3">
          <button variant="outline" @click="showArchived = !showArchived">
            <archive-box-icon class="w-4 h-4 mr-2" />
            {{ showArchived ? '隐藏已归档' : '显示已归档' }}
          </button>
          <button variant="primary" @click="showCourseStore = true">
            <plus-icon class="w-4 h-4 mr-2" />
            浏览课程
          </button>
        </div>
      </div>
    </div>

    <!-- 课程统计 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
      <card padding="lg" class="text-center">
        <div class="text-2xl font-bold text-blue-600 dark:text-blue-400 mb-1">
          {{ activeCourses.length }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">进行中</p>
      </card>

      <card padding="lg" class="text-center">
        <div class="text-2xl font-bold text-green-600 dark:text-green-400 mb-1">
          {{ completedCourses.length }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">已完成</p>
      </card>

      <card padding="lg" class="text-center">
        <div class="text-2xl font-bold text-purple-600 dark:text-purple-400 mb-1">
          {{ averageProgress.toFixed(1) }}%
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">平均进度</p>
      </card>

      <card padding="lg" class="text-center">
        <div class="text-2xl font-bold text-orange-600 dark:text-orange-400 mb-1">
          {{ totalStudyHours }}h
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">学习时长</p>
      </card>
    </div>

    <!-- 搜索和筛选 -->
    <card padding="lg" class="mb-8">
      <div class="flex flex-col md:flex-row md:items-center space-y-4 md:space-y-0 md:space-x-4">
        <!-- 搜索框 -->
        <div class="flex-1 relative">
          <magnifying-glass-icon class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索课程..."
            class="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          />
        </div>

        <!-- 分类筛选 -->
        <select v-model="selectedCategory" class="input md:w-48">
          <option value="">全部分类</option>
          <option v-for="category in categories" :key="category" :value="category">
            {{ category }}
          </option>
        </select>

        <!-- 状态筛选 -->
        <select v-model="selectedStatus" class="input md:w-48">
          <option value="">全部状态</option>
          <option value="active">进行中</option>
          <option value="completed">已完成</option>
          <option value="paused">已暂停</option>
        </select>

        <!-- 排序方式 -->
        <select v-model="sortBy" class="input md:w-48">
          <option value="recent">最近学习</option>
          <option value="progress">学习进度</option>
          <option value="name">课程名称</option>
          <option value="startDate">开始时间</option>
        </select>
      </div>
    </card>

    <!-- 加载状态 -->
    <div v-if="loading" class="flex justify-center items-center h-64">
      <div class="animate-spin rounded-full h-16 w-16 border-b-2 border-primary-500"></div>
    </div>

    <!-- 课程列表 -->
    <div v-else>
      <!-- 视图切换 -->
      <div class="flex justify-between items-center mb-6">
        <div class="flex items-center space-x-2">
          <button
            @click="viewMode = 'grid'"
            :class="[
              'p-2 rounded-lg transition-colors',
              viewMode === 'grid' 
                ? 'bg-primary-100 dark:bg-primary-900 text-primary-600 dark:text-primary-400' 
                : 'text-gray-400 hover:text-gray-600 dark:hover:text-gray-300'
            ]"
          >
            <squares2-x2-icon class="w-5 h-5" />
          </button>
          <button
            @click="viewMode = 'list'"
            :class="[
              'p-2 rounded-lg transition-colors',
              viewMode === 'list' 
                ? 'bg-primary-100 dark:bg-primary-900 text-primary-600 dark:text-primary-400' 
                : 'text-gray-400 hover:text-gray-600 dark:hover:text-gray-300'
            ]"
          >
            <bars3-icon class="w-5 h-5" />
          </button>
        </div>
        
        <p class="text-sm text-gray-500 dark:text-gray-400">
          显示 {{ filteredCourses.length }} 门课程
        </p>
      </div>

      <!-- 网格视图 -->
      <div v-if="viewMode === 'grid'" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <card 
          v-for="course in filteredCourses" 
          :key="course.id"
          hoverable
          class="overflow-hidden cursor-pointer group"
          @click="enterCourse(course)"
        >
          <!-- 课程封面 -->
          <div class="relative h-48 overflow-hidden">
            <img
              v-if="course.coverUrl"
              :src="course.coverUrl"
              :alt="course.title"
              class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            />
            <div
              v-else
              class="w-full h-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center"
            >
              <span class="text-4xl font-bold text-white">{{ course.title.charAt(0) }}</span>
            </div>
            
            <!-- 状态标签 -->
            <div class="absolute top-4 right-4">
              <badge :variant="getStatusVariant(course.status)" size="sm">
                {{ getStatusText(course.status) }}
              </badge>
            </div>
            
            <!-- 进度条 -->
            <div class="absolute bottom-0 left-0 right-0 bg-black bg-opacity-50 p-4">
              <div class="flex items-center justify-between text-white text-sm mb-2">
                <span>学习进度</span>
                <span>{{ Math.round(course.progress) }}%</span>
              </div>
              <progress :value="course.progress" color="primary" size="sm" />
            </div>
          </div>

          <!-- 课程信息 -->
          <div class="p-6">
            <div class="flex items-start justify-between mb-2">
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white line-clamp-2">
                {{ course.title }}
              </h3>
              <button
                @click.stop="toggleFavorite(course)"
                class="p-1 rounded-full hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
              >
                <heart-icon 
                  :class="[
                    'w-5 h-5',
                    course.isFavorite 
                      ? 'text-red-500 fill-current' 
                      : 'text-gray-400'
                  ]"
                />
              </button>
            </div>
            
            <p class="text-gray-600 dark:text-gray-400 text-sm mb-4 line-clamp-2">
              {{ course.description }}
            </p>
            
            <div class="space-y-3">
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-500 dark:text-gray-400">讲师:</span>
                <span class="text-gray-900 dark:text-white">{{ course.instructor }}</span>
              </div>
              
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-500 dark:text-gray-400">课时:</span>
                <span class="text-gray-900 dark:text-white">
                  {{ course.completedLessons }}/{{ course.totalLessons }}
                </span>
              </div>
              
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-500 dark:text-gray-400">学习时长:</span>
                <span class="text-gray-900 dark:text-white">{{ course.studyHours }}h</span>
              </div>
            </div>
            
            <div class="mt-4 flex space-x-2">
              <button 
                v-if="course.status === 'active'"
                variant="primary" 
                size="sm" 
                class="flex-1"
                @click.stop="continueLearning(course)"
              >
                继续学习
              </button>
              <button 
                v-else-if="course.status === 'completed'"
                variant="success" 
                size="sm" 
                class="flex-1"
                @click.stop="reviewCourse(course)"
              >
                复习课程
              </button>
              <button 
                v-else
                variant="outline" 
                size="sm" 
                class="flex-1"
                @click.stop="resumeCourse(course)"
              >
                恢复学习
              </button>
              
              <button 
                variant="outline" 
                size="sm"
                @click.stop="showCourseMenu(course)"
              >
                <ellipsis-horizontal-icon class="w-4 h-4" />
              </button>
            </div>
          </div>
        </card>
      </div>

      <!-- 列表视图 -->
      <div v-else class="space-y-4">
        <card 
          v-for="course in filteredCourses" 
          :key="course.id"
          padding="lg"
          hoverable
          class="cursor-pointer"
          @click="enterCourse(course)"
        >
          <div class="flex items-center space-x-6">
            <!-- 课程封面 -->
            <div class="flex-shrink-0">
              <img
                v-if="course.coverUrl"
                :src="course.coverUrl"
                :alt="course.title"
                class="w-20 h-20 rounded-lg object-cover"
              />
              <div
                v-else
                class="w-20 h-20 rounded-lg bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center"
              >
                <span class="text-xl font-bold text-white">{{ course.title.charAt(0) }}</span>
              </div>
            </div>

            <!-- 课程信息 -->
            <div class="flex-1 min-w-0">
              <div class="flex items-start justify-between">
                <div class="flex-1">
                  <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-1">
                    {{ course.title }}
                  </h3>
                  <p class="text-gray-600 dark:text-gray-400 text-sm mb-2">{{ course.description }}</p>
                  <div class="flex items-center space-x-4 text-sm text-gray-500 dark:text-gray-400">
                    <span>{{ course.instructor }}</span>
                    <span>{{ course.completedLessons }}/{{ course.totalLessons }} 课时</span>
                    <span>{{ course.studyHours }}h</span>
                  </div>
                </div>
                
                <div class="flex items-center space-x-4">
                  <div class="text-right">
                    <badge :variant="getStatusVariant(course.status)" size="sm" class="mb-2">
                      {{ getStatusText(course.status) }}
                    </badge>
                    <div class="flex items-center space-x-2">
                      <progress :value="course.progress" color="primary" size="sm" class="w-24" />
                      <span class="text-sm text-gray-600 dark:text-gray-400">{{ Math.round(course.progress) }}%</span>
                    </div>
                  </div>
                  
                  <div class="flex space-x-2">
                    <button 
                      v-if="course.status === 'active'"
                      variant="primary" 
                      size="sm"
                      @click.stop="continueLearning(course)"
                    >
                      继续学习
                    </button>
                    <button 
                      variant="outline" 
                      size="sm"
                      @click.stop="showCourseMenu(course)"
                    >
                      <ellipsis-horizontal-icon class="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </card>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredCourses.length === 0" class="text-center py-12">
        <book-open-icon class="w-16 h-16 text-gray-400 mx-auto mb-4" />
        <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">
          {{ searchQuery ? '没有找到匹配的课程' : '还没有课程' }}
        </h3>
        <p class="text-gray-600 dark:text-gray-400 mb-4">
          {{ searchQuery ? '尝试修改搜索条件' : '开始您的学习之旅，浏览可用的课程' }}
        </p>
        <button variant="primary" @click="showCourseStore = true">
          <plus-icon class="w-4 h-4 mr-2" />
          浏览课程
        </button>
      </div>
    </div>

    <!-- 课程商店弹窗 -->
    <div 
      v-if="showCourseStore"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click.self="showCourseStore = false"
    >
      <div class="bg-white dark:bg-gray-800 rounded-lg w-full max-w-4xl mx-4 max-h-[90vh] overflow-hidden flex flex-col">
        <div class="p-6 border-b border-gray-200 dark:border-gray-600 flex justify-between items-center">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">课程商店</h3>
          <button variant="outline" size="sm" @click="showCourseStore = false">
            <x-mark-icon class="w-4 h-4" />
          </button>
        </div>
        
        <div class="p-6 overflow-y-auto flex-1">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div 
              v-for="course in availableCourses" 
              :key="course.id"
              class="border border-gray-200 dark:border-gray-600 rounded-lg p-4 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
            >
              <h4 class="font-medium text-gray-900 dark:text-white mb-2">{{ course.title }}</h4>
              <p class="text-sm text-gray-600 dark:text-gray-400 mb-3">{{ course.description }}</p>
              <div class="flex justify-between items-center">
                <span class="text-sm text-gray-500">{{ course.instructor }}</span>
                <button size="sm" @click="enrollCourse(course)">
                  加入课程
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCoursesStore } from '@/stores/courses'
import { useStudentStore } from '@/stores/student'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Progress from '@/components/ui/Progress.vue'
import type { Course } from '@/types/student'
import {
  MagnifyingGlassIcon,
  PlusIcon,
  BookOpenIcon,
  HeartIcon,
  EllipsisHorizontalIcon,
  XMarkIcon,
  Squares2X2Icon,
  Bars3Icon,
  ArchiveBoxIcon
} from '@heroicons/vue/24/outline'

// Stores & Router
const router = useRouter()
const coursesStore = useCoursesStore()
const studentStore = useStudentStore()
const uiStore = useUIStore()

// 状态
const loading = ref(false)
const searchQuery = ref('')
const selectedCategory = ref('')
const selectedStatus = ref('')
const sortBy = ref('recent')
const viewMode = ref<'grid' | 'list'>('grid')
const showArchived = ref(false)
const showCourseStore = ref(false)

// 模拟数据
const courses = ref<Course[]>([
  {
    id: '1',
    title: '高等数学基础',
    description: '掌握微积分、线性代数等数学基础知识，为后续专业课程打下坚实基础',
    instructor: '张教授',
    category: '数学',
    coverUrl: '',
    totalLessons: 24,
    completedLessons: 18,
    progress: 75,
    studyHours: 36,
    status: 'active',
    isFavorite: true,
    startDate: '2024-01-10T00:00:00Z',
    lastStudied: '2024-01-15T14:30:00Z'
  },
  {
    id: '2',
    title: '数据结构与算法',
    description: '学习常用数据结构和算法设计技巧，提升编程能力和逻辑思维',
    instructor: '李教授',
    category: '计算机科学',
    coverUrl: '',
    totalLessons: 32,
    completedLessons: 20,
    progress: 62.5,
    studyHours: 48,
    status: 'active',
    isFavorite: false,
    startDate: '2024-01-05T00:00:00Z',
    lastStudied: '2024-01-14T16:20:00Z'
  },
  {
    id: '3',
    title: '英语语法精讲',
    description: '系统学习英语语法规则，提高英语表达的准确性和流畅性',
    instructor: '王老师',
    category: '语言',
    coverUrl: '',
    totalLessons: 20,
    completedLessons: 20,
    progress: 100,
    studyHours: 25,
    status: 'completed',
    isFavorite: true,
    startDate: '2023-12-01T00:00:00Z',
    lastStudied: '2024-01-10T10:15:00Z'
  },
  {
    id: '4',
    title: '物理学导论',
    description: '介绍经典物理学基本概念和原理，培养科学思维方法',
    instructor: '赵教授',
    category: '物理',
    coverUrl: '',
    totalLessons: 28,
    completedLessons: 8,
    progress: 28.6,
    studyHours: 15,
    status: 'paused',
    isFavorite: false,
    startDate: '2024-01-08T00:00:00Z',
    lastStudied: '2024-01-12T09:30:00Z'
  }
])

const availableCourses = ref([
  {
    id: '5',
    title: '机器学习入门',
    description: '学习机器学习基础概念和常用算法',
    instructor: '陈教授',
    category: '人工智能'
  },
  {
    id: '6',
    title: '数字媒体设计',
    description: '掌握图形设计和视频制作技能',
    instructor: '刘老师',
    category: '设计'
  }
])

// 计算属性
const categories = computed(() => {
  const cats = [...new Set(courses.value.map(course => course.category))]
  return cats.sort()
})

const activeCourses = computed(() => 
  courses.value.filter(course => course.status === 'active')
)

const completedCourses = computed(() => 
  courses.value.filter(course => course.status === 'completed')
)

const averageProgress = computed(() => {
  if (courses.value.length === 0) return 0
  const total = courses.value.reduce((sum, course) => sum + course.progress, 0)
  return total / courses.value.length
})

const totalStudyHours = computed(() => 
  courses.value.reduce((total, course) => total + course.studyHours, 0)
)

const filteredCourses = computed(() => {
  let filtered = courses.value

  // 搜索筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(course => 
      course.title.toLowerCase().includes(query) ||
      course.description.toLowerCase().includes(query) ||
      course.instructor.toLowerCase().includes(query)
    )
  }

  // 分类筛选
  if (selectedCategory.value) {
    filtered = filtered.filter(course => course.category === selectedCategory.value)
  }

  // 状态筛选
  if (selectedStatus.value) {
    filtered = filtered.filter(course => course.status === selectedStatus.value)
  }

  // 是否显示已归档
  if (!showArchived.value) {
    filtered = filtered.filter(course => course.status !== 'archived')
  }

  // 排序
  return filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'recent':
        return new Date(b.lastStudied || 0).getTime() - new Date(a.lastStudied || 0).getTime()
      case 'progress':
        return b.progress - a.progress
      case 'name':
        return a.title.localeCompare(b.title)
      case 'startDate':
        return new Date(b.startDate).getTime() - new Date(a.startDate).getTime()
      default:
        return 0
    }
  })
})

// 方法
const getStatusVariant = (status: string) => {
  switch (status) {
    case 'active': return 'primary'
    case 'completed': return 'success'
    case 'paused': return 'warning'
    case 'archived': return 'secondary'
    default: return 'secondary'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'active': return '进行中'
    case 'completed': return '已完成'
    case 'paused': return '已暂停'
    case 'archived': return '已归档'
    default: return '未知'
  }
}

const enterCourse = (course: Course) => {
  router.push(`/student/courses/${course.id}`)
}

const continueLearning = (course: Course) => {
  uiStore.showNotification({
    type: 'info',
    title: '继续学习',
    message: `正在进入课程 "${course.title}"`
  })
  router.push(`/student/courses/${course.id}`)
}

const reviewCourse = (course: Course) => {
  router.push(`/student/courses/${course.id}`)
}

const resumeCourse = (course: Course) => {
  // 更新课程状态为active
  const courseIndex = courses.value.findIndex(c => c.id === course.id)
  if (courseIndex !== -1) {
    courses.value[courseIndex].status = 'active'
  }
  
  uiStore.showNotification({
    type: 'success',
    title: '恢复成功',
    message: `课程 "${course.title}" 已恢复学习`
  })
}

const toggleFavorite = (course: Course) => {
  const courseIndex = courses.value.findIndex(c => c.id === course.id)
  if (courseIndex !== -1) {
    courses.value[courseIndex].isFavorite = !courses.value[courseIndex].isFavorite
    
    uiStore.showNotification({
      type: 'success',
      title: courses.value[courseIndex].isFavorite ? '已收藏' : '取消收藏',
      message: `课程 "${course.title}" ${courses.value[courseIndex].isFavorite ? '已添加到收藏' : '已从收藏中移除'}`
    })
  }
}

const showCourseMenu = (course: Course) => {
  // 显示课程操作菜单（暂停、归档、退出等）
  uiStore.showNotification({
    type: 'info',
    title: '课程菜单',
    message: '课程操作菜单功能开发中...'
  })
}

const enrollCourse = async (course: any) => {
  try {
    // 模拟加入课程
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const newCourse: Course = {
      ...course,
      totalLessons: 20,
      completedLessons: 0,
      progress: 0,
      studyHours: 0,
      status: 'active',
      isFavorite: false,
      startDate: new Date().toISOString(),
      lastStudied: new Date().toISOString()
    }
    
    courses.value.unshift(newCourse)
    
    uiStore.showNotification({
      type: 'success',
      title: '加入成功',
      message: `已成功加入课程 "${course.title}"`
    })
    
    showCourseStore.value = false
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '加入失败',
      message: '加入课程时发生错误'
    })
  }
}

// 生命周期
onMounted(async () => {
  loading.value = true
  try {
    // 这里可以加载真实的课程数据
    await studentStore.fetchCourses()
    // courses.value = studentStore.courses
  } catch (error) {
    console.error('加载课程失败:', error)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped lang="postcss">
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style> 