<template>
  <div class="min-h-screen p-6">
    <div class="max-w-7xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <nav class="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400 mb-2">
              <router-link to="/teacher/courses" class="hover:text-gray-700 dark:hover:text-gray-200">
                {{ t('teacher.students.breadcrumb.courses') }}
              </router-link>
              <chevron-right-icon class="w-4 h-4" />
              <router-link :to="`/teacher/courses/${courseId}`" class="hover:text-gray-700 dark:hover:text-gray-200">
                {{ courseName }}
              </router-link>
              <chevron-right-icon class="w-4 h-4" />
              <span>{{ t('teacher.students.breadcrumb.self') }}</span>
            </nav>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">{{ t('teacher.students.title') }}</h1>
            <p class="text-gray-600 dark:text-gray-400">{{ t('teacher.students.subtitle') }}</p>
          </div>
          <div class="flex items-center space-x-3">
            <Button variant="indigo" @click="openInviteModal">
              <UserPlusIcon class="w-4 h-4 mr-2" />
              {{ t('teacher.students.actions.invite') }}
            </Button>
            <Button variant="teal" @click="exportData">
              <ArrowDownTrayIcon class="w-4 h-4 mr-2" />
              {{ t('teacher.students.actions.export') }}
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
          <p class="text-sm text-gray-600 dark:text-gray-400">{{ t('teacher.students.cards.total') }}</p>
          <div class="mt-2 flex items-center justify-center space-x-1">
             <ArrowTrendingUpIcon class="w-4 h-4 text-green-500" />
            <span class="text-xs text-green-600">{{ t('teacher.students.cards.newThisWeek', { count: stats.newStudentsThisWeek }) }}</span>
          </div>
        </card>
        <card padding="lg" class="text-center">
          <div class="text-3xl font-bold text-green-600 dark:text-green-400 mb-2">
            {{ stats.averageProgress }}%
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">{{ t('teacher.students.cards.avgProgress') }}</p>
          <div class="mt-2">
             <Progress :value="stats.averageProgress" :max="100" size="sm" color="primary" />
          </div>
        </card>
        <card padding="lg" class="text-center">
          <div class="text-3xl font-bold text-yellow-600 dark:text-yellow-400 mb-2">
            {{ Number(stats.averageGrade || 0).toFixed(1) }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">{{ t('teacher.students.cards.avgGrade') }}</p>
          <div class="mt-2 flex items-center justify-center space-x-1">
             <StarIcon class="w-4 h-4 text-yellow-500" />
            <span class="text-xs text-gray-600">{{ t('teacher.students.cards.passRate', { rate: stats.passRate }) }}</span>
          </div>
        </card>
        <card padding="lg" class="text-center">
          <div class="text-3xl font-bold text-purple-600 dark:text-purple-400 mb-2">
            {{ stats.activeStudents }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">{{ t('teacher.students.cards.active') }}</p>
          <div class="mt-2 flex items-center justify-center space-x-1">
             <ClockIcon class="w-4 h-4 text-blue-500" />
            <span class="text-xs text-blue-600">{{ t('teacher.students.cards.activeThisWeek') }}</span>
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
              <MagnifyingGlassIcon class="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                v-model="searchQuery"
                type="text"
                :placeholder="t('teacher.students.filters.searchPlaceholder')"
                class="input pl-10 w-64"
              />
            </div>

            <!-- 筛选项：后续后端支持后开启提交查询参数（当前保留UI与本地过滤） -->
            <select v-model="progressFilter" class="input w-40">
              <option value="">{{ t('teacher.students.filters.progress.all') }}</option>
              <option value="not-started">{{ t('teacher.students.filters.progress.notStarted') }}</option>
              <option value="in-progress">{{ t('teacher.students.filters.progress.inProgress') }}</option>
              <option value="completed">{{ t('teacher.students.filters.progress.completed') }}</option>
            </select>
            <select v-model="gradeFilter" class="input w-40">
              <option value="">{{ t('teacher.students.filters.grade.all') }}</option>
              <option value="excellent">{{ t('teacher.students.filters.grade.excellent') }}</option>
              <option value="good">{{ t('teacher.students.filters.grade.good') }}</option>
              <option value="average">{{ t('teacher.students.filters.grade.average') }}</option>
              <option value="below">{{ t('teacher.students.filters.grade.below') }}</option>
            </select>
            <select v-model="activityFilter" class="input w-40">
              <option value="">{{ t('teacher.students.filters.activity.all') }}</option>
              <option value="high">{{ t('teacher.students.filters.activity.high') }}</option>
              <option value="medium">{{ t('teacher.students.filters.activity.medium') }}</option>
              <option value="low">{{ t('teacher.students.filters.activity.low') }}</option>
              <option value="inactive">{{ t('teacher.students.filters.activity.inactive') }}</option>
            </select>
          </div>

          <!-- 批量操作 -->
          <div class="flex items-center space-x-3">
             <div v-if="selectedStudents.length > 0" class="flex items-center space-x-2">
               <span class="text-sm text-gray-600 dark:text-gray-400">
                 {{ t('teacher.students.batch.selectedCount', { count: selectedStudents.length }) }}
               </span>
               <Button variant="teal" size="sm" @click="batchExport">
                 <arrow-down-tray-icon class="w-4 h-4 mr-1" />
                 {{ t('teacher.students.batch.export') }}
               </Button>
               <Button variant="outline" size="sm" @click="batchRemove">
                 <user-minus-icon class="w-4 h-4 mr-1" />
                 {{ t('teacher.students.batch.remove') }}
               </Button>
             </div>
            
            <!-- 排序 -->
            <select v-if="selectedStudents.length === 0" v-model="sortBy" class="input input-sm">
              <option value="name">{{ t('teacher.students.filters.sort.name') }}</option>
              <option value="progress">{{ t('teacher.students.filters.sort.progress') }}</option>
              <option value="grade">{{ t('teacher.students.filters.sort.grade') }}</option>
              <option value="lastActive">{{ t('teacher.students.filters.sort.lastActive') }}</option>
              <option value="joinDate">{{ t('teacher.students.filters.sort.joinDate') }}</option>
            </select>

            <!-- 已移除视图切换按钮，固定为表格视图 -->
          </div>
        </div>
      </card>

      <!-- 邀请学生弹窗 -->
      <div v-if="showInviteModal" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
        <div class="bg-white dark:bg-gray-800 w-full max-w-lg rounded-xl shadow-xl border border-gray-200 dark:border-gray-700">
          <div class="p-4 border-b border-gray-200 dark:border-gray-700">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.students.invite.title') }}</h3>
          </div>
          <div class="p-4 space-y-4">
            <p class="text-sm text-gray-600 dark:text-gray-400">{{ t('teacher.students.invite.desc') }}</p>
            <textarea v-model="inviteRaw" rows="6" class="input w-full" :placeholder="t('teacher.students.invite.placeholder')"></textarea>
            <div class="text-xs text-gray-500">{{ t('teacher.students.invite.parsed', { count: parsedInviteIds.length }) }}</div>
          </div>
          <div class="p-4 flex justify-end gap-2 border-t border-gray-200 dark:border-gray-700">
            <Button variant="outline" @click="closeInviteModal">{{ t('teacher.students.invite.cancel') }}</Button>
            <Button variant="teal" :disabled="parsedInviteIds.length===0 || inviting" :loading="inviting" @click="submitInvite">{{ t('teacher.students.invite.confirm') }}</Button>
          </div>
        </div>
      </div>

      <!-- 学生列表 - 表格视图 -->
      <card padding="lg">
        <template #header>
          <div class="flex justify-between items-center">
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.students.table.list') }}</h2>
            <div class="flex items-center space-x-2">
              <span class="text-sm text-gray-500 dark:text-gray-400">
                {{ t('teacher.students.table.total', { count: stats.totalStudents }) }}
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
                  {{ t('teacher.students.table.student') }}
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  {{ t('teacher.students.table.progress') }}
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  {{ t('teacher.students.table.grade') }}
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  {{ t('teacher.students.table.activity') }}
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  {{ t('teacher.students.table.lastActive') }}
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  {{ t('teacher.students.table.actions') }}
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
                      <UserAvatar :avatar="student.avatar" :size="40">
                        <div class="w-10 h-10 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center">
                          <UserIcon class="w-5 h-5 text-gray-400" />
                        </div>
                      </UserAvatar>
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
                     <Badge 
                      v-if="student.averageGrade"
                      :variant="getGradeBadgeVariant(student.averageGrade)"
                      size="sm"
                    >
                      {{ getGradeLevel(student.averageGrade) }}
                     </Badge>
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
                        {{ t('teacher.students.table.studyHours', { hours: student.studyTime }) }}
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
                      {{ t('teacher.students.table.view') }}
                    </Button>
                    <Button variant="purple" size="sm" @click="sendMessage(student.id)">
                      <ChatBubbleLeftIcon class="w-4 h-4 mr-1" />
                      {{ t('teacher.students.table.message') }}
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
                           {{ t('teacher.students.table.viewGrades') }}
                          </button>
                          <button @click="resetProgress(student.id)" class="flex items-center gap-2 w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700">
                             <ArrowPathIcon class="w-4 h-4" />
                             {{ t('teacher.students.table.reset') }}
                          </button>
                          <button @click="exportStudentData(student.id)" class="flex items-center gap-2 w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700">
                             <ArrowDownTrayIcon class="w-4 h-4" />
                             {{ t('teacher.students.table.export') }}
                          </button>
                          <hr class="my-1 border-gray-200 dark:border-gray-600" />
                          <button @click="removeStudent(student.id)" class="flex items-center gap-2 w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-gray-100 dark:hover:bg-gray-700">
                             <UserMinusIcon class="w-4 h-4" />
                             {{ t('teacher.students.table.remove') }}
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

        <!-- 分页（统一为作业管理样式） -->
          <div class="mt-6 flex items-center justify-between">
            <div class="flex items-center space-x-2">
              <span class="text-sm text-gray-700 dark:text-gray-300">{{ t('teacher.assignments.pagination.perPagePrefix') }}</span>
              <select v-model.number="pageSize" class="input input-sm w-20">
                <option :value="10">10</option>
                <option :value="20">20</option>
                <option :value="50">50</option>
              </select>
              <span class="text-sm text-gray-700 dark:text-gray-300">{{ t('teacher.assignments.pagination.perPageSuffix') }}</span>
            </div>
            <div class="flex items-center space-x-2">
              <Button variant="outline" size="sm" @click="currentPage = Math.max(1, currentPage - 1)" :disabled="currentPage === 1">{{ t('teacher.assignments.pagination.prev') }}</Button>
              <span class="text-sm">{{ t('teacher.assignments.pagination.page', { page: currentPage }) }}</span>
              <Button variant="outline" size="sm" @click="currentPage = Math.min(totalPages, currentPage + 1)" :disabled="currentPage >= totalPages">{{ t('teacher.assignments.pagination.next') }}</Button>
            </div>
        </div>
      </card>

      <!-- 已移除卡片视图 -->

      <teleport to="body">
        <ChatDrawer
          :open="chattingOpen"
          :peer-id="chattingPeerId || ''"
          :course-id="courseId"
          :peer-name="chattingPeerName"
          @close="chattingOpen=false"
        />
      </teleport>
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
import UserAvatar from '@/components/ui/UserAvatar.vue'
import ChatDrawer from '@/features/teacher/components/ChatDrawer.vue'
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'

// Router and Stores
const route = useRoute()
const router = useRouter()
const uiStore = useUIStore()
const { t, locale } = useI18n()

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
const chattingPeerId = ref<string | null>(null)
const chattingOpen = ref(false)
const chattingPeerName = computed(() => {
  const pid = chattingPeerId.value
  const stu = students.value.find((s: any) => s.id === pid)
  return (stu && stu.name) ? stu.name : ''
})

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
    const payload: any = await teacherApi.getCourseStudentPerformance(courseId, {
      page: currentPage.value,
      size: pageSize.value,
      search: searchQuery.value || undefined,
      sortBy: sortBy.value || 'name',
      activity: activityFilter.value || undefined,
      grade: gradeFilter.value || undefined,
      progress: progressFilter.value || undefined
    })
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
    uiStore.showNotification({ type: 'error', title: t('teacher.students.load.failTitle'), message: e?.message || t('teacher.students.load.failMsg') })
  }
}

// 计算属性：本地筛选与排序（后端部分能力未就绪时的兜底）
const filteredStudents = computed(() => {
  let arr = students.value.slice()
  // 关键字（本地）补充过滤
  if (searchQuery.value) {
    const q = searchQuery.value.trim().toLowerCase()
    arr = arr.filter((s) =>
      String(s.name || '').toLowerCase().includes(q) ||
      String(s.studentId || '').toLowerCase().includes(q)
    )
  }
  // 进度过滤
  if (progressFilter.value) {
    arr = arr.filter((s) => {
      const p = Number(s.progress || 0)
      if (progressFilter.value === 'not-started') return p <= 0
      if (progressFilter.value === 'in-progress') return p > 0 && p < 100
      if (progressFilter.value === 'completed') return p >= 100
      return true
    })
  }
  // 成绩过滤
  if (gradeFilter.value) {
    arr = arr.filter((s) => {
      const g = Number(s.averageGrade)
      if (!Number.isFinite(g)) return false
      if (gradeFilter.value === 'excellent') return g >= 90
      if (gradeFilter.value === 'good') return g >= 80 && g < 90
      if (gradeFilter.value === 'average') return g >= 70 && g < 80
      if (gradeFilter.value === 'below') return g < 70
      return true
    })
  }
  // 活跃度过滤
  if (activityFilter.value) {
    arr = arr.filter((s) => String(s.activityLevel || '').toLowerCase() === activityFilter.value)
  }
  // 排序
  const by = sortBy.value
  arr.sort((a: any, b: any) => {
    switch (by) {
      case 'progress': return Number(b.progress || 0) - Number(a.progress || 0)
      case 'grade': return Number(b.averageGrade || 0) - Number(a.averageGrade || 0)
      case 'lastActive': return new Date(b.lastActiveAt || 0).getTime() - new Date(a.lastActiveAt || 0).getTime()
      case 'joinDate': return new Date(b.joinedAt || 0).getTime() - new Date(a.joinedAt || 0).getTime()
      default: return String(a.name || '').localeCompare(String(b.name || ''))
    }
  })
  return arr
})

const totalPages = computed(() => Math.max(1, Math.ceil((filteredStudents.value.length || 0) / pageSize.value)))

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

// 基于筛选后的统计（前端兜底）
watch(filteredStudents, (arr) => {
  const avg = (ns: number[]) => (ns.length ? Math.round((ns.reduce((a, b) => a + b, 0) / ns.length) * 100) / 100 : 0)
  stats.totalStudents = arr.length
  stats.averageProgress = avg(arr.map((s: any) => Number(s.progress || 0)))
  const grades = arr.map((s: any) => Number(s.averageGrade)).filter((n) => Number.isFinite(n))
  stats.averageGrade = grades.length ? avg(grades) : 0
}, { immediate: true })

// 方法
const getGradeBadgeVariant = (grade: number) => {
  if (grade >= 90) return 'success'
  if (grade >= 80) return 'primary'
  if (grade >= 70) return 'warning'
  return 'danger'
}

const getGradeLevel = (grade: number) => {
  if (grade >= 90) return t('teacher.students.table.level.excellent')
  if (grade >= 80) return t('teacher.students.table.level.good')
  if (grade >= 70) return t('teacher.students.table.level.average')
  return t('teacher.students.table.level.improve')
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
    case 'high': return t('teacher.students.filters.activity.high')
    case 'medium': return t('teacher.students.filters.activity.medium')
    case 'low': return t('teacher.students.filters.activity.low')
    case 'inactive': return t('teacher.students.filters.activity.inactive')
    default: return t('teacher.students.table.level.unknown')
  }
}

const formatRelativeTime = (timestamp: string) => {
  const now = new Date()
  const time = new Date(timestamp)
  const diff = now.getTime() - time.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (minutes < 60) return String(t('shared.community.list.minutesAgo', { count: minutes }))
  if (hours < 24) return String(t('shared.community.list.hoursAgo', { count: hours }))
  if (days < 7) return String(t('shared.community.list.daysAgo', { count: days }))
  return time.toLocaleDateString(locale.value as unknown as string)
}

const toggleSelectAll = (event: Event) => {
  const checked = (event.target as HTMLInputElement).checked
  selectedStudents.value = checked ? filteredStudents.value.map(s => s.id) : []
}

const toggleStudentMenu = (studentId: string) => {
  showStudentMenu.value = showStudentMenu.value === studentId ? null : studentId
}

const viewStudentDetail = (studentId: string) => {
  const s = students.value.find(x => x.id === studentId)
  router.push({
    path: `/teacher/students/${studentId}`,
    query: {
      name: s?.name || '',
      courseId,
      courseTitle: courseName.value || ''
    }
  })
}

const sendMessage = async (studentId: string) => {
  chattingPeerId.value = studentId
  chattingOpen.value = true
}

const viewGrades = (studentId: string) => {
        router.push(`/teacher/students/${studentId}`)
}

// 移除模拟重置逻辑，保留空函数
const resetProgress = async (studentId: string) => {
  if (!confirm(t('teacher.students.table.confirmReset') as string)) return
  try {
    const { teacherApi } = await import('@/api/teacher.api')
    await teacherApi.resetStudentCourseProgress(courseId, studentId)
    const s = students.value.find(x => x.id === studentId)
    if (s) {
      s.progress = 0
      s.completedLessons = 0
      s.lastActiveAt = new Date().toISOString()
    }
    uiStore.showNotification({ type: 'success', title: t('app.notifications.success.title') as string, message: t('teacher.students.table.resetSuccess') as string })
  } catch (e: any) {
    uiStore.showNotification({ type: 'error', title: t('app.notifications.error.title') as string, message: e?.message || t('teacher.students.table.resetFailed') as string })
  }
}

const exportStudentData = async (_studentId: string) => {
  const s = students.value.find(x => x.id === _studentId)
  if (!s) return
  // 生成单个学生的CSV（前端导出）
  const headers = [
    'student_id','name','progress','completed_lessons','total_lessons','average_grade','activity_level','study_time_per_week','last_active_at','joined_at'
  ]
  const row = [
    `"${String(s.studentId ?? '')}"`,
    `"${String(s.name ?? '').replace(/"/g, '""')}"`,
    String(s.progress ?? ''),
    String(s.completedLessons ?? ''),
    String(s.totalLessons ?? ''),
    String(s.averageGrade ?? ''),
    `"${String(s.activityLevel ?? '')}"`,
    String(s.studyTime ?? ''),
    `"${String(s.lastActiveAt ?? '')}"`,
    `"${String(s.joinedAt ?? '')}"`
  ]
  const csv = `${headers.join(',')}\n${row.join(',')}\n`
  const blob = new Blob([csv], { type: 'text/csv;charset=UTF-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `course_${courseId}_student_${_studentId}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const removeStudent = async (studentId: string) => {
  if (!confirm(t('teacher.students.table.confirmRemove') as string)) return
  try {
    const { courseApi } = await import('@/api/course.api')
    await courseApi.removeStudent(courseId, studentId)
    const index = students.value.findIndex(s => s.id === studentId)
    if (index > -1) students.value.splice(index, 1)
    uiStore.showNotification({
      type: 'success',
      title: t('teacher.students.table.removeSuccess'),
      message: t('teacher.students.table.removeSuccessMsg')
    })
  } catch (error: any) {
    uiStore.showNotification({
      type: 'error',
      title: t('teacher.students.table.removeFail'),
      message: error?.message || t('teacher.students.table.removeFailMsg')
    })
  }
}

const batchSendMessage = async () => {
  if (selectedStudents.value.length === 0) return
  const title = prompt(t('teacher.students.message.promptTitle') as string, t('teacher.students.message.defaultTitle') as string) || ''
  const content = prompt(t('teacher.students.message.promptContent') as string, '') || ''
  if (!title || !content) return
  try {
    const { notificationAPI } = await import('@/api/notification.api')
    const recipientIds = selectedStudents.value.map(id => Number(id))
    await notificationAPI.batchSend({ recipientIds, title, content, type: 'message', category: 'course', relatedType: 'course', relatedId: Number(courseId) })
    uiStore.showNotification({ type: 'success', title: t('app.notifications.success.title') as string, message: t('teacher.students.message.batchSent') as string })
  } catch (e: any) {
    uiStore.showNotification({ type: 'error', title: t('app.notifications.error.title') as string, message: e?.message || t('teacher.students.message.failed') as string })
  }
}

const batchExport = async () => {
  if (selectedStudents.value.length === 0) return
  // headers 与单个导出保持一致
  const headers = [
    'student_id','name','progress','completed_lessons','total_lessons','average_grade','activity_level','study_time_per_week','last_active_at','joined_at'
  ]
  const selected = students.value.filter(s => selectedStudents.value.includes(s.id))
  if (selected.length === 0) return
  const escape = (v: any) => `"${String(v ?? '').replace(/"/g, '""')}"`
  const rows = selected.map((s) => [
    escape(s.studentId),
    escape(s.name),
    String(s.progress ?? ''),
    String(s.completedLessons ?? ''),
    String(s.totalLessons ?? ''),
    String(s.averageGrade ?? ''),
    escape(s.activityLevel),
    String(s.studyTime ?? ''),
    escape(s.lastActiveAt),
    escape(s.joinedAt)
  ].join(','))
  const csv = `${headers.join(',')}\n${rows.join('\n')}\n`
  const blob = new Blob([csv], { type: 'text/csv;charset=UTF-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `course_${courseId}_students_selected.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  uiStore.showNotification({ type: 'success', title: t('app.notifications.success.title') as string, message: `${selected.length} ${t('teacher.students.batch.export')}` })
}

const batchRemove = async () => {
  if (selectedStudents.value.length === 0) return
  if (!confirm(t('teacher.students.table.confirmRemove') as string)) return
  try {
    const { courseApi } = await import('@/api/course.api')
    const ids = [...selectedStudents.value]
    // 逐个调用后端移除
    const results = await Promise.allSettled(ids.map(id => courseApi.removeStudent(courseId, id)))
    // 成功的ID集合
    const successIds = ids.filter((_, idx) => results[idx].status === 'fulfilled')
    if (successIds.length > 0) {
      students.value = students.value.filter(s => !successIds.includes(s.id))
    }
    // 清空选择
    selectedStudents.value = []
    const okCount = successIds.length
    const failCount = ids.length - okCount
    if (okCount > 0) {
      uiStore.showNotification({ type: 'success', title: t('teacher.students.table.removeSuccess') as string, message: `${okCount}` })
    }
    if (failCount > 0) {
      uiStore.showNotification({ type: 'warning', title: t('teacher.students.table.removeFail') as string, message: `${failCount}` })
    }
  } catch (error: any) {
    uiStore.showNotification({ type: 'error', title: t('teacher.students.table.removeFail') as string, message: error?.message || '' })
  }
}

const exportData = async () => {
  try {
    const { teacherApi } = await import('@/api/teacher.api')
    const params: any = {}
    if (searchQuery.value) params.search = searchQuery.value
    if (sortBy.value) params.sortBy = sortBy.value
    if (activityFilter.value) params.activity = activityFilter.value
    if (gradeFilter.value) params.grade = gradeFilter.value
    if (progressFilter.value) params.progress = progressFilter.value
    const res: any = await teacherApi.exportCourseStudents(courseId, params)
    const blob = new Blob([res as any], { type: 'text/csv;charset=UTF-8' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `course_${courseId}_students.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (e: any) {
    uiStore.showNotification({ type: 'error', title: t('teacher.students.export.failTitle'), message: e?.message || t('teacher.students.export.failMsg') })
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
      title: t('teacher.students.invite.successTitle'),
      message: t('teacher.students.invite.successMsg', { count: parsedInviteIds.value.length })
    })
    closeInviteModal()
    await fetchCourseStudents()
  } catch (e: any) {
    uiStore.showNotification({ type: 'error', title: t('teacher.students.invite.failTitle'), message: e?.message || t('teacher.students.invite.failMsg') })
  } finally {
    inviting.value = false
  }
}

// 生命周期
const handleDocClick = () => { showStudentMenu.value = null }

onMounted(async () => {
  document.addEventListener('click', handleDocClick)
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
  document.removeEventListener('click', handleDocClick)
})
 </script> 