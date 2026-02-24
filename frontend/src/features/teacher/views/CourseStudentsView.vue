<template>
  <div class="min-h-screen p-6">
    <div class="max-w-7xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
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
        <page-header :title="t('teacher.students.title')" :subtitle="t('teacher.students.subtitle')">
          <template #actions>
            <div class="flex items-center space-x-3">
              <Button variant="primary" @click="openInviteModal">
                <user-plus-icon class="w-4 h-4 mr-2" />
                {{ t('teacher.students.actions.invite') }}
              </Button>
              <Button variant="purple" @click="openKeyModal">
                <lock-closed-icon class="w-4 h-4 mr-2" />
                {{ t('teacher.students.actions.setEnrollKey') }}
              </Button>
              <Button variant="success" @click="exportData">
                <arrow-down-tray-icon class="w-4 h-4 mr-2" />
                {{ t('teacher.students.actions.export') }}
              </Button>
            </div>
          </template>
        </page-header>
      </div>

      <!-- 统计卡片（复用 /ui/StatCard 彩色卡片 + 图标） -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <start-card :label="t('teacher.students.cards.total') as string" :value="stats.totalStudents" tone="blue" :icon="UserGroupIcon" />
        <start-card :label="t('teacher.students.cards.avgProgress') as string" :value="`${stats.averageProgress}%`" tone="emerald" :icon="ArrowTrendingUpIcon" />
        <start-card :label="t('teacher.students.cards.avgGrade') as string" :value="Number(stats.averageGrade || 0).toFixed(1)" tone="amber" :icon="StarIcon" />
        <start-card :label="t('teacher.students.cards.active') as string" :value="stats.activeStudents" tone="violet" :icon="ClockIcon" />
      </div>

      <!-- 筛选与操作（降低容器高度，去除控件阴影） -->
      <div class="space-y-6">
        <card padding="sm" tint="secondary">
          <div class="filters-row flex items-center flex-nowrap gap-2 overflow-x-auto whitespace-nowrap py-1">
          <!-- 三个过滤器（横向排列） -->
          <span class="text-xs text-gray-600 dark:text-gray-300 mr-1">{{ t('teacher.students.filters.labels.progress') }}</span>
          <glass-popover-select
            v-model="progressFilter"
            :options="progressOptions"
            size="sm"
            width="9rem"
            :fullWidth="false"
          />
          <span class="text-xs text-gray-600 dark:text-gray-300 mr-1">{{ t('teacher.students.filters.labels.grade') }}</span>
          <glass-popover-select
            v-model="gradeFilter"
            :options="gradeOptions"
            size="sm"
            width="10rem"
            :fullWidth="false"
          />
          <span class="text-xs text-gray-600 dark:text-gray-300 mr-1">{{ t('teacher.students.filters.labels.activity') }}</span>
          <glass-popover-select
            v-model="activityFilter"
            :options="activityOptions"
            size="sm"
            width="9rem"
            :fullWidth="false"
          />
          <!-- 排序选择器（与前面控件保持相同间距与高度） / 批量操作互斥 -->
          <div v-if="selectedStudents.length === 0">
            <span class="text-xs text-gray-600 dark:text-gray-300 mr-1">{{ t('teacher.students.filters.labels.sort') }}</span>
            <glass-popover-select
              v-model="sortBy"
              :options="sortOptions"
              size="sm"
              width="12rem"
              :fullWidth="false"
            />
          </div>

          <!-- 批量操作（出现时替代排序，并右对齐） -->
          <div v-else class="flex items-center gap-2">
            <span class="text-sm text-gray-600 dark:text-gray-400">
              {{ t('teacher.students.batch.selectedCount', { count: selectedStudents.length }) }}
            </span>
            <Button variant="success" size="sm" @click="batchExport">
              <arrow-down-tray-icon class="w-4 h-4 mr-1" />
              {{ t('teacher.students.batch.export') }}
            </Button>
            <Button variant="outline" size="sm" @click="batchRemove">
              <user-minus-icon class="w-4 h-4 mr-1" />
              {{ t('teacher.students.batch.remove') }}
            </Button>
          </div>

          <!-- 搜索框（右对齐） -->
          <div class="ml-auto relative">
            <glass-search-input v-model="searchQuery" :placeholder="t('teacher.students.filters.searchPlaceholder')" size="sm" class="w-56" />
          </div>
        </div>
        </card>

        <!-- 学生列表 - 表格视图 -->
        <card padding="lg" tint="info">
        <template #header>
          <div class="flex justify-between items-center">
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.students.table.list') }}</h2>
          </div>
        </template>

        <div class="overflow-x-auto no-scrollbar rounded-xl overflow-hidden glass-regular glass-interactive border border-gray-200/40 dark:border-gray-700/40">
          <table class="min-w-full divide-y divide-white/10">
            <thead class="bg-white/5 dark:bg-white/5 backdrop-blur-sm">
              <tr>
                <th class="px-8 py-3 text-center w-14">
                  <input
                    type="checkbox"
                    :checked="selectedStudents.length === filteredStudents.length"
                    @change="toggleSelectAll"
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                </th>
                <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">
                  {{ t('teacher.students.table.student') }}
                </th>
                <th class="px-7 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide w-48">
                  {{ t('teacher.students.table.progress') }}
                </th>
                <th class="px-9 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide w-[260px]">
                  {{ t('teacher.students.table.radar') }}
                </th>
                <!-- 成绩列：缩小一点横向占用（避免英文版挤不下），但不换行；不调整标题字号 -->
                <th class="px-5 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">
                  {{ t('teacher.students.table.grade') }}
                </th>
                <!-- 活跃度列：缩小一点横向占用（避免英文版挤不下），但不换行；不调整标题字号 -->
                <th class="px-4 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap w-24">
                  {{ t('teacher.students.table.activity') }}
                </th>
                <th class="px-7 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide w-28">
                  <!-- 中文：两字一行（最后/活动）；英文：保持单行 -->
                  <span v-if="isZhLocale" class="inline-block leading-tight">
                    {{ lastActiveHeaderLine1 }}<br />
                    {{ lastActiveHeaderLine2 }}
                  </span>
                  <span v-else class="whitespace-nowrap">
                    {{ t('teacher.students.table.lastActive') }}
                  </span>
                </th>
                <th class="px-4 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide w-16">
                  {{ t('teacher.students.table.actions') }}
                </th>
              </tr>
            </thead>
            <tbody class="bg-transparent divide-y divide-white/10">
              <tr v-for="student in paginatedStudents" :key="student.id" class="hover:bg-white/10 transition-colors duration-150">
                <td class="px-8 py-3 text-center">
                  <div class="inline-flex items-center justify-center">
                    <input
                      type="checkbox"
                      :value="student.id"
                      v-model="selectedStudents"
                      class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    />
                  </div>
                </td>
                <td class="px-6 py-3 text-center">
                  <div class="flex items-center justify-center gap-3">
                    <div class="flex-shrink-0 w-10 h-10">
                      <user-avatar :avatar="student.avatar" :size="40">
                        <div class="w-10 h-10 rounded-full bg-gray-200 dark:bg-gray-600 ring-1 ring-white/20 flex items-center justify-center">
                          <user-icon class="w-5 h-5 text-gray-400" />
                        </div>
                      </user-avatar>
                    </div>
                    <div class="text-center">
                      <div class="text-sm font-medium text-gray-900 dark:text-white">
                        {{ student.name }}
                      </div>
                      <div class="text-sm text-gray-500 dark:text-gray-400">
                        {{ student.studentId }}
                      </div>
                    </div>
                  </div>
                </td>
                <td class="px-7 py-3 text-center">
                  <div class="flex items-center justify-center max-w-[180px] mx-auto">
                    <div class="flex-1">
                      <div class="flex justify-between items-center mb-1">
                        <span class="text-sm font-medium text-gray-900 dark:text-white">
                          {{ student.progress }}%
                        </span>
                        <span class="text-xs text-gray-500">
                          {{ student.completedLessons }}/{{ student.totalLessons }}
                        </span>
                      </div>
                      <Progress
                        :value="Number(student.progress || 0)"
                        size="sm"
                        :color="Number(student.progress || 0) >= 100 ? 'success' : 'primary'"
                      />
                    </div>
                  </div>
                </td>
                <td class="px-9 py-3 min-w-[260px] text-center">
                  <div class="flex flex-col gap-2 items-center">
                    <div class="flex items-center justify-center gap-2">
                      <badge :variant="getRadarBadgeVariant(student.radarClassification)" size="sm" class="px-2 font-semibold uppercase tracking-wide">
                        {{ formatRadarBadgeLabel(student.radarClassification) }}
                      </badge>
                      <span class="text-sm font-semibold text-gray-900 dark:text-white">
                        {{ t('teacher.students.table.radarAreaLabel', { value: formatRadarArea(student.radarArea) }) }}
                      </span>
                    </div>
                    <div v-if="student.dimensionScores && Object.keys(student.dimensionScores).length" class="flex flex-wrap justify-center gap-x-3 gap-y-1 text-xs text-gray-600 dark:text-gray-400">
                      <span
                        v-for="([code, value], idx) in Object.entries(student.dimensionScores || {})"
                        :key="`${student.id}-${code}-${idx}`"
                        class="inline-flex items-center gap-1 uppercase"
                      >
                        <span class="font-medium">{{ formatDimensionLabel(code) }}</span>
                        <span class="text-gray-400">·</span>
                        <span>{{ formatRadarValue(value) }}</span>
                      </span>
                    </div>
                    <div v-else class="text-xs text-gray-400 dark:text-gray-500 text-center">
                      {{ t('teacher.students.table.noRadarData') }}
                    </div>
                  </div>
                </td>
                <td class="px-5 py-3 min-w-[112px] text-center">
                  <div class="flex items-center justify-center">
                    <span class="text-sm font-medium text-gray-900 dark:text-white">
                      {{ student.averageGrade || '--' }}
                    </span>
                     <badge 
                      v-if="student.averageGrade"
                      :variant="getGradeBadgeVariant(student.averageGrade)"
                      size="sm"
                      class="ml-2 whitespace-nowrap"
                    >
                      {{ getGradeLevel(student.averageGrade) }}
                     </badge>
                  </div>
                </td>
                <td class="px-4 py-3 min-w-[120px] text-center">
                  <div class="flex items-center justify-center">
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
                <td class="px-7 py-3 text-sm text-gray-500 dark:text-gray-400 text-center">
                  {{ formatRelativeTime(student.lastActiveAt) }}
                </td>
                <td class="px-4 py-4 text-center">
                  <div class="flex justify-center">
                    <div class="relative" @click.stop :ref="(el: Element | ComponentPublicInstance | null) => setMenuButtonRef((el as HTMLElement) ?? null, student.id)">
                      <Button
                        variant="ghost"
                        size="sm"
                        @click="toggleStudentMenu(student.id)"
                      >
                        <ellipsis-vertical-icon class="w-4 h-4" />
                      </Button>
                      <teleport to="body">
                        <div
                          v-if="showStudentMenu === student.id"
                          ref="menuRef"
                          class="fixed z-[9999] rounded-2xl shadow-lg popover-glass border border-white/25 dark:border-white/10 overflow-y-auto no-scrollbar p-1"
                          :style="{ left: menuPos.left + 'px', top: menuPos.top + 'px', width: menuPos.width + 'px', maxHeight: '200px' }"
                          @click.stop
                        >
                          <div class="py-1 space-y-1">
                            <Button variant="menu" size="sm" class="w-full justify-start gap-2" @click="handleStudentView(student.id)">
                              <eye-icon class="w-4 h-4" />
                              {{ t('teacher.students.table.view') }}
                            </Button>
                            <Button variant="menu" size="sm" class="w-full justify-start gap-2" @click="handleStudentMessage(student.id)">
                              <chat-bubble-left-icon class="w-4 h-4" />
                              {{ t('teacher.students.table.message') }}
                            </Button>
                            <Button variant="menu" size="sm" class="w-full justify-start gap-2" @click="resetProgress(student.id)">
                               <arrow-path-icon class="w-4 h-4" />
                               {{ t('teacher.students.table.reset') }}
                            </Button>
                            <Button variant="menu" size="sm" class="w-full justify-start gap-2" @click="exportStudentData(student.id)">
                               <arrow-down-tray-icon class="w-4 h-4" />
                               {{ t('teacher.students.table.export') }}
                            </Button>
                            <hr class="my-1 border-white/10" />
                            <Button variant="danger" size="sm" class="w-full justify-start gap-2" @click="removeStudent(student.id)">
                               <user-minus-icon class="w-4 h-4" />
                               {{ t('teacher.students.table.remove') }}
                            </Button>
                          </div>
                        </div>
                      </teleport>
                    </div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 分页（抽离为统一组件） -->
        <pagination-bar
          :page="currentPage"
          :page-size="pageSize"
          :total-pages="totalPages"
          :total-items="serverTotal"
          :page-size-options="[10, 20, 50]"
          @update:page="(p:number)=> currentPage = p"
          @update:pageSize="(s:number)=> pageSize = s"
        />
        </card>
      </div>

      <!-- 邀请学生弹窗（复用 GlassModal） -->
      <glass-modal
        v-if="showInviteModal"
        :title="t('teacher.students.invite.title') as string"
        size="xl"
        heightVariant="tall"
        :solidBody="true"
        @close="closeInviteModal"
      >
        <div class="space-y-4">
          <p class="text-sm text-gray-600 dark:text-gray-400">{{ t('teacher.students.invite.desc') }}</p>
          <glass-textarea v-model="inviteRaw" :rows="6" :placeholder="t('teacher.students.invite.placeholder') as string" />
          <div class="text-xs text-gray-500">{{ t('teacher.students.invite.parsed', { count: parsedInviteIds.length }) }}</div>
        </div>
        <template #footer>
          <Button variant="secondary" @click="closeInviteModal">{{ t('teacher.students.invite.cancel') }}</Button>
          <Button variant="teal" :disabled="parsedInviteIds.length===0 || inviting" :loading="inviting" @click="submitInvite">{{ t('teacher.students.invite.confirm') }}</Button>
        </template>
      </glass-modal>

      <!-- 课程入课密钥设置弹窗（复用 GlassModal） -->
      <glass-modal
        v-if="showKeyModal"
        :title="t('teacher.students.enrollKey.title') as string"
        size="lg"
        heightVariant="tall"
        :solidBody="true"
        @close="closeKeyModal"
      >
        <div class="space-y-4">
          <div class="flex items-center gap-3">
            <span class="text-sm text-gray-700 dark:text-gray-300">{{ t('teacher.students.enrollKey.require') }}</span>
            <glass-switch v-model="requireKey" size="md" />
          </div>
          <div>
            <label class="block text-sm mb-1">{{ t('teacher.students.enrollKey.keyLabel') }}</label>
            <glass-input v-model="inputKey" :disabled="!requireKey" type="password" :placeholder="t('teacher.students.enrollKey.placeholder') as string" />
          </div>
        </div>
        <template #footer>
          <Button variant="secondary" @click="closeKeyModal">{{ t('teacher.students.invite.cancel') }}</Button>
          <Button variant="teal" :loading="savingKey" @click="saveEnrollKey">{{ t('teacher.students.enrollKey.save') }}</Button>
        </template>
      </glass-modal>

      <!-- 已移除卡片视图 -->

      <!-- 改为调用全局抽屉：删除本地 Teleport -->
      <confirm-dialog
        :open="confirmOpen"
        :title="confirmDialog.state.title"
        :message="confirmDialog.state.message"
        :confirm-text="confirmDialog.state.confirmText || ((t('common.confirm') as string) || '确定')"
        :cancel-text="confirmDialog.state.cancelText || ((t('common.cancel') as string) || '取消')"
        :confirm-variant="confirmDialog.state.confirmVariant"
        @confirm="confirmDialog.onConfirm"
        @cancel="confirmDialog.onCancel"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick, type ComponentPublicInstance } from 'vue'
import apiClient, { baseURL } from '@/api/config'
import { useRoute, useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Progress from '@/components/ui/Progress.vue'
// StartCard 不再使用，改用彩色 StatCard
import StartCard from '@/components/ui/StartCard.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import { 
  ChevronRightIcon,
  DocumentArrowDownIcon,
  UserPlusIcon,
  ArrowTrendingUpIcon,
  StarIcon,
  ClockIcon,
  UserGroupIcon,
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
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import { LockClosedIcon } from '@heroicons/vue/24/outline'
import GlassSwitch from '@/components/ui/inputs/GlassSwitch.vue'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import { useConfirmDialog } from '@/shared/composables/useConfirmDialog'
import { useCourseStore } from '@/stores/course'
import { useChatStore } from '@/stores/chat'
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'
import { debounce } from '@/shared/utils/debounce'

// Router and Stores
const route = useRoute()
const router = useRouter()
const uiStore = useUIStore()
const { t, locale } = useI18n()

const confirmDialog = useConfirmDialog({
  confirmText: (t('common.confirm') as string) || '确定',
  cancelText: (t('common.cancel') as string) || '取消',
  confirmVariant: 'danger',
})
const confirmOpen = computed(() => confirmDialog.open.value)

// 表头细节：仅中文将“最后活动”拆成两行（最后/活动），英文保持单行
const isZhLocale = computed(() => String(locale.value || '').toLowerCase().startsWith('zh'))
const lastActiveHeaderLine1 = computed(() => '最后')
const lastActiveHeaderLine2 = computed(() => '活动')

// 状态
const courseId = route.params.id as string
const courseName = ref('')
const searchQuery = ref('')
// 搜索输入做“本地防抖”，避免每次按键都触发前端重算
const debouncedSearchQuery = ref('')
const progressFilter = ref('')
const gradeFilter = ref('')
const activityFilter = ref('')
const sortBy = ref('radar')
const viewMode = ref('table')
const selectedStudents = ref<string[]>([])
const showStudentMenu = ref<string | null>(null)
const currentPage = ref(1)
const pageSize = ref(20)
const chat = useChatStore()

// 三点菜单定位
const menuButtonMap = new Map<string, HTMLElement>()
const menuPos = ref({ left: 0, top: 0, width: 160 })
const menuRef = ref<HTMLElement | null>(null)

// 仅当菜单打开时绑定 scroll/resize，避免泄漏与无意义回调
let followHandler: (() => void) | null = null
function bindMenuFollowHandler() {
  if (followHandler) return
  followHandler = () => {
    if (!showStudentMenu.value) return
    computeMenuPos(showStudentMenu.value)
  }
  window.addEventListener('scroll', followHandler, { passive: true })
  window.addEventListener('resize', followHandler, { passive: true })
}
function unbindMenuFollowHandler() {
  if (!followHandler) return
  window.removeEventListener('scroll', followHandler)
  window.removeEventListener('resize', followHandler)
  followHandler = null
}

// Glass 选项
const progressOptions = computed(() => ([
  { label: t('teacher.students.filters.progress.all') as string, value: '' },
  { label: t('teacher.students.filters.progress.notStarted') as string, value: 'not-started' },
  { label: t('teacher.students.filters.progress.inProgress') as string, value: 'in-progress' },
  { label: t('teacher.students.filters.progress.completed') as string, value: 'completed' },
]))

const gradeOptions = computed(() => ([
  { label: t('teacher.students.filters.grade.all') as string, value: '' },
  { label: t('teacher.students.filters.grade.excellent') as string, value: 'excellent' },
  { label: t('teacher.students.filters.grade.good') as string, value: 'good' },
  { label: t('teacher.students.filters.grade.average') as string, value: 'average' },
  { label: t('teacher.students.filters.grade.below') as string, value: 'below' },
]))

const activityOptions = computed(() => ([
  { label: t('teacher.students.filters.activity.all') as string, value: '' },
  { label: t('teacher.students.filters.activity.high') as string, value: 'high' },
  { label: t('teacher.students.filters.activity.medium') as string, value: 'medium' },
  { label: t('teacher.students.filters.activity.low') as string, value: 'low' },
  { label: t('teacher.students.filters.activity.inactive') as string, value: 'inactive' },
]))

const sortOptions = computed(() => ([
  { label: t('teacher.students.filters.sort.radar') as string, value: 'radar' },
  { label: t('teacher.students.filters.sort.name') as string, value: 'name' },
  { label: t('teacher.students.filters.sort.progress') as string, value: 'progress' },
  { label: t('teacher.students.filters.sort.grade') as string, value: 'grade' },
  { label: t('teacher.students.filters.sort.lastActive') as string, value: 'lastActive' },
  { label: t('teacher.students.filters.sort.activity') as string, value: 'activity' },
]))

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
const serverTotal = ref<number>(0)

let fetchSeq = 0

const fetchCourseNameOnce = async () => {
  if (courseName.value) return
  try {
    const { courseApi } = await import('@/api/course.api')
    const courseRes: any = await courseApi.getCourseById(Number(courseId))
    courseName.value = courseRes?.title || ''
  } catch { /* empty */ }
}

const fetchCourseStudents = async () => {
  const seq = ++fetchSeq
  await fetchCourseNameOnce()
  try {
    const { teacherApi } = await import('@/api/teacher.api')
    const payload: any = await teacherApi.getCourseStudentPerformance(courseId, {
      page: currentPage.value,
      size: pageSize.value,
      search: searchQuery.value || undefined,
      sortBy: sortBy.value || 'radar',
      activity: activityFilter.value || undefined,
      grade: gradeFilter.value || undefined,
      progress: progressFilter.value || undefined
    })
    // 仅应用“最新一次”请求结果，避免快速切换筛选导致的乱序覆盖
    if (seq !== fetchSeq) return
    const items = payload?.items || []
    students.value = items.map((i: any) => ({
      id: String(i.studentId),
      name: buildDisplayName(i),
      studentId: buildStudentIdentifier(i),
      avatar: i.avatar || '',
      progress: i.progress ?? 0,
      completedLessons: i.completedLessons ?? 0,
      totalLessons: i.totalLessons ?? 0,
      averageGrade: i.averageGrade ?? undefined,
      activityLevel: i.activityLevel || 'medium',
      studyTime: i.studyTimePerWeek || 0,
      lastActiveAt: i.lastActiveAt || new Date().toISOString(),
      joinedAt: new Date().toISOString(),
      radarArea: i.radarArea ?? 0,
      radarClassification: i.radarClassification || 'D',
      dimensionScores: i.dimensionScores || {}
    }))
    serverTotal.value = Number(payload?.total || items.length || 0)
    stats.totalStudents = serverTotal.value
    // 汇总统计（后端返回全集过滤统计）
    stats.averageProgress = payload?.averageProgress ?? 0
    stats.averageGrade = payload?.averageGrade ?? 0
    stats.activeStudents = payload?.activeStudents ?? 0
    stats.passRate = payload?.passRate ?? 0
  } catch (e: any) {
    if (seq !== fetchSeq) return
    uiStore.showNotification({ type: 'error', title: t('teacher.students.load.failTitle'), message: e?.message || t('teacher.students.load.failMsg') })
  }
}

// 计算属性：本地筛选与排序（后端部分能力未就绪时的兜底）
const filteredStudents = computed(() => {
  let arr = students.value.slice()
  // 关键字（本地）补充过滤
  if (debouncedSearchQuery.value) {
    const q = debouncedSearchQuery.value
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
      case 'radar': return Number(b.radarArea || 0) - Number(a.radarArea || 0)
      case 'progress': return Number(b.progress || 0) - Number(a.progress || 0)
      case 'grade': return Number(b.averageGrade || 0) - Number(a.averageGrade || 0)
      case 'lastActive': return new Date(b.lastActiveAt || 0).getTime() - new Date(a.lastActiveAt || 0).getTime()
      case 'joinDate': return new Date(b.joinedAt || 0).getTime() - new Date(a.joinedAt || 0).getTime()
      default: return String(a.name || '').localeCompare(String(b.name || ''))
    }
  })
  return arr
})

const totalPages = computed(() => Math.max(1, Math.ceil((serverTotal.value || 0) / pageSize.value)))

const paginatedStudents = computed(() => {
  // 服务器已分页返回本页数据，这里仅做本地二次过滤（关键词/兜底）
  return filteredStudents.value
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
watch(students, (arr) => {
  const avg = (ns: number[]) => (ns.length ? Math.round((ns.reduce((a, b) => a + b, 0) / ns.length) * 100) / 100 : 0)
  // 总人数使用服务端 total，避免被当前页数量覆盖
  stats.totalStudents = serverTotal.value
  // 若服务端未提供平均值，可用当前页作兜底
  if (stats.averageProgress === 0 && arr.length > 0) {
    stats.averageProgress = avg(arr.map((s: any) => Number(s.progress || 0)))
  }
  const grades = arr.map((s: any) => Number(s.averageGrade)).filter((n) => Number.isFinite(n))
  if ((stats.averageGrade ?? 0) === 0 && grades.length > 0) {
    stats.averageGrade = avg(grades)
  }
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

const getActivityColor = (level?: string) => {
  switch ((level || '').toLowerCase()) {
    case 'high': return 'bg-green-500'
    case 'medium': return 'bg-yellow-500'
    case 'low': return 'bg-orange-500'
    case 'inactive': return 'bg-red-500'
    default: return 'bg-gray-500'
  }
}

const getActivityText = (level?: string) => {
  switch ((level || '').toLowerCase()) {
    case 'high': return t('teacher.students.filters.activity.high')
    case 'medium': return t('teacher.students.filters.activity.medium')
    case 'low': return t('teacher.students.filters.activity.low')
    case 'inactive': return t('teacher.students.filters.activity.inactive')
    default: return t('teacher.students.table.level.unknown')
  }
}

const getRadarBadgeVariant = (classification?: string) => {
  switch ((classification || '').toUpperCase()) {
    case 'A': return 'success'
    case 'B': return 'warning'
    case 'C': return 'info'
    default: return 'danger'
  }
}

const formatRadarBadgeLabel = (classification?: string) => {
  const key = (classification || 'D').toUpperCase() as 'A' | 'B' | 'C' | 'D'
  const desc = t(`teacher.students.radar.classification.${key}`)
  return `${key} · ${desc}`
}

const formatRadarArea = (area?: number) => {
  if (!Number.isFinite(Number(area))) return '--'
  return `${Number(area).toFixed(1)}%`
}

const formatDimensionLabel = (code: string) => {
  const label = t(`shared.radarLegend.dimensions.${code}.title`) as any
  return label || code
}

const formatRadarValue = (value?: number) => {
  if (!Number.isFinite(Number(value))) return '--'
  return Number(value).toFixed(1)
}

const buildDisplayName = (user: any) => {
  if (!user) return ''
  const lastName = String(user.lastName || '').trim()
  const firstName = String(user.firstName || '').trim()
  const combined = `${lastName}${firstName}`.trim()
  if (combined) return combined
  const nickname = String(user.nickname || '').trim()
  if (nickname) return nickname
  const username = String(user.studentName || user.username || user.studentNo || '').trim()
  if (username) return username
  return `#${user.studentId ?? ''}`
}

const buildStudentIdentifier = (user: any) => {
  if (!user) return ''
  const studentNo = String(user.studentNo || '').trim()
  if (studentNo) return studentNo
  const username = String(user.username || user.studentName || '').trim()
  if (username) return username
  return String(user.studentId ?? '')
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
  if (showStudentMenu.value === studentId) {
    showStudentMenu.value = null
    return
  }
  showStudentMenu.value = studentId
  nextTick(() => { computeMenuPos(studentId); bindMenuFollowHandler() })
}
 
 const closeStudentMenu = () => {
   showStudentMenu.value = null
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

const openStudentChat = async (studentId: string) => {
  const stu = students.value.find((s: any) => s.id === studentId)
  chat.openChat(studentId, stu?.name || '', courseId)
}

const handleStudentView = (studentId: string) => {
  closeStudentMenu()
  viewStudentDetail(studentId)
}

const handleStudentMessage = (studentId: string) => {
  closeStudentMenu()
  openStudentChat(studentId)
}

const viewGrades = (studentId: string) => {
        router.push(`/teacher/students/${studentId}`)
}

// 移除模拟重置逻辑，保留空函数
const resetProgress = async (studentId: string) => {
  const ok = await confirmDialog.confirm({
    title: (t('common.confirm') as string) || '确定',
    message: (t('teacher.students.table.confirmReset') as string) || '确认重置该学生进度？'
  })
  if (!ok) return
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
  const dimCodes = ['MORAL_COGNITION','LEARNING_ATTITUDE','LEARNING_ABILITY','LEARNING_METHOD','ACADEMIC_GRADE'] as const
  const headers = [
    'student_id','name','progress','completed_lessons','total_lessons','average_grade','activity_level','study_time_per_week','last_active_at','joined_at',
    'radar_area','radar_classification',
    ...dimCodes.flatMap(c => [`${c}_score`, `${c}_rating`])
  ]
  const dimRating = (v: any) => {
    const n = Number(v)
    if (!Number.isFinite(n)) return ''
    if (n >= 85) return 'A'
    if (n >= 70) return 'B'
    if (n >= 60) return 'C'
    return 'D'
  }
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
    `"${String(s.joinedAt ?? '')}"`,
    String(s.radarArea ?? ''),
    `"${String(s.radarClassification ?? '')}"`,
    ...dimCodes.flatMap((c) => {
      const score = (s.dimensionScores || {})[c]
      return [String(score ?? ''), dimRating(score)]
    })
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
  const ok = await confirmDialog.confirm({
    title: (t('common.confirm') as string) || '确定',
    message: (t('teacher.students.table.confirmRemove') as string) || '确认移除该学生？'
  })
  if (!ok) return
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
  // headers 与后端导出保持一致（增加雷达面积/总评级 + 五维分数/评级）
  const dimCodes = ['MORAL_COGNITION','LEARNING_ATTITUDE','LEARNING_ABILITY','LEARNING_METHOD','ACADEMIC_GRADE'] as const
  const headers = [
    'student_id','name','progress','completed_lessons','total_lessons','average_grade','activity_level','study_time_per_week','last_active_at','joined_at',
    'radar_area','radar_classification',
    ...dimCodes.flatMap(c => [`${c}_score`, `${c}_rating`])
  ]
  const selected = students.value.filter(s => selectedStudents.value.includes(s.id))
  if (selected.length === 0) return
  const escape = (v: any) => `"${String(v ?? '').replace(/"/g, '""')}"`
  const dimRating = (v: any) => {
    const n = Number(v)
    if (!Number.isFinite(n)) return ''
    if (n >= 85) return 'A'
    if (n >= 70) return 'B'
    if (n >= 60) return 'C'
    return 'D'
  }
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
    escape(s.joinedAt),
    String(s.radarArea ?? ''),
    escape(s.radarClassification ?? ''),
    ...dimCodes.flatMap((c) => {
      const score = (s.dimensionScores || {})[c]
      return [String(score ?? ''), dimRating(score)]
    })
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
  const ok = await confirmDialog.confirm({
    title: (t('common.confirm') as string) || '确定',
    message: (t('teacher.students.table.confirmRemove') as string) || '确认移除所选学生？'
  })
  if (!ok) return
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
// 入课密钥设置
const showKeyModal = ref(false)
const requireKey = ref(false)
const inputKey = ref('')
const savingKey = ref(false)
const hasExistingEnrollKey = ref(false)

function openKeyModal() {
  showKeyModal.value = true
  // 预加载课程配置
  import('@/api/course.api').then(async ({ courseApi }) => {
    try {
      const res: any = await courseApi.getCourseById(Number(courseId))
      requireKey.value = Boolean((res as any)?.requireEnrollKey)
      // 以当前课程设置是否需要密钥作为是否已有历史密钥的近似判断
      hasExistingEnrollKey.value = requireKey.value === true
    } catch {}
  })
}
function closeKeyModal() {
  showKeyModal.value = false
  inputKey.value = ''
}
async function saveEnrollKey() {
  if (savingKey.value) return
  savingKey.value = true
  try {
    if (requireKey.value && !inputKey.value && !hasExistingEnrollKey.value) {
      // 没有历史密钥时需校验输入，这里简单前端校验：若无输入则提示
      uiStore.showNotification({ type: 'warning', title: t('app.notifications.warning.title') as string, message: t('teacher.students.enrollKey.placeholder') as string })
      savingKey.value = false
      return
    }
    const { courseApi } = await import('@/api/course.api')
    await courseApi.setCourseEnrollKey(courseId, requireKey.value, inputKey.value || undefined)
    uiStore.showNotification({ type: 'success', title: t('app.notifications.success.title') as string, message: t('teacher.students.enrollKey.saved') as string })
    closeKeyModal()
  } catch (e: any) {
    uiStore.showNotification({ type: 'error', title: t('app.notifications.error.title') as string, message: e?.message || t('teacher.students.enrollKey.failed') as string })
  } finally {
    savingKey.value = false
  }
}
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
  // 初始化本地防抖搜索值，避免首次渲染与输入时的抖动
  debouncedSearchQuery.value = String(searchQuery.value || '').trim().toLowerCase()
  await fetchCourseStudents()
})

// 监听筛选/分页/排序/搜索，服务端拉取（统一做 debounce，避免连发请求）
const debouncedFetchStudents = debounce(() => { fetchCourseStudents() }, 250)
const syncDebouncedSearch = debounce(() => {
  debouncedSearchQuery.value = String(searchQuery.value || '').trim().toLowerCase()
}, 200)

watch(currentPage, () => {
  debouncedFetchStudents()
})

watch([pageSize, sortBy, progressFilter, gradeFilter, activityFilter], () => {
  // 过滤/排序/每页条数改变时回到第一页，减少“空页”与无意义请求
  currentPage.value = 1
  debouncedFetchStudents()
})

watch(searchQuery, () => {
  currentPage.value = 1
  syncDebouncedSearch()
  debouncedFetchStudents()
})

onUnmounted(() => {
  document.removeEventListener('click', handleDocClick)
  unbindMenuFollowHandler()
  debouncedFetchStudents.cancel()
  syncDebouncedSearch.cancel()
})

// 菜单关闭时立即解绑跟随监听，避免后台持续触发
watch(showStudentMenu, (v) => {
  if (v) bindMenuFollowHandler()
  else unbindMenuFollowHandler()
})

function setMenuButtonRef(el: HTMLElement | null, id: string) {
  if (!id) return
  if (el) menuButtonMap.set(id, el)
  else menuButtonMap.delete(id)
}

function computeMenuPos(id: string) {
  const btn = menuButtonMap.get(id)
  if (!btn) return
  const rect = (btn as HTMLElement).getBoundingClientRect()
  const margin = 6
  const viewportW = window.innerWidth
  const viewportH = window.innerHeight
  const minWidthPx = 160
  const preferredWidth = Math.max(minWidthPx, rect.width)
  let left = rect.right - preferredWidth
  left = Math.max(8, Math.min(left, viewportW - preferredWidth - 8))
  let top = rect.bottom + margin
  const estimatedMenuH = 176 // 预估高度，避免过长
  if (top + estimatedMenuH > viewportH - 8) {
    top = Math.max(8, rect.top - margin - estimatedMenuH)
  }
  menuPos.value = { left, top, width: preferredWidth }
}
 </script> 

<style scoped>
.filters-row :where(.shadow, .shadow-sm, .shadow-md, .shadow-lg, .shadow-xl, .shadow-2xl) {
  box-shadow: none !important;
}
.filters-row :where(.ring, .ring-1, .ring-2, .ring-offset-1, .ring-offset-2) {
  --tw-ring-offset-shadow: 0 0 #0000 !important;
  --tw-ring-shadow: 0 0 #0000 !important;
  box-shadow: none !important;
}
/* 移除胶囊选择器的底色与边框，避免出现整体长方形条 */
.filters-row :where(.ui-pill--select){
  box-shadow: none !important;
}
/* 搜索输入同样移除阴影/环效果 */
.filters-row :where(.ui-pill--input){
  box-shadow: none !important;
}
</style>