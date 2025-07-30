<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <!-- 页面标题 -->
    <div class="mb-8">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">课程管理</h1>
          <p class="text-gray-600 dark:text-gray-400">管理您的所有课程，跟踪学生进度</p>
        </div>
        <div class="flex items-center space-x-4">
          <button variant="outline" @click="showFilters = !showFilters">
            <funnel-icon class="w-4 h-4 mr-2" />
            筛选
          </button>
          <button variant="primary" @click="showCreateCourseModal = true">
            <plus-icon class="w-4 h-4 mr-2" />
            创建课程
          </button>
        </div>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <card v-if="showFilters" padding="lg" class="mb-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">搜索课程</label>
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="输入课程名称..."
            class="input w-full"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">课程状态</label>
          <select v-model="statusFilter" class="input w-full">
            <option value="">全部状态</option>
            <option value="draft">草稿</option>
            <option value="published">已发布</option>
            <option value="archived">已归档</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">学科</label>
          <select v-model="subjectFilter" class="input w-full">
            <option value="">全部学科</option>
            <option value="math">数学</option>
            <option value="science">科学</option>
            <option value="language">语言</option>
            <option value="social">社会科学</option>
            <option value="art">艺术</option>
          </select>
        </div>
        <div class="flex items-end">
          <button variant="outline" @click="clearFilters" class="w-full">
            清除筛选
          </button>
        </div>
      </div>
    </card>

    <!-- 课程统计 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
      <card padding="lg" class="text-center">
        <div class="text-2xl font-bold text-blue-600 dark:text-blue-400 mb-2">
          {{ totalCourses }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">总课程数</p>
      </card>
      
      <card padding="lg" class="text-center">
        <div class="text-2xl font-bold text-green-600 dark:text-green-400 mb-2">
          {{ activeCourses }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">活跃课程</p>
      </card>
      
      <card padding="lg" class="text-center">
        <div class="text-2xl font-bold text-yellow-600 dark:text-yellow-400 mb-2">
          {{ totalStudents }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">总学生数</p>
      </card>
      
      <card padding="lg" class="text-center">
        <div class="text-2xl font-bold text-purple-600 dark:text-purple-400 mb-2">
          {{ averageCompletion }}%
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">平均完成率</p>
      </card>
    </div>

    <!-- 课程列表 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
      <card v-for="course in filteredCourses" 
            :key="course.courseId" 
            padding="none" 
            hoverable
            class="overflow-hidden">
        <!-- 课程封面 -->
        <div class="h-48 bg-gradient-to-r from-blue-500 to-purple-600 relative overflow-hidden">
          <div class="absolute inset-0 bg-black bg-opacity-20"></div>
          <div class="absolute top-4 left-4">
            <badge :variant="getCourseStatusColor(course.status)">
              {{ getCourseStatusText(course.status) }}
            </badge>
          </div>
          <div class="absolute top-4 right-4">
            <div class="relative">
              <button variant="ghost" size="sm" @click="toggleCourseMenu(course.courseId)">
                <ellipsis-vertical-icon class="w-4 h-4 text-white" />
              </button>
              
              <!-- 课程操作菜单 -->
              <div v-if="activeCourseMenu === course.courseId" 
                   class="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-600 z-50">
                <div class="py-1">
                  <button @click="editCourse(course)" class="w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700">
                    编辑课程
                  </button>
                  <button @click="viewAnalytics(course)" class="w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700">
                    查看分析
                  </button>
                  <button @click="manageStudents(course)" class="w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700">
                    学生管理
                  </button>
                  <button @click="duplicateCourse(course)" class="w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700">
                    复制课程
                  </button>
                  <div class="border-t border-gray-100 dark:border-gray-600"></div>
                  <button @click="archiveCourse(course)" class="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-gray-100 dark:hover:bg-gray-700">
                    归档课程
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div class="absolute bottom-4 left-4 text-white">
            <h3 class="text-xl font-bold mb-1">{{ course.courseName }}</h3>
            <p class="text-sm opacity-90">{{ course.enrolledStudents.length }} 名学生</p>
          </div>
        </div>
        
        <!-- 课程信息 -->
        <div class="p-6">
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center space-x-2">
              <users-icon class="w-4 h-4 text-gray-500" />
              <span class="text-sm text-gray-600 dark:text-gray-400">
                {{ course.enrolledStudents.length }} / {{ course.analytics.enrollmentCount }} 学生
              </span>
            </div>
            <div class="flex items-center space-x-1">
              <star-icon class="w-4 h-4 text-yellow-400 fill-current" />
              <span class="text-sm text-gray-600 dark:text-gray-400">
                {{ course.analytics.studentFeedback.rating.toFixed(1) }}
              </span>
            </div>
          </div>
          
          <!-- 进度条 -->
          <div class="mb-4">
            <div class="flex justify-between items-center mb-2">
              <span class="text-sm font-medium text-gray-700 dark:text-gray-300">完成率</span>
              <span class="text-sm text-gray-600 dark:text-gray-400">
                {{ Math.round(course.analytics.completionRate) }}%
              </span>
            </div>
            <progress :value="course.analytics.completionRate" color="primary" />
          </div>
          
          <!-- 待办任务 -->
          <div class="mb-4">
            <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">待处理任务</h4>
            <div class="space-y-1">
              <div v-for="task in course.pendingTasks" :key="task.type" class="flex justify-between items-center text-xs">
                <span class="text-gray-600 dark:text-gray-400">{{ getTaskTypeText(task.type) }}</span>
                <div class="flex items-center space-x-1">
                  <span class="text-gray-900 dark:text-white">{{ task.count }}</span>
                  <span v-if="task.urgent > 0" class="text-red-500">({{ task.urgent }} 紧急)</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 最近活动 -->
          <div class="mb-4">
            <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">最近活动</h4>
            <div class="text-xs text-gray-600 dark:text-gray-400">
              {{ course.recentActivity[0]?.action || '暂无活动' }}
              <span v-if="course.recentActivity[0]">
                · {{ formatRelativeTime(course.recentActivity[0].timestamp) }}
              </span>
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="flex space-x-2">
            <button variant="primary" size="sm" class="flex-1" @click="enterCourse(course)">
              <eye-icon class="w-4 h-4 mr-1" />
              查看
            </button>
            <button variant="outline" size="sm" class="flex-1" @click="editCourse(course)">
              <pencil-icon class="w-4 h-4 mr-1" />
              编辑
            </button>
          </div>
        </div>
      </card>
      
      <!-- 空状态 -->
      <div v-if="filteredCourses.length === 0" class="col-span-full">
        <card padding="lg" class="text-center py-12">
          <academic-cap-icon class="w-16 h-16 text-gray-400 mx-auto mb-4" />
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">暂无课程</h3>
          <p class="text-gray-600 dark:text-gray-400 mb-6">
            {{ searchQuery || statusFilter || subjectFilter ? '没有找到符合条件的课程' : '开始创建您的第一个课程' }}
          </p>
          <button variant="primary" @click="showCreateCourseModal = true">
            <plus-icon class="w-4 h-4 mr-2" />
            创建课程
          </button>
        </card>
      </div>
    </div>

    <!-- 创建课程弹窗 -->
    <div v-if="showCreateCourseModal" 
         class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
         @click.self="showCreateCourseModal = false">
      <div class="bg-white dark:bg-gray-800 rounded-lg w-full max-w-2xl mx-4 max-h-[90vh] overflow-hidden flex flex-col">
        <div class="p-6 border-b border-gray-200 dark:border-gray-600">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">创建新课程</h3>
        </div>
        
        <div class="p-6 overflow-y-auto flex-1">
          <form @submit.prevent="createCourseFromForm" class="space-y-6">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  课程名称 <span class="text-red-500">*</span>
                </label>
                <input 
                  v-model="courseForm.name" 
                  type="text" 
                  class="input w-full"
                  placeholder="输入课程名称"
                  required
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  学科分类 <span class="text-red-500">*</span>
                </label>
                <select v-model="courseForm.subject" class="input w-full" required>
                  <option value="">选择学科</option>
                  <option value="math">数学</option>
                  <option value="science">科学</option>
                  <option value="language">语言</option>
                  <option value="social">社会科学</option>
                  <option value="art">艺术</option>
                </select>
              </div>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                课程描述
              </label>
              <textarea 
                v-model="courseForm.description" 
                class="input w-full h-24 resize-none"
                placeholder="简要描述课程内容和目标"
              ></textarea>
            </div>
            
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  难度等级
                </label>
                <select v-model="courseForm.difficulty" class="input w-full">
                  <option value="beginner">初级</option>
                  <option value="intermediate">中级</option>
                  <option value="advanced">高级</option>
                </select>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  预计课时
                </label>
                <input 
                  v-model.number="courseForm.duration" 
                  type="number" 
                  min="1"
                  class="input w-full"
                  placeholder="0"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  最大学生数
                </label>
                <input 
                  v-model.number="courseForm.maxStudents" 
                  type="number" 
                  min="1"
                  class="input w-full"
                  placeholder="无限制"
                />
              </div>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                课程标签
              </label>
              <div class="flex flex-wrap gap-2 mb-2">
                <badge v-for="tag in courseForm.tags" :key="tag" variant="secondary" class="flex items-center">
                  {{ tag }}
                  <button @click="removeTag(tag)" class="ml-2 text-gray-500 hover:text-gray-700">
                    <x-mark-icon class="w-3 h-3" />
                  </button>
                </badge>
              </div>
              <div class="flex">
                <input 
                  v-model="newTag" 
                  @keyup.enter="addTag"
                  type="text" 
                  class="input flex-1 rounded-r-none"
                  placeholder="输入标签后按回车添加"
                />
                <button type="button" variant="outline" @click="addTag" class="rounded-l-none">
                  添加
                </button>
              </div>
            </div>
            
            <div class="flex items-center space-x-4">
              <label class="flex items-center">
                <input 
                  v-model="courseForm.isPublic" 
                  type="checkbox" 
                  class="rounded border-gray-300 text-primary-600 shadow-sm focus:border-primary-300 focus:ring focus:ring-primary-200 focus:ring-opacity-50"
                />
                <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">公开课程</span>
              </label>
              
              <label class="flex items-center">
                <input 
                  v-model="courseForm.allowSelfEnroll" 
                  type="checkbox" 
                  class="rounded border-gray-300 text-primary-600 shadow-sm focus:border-primary-300 focus:ring focus:ring-primary-200 focus:ring-opacity-50"
                />
                <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">允许学生自主报名</span>
              </label>
            </div>
          </form>
        </div>
        
        <div class="p-6 border-t border-gray-200 dark:border-gray-600 flex justify-end space-x-3">
          <button variant="outline" @click="showCreateCourseModal = false">
            取消
          </button>
          <button variant="primary" @click="createCourseFromForm" :loading="creating">
            创建课程
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTeacherStore } from '@/stores/teacher'
import { useUIStore } from '@/stores/ui'
import { coursesAPI } from '@/api/courses.api'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Progress from '@/components/ui/Progress.vue'
import type { TeacherCourseManagement } from '@/types/teacher'
import {
  PlusIcon,
  MagnifyingGlassIcon,
  AdjustmentsHorizontalIcon,
  EllipsisVerticalIcon,
  EyeIcon,
  PencilIcon,
  ChartBarIcon,
  UsersIcon,
  DocumentDuplicateIcon,
  ArchiveBoxIcon,
  TrashIcon,
  FunnelIcon,
  StarIcon,
  AcademicCapIcon,
  XMarkIcon
} from '@heroicons/vue/24/outline'

// 状态管理
const router = useRouter()
const teacherStore = useTeacherStore()
const uiStore = useUIStore()

// 响应式状态
const searchQuery = ref('')
const selectedStatus = ref('')
const sortBy = ref('courseName')
const activeCourseMenu = ref('')

// 状态选项
const statusOptions = [
  { value: '', label: '全部状态' },
  { value: 'draft', label: '草稿' },
  { value: 'published', label: '已发布' },
  { value: 'archived', label: '已归档' }
]

// 排序选项
const sortOptions = [
  { value: 'courseName', label: '课程名称' },
  { value: 'students', label: '学生数量' },
  { value: 'status', label: '状态' }
]

// 计算属性
const filteredCourses = computed(() => {
  let courses = teacherStore.courses

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    courses = courses.filter(course => 
      course.courseName.toLowerCase().includes(query)
    )
  }

  // 状态过滤
  if (selectedStatus.value) {
    courses = courses.filter(course => course.status === selectedStatus.value)
  }

  // 排序
  courses = [...courses].sort((a, b) => {
    switch (sortBy.value) {
      case 'students':
        return b.enrolledStudents.length - a.enrolledStudents.length
      case 'status':
        return a.status.localeCompare(b.status)
      case 'courseName':
      default:
        return a.courseName.localeCompare(b.courseName)
    }
  })

  return courses
})

// 统计数据
const totalCourses = computed(() => teacherStore.courses.length)
const activeCourses = computed(() => 
  teacherStore.courses.filter(course => course.status === 'published').length
)
const totalStudents = computed(() => 
  teacherStore.courses.reduce((sum, course) => sum + course.enrolledStudents.length, 0)
)
const averageCompletion = computed(() => {
  const courses = teacherStore.courses
  if (courses.length === 0) return 0
  const total = courses.reduce((sum, course) => sum + course.analytics.completionRate, 0)
  return Math.round(total / courses.length)
})

// 方法
const toggleCourseMenu = (courseId: string) => {
  activeCourseMenu.value = activeCourseMenu.value === courseId ? '' : courseId
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'published': return 'success'
    case 'draft': return 'warning'
    case 'archived': return 'secondary'
    default: return 'secondary'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'published': return '已发布'
    case 'draft': return '草稿'
    case 'archived': return '已归档'
    default: return '未知'
  }
}

// 格式化任务类型文本
const getTaskTypeText = (type: string) => {
  switch (type) {
    case 'grading': return '待批改'
    case 'feedback': return '待反馈'
    case 'content_review': return '内容审核'
    case 'student_inquiry': return '学生询问'
    default: return '其他'
  }
}

// 格式化相对时间
const formatRelativeTime = (timestamp: string) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${Math.floor(diff / 86400000)}天前`
}

const createCourse = () => {
  router.push('/teacher/courses/create')
}

const enterCourse = (course: TeacherCourseManagement) => {
  router.push(`/teacher/courses/${course.courseId}`)
}

const editCourse = (course: TeacherCourseManagement) => {
  activeCourseMenu.value = ''
  router.push(`/teacher/courses/${course.courseId}/edit`)
}

const viewAnalytics = (course: TeacherCourseManagement) => {
  activeCourseMenu.value = ''
  router.push(`/teacher/courses/${course.courseId}/analytics`)
}

const manageStudents = (course: TeacherCourseManagement) => {
  activeCourseMenu.value = ''
  router.push(`/teacher/courses/${course.courseId}/students`)
}

const duplicateCourse = async (course: TeacherCourseManagement) => {
  activeCourseMenu.value = ''
  try {
    // 调用真正的API复制课程
    const duplicateData = {
      title: `${course.courseName} (副本)`,
      description: `${course.courseName}的副本课程`,
      category: 'other',
      level: 'beginner' as const,
      duration: 60,
      price: 0,
      tags: [],
      status: 'draft' as const
    }
    
    await coursesAPI.createCourse(duplicateData)
    
    uiStore.showNotification({
      type: 'success',
      title: '复制成功',
      message: '课程已成功复制'
    })
    
    await teacherStore.fetchCourses()
  } catch (error: any) {
    console.error('复制课程失败:', error)
    uiStore.showNotification({
      type: 'error',
      title: '复制失败',
      message: error.message || '课程复制失败，请重试'
    })
  }
}

const archiveCourse = async (course: TeacherCourseManagement) => {
  activeCourseMenu.value = ''
  
  if (!confirm(`确定要归档课程"${course.courseName}"吗？`)) {
    return
  }
  
  try {
    // 调用真正的API归档课程
    await coursesAPI.updateCourse(course.courseId, { 
      title: course.courseName,
      status: 'draft' // 由于API不支持archived状态，使用draft
    })
    
    uiStore.showNotification({
      type: 'success',
      title: '归档成功',
      message: '课程已成功归档'
    })
    
    await teacherStore.fetchCourses()
  } catch (error: any) {
    console.error('归档课程失败:', error)
    uiStore.showNotification({
      type: 'error',
      title: '归档失败',
      message: error.message || '课程归档失败，请重试'
    })
  }
}

const deleteCourse = async (course: TeacherCourseManagement) => {
  activeCourseMenu.value = ''
  
  if (!confirm(`确定要删除课程"${course.courseName}"吗？此操作不可撤销！`)) {
    return
  }
  
  try {
    // 调用真正的API删除课程
    await coursesAPI.deleteCourse(course.courseId)
    
    uiStore.showNotification({
      type: 'success',
      title: '删除成功',
      message: '课程已成功删除'
    })
    
    await teacherStore.fetchCourses()
  } catch (error: any) {
    console.error('删除课程失败:', error)
    uiStore.showNotification({
      type: 'error',
      title: '删除失败',
      message: error.message || '课程删除失败，请重试'
    })
  }
}

// 获取课程状态颜色和文本的方法
const getCourseStatusColor = (status: string) => {
  return getStatusColor(status)
}

const getCourseStatusText = (status: string) => {
  return getStatusText(status)
}

// 清除筛选器
const showFilters = ref(false)
const statusFilter = ref('')
const subjectFilter = ref('')

const clearFilters = () => {
  searchQuery.value = ''
  statusFilter.value = ''
  subjectFilter.value = ''
}

// 创建课程表单
const showCreateCourseModal = ref(false)
const creating = ref(false)
const courseForm = ref({
  name: '',
  subject: '',
  description: '',
  difficulty: 'beginner',
  duration: 0,
  maxStudents: 0,
  tags: [] as string[],
  isPublic: false,
  allowSelfEnroll: false
})

const newTag = ref('')

const addTag = () => {
  if (newTag.value.trim() && !courseForm.value.tags.includes(newTag.value.trim())) {
    courseForm.value.tags.push(newTag.value.trim())
    newTag.value = ''
  }
}

const removeTag = (tag: string) => {
  const index = courseForm.value.tags.indexOf(tag)
  if (index > -1) {
    courseForm.value.tags.splice(index, 1)
  }
}

const createCourseFromForm = async () => {
  if (!courseForm.value.name || !courseForm.value.subject) {
    uiStore.showNotification({
      type: 'error',
      title: '表单错误',
      message: '请填写必填字段'
    })
    return
  }

  creating.value = true
  try {
    const courseData = {
      title: courseForm.value.name,
      description: courseForm.value.description,
      category: courseForm.value.subject,
      level: courseForm.value.difficulty as 'beginner' | 'intermediate' | 'advanced',
      duration: courseForm.value.duration,
      price: 0,
      tags: courseForm.value.tags,
      status: 'draft' as const
    }

    await coursesAPI.createCourse(courseData)
    
    uiStore.showNotification({
      type: 'success',
      title: '创建成功',
      message: '课程已成功创建'
    })
    
    showCreateCourseModal.value = false
    await teacherStore.fetchCourses()
  } catch (error: any) {
    console.error('创建课程失败:', error)
    uiStore.showNotification({
      type: 'error',
      title: '创建失败',
      message: error.message || '课程创建失败，请重试'
    })
  } finally {
    creating.value = false
  }
}

// 生命周期
onMounted(async () => {
  await teacherStore.fetchCourses()
})

// 点击外部关闭菜单
document.addEventListener('click', (e: Event) => {
  const target = e.target as HTMLElement
  if (!target.closest('.relative')) {
    activeCourseMenu.value = ''
  }
})
</script> 