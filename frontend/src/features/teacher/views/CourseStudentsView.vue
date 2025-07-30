<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
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
            <button variant="outline" @click="exportData">
              <document-arrow-down-icon class="w-4 h-4 mr-2" />
              导出数据
            </button>
            <button variant="primary" @click="inviteStudents">
              <user-plus-icon class="w-4 h-4 mr-2" />
              邀请学生
            </button>
          </div>
        </div>
      </div>

      <!-- 统计卡片 -->
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
            {{ stats.averageGrade.toFixed(1) }}
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

            <!-- 进度筛选 -->
            <select v-model="progressFilter" class="input w-40">
              <option value="">全部进度</option>
              <option value="not-started">未开始</option>
              <option value="in-progress">进行中</option>
              <option value="completed">已完成</option>
            </select>

            <!-- 成绩筛选 -->
            <select v-model="gradeFilter" class="input w-40">
              <option value="">全部成绩</option>
              <option value="excellent">优秀(90+)</option>
              <option value="good">良好(80-89)</option>
              <option value="average">中等(70-79)</option>
              <option value="below">需提高(&#60;70)</option>
            </select>

            <!-- 活跃度筛选 -->
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
              <button variant="outline" size="sm" @click="batchSendMessage">
                发送消息
              </button>
              <button variant="outline" size="sm" @click="batchExport">
                批量导出
              </button>
              <button variant="danger" size="sm" @click="batchRemove">
                移除学生
              </button>
            </div>
            
            <!-- 排序 -->
            <select v-model="sortBy" class="input input-sm">
              <option value="name">按姓名排序</option>
              <option value="progress">按进度排序</option>
              <option value="grade">按成绩排序</option>
              <option value="lastActive">按活跃度排序</option>
              <option value="joinDate">按加入时间排序</option>
            </select>

            <!-- 视图切换 -->
            <div class="flex rounded-md border border-gray-300 dark:border-gray-600">
              <button
                @click="viewMode = 'table'"
                class="px-3 py-1 text-sm rounded-l-md transition-colors"
                :class="viewMode === 'table' 
                  ? 'bg-primary-600 text-white' 
                  : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700'"
              >
                <table-cells-icon class="w-4 h-4" />
              </button>
              <button
                @click="viewMode = 'grid'"
                class="px-3 py-1 text-sm rounded-r-md transition-colors"
                :class="viewMode === 'grid' 
                  ? 'bg-primary-600 text-white' 
                  : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700'"
              >
                <squares2-x2-icon class="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      </card>

      <!-- 学生列表 - 表格视图 -->
      <card padding="lg" v-if="viewMode === 'table'">
        <template #header>
          <div class="flex justify-between items-center">
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white">学生列表</h2>
            <div class="flex items-center space-x-2">
              <span class="text-sm text-gray-500 dark:text-gray-400">
                共 {{ filteredStudents.length }} 名学生
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
                    <button variant="ghost" size="sm" @click="viewStudentDetail(student.id)">
                      <eye-icon class="w-4 h-4" />
                    </button>
                    <button variant="ghost" size="sm" @click="sendMessage(student.id)">
                      <chat-bubble-left-icon class="w-4 h-4" />
                    </button>
                    <div class="relative" @click.stop>
                      <button variant="ghost" size="sm" @click="toggleStudentMenu(student.id)">
                        <ellipsis-vertical-icon class="w-4 h-4" />
                      </button>
                      <div
                        v-if="showStudentMenu === student.id"
                        class="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 rounded-md shadow-lg z-10 border border-gray-200 dark:border-gray-600"
                      >
                        <div class="py-1">
                          <button @click="viewGrades(student.id)" class="block w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700">
                            查看成绩
                          </button>
                          <button @click="resetProgress(student.id)" class="block w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700">
                            重置进度
                          </button>
                          <button @click="exportStudentData(student.id)" class="block w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700">
                            导出数据
                          </button>
                          <hr class="my-1 border-gray-200 dark:border-gray-600" />
                          <button @click="removeStudent(student.id)" class="block w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-gray-100 dark:hover:bg-gray-700">
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
            <button 
              variant="outline" 
              size="sm" 
              @click="currentPage--" 
              :disabled="currentPage === 1"
            >
              上一页
            </button>
            
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
            
            <button 
              variant="outline" 
              size="sm" 
              @click="currentPage++" 
              :disabled="currentPage === totalPages"
            >
              下一页
            </button>
          </div>
        </div>
      </card>

      <!-- 学生列表 - 卡片视图 -->
      <div v-if="viewMode === 'grid'" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        <card 
          v-for="student in paginatedStudents" 
          :key="student.id"
          padding="lg"
          hoverable
          class="relative"
        >
          <div class="absolute top-4 right-4">
            <input
              type="checkbox"
              :value="student.id"
              v-model="selectedStudents"
              class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
            />
          </div>
          
          <div class="text-center">
            <div class="mx-auto w-16 h-16 mb-4">
              <img 
                v-if="student.avatar" 
                :src="student.avatar" 
                :alt="student.name"
                class="w-16 h-16 rounded-full"
              />
              <div v-else class="w-16 h-16 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center">
                <user-icon class="w-8 h-8 text-gray-400" />
              </div>
            </div>
            
            <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-1">
              {{ student.name }}
            </h3>
            <p class="text-sm text-gray-500 dark:text-gray-400 mb-4">
              {{ student.studentId }}
            </p>
            
            <!-- 进度 -->
            <div class="mb-4">
              <div class="flex justify-between items-center mb-2">
                <span class="text-sm text-gray-600 dark:text-gray-400">学习进度</span>
                <span class="text-sm font-medium">{{ student.progress }}%</span>
              </div>
              <progress :value="student.progress" :max="100" size="sm" />
            </div>
            
            <!-- 统计信息 -->
            <div class="grid grid-cols-2 gap-4 mb-4 text-sm">
              <div class="text-center">
                <div class="font-semibold text-gray-900 dark:text-white">
                  {{ student.averageGrade || '--' }}
                </div>
                <div class="text-gray-500 dark:text-gray-400">平均成绩</div>
              </div>
              <div class="text-center">
                <div class="font-semibold text-gray-900 dark:text-white">
                  {{ student.studyTime }}h
                </div>
                <div class="text-gray-500 dark:text-gray-400">周学习时长</div>
              </div>
            </div>
            
            <!-- 活跃状态 -->
            <div class="flex items-center justify-center space-x-2 mb-4">
              <div
                class="w-2 h-2 rounded-full"
                :class="getActivityColor(student.activityLevel)"
              ></div>
              <span class="text-sm text-gray-600 dark:text-gray-400">
                {{ getActivityText(student.activityLevel) }}
              </span>
            </div>
            
            <!-- 操作按钮 -->
            <div class="flex space-x-2">
              <button variant="outline" size="sm" class="flex-1" @click="viewStudentDetail(student.id)">
                查看详情
              </button>
              <button variant="outline" size="sm" @click="sendMessage(student.id)">
                <chat-bubble-left-icon class="w-4 h-4" />
              </button>
            </div>
          </div>
        </card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
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
  TableCellsIcon,
  Squares2X2Icon,
  UserIcon,
  EyeIcon,
  ChatBubbleLeftIcon,
  EllipsisVerticalIcon
} from '@heroicons/vue/24/outline'

// Router and Stores
const route = useRoute()
const router = useRouter()
const uiStore = useUIStore()

// 状态
const courseId = route.params.id as string
const courseName = ref('高等数学基础教程')
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

// 统计数据
const stats = reactive({
  totalStudents: 156,
  newStudentsThisWeek: 12,
  averageProgress: 78,
  averageGrade: 82.5,
  passRate: 85,
  activeStudents: 142
})

// 模拟学生数据
const students = ref([
  {
    id: '1',
    name: '张同学',
    studentId: '2021001001',
    avatar: '',
    progress: 85,
    completedLessons: 17,
    totalLessons: 20,
    averageGrade: 88,
    activityLevel: 'high',
    studyTime: 12,
    lastActiveAt: '2024-01-15T10:30:00Z',
    joinedAt: '2023-09-01T00:00:00Z'
  },
  {
    id: '2',
    name: '李同学',
    studentId: '2021001002',
    avatar: '',
    progress: 92,
    completedLessons: 18,
    totalLessons: 20,
    averageGrade: 92,
    activityLevel: 'high',
    studyTime: 15,
    lastActiveAt: '2024-01-15T09:15:00Z',
    joinedAt: '2023-09-01T00:00:00Z'
  },
  {
    id: '3',
    name: '王同学',
    studentId: '2021001003',
    avatar: '',
    progress: 65,
    completedLessons: 13,
    totalLessons: 20,
    averageGrade: 75,
    activityLevel: 'medium',
    studyTime: 8,
    lastActiveAt: '2024-01-14T16:20:00Z',
    joinedAt: '2023-09-01T00:00:00Z'
  },
  {
    id: '4',
    name: '刘同学',
    studentId: '2021001004',
    avatar: '',
    progress: 45,
    completedLessons: 9,
    totalLessons: 20,
    averageGrade: 68,
    activityLevel: 'low',
    studyTime: 5,
    lastActiveAt: '2024-01-13T14:45:00Z',
    joinedAt: '2023-09-01T00:00:00Z'
  },
  {
    id: '5',
    name: '陈同学',
    studentId: '2021001005',
    avatar: '',
    progress: 30,
    completedLessons: 6,
    totalLessons: 20,
    averageGrade: 55,
    activityLevel: 'inactive',
    studyTime: 2,
    lastActiveAt: '2024-01-10T11:30:00Z',
    joinedAt: '2023-09-01T00:00:00Z'
  }
  // 更多学生数据...
])

// 计算属性
const filteredStudents = computed(() => {
  return students.value.filter(student => {
    // 搜索筛选
    if (searchQuery.value) {
      const query = searchQuery.value.toLowerCase()
      if (!student.name.toLowerCase().includes(query) && 
          !student.studentId.toLowerCase().includes(query)) {
        return false
      }
    }

    // 进度筛选
    if (progressFilter.value) {
      switch (progressFilter.value) {
        case 'not-started':
          if (student.progress > 0) return false
          break
        case 'in-progress':
          if (student.progress === 0 || student.progress === 100) return false
          break
        case 'completed':
          if (student.progress < 100) return false
          break
      }
    }

    // 成绩筛选
    if (gradeFilter.value && student.averageGrade) {
      switch (gradeFilter.value) {
        case 'excellent':
          if (student.averageGrade < 90) return false
          break
        case 'good':
          if (student.averageGrade < 80 || student.averageGrade >= 90) return false
          break
        case 'average':
          if (student.averageGrade < 70 || student.averageGrade >= 80) return false
          break
        case 'below':
          if (student.averageGrade >= 70) return false
          break
      }
    }

    // 活跃度筛选
    if (activityFilter.value) {
      if (student.activityLevel !== activityFilter.value) return false
    }

    return true
  }).sort((a, b) => {
    switch (sortBy.value) {
      case 'name':
        return a.name.localeCompare(b.name)
      case 'progress':
        return b.progress - a.progress
      case 'grade':
        return (b.averageGrade || 0) - (a.averageGrade || 0)
      case 'lastActive':
        return new Date(b.lastActiveAt).getTime() - new Date(a.lastActiveAt).getTime()
      case 'joinDate':
        return new Date(b.joinedAt).getTime() - new Date(a.joinedAt).getTime()
      default:
        return 0
    }
  })
})

const totalPages = computed(() => Math.ceil(filteredStudents.value.length / pageSize.value))

const paginatedStudents = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredStudents.value.slice(start, end)
})

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

const sendMessage = (studentId: string) => {
  uiStore.showNotification({
    type: 'info',
    title: '发送消息',
    message: '消息功能开发中...'
  })
}

const viewGrades = (studentId: string) => {
        router.push(`/teacher/students/${studentId}`)
}

const resetProgress = async (studentId: string) => {
  if (confirm('确定要重置该学生的学习进度吗？')) {
    try {
      await new Promise(resolve => setTimeout(resolve, 1000))
      const student = students.value.find(s => s.id === studentId)
      if (student) {
        student.progress = 0
        student.completedLessons = 0
      }
      
      uiStore.showNotification({
        type: 'success',
        title: '重置成功',
        message: '学生学习进度已重置'
      })
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '重置失败',
        message: '重置进度时发生错误'
      })
    }
  }
}

const exportStudentData = async (studentId: string) => {
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    uiStore.showNotification({
      type: 'success',
      title: '导出成功',
      message: '学生数据已导出'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '导出失败',
      message: '导出数据时发生错误'
    })
  }
}

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

const batchSendMessage = () => {
  uiStore.showNotification({
    type: 'info',
    title: '批量发送消息',
    message: '消息功能开发中...'
  })
}

const batchExport = async () => {
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    uiStore.showNotification({
      type: 'success',
      title: '批量导出成功',
      message: `已导出 ${selectedStudents.value.length} 名学生的数据`
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '导出失败',
      message: '批量导出时发生错误'
    })
  }
}

const batchRemove = async () => {
  if (confirm(`确定要移除选中的 ${selectedStudents.value.length} 名学生吗？`)) {
    try {
      await new Promise(resolve => setTimeout(resolve, 1500))
      students.value = students.value.filter(s => !selectedStudents.value.includes(s.id))
      selectedStudents.value = []
      
      uiStore.showNotification({
        type: 'success',
        title: '批量移除成功',
        message: '选中的学生已从课程中移除'
      })
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '移除失败',
        message: '批量移除时发生错误'
      })
    }
  }
}

const exportData = async () => {
  try {
    uiStore.showNotification({
      type: 'info',
      title: '导出中...',
      message: '正在生成学生数据报告'
    })
    
    await new Promise(resolve => setTimeout(resolve, 3000))
    
    uiStore.showNotification({
      type: 'success',
      title: '导出完成',
      message: '学生数据已导出到本地'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '导出失败',
      message: '导出数据时发生错误'
    })
  }
}

const inviteStudents = () => {
  uiStore.showNotification({
    type: 'info',
    title: '邀请学生',
    message: '邀请功能开发中...'
  })
}

// 生命周期
onMounted(() => {
  // 点击外部关闭菜单
  document.addEventListener('click', () => {
    showStudentMenu.value = null
  })
})

onUnmounted(() => {
  document.removeEventListener('click', () => {
    showStudentMenu.value = null
  })
})
</script> 