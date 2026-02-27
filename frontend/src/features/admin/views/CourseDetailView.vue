<template>
  <div class="p-8" data-export-root="1">
    <PageHeader :title="courseTitle" :subtitle="`#${courseId}`">
      <template #actions>
        <div class="flex items-center gap-2">
          <Button variant="info" @click="router.push('/admin/courses')">
            <template #icon>
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                <path stroke-linecap="round" stroke-linejoin="round" d="M15 19.5 7.5 12 15 4.5" />
              </svg>
            </template>
            {{ t('common.back') || '返回' }}
          </Button>
          <Button variant="warning" :disabled="courseLoading" @click="onExportCourseZip">
            <template #icon>
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 3v12m0 0 4-4m-4 4-4-4M4 17.5v1A2.5 2.5 0 0 0 6.5 21h11a2.5 2.5 0 0 0 2.5-2.5v-1" />
              </svg>
            </template>
            {{ t('admin.courses.exportAllCourseData') || '导出课程ZIP' }}
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

    <div v-else class="mt-4 space-y-6">
      <!-- 概览（管理员增强版，只读） -->
      <card padding="md" tint="info">
          <div class="flex items-start gap-6">
            <div class="min-w-0 flex-1">
              <div class="text-xs text-subtle mb-2">{{ t('admin.courses.overview') || '概览' }}</div>
              <div class="text-2xl font-semibold">{{ courseTitle }}</div>
              <div class="text-sm text-subtle mt-1 line-clamp-3">{{ course?.description || '-' }}</div>

              <div class="mt-4 flex flex-wrap gap-2">
                <badge v-if="course?.status" size="sm" :variant="statusVariant(course?.status)">
                  {{ t('admin.courses.statusLabel') || '状态' }} · {{ statusLabel(course?.status) }}
                </badge>
                <badge v-if="(course as any)?.category" size="sm" :variant="getCategoryVariant(String((course as any)?.category))">
                  {{ t('shared.course.fields.category') || '类别' }} · {{ localizeCategory((course as any)?.category, t) }}
                </badge>
                <badge v-if="(course as any)?.difficulty" size="sm" :variant="getDifficultyVariant(String((course as any)?.difficulty))">
                  {{ t('shared.course.fields.difficulty') || '难度' }} · {{ localizeDifficulty((course as any)?.difficulty, t) }}
                </badge>
                <badge v-if="(course as any)?.requireEnrollKey != null" size="sm" :variant="(course as any)?.requireEnrollKey ? 'warning' : 'secondary'">
                  {{ t('admin.courses.enrollKey') || '入课密钥' }} · {{ (course as any)?.requireEnrollKey ? (t('common.enabled') || '开启') : (t('common.disabled') || '关闭') }}
                </badge>
              </div>

              <div class="mt-4 grid grid-cols-1 md:grid-cols-3 gap-3 text-sm">
                <div class="rounded-xl border border-white/20 dark:border-white/10 bg-white/5 glass-ultraThin glass-tint-primary shadow-sm px-3 py-2 min-w-0">
                  <div class="text-xs text-subtle">{{ t('admin.courses.teacher') || '教师' }}</div>
                  <div class="font-medium mt-1 truncate">{{ teacherName }}</div>
                </div>
                <div class="rounded-xl border border-white/20 dark:border-white/10 bg-white/5 glass-ultraThin glass-tint-info shadow-sm px-3 py-2 min-w-0">
                  <div class="text-xs text-subtle">{{ t('admin.courses.students') || '在读' }}</div>
                  <div class="font-medium mt-1 truncate">{{ activeEnrollments ?? '-' }}</div>
                </div>
                <div class="rounded-xl border border-white/20 dark:border-white/10 bg-white/5 glass-ultraThin glass-tint-warning shadow-sm px-3 py-2 min-w-0">
                  <div class="text-xs text-subtle">{{ t('admin.courses.createdAt') || '创建' }}</div>
                  <div class="font-medium mt-1 truncate">{{ createdAtText }}</div>
                </div>
              </div>

              <div class="mt-3 grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                <div class="rounded-xl border border-white/20 dark:border-white/10 bg-white/5 glass-ultraThin glass-tint-success shadow-sm px-3 py-2 min-w-0">
                  <div class="text-xs text-subtle">{{ t('admin.courses.dateRange') || '起止' }}</div>
                  <div class="font-medium mt-1 truncate">{{ courseDateRange }}</div>
                </div>
                <div class="rounded-xl border border-white/20 dark:border-white/10 bg-white/5 glass-ultraThin glass-tint-secondary shadow-sm px-3 py-2 min-w-0">
                  <div class="text-xs text-subtle">{{ t('admin.courses.cover') || '封面' }}</div>
                  <div class="font-medium mt-1 truncate">{{ (course as any)?.coverImage ? '#' + String((course as any)?.coverImage) : '-' }}</div>
                </div>
              </div>
            </div>

            <div class="shrink-0 w-56">
              <div class="rounded-2xl overflow-hidden border border-white/10 bg-white/5">
                <div v-if="(course as any)?.coverImage && coverSrc" class="w-full h-36 bg-gray-200 dark:bg-gray-700">
                  <img :src="coverSrc" class="w-full h-full object-cover" :alt="String(courseTitle)" @error="clearCoverSrc()" />
                </div>
                <div v-else class="w-full h-36 flex items-center justify-center text-xs text-subtle">
                  {{ t('admin.courses.noCover') || '无封面' }}
                </div>
              </div>
              <div class="mt-3 grid grid-cols-2 gap-2">
                <div class="rounded-xl border border-white/20 dark:border-white/10 bg-white/5 glass-ultraThin glass-tint-accent shadow-sm px-3 py-2 min-w-0">
                  <div class="text-xs text-subtle">{{ t('admin.courses.kpi.lessons') || '节次' }}</div>
                  <div class="font-semibold mt-1 truncate">{{ lessonsCountText }}</div>
                </div>
                <div class="rounded-xl border border-white/20 dark:border-white/10 bg-white/5 glass-ultraThin glass-tint-warning shadow-sm px-3 py-2 min-w-0">
                  <div class="text-xs text-subtle">{{ t('admin.courses.kpi.reports') || '报告' }}</div>
                  <div class="font-semibold mt-1 truncate">{{ reportsCountText }}</div>
                </div>
              </div>
            </div>
          </div>
      </card>

      <card padding="md" tint="secondary">
        <div class="text-xs text-subtle mb-2">{{ t('admin.courses.navigation') || '导航' }}</div>
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
          <StartCard
            v-for="opt in tabOptions"
            :key="String(opt.value)"
            :label="String(opt.label)"
            :value="opt.count ?? '--'"
            :tone="opt.tone || 'blue'"
            :icon="opt.icon"
            :clickable="true"
            :active="activeTab === opt.value"
            :dense="true"
            @click="onPickTab(String(opt.value))"
          />
        </div>
      </card>

      <!-- Content -->
      <ContentPanel
        v-if="activeTab === 'content'"
        :course-id="courseId"
        :active="activeTab === 'content'"
        @counts="onContentCounts"
      />

      <!-- Students -->
      <StudentsPanel v-if="activeTab === 'students'" :course-id="courseId" :active="activeTab === 'students'" @stats="onStudentStats" />

      <!-- Grades -->
      <GradesPanel v-if="activeTab === 'grades'" :course-id="courseId" :active="activeTab === 'grades'" @counts="onGradesCounts" />

      <!-- Reports -->
      <ReportsPanel v-if="activeTab === 'reports'" :course-id="courseId" :active="activeTab === 'reports'" @counts="onReportsCounts" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import Badge from '@/components/ui/Badge.vue'
import StartCard from '@/components/ui/StartCard.vue'
import ContentPanel from '@/features/admin/components/course/ContentPanel.vue'
import StudentsPanel from '@/features/admin/components/course/StudentsPanel.vue'
import GradesPanel from '@/features/admin/components/course/GradesPanel.vue'
import ReportsPanel from '@/features/admin/components/course/ReportsPanel.vue'
import { adminApi } from '@/api/admin.api'
import { teacherApi } from '@/api/teacher.api'
import { lessonApi } from '@/api/lesson.api'
import apiClient, { baseURL } from '@/api/config'
import { downloadBlobAsFile } from '@/utils/download'
import { getCategoryVariant, getDifficultyVariant } from '@/shared/utils/badgeColor'
import { localizeCategory, localizeDifficulty } from '@/shared/utils/localize'
import { BookOpenIcon, ChartBarIcon, DocumentTextIcon, UsersIcon } from '@heroicons/vue/24/outline'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const courseId = String(route.params.id || '')

type TabKey = 'content' | 'students' | 'grades' | 'reports'
type TabOption = {
  value: TabKey
  label: string
  description?: string
  icon?: any
  count?: number | string | null
  tone?: 'blue' | 'emerald' | 'violet' | 'amber' | 'indigo'
  disabled?: boolean
}
const activeTab = ref<TabKey>('students')

const studentsCounts = ref<{ total: number; averageProgress: number | null; averageGrade: number | null; activeStudents: number | null }>({
  total: 0,
  averageProgress: null,
  averageGrade: null,
  activeStudents: null,
})
function onStudentStats(v: { total: number; averageProgress: number | null; averageGrade: number | null; activeStudents: number | null }) {
  studentsCounts.value = { ...studentsCounts.value, ...(v || {}) }
}

const gradesCounts = ref<{ totalAssignments: number; totalStudents: number }>({ totalAssignments: 0, totalStudents: 0 })
function onGradesCounts(v: { totalAssignments: number; totalStudents: number }) {
  gradesCounts.value = { ...gradesCounts.value, ...(v || {}) }
}

const reportsCounts = ref<{ totalReports: number }>({ totalReports: 0 })
function onReportsCounts(v: { totalReports: number }) {
  reportsCounts.value = { ...reportsCounts.value, ...(v || {}) }
}

const tabOptions = computed<TabOption[]>(() => {
  return [
    {
      value: 'content',
      label: String(t('admin.courses.tabs.content') || '内容'),
      description: String(t('admin.courses.tabs.contentDesc') || '章节与资源'),
      icon: BookOpenIcon,
      count: contentCounts.value.lessonCount ? contentCounts.value.lessonCount : null,
      tone: 'violet',
    },
    {
      value: 'students',
      label: String(t('admin.courses.tabs.students') || '学生'),
      description: String(t('admin.courses.tabs.studentsDesc') || '表现与画像'),
      icon: UsersIcon,
      count: studentsCounts.value.total || null,
      tone: 'blue',
    },
    {
      value: 'grades',
      label: String(t('admin.courses.tabs.grades') || '成绩'),
      description: String(t('admin.courses.tabs.gradesDesc') || '分析与导出'),
      icon: ChartBarIcon,
      count: gradesCounts.value.totalAssignments || null,
      tone: 'amber',
    },
    {
      value: 'reports',
      label: String(t('admin.courses.tabs.reports') || '报告'),
      description: String(t('admin.courses.tabs.reportsDesc') || '能力与历史'),
      icon: DocumentTextIcon,
      count: reportsCounts.value.totalReports || null,
      tone: 'emerald',
    },
  ]
})

function onPickTab(v: string) {
  if (v !== 'content' && v !== 'students' && v !== 'grades' && v !== 'reports') return
  if (activeTab.value === v) return
  const selected = tabOptions.value.find((opt) => String(opt.value) === String(v))
  if (selected?.disabled) return
  activeTab.value = v
}

const courseLoading = ref(false)
const courseError = ref<string | null>(null)
const course = ref<any>(null)
const activeEnrollments = ref<number | null>(null)

const courseTitle = computed(() => course.value?.title || (t('admin.sidebar.courses') || '课程'))
const teacherName = computed(() => (course.value as any)?.teacherName || (course.value as any)?.teacher?.nickname || (course.value as any)?.teacher?.username || '-')

const courseDateRange = computed(() => {
  const s = (course.value as any)?.startDate
  const e = (course.value as any)?.endDate
  if (s && e) return `${s} ~ ${e}`
  if (s) return `${s} ~ -`
  if (e) return `- ~ ${e}`
  return '-'
})

const createdAtText = computed(() => {
  const raw = String((course.value as any)?.createdAt || '').trim()
  if (!raw) return '-'
  // Prefer a common readable format: YYYY-MM-DD HH:mm
  return raw.replace('T', ' ').slice(0, 16)
})

const reportsCountText = computed(() => (reportsCounts.value.totalReports ? String(reportsCounts.value.totalReports) : '--'))

const contentCounts = ref<{ lessonCount: number; chapterCount: number; materialCount: number; assignmentCount: number }>({
  lessonCount: 0,
  chapterCount: 0,
  materialCount: 0,
  assignmentCount: 0,
})

function onContentCounts(v: { lessonCount: number; chapterCount: number; materialCount: number; assignmentCount: number }) {
  contentCounts.value = { ...contentCounts.value, ...(v || {}) }
}

async function preloadTabCounts() {
  const results = await Promise.allSettled([
    lessonApi.getLessonsByCourse(courseId),
    teacherApi.getCourseStudentPerformance(courseId, { page: 1, size: 1 }),
    teacherApi.getCourseAnalytics(courseId),
    adminApi.pageAbilityReports({ page: 1, size: 1, courseId: Number(courseId) }),
  ])

  const lessonsRes = results[0]
  if (lessonsRes.status === 'fulfilled') {
    const raw: any = lessonsRes.value
    const list = raw?.data || raw || []
    if (Array.isArray(list)) {
      contentCounts.value.lessonCount = list.length
    }
  }

  const studentsRes = results[1]
  if (studentsRes.status === 'fulfilled') {
    const raw: any = studentsRes.value
    const total = Number(raw?.total ?? 0)
    if (Number.isFinite(total)) studentsCounts.value.total = total
  }

  const gradesRes = results[2]
  if (gradesRes.status === 'fulfilled') {
    const raw: any = gradesRes.value
    const data = raw?.data ?? raw
    const totalAssignments = Number(data?.totalAssignments ?? data?.assignmentCount ?? 0)
    if (Number.isFinite(totalAssignments)) gradesCounts.value.totalAssignments = totalAssignments
  }

  const reportsRes = results[3]
  if (reportsRes.status === 'fulfilled') {
    const raw: any = reportsRes.value
    const total = Number(raw?.total ?? 0)
    if (Number.isFinite(total)) reportsCounts.value.totalReports = total
  }
}

function normalizeStatus(v: any): string {
  const s = String(v || '').trim()
  return s.toLowerCase()
}

function statusLabel(v: any): string {
  const s = normalizeStatus(v)
  if (s === 'draft') return String(t('admin.courseStatus.draft') || 'draft')
  if (s === 'published') return String(t('admin.courseStatus.published') || 'published')
  if (s === 'archived') return String(t('admin.courseStatus.archived') || 'archived')
  return String(v || '-')
}

function statusVariant(v: any) {
  const s = normalizeStatus(v)
  if (s === 'published') return 'success'
  if (s === 'draft') return 'secondary'
  if (s === 'archived') return 'warning'
  return 'info'
}

async function reloadCourse() {
  courseLoading.value = true
  courseError.value = null
  try {
    const res: any = await adminApi.getCourse(courseId)
    course.value = res?.course || res
    activeEnrollments.value = Number(res?.activeEnrollments ?? null)
    await loadCover()
  } catch (e: any) {
    courseError.value = e?.message || 'Failed to load'
  } finally {
    courseLoading.value = false
  }
}

async function onExportCourseZip() {
  try {
    const blob = await adminApi.exportCourseDataZip({ courseId })
    downloadBlobAsFile(blob as any, `course_${courseId}_all_data.zip`)
  } catch (e: any) {
    courseError.value = e?.message || 'Export failed'
  }
}

const coverSrc = ref('')
let coverObjectUrl: string | null = null

function isHttpUrl(v?: string) {
  return !!v && /^(http|https):\/\//i.test(String(v))
}

function clearCoverSrc() {
  try {
    if (coverObjectUrl) URL.revokeObjectURL(coverObjectUrl)
  } catch {}
  coverObjectUrl = null
  coverSrc.value = ''
}

async function loadCover() {
  const v: any = (course.value as any)?.coverImage
  if (!v) {
    clearCoverSrc()
    return
  }
  if (isHttpUrl(v)) {
    clearCoverSrc()
    coverSrc.value = String(v)
    return
  }
  try {
    const blob: any = await apiClient.get(`/files/${encodeURIComponent(String(v))}/preview`, { responseType: 'blob' })
    clearCoverSrc()
    coverObjectUrl = URL.createObjectURL(blob)
    coverSrc.value = coverObjectUrl
  } catch {
    clearCoverSrc()
    coverSrc.value = `${String(baseURL || '/api').replace(/\/+$/, '')}/files/${encodeURIComponent(String(v))}/preview`
  }
}

onUnmounted(() => {
  clearCoverSrc()
})

const lessonsCountText = computed(() => (contentCounts.value.lessonCount ? String(contentCounts.value.lessonCount) : '--'))

watch(activeTab, (tab) => {
  router.replace({ path: route.path, query: { ...route.query, tab } })
})

watch(() => route.query.tab, (v) => {
  const next = String(v || '')
  if (next === 'content' || next === 'students' || next === 'grades' || next === 'reports') {
    if (activeTab.value !== next) activeTab.value = next
  }
})

onMounted(async () => {
  const initial = String(route.query.tab || '')
  if (initial === 'content' || initial === 'students' || initial === 'grades' || initial === 'reports') {
    activeTab.value = initial
  }
  await Promise.allSettled([reloadCourse(), preloadTabCounts()])
})
</script>

