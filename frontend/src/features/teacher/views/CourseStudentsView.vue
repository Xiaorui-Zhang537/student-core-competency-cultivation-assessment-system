<template>
  <div class="min-h-screen p-6">
    <div class="max-w-7xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <nav class="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400 mb-2">
              <router-link to="/teacher/courses" class="hover:text-gray-700 dark:hover:text-gray-200">
                课程管理
              </router-link>
              <chevron-right-icon class="w-4 h-4" />
              <router-link :to="`/teacher/courses/${courseId}`" class="hover:text-gray-700 dark:hover:text-gray-200">
                {{ courseName }}
              </router-link>
              <chevron-right-icon class="w-4 h-4" />
              <span>学生管理</span>
            </nav>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">学生管理</h1>
            <p class="text-gray-600 dark:text-gray-400">管理课程学生的学习进度和成绩</p>
          </div>
          <div class="flex items-center space-x-3">
            <Button variant="indigo" @click="openInviteModal">
              <UserPlusIcon class="w-4 h-4 mr-2" />
              邀请学生
            </Button>
            <Button variant="teal" @click="exportData">
              <ArrowDownTrayIcon class="w-4 h-4 mr-2" />
              导出数据
            </Button>
            <!-- 去除模拟邀请功能，后续有真实接口再恢复 -->
          </div>
        </div>
      </div>

      <!-- 统计卡片（基于接口数据汇总） -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <card padding="lg" class="text-center">
          <div class="text-3xl font-bold text-blue-600 dark:text-blue-400 mb-2">
            {{ stats.totalStudents }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">总学生数</p>
          <div class="mt-2 flex items-center justify-center space-x-1">
            <arrow-trending-up-icon class="w-4 h-4 text-green-500" />
            <span class="text-xs text-green-600">+{{ stats.newStudentsThisWeek }} 本周新增</span>
          </div>
        </card>
        <card padding="lg" class="text-center">
          <div class="text-3xl font-bold text-green-600 dark:text-green-400 mb-2">
            {{ stats.averageProgress }}%
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">平均进度</p>
          <div class="mt-2">
            <progress :value="stats.averageProgress" :max="100" size="sm" color="primary" />
          </div>
        </card>
        <card padding="lg" class="text-center">
          <div class="text-3xl font-bold text-yellow-600 dark:text-yellow-400 mb-2">
            {{ Number(stats.averageGrade || 0).toFixed(1) }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">平均成绩</p>
          <div class="mt-2 flex items-center justify-center space-x-1">
            <star-icon class="w-4 h-4 text-yellow-500" />
            <span class="text-xs text-gray-600">{{ stats.passRate }}% 及格率</span>
          </div>
        </card>
        <card padding="lg" class="text-center">
          <div class="text-3xl font-bold text-purple-600 dark:text-purple-400 mb-2">
            {{ stats.activeStudents }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">活跃学生</p>
          <div class="mt-2 flex items-center justify-center space-x-1">
            <clock-icon class="w-4 h-4 text-blue-500" />
            <span class="text-xs text-blue-600">本周活跃</span>
          </div>
        </card>
      </div>

      <!-- 筛选和操作栏 -->
      <card padding="lg" class="mb-8">
        <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between space-y-4 lg:space-y-0">
          <!-- 搜索和筛选 -->
          <div class="flex flex-col sm:flex-row space-y-3 sm:space-y-0 sm:space-x-4">
            <!-- 搜索框 -->
            <div class="relative">
              <magnifying-glass-icon class="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                v-model="searchQuery"
                type="text"
                placeholder="搜索学生姓名或学号..."
                class="input pl-10 w-64"
              />
            </div>

            <!-- 筛选项：后续后端支持后开启提交查询参数（当前保留UI与本地过滤） -->
            <select v-model="progressFilter" class="input w-40">
              <option value="">全部进度</option>
              <option value="not-started">未开始</option>
              <option value="in-progress">进行中</option>
              <option value="completed">已完成</option>
            </select>
            <select v-model="gradeFilter" class="input w-40">
              <option value="">全部成绩</option>
              <option value="excellent">优秀(90+)</option>
              <option value="good">良好(80-89)</option>
              <option value="average">中等(70-79)</option>
              <option value="below">需提高(&#60;70)</option>
            </select>
            <select v-model="activityFilter" class="input w-40">
              <option value="">全部活跃度</option>
              <option value="high">高活跃</option>
              <option value="medium">中等活跃</option>
              <option value="low">低活跃</option>
              <option value="inactive">不活跃</option>
            </select>
          </div>

          <!-- 批量操作 -->
          <div class="flex items-center space-x-3">
            <div v-if="selectedStudents.length > 0" class="flex items-center space-x-2">
              <span class="text-sm text-gray-600 dark:text-gray-400">
                已选择 {{ selectedStudents.length }} 名学生
              </span>
              <Button variant="purple" size="sm" @click="batchSendMessage">
                <PaperAirplaneIcon class="w-4 h-4 mr-1" />
                发送消息
              </Button>
              <Button variant="teal" size="sm" @click="batchExport">
                <ArrowDownTrayIcon class="w-4 h-4 mr-1" />
                批量导出
              </Button>
              <Button variant="outline" size="sm" @click="batchRemove">
                <UserMinusIcon class="w-4 h-4 mr-1" />
                移除学生
              </Button>
            </div>
            
            <!-- 排序 -->
            <select v-model="sortBy" class="input input-sm">
              <option value="name">按姓名排序</option>
              <option value="progress">按进度排序</option>
              <option value="grade">按成绩排序</option>
              <option value="lastActive">按活跃度排序</option>
              <option value="joinDate">按加入时间排序</option>
            </select>

            <!-- 已移除视图切换按钮，固定为表格视图 -->
          </div>
        </div>
      </card>

      <!-- 邀请学生弹窗 -->
      <div v-if="showInviteModal" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
        <div class="bg-white dark:bg-gray-800 w-full max-w-lg rounded-xl shadow-xl border border-gray-200 dark:border-gray-700">
          <div class="p-4 border-b border-gray-200 dark:border-gray-700">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">邀请学生加入课程</h3>
          </div>
          <div class="p-4 space-y-4">
            <p class="text-sm text-gray-600 dark:text-gray-400">支持批量输入学生ID，使用逗号或换行分隔。</p>
            <textarea v-model="inviteRaw" rows="6" class="input w-full" placeholder="例如：1001,1002,1003 或者每行一个"></textarea>
            <div class="text-xs text-gray-500">已解析：{{ parsedInviteIds.length }} 个</div>
          </div>
          <div class="p-4 flex justify-end gap-2 border-t border-gray-200 dark:border-gray-700">
            <Button variant="outline" @click="closeInviteModal">取消</Button>
            <Button variant="teal" :disabled="parsedInviteIds.length===0 || inviting" :loading="inviting" @click="submitInvite">确认邀请</Button>
          </div>
        </div>
      </div>

      <!-- 学生列表 - 表格视图 -->
      <card padding="lg">
        <template #header>
          <div class="flex justify-between items-center">
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white">学生列表</h2>
            <div class="flex items-center space-x-2">
              <span class="text-sm text-gray-500 dark:text-gray-400">
                共 {{ stats.totalStudents }} 名学生
              </span>
            </div>
          </div>
        </template>

        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-600">
            <thead class="bg-gray-50 dark:bg-gray-700">
              <tr>
                <th class="px-6 py-3 text-left">
                  <input
                    type="checkbox"
                    :checked="selectedStudents.length === filteredStudents.length"
                    @change="toggleSelectAll"
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  学生信息
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  学习进度
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  成绩
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  活跃度
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  最后活动
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  操作
                </th>
              </tr>
            </thead>
            <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-600">
              <tr v-for="student in paginatedStudents" :key="student.id" class="hover:bg-gray-50 dark:hover:bg-gray-700">
                <td class="px-6 py-4">
                  <input
                    type="checkbox"
                    :value="student.id"
                    v-model="selectedStudents"
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                </td>
                <td class="px-6 py-4">
                  <div class="flex items-center">
                    <div class="flex-shrink-0 w-10 h-10">
                      <img 
                        v-if="student.avatar" 
                        :src="student.avatar" 
                        :alt="student.name"
                        class="w-10 h-10 rounded-full"
                      />
                      <div v-else class="w-10 h-10 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center">
                        <user-icon class="w-5 h-5 text-gray-400" />
                      </div>
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-gray-900 dark:text-white">
                        {{ student.name }}
                      </div>
                      <div class="text-sm text-gray-500 dark:text-gray-400">
                        {{ student.studentId }}
                      </div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4">
                  <div class="flex items-center">
                    <div class="flex-1">
                      <div class="flex justify-between items-center mb-1">
                        <span class="text-sm font-medium text-gray-900 dark:text-white">
                          {{ student.progress }}%
                        </span>
                        <span class="text-xs text-gray-500">
                          {{ student.completedLessons }}/{{ student.totalLessons }}
                        </span>
                      </div>
                      <progress :value="student.progress" :max="100" size="sm" />
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4">
                  <div class="flex items-center space-x-2">
                    <span class="text-sm font-medium text-gray-900 dark:text-white">
                      {{ student.averageGrade || '--' }}
                    </span>
                    <badge 
                      v-if="student.averageGrade"
                      :variant="getGradeBadgeVariant(student.averageGrade)"
                      size="sm"
                    >
                      {{ getGradeLevel(student.averageGrade) }}
                    </badge>
                  </div>
                </td>
                <td class="px-6 py-4">
                  <div class="flex items-center">
                    <div class="flex-1">
                      <div class="flex items-center space-x-2">
                        <div
                          class="w-2 h-2 rounded-full"
                          :class="getActivityColor(student.activityLevel)"
                        ></div>
                        <span class="text-sm text-gray-900 dark:text-white">
                          {{ getActivityText(student.activityLevel) }}
                        </span>
                      </div>
                      <div class="text-xs text-gray-500 mt-1">
                        {{ student.studyTime }}小时/周
                      </div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
                  {{ formatRelativeTime(student.lastActiveAt) }}
                </td>
                <td class="px-6 py-4">
                  <div class="flex items-center space-x-2">
                    <Button variant="outline" size="sm" @click="viewStudentDetail(student.id)">
                      <EyeIcon class="w-4 h-4 mr-1" />
                      详情
                    </Button>
                    <Button variant="purple" size="sm" @click="sendMessage(student.id)">
                      <ChatBubbleLeftIcon class="w-4 h-4 mr-1" />
                      消息
                    </Button>
                    <div class="relative" @click.stop>
                      <Button variant="ghost" size="sm" @click="toggleStudentMenu(student.id)">
                        <EllipsisVerticalIcon class="w-4 h-4" />
                      </Button>
                      <div
                        v-if="showStudentMenu === student.id"
                        class="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 rounded-md shadow-lg z-10 border border-gray-200 dark:border-gray-600"
                      >
                        <div class="py-1">
                          <button @click="viewGrades(student.id)" class="flex items-center gap-2 w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700">
                            <AcademicCapIcon class="w-4 h-4" />
                            查看成绩
                          </button>
                          <button @click="resetProgress(student.id)" class="flex items-center gap-2 w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700">
                            <ArrowPathIcon class="w-4 h-4" />
                            重置进度
                          </button>
                          <button @click="exportStudentData(student.id)" class="flex items-center gap-2 w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700">
                            <ArrowDownTrayIcon class="w-4 h-4" />
                            导出数据
                          </button>
                          <hr class="my-1 border-gray-200 dark:border-gray-600" />
                          <button @click="removeStudent(student.id)" class="flex items-center gap-2 w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-gray-100 dark:hover:bg-gray-700">
                            <UserMinusIcon class="w-4 h-4" />
                            移除学生
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 分页 -->
        <div class="mt-6 flex items-center justify-between">
          <div class="flex items-center space-x-2">
            <span class="text-sm text-gray-700 dark:text-gray-300">每页显示</span>
            <select v-model="pageSize" class="input input-sm w-20">
              <option :value="10">10</option>
              <option :value="20">20</option>
              <option :value="50">50</option>
              <option :value="100">100</option>
            </select>
            <span class="text-sm text-gray-700 dark:text-gray-300">条记录</span>
          </div>
          
          <div class="flex items-center space-x-2">
            <Button 
              variant="outline" 
              size="sm" 
              @click="currentPage--" 
              :disabled="currentPage === 1"
            >
              <ChevronLeftIcon class="w-4 h-4 mr-1" />
              上一页
            </Button>
            
            <div class="flex space-x-1">
              <button
                v-for="page in pageNumbers"
                :key="page"
                @click="currentPage = page"
                class="px-3 py-1 text-sm rounded transition-colors"
                :class="currentPage === page 
                  ? 'bg-primary-600 text-white' 
                  : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'"
              >
                {{ page }}
              </button>
            </div>
            
            <Button 
              variant="outline" 
              size="sm" 
              @click="currentPage++" 
              :disabled="currentPage === totalPages"
            >
              下一页
              <ChevronRightIcon2 class="w-4 h-4 ml-1" />
            </Button>
          </div>
        </div>
      </card>

      <!-- 已移除卡片视图 -->
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import apiClient, { baseURL } from '@/api/config'
import { useRoute, useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Progress from '@/components/ui/Progress.vue'
import {
  ChevronRightIcon,
  DocumentArrowDownIcon,
  UserPlusIcon,
  ArrowTrendingUpIcon,
  StarIcon,
  ClockIcon,
  MagnifyingGlassIcon,
  UserIcon,
  EyeIcon,
  ChatBubbleLeftIcon,
  EllipsisVerticalIcon,
  ArrowDownTrayIcon,
  PaperAirplaneIcon,
  UserMinusIcon,
  ChevronLeftIcon,
  ChevronRightIcon as ChevronRightIcon2,
  AcademicCapIcon,
  ArrowPathIcon
} from '@heroicons/vue/24/outline'

// Router and Stores
const route = useRoute()
const router = useRouter()
const uiStore = useUIStore()

// 状态
const courseId = route.params.id as string
const courseName = ref('')
const searchQuery = ref('')
const progressFilter = ref('')
const gradeFilter = ref('')
const activityFilter = ref('')
const sortBy = ref('name')
const viewMode = ref('table')
const selectedStudents = ref<string[]>([])
const showStudentMenu = ref<string | null>(null)
const currentPage = ref(1)
const pageSize = ref(20)

// 统计数据（占位：后续可接入专用统计端点）
const stats = reactive({
  totalStudents: 0,
  newStudentsThisWeek: 0,
  averageProgress: 0,
  averageGrade: 0,
  passRate: 0,
  activeStudents: 0
})

// 学生数据（从后端拉取）
const students = ref<any[]>([])

const fetchCourseStudents = async () => {
  try {
    // 拉取课程名
    const { courseApi } = await import('@/api/course.api')
    const courseRes: any = await courseApi.getCourseById(Number(courseId))
    // Axios 拦截器已解包，直接是课程对象
    courseName.value = courseRes?.title || ''
  } catch { /* empty */ }
  try {
    const { teacherApi } = await import('@/api/teacher.api')
    const res: any = await teacherApi.getCourseStudentPerformance(courseId, {
      page: currentPage.value,
      size: pageSize.value,
      search: searchQuery.value || undefined,
      sortBy: sortBy.value || 'name',
      activity: activityFilter.value || undefined,
      grade: gradeFilter.value || undefined,
      progress: progressFilter.value || undefined
    })
    const payload = res?.data || res
    const items = payload?.items || []
    students.value = items.map((i: any) => ({
      id: String(i.studentId),
      name: i.studentName || `学生${i.studentId}`,
      studentId: i.studentNo || String(i.studentId),
      avatar: i.avatar || '',
      progress: i.progress ?? 0,
      completedLessons: i.completedLessons ?? 0,
      totalLessons: i.totalLessons ?? 0,
      averageGrade: i.averageGrade ?? undefined,
      activityLevel: i.activityLevel || 'medium',
      studyTime: i.studyTimePerWeek || 0,
      lastActiveAt: i.lastActiveAt || new Date().toISOString(),
      joinedAt: new Date().toISOString()
    }))
    stats.totalStudents = payload?.total || students.value.length
    // 汇总统计（后端返回全集过滤统计）
    stats.averageProgress = payload?.averageProgress ?? 0
    stats.averageGrade = payload?.averageGrade ?? 0
    stats.activeStudents = payload?.activeStudents ?? 0
    stats.passRate = payload?.passRate ?? 0
  } catch (e: any) {
    uiStore.showNotification({ type: 'error', title: '加载失败', message: e?.message || '获取课程学生失败' })
  }
}

// 计算属性
const filteredStudents = computed(() => students.value)

const totalPages = computed(() => Math.max(1, Math.ceil((stats.totalStudents || 0) / pageSize.value)))

const paginatedStudents = computed(() => students.value)

const pageNumbers = computed(() => {
  const pages = []
  const maxVisiblePages = 5
  let startPage = Math.max(1, currentPage.value - Math.floor(maxVisiblePages / 2))
  let endPage = Math.min(totalPages.value, startPage + maxVisiblePages - 1)

  if (endPage - startPage < maxVisiblePages - 1) {
    startPage = Math.max(1, endPage - maxVisiblePages + 1)
  }

  for (let i = startPage; i <= endPage; i++) {
    pages.push(i)
  }
  return pages
})

// 方法
const getGradeBadgeVariant = (grade: number) => {
  if (grade >= 90) return 'success'
  if (grade >= 80) return 'primary'
  if (grade >= 70) return 'warning'
  return 'danger'
}

const getGradeLevel = (grade: number) => {
  if (grade >= 90) return '优秀'
  if (grade >= 80) return '良好'
  if (grade >= 70) return '中等'
  return '需提高'
}

const getActivityColor = (level: string) => {
  switch (level) {
    case 'high': return 'bg-green-500'
    case 'medium': return 'bg-yellow-500'
    case 'low': return 'bg-orange-500'
    case 'inactive': return 'bg-red-500'
    default: return 'bg-gray-500'
  }
}

const getActivityText = (level: string) => {
  switch (level) {
    case 'high': return '高活跃'
    case 'medium': return '中等活跃'
    case 'low': return '低活跃'
    case 'inactive': return '不活跃'
    default: return '未知'
  }
}

const formatRelativeTime = (timestamp: string) => {
  const now = new Date()
  const time = new Date(timestamp)
  const diff = now.getTime() - time.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return time.toLocaleDateString('zh-CN')
}

const toggleSelectAll = (event: Event) => {
  const checked = (event.target as HTMLInputElement).checked
  selectedStudents.value = checked ? filteredStudents.value.map(s => s.id) : []
}

const toggleStudentMenu = (studentId: string) => {
  showStudentMenu.value = showStudentMenu.value === studentId ? null : studentId
}

const viewStudentDetail = (studentId: string) => {
  router.push(`/teacher/students/${studentId}`)
}

// 移除模拟的发送消息入口（占位保留函数，避免模板引用报错）
const sendMessage = (_studentId: string) => {}

const viewGrades = (studentId: string) => {
        router.push(`/teacher/students/${studentId}`)
}

// 移除模拟重置逻辑，保留空函数
const resetProgress = async (_studentId: string) => {}

const exportStudentData = async (_studentId: string) => {}

const removeStudent = async (studentId: string) => {
  if (confirm('确定要将该学生从课程中移除吗？')) {
    try {
      await new Promise(resolve => setTimeout(resolve, 1000))
      const index = students.value.findIndex(s => s.id === studentId)
      if (index > -1) {
        students.value.splice(index, 1)
      }
      
      uiStore.showNotification({
        type: 'success',
        title: '移除成功',
        message: '学生已从课程中移除'
      })
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '移除失败',
        message: '移除学生时发生错误'
      })
    }
  }
}

const batchSendMessage = () => {}

const batchExport = async () => {}

const batchRemove = async () => {}

const exportData = async () => {
  try {
    const params = new URLSearchParams()
    if (searchQuery.value) params.append('search', searchQuery.value)
    if (sortBy.value) params.append('sortBy', sortBy.value)
    if (activityFilter.value) params.append('activity', activityFilter.value)
    if (gradeFilter.value) params.append('grade', gradeFilter.value)
    if (progressFilter.value) params.append('progress', progressFilter.value)

    const url = `${baseURL}/teachers/analytics/course/${courseId}/students/export?${params.toString()}`
    const res = await apiClient.get(url, { responseType: 'blob' })
    const blob = new Blob([res as any], { type: 'text/csv;charset=UTF-8' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `course_${courseId}_students.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (e: any) {
    uiStore.showNotification({ type: 'error', title: '导出失败', message: e?.message || '导出数据时发生错误' })
  }
}

// 邀请学生弹窗与提交逻辑
const showInviteModal = ref(false)
const inviteRaw = ref('')
const inviting = ref(false)
const parsedInviteIds = computed(() => {
  const tokens = inviteRaw.value.split(/[^0-9]+/).filter(Boolean)
  const nums = Array.from(new Set(tokens.map((t) => Number(t)).filter((n) => Number.isFinite(n))))
  return nums
})
const openInviteModal = () => {
  showInviteModal.value = true
}
const closeInviteModal = () => {
  showInviteModal.value = false
  inviteRaw.value = ''
}
const submitInvite = async () => {
  if (parsedInviteIds.value.length === 0) return
  try {
    inviting.value = true
    const { courseApi } = await import('@/api/course.api')
    await courseApi.inviteStudents(courseId, parsedInviteIds.value)
    uiStore.showNotification({
      type: 'success',
      title: '邀请成功',
      message: `已处理 ${parsedInviteIds.value.length} 个学生ID`
    })
    closeInviteModal()
    await fetchCourseStudents()
  } catch (e: any) {
    uiStore.showNotification({ type: 'error', title: '邀请失败', message: e?.message || '提交失败' })
  } finally {
    inviting.value = false
  }
}

// 生命周期
onMounted(async () => {
  // 点击外部关闭菜单
  const close = () => { showStudentMenu.value = null }
  document.addEventListener('click', close)
  await fetchCourseStudents()
})

// 监听筛选/分页/排序/搜索，服务端拉取
watch([currentPage, pageSize, sortBy, progressFilter, gradeFilter, activityFilter], () => {
  fetchCourseStudents()
})
watch(searchQuery, () => {
  currentPage.value = 1
  fetchCourseStudents()
})

onUnmounted(() => {
  // 事件在 onMounted 中注册为具名函数
  const close = () => { showStudentMenu.value = null }
  document.removeEventListener('click', close)
})
</script> 