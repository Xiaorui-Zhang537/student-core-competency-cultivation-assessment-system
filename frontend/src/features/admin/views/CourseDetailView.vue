<template>
  <div class="p-8" data-export-root="1">
    <PageHeader :title="courseTitle" :subtitle="`#${courseId}`">
      <template #actions>
        <div class="flex items-center gap-2">
          <Button variant="outline" @click="router.push('/admin/courses')">{{ t('common.back') || '返回' }}</Button>
          <Button v-if="activeTab === 'students'" variant="outline" :disabled="studentsLoading" @click="onExportStudentsCsv">
            {{ t('admin.tools.exportCsv') || '导出CSV' }}
          </Button>
        </div>
      </template>
    </PageHeader>

    <loading-overlay v-if="courseLoading" class="mt-4" :text="String(t('common.loading') || '加载中…')" />
    <error-state
      v-else-if="courseError"
      class="mt-4"
      :title="String(t('common.error') || '加载失败')"
      :message="courseError"
      :actionText="String(t('common.retry') || '重试')"
      @action="reloadCourse"
    />

    <div v-else class="mt-4 space-y-8">
      <!-- 概览 -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <card padding="md" tint="info" class="lg:col-span-2">
          <div class="text-xs text-subtle mb-2">{{ t('admin.courses.overview') || '概览' }}</div>
          <div class="text-2xl font-semibold">{{ courseTitle }}</div>
          <div class="text-sm text-subtle mt-1 line-clamp-2">{{ course?.description || '-' }}</div>
          <div class="mt-4 grid grid-cols-2 md:grid-cols-4 gap-3 text-sm">
            <div class="glass-badge px-3 py-2 justify-between">
              <span class="text-subtle">{{ t('admin.courses.statusLabel') || '状态' }}</span>
              <span class="font-medium">{{ course?.status || '-' }}</span>
            </div>
            <div class="glass-badge px-3 py-2 justify-between">
              <span class="text-subtle">{{ t('admin.courses.teacher') || '教师' }}</span>
              <span class="font-medium">{{ teacherName }}</span>
            </div>
            <div class="glass-badge px-3 py-2 justify-between">
              <span class="text-subtle">{{ t('admin.courses.students') || '在读' }}</span>
              <span class="font-medium">{{ activeEnrollments ?? '-' }}</span>
            </div>
            <div class="glass-badge px-3 py-2 justify-between">
              <span class="text-subtle">{{ t('admin.courses.createdAt') || '创建' }}</span>
              <span class="font-medium">{{ (course as any)?.createdAt || '-' }}</span>
            </div>
          </div>
        </card>

        <card padding="md" tint="accent">
          <div class="text-xs text-subtle mb-2">{{ t('admin.courses.quickNav') || '快速入口' }}</div>
          <div class="flex flex-col gap-2">
            <Button size="sm" variant="outline" @click="activeTab = 'content'">{{ t('admin.courses.tabs.content') || '内容' }}</Button>
            <Button size="sm" variant="outline" @click="activeTab = 'students'">{{ t('admin.courses.tabs.students') || '学生' }}</Button>
            <Button size="sm" variant="outline" @click="activeTab = 'grades'">{{ t('admin.courses.tabs.grades') || '成绩' }}</Button>
            <Button size="sm" variant="outline" @click="activeTab = 'reports'">{{ t('admin.courses.tabs.reports') || '报告' }}</Button>
          </div>
        </card>
      </div>

      <!-- tabs -->
      <card padding="sm" tint="secondary">
        <div class="flex items-center gap-2 flex-wrap">
          <Button size="sm" :variant="activeTab === 'content' ? 'solid' : 'outline'" @click="activeTab = 'content'">
            {{ t('admin.courses.tabs.content') || '内容' }}
          </Button>
          <Button size="sm" :variant="activeTab === 'students' ? 'solid' : 'outline'" @click="activeTab = 'students'">
            {{ t('admin.courses.tabs.students') || '学生' }}
          </Button>
          <Button size="sm" :variant="activeTab === 'grades' ? 'solid' : 'outline'" @click="activeTab = 'grades'">
            {{ t('admin.courses.tabs.grades') || '成绩' }}
          </Button>
          <Button size="sm" :variant="activeTab === 'reports' ? 'solid' : 'outline'" @click="activeTab = 'reports'">
            {{ t('admin.courses.tabs.reports') || '报告' }}
          </Button>
        </div>
      </card>

      <!-- Content -->
      <card v-if="activeTab === 'content'" padding="md" tint="secondary">
        <div class="flex items-center justify-between mb-3">
          <div class="text-sm font-medium">{{ t('admin.courses.contentTitle') || '课程内容' }}</div>
          <Button size="sm" variant="outline" :disabled="lessonsLoading" @click="reloadLessons">{{ t('common.refresh') || '刷新' }}</Button>
        </div>
        <loading-overlay v-if="lessonsLoading" :text="String(t('common.loading') || '加载中…')" />
        <error-state v-else-if="lessonsError" :title="String(t('common.error') || '加载失败')" :message="lessonsError" :actionText="String(t('common.retry') || '重试')" @action="reloadLessons" />
        <div v-else class="space-y-4">
          <div v-for="l in lessons" :key="l.id" class="p-4 rounded-2xl border border-white/10 bg-white/5">
            <div class="flex items-center justify-between gap-3">
              <div class="min-w-0">
                <div class="text-base font-semibold truncate">{{ l.title || `Lesson #${l.id}` }}</div>
                <div class="text-xs text-subtle line-clamp-2">{{ l.description || '-' }}</div>
              </div>
              <div class="text-xs text-subtle whitespace-nowrap">#{{ l.id }}</div>
            </div>
          </div>
          <empty-state v-if="lessons.length === 0" :title="String(t('common.empty') || '暂无数据')" />
        </div>
      </card>

      <!-- Students -->
      <card v-if="activeTab === 'students'" padding="md" tint="info">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
          <start-card :label="t('admin.courseStudents.cards.total') as string" :value="studentsTotal" tone="blue" />
          <start-card :label="t('admin.courseStudents.cards.avgProgress') as string" :value="avgProgressText" tone="emerald" />
          <start-card :label="t('admin.courseStudents.cards.avgGrade') as string" :value="avgGradeText" tone="amber" />
          <start-card :label="t('admin.courseStudents.cards.active') as string" :value="activeStudentsText" tone="violet" />
        </div>

        <filter-bar tint="secondary" align="center" :dense="false" class="mb-3 rounded-full h-19">
          <template #left>
            <div class="flex items-center gap-3 flex-wrap">
              <div class="w-72">
                <glass-search-input
                  v-model="studentSearch"
                  :placeholder="String(t('admin.courseStudents.filters.searchPlaceholder') || t('common.search') || '搜索')"
                  size="sm"
                  tint="info"
                />
              </div>

              <div class="w-auto flex items-center gap-2">
                <span class="text-xs font-medium leading-tight text-subtle">{{ t('admin.courseStudents.filters.labels.progress') || '进度' }}</span>
                <div class="w-44">
                  <glass-popover-select v-model="progressFilter" :options="progressOptions" size="sm" tint="secondary" />
                </div>
              </div>

              <div class="w-auto flex items-center gap-2">
                <span class="text-xs font-medium leading-tight text-subtle">{{ t('admin.courseStudents.filters.labels.grade') || '成绩' }}</span>
                <div class="w-44">
                  <glass-popover-select v-model="gradeFilter" :options="gradeOptions" size="sm" tint="secondary" />
                </div>
              </div>

              <div class="w-auto flex items-center gap-2">
                <span class="text-xs font-medium leading-tight text-subtle">{{ t('admin.courseStudents.filters.labels.activity') || '活跃度' }}</span>
                <div class="w-44">
                  <glass-popover-select v-model="activityFilter" :options="activityOptions" size="sm" tint="secondary" />
                </div>
              </div>

              <div class="w-auto flex items-center gap-2">
                <span class="text-xs font-medium leading-tight text-subtle">{{ t('admin.courseStudents.filters.labels.sort') || '排序' }}</span>
                <div class="w-52">
                  <glass-popover-select v-model="sortBy" :options="sortOptions" size="sm" tint="secondary" />
                </div>
              </div>
            </div>
          </template>
          <template #right>
            <Button size="sm" variant="outline" :disabled="studentsLoading" @click="reloadStudents">{{ t('common.search') || '查询' }}</Button>
          </template>
        </filter-bar>

        <loading-overlay v-if="studentsLoading" :text="String(t('common.loading') || '加载中…')" />
        <error-state v-else-if="studentsError" :title="String(t('common.error') || '加载失败')" :message="studentsError" :actionText="String(t('common.retry') || '重试')" @action="reloadStudents" />
        <div v-else>
          <glass-table>
            <template #head>
              <tr>
                <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.columns.id') || 'ID' }}</th>
                <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">{{ t('admin.courseStudents.table.student') || (t('admin.sidebar.students') || '学生') }}</th>
                <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap w-56">{{ t('admin.courseStudents.table.progress') || '进度' }}</th>
                <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap w-[260px]">{{ t('admin.courseStudents.table.radar') || '雷达' }}</th>
                <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.courseStudents.table.grade') || '成绩' }}</th>
                <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.courseStudents.table.activity') || '活跃度' }}</th>
                <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.courseStudents.table.lastActive') || '最后活跃' }}</th>
                <th class="px-6 py-3 text-right text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.courses.actions') || '操作' }}</th>
              </tr>
            </template>

            <template #body>
              <tr v-for="s in students" :key="s.studentId" class="hover:bg-white/10 transition-colors duration-150">
                <td class="px-6 py-3 text-center font-mono text-xs">{{ s.studentId }}</td>
                <td class="px-6 py-3">
                  <div class="flex items-center gap-3">
                    <user-avatar :avatar="s.avatar" :size="36">
                      <div class="w-9 h-9 rounded-full bg-gray-200 dark:bg-gray-600 ring-1 ring-white/20" />
                    </user-avatar>
                    <div class="min-w-0">
                      <div class="font-medium truncate">{{ s.studentName || `#${s.studentId}` }}</div>
                      <div class="text-xs text-subtle truncate">{{ s.studentNo || '-' }}</div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-3 text-center">
                  <div class="flex items-center justify-center max-w-[180px] mx-auto">
                    <div class="flex-1">
                      <div class="flex justify-between items-center mb-1">
                        <span class="text-sm font-medium text-gray-900 dark:text-white">
                          {{ typeof s.progress === 'number' ? `${s.progress}%` : '--' }}
                        </span>
                        <span class="text-xs text-gray-500">
                          {{ (s.completedLessons ?? '--') }}/{{ (s.totalLessons ?? '--') }}
                        </span>
                      </div>
                      <Progress
                        :value="Number(s.progress || 0)"
                        size="sm"
                        :color="Number(s.progress || 0) >= 100 ? 'success' : 'primary'"
                      />
                    </div>
                  </div>
                </td>
                <td class="px-6 py-3 min-w-[260px] text-center">
                  <div class="flex flex-col gap-2 items-center">
                    <div class="flex items-center justify-center gap-2">
                      <badge :variant="getRadarBadgeVariant(s.radarClassification)" size="sm" class="px-2 font-semibold uppercase tracking-wide">
                        {{ String(s.radarClassification || '-') }}
                      </badge>
                      <span class="text-sm font-semibold text-gray-900 dark:text-white">
                        {{ typeof s.radarArea === 'number' ? `${Number(s.radarArea).toFixed(1)}%` : '--' }}
                      </span>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-3 text-sm text-center">{{ typeof s.averageGrade === 'number' ? Number(s.averageGrade).toFixed(1) : '-' }}</td>
                <td class="px-6 py-3 text-sm text-center">{{ s.activityLevel || '-' }}</td>
                <td class="px-6 py-3 text-xs text-subtle text-center">{{ s.lastActiveAt || '-' }}</td>
                <td class="px-6 py-3 text-right">
                  <Button size="sm" variant="outline" @click="openStudent(s.studentId)">{{ t('admin.courses.viewStudent') || '查看画像' }}</Button>
                </td>
              </tr>

              <tr v-if="students.length === 0">
                <td colspan="8" class="px-6 py-6">
                  <empty-state :title="String(t('common.empty') || '暂无数据')" />
                </td>
              </tr>
            </template>
          </glass-table>
        </div>

        <pagination-bar
          class="mt-2"
          :page="studentsPage"
          :page-size="studentsPageSize"
          :total-items="studentsTotal"
          :total-pages="studentsTotalPages"
          :disabled="studentsLoading"
          @update:page="(v: number) => { studentsPage = v; reloadStudents() }"
          @update:pageSize="(v: number) => { studentsPageSize = v; studentsPage = 1; reloadStudents() }"
        />
      </card>

      <!-- Grades -->
      <div v-if="activeTab === 'grades'" class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <card padding="md" tint="warning" class="lg:col-span-2">
          <div class="flex items-center justify-between mb-3">
            <div class="text-sm font-medium">{{ t('admin.courses.gradesStatsTitle') || '成绩统计' }}</div>
            <Button size="sm" variant="outline" :disabled="gradesLoading" @click="reloadGradesStats">{{ t('common.refresh') || '刷新' }}</Button>
          </div>
          <loading-overlay v-if="gradesLoading" :text="String(t('common.loading') || '加载中…')" />
          <error-state v-else-if="gradesError" :title="String(t('common.error') || '加载失败')" :message="gradesError" :actionText="String(t('common.retry') || '重试')" @action="reloadGradesStats" />
          <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="glass-badge px-3 py-3 justify-between">
              <span class="text-subtle">{{ t('admin.courses.avgScore') || '平均分' }}</span>
              <span class="font-semibold">{{ gradeStats?.averageScore ?? '-' }}</span>
            </div>
            <div class="glass-badge px-3 py-3 justify-between">
              <span class="text-subtle">{{ t('admin.courses.submissionCount') || '提交数' }}</span>
              <span class="font-semibold">{{ gradeStats?.submissionCount ?? '-' }}</span>
            </div>
            <div class="glass-badge px-3 py-3 justify-between">
              <span class="text-subtle">{{ t('admin.courses.maxScore') || '最高分' }}</span>
              <span class="font-semibold">{{ gradeStats?.maxScore ?? '-' }}</span>
            </div>
            <div class="glass-badge px-3 py-3 justify-between">
              <span class="text-subtle">{{ t('admin.courses.minScore') || '最低分' }}</span>
              <span class="font-semibold">{{ gradeStats?.minScore ?? '-' }}</span>
            </div>
          </div>
        </card>

        <admin-distribution-panel
          :title="t('admin.courses.gradeDistTitle') || '成绩分布'"
          :subtitle="t('admin.courses.gradeDistSubtitle') || '按课程维度聚合的分布统计（后端返回区间桶）'"
          tint="accent"
          :data="gradeDistPie"
          height="360px"
        />
      </div>

      <!-- Reports -->
      <card v-if="activeTab === 'reports'" padding="md" tint="accent">
        <div class="flex items-center justify-between mb-3">
          <div class="text-sm font-medium">{{ t('admin.courses.reportsTitle') || '能力报告' }}</div>
          <Button size="sm" variant="outline" :disabled="reportsLoading" @click="reloadReports">{{ t('common.refresh') || '刷新' }}</Button>
        </div>
        <loading-overlay v-if="reportsLoading" :text="String(t('common.loading') || '加载中…')" />
        <error-state v-else-if="reportsError" :title="String(t('common.error') || '加载失败')" :message="reportsError" :actionText="String(t('common.retry') || '重试')" @action="reloadReports" />

        <div v-else>
          <glass-table>
            <template #head>
              <tr>
                <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.columns.id') || 'ID' }}</th>
                <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.sidebar.students') || '学生' }}</th>
                <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">{{ t('admin.moderation.abilityReportTitle') || '报告' }}</th>
                <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.createdAt') || '时间' }}</th>
              </tr>
            </template>
            <template #body>
              <tr v-for="r in reports" :key="r.id" class="hover:bg-white/10 transition-colors duration-150">
                <td class="px-6 py-3 text-center font-mono text-xs">{{ r.id }}</td>
                <td class="px-6 py-3 text-sm">{{ r.studentName || `#${r.studentId}` }}</td>
                <td class="px-6 py-3">
                  <div class="font-medium">{{ r.title }}</div>
                  <div class="text-xs text-subtle">{{ r.reportType }} · score: {{ r.overallScore ?? '-' }}</div>
                </td>
                <td class="px-6 py-3 text-xs text-subtle text-center">{{ r.createdAt || '-' }}</td>
              </tr>
              <tr v-if="reports.length === 0">
                <td colspan="4" class="px-6 py-6">
                  <empty-state :title="String(t('common.empty') || '暂无数据')" />
                </td>
              </tr>
            </template>
          </glass-table>
        </div>

        <pagination-bar
          class="mt-2"
          :page="reportsPage"
          :page-size="reportsPageSize"
          :total-items="reportsTotal"
          :total-pages="reportsTotalPages"
          :disabled="reportsLoading"
          @update:page="(v: number) => { reportsPage = v; reloadReports() }"
          @update:pageSize="(v: number) => { reportsPageSize = v; reportsPage = 1; reloadReports() }"
        />
      </card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import FilterBar from '@/components/ui/filters/FilterBar.vue'
import AdminDistributionPanel from '@/features/admin/components/AdminDistributionPanel.vue'
import GlassTable from '@/components/ui/tables/GlassTable.vue'
import StartCard from '@/components/ui/StartCard.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import Progress from '@/components/ui/Progress.vue'
import Badge from '@/components/ui/Badge.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { adminApi } from '@/api/admin.api'
import { teacherApi } from '@/api/teacher.api'
import { lessonApi } from '@/api/lesson.api'
import { gradeApi } from '@/api/grade.api'
import { downloadCsv } from '@/utils/download'
import type { CourseStudentPerformanceItem } from '@/types/teacher'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const courseId = String(route.params.id || '')

const activeTab = ref<'content' | 'students' | 'grades' | 'reports'>('students')

const courseLoading = ref(false)
const courseError = ref<string | null>(null)
const course = ref<any>(null)
const activeEnrollments = ref<number | null>(null)

const courseTitle = computed(() => course.value?.title || (t('admin.sidebar.courses') || '课程'))
const teacherName = computed(() => (course.value as any)?.teacherName || (course.value as any)?.teacher?.nickname || (course.value as any)?.teacher?.username || '-')

async function reloadCourse() {
  courseLoading.value = true
  courseError.value = null
  try {
    const res: any = await adminApi.getCourse(courseId)
    course.value = res?.course || res
    activeEnrollments.value = Number(res?.activeEnrollments ?? null)
  } catch (e: any) {
    courseError.value = e?.message || 'Failed to load'
  } finally {
    courseLoading.value = false
  }
}

// lessons
const lessonsLoading = ref(false)
const lessonsError = ref<string | null>(null)
const lessons = ref<any[]>([])

async function reloadLessons() {
  lessonsLoading.value = true
  lessonsError.value = null
  try {
    const res: any = await lessonApi.getLessonsByCourse(courseId)
    lessons.value = (res as any) || []
  } catch (e: any) {
    lessonsError.value = e?.message || 'Failed to load'
  } finally {
    lessonsLoading.value = false
  }
}

// students
const studentsLoading = ref(false)
const studentsError = ref<string | null>(null)
const students = ref<CourseStudentPerformanceItem[]>([])
const studentSearch = ref('')
const progressFilter = ref('')
const gradeFilter = ref('')
const activityFilter = ref('')
const sortBy = ref('name')
let studentsPage = ref(1)
let studentsPageSize = ref(20)
let studentsTotal = ref(0)
let studentsTotalPages = ref(1)
const avgProgress = ref<number | null>(null)
const avgGrade = ref<number | null>(null)
const activeStudents = ref<number | null>(null)

const avgProgressText = computed(() => (avgProgress.value == null ? '--' : `${Number(avgProgress.value).toFixed(1)}%`))
const avgGradeText = computed(() => (avgGrade.value == null ? '--' : Number(avgGrade.value).toFixed(1)))
const activeStudentsText = computed(() => (activeStudents.value == null ? '--' : Number(activeStudents.value)))

const progressOptions = computed(() => ([
  { label: String(t('admin.courseStudents.filters.progress.all') || t('common.all') || '全部'), value: '' },
  { label: String(t('admin.courseStudents.filters.progress.notStarted') || '未开始'), value: 'not-started' },
  { label: String(t('admin.courseStudents.filters.progress.inProgress') || '进行中'), value: 'in-progress' },
  { label: String(t('admin.courseStudents.filters.progress.completed') || '已完成'), value: 'completed' },
]))
const gradeOptions = computed(() => ([
  { label: String(t('admin.courseStudents.filters.grade.all') || t('common.all') || '全部'), value: '' },
  { label: String(t('admin.courseStudents.filters.grade.excellent') || '优秀'), value: 'excellent' },
  { label: String(t('admin.courseStudents.filters.grade.good') || '良好'), value: 'good' },
  { label: String(t('admin.courseStudents.filters.grade.average') || '一般'), value: 'average' },
  { label: String(t('admin.courseStudents.filters.grade.below') || '待提升'), value: 'below' },
]))
const activityOptions = computed(() => ([
  { label: String(t('admin.courseStudents.filters.activity.all') || t('common.all') || '全部'), value: '' },
  { label: String(t('admin.courseStudents.filters.activity.high') || '高'), value: 'high' },
  { label: String(t('admin.courseStudents.filters.activity.medium') || '中'), value: 'medium' },
  { label: String(t('admin.courseStudents.filters.activity.low') || '低'), value: 'low' },
  { label: String(t('admin.courseStudents.filters.activity.inactive') || '不活跃'), value: 'inactive' },
]))
const sortOptions = computed(() => ([
  { label: String(t('admin.courseStudents.filters.sort.name') || '姓名'), value: 'name' },
  { label: String(t('admin.courseStudents.filters.sort.radar') || '雷达'), value: 'radar' },
  { label: String(t('admin.courseStudents.filters.sort.progress') || '进度'), value: 'progress' },
  { label: String(t('admin.courseStudents.filters.sort.grade') || '成绩'), value: 'grade' },
  { label: String(t('admin.courseStudents.filters.sort.lastActive') || '最后活跃'), value: 'lastActive' },
  { label: String(t('admin.courseStudents.filters.sort.activity') || '活跃度'), value: 'activity' },
]))

const getRadarBadgeVariant = (classification?: string) => {
  switch ((classification || '').toUpperCase()) {
    case 'A': return 'success'
    case 'B': return 'warning'
    case 'C': return 'info'
    default: return 'danger'
  }
}

async function reloadStudents() {
  studentsLoading.value = true
  studentsError.value = null
  try {
    const res: any = await teacherApi.getCourseStudentPerformance(courseId, {
      page: studentsPage.value,
      size: studentsPageSize.value,
      search: studentSearch.value || undefined,
      sortBy: sortBy.value || undefined,
      activity: activityFilter.value || undefined,
      grade: gradeFilter.value || undefined,
      progress: progressFilter.value || undefined,
    })
    students.value = ((res?.items || []) as CourseStudentPerformanceItem[]) || []
    studentsTotal.value = Number(res?.total || 0)
    studentsTotalPages.value = Math.max(1, Math.ceil(studentsTotal.value / Math.max(1, studentsPageSize.value)))
    avgProgress.value = (typeof res?.averageProgress === 'number') ? res.averageProgress : null
    avgGrade.value = (typeof res?.averageGrade === 'number') ? res.averageGrade : null
    activeStudents.value = (typeof res?.activeStudents === 'number') ? res.activeStudents : null
  } catch (e: any) {
    studentsError.value = e?.message || 'Failed to load'
  } finally {
    studentsLoading.value = false
  }
}

function openStudent(studentId: string | number) {
  router.push(`/admin/courses/${courseId}/students/${studentId}`)
}

async function onExportStudentsCsv() {
  try {
    const blob = await teacherApi.exportCourseStudents(courseId, {
      search: studentSearch.value || undefined,
      sortBy: sortBy.value || undefined,
      activity: activityFilter.value || undefined,
      grade: gradeFilter.value || undefined,
      progress: progressFilter.value || undefined,
    })
    downloadCsv(blob as any, `course_${courseId}_students.csv`)
  } catch (e: any) {
    // 静默：导出失败不阻塞页面
    console.warn(e)
  }
}

// grades stats
const gradesLoading = ref(false)
const gradesError = ref<string | null>(null)
const gradeStats = ref<any>(null)
const gradeDistPie = computed(() => {
  const dist = gradeStats.value?.gradeDistribution
  if (!Array.isArray(dist)) return []
  return dist.map((d: any) => ({
    name: String(d?.range || d?.bucket || d?.name || d?.label || 'N/A'),
    value: Number(d?.count ?? d?.value ?? 0)
  }))
})

async function reloadGradesStats() {
  gradesLoading.value = true
  gradesError.value = null
  try {
    gradeStats.value = await gradeApi.getCourseGradeStatistics(courseId)
  } catch (e: any) {
    gradesError.value = e?.message || 'Failed to load'
  } finally {
    gradesLoading.value = false
  }
}

// reports
const reportsLoading = ref(false)
const reportsError = ref<string | null>(null)
const reports = ref<any[]>([])
const reportsPage = ref(1)
const reportsPageSize = ref(10)
const reportsTotal = ref(0)
const reportsTotalPages = ref(1)

async function reloadReports() {
  reportsLoading.value = true
  reportsError.value = null
  try {
    const res: any = await adminApi.pageAbilityReports({
      page: reportsPage.value,
      size: reportsPageSize.value,
      courseId: Number(courseId),
    })
    reports.value = res?.items || []
    reportsTotal.value = Number(res?.total || 0)
    reportsTotalPages.value = Number(res?.totalPages || 1)
  } catch (e: any) {
    reportsError.value = e?.message || 'Failed to load'
  } finally {
    reportsLoading.value = false
  }
}

watch(activeTab, (tab) => {
  if (tab === 'content' && lessons.value.length === 0 && !lessonsLoading.value) reloadLessons()
  if (tab === 'students' && students.value.length === 0 && !studentsLoading.value) reloadStudents()
  if (tab === 'grades' && !gradeStats.value && !gradesLoading.value) reloadGradesStats()
  if (tab === 'reports' && reports.value.length === 0 && !reportsLoading.value) reloadReports()
})

onMounted(async () => {
  await reloadCourse()
  await reloadStudents()
})
</script>

